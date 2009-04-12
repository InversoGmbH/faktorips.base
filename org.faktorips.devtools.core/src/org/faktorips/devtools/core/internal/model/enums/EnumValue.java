/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumValue</code>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumValue
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumValue extends BaseIpsObjectPart implements IEnumValue {

    /** Collection containing all enum attribute values that belong to this enum value object. */
    private IpsObjectPartCollection enumAttributeValues;

    /**
     * Creates a new <code>EnumValue</code>.
     * 
     * @param parent The enum value container this enum value belongs to.
     * @param id A unique id for this enum value.
     */
    public EnumValue(IEnumValueContainer parent, int id) {
        super(parent, id);
        this.descriptionChangable = false;
        this.enumAttributeValues = new IpsObjectPartCollection(this, EnumAttributeValue.class,
                IEnumAttributeValue.class, IEnumAttributeValue.XML_TAG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<IEnumAttributeValue> getEnumAttributeValues() {
        List<IEnumAttributeValue> attributeValuesList = new ArrayList<IEnumAttributeValue>();
        IIpsObjectPart[] parts = enumAttributeValues.getParts();
        for (IIpsObjectPart currentObjectPart : parts) {
            attributeValuesList.add((IEnumAttributeValue)currentObjectPart);
        }

        return attributeValuesList;
    }

    /**
     * {@inheritDoc}
     */
    public IEnumAttributeValue newEnumAttributeValue() throws CoreException {
        return (IEnumAttributeValue)newPart(IEnumAttributeValue.class);
    }

    /**
     * {@inheritDoc}
     */
    public int moveEnumAttributeValue(IEnumAttributeValue enumAttributeValue, boolean up) {
        ArgumentCheck.notNull(enumAttributeValue);
        if (!(getEnumAttributeValues().contains(enumAttributeValue))) {
            throw new NoSuchElementException();
        }

        int index = -1;
        for (IEnumAttributeValue currentEnumAttributeValue : getEnumAttributeValues()) {
            index++;
            if (currentEnumAttributeValue == enumAttributeValue) {
                break;
            }
        }

        // Return if element is already the first / last one
        if (up) {
            if (index == 0) {
                return index;
            }
        } else {
            if (index == getEnumAttributeValuesCount() - 1) {
                return index;
            }
        }

        // Perform the moving
        int[] newIndex = enumAttributeValues.moveParts(new int[] { index }, up);

        return newIndex[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        // Number enum attribute values must match number enum attributes of the enum type
        IEnumValueContainer enumValueContainer = getEnumValueContainer();
        int numberEnumAttributes = (enumValueContainer instanceof IEnumType) ? ((IEnumType)enumValueContainer)
                .getEnumAttributesCount(true) : ((IEnumContent)enumValueContainer).getReferencedEnumAttributesCount();
        if (numberEnumAttributes != getEnumAttributeValuesCount()) {
            String text = NLS.bind(Messages.EnumValue_NumberAttributeValuesDoesNotCorrespondToNumberAttributes,
                    enumValueContainer.getQualifiedName());
            Message validationMessage = new Message(
                    MSGCODE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES, text, Message.ERROR, this);
            list.add(validationMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getEnumAttributeValuesCount() {
        return enumAttributeValues.size();
    }

    /**
     * {@inheritDoc}
     */
    public IEnumValueContainer getEnumValueContainer() {
        return (IEnumValueContainer)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public IEnumAttributeValue findEnumAttributeValue(IEnumAttribute enumAttribute) throws CoreException {
        if (enumAttribute == null) {
            return null;
        }
        IEnumType enumType = getEnumValueContainer().findEnumType();
        ArgumentCheck.isTrue(enumAttribute.getEnumType() == enumType);

        for (IEnumAttributeValue currentEnumAttributeValue : getEnumAttributeValues()) {
            IEnumAttribute currentReferencedEnumAttribute = currentEnumAttributeValue.findEnumAttribute();
            if (currentReferencedEnumAttribute != null) {
                if (currentReferencedEnumAttribute.getName().equals(enumAttribute.getName())) {
                    return currentEnumAttributeValue;
                }
            }
        }

        return null;
    }

}
