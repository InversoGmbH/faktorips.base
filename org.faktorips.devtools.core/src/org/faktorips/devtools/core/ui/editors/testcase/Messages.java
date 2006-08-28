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

package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.osgi.util.NLS;

/**
 * @author Joerg Ortmann
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.testcase.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String TestCaseEditor_Title;
	public static String TestCaseEditor_Title_Failure;
	public static String TestCaseEditor_Title_Error;
	public static String TestCaseEditor_Title_Success;
	public static String TestCaseEditor_Input_Title;
	public static String TestCaseEditor_ExpectedResult_Title;
	public static String TestCaseEditor_Input_SectionTitle;
	public static String TestCaseEditor_Input_Description;
	public static String TestCaseEditor_Combined_Title;
	public static String TestCaseEditor_Combined_SectionTitle;
	public static String TestCaseEditor_Combined_Description;
	public static String TestCaseEditor_ExpectedResult_SectionTitle;
	public static String TestCaseEditor_ExpectedResult_Description;
	public static String TestCaseLabelProvider_undefined;
	public static String TestCaseSection_ButtonAdd;
	public static String TestCaseSection_ButtonRemove;
	public static String TestCaseSection_ButtonProductCmpt;
	public static String TestCaseDetailArea_SectionValuesText;
	public static String TestCaseSection_Error_CreatingRelation;
	public static String TestCaseSection_DialogSelectProductCmpt_Title;
	public static String TestCaseSection_DialogSelectProductCmpt_Description;
	public static String TestCaseSection_DialogSelectTestRelation_Title;
	public static String TestCaseSection_DialogSelectTestRelation_Description;
	public static String TestPolicyCmptSelectionDialog_Title;
	public static String TestPolicyCmptSelectionDialog_Error_NoTestPolicyCmptFound;
	public static String TestPolicyCmptSelectionDialog_Description;
	public static String TestPolicyCmptSelectionDialog_Error_WrongType;
	
	public static String TestCaseSection_FailureFormat_FailureIn;
	public static String TestCaseSection_FailureFormat_Actual;
	public static String TestCaseSection_FailureFormat_Expected;
	public static String TestCaseSection_FailureFormat_Attribute;
	public static String TestCaseSection_FailureFormat_Object;
    public static String TestCaseSection_FailureFormat_Message;
    public static String TestCaseSection_FilterInput;
    public static String TestCaseSection_FilterInput_ToolTip;
    public static String TestCaseSection_FilterExpected;
    public static String TestCaseSection_FilterExpected_ToolTip;
    public static String TestCaseSection_FilterCombined;
    public static String TestCaseSection_FilterCombined_ToolTip;
    public static String TestCaseSection_Dialog_SaveBeforeStartTest;
    public static String TestCaseSection_Dialog_SaveBeforeStartTest_Title;
    public static String TestCaseSection_ToolBar_WithoutRelation;
    public static String TestCaseSection_ToolBar_FlatStructure;
    public static String TestCaseSection_ToolBar_RunTest;
}
