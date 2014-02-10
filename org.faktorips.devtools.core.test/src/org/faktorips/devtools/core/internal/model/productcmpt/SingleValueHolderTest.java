/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

public class SingleValueHolderTest {

    @Test
    public void testValidate_msgListEmpty() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        ValueDatatype datatype = mock(ValueDatatype.class);
        when(attribute.findDatatype(project)).thenReturn(datatype);
        when(datatype.checkReadyToUse()).thenReturn(new MessageList());
        when(datatype.isParsable(anyString())).thenReturn(true);

        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.containsValue(anyString(), eq(project))).thenReturn(true);
        when(attribute.getValueSet()).thenReturn(valueSet);

        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue);
        MessageList messageList = singleValueHolder.validate(project);
        assertTrue(messageList.isEmpty());
    }

    @Test
    public void testValidate_msgList_notInValueSet() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        ValueDatatype datatype = mock(ValueDatatype.class);
        when(attribute.findDatatype(project)).thenReturn(datatype);
        when(datatype.checkReadyToUse()).thenReturn(new MessageList());
        when(datatype.isParsable(anyString())).thenReturn(true);

        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.containsValue("abc", project)).thenReturn(true);
        when(attribute.getValueSet()).thenReturn(valueSet);

        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue);
        MessageList messageList = singleValueHolder.validate(project);
        assertEquals(1, messageList.size());
        assertNotNull(messageList.getMessageByCode(AttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        singleValueHolder = new SingleValueHolder(attributeValue, "abc");
        messageList = singleValueHolder.validate(project);
        assertTrue(messageList.isEmpty());
    }

    @Test
    public void testValidate_msgList_datatypeNotFound() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue);
        MessageList messageList = singleValueHolder.validate(project);
        assertEquals(1, messageList.size());
        Message messageByCode = messageList
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND);
        assertNotNull(messageByCode);
        assertEquals(singleValueHolder.getParent(), messageByCode.getInvalidObjectProperties()[0].getObject());
        assertEquals(IAttributeValue.PROPERTY_VALUE_HOLDER, messageByCode.getInvalidObjectProperties()[0].getProperty());
        assertEquals(singleValueHolder, messageByCode.getInvalidObjectProperties()[1].getObject());
        assertEquals(IValueHolder.PROPERTY_VALUE, messageByCode.getInvalidObjectProperties()[1].getProperty());

    }

    @Test
    public void testValidate_msgList_notReadyToUse() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        ValueDatatype datatype = mock(ValueDatatype.class);
        when(attribute.findDatatype(project)).thenReturn(datatype);
        when(datatype.checkReadyToUse()).thenReturn(new MessageList(Message.newError("", "")));

        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue);
        MessageList messageList = singleValueHolder.validate(project);
        assertEquals(1, messageList.size());
        Message messageByCode = messageList
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_IS_INVALID);
        assertNotNull(messageByCode);
        assertEquals(singleValueHolder.getParent(), messageByCode.getInvalidObjectProperties()[0].getObject());
        assertEquals(IAttributeValue.PROPERTY_VALUE_HOLDER, messageByCode.getInvalidObjectProperties()[0].getProperty());
        assertEquals(singleValueHolder, messageByCode.getInvalidObjectProperties()[1].getObject());
        assertEquals(IValueHolder.PROPERTY_VALUE, messageByCode.getInvalidObjectProperties()[1].getProperty());
    }

    @Test
    public void testValidate_msgList_notParsable() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        ValueDatatype datatype = mock(ValueDatatype.class);
        when(attribute.findDatatype(project)).thenReturn(datatype);
        when(datatype.checkReadyToUse()).thenReturn(new MessageList());

        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue);
        MessageList messageList = singleValueHolder.validate(project);
        assertEquals(1, messageList.size());
        Message messageByCode = messageList
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE);
        assertNotNull(messageByCode);
        assertEquals(singleValueHolder.getParent(), messageByCode.getInvalidObjectProperties()[0].getObject());
        assertEquals(IAttributeValue.PROPERTY_VALUE_HOLDER, messageByCode.getInvalidObjectProperties()[0].getProperty());
        assertEquals(singleValueHolder, messageByCode.getInvalidObjectProperties()[1].getObject());
        assertEquals(IValueHolder.PROPERTY_VALUE, messageByCode.getInvalidObjectProperties()[1].getProperty());
    }

    @Test
    public void testValidate_msgList_MultilingualInvalid() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attribute.isMultilingual()).thenReturn(true);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);
        IIpsProjectProperties projectProperties = mock(IIpsProjectProperties.class);
        when(project.getReadOnlyProperties()).thenReturn(projectProperties);
        when(projectProperties.getSupportedLanguages()).thenReturn(new HashSet<ISupportedLanguage>());

        ValueDatatype datatype = mock(ValueDatatype.class);
        when(attribute.findDatatype(project)).thenReturn(datatype);
        when(datatype.checkReadyToUse()).thenReturn(new MessageList());
        when(datatype.isParsable(anyString())).thenReturn(true);

        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.containsValue(anyString(), eq(project))).thenReturn(true);
        when(attribute.getValueSet()).thenReturn(valueSet);

        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue, "Versicherung");
        MessageList messageList = singleValueHolder.validate(project);
        assertEquals(1, messageList.size());
        Message messageByCode = messageList.getMessageByCode(AttributeValue.MSGCODE_INVALID_VALUE_TYPE);
        assertNotNull(messageByCode);
        assertEquals(singleValueHolder.getParent(), messageByCode.getInvalidObjectProperties()[0].getObject());
        assertEquals(IAttributeValue.PROPERTY_VALUE_HOLDER, messageByCode.getInvalidObjectProperties()[0].getProperty());
        assertEquals(singleValueHolder, messageByCode.getInvalidObjectProperties()[1].getObject());
        assertEquals(IValueHolder.PROPERTY_VALUE, messageByCode.getInvalidObjectProperties()[1].getProperty());

        when(attribute.isMultilingual()).thenReturn(false);

        singleValueHolder = new SingleValueHolder(attributeValue, new InternationalStringValue());
        messageList = singleValueHolder.validate(project);
        assertEquals(1, messageList.size());
        messageByCode = messageList.getMessageByCode(AttributeValue.MSGCODE_INVALID_VALUE_TYPE);
        assertNotNull(messageByCode);
        assertEquals(singleValueHolder.getParent(), messageByCode.getInvalidObjectProperties()[0].getObject());
        assertEquals(IAttributeValue.PROPERTY_VALUE_HOLDER, messageByCode.getInvalidObjectProperties()[0].getProperty());
        assertEquals(singleValueHolder, messageByCode.getInvalidObjectProperties()[1].getObject());
        assertEquals(IValueHolder.PROPERTY_VALUE, messageByCode.getInvalidObjectProperties()[1].getProperty());
    }

    @Test
    public void testGetValueType() {
        IAttributeValue attributeValue = mock(IAttributeValue.class);

        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue, "abc");
        assertEquals(ValueType.STRING, singleValueHolder.getValueType());

        singleValueHolder = new SingleValueHolder(attributeValue, new StringValue("abc"));
        assertEquals(ValueType.STRING, singleValueHolder.getValueType());

        singleValueHolder = new SingleValueHolder(attributeValue, new InternationalStringValue());
        assertEquals(ValueType.INTERNATIONAL_STRING, singleValueHolder.getValueType());
    }
}
