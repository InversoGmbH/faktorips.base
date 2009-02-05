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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

/**
 * The ui section for the enum type structure page that contains the enum attributes of the enum
 * type to be edited.
 * 
 * @see org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeStructurePage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributesSection extends SimpleIpsPartsSection {

    /**
     * Creates a new <code>EnumAttributesSection</code> containing the enum attributes of the given
     * enum type.
     * 
     * @param enumType The enum type to show the enum attributes from.
     * @param parent The parent ui composite.
     * @param toolkit The ui toolkit that shall be used to create ui elements.
     */
    public EnumAttributesSection(IEnumType enumType, Composite parent, UIToolkit toolkit) {
        super(enumType, parent, Messages.EnumAttributesSection_title, toolkit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new EnumAttributesComposite((IEnumType)getIpsObject(), parent, toolkit);
    }

    /**
     * A composite that shows an enum type's attributes in a viewer and allows to edit the
     * attributes in a dialog, to create new attributes and to delete attributes.
     */
    private class EnumAttributesComposite extends IpsPartsComposite {

        private IEnumType enumType;

        /**
         * Creates a new <code>EnumAttributesComposite</code> based upon the attributes of the given
         * enum type.
         * 
         * @param enumType The enum type to show the attributes of.
         * @param parent The parent ui composite.
         * @param toolkit The ui toolkit to create ui elements with.
         */
        public EnumAttributesComposite(IEnumType enumType, Composite parent, UIToolkit toolkit) {
            super(enumType, parent, toolkit);

            this.enumType = enumType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new IStructuredContentProvider() {

                public Object[] getElements(Object inputElement) {
                    return enumType.getEnumAttributes().toArray();
                }

                public void dispose() {
                    // nothing todo
                }

                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                    // nothing todo
                }

            };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected IIpsObjectPart newIpsPart() {
            CoreException possibleException;

            try {
                return enumType.newEnumAttribute();
            } catch (CoreException e) {
                possibleException = e;
                IpsPlugin.logAndShowErrorDialog(e);
            }

            throw new RuntimeException(possibleException);
        }

    }

}
