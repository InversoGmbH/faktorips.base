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

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;


/**
 *
 */
public class ForeignKeysSection extends SimpleIpsPartsSection{

    /**
     * @param parent
     * @param style
     * @param toolkit
     */
    public ForeignKeysSection(ITableStructure table, Composite parent, UIToolkit toolkit) {
        super(table, parent, Messages.ForeignKeysSection_title, toolkit);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection#createIpsPartsComposite(org.eclipse.swt.widgets.Composite, org.faktorips.devtools.core.ui.UIToolkit)
     */
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new ForeignKeysComposite(getIpsObject(), parent, toolkit);
    }
    
    private class ForeignKeysComposite extends IpsPartsComposite {

        public ForeignKeysComposite(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
            super(pdObject, parent, toolkit);
        }

        public ITableStructure getTableStructure() {
            return (ITableStructure)getIpsObject();
        }
        
        /** 
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#createContentProvider()
         */
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }

        protected IIpsObjectPart newIpsPart() {
            return getTableStructure().newForeignKey();
        }

        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new KeyEditDialog((IForeignKey)part, shell);
        }
        
        /**
         * Overridden method.
         * @see org.faktorips.devtools.core.ui.editors.IpsPartsComposite#moveParts(int[], boolean)
         */
        protected int[] moveParts(int[] indexes, boolean up) {
            return getTableStructure().moveForeignKeys(indexes, up);
        }
        
		private class ContentProvider implements IStructuredContentProvider {
			public Object[] getElements(Object inputElement) {
				 return getTableStructure().getForeignKeys();
			}
			public void dispose() {
				// nothing todo
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// nothing todo
			}
		}
	
    } // class ForeignKeysComposite

}
