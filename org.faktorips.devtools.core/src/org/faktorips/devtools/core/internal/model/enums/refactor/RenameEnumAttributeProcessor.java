/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums.refactor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeReference;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.devtools.core.refactor.IpsSrcFileModificationSet;
import org.faktorips.util.message.MessageList;

/**
 * Refactoring processor for the "Rename Enumeration Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public class RenameEnumAttributeProcessor extends IpsRenameProcessor {

    /** Set containing all potentially referencing enumeration types (subclasses). */
    private Set<IIpsSrcFile> enumTypeSrcFiles;

    public RenameEnumAttributeProcessor(IEnumAttribute enumAttribute) {
        super(enumAttribute, enumAttribute.getName());

        /*
         * Refactoring the current default value provider attribute makes it necessary to ignore
         * this validation.
         */
        getIgnoredValidationMessageCodes()
                .add(IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST);
    }

    private IIpsSrcFile getIpsSrcFile() {
        return getEnumAttribute().getIpsSrcFile();
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        HashSet<IIpsSrcFile> result = new HashSet<IIpsSrcFile>();
        try {
            result.add(getIpsSrcFile());
            enumTypeSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.ENUM_TYPE);
            for (IIpsSrcFile ipsSrcFile : enumTypeSrcFiles) {
                result.add(ipsSrcFile);
            }
            if (!(getEnumType().isContainingValues())) {
                IEnumContent enumContent = getEnumType().findEnumContent(getIpsProject());
                if (enumContent != null) {
                    result.add(enumContent.getIpsSrcFile());
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return result;
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) throws CoreException {
        validationMessageList.add(getEnumAttribute().validate(getIpsProject()));
        validationMessageList.add(getEnumType().validate(getIpsProject()));
    }

    @Override
    protected IpsSrcFileModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreException {
        IpsSrcFileModificationSet modifications = createDefaultModifications();
        updateSubclassReferences();
        if (!(getEnumType().isAbstract())) {
            if (getEnumType().isContainingValues()) {
                updateLiteralNameReference();
            } else {
                updateEnumContentReference();
            }
        }
        updateEnumAttributeName();
        return modifications;
    }

    private void updateLiteralNameReference() {
        IEnumLiteralNameAttribute literalNameAttribute = getEnumType().getEnumLiteralNameAttribute();
        if (literalNameAttribute.getDefaultValueProviderAttribute().equals(getOriginalName())) {
            literalNameAttribute.setDefaultValueProviderAttribute(getNewName());
        }
    }

    private void updateSubclassReferences() throws CoreException {
        for (IIpsSrcFile ipsSrcFile : enumTypeSrcFiles) {
            IEnumType enumType = (IEnumType)ipsSrcFile.getIpsObject();
            boolean isSubEnumType = enumType.isSubEnumTypeOf(getEnumType(), getIpsProject());
            if (isSubEnumType) {
                IEnumAttribute inheritedAttribute = enumType.getEnumAttributeIncludeSupertypeCopies(getOriginalName());
                if (inheritedAttribute != null) {
                    inheritedAttribute.setName(getNewName());
                }
            }
        }
    }

    private void updateEnumContentReference() throws CoreException {
        IEnumContent enumContent = getEnumType().findEnumContent(getIpsProject());
        if (enumContent != null) {
            IEnumAttributeReference attributeReference = enumContent.getEnumAttributeReference(getOriginalName());
            if (attributeReference != null) {
                attributeReference.setName(getNewName());
            }
        }
    }

    private void updateEnumAttributeName() {
        getEnumAttribute().setName(getNewName());
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return true;
    }

    private IEnumType getEnumType() {
        return getEnumAttribute().getEnumType();
    }

    private IEnumAttribute getEnumAttribute() {
        return (IEnumAttribute)getIpsElement();
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.enums.refactor.RenameEnumAttributeProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameEnumAttributeProcessor_processorName;
    }

}
