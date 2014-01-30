/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.LocalizedStringsSet;

public class ValidationRuleMessagesPropertiesBuilder extends AbstractArtefactBuilder {

    // translated by LocalizedTextHelper
    private static final String MESSAGES_COMMENT = "MESSAGES_COMMENT";

    static final String MESSAGES_EXTENSION = "properties";

    private final Map<IFile, ValidationRuleMessagesGenerator> messageGeneratorMap;

    /**
     * This constructor setting the specified builder set and initializes a new map as
     * {@link #messageGeneratorMap}
     * 
     * @param builderSet The builder set this builder belongs to
     */
    public ValidationRuleMessagesPropertiesBuilder(StandardBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(ValidationRuleMessagesPropertiesBuilder.class));
        messageGeneratorMap = new HashMap<IFile, ValidationRuleMessagesGenerator>();
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    @Override
    public String getName() {
        return "ValidationRuleMessagesPropertiesBuilder";
    }

    /**
     * {@inheritDoc}
     * 
     * The {@link ValidationRuleMessagesPropertiesBuilder} have to check the modification state for
     * every the property files. In case of {@link IncrementalProjectBuilder#FULL_BUILD} we clear
     * every property file and try to read the existing file.
     * <p>
     * This method also creates the folder of the property file if it does not exists yet. We need
     * to do this before building because after otherwise the PDE-Builder may mark the folder as not
     * existing in the MANIFEST.MF.
     * 
     */
    @Override
    public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        super.beforeBuildProcess(project, buildKind);
        for (IIpsPackageFragmentRoot srcRoot : project.getSourceIpsPackageFragmentRoots()) {
            for (ISupportedLanguage supportedLanguage : project.getReadOnlyProperties().getSupportedLanguages()) {
                if (buildKind == IncrementalProjectBuilder.FULL_BUILD) {
                    getMessagesGenerator(srcRoot, supportedLanguage).loadMessages();
                }
                IFile propertyFile = getPropertyFile(srcRoot, supportedLanguage);
                if (propertyFile == null) {
                    continue;
                }
                createFolderIfNotThere((IFolder)propertyFile.getParent());
            }
        }
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        for (ISupportedLanguage supportedLanguage : ipsSrcFile.getIpsProject().getReadOnlyProperties()
                .getSupportedLanguages()) {
            ValidationRuleMessagesGenerator messagesGenerator = getMessagesGenerator(ipsSrcFile, supportedLanguage);
            IIpsObject ipsObject = ipsSrcFile.getIpsObject();
            if (ipsObject instanceof IPolicyCmptType) {
                messagesGenerator.generate((IPolicyCmptType)ipsObject);
            }
        }
    }

    /**
     * If the property file was modified, the {@link ValidationRuleMessagesPropertiesBuilder} have
     * to save the new property file.
     * 
     * {@inheritDoc}
     */
    @Override
    public void afterBuildProcess(IIpsProject ipsProject, int buildKind) throws CoreException {
        super.afterBuildProcess(ipsProject, buildKind);
        IIpsPackageFragmentRoot[] srcRoots = ipsProject.getSourceIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot srcRoot : srcRoots) {
            for (ISupportedLanguage supportedLanguage : ipsProject.getReadOnlyProperties().getSupportedLanguages()) {
                String comment = NLS.bind(getLocalizedText(ipsProject, MESSAGES_COMMENT), ipsProject.getName() + "/"
                        + srcRoot.getName());
                ValidationRuleMessagesGenerator messagesGenerator = getMessagesGenerator(srcRoot, supportedLanguage);
                messagesGenerator.saveIfModified(comment);
            }
        }
    }

    protected String getResourceBundleBaseName(IIpsSrcFolderEntry entry) {
        return getBuilderSet().getValidationMessageBundleBaseName(entry);
    }

    protected IFile getPropertyFile(IIpsPackageFragmentRoot root, ISupportedLanguage supportedLanguage) {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        IFolder derivedFolder = entry.getOutputFolderForDerivedJavaFiles();
        String resourceBundleBaseName = getResourceBundleBaseName(entry)
                + getMessagesFileSuffix(supportedLanguage.getLocale());
        IPath path = QNameUtil.toPath(resourceBundleBaseName).addFileExtension(MESSAGES_EXTENSION);
        IFile messagesFile = derivedFolder.getFile(path);
        return messagesFile;
    }

    private String getMessagesFileSuffix(Locale locale) {
        return "_" + locale.toString();
    }

    protected ValidationRuleMessagesGenerator getMessagesGenerator(IIpsSrcFile ipsSrcFile,
            ISupportedLanguage supportedLanguage) {
        IIpsPackageFragmentRoot root = ipsSrcFile.getIpsPackageFragment().getRoot();
        return getMessagesGenerator(root, supportedLanguage);
    }

    protected ValidationRuleMessagesGenerator getMessagesGenerator(IIpsPackageFragmentRoot root,
            ISupportedLanguage supportedLanguage) {
        IFile propertyFile = getPropertyFile(root, supportedLanguage);
        ValidationRuleMessagesGenerator messagesGenerator = messageGeneratorMap.get(propertyFile);
        if (messagesGenerator == null) {
            messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile, supportedLanguage, this);
            messageGeneratorMap.put(propertyFile, messagesGenerator);
        }
        return messagesGenerator;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE);
    }

    @Override
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        for (ISupportedLanguage supportedLanguage : ipsSrcFile.getIpsProject().getReadOnlyProperties()
                .getSupportedLanguages()) {
            ValidationRuleMessagesGenerator messagesGenerator = getMessagesGenerator(ipsSrcFile.getIpsPackageFragment()
                    .getRoot(), supportedLanguage);
            QualifiedNameType qualifiedNameType = ipsSrcFile.getQualifiedNameType();
            if (qualifiedNameType.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE) {
                messagesGenerator.deleteAllMessagesFor(qualifiedNameType.getName());
            }
        }
    }

    /**
     * The property-files built by this builder are 100% generated files.
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    @Override
    public boolean isBuildingInternalArtifacts() {
        return getBuilderSet().isGeneratePublishedInterfaces();
    }

}
