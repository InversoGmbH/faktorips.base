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

package org.faktorips.devtools.core.model.enums;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.enums.Messages;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class <code>IEnumContent</code> which are also
 * used in the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class EnumContentValidations {

    /**
     * Validates whether the given package equals the enum content package specification in the
     * given enum type.
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumContent The enum content that might be invalid or <code>null</code> if that
     *            information cannot be supported.
     * @param enumType The enum type the enum content is build upon.
     * @param enumContentPackageFragmentQualifiedName The qualified name of the package the enum
     *            content to be validated is stored in.
     * 
     * @throws NullPointerException If <code>validationMessageList</code>, <code>enumType</code> or
     *             <code>enumContentPackageFragmentQualifiedName</code> is <code>null</code>.
     */
    public static void validatePackageFragment(MessageList validationMessageList,
            IEnumContent enumContent,
            IEnumType enumType,
            String enumContentPackageFragmentQualifiedName) {

        ArgumentCheck
                .notNull(new Object[] { validationMessageList, enumType, enumContentPackageFragmentQualifiedName });

        String enumContentPackageSpecification = enumType.getEnumContentPackageFragment();
        if (!(enumContentPackageFragmentQualifiedName.equals(enumContentPackageSpecification))) {
            String text = NLS.bind(Messages.EnumContent_PackageFragmentNotCorrect, enumType.getQualifiedName());
            Message message;
            if (enumContent == null) {
                message = new Message(IEnumContent.MSGCODE_ENUM_CONTENT_PACKAGE_FRAGMENT_NOT_CORRECT, text,
                        Message.ERROR);
            } else {
                message = new Message(IEnumContent.MSGCODE_ENUM_CONTENT_PACKAGE_FRAGMENT_NOT_CORRECT, text,
                        Message.ERROR, enumContent);
            }
            validationMessageList.add(message);
        }
    }

    /**
     * Validates the enum type property of the given enum content.
     * <p>
     * Appropriate validation messages will be added to the given message list if:
     * <ul>
     * <li>The qualified name of the enum type equals an empty string (enum type is missing)
     * <li>The enum type is specified but does not exist
     * <li>The enum type does exist but its values are defined in the model
     * <li>The enum type does exist but is abstract
     * </ul>
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumContent The enum content that might be invalid or <code>null</code> if that
     *            information cannot be supported.
     * @param enumTypeQualifiedName The qualified name of the enum type this enum content is based
     *            upon.
     * @param ipsProject The ips object path of this ips project will be searched.
     * 
     * @throws CoreException If an error occurs while searching for the enum type.
     * @throws NullPointerException If <code>validationMessageList</code>,
     *             <code>enumTypeQualifiedName</code> or <code>ipsProject</code> is
     *             <code>null</code>.
     */
    public static void validateEnumType(MessageList validationMessageList,
            IEnumContent enumContent,
            String enumTypeQualifiedName,
            IIpsProject ipsProject) throws CoreException {

        ArgumentCheck.notNull(new Object[] { validationMessageList, enumTypeQualifiedName, ipsProject });

        String text;
        ObjectProperty[] objectProperties = (enumContent != null) ? new ObjectProperty[] { new ObjectProperty(
                enumContent, IEnumContent.PROPERTY_ENUM_TYPE) } : new ObjectProperty[0];

        // Enum type missing?
        if (enumTypeQualifiedName.equals("")) {
            text = Messages.EnumContent_EnumTypeMissing;
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_MISSING, text,
                    Message.ERROR, objectProperties));
            return;
        }

        // Enum type exists?
        IEnumType enumTypeRef = ipsProject.findEnumType(enumTypeQualifiedName);
        if (enumTypeRef == null) {
            text = NLS.bind(Messages.EnumContent_EnumTypeDoesNotExist, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST, text,
                    Message.ERROR, objectProperties));
            return;
        }

        // Values are part of model?
        if (enumTypeRef.getValuesArePartOfModel()) {
            text = NLS.bind(Messages.EnumContent_ValuesArePartOfModel, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_MODEL, text,
                    Message.ERROR, objectProperties));
        }

        // Enum type abstract?
        if (enumTypeRef.isAbstract()) {
            text = NLS.bind(Messages.EnumContent_EnumTypeIsAbstract, enumTypeQualifiedName);
            validationMessageList.add(new Message(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT, text,
                    Message.ERROR, objectProperties));
        }
    }

    /**
     * Validates the number of referenced enum attributes. This number is invalid if it does not
     * correspond to the number of enum attributes in the referenced enum type.
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumContent The enumContent that might be invalid.
     * 
     * @throws CoreException If an error occurs while searching for the enum type referenced by the
     *             given enum content.
     * @throws NullPointerException If <code>validationMessageList</code> or
     *             <code>enumContent</code> is <code>null</code>.
     */
    public static void validateReferencedEnumAttributesCount(MessageList validationMessageList, IEnumContent enumContent)
            throws CoreException {

        ArgumentCheck.notNull(new Object[] { validationMessageList, enumContent });

        IEnumType enumType = enumContent.findEnumType();
        if (enumType == null) {
            return;
        }

        if (enumType.getEnumAttributesCount(true) != enumContent.getReferencedEnumAttributesCount()) {
            String text = NLS.bind(Messages.EnumContent_ReferencedEnumAttributesCountInvalid, enumType
                    .getQualifiedName());
            Message validationMessage = new Message(
                    IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID, text, Message.ERROR,
                    new ObjectProperty[] { new ObjectProperty(enumContent,
                            IEnumContent.PROPERTY_REFERENCED_ENUM_ATTRIBUTES_COUNT) });
            validationMessageList.add(validationMessage);
        }
    }

    /** Prohibits initialization. */
    private EnumContentValidations() {

    }

}
