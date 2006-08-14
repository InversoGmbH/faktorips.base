/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test attribute value class. 
 * Defines an attribute value for a specific policy component class within a test case definition.
 * 
 * @author Joerg Ortmann
 */
public class TestAttributeValue  extends IpsObjectPart implements ITestAttributeValue {
	
	/* Tags */
	static final String TAG_NAME = "AttributeValue"; //$NON-NLS-1$

	private String testAttribute = ""; //$NON-NLS-1$
	
    private boolean deleted = false;
	
	private String value = ""; //$NON-NLS-1$
	
	public TestAttributeValue(IIpsObject parent, int id) {
		super(parent, id);
	}

	public TestAttributeValue(IIpsObjectPart parent, int id) {
		super(parent, id);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getTestAttribute() {
		return testAttribute;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTestAttribute(String testAttribute) {
		this.testAttribute = testAttribute;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestAttribute findTestAttribute() throws CoreException {
        if (StringUtils.isEmpty(testAttribute)) {
            return null;
        }
        ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) getParent();
        ITestPolicyCmptTypeParameter typeParam = testPolicyCmpt.getTestCase().findTestPolicyCmptTypeParameter(testPolicyCmpt);
        if (typeParam == null){
        	return null;
        }
        return typeParam.getTestAttribute(testAttribute);
	}

	/**
	 * {@inheritDoc}
	 */
    public String getValue() {
		return value;
	}

    /**
     * {@inheritDoc}
     */
	public void setValue(String newValue) {
		String oldValue = this.value;
		this.value = newValue;
		valueChanged(oldValue, newValue);
	}
	
    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		testAttribute = element.getAttribute(PROPERTY_ATTRIBUTE);
		value = ValueToXmlHelper.getValueFromElement(element, PROPERTY_VALUE);
	}

    /**
     * {@inheritDoc}
     */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_ATTRIBUTE, testAttribute);
		ValueToXmlHelper.addValueToElement(value, element, PROPERTY_VALUE);
	}  
	
    /** 
     * {@inheritDoc}
     */
    public void delete() {
        ((TestPolicyCmpt)getIpsObject()).removeTestAttributeValue(this);
        updateSrcFile();
        deleted = true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }    
    
    /** 
     * {@inheritDoc}
     */
    public Image getImage() {
		return null;
    }
    
    /**
     * This object has no parts.
     */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type: " + partType); //$NON-NLS-1$
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList messageList) throws CoreException {
		super.validateThis(messageList);
		ITestAttribute testAttr = findTestAttribute();
		if (testAttr==null) {
			String text = NLS.bind(Messages.TestAttributeValue_ValidateError_TestAttributeNotFound, getTestAttribute());
			Message msg = new Message(MSGCODE_TESTATTRIBUTE_NOT_FOUND, text, Message.ERROR, this, PROPERTY_ATTRIBUTE);
			messageList.add(msg);	
			return;
		}
		
		IAttribute attribute = testAttr.findAttribute();
		if (attribute == null){
			String text = NLS.bind(Messages.TestAttributeValue_ValidateError_AttributeNotFound, testAttr.getAttribute());
			Message msg = new Message(MSGCODE_ATTRIBUTE_NOT_FOUND, text, Message.WARNING, this, ITestAttribute.PROPERTY_ATTRIBUTE);
			messageList.add(msg);
			return;
		}
		ValidationUtils.checkValue(attribute.getDatatype(), value, this, PROPERTY_VALUE, messageList);
	}
}
