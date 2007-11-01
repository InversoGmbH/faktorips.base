/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.pctype.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String Relation_msgContainerRelNotReverseRel;

	public static String Relation_msgSamePluralRolename;

	public static String Relation_msgSameSingularRoleName;

	public static String Relation_msgRelationNotInTarget;

	public static String Relation_msgReverseRelationNotSpecified;

	public static String Relation_msgReverseRelOfContainerRelMustBeContainerRelToo;

	public static String Relation_msgReverseCompositionMissmatch;

	public static String Relation_msgReverseAssociationMissmatch;

	public static String Method_msgInvalidParameterName;

	public static String Method_msgDatatypeEmpty;

    public static String TableStructureUsage_msgRoleNameAlreadyInSupertype;

    public static String TableStructureUsage_msgSameRoleName;

    public static String ValidationRule_0;

    public static String ValidationRule_ConstantAttributesCantBeValidated;

	public static String ValidationRule_msgFunctionNotExists;

	public static String ValidationRule_msgIgnored;

	public static String ValidationRule_msgUndefinedAttribute;

	public static String ValidationRule_msgDuplicateEntries;

	public static String Attribute_msgNoInputparams;

	public static String Attribute_msgNoParamsNeccessary;

	public static String Attribute_msgEmptyName;

	public static String Attribute_msgInvalidParamName;

	public static String Attribute_msgDatatypeEmpty;

	public static String Attribute_msgDatatypeNotFound;

	public static String PolicyCmptType_msgSupertypeNotFound;

	public static String PolicyCmptType_msgNameMissing;

	public static String PolicyCmptType_msgAbstractMissmatch;

	public static String PolicyCmptType_msgMustOverrideAbstractMethod;

    public static String PolicyCmptType_msgMustImplementContainerRelation;

	public static String Relation_msgRevereseCompositionMustHaveMaxCardinality1;

	public static String Relation_msgReverseCompositionCantBeMarkedAsProductRelevant;

	public static String Relation_msgRelationCanBeProductRelevantOnlyIfTypeIs;

	public static String Relation_msgImplementationMustHaveSameProductRelevantValue;

	public static String Attribute_msgAttributeCantBeProductRelevantIfTypeIsNot;

	public static String PolicyCmptType_msgInconsistentTypeHierarchy;

	public static String Attribute_msgNameCollision;

	public static String Attribute_msgpartUnknown;

	public static String Attribute_msgNothingToOverwrite;

	public static String PolicyCmptType_msgNameMissmatch;

	public static String PolicyCmptType_msgInvalidProductCmptTypeName;

	public static String Relation_msgNoTargetRoleSingular;

	public static String Relation_msgNoTargetRolePlural;

	public static String Relation_msgTargetRoleSingularIlleaglySameAsTargetRolePluralProdSide;

	public static String Attribute_msgNameCollisionLocal;

    public static String ValidationRule_msgOneBusinessFunction;

    public static String ValidationRule_msgValueSetRule;
    
	public static String Relation_Error_RelationIsNoContainerRelation;

	public static String Relation_msgRelationCanOnlyProdRelIfTargetTypeIsConfByProduct;

    public static String ValidationRule_msgNoNewlineAllowed;

    public static String TableStructureUsage_msgTableStructureNotExists;

    public static String TableStructureUsage_msgInvalidRoleName;

    public static String TableStructureUsage_msgAtLeastOneStructureMustBeReferenced;

    public static String PolicyCmptType_msgSuperTypeNotProdRelevantIfProductRelevant;

    public static String TableStructureUsage_msgPolicyCmptTypeIsNotConfiguratedByProduct;

    public static String ValidationRule_msgCodeShouldBeProvided;

    public static String Relation_noReverseRelationNeededForDetailToMasterRelations;

    public static String Attribute_proposalForRuleName;

    public static String Attribute_proposalForMsgCode;
}
