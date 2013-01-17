/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

/**
 * A section to display and edit a type's attributes.
 */
public abstract class AttributesSection extends SimpleIpsPartsSection {

    protected AttributesSection(IType type, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
        super(type, parent, site, ExpandableComposite.TITLE_BAR, Messages.AttributesSection_title, toolkit);
    }

    protected IType getType() {
        return (IType)getIpsObject();
    }

    /**
     * A composite that shows a policy component's attributes in a viewer and allows to edit
     * attributes in a dialog, create new attributes and delete attributes.
     */
    protected abstract class AttributesComposite extends IpsPartsComposite {

        private IpsAction openEnumTypeAction;
        private Button overrideButton;

        protected AttributesComposite(IType type, Composite parent, UIToolkit toolkit) {
            super(type, parent, getSite(), true, true, true, true, true, true, true, true, toolkit);
            openEnumTypeAction = new OpenEnumerationTypeInNewEditor(getViewer());
        }

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            return getType().newAttribute();
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            return getType().moveAttributes(indexes, up);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new AttributeContentProvider();
        }

        @Override
        protected void createContextMenuThis(MenuManager contextMenuManager) {
            contextMenuManager.add(new Separator());
            contextMenuManager.add(openEnumTypeAction);
        }

        @Override
        protected void openLink() {
            openEnumTypeAction.run();
        }

        @Override
        public void setDataChangeable(boolean flag) {
            super.setDataChangeable(flag);
            overrideButton.setEnabled(flag);
        }

        @Override
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
            super.createButtons(buttons, toolkit);
            createButtonSpace(buttons, toolkit);

            overrideButton = toolkit.createButton(buttons, Messages.AttributesSection_OverrideButton);
            overrideButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING));
            overrideButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    overrideClicked();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // Nothing to do
                }
            });
            updateOverrideButtonEnabledState();

            return true;
        }

        protected void updateOverrideButtonEnabledState() {
            try {
                boolean supertypeExisting = getType().hasExistingSupertype(getType().getIpsProject());
                overrideButton.setEnabled(supertypeExisting);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        private void overrideClicked() {
            OverrideAttributeDialog dialog = new OverrideAttributeDialog(getType(), getShell());
            if (dialog.open() == Window.OK) {
                getType().overrideAttributes(dialog.getSelectedParts());
                refresh();
            }
        }

        private class AttributeContentProvider implements IStructuredContentProvider {

            @Override
            public Object[] getElements(Object inputElement) {
                return getType().getAttributes().toArray();
            }

            @Override
            public void dispose() {
                // Nothing to do
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do
            }

        }

        private class OpenEnumerationTypeInNewEditor extends IpsAction {

            public OpenEnumerationTypeInNewEditor(ISelectionProvider selectionProvider) {
                super(selectionProvider);
                setText(Messages.AttributesSection_openEnumContentInNewEditor);
            }

            @Override
            protected boolean computeEnabledProperty(IStructuredSelection selection) {
                Object selected = selection.getFirstElement();
                if (!(selected instanceof IAttribute)) {
                    return false;
                }

                IAttribute attribute = (IAttribute)selected;
                try {
                    Datatype datatype = attribute.findDatatype(attribute.getIpsProject());
                    return datatype instanceof EnumTypeDatatypeAdapter;
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void run(IStructuredSelection selection) {
                Object selected = selection.getFirstElement();
                if (!(selected instanceof IAttribute)) {
                    return;
                }

                IAttribute attribute = (IAttribute)selected;
                try {
                    Datatype datatype = attribute.findDatatype(attribute.getIpsProject());
                    if (datatype instanceof EnumTypeDatatypeAdapter) {
                        EnumTypeDatatypeAdapter enumDatatype = (EnumTypeDatatypeAdapter)datatype;
                        IpsUIPlugin.getDefault().openEditor(enumDatatype.getEnumValueContainer());
                    }
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }

}
