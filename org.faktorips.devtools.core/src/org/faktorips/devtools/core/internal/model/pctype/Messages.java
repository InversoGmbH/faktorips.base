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

    public static String PolicyCmptType_msg_IfTheSupertypeIsNotConfigurableTheTypeCanBeConfigurable;

    public static String PolicyCmptType_msg_ProductCmptTypeNameMissing;

    public static String PolicyCmptType_msgSubtypeConfigurableWhenSupertypeConfigurable;

    public static String PolicyCmptType_productCmptType;

    public static String PolicyCmptType_TheTypeDoesNotConfigureThisType;

    public static String PolicyCmptTypeAttribute_msg_ComputationMethodSignatureDoesNotExists;

    public static String PolicyCmptTypeAttribute_msg_ComputationMethodSignatureHasADifferentDatatype;

    public static String PolicyCmptTypeAttribute_msg_ComputationMethodSignatureIsMissing;

    public static String PolicyCmptTypeAttribute_TypeOfOverwrittenAttributeCantBeChanged;

    public static String Association_msg_InverseAssociationInconsistentWithDerivedUnion;

	public static String Association_msg_AssociationNotFoundInTarget;

	public static String Association_msg_InverseAssociationMismatch;

	public static String Association_msg_InverseAssociationMustBeMarkedAsDerivedUnionToo;

    public static String Association_msg_ForDetailToMasterAssociationsNeedNotSpecifyInverse;

	public static String Association_msg_InverseAssociationMustBeOfTypeAssociation;

    public static String Association_msg_DetailToMasterAssociationMustHaveMaxCardinality1;

    public static String ValidationRule_ConstantAttributesCantBeValidated;

	public static String ValidationRule_msgFunctionNotExists;

	public static String ValidationRule_msgIgnored;

	public static String ValidationRule_msgUndefinedAttribute;

	public static String ValidationRule_msgDuplicateEntries;

    public static String ValidationRule_msgOneBusinessFunction;

    public static String ValidationRule_msgValueSetRule;
    
    public static String ValidationRule_msgNoNewlineAllowed;

    public static String ValidationRule_msgCodeShouldBeProvided;

	public static String Attribute_msgAttributeCantBeProductRelevantIfTypeIsNot;

	public static String Attribute_msgNothingToOverwrite;
    
    public static String Attribute_proposalForRuleName;

    public static String Attribute_proposalForMsgCode;
}
