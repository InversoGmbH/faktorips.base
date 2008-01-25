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

package org.faktorips.devtools.core.ui;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

    public static String AbstractCompletionProcessor_labelDefaultPackage;

    public static String DefaultLabelProvider_labelDefaultPackage;

    public static String FaktorIpsPreferencePage_advancedTeamFunctionsInProductDefExplorer;

    public static String FaktorIpsPreferencePage_label_fourSections;

    public static String FaktorIpsPreferencePage_label_twoSections;

    public static String FaktorIpsPreferencePage_labelEnumTypeDisplay;

    public static String FaktorIpsPreferencePage_labeRangeEditFieldsInOneRow;

    public static String FaktorIpsPreferencePage_title_numberOfSections;

	public static String PdPackageSelectionDialog_title;

	public static String PdPackageSelectionDialog_description;

	public static String FaktorIpsPreferencePage_labelWorkingDate;

	public static String FaktorIpsPreferencePage_labelNullValue;

	public static String FaktorIpsPreferencePage_labelProductTypePostfix;

	public static String FaktorIpsPreferencePage_labelNamingScheme;

	public static String PdObjectSelectionDialog_labelMatches;

	public static String PdObjectSelectionDialog_labelQualifier;

	public static String DatatypeSelectionDialog_title;

	public static String DatatypeSelectionDialog_description;

	public static String DatatypeSelectionDialog_labelMatchingDatatypes;

	public static String DatatypeSelectionDialog_msgLabelQualifier;

	public static String PdSourceRootSelectionDialog_title;

	public static String PdSourceRootSelectionDialog_description;

	public static String AbstractCompletionProcessor_msgNoProject;

	public static String AbstractCompletionProcessor_msgInternalError;

	public static String FaktorIpsPreferencePage_FaktorIpsPreferencePage_enableGenerating;

	public static String FolderPropertiesPage_labelSortNumber;

	public static String FolderPropertiesPage_msgSortNumberInvalid;

	public static String FaktorIpsPreferencePage_labelEditRecentGenerations;

	public static String FaktorIpsPreferencePage_labelCanNavigateToModelOrSourceCode;

	public static String FaktorIpsPreferencePage_titleWorkingMode;

	public static String FaktorIpsPreferencePage_labelWorkingModeBrowse;

	public static String FaktorIpsPreferencePage_labelWorkingModeEdit;

	public static String FaktorIpsPreferencePage_modifyRuntimeId;

    public static String FaktorIpsPreferencePage_labelMaxHeapSizeIpsTestRunner;
}
