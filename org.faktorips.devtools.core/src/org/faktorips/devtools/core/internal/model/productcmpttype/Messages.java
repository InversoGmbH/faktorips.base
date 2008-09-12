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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Jan Ortmann
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.productcmpttype.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

    public static String ProductCmptType_DuplicateFormulaName;

    public static String ProductCmptType_InconsistentTypeHierarchies;

    public static String ProductCmptType_msgDuplicateFormulasNotAllowedInSameType;

    public static String ProductCmptType_msgOverloadedFormulaMethodCannotBeOverridden;

    public static String ProductCmptType_msgProductCmptTypeAbstractWhenPolicyCmptTypeAbstract;

    public static String ProductCmptType_multiplePropertyNames;

    public static String ProductCmptType_notMarkedAsConfigurable;

    public static String ProductCmptType_PolicyCmptTypeDoesNotExist;

    public static String ProductCmptType_policyCmptTypeDoesNotSpecifyThisType;

    public static String ProductCmptType_TypeMustConfigureAPolicyCmptTypeIfSupertypeDoes;

    public static String ProductCmptTypeMethod_FormulaNameIsMissing;

    public static String ProductCmptTypeMethod_FormulaSignatureDatatypeMustBeAValueDatatype;

    public static String ProductCmptTypeMethod_FormulaSignatureMustntBeAbstract;

    public static String ProductCmptTypeMethod_msgNoOverloadableFormulaInSupertypeHierarchy;

    public static String ProductCmptTypeMethod_msgOverloadedSignatureNotInTypeHierarchy;

    public static String TableStructureUsage_msgAtLeastOneStructureMustBeReferenced;

    public static String TableStructureUsage_msgInvalidRoleName;

    public static String TableStructureUsage_msgRoleNameAlreadyInSupertype;

    public static String TableStructureUsage_msgSameRoleName;

    public static String TableStructureUsage_msgTableStructureNotExists;

}
