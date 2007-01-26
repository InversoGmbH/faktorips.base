/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 */
public class EnumValueSetTest extends AbstractIpsPluginTest {
    
    private DefaultEnumType gender;
    
    private IPolicyCmptType type;
    
    private IAttribute attr;
    private IConfigElement ce;

    private IIpsProject ipsProject;
    private IProductCmptGeneration generation;
    

    protected void setUp() throws Exception {
    	super.setUp();
        gender = new DefaultEnumType("Gender", DefaultEnumValue.class);
        new DefaultEnumValue(gender, "male");
        new DefaultEnumValue(gender, "female");
        
        ipsProject = super.newIpsProject("TestProject");
        type = newPolicyCmptType(ipsProject, "test.Base");
        attr = type.newAttribute();
        attr.setName("attr");
        attr.setDatatype(Datatype.MONEY.getQualifiedName());
        type.getIpsSrcFile().save(true, null);
        
        IProductCmpt cmpt = newProductCmpt(ipsProject, "test.Product");
        cmpt.setPolicyCmptType(type.getQualifiedName());
        generation = (IProductCmptGeneration)cmpt.newGeneration(new GregorianCalendar(20006, 4, 26));
        
        ce = generation.newConfigElement();
        ce.setPcTypeAttribute("attr");
    }

    public void testContainsValue() {
        EnumValueSet set = new EnumValueSet(ce, 1);
        set.addValue("10EUR");
        set.addValue("20EUR");
        set.addValue("30EUR");
        assertTrue(set.containsValue("10EUR"));
        assertTrue(set.containsValue("10 EUR"));
        assertFalse(set.containsValue("15 EUR"));
        assertFalse(set.containsValue("abc"));
        
        set.addValue(null);
        assertTrue(set.containsValue(null));
        
        MessageList list = new MessageList();
        set.containsValue("15 EUR", list, null, null);
        assertTrue(list.containsErrorMsg());
        
        list.clear();
        set.containsValue("10 EUR", list, null, null);
        assertFalse(list.containsErrorMsg());
    }
    
    public void testContainsValueInvalidSet() {
        EnumValueSet set = new EnumValueSet(ce, 1);
        set.addValue("1");
        MessageList list = new MessageList();
        set.containsValue("10 EUR", list, null, null);
        assertNotNull(list.getMessageByCode(IEnumValueSet.MSGCODE_VALUE_NOT_CONTAINED));
        
        list.clear();
        set.addValue("10EUR");
        set.containsValue("10 EUR", list, null, null);
        assertNull(list.getMessageByCode(IEnumValueSet.MSGCODE_VALUE_NOT_CONTAINED));
    }
    
    public void testContainsValueSet() throws Exception {
    	EnumValueSet superset = new EnumValueSet(ce, 50);
    	superset.addValue("1EUR");
    	superset.addValue("2EUR");
    	superset.addValue("3EUR");
    	
    	EnumValueSet subset = new EnumValueSet(ce, 100);
    	assertTrue(superset.containsValueSet(subset));
    	
    	subset.addValue("1EUR");
    	assertTrue(superset.containsValueSet(subset));

    	subset.addValue("2EUR");
    	subset.addValue("3EUR");
    	assertTrue(superset.containsValueSet(subset));

    	MessageList list = new MessageList();
    	superset.containsValueSet(subset, list, null, null);
    	assertFalse(list.containsErrorMsg());

    	subset.addValue("4EUR");
    	assertFalse(superset.containsValueSet(subset));
    	
    	list.clear();
    	superset.containsValueSet(subset, list, null, null);
    	assertTrue(list.containsErrorMsg());

    	subset.removeValue("4EUR");
    	subset.addValue(null);
    	assertFalse(superset.containsValueSet(subset));

    	superset.addValue(null);
    	assertTrue(superset.containsValueSet(subset));
    	
        IAttribute attr2 = type.newAttribute();
        attr2.setName("attr2");
        attr2.setDatatype(Datatype.STRING.getQualifiedName());
        type.getIpsSrcFile().save(true, null);
        
    	IConfigElement ce2 = generation.newConfigElement();
        ce2.setPcTypeAttribute("attr2");
        
    	subset = new EnumValueSet(ce2, 50);
    	subset.addValue("2EUR");
    	
    	list.clear();
    	assertFalse(superset.containsValueSet(subset, list, null, null));
    	assertNotNull(list.getMessageByCode(IValueSet.MSGCODE_DATATYPES_NOT_MATCHING));
    }

    public void testAddValue() {
        IEnumValueSet set = new EnumValueSet(ce, 1);
        set.addValue("one");
        set.addValue("two");
        assertEquals(2, set.getValues().length);
        assertEquals("one", set.getValue(0));
    }

    public void testRemoveValue() {
        IEnumValueSet set = new EnumValueSet(ce, 1);
        set.addValue("one");
        set.addValue("two");
        assertEquals(2, set.getValues().length);
        set.removeValue(0);
        assertEquals("two", set.getValue(0));
    }

    public void testGetValue() {
        IEnumValueSet set = new EnumValueSet(ce, 1);
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("two", set.getValue(2));

    }

    public void testSetValue() {
        IEnumValueSet set = new EnumValueSet(ce, 1);
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        set.setValue(1, "anderstwo");
        assertEquals("anderstwo", set.getValue(1));
    }

    public void testCreateFromXml() {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();
        Element element = XmlUtil.getFirstElement(root);
        IEnumValueSet set = new EnumValueSet(ce, 1);
        set.initFromXml(element);
        assertEquals("one", set.getValue(0));
        assertEquals("two", set.getValue(1));
        assertEquals("three", set.getValue(2));

    }

    public void testToXml() {
        EnumValueSet set = new EnumValueSet(ce, 1);
        set.addValue("one");
        set.addValue("two");
        set.addValue("two");
        Element element = set.toXml(this.newDocument());
        IEnumValueSet set2 = new EnumValueSet(ce, 1);
        set2.initFromXml(element);
        assertEquals("one", set2.getValue(0));
        assertEquals("two", set2.getValue(1));
        assertEquals("two", set2.getValue(2));

    }

    public void testValidate() throws Exception {
        EnumValueSet set = new EnumValueSet(ce, 1);
        MessageList list = new MessageList();
        set.validate(list);
        assertEquals(0, list.getNoOfMessages());

        set.addValue("2EUR");
        set.validate(list);
        assertEquals(0, list.getNoOfMessages());

        set.addValue("2w");
        set.validate(list);
        assertEquals(1, list.getNoOfMessages());

        assertFalse(list.getMessagesFor("2w").isEmpty());
        set.removeValue("2w");
        set.addValue("2EUR");
        list.clear();
        set.validate(list);
        assertEquals(2, list.getNoOfMessages());
        assertEquals(list.getMessage(0).getCode(), IEnumValueSet.MSGCODE_DUPLICATE_VALUE);
        
        list.clear();
        set.removeValue("2EUR");
        set.addValue(null);
        set.validate(list);
        assertEquals(0, list.getNoOfMessages());
        
        set.addValue(null);
        set.validate(list);
        assertNotNull(list.getMessageByCode(IEnumValueSet.MSGCODE_DUPLICATE_VALUE));
        
        set.removeValue(null);
		ValueDatatype[] vds = ipsProject.getValueDatatypes(false);
		ArrayList vdlist = new ArrayList();
		vdlist.addAll(Arrays.asList(vds));
		vdlist.add(new PrimitiveIntegerDatatype());
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed((ValueDatatype[])vdlist.toArray(new ValueDatatype[vdlist.size()]));
        ipsProject.setProperties(properties);
        
        IAttribute attr = ce.findPcTypeAttribute();
        attr.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        attr.getIpsObject().getIpsSrcFile().save(true, null);

        list.clear();
        set.validate(list);
        assertNotNull(list.getMessageByCode(IEnumValueSet.MSGCODE_VALUE_NOT_PARSABLE));
        
        set.removeValue(0);
        set.removeValue(0);
        set.addValue("1");
        set.addValue(null);
        list.clear();
        set.validate(list);
        assertNotNull(list.getMessageByCode(IEnumValueSet.MSGCODE_NULL_NOT_SUPPORTED));
        
    }

    public void testGetValues() {
        EnumValueSet set = new EnumValueSet(ce, 50);
        String[] values = set.getValues();
    	
        assertEquals(0, values.length);
        
        set.addValue("1");        
        values = set.getValues();
        assertEquals(1, values.length);
        
        set.addValue(null);
        values = set.getValues();
        assertEquals(2, values.length);
    }

    public void testGetContainsNull() {
        EnumValueSet set = new EnumValueSet(ce, 50);
        
        assertFalse(set.getContainsNull());
        
        set.setContainsNull(true);
        assertTrue(set.getContainsNull());
        
        set.setContainsNull(false);
        assertFalse(set.getContainsNull());
        
        set.addValue(null);
        assertTrue(set.getContainsNull());
    }
    
    public void testSetContainsNull() {
        EnumValueSet set = new EnumValueSet(ce, 50);
        
        assertFalse(set.getContainsNull());
        
        set.setContainsNull(true);
        
        assertTrue(set.getContainsNull());
        assertNull(set.getValue(0));
        assertEquals(1, set.size());
        
        set.setContainsNull(false);
        assertFalse(set.getContainsNull());
        assertEquals(0, set.size());
    	
    }
    
    
    class EnumDatatypePaymentMode extends AbstractDatatype implements EnumDatatype {

		/**
		 * {@inheritDoc}
		 */
		public String[] getAllValueIds(boolean includeNull) {
			return new String[]{"annual", "monthly"};
		}

		
		/**
		 * {@inheritDoc}
		 */
		public ValueDatatype getWrapperType() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isParsable(String value) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getName() {
			return null;
		}

		/**
         * {@inheritDoc}
		 */
		public String getDefaultValue() {
            return null;
        }

        /**
		 * {@inheritDoc}
		 */
		public String getQualifiedName() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isPrimitive() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isValueDatatype() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getJavaClassName() {
			return null;
		}


		public boolean isSupportingNames() {
			return false;
		}


		public String getValueName(String id) {
			throw new RuntimeException("Not supported");
		}


        public boolean isNull(String value) {
            return false;
        }


        public boolean supportsCompare() {
            return false;
        }


        public int compare(String valueA, String valueB) throws UnsupportedOperationException {
            return 0;
        }


        public boolean areValuesEqual(String valueA, String valueB) {
            return false;
        }

    }
}
