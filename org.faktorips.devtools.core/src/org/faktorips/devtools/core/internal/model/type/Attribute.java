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

package org.faktorips.devtools.core.internal.model.type;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAttribute.
 * 
 * @author Jan Ortmann
 */
public abstract class Attribute extends IpsObjectPart implements IAttribute {

    final static String TAG_NAME = "Attribute"; //$NON-NLS-1$

    private String datatype = ""; //$NON-NLS-1$
    private Modifier modifier = Modifier.PUBLISHED;
    private String defaultValue = null;
    
    public Attribute(IIpsObject parent, int id) {
        super(parent, id);
        name = ""; //$NON-NLS-1$
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
    public IType getType() {
        return (IType)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, newName);
    }

    /**
     * {@inheritDoc}
     */
    public Modifier getModifier() {
        return modifier;
    }

    /**
     * {@inheritDoc}
     */
    public void setModifier(Modifier newModifer) {
        ArgumentCheck.notNull(newModifer);
        Modifier oldModifier = modifier;
        modifier = newModifer;
        valueChanged(oldModifier, newModifer);
    }

    /**
     * {@inheritDoc}
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(String newDatatype) {
        String oldDatatype = datatype;
        datatype = newDatatype;
        valueChanged(oldDatatype, newDatatype);
    }
    
    /**
     * {@inheritDoc}
     */
    public ValueDatatype findDatatype(IIpsProject project) throws CoreException {
        return project.findValueDatatype(datatype);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaultValue(String newValue) {
        String oldValue = defaultValue;
        defaultValue = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        modifier = Modifier.getModifier(element.getAttribute(PROPERTY_MODIFIER));
        if (modifier==null) {
            modifier = Modifier.PUBLISHED;
        }
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        defaultValue = ValueToXmlHelper.getValueFromElement(element, "DefaultValue"); //$NON-NLS-1$    
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name); 
        element.setAttribute(PROPERTY_DATATYPE, datatype); 
        element.setAttribute(PROPERTY_MODIFIER, modifier.getId());
        ValueToXmlHelper.addValueToElement(defaultValue, element, "DefaultValue"); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList result) throws CoreException {
        super.validateThis(result);
        IStatus status = JavaConventions.validateFieldName(name);
        if (!status.isOK()) {
            result.add(new Message(MSGCODE_INVALID_ATTRIBUTE_NAME, Messages.Attribute_msg_InvalidAttributeName + name
                    + "!", Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        ValueDatatype datatypeObject = ValidationUtils.checkValueDatatypeReference(getDatatype(), false, this,
                PROPERTY_DATATYPE, "", result); //$NON-NLS-1$
        if (datatypeObject != null) {
            validateDefaultValue(datatypeObject, result);
        } else {
            if (!StringUtils.isEmpty(defaultValue)) {
                String text = NLS.bind(Messages.Attribute_msg_DefaultNotParsable_UnknownDatatype, defaultValue);
                result.add(new Message(MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE, text, Message.WARNING, this,
                        PROPERTY_DEFAULT_VALUE)); //$NON-NLS-1$
            }
        }
    }
    
    private void validateDefaultValue(ValueDatatype valueDatatype, MessageList result) throws CoreException {
        if (!valueDatatype.isParsable(defaultValue)) {
            String defaultValueInMsg = defaultValue;
            if (defaultValue == null) {
                defaultValueInMsg = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            } else if (defaultValue.equals("")) { //$NON-NLS-1$
                defaultValueInMsg = Messages.Attribute_msg_DefaultValueIsEmptyString;
            }
            String text = NLS.bind(Messages.Attribute_msg_ValueTypeMismatch, defaultValueInMsg, getDatatype());
            result.add(new Message(MSGCODE_VALUE_NOT_PARSABLE, text, Message.ERROR, this, PROPERTY_DEFAULT_VALUE)); //$NON-NLS-1$
            return;
        }
        IValueSet valueSet = getValueSet();
        if (valueSet != null) {
            if (defaultValue!=null && !valueSet.containsValue(defaultValue)) {
                result.add(new Message(MSGCODE_DEFAULT_NOT_IN_VALUESET, NLS.bind(
                        Messages.Attribute_msg_DefaultNotInValueset, defaultValue), //$NON-NLS-1$
                        Message.WARNING, this, PROPERTY_DEFAULT_VALUE));
            }
        }
    }

    protected abstract IValueSet getValueSet() throws CoreException;
    
}
