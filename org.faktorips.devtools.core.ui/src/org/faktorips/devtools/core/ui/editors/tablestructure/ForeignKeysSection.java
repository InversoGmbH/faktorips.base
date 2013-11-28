/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

public class ForeignKeysSection extends SimpleIpsPartsSection {

    public ForeignKeysSection(ITableStructure table, Composite parent, UIToolkit toolkit) {
        super(table, parent, null, ExpandableComposite.TITLE_BAR, Messages.ForeignKeysSection_title, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new ForeignKeysComposite(getIpsObject(), parent, toolkit);
    }

    private class ForeignKeysComposite extends IpsPartsComposite {

        public ForeignKeysComposite(IIpsObject pdObject, Composite parent, UIToolkit toolkit) {
            super(pdObject, parent, getSite(), toolkit);
        }

        public ITableStructure getTableStructure() {
            return (ITableStructure)getIpsObject();
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return getTableStructure().newForeignKey();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            // return new KeyEditDialog((IForeignKey)part, shell);
            return new ForeignKeyEditDialog((IForeignKey)part, shell);
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getTableStructure().moveForeignKeys(indexes, up);
        }

        private class ContentProvider implements IStructuredContentProvider {
            @Override
            public Object[] getElements(Object inputElement) {
                return getTableStructure().getForeignKeys();
            }

            @Override
            public void dispose() {
                // nothing todo
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing todo
            }
        }

    }

}
