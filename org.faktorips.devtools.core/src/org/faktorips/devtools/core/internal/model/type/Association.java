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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.AtomicIpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAssociation.
 * 
 * @author Jan Ortmann
 */
public abstract class Association extends AtomicIpsObjectPart implements IAssociation {

    final static String TAG_NAME = "Association"; //$NON-NLS-1$

    protected AssociationType type = IAssociation.DEFAULT_RELATION_TYPE;
    protected String target = ""; //$NON-NLS-1$
    protected String targetRoleSingular = ""; //$NON-NLS-1$
    protected String targetRolePlural = ""; //$NON-NLS-1$
    protected int minCardinality = 0;
    protected int maxCardinality = Integer.MAX_VALUE; 
    protected String subsettedDerivedUnion = ""; //$NON-NLS-1$
    protected boolean derivedUnion = false;
    
    public Association(IIpsObject parent, int id) {
        super(parent, id);
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
    public AggregationKind getAggregationKind() {
        return getAssociationType().getAggregationKind();
    }

    /** 
     * {@inheritDoc}
     */
    public AssociationType getAssociationType() {
        return type;
    }
    
    /** 
     * {@inheritDoc}
     */
    public void setAssociationType(AssociationType newType) {
        ArgumentCheck.notNull(newType);
        AssociationType oldType = type;
        type = newType;
        valueChanged(oldType, newType);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isAssoziation() {
        return type.isAssoziation();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return targetRoleSingular;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDerived() {
        return isDerivedUnion();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDerivedUnion() {
        return derivedUnion;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setDerivedUnion(boolean flag) {
        boolean oldValue = derivedUnion;
        this.derivedUnion = flag;
        valueChanged(oldValue, derivedUnion);
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getTarget() {
        return target;
    }

    /** 
     * {@inheritDoc}
     */
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, newTarget);
    }

    /**
     * {@inheritDoc}
     */
    public IType findTarget(IIpsProject ipsProject) throws CoreException {
        return (IType)ipsProject.findIpsObject(getIpsObject().getIpsObjectType(), target);
    }

    /** 
     * {@inheritDoc}
     */
    public String getTargetRoleSingular() {
        return targetRoleSingular;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultTargetRoleSingular() {
        return StringUtils.capitalise(QNameUtil.getUnqualifiedName(target));
    }

    /** 
     * {@inheritDoc}
     */
    public void setTargetRoleSingular(String newRole) {
        String oldRole = targetRoleSingular;
        targetRoleSingular = newRole;
        valueChanged(oldRole, newRole);
    }
    

    /**
     * {@inheritDoc}
     */
    public String getTargetRolePlural() {
        return targetRolePlural;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultTargetRolePlural() {
        return targetRoleSingular;
    }

    /**
     * {@inheritDoc}
     */
    public void setTargetRolePlural(String newRole) {
        String oldRole = targetRolePlural;
        targetRolePlural = newRole;
        valueChanged(oldRole, newRole);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isTargetRolePluralRequired() {
        return is1ToMany() || getIpsProject().getIpsArtefactBuilderSet().isRoleNamePluralRequiredForTo1Relations();
    }

    /** 
     * {@inheritDoc}
     */
    public int getMinCardinality() {
        return minCardinality;
    }

    /** 
     * {@inheritDoc}
     */
    public void setMinCardinality(int newValue) {
        int oldValue = minCardinality;
        minCardinality = newValue;
        valueChanged(oldValue, newValue);
    }

    /** 
     * {@inheritDoc}
     */
    public int getMaxCardinality() {
        return maxCardinality;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean is1ToMany() {
        return isQualified() || maxCardinality > 1;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean is1To1() {
        return maxCardinality == 1;
    }

    /**
     * {@inheritDoc}
     */
    public void setMaxCardinality(int newValue) {
        int oldValue = maxCardinality;
        maxCardinality = newValue;
        valueChanged(oldValue, newValue);
    }

    /** 
     * {@inheritDoc}
     */
    public void setSubsettedDerivedUnion(String newRelation) {
        String oldValue = subsettedDerivedUnion;
        subsettedDerivedUnion = newRelation;
        valueChanged(oldValue, newRelation);
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getSubsettedDerivedUnion() {
        return subsettedDerivedUnion;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isSubsetOfADerivedUnion() {
        return StringUtils.isNotEmpty(subsettedDerivedUnion);
    }
    
    /**
     * {@inheritDoc}
     */
    public IAssociation findSubsettedDerivedUnion(IIpsProject project) throws CoreException {
        return getType().findAssociation(subsettedDerivedUnion, project);
    }
    
    /**
     * {@inheritDoc}
     */
    public IAssociation[] findDerivedUnionCandidates(IIpsProject ipsProject) throws CoreException {
        IType targetType = findTarget(ipsProject);
        if (target==null) {
            return new IAssociation[0];
        }
        DerivedUnionCandidatesFinder finder = new DerivedUnionCandidatesFinder(targetType, ipsProject);
        finder.start(getType());
        return (IAssociation[])finder.candidates.toArray(new IAssociation[finder.candidates.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSubsetOfDerivedUnion(IAssociation derivedUnion, IIpsProject project) throws CoreException {
        if (derivedUnion==null) {
            return false;
        }
        return derivedUnion.equals(findSubsettedDerivedUnion(project));
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
        type = AssociationType.getRelationType(element.getAttribute(PROPERTY_ASSOCIATION_TYPE));
        if (type==null) {
            type = IAssociation.DEFAULT_RELATION_TYPE;
        }
        target = element.getAttribute(PROPERTY_TARGET);
        targetRoleSingular = element.getAttribute(PROPERTY_TARGET_ROLE_SINGULAR);
        targetRolePlural = element.getAttribute(PROPERTY_TARGET_ROLE_PLURAL);
        try {
            minCardinality = Integer.parseInt(element.getAttribute(PROPERTY_MIN_CARDINALITY));
        } catch (NumberFormatException e) {
            minCardinality = 0;
        }
        String max = element.getAttribute(PROPERTY_MAX_CARDINALITY);
        if (max.equals("*")) { //$NON-NLS-1$
            maxCardinality = CARDINALITY_MANY;
        } else {
            try {
                maxCardinality = Integer.parseInt(max);
            } catch (NumberFormatException e) {
                maxCardinality = 0;
            }
        }
        derivedUnion = Boolean.valueOf(element.getAttribute(PROPERTY_DERIVED_UNION)).booleanValue();
        subsettedDerivedUnion = element.getAttribute(PROPERTY_SUBSETTED_DERIVED_UNION);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_ASSOCIATION_TYPE, type.getId());
        newElement.setAttribute(PROPERTY_TARGET, target);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_SINGULAR, targetRoleSingular);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_PLURAL, targetRolePlural);
        newElement.setAttribute(PROPERTY_MIN_CARDINALITY, "" + minCardinality); //$NON-NLS-1$
        
        if (maxCardinality == CARDINALITY_MANY) {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY, "*"); //$NON-NLS-1$
        } else {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY, "" + maxCardinality); //$NON-NLS-1$
        }
        
        newElement.setAttribute(PROPERTY_DERIVED_UNION, "" + derivedUnion); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_SUBSETTED_DERIVED_UNION, subsettedDerivedUnion); 
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        IIpsProject ipsProject = getIpsProject();
        ValidationUtils.checkIpsObjectReference(target, getIpsObject().getIpsObjectType(), "target", this,  //$NON-NLS-1$
                PROPERTY_TARGET, MSGCODE_TARGET_DOES_NOT_EXIST, list); //$NON-NLS-1$
        ValidationUtils.checkStringPropertyNotEmpty(targetRoleSingular, Messages.Association_msg_TargetRoleSingular, this, PROPERTY_TARGET_ROLE_SINGULAR,
                MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET, list);
        if (maxCardinality == 0) {
            String text = Messages.Association_msg_MaxCardinalityMustBeAtLeast1;
            list.add(new Message(MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1, text, Message.ERROR, this, PROPERTY_MAX_CARDINALITY)); //$NON-NLS-1$
        } else if (maxCardinality == 1 && isDerivedUnion()) {
            String text = Messages.Association_msg_MaxCardinalityForDerivedUnionTooLow;
            list.add(new Message(MSGCODE_MAX_CARDINALITY_FOR_DERIVED_UNION_TOO_LOW, text, Message.ERROR, this, new String[]{PROPERTY_DERIVED_UNION, PROPERTY_MAX_CARDINALITY})); //$NON-NLS-1$
        } else if (minCardinality > maxCardinality) {
            String text = Messages.Association_msg_MinCardinalityGreaterThanMaxCardinality;
            list.add(new Message(MSGCODE_MAX_IS_LESS_THAN_MIN, text, Message.ERROR, this, new String[]{PROPERTY_MIN_CARDINALITY, PROPERTY_MAX_CARDINALITY})); //$NON-NLS-1$
        }
        
        if (maxCardinality > 1 || getIpsProject().getIpsArtefactBuilderSet().isRoleNamePluralRequiredForTo1Relations()) {
            ValidationUtils.checkStringPropertyNotEmpty(targetRolePlural,
                    Messages.Association_msg_TargetRolePlural, this, PROPERTY_TARGET_ROLE_PLURAL,
                    MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET, list);
        }
        if (StringUtils.isNotEmpty(this.getTargetRolePlural())
                && this.getTargetRolePlural().equals(this.getTargetRoleSingular())) {
            String text = Messages.Association_msg_TargetRoleSingularIlleaglySameAsTargetRolePlural;
            list.add(new Message(
                    MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR,
                    text, Message.ERROR, this, new String[] {
                            PROPERTY_TARGET_ROLE_SINGULAR,
                            PROPERTY_TARGET_ROLE_PLURAL }));
        }
        validateDerivedUnion(list, ipsProject);
    }
    
    private void validateDerivedUnion(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(subsettedDerivedUnion)) {
            return;
        }
        IAssociation unionAss = findSubsettedDerivedUnion(ipsProject);
        if (unionAss==null) {
            String text = NLS.bind(Messages.Association_msg_DerivedUnionDoesNotExist, subsettedDerivedUnion);
            list.add(new Message(MSGCODE_DERIVED_UNION_NOT_FOUND, text, Message.ERROR, this, PROPERTY_SUBSETTED_DERIVED_UNION)); //$NON-NLS-1$
            return;
        }
        if (!unionAss.isDerivedUnion()) {
            String text = Messages.Association_msg_NotMarkedAsDerivedUnion;
            list.add(new Message(MSGCODE_NOT_MARKED_AS_DERIVED_UNION, text, Message.ERROR, this, PROPERTY_SUBSETTED_DERIVED_UNION)); //$NON-NLS-1$
        }
        IType unionTarget = unionAss.findTarget(ipsProject);
        if (unionTarget==null) {
            String text = Messages.Association_msg_TargetOfDerivedUnionDoesNotExist;
            list.add(new Message(MSGCODE_TARGET_OF_DERIVED_UNION_DOES_NOT_EXIST, text, Message.WARNING, this, PROPERTY_SUBSETTED_DERIVED_UNION)); //$NON-NLS-1$
            return;
        }
        IType targetType = findTarget(ipsProject);
        if (targetType!=null && !targetType.isSubtypeOrSameType(unionTarget, ipsProject)) {
            String text = Messages.Association_msg_TargetNotSubclass;
            list.add(new Message(IAssociation.MSGCODE_TARGET_TYPE_NOT_A_SUBTYPE, text, Message.ERROR, this, PROPERTY_SUBSETTED_DERIVED_UNION));     //$NON-NLS-1$
        }
    }
    
    
    private class DerivedUnionCandidatesFinder extends TypeHierarchyVisitor {

        private List candidates = new ArrayList();
        private IType targetType;
        
        public DerivedUnionCandidatesFinder(IType targetType, IIpsProject ipsProject) {
            super(ipsProject);
            this.targetType = targetType;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IType currentType) throws CoreException {
            IAssociation[] associations = currentType.getAssociations();
            for (int j = 0; j < associations.length; j++) {
                if (!associations[j].isDerivedUnion())
                    continue;
                
                IType derivedUnionTarget = associations[j].findTarget(ipsProject);
                if (derivedUnionTarget == null)
                    continue;
                
                if (targetType.isSubtypeOrSameType(derivedUnionTarget, ipsProject)) {
                    candidates.add(associations[j]);
                }
            }
            return true;
        }
        
    }
}
