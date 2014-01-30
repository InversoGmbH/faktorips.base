/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class AttributeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IType type;
    private IAttribute attribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "Product");
        attribute = ((IProductCmptType)type).newProductCmptTypeAttribute();
    }

    @Test
    public void testValidate_defaultNotInValueset() throws Exception {
        IProductCmptTypeAttribute attributeWithValueSet = ((IProductCmptType)type).newProductCmptTypeAttribute();
        attributeWithValueSet.setDatatype(Datatype.INTEGER.getQualifiedName());
        attributeWithValueSet.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attributeWithValueSet.getValueSet();
        range.setLowerBound("0");
        range.setUpperBound("10");
        range.setStep("1");
        attributeWithValueSet.setDefaultValue("1");
        MessageList ml = attributeWithValueSet.validate(attributeWithValueSet.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));

        attributeWithValueSet.setDefaultValue("100");
        ml = attributeWithValueSet.validate(attributeWithValueSet.getIpsProject());
        Message msg = ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET);
        assertNotNull(msg);
        assertEquals(Message.WARNING, msg.getSeverity());

        attributeWithValueSet.setDefaultValue(null);
        ml = attributeWithValueSet.validate(attributeWithValueSet.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_IN_VALUESET));
    }

    @Test
    public void testValidate_defaultNotParsableUnknownDatatype() throws Exception {
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setDefaultValue("1");

        MessageList ml = attribute.validate(attribute.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE));

        attribute.setDatatype("a");
        ml = attribute.validate(attribute.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE));
    }

    @Test
    public void testValidate_defaultNotParsableInvalidDatatype() throws Exception {
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        MessageList ml = attribute.validate(attribute.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE));

        attribute.setDatatype("abc");
        ml = attribute.validate(attribute.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE));
    }

    @Test
    public void testValidate_valueNotParsable() throws Exception {
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setDefaultValue("1");
        MessageList ml = attribute.validate(attribute.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_VALUE_NOT_PARSABLE));

        attribute.setDefaultValue("a");
        ml = attribute.validate(attribute.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_VALUE_NOT_PARSABLE));
    }

    @Test
    public void testValidate_invalidAttributeName() throws Exception {
        attribute.setName("test");
        MessageList ml = attribute.validate(attribute.getIpsProject());
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_INVALID_ATTRIBUTE_NAME));

        attribute.setName("a.b");
        ml = attribute.validate(attribute.getIpsProject());
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_INVALID_ATTRIBUTE_NAME));
    }

    @Test
    public void testValidate_OverwrittenAttributeHasDifferentDatatype() throws Exception {
        attribute.setName("name");
        attribute.setDatatype("String");
        attribute.setOverwrite(true);

        MessageList ml = attribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_DATATYPE));

        IProductCmptType supertype = newProductCmptType(ipsProject, "sup.SuperType");
        type.setSupertype(supertype.getQualifiedName());
        IProductCmptTypeAttribute superAttr = supertype.newProductCmptTypeAttribute();
        superAttr.setName("name");
        superAttr.setDatatype("Integer");

        ml = attribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_DATATYPE));

        attribute.setDatatype(superAttr.getDatatype());
        ml = attribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_DATATYPE));
    }

    @Test
    public void testValidate_OverwrittenAttributeHasDifferentModifier() throws Exception {
        attribute.setName("name");
        attribute.setDatatype("String");
        attribute.setModifier(Modifier.PUBLIC);
        attribute.setOverwrite(true);

        MessageList ml = attribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_MODIFIER));

        IProductCmptType supertype = newProductCmptType(ipsProject, "sup.SuperType");
        type.setSupertype(supertype.getQualifiedName());
        IProductCmptTypeAttribute superAttr = supertype.newProductCmptTypeAttribute();
        superAttr.setName("name");
        superAttr.setDatatype("String");
        superAttr.setModifier(Modifier.PUBLISHED);

        ml = attribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_MODIFIER));

        attribute.setModifier(superAttr.getModifier());
        ml = attribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_MODIFIER));
    }

    @Test
    public void testSetName() {
        testPropertyAccessReadWrite(Attribute.class, IIpsElement.PROPERTY_NAME, attribute, "newName");
    }

    @Test
    public void testSetValueDatatype() {
        testPropertyAccessReadWrite(Attribute.class, IAttribute.PROPERTY_DATATYPE, attribute, "newDatatype");
    }

    @Test
    public void testFindValueDatatype() throws CoreException {
        attribute.setDatatype(Datatype.BOOLEAN.getName());
        assertEquals(Datatype.BOOLEAN, attribute.findDatatype(ipsProject));
        attribute.setDatatype("unkown");
        assertNull(attribute.findDatatype(ipsProject));
    }

    @Test
    public void testSetDefaultValue() {
        testPropertyAccessReadWrite(Attribute.class, IAttribute.PROPERTY_DEFAULT_VALUE, attribute, "newDefault");
    }

    @Test
    public void testInitFromXml() {
        IProductCmptTypeAttribute attr = ((IProductCmptType)type).newProductCmptTypeAttribute();
        Element rootEl = getTestDocument().getDocumentElement();

        // product attribute
        attr.setModifier(Modifier.PUBLISHED);
        attr.initFromXml(XmlUtil.getElement(rootEl, Attribute.TAG_NAME, 0));
        assertEquals("rate", attr.getName());
        assertEquals(Modifier.PUBLIC, attr.getModifier());
        assertEquals("Integer", attr.getDatatype());
    }

    @Test
    public void testToXml() {
        attribute.setName("a1");
        attribute.setDefaultValue("newDefault");
        attribute.setModifier(Modifier.PUBLIC);
        attribute.setDatatype("Date");

        Element el = attribute.toXml(newDocument());

        IAttribute copy = ((IProductCmptType)type).newProductCmptTypeAttribute();
        copy.initFromXml(el);
        assertEquals(attribute.getName(), copy.getName());
        assertEquals(attribute.getModifier(), copy.getModifier());
        assertEquals(attribute.getDatatype(), copy.getDatatype());
        assertEquals(attribute.getDefaultValue(), copy.getDefaultValue());

        // test null as default value
        attribute.setDefaultValue(null);
        el = attribute.toXml(newDocument());
        copy.initFromXml(el);
        assertNull(copy.getDefaultValue());
    }

}
