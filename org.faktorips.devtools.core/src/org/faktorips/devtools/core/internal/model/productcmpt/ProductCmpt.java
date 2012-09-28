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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.core.internal.model.ipsobject.TimedIpsObject;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.ProductCmptValidations;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.type.TypeValidations;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of product component.
 * 
 * @author Jan Ortmann
 */
public class ProductCmpt extends TimedIpsObject implements IProductCmpt {

    private final ProductCmptLinkCollection linkCollection = new ProductCmptLinkCollection();
    private final PropertyValueCollection propertyValueCollection = new PropertyValueCollection();
    private String productCmptType = ""; //$NON-NLS-1$
    private String runtimeId = ""; //$NON-NLS-1$

    public ProductCmpt(IIpsSrcFile file) {
        super(file);
    }

    public ProductCmpt() {
        super();
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT;
    }

    @Override
    public IProductCmptGeneration getProductCmptGeneration(int index) {
        return (IProductCmptGeneration)getGeneration(index);
    }

    @Override
    public IProductCmptGeneration getFirstGeneration() {
        return (IProductCmptGeneration)super.getFirstGeneration();
    }

    @Override
    public IProductCmptGeneration getLatestProductCmptGeneration() {
        IIpsObjectGeneration latestGeneration = getLatestGeneration();
        return latestGeneration == null ? null : (IProductCmptGeneration)latestGeneration;
    }

    @Override
    public IProductCmptKind findProductCmptKind() throws CoreException {
        IProductCmptNamingStrategy stratgey = getIpsProject().getProductCmptNamingStrategy();
        try {
            String kindName = stratgey.getKindId(getName());
            return new ProductCmptKind(kindName, getIpsProject().getRuntimeIdPrefix() + kindName);
        } catch (Exception e) {
            return null; // error in parsing the name results in a "not found" for the client
        }
    }

    @Override
    public String getVersionId() throws CoreException {
        try {
            return getIpsProject().getProductCmptNamingStrategy().getVersionId(getName());
        } catch (IllegalArgumentException e) {
            throw new CoreException(new IpsStatus("Can't get version id for " + this, e)); //$NON-NLS-1$
        }
    }

    @Override
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException {
        IProductCmptType productCmptType = findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return null;
        }
        return productCmptType.findPolicyCmptType(ipsProject);
    }

    @Override
    public String getProductCmptType() {
        return productCmptType;
    }

    @Override
    public void setProductCmptType(String newType) {
        String oldType = productCmptType;
        productCmptType = newType;
        valueChanged(oldType, newType);
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findProductCmptType(productCmptType);
    }

    @Override
    protected IpsObjectGeneration createNewGeneration(String id) {
        return new ProductCmptGeneration(this, id);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType type = ProductCmptValidations.validateProductCmptType(this, productCmptType, list, ipsProject);
        if (type == null) {
            return;
        }
        Message message = TypeValidations.validateTypeHierachy(type, ipsProject);
        if (message != null) {
            String typeLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(type);
            String msg = NLS.bind(Messages.ProductCmpt_msgInvalidTypeHierarchy, typeLabel);
            list.add(new Message(MSGCODE_INCONSISTENT_TYPE_HIERARCHY, msg, Message.ERROR, type,
                    PROPERTY_PRODUCT_CMPT_TYPE));
            // do not continue validation if hierarchy is invalid
            return;
        }
        IProductCmptNamingStrategy strategy = ipsProject.getProductCmptNamingStrategy();
        MessageList list2 = strategy.validate(getName());
        for (Message msg : list2) {
            Message msgNew = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_NAME);
            list.add(msgNew);
        }
        list2 = strategy.validateRuntimeId(getRuntimeId());
        for (Message msg : list2) {
            Message msgNew = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_RUNTIME_ID);
            list.add(msgNew);
        }

        list2 = getIpsProject().checkForDuplicateRuntimeIds(new IIpsSrcFile[] { getIpsSrcFile() });
        list.add(list2);
    }

    @Override
    public boolean containsFormula() {
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            if (((ProductCmptGeneration)generation).containsFormula()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsFormulaTest() {
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            IProductCmptGeneration gen = (IProductCmptGeneration)generation;
            if (gen.getNumOfFormulas() > 0) {
                IFormula[] formulas = gen.getFormulas();
                for (IFormula formula : formulas) {
                    if (formula.getFormulaTestCases().length > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) throws CoreException {
        Set<IDependency> dependencySet = new HashSet<IDependency>();

        if (!StringUtils.isEmpty(productCmptType)) {
            IDependency dependency = IpsObjectDependency.createInstanceOfDependency(getQualifiedNameType(),
                    new QualifiedNameType(productCmptType, IpsObjectType.PRODUCT_CMPT_TYPE));
            dependencySet.add(dependency);
            addDetails(details, dependency, this, PROPERTY_PRODUCT_CMPT_TYPE);
        }

        // add dependency to related product cmpt's and add dependency to table contents
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            ((ProductCmptGeneration)generation).dependsOn(dependencySet, details);
        }

        return dependencySet.toArray(new IDependency[dependencySet.size()]);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_PRODUCT_CMPT_TYPE, productCmptType);
        element.setAttribute(PROPERTY_RUNTIME_ID, runtimeId);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        productCmptType = element.getAttribute(PROPERTY_PRODUCT_CMPT_TYPE);
        runtimeId = element.getAttribute(PROPERTY_RUNTIME_ID);
    }

    @Override
    @Deprecated
    public IProductCmptTreeStructure getStructure(IIpsProject ipsProject) throws CycleInProductStructureException {
        return new ProductCmptTreeStructure(this, ipsProject);
    }

    @Override
    public IProductCmptTreeStructure getStructure(GregorianCalendar date, IIpsProject ipsProject)
            throws CycleInProductStructureException {
        return new ProductCmptTreeStructure(this, date, ipsProject);
    }

    @Override
    public String getRuntimeId() {
        return runtimeId;
    }

    @Override
    public void setRuntimeId(String runtimeId) {
        String oldId = this.runtimeId;
        this.runtimeId = runtimeId;
        valueChanged(oldId, runtimeId);
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        IProductCmptType cmptType = findProductCmptType(getIpsProject());
        if (cmptType != null) {
            return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(cmptType);
        } else {
            return getProductCmptType();
        }
    }

    @Override
    public boolean containsDifferenceToModel(IIpsProject ipsProject) throws CoreException {
        return !computeDeltaToModel(ipsProject).isEmpty();
    }

    @Override
    public void fixAllDifferencesToModel(IIpsProject ipsProject) throws CoreException {
        computeDeltaToModel(ipsProject).fixAllDifferencesToModel();
    }

    @Override
    public IPropertyValueContainerToTypeDelta computeDeltaToModel(IIpsProject ipsProject) throws CoreException {
        return new ProductCmptToTypeDelta(this, ipsProject);
    }

    @Override
    public boolean isReferencingProductCmpt(IIpsProject ipsProjectToSearch, IProductCmpt productCmptCandidate) {
        int numOfGenerations = getNumOfGenerations();
        for (int i = 0; i < numOfGenerations; i++) {
            IProductCmptGeneration generation = (IProductCmptGeneration)getGeneration(i);
            IProductCmptLink[] links = generation.getLinks();
            for (IProductCmptLink link : links) {
                if (productCmptCandidate.getQualifiedName().equals(link.getTarget())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isUsedAsTargetProductCmpt(IIpsProject ipsProjectToSearch, IProductCmpt productCmptCandidate) {
        return isReferencingProductCmpt(ipsProjectToSearch, productCmptCandidate);
    }

    @Override
    public IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findIpsSrcFile(IpsObjectType.PRODUCT_CMPT_TYPE, getProductCmptType());
    }

    @Override
    public String getMetaClass() {
        return getProductCmptType();
    }

    @Override
    public IPropertyValue getPropertyValue(IProductCmptProperty property) {
        return propertyValueCollection.getPropertyValue(property);
    }

    @Override
    public boolean hasPropertyValue(IProductCmptProperty property) {
        return getPropertyValue(property) != null;
    }

    @Override
    public IPropertyValue getPropertyValue(String propertyName) {
        return propertyValueCollection.getPropertyValue(propertyName);
    }

    @Override
    public <T extends IPropertyValue> List<T> getPropertyValues(Class<T> type) {
        return propertyValueCollection.getPropertyValues(type);
    }

    @Override
    public List<IPropertyValue> getAllPropertyValues() {
        return propertyValueCollection.getAllPropertyValues();
    }

    @Override
    public IPropertyValue newPropertyValue(IProductCmptProperty property) {
        if (property.getProductCmptPropertyType() == ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE) {
            IPropertyValue newPropertyValue = propertyValueCollection.newPropertyValue(this, property, getNextPartId());
            objectHasChanged();
            return newPropertyValue;
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        IIpsObjectPart part = super.newPartThis(xmlTag, id);
        if (part != null) {
            return part;
        }
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(IIpsObjectGeneration.TAG_NAME)) {
            return newGenerationInternal(id);
        }
        if (xmlTagName.equals(IProductCmptLink.TAG_NAME)) {
            IProductCmptLink newLinkInternal = createAndAddNewLinkInternal(id);
            return newLinkInternal;
        }
        if (xmlTagName.equals(AttributeValue.TAG_NAME)) {
            IIpsObjectPart newPartThis = propertyValueCollection.newPropertyValue(this, AttributeValue.TAG_NAME, id);
            return newPartThis;
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        IIpsObjectPart part = super.newPartThis(partType);
        if (part != null) {
            return part;
        }
        if (IPolicyCmptTypeAssociation.class.isAssignableFrom(partType)) {
            return createAndAddNewLinkInternal(getNextPartId());
        } else if (IPropertyValue.class.isAssignableFrom(partType)) {
            Class<? extends IPropertyValue> propertyValueType = partType.asSubclass(IPropertyValue.class);
            IPropertyValue newPart = propertyValueCollection.newPropertyValue(this, getNextPartId(), propertyValueType);
            return newPart;
        }
        return null;
    }

    /**
     * Creates a link without a corresponding association name. The association thus remains
     * undefined. Moreover the association must not be set as this method is used for XML
     * initialization which in turn must not trigger value changes (and setAssociation() would).
     * 
     * @param id the future part id of the new link
     */
    private IProductCmptLink createAndAddNewLinkInternal(String id) {
        ProductCmptLink newLink = new ProductCmptLink(this, id);
        linkCollection.addLink(newLink);
        return newLink;
    }

    @Override
    public List<IAttributeValue> getAttributeValues() {
        return propertyValueCollection.getPropertyValues(IAttributeValue.class);
    }

    @Override
    public IAttributeValue getAttributeValue(String attribute) {
        return propertyValueCollection.getPropertyValue(IAttributeValue.class, attribute);
    }

    @Override
    public boolean isContainerFor(IProductCmptProperty property) {
        return !property.isChangingOverTime();
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        IIpsElement[] childrenThis = super.getChildrenThis();
        List<IIpsElement> children = new ArrayList<IIpsElement>();
        children.addAll(Arrays.asList(childrenThis));
        children.addAll(propertyValueCollection.getAllPropertyValues());
        children.addAll(getLinksAsList());
        return children.toArray(new IIpsElement[children.size()]);
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (super.addPartThis(part)) {
            return true;
        }
        if (part instanceof IProductCmptLink) {
            return linkCollection.addLink((IProductCmptLink)part);
        } else if (part instanceof IPropertyValue) {
            IPropertyValue propertyValue = (IPropertyValue)part;
            return propertyValueCollection.addPropertyValue(propertyValue);
        } else {
            return false;
        }
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (super.removePartThis(part)) {
            return true;
        }
        if (part instanceof IProductCmptLink) {
            return linkCollection.remove((IProductCmptLink)part);
        } else if (part instanceof IPropertyValue) {
            IPropertyValue propertyValue = (IPropertyValue)part;
            return propertyValueCollection.removePropertyValue(propertyValue);
        } else {
            return false;
        }
    }

    @Override
    protected void reinitPartCollectionsThis() {
        super.reinitPartCollectionsThis();
        propertyValueCollection.clear();
        linkCollection.clear();
    }

    @Override
    public List<IProductCmptGeneration> getProductCmptGenerations() {
        List<IProductCmptGeneration> generations = new ArrayList<IProductCmptGeneration>();
        List<IIpsObjectGeneration> ipsObjectGenerations = getGenerations();
        for (IIpsObjectGeneration ipsObjectGeneration : ipsObjectGenerations) {
            generations.add((IProductCmptGeneration)ipsObjectGeneration);
        }
        return generations;
    }

    @Override
    public IProductCmptGeneration getGenerationEffectiveOn(GregorianCalendar date) {
        return (IProductCmptGeneration)super.getGenerationEffectiveOn(date);
    }

    @Override
    public IProductCmptGeneration getBestMatchingGenerationEffectiveOn(GregorianCalendar date) {
        return (IProductCmptGeneration)super.getBestMatchingGenerationEffectiveOn(date);
    }

    @Override
    public IProductCmptGeneration getGenerationByEffectiveDate(GregorianCalendar date) {
        return (IProductCmptGeneration)super.getGenerationByEffectiveDate(date);
    }

    @Override
    public List<IPropertyValue> findPropertyValues(IProductCmptCategory category,
            GregorianCalendar effectiveDate,
            IIpsProject ipsProject) throws CoreException {

        IProductCmptGeneration generation = getGenerationByEffectiveDate(effectiveDate);
        return category != null ? findPropertyValuesForSpecificCategory(generation, category, ipsProject)
                : findPropertyValuesForNoCategory(generation);
    }

    private List<IPropertyValue> findPropertyValuesForSpecificCategory(IProductCmptGeneration generation,
            IProductCmptCategory category,
            IIpsProject ipsProject) throws CoreException {

        List<IPropertyValue> propertyValues = new ArrayList<IPropertyValue>();

        IProductCmptType contextType = findProductCmptType(ipsProject);
        if (contextType == null) {
            return propertyValues;
        }

        for (IProductCmptProperty property : category.findProductCmptProperties(contextType, true, ipsProject)) {
            if (hasPropertyValue(property)) {
                propertyValues.add(getPropertyValue(property));
            } else if (generation != null && generation.hasPropertyValue(property)) {
                propertyValues.add(generation.getPropertyValue(property));
            }
        }

        return propertyValues;
    }

    private List<IPropertyValue> findPropertyValuesForNoCategory(IProductCmptGeneration generation) {
        List<IPropertyValue> propertyValues = new ArrayList<IPropertyValue>();
        propertyValues.addAll(getAllPropertyValues());
        propertyValues.addAll(generation.getAllPropertyValues());
        return propertyValues;
    }

    @Override
    public boolean isContainerFor(IProductCmptTypeAssociation association) {
        return !association.isChangingOverTime();
    }

    @Override
    public int getNumOfLinks() {
        return linkCollection.size();
    }

    @Override
    public IProductCmptLink newLink(IProductCmptTypeAssociation association) {
        return newLink(association.getName());
    }

    @Override
    public IProductCmptLink newLink(String associationName) {
        IProductCmptLink newLink = linkCollection.createAndAddNewLink(this, associationName, getNextPartId());
        objectHasChanged();
        return newLink;
    }

    @Override
    public IProductCmptLink newLink(String associationName, IProductCmptLink insertAbove) {
        IProductCmptLink newLink = linkCollection.createAndInsertNewLink(this, associationName, getNextPartId(),
                insertAbove);
        objectHasChanged();
        return newLink;
    }

    @Override
    public boolean canCreateValidLink(IProductCmpt target, IAssociation association, IIpsProject ipsProject)
            throws CoreException {
        return ProductCmptLinkContainerUtil.canCreateValidLink(this, target, association, ipsProject);
    }

    @Override
    public boolean moveLink(IProductCmptLink toMove, IProductCmptLink target, boolean above) {
        boolean moved = linkCollection.moveLink(toMove, target, above);
        if (moved) {
            objectHasChanged();
        }
        return moved;
    }

    @Override
    public List<IProductCmptLink> getLinksAsList() {
        return linkCollection.getLinks();
    }

    @Override
    public List<IProductCmptLink> getLinksAsList(String associationName) {
        return linkCollection.getLinks(associationName);
    }

    @Override
    public IProductCmpt getProductCmpt() {
        return this;
    }

}
