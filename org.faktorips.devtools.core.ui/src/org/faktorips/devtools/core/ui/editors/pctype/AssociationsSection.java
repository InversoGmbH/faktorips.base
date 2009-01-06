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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.AssociationsLabelProvider;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.editors.pctype.associationwizard.NewPcTypeAssociationWizard;
import org.faktorips.util.memento.Memento;

/**
 * A section to display and edit a type's relations.
 */
public class AssociationsSection extends SimpleIpsPartsSection {

    public AssociationsSection(
            IPolicyCmptType pcType, 
            Composite parent,
            UIToolkit toolkit) {
        super(pcType, parent, Messages.AssociationsSection_title, toolkit);
    }
    
    public IPolicyCmptType getPcType() {
        return (IPolicyCmptType)getIpsObject();
    }

	/** 
     * {@inheritDoc}
	 */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new AssociationsComposite((IPolicyCmptType)getIpsObject(), parent, toolkit);
    }

    /*
     * Action to open the selected target in a new editor window
     */
    private class OpenTargetPcTypeInEditorAction extends IpsAction {
        public OpenTargetPcTypeInEditorAction(ISelectionProvider selectionProvider) {
            super(selectionProvider);
            setText(Messages.AssociationsSection_menuOpenTargetInNewEditor);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            return (selected instanceof IPolicyCmptTypeAssociation);
        }

        /** 
         * {@inheritDoc}
         */
        public void run(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            if (selected instanceof IPolicyCmptTypeAssociation) {
                IPolicyCmptTypeAssociation policyCmptTypeAssociation = (IPolicyCmptTypeAssociation)selected;
                try {
                    IType target = policyCmptTypeAssociation.findTarget(getPcType().getIpsProject());
                    IpsUIPlugin.getDefault().openEditor(target);
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }
    }
    
    /**
     * A composite that shows a policy component's associations in a viewer and 
     * allows to edit associations in a dialog, create new associations and delete associations.
     */
    private class AssociationsComposite extends IpsPartsComposite {
    	private Button wizardNewButton;
        private OpenTargetPcTypeInEditorAction openAction ;
        
        AssociationsComposite(IIpsObject pdObject, Composite parent,
                UIToolkit toolkit) {
        	// create default buttons without the new button, 
        	//   because the new button will be overridden with wizard functionality
            super(pdObject, parent, false, true, true, true, true, toolkit);
            openAction = new OpenTargetPcTypeInEditorAction(getViewer());
            buildContextMenu();
        }

        private void buildContextMenu() {
            final MenuManager menuManager = new MenuManager();
            menuManager.setRemoveAllWhenShown(true);
            // display menu only if one element is selected
            menuManager.addMenuListener(new IMenuListener(){
                public void menuAboutToShow(IMenuManager manager) {
                    ISelection selection = getViewer().getSelection();
                    if (selection.isEmpty()){
                        return;
                    }
                    menuManager.add(openAction);
                }
            });
            
            Menu menu = menuManager.createContextMenu(getViewer().getControl());
            getViewer().getControl().setMenu(menu);
        }
        
        /** 
         * {@inheritDoc}
         */
        protected IStructuredContentProvider createContentProvider() {
            return new RelationContentProvider();
        }

        /**
         * {@inheritDoc}
         */
        protected ILabelProvider createLabelProvider() {
            return new AssociationsLabelProvider();
        }
        
        /** 
         * {@inheritDoc}
         */
        protected IIpsObjectPart newIpsPart() {
            return getPcType().newPolicyCmptTypeAssociation();
        }
        
        /**
         * {@inheritDoc}
         */
        public void setDataChangeable(boolean flag) {
            super.setDataChangeable(flag);
            getUiToolkit().setDataChangeable(wizardNewButton, flag);
        }

        /** 
         * {@inheritDoc}
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AssociationEditDialog((IPolicyCmptTypeAssociation)part, shell);
        }
        
        /**
         * {@inheritDoc}
         */
        protected int[] moveParts(int[] indexes, boolean up) {
            return getPcType().moveAssociations(indexes, up);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
        	createNewWizardButton(buttons, toolkit);
        	super.createButtons(buttons, toolkit);
    		return true;
        }
        
        /**
         * Creates the "New..." button to initiate the new-relation-wizard.
         */
        private void createNewWizardButton(Composite buttons, UIToolkit toolkit) {
        	wizardNewButton = toolkit.createButton(buttons, Messages.AssociationsSection_newButton);
        	wizardNewButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
    		wizardNewButton.addSelectionListener(new SelectionListener() {
    			public void widgetSelected(SelectionEvent e) {
    				try {
    					newWizardClicked();
    				} catch (Exception ex) {
    					IpsPlugin.logAndShowErrorDialog(ex);
    				}
    			}
    			public void widgetDefaultSelected(SelectionEvent e) {
    			}
    		});
        }
		
        /**
         * Open the new-association-wizard
         */
        private void newWizardClicked() {
            IIpsSrcFile file = getIpsObject().getIpsSrcFile();
            boolean dirty = file.isDirty();
            Memento memento = getIpsObject().newMemento();
            IIpsObjectPart newRelation = newIpsPart();
            WizardDialog dialog = new WizardDialog(getShell(), new NewPcTypeAssociationWizard((IPolicyCmptTypeAssociation)newRelation));
            dialog.open();
            if (dialog.getReturnCode()==Window.CANCEL) {
                getIpsObject().setState(memento);
                if (!dirty) {
                    file.markAsClean();
                }
            }
            refresh();
        }
        
        /**
         * {@inheritDoc}
         */
        protected void openLink() {
            openAction.run();
        }

        private class RelationContentProvider implements IStructuredContentProvider {
    		public Object[] getElements(Object inputElement) {
    			 return getPcType().getPolicyCmptTypeAssociations();
    		}
    		public void dispose() {
    			// nothing todo
    		}
    		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    			// nothing todo
    		}
    	}

	}
}
