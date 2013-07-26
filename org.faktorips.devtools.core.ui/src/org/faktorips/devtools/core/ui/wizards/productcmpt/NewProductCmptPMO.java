/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.ui.commands.NewResourceNameValidator;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionPMO;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 * The presentation model object for the {@link NewProductCmptWizard}.
 * 
 * @author dirmeier
 */
public class NewProductCmptPMO extends NewProductDefinitionPMO {

    public static final String PROPERTY_SELECTED_BASE_TYPE = "selectedBaseType"; //$NON-NLS-1$

    public static final String PROPERTY_SELECTED_TYPE = "selectedType"; //$NON-NLS-1$

    public static final String PROPERTY_KIND_ID = "kindId"; //$NON-NLS-1$

    public static final String PROPERTY_VERSION_ID = "versionId"; //$NON-NLS-1$

    public static final String PROPERTY_RUNTIME_ID = "runtimeId"; //$NON-NLS-1$

    public static final String PROPERTY_NEED_VERSION_ID = "needVersionId"; //$NON-NLS-1$

    private final NewProductCmptValidator validator;

    private IProductCmptType selectedBaseType;

    private IProductCmptType selectedType;

    private Set<IProductCmptType> baseTypes = new HashSet<IProductCmptType>();

    private String kindId = StringUtils.EMPTY;

    private String versionId = StringUtils.EMPTY;

    private IProductCmpt contextProductCmpt = null;

    private IProductCmpt copyProductCmpt;

    private boolean copyValidProductCmpt;

    private List<IProductCmptType> subtypes;

    private IProductCmptGeneration addToProductCmptGeneration;

    private IProductCmptTypeAssociation addToAssociation;

    private String runtimeId = StringUtils.EMPTY;

    public NewProductCmptPMO() {
        super();
        validator = new NewProductCmptValidator(this);
    }

    @Override
    public void setIpsProject(IIpsProject ipsProject) {
        updateBaseTypeList(ipsProject);
        try {
            if (selectedBaseType != null
                    && !selectedBaseType.equals(ipsProject.findProductCmptType(selectedBaseType.getQualifiedName()))) {
                selectedBaseType = null;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        updateVersionId(ipsProject);
        updateRuntimeId(ipsProject);
        super.setIpsProject(ipsProject);
    }

    @Override
    public void setEffectiveDate(GregorianCalendar effectiveDate) {
        super.setEffectiveDate(effectiveDate);
        updateVersionId(getIpsProject());
    }

    private void updateVersionId(IIpsProject ipsProject) {
        if (ipsProject != null && getEffectiveDate() != null) {
            IProductCmptNamingStrategy namingStrategy = ipsProject.getProductCmptNamingStrategy();
            setVersionId(namingStrategy.getNextVersionId(contextProductCmpt, getEffectiveDate()));
        }
    }

    public boolean isNeedVersionId() {
        if (getIpsProject() == null) {
            return true;
        }
        IProductCmptNamingStrategy namingStrategy = getIpsProject().getProductCmptNamingStrategy();
        return namingStrategy.supportsVersionId();
    }

    /**
     * Searches all {@link IProductCmptType} in selected project and adding fills the list of base
     * types.
     * <p>
     * Every type that has either no super type or which super type is an layer supertype is added
     * as an base type.
     */
    void updateBaseTypeList(IIpsProject ipsProject) {
        if (ipsProject == null) {
            return;
        }
        baseTypes = new HashSet<IProductCmptType>();
        try {
            IIpsSrcFile[] findIpsSrcFiles = ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE);
            Set<IIpsSrcFile> concreteTypes = new HashSet<IIpsSrcFile>();
            // 1. making a list containing all NOT ABSTRACT types
            for (IIpsSrcFile ipsSrcFile : findIpsSrcFiles) {
                if (!Boolean.valueOf(ipsSrcFile.getPropertyValue(IProductCmptType.PROPERTY_ABSTRACT))) {
                    concreteTypes.add(ipsSrcFile);
                }
            }
            baseTypes = getBaseTypesFromConcreteTypes(concreteTypes, ipsProject);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private Set<IProductCmptType> getBaseTypesFromConcreteTypes(Set<IIpsSrcFile> types, IIpsProject ipsProject)
            throws CoreException {
        Set<IIpsSrcFile> superTypes = new HashSet<IIpsSrcFile>();
        Set<IProductCmptType> baseTypes = new HashSet<IProductCmptType>();
        for (IIpsSrcFile ipsSrcFile : types) {
            if (Boolean.parseBoolean(ipsSrcFile.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE))) {
                continue;
            }
            String superType = ipsSrcFile.getPropertyValue(IProductCmptType.PROPERTY_SUPERTYPE);
            IIpsSrcFile superTypeIpsSrcFile = null;
            if (StringUtils.isNotEmpty(superType)) {
                superTypeIpsSrcFile = ipsProject.findIpsSrcFile(new QualifiedNameType(superType,
                        IpsObjectType.PRODUCT_CMPT_TYPE));
            }
            if (superTypeIpsSrcFile != null
                    && !Boolean.parseBoolean(superTypeIpsSrcFile
                            .getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE))) {
                superTypes.add(superTypeIpsSrcFile);
            } else {
                baseTypes.add((IProductCmptType)ipsSrcFile.getIpsObject());
            }
        }
        if (!superTypes.isEmpty()) {
            baseTypes.addAll(getBaseTypesFromConcreteTypes(superTypes, ipsProject));
        }
        return baseTypes;
    }

    /**
     * @return Returns the baseTypes.
     */
    public List<IProductCmptType> getBaseTypes() {
        List<IProductCmptType> sortedBaseTypes = new ArrayList<IProductCmptType>(baseTypes);
        Collections.sort(sortedBaseTypes, new Comparator<IProductCmptType>() {

            @Override
            public int compare(IProductCmptType o1, IProductCmptType o2) {
                MultiLanguageSupport multiLanguageSupport = IpsPlugin.getMultiLanguageSupport();
                if (multiLanguageSupport != null) {
                    String label1 = multiLanguageSupport.getLocalizedLabel(o1);
                    String label2 = multiLanguageSupport.getLocalizedLabel(o2);
                    return label1.compareTo(label2);
                } else {
                    return o1.getName().compareTo(o2.getName());
                }
            }

        });
        return sortedBaseTypes;
    }

    /**
     * @return Returns the selectedBaseType.
     */
    public IProductCmptType getSelectedBaseType() {
        return selectedBaseType;
    }

    /**
     * @param selectedBaseType The selectedBaseType to set.
     */
    public void setSelectedBaseType(IProductCmptType selectedBaseType) {
        IProductCmptType oldSelection = this.selectedBaseType;
        this.selectedBaseType = selectedBaseType;
        updateSubtypeList();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SELECTED_BASE_TYPE, oldSelection, selectedBaseType));

    }

    /**
     * @param selectedType The selectedType to set.
     */
    public void setSelectedType(IProductCmptType selectedType) {
        IProductCmptType oldSelection = this.selectedType;
        this.selectedType = selectedType;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SELECTED_TYPE, oldSelection, selectedType));
    }

    /**
     * @return Returns the selectedType.
     */
    public IProductCmptType getSelectedType() {
        return selectedType;
    }

    public List<IProductCmptType> getSubtypes() {
        return subtypes;
    }

    private void updateSubtypeList() {
        if (isCopyValidMode()) {
            subtypes = Arrays.asList(selectedType);
            return;
        }

        ArrayList<IProductCmptType> result = new ArrayList<IProductCmptType>();
        if (selectedBaseType == null) {
            subtypes = result;
        } else {
            List<IType> subtypesList = selectedBaseType.findSubtypes(true, true, getIpsProject());
            for (IType type : subtypesList) {
                if (!type.isAbstract()) {
                    result.add((IProductCmptType)type);
                }
            }
            subtypes = result;
            if (!subtypes.isEmpty()) {
                setSelectedType(subtypes.get(0));
            } else {
                setSelectedType(null);
            }
        }
    }

    /**
     * @param name The kindId to set.
     */
    public void setKindId(String name) {
        String oldName = this.kindId;
        this.kindId = name;
        updateRuntimeId(getIpsProject());
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_KIND_ID, oldName, name));
    }

    /**
     * @return Returns the kindId.
     */
    public String getKindId() {
        return kindId;
    }

    /**
     * @param versionId The versionId to set.
     */
    public void setVersionId(String versionId) {
        String oldId = this.versionId;
        this.versionId = versionId;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_VERSION_ID, oldId, versionId));
    }

    /**
     * @return Returns the versionId.
     */
    public String getVersionId() {
        return versionId;
    }

    private void updateRuntimeId(IIpsProject ipsProject) {
        try {
            setRuntimeId(ipsProject.getProductCmptNamingStrategy().getUniqueRuntimeId(ipsProject, getName()));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        } catch (IllegalArgumentException e) {
            setRuntimeId(StringUtils.EMPTY);
        }
    }

    @Override
    public String getName() {
        if (StringUtils.isEmpty(kindId)) {
            return StringUtils.EMPTY;
        } else {
            return getIpsProject().getProductCmptNamingStrategy().getProductCmptName(kindId, versionId);
        }
    }

    public String getQualifiedName() {
        return QNameUtil.concat(getIpsPackage().getName(), getName());
    }

    /**
     * Setting the defaults for the new product component wizard.
     * <p>
     * The default package set the project, the package root and the package fragment. The default
     * package should not be null, if it is null, the method does nothing. The default type may be
     * null, if not null it is used to specify the base type as well as the current selected type,
     * if it is not abstract. The default product component may also be null. It does not change the
     * default type but is used to fill default kind id and version id.
     * 
     * @param defaultPackage Used for default project, package root and package fragment, should not
     *            be null.
     * @param defaultType The type to initialize the selected base type and selected concrete type
     * @param defaultProductCmpt a product component to initialize the version id and kind id - does
     *            not set the default type!
     */
    public void initDefaults(IIpsPackageFragment defaultPackage,
            IProductCmptType defaultType,
            IProductCmpt defaultProductCmpt) {
        try {
            if (defaultPackage == null) {
                return;
            }
            if (contextProductCmpt != null) {
                contextProductCmpt = defaultProductCmpt;
                setKindId(contextProductCmpt.getKindId().getName());
            }
            setIpsProject(defaultPackage.getIpsProject());
            setPackageRoot(defaultPackage.getRoot());
            setIpsPackage(defaultPackage);
            if (defaultType != null) {
                initDefaultType(defaultType, defaultPackage.getIpsProject());
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void initDefaultType(IProductCmptType cmptType, IIpsProject ipsProject) {
        SelectedBaseTypeVisitor selectedBaseTypeVisitor = new SelectedBaseTypeVisitor(getBaseTypes(), ipsProject);
        try {
            selectedBaseTypeVisitor.start(cmptType);
            setSelectedBaseType(selectedBaseTypeVisitor.selectedBaseType);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        setSelectedType(cmptType);
    }

    /**
     * @return Returns the validator.
     */
    @Override
    protected NewProductCmptValidator getValidator() {
        return validator;
    }

    /**
     * Sets a {@link IProductCmptGeneration} and a {@link IProductCmptTypeAssociation} to which the
     * newly created product component will be added.
     * <p>
     * The type of the new product component have to be compatible to the target type of the
     * {@link #getAddToAssociation()}
     * <p>
     * This method overwrites the selected base type with the target type of the given association.
     * It uses exactly the target type of the association also it may not be in the list of
     * available base type. With this behavior the list of selectable concrete types contains
     * exactly the types that could be selected for this association. If the target type of the
     * association is not abstract it is also used as default selected type.
     * 
     * @param addToProductCmptGeneration The product component you want to add the newly created
     *            product component to
     * @param addToAssociation The association in which context the newly created product component
     *            is added to the {@link #addToProductCmptGeneration}
     */
    public void setAddToAssociation(IProductCmptGeneration addToProductCmptGeneration,
            IProductCmptTypeAssociation addToAssociation) {
        this.addToProductCmptGeneration = null;
        this.addToAssociation = null;
        if (addToProductCmptGeneration == null || addToAssociation == null) {
            return;
        }
        try {
            IProductCmptType targetProductCmptType = addToAssociation
                    .findTargetProductCmptType(addToProductCmptGeneration.getIpsProject());
            this.addToProductCmptGeneration = addToProductCmptGeneration;
            this.addToAssociation = addToAssociation;
            setEffectiveDate(addToProductCmptGeneration.getValidFrom());
            setSelectedBaseType(targetProductCmptType);
            if (targetProductCmptType != null && !targetProductCmptType.isAbstract()) {
                setSelectedType(targetProductCmptType);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Configures this PMO so that the product component to create is a copy of the given product
     * component.
     * 
     * @throws IllegalStateException if no IPS project has been set using
     *             {@link #setIpsProject(IIpsProject)}
     */
    public void setCopyProductCmpt(IProductCmpt productCmptToCopy) {
        if (getIpsProject() == null) {
            throw new IllegalStateException("IPS Project must be set before setting the product to be copied."); //$NON-NLS-1$
        }
        copyProductCmpt = productCmptToCopy;

        initializeNameForProductCmptCopy();
        initializeTypeAndBaseTypeForProductCmptCopy(productCmptToCopy);
    }

    private void initializeNameForProductCmptCopy() {
        IPath targetPath = copyProductCmpt.getIpsPackageFragment().getCorrespondingResource().getFullPath();
        NewResourceNameValidator resourceNameValidator = new NewResourceNameValidator(targetPath, IResource.FILE,
                '.' + IpsObjectType.PRODUCT_CMPT.getFileExtension(), copyProductCmpt.getIpsSrcFile());
        String nameOfCopy = resourceNameValidator.getValidResourceName(copyProductCmpt.getName());
        setKindId(copyProductCmpt.getIpsProject().getProductCmptNamingStrategy().getKindId(nameOfCopy));
    }

    private void initializeTypeAndBaseTypeForProductCmptCopy(IProductCmpt productCmptToCopy) {
        try {
            IProductCmptType productCmptType = productCmptToCopy.findProductCmptType(productCmptToCopy.getIpsProject());
            copyValidProductCmpt = productCmptType != null;
            setSelectedType(productCmptType);

            SelectedBaseTypeVisitor baseTypeVisitor = new SelectedBaseTypeVisitor(getBaseTypes(),
                    productCmptToCopy.getIpsProject());
            baseTypeVisitor.start(productCmptType);
            setSelectedBaseType(baseTypeVisitor.selectedBaseType);

        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns the product component generation to which the newly created product component will be
     * added
     * 
     * @see #setAddToAssociation(IProductCmptGeneration, IProductCmptTypeAssociation)
     */
    public IProductCmptGeneration getAddToProductCmptGeneration() {
        return addToProductCmptGeneration;
    }

    /**
     * Returns the association to which the newly created product component will be added
     * 
     * @see #setAddToAssociation(IProductCmptGeneration, IProductCmptTypeAssociation)
     */
    public IProductCmptTypeAssociation getAddToAssociation() {
        return addToAssociation;
    }

    public boolean isAddToMode() {
        return getAddToAssociation() != null && getAddToProductCmptGeneration() != null;
    }

    /**
     * Returns whether the wizard is in copy mode or not.
     * <p>
     * When in copy mode, the product component provided via
     * {@link #setCopyProductCmpt(IProductCmpt)} is being copied.
     */
    public boolean isCopyMode() {
        return copyProductCmpt != null;
    }

    /**
     * Returns true if the wizard is
     * <ul>
     * <li>in copy mode and
     * <li>the product component type of the product component to be copied can be found
     */
    public boolean isCopyValidMode() {
        return isCopyMode() && copyValidProductCmpt;
    }

    /**
     * Returns whether the first wizard page (base type selection) is needed or not.
     * <p>
     * The page is not needed if
     * <ul>
     * <li>{@link #isAddToMode()} returns {@code true} or
     * <li>{@link #isCopyMode()} returns {@code true} and the product component type of the product
     * component being copied can be found
     */
    public boolean isFirstPageNeeded() {
        return !isAddToMode() && !isCopyValidMode();
    }

    /**
     * @param runtimeId The runtimeId to set.
     */
    public void setRuntimeId(String runtimeId) {
        String oldRuntimeId = this.runtimeId;
        this.runtimeId = runtimeId;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_RUNTIME_ID, oldRuntimeId, runtimeId));
    }

    /**
     * @return Returns the runtimeId.
     */
    public String getRuntimeId() {
        return runtimeId;
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT;
    }

    /**
     * Returns the product component being copied by this wizard or {@code null} if this wizard is
     * used to create a new product component.
     */
    public IProductCmpt getCopyProductCmpt() {
        return copyProductCmpt;
    }

    /**
     * Searches the type hierarchy and looks for a supertype that is part of the base type list.
     * 
     * @author dirmeier
     */
    private static class SelectedBaseTypeVisitor extends TypeHierarchyVisitor<IProductCmptType> {

        private final List<IProductCmptType> baseTypes;

        private IProductCmptType selectedBaseType = null;

        public SelectedBaseTypeVisitor(List<IProductCmptType> baseTypes, IIpsProject ipsProject) {
            super(ipsProject);
            this.baseTypes = baseTypes;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            if (baseTypes.contains(currentType)) {
                selectedBaseType = currentType;
                return false;
            } else {
                return true;
            }
        }

    }

}
