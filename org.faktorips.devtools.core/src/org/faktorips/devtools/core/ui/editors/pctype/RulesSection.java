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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;


/**
 *
 */
public class RulesSection extends SimpleIpsPartsSection {

    public RulesSection(IPolicyCmptType pcType, Composite parent, UIToolkit toolkit) {
        super(pcType, parent, Messages.RulesSection_title, toolkit);
    }
    
    public IPolicyCmptType getPcType() {
        return (IPolicyCmptType)getIpsObject();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection#createIpsPartsComposite(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new RulesComposite((IPolicyCmptType)getIpsObject(), parent, toolkit);
    }

    /**
     * A composite that shows a policy component's rules in a viewer and 
     * allows to edit rules in a dialog, create new rules and delete rules.
     */
    private class RulesComposite extends IpsPartsComposite {

        RulesComposite(IIpsObject pdObject, Composite parent,
                UIToolkit toolkit) {
            super(pdObject, parent, toolkit);
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createContentProvider()
         */
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#newIpsPart()
         */
        protected IIpsObjectPart newIpsPart() {
            return getPcType().newRule();
        }

        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createEditDialog(org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart, org.eclipse.swt.widgets.Shell)
         */
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
        	IValidationRule rule = (IValidationRule)part;
        	if (rule.isCheckValueAgainstValueSetRule()) {
        		String[] attrNames = rule.getValidatedAttributes();
        		if (attrNames.length == 1) {
        			IPolicyCmptTypeAttribute attr = getPcType().getPolicyCmptTypeAttribute(attrNames[0]);
        			if (attr == null) {
        				String msg = NLS.bind(Messages.RulesSection_msgMissingAttribute, attrNames[0]);
        				MessageDialog.openInformation(getShell(), Messages.RulesSection_titleMissingAttribute, msg);
        				rule.delete();
        				return null;
        			}
        			AttributeEditDialog dialog = new AttributeEditDialog(attr, getShell());
        			dialog.showValidationRulePage();
                    dialog.setDataChangeable(isDataChangeable());
        			return dialog;
        		}
        	}
            return new RuleEditDialog((IValidationRule)part, shell);
        }
        
        protected int[] moveParts(int[] indexes, boolean up) {
            return getPcType().moveRules(indexes, up);
        }
        
		private class ContentProvider implements IStructuredContentProvider {
			public Object[] getElements(Object inputElement) {
				 return getPcType().getRules();
			}
			public void dispose() {
				// nothing todo
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// nothing todo
			}
		}
    
    } // class RulesComposite
	

}
