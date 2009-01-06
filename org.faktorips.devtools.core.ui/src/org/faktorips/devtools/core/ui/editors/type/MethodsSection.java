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

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;


/**
 * A section to display and edit a type's methods.
 */
public class MethodsSection extends SimpleIpsPartsSection {
    
    public MethodsSection(
            IType type, 
            Composite parent, 
            UIToolkit toolkit) {
        super(type, parent, Messages.MethodsSection_title, toolkit);
    }
    
    /** 
     * {@inheritDoc}
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new MethodsComposite((IType)getIpsObject(), parent, toolkit);
    }
    
    protected EditDialog createEditDialog(IMethod method, Shell shell) {
        return new MethodEditDialog(method, shell);
    }

    /**
     * Creates the override dialog. This method is called when the override button is clicked.
     * @param type the IType upon which the methods to override are calculated
     * @param shell the shell which is need by the dialog
     * @return the override dialog 
     */
    protected OverrideMethodDialog createOverrideMethodDialog(IType type, Shell shell){
        return new OverrideMethodDialog(type, shell);
    }
    
    /**
     * A composite that shows a policy component's methods in a viewer and 
     * allows to edit methods in a dialog, create new methods and delete methods.
     */
    class MethodsComposite extends IpsPartsComposite {
        
        private Button overrideButton;

        public MethodsComposite(IType pcType, Composite parent, UIToolkit toolkit) {
            super(pcType, parent, toolkit);
        }
        
        public IType getType() {
            return (IType)getIpsObject();
        }
        
        /** 
         * {@inheritDoc}
         */
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
            super.createButtons(buttons, toolkit);
            createButtonSpace(buttons, toolkit);
    		overrideButton = toolkit.createButton(buttons, Messages.MethodsSection_button);
    		overrideButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
    		overrideButton.addSelectionListener(new SelectionListener() {
    			public void widgetSelected(SelectionEvent e) {
    				try {
    					overrideClicked();
    				} catch (Exception ex) {
    					IpsPlugin.logAndShowErrorDialog(ex);
    				}
    			}
    			public void widgetDefaultSelected(SelectionEvent e) {
    			}
    		});
    		return true;
        }
        
        private void overrideClicked() {
            try {
            	OverrideMethodDialog dialog = createOverrideMethodDialog(getType(), getShell());
                if (dialog.open()==Window.OK) {
                    getType().overrideMethods(dialog.getSelectedMethods());
                }
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
        
        /** 
         * {@inheritDoc}
         */
        protected IStructuredContentProvider createContentProvider() {
            return new MethodContentProvider();
        }

        /** 
         * {@inheritDoc}
         */
        protected IIpsObjectPart newIpsPart() {
            return getType().newMethod();
        }

        /** 
         * {@inheritDoc}
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return MethodsSection.this.createEditDialog((IMethod)part, shell);
        }

        /**
         * {@inheritDoc}
         */
        protected int[] moveParts(int[] indexes, boolean up) {
            return getType().moveMethods(indexes, up);
        }
        
        /**
         * {@inheritDoc}
         */
        public void setDataChangeable(boolean flag) {
            super.setDataChangeable(flag);
            overrideButton.setEnabled(flag);
        }

        private class MethodContentProvider implements IStructuredContentProvider {
    		public Object[] getElements(Object inputElement) {
    			return getType().getMethods();
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
