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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;

/**
 * 
 */
public class FaktorIpsPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public FaktorIpsPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createFieldEditors() {
		
		StringFieldEditor workingDateField = new StringFieldEditor(
				IpsPreferences.WORKING_DATE,
				Messages.FaktorIpsPreferencePage_labelWorkingDate,
				getFieldEditorParent());
		addField(workingDateField);
        
		StringFieldEditor nullRepresentation = new StringFieldEditor(
				IpsPreferences.NULL_REPRESENTATION_STRING,
				Messages.FaktorIpsPreferencePage_labelNullValue,
				getFieldEditorParent());
		addField(nullRepresentation);

		StringFieldEditor productCmptPostfixField = new StringFieldEditor(
				IpsPreferences.DEFAULT_PRODUCT_CMPT_TYPE_POSTFIX,
				Messages.FaktorIpsPreferencePage_labelProductTypePostfix,
				getFieldEditorParent());
		addField(productCmptPostfixField);

		IChangesOverTimeNamingConvention[] conventions = IpsPlugin.getDefault()
				.getIpsModel().getChangesOverTimeNamingConvention();
		String[][] nameValues = new String[conventions.length][2];
		for (int i = 0; i < conventions.length; i++) {
			nameValues[i][0] = conventions[i].getName();
			nameValues[i][1] = conventions[i].getId();
		}
		ComboFieldEditor changeOverTimeField = new ComboFieldEditor(
				IpsPreferences.CHANGES_OVER_TIME_NAMING_CONCEPT,
				Messages.FaktorIpsPreferencePage_labelNamingScheme, nameValues,
				getFieldEditorParent());
		addField(changeOverTimeField);

        StringFieldEditor ipsTestRunnerMaxHeapSize = new StringFieldEditor(
                IpsPreferences.IPSTESTRUNNER_MAX_HEAP_SIZE,
                Messages.FaktorIpsPreferencePage_labelMaxHeapSizeIpsTestRunner,
                getFieldEditorParent());
        addField(ipsTestRunnerMaxHeapSize);
        
        String label = NLS.bind(Messages.FaktorIpsPreferencePage_labelEditRecentGenerations, IpsPlugin.getDefault()
				.getIpsPreferences().getChangesOverTimeNamingConvention()
				.getGenerationConceptNamePlural());
		BooleanFieldEditor editRecentGernations = new BooleanFieldEditor(
				IpsPreferences.EDIT_RECENT_GENERATION, label, getFieldEditorParent());
		addField(editRecentGernations);

		label = NLS.bind(Messages.FaktorIpsPreferencePage_labelEditGenerationsWithSuccessor, IpsPlugin.getDefault()
				.getIpsPreferences().getChangesOverTimeNamingConvention()
				.getGenerationConceptNamePlural());
		BooleanFieldEditor editGernationsWithSuccessor = new BooleanFieldEditor(
				IpsPreferences.EDIT_GENERATION_WITH_SUCCESSOR, label, getFieldEditorParent());
		addField(editGernationsWithSuccessor);

		BooleanFieldEditor editRuntimeId = new BooleanFieldEditor(
				IpsPreferences.MODIFY_RUNTIME_ID, Messages.FaktorIpsPreferencePage_modifyRuntimeId, getFieldEditorParent());
		addField(editRuntimeId);

		BooleanFieldEditor enableGeneratingField = new BooleanFieldEditor(
				IpsPreferences.ENABLE_GENERATING,
				Messages.FaktorIpsPreferencePage_FaktorIpsPreferencePage_enableGenerating, getFieldEditorParent());
		addField(enableGeneratingField);

		BooleanFieldEditor canNavigateToModel = new BooleanFieldEditor(
				IpsPreferences.NAVIGATE_TO_MODEL,
				Messages.FaktorIpsPreferencePage_labelCanNavigateToModel, getFieldEditorParent());
		addField(canNavigateToModel);
		
		RadioGroupFieldEditor workingMode = new RadioGroupFieldEditor(
				IpsPreferences.WORKING_MODE, Messages.FaktorIpsPreferencePage_titleWorkingMode, 2, new String[][] {
						{ Messages.FaktorIpsPreferencePage_labelWorkingModeBrowse, IpsPreferences.WORKING_MODE_BROWSE },
						{ Messages.FaktorIpsPreferencePage_labelWorkingModeEdit, IpsPreferences.WORKING_MODE_EDIT } },
				getFieldEditorParent(), true);
		addField(workingMode);
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(IWorkbench workbench) {
		setPreferenceStore(IpsPlugin.getDefault().getPreferenceStore());
	}

}
