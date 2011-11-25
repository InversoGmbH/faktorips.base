/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;

public class TypeSelectionPage extends WizardPage {

    private final ResourceManager resourManager;
    private final NewProductCmptPMO pmo;
    private final BindingContext bindingContext;
    private TypeSelectionUpdater listInputUpdater;

    public TypeSelectionPage(NewProductCmptPMO pmo) {
        super("New Product Component");
        this.pmo = pmo;
        setTitle("Which kind of product component do you want to create?");
        resourManager = new LocalResourceManager(JFaceResources.getResources());
        bindingContext = new BindingContext();
        pmo.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (isCurrentPage()) {
                    getContainer().updateButtons();
                }
            }
        });
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = toolkit.createGridComposite(parent, 1, false, false);
        Composite twoColumnComposite = toolkit.createLabelEditColumnComposite(composite);

        // Select Project
        toolkit.createLabel(twoColumnComposite, "Project:");
        IpsProjectRefControl ipsProjectRefControl = toolkit.createIpsProjectRefControl(twoColumnComposite);

        TypeSelectionComposite typeSelectionComposite = new TypeSelectionComposite(composite, toolkit);
        typeSelectionComposite.setTitle("Type:");

        setControl(composite);

        bindControls(ipsProjectRefControl, typeSelectionComposite);
        bindingContext.updateUI();
    }

    void bindControls(IpsProjectRefControl ipsProjectRefControl, final TypeSelectionComposite typeSelectionComposite) {
        bindingContext.bindContent(ipsProjectRefControl, pmo, NewProductCmptPMO.PROPERTY_IPSPROJECT);

        typeSelectionComposite.addDoubleClickListener(new DoubleClickListener(this));
        listInputUpdater = new TypeSelectionUpdater(typeSelectionComposite, pmo);
        pmo.addPropertyChangeListener(listInputUpdater);
        listInputUpdater.updateListViewer();

        bindingContext.bindContent(typeSelectionComposite.getListViewerField(), pmo,
                NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE);

    }

    @Override
    public boolean isPageComplete() {
        return pmo.getIpsProject() != null && pmo.getSelectedBaseType() != null;
    }

    @Override
    public void dispose() {
        super.dispose();
        resourManager.dispose();
        bindingContext.dispose();
        pmo.removePropertyChangeListener(listInputUpdater);
    }

    private static class TypeSelectionUpdater implements PropertyChangeListener {

        private final NewProductCmptPMO pmo;
        private final TypeSelectionComposite typeSelectionComposite;

        public TypeSelectionUpdater(TypeSelectionComposite typeSelectionComposite, NewProductCmptPMO pmo) {
            this.typeSelectionComposite = typeSelectionComposite;
            this.pmo = pmo;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(NewProductCmptPMO.PROPERTY_IPSPROJECT)) {
                updateListViewer();
            }
            if (NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE.equals(evt.getPropertyName())) {
                if (pmo.getSelectedBaseType() == null) {
                    typeSelectionComposite.setDescriptionTitle(StringUtils.EMPTY);
                    typeSelectionComposite.setDescription(StringUtils.EMPTY);
                } else {
                    typeSelectionComposite.setDescriptionTitle(IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(
                            pmo.getSelectedBaseType()));
                    typeSelectionComposite.setDescription(IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(
                            pmo.getSelectedBaseType()));
                }
            }
        }

        void updateListViewer() {
            typeSelectionComposite.setListInput(pmo.getBaseTypes());
        }

    }

    private static class DoubleClickListener implements IDoubleClickListener {

        private final TypeSelectionPage page;

        public DoubleClickListener(TypeSelectionPage page) {
            this.page = page;
        }

        @Override
        public void doubleClick(DoubleClickEvent event) {
            page.getWizard().getContainer().showPage(page.getNextPage());

        }
    }

}