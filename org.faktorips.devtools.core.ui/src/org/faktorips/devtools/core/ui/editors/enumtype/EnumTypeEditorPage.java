/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.ui.actions.EnumImportExportAction;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesSection;
import org.faktorips.devtools.core.ui.editors.type.TypeEditor;
import org.faktorips.devtools.core.ui.editors.type.TypeEditorStructurePage;

/**
 * Base page for <code>IEnumType</code> editors providing controls to edit its 
 * properties and attributes.
 * <p/>
 * This class also adds controls to import an export enum values.
 * 
 * @see EnumTypeEditor
 * 
 * @author Roman Grutza
 * 
 * @since 2.3
 */
public abstract class EnumTypeEditorPage extends TypeEditorStructurePage {

    /** The enum type the enum type editor this page belongs to is currently editing. */
    // package access for EnumTypeStructurePage, EnumTypeValuesPage
    IEnumType enumType;

    /** Values section showing the enumType */
    // package access for EnumTypeStructurePage, EnumTypeValuesPage
    EnumValuesSection enumValuesSection;
    
    /** Actions corresponding to the toolbar items */
    private EnumImportExportActionInEditor importAction;
    private EnumImportExportActionInEditor exportAction;
    
    /** to toggle import/export actions on page change */
    protected ContentsChangeListener changeListener;

    public EnumTypeEditorPage(TypeEditor editor, IEnumType type, boolean twoSectionsWhenTrueOtherwiseFour, String title) {
        super(editor, twoSectionsWhenTrueOtherwiseFour, title);
        
        this.enumType = type;
        changeListener = new ContentsChangeListener() {
            public void contentsChanged(ContentChangeEvent event) {
                if (event.getIpsSrcFile().equals(enumType.getIpsSrcFile()) ) {
                    updateToolbarActionEnabledStates();
                }
            }
        };
        enumType.getIpsModel().addChangeListener(changeListener);
    }

    @Override
    public void dispose() {
        super.dispose();
        enumType.getIpsModel().removeChangeListener(changeListener);
    }

    /** Creates actions for import and export */
    protected void createToolbarActions() {
        importAction = new EnumImportExportActionInEditor(getSite().getShell(),
                enumType, true);
        exportAction = new EnumImportExportActionInEditor(getSite().getShell(),
                enumType, false);
        
        addActionsToToolbar();
    }

    /** Creates toolbar items to trigger im- and export operations */
    private void addActionsToToolbar() {
        ScrolledForm form = getManagedForm().getForm();
        form.getToolBarManager().add(importAction);
        form.getToolBarManager().add(exportAction);
        
        form.updateToolBar();
        
        updateToolbarActionEnabledStates();
    }

    /** Enable im/export operations if the enum type's values are part of the model */
    protected void updateToolbarActionEnabledStates() {
        boolean enableImportExportActions = enumType.isContainingValues();
        
        importAction.setEnabled(enableImportExportActions);
        exportAction.setEnabled(enableImportExportActions);
    }
    
    /** 
     * Extend <code>EnumImportExportAction</code> in order to react to import operations 
     * and update the view after the operation is completed.
     */
    class EnumImportExportActionInEditor extends EnumImportExportAction {
        public EnumImportExportActionInEditor(Shell shell, IEnumValueContainer enumValueContainer, boolean isImport) {
            super(shell, enumValueContainer);
            if (isImport) {
                initImportAction();
            } else {
                initExportAction();
            }            
        }

        /**
         * {@inheritDoc}
         */
        public void run(IStructuredSelection selection) {
            if (super.runInternal(selection)) {
                if (enumValuesSection != null) {
                    enumValuesSection.refresh();
                }
            }
        }        
    }
}
