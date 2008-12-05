/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.bf.ui.properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.faktorips.devtools.bf.ui.edit.NodeEditPart;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;

/**
 * A section that is displayed in the property view. The properties of a business function
 * call action can be edited with it. 
 * 
 * @author Peter Erzberger
 */
public class CallBusinessFunctionActionPropertySection extends AbstractPropertySection {

    private BusinessFunctionRefControl businessFunctionField;
    protected BindingContext bindingContext;
    protected UIToolkit uiToolkit;

    @Override
    public void aboutToBeHidden() {
    }

    @Override
    public void aboutToBeShown() {
    }

    @Override
    public final void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        parent.setLayout(new GridLayout(1, true));
        uiToolkit = new UIToolkit(new FormToolkit(parent.getDisplay()));
        Composite panel = uiToolkit.createGridComposite(parent, 1, true, true);
        bindingContext = new BindingContext();
        Composite content = uiToolkit.createLabelEditColumnComposite(panel);
        uiToolkit.createLabel(content, Messages.getString("CallBusinessFunctionActionPropertySection.bfLabel")); //$NON-NLS-1$
        businessFunctionField = new BusinessFunctionRefControl(content, uiToolkit);
        GridData data = new GridData();
        data.grabExcessHorizontalSpace = false;
        data.widthHint = 300;
        businessFunctionField.setLayoutData(data);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public IBFElement getBFElement() {
        return ((NodeEditPart)((IStructuredSelection)getSelection()).getFirstElement()).getBFElement();
    }

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        businessFunctionField.setIpsProject(getBFElement().getIpsProject());
        businessFunctionField.setCurrentBusinessFunction(getBFElement().getBusinessFunction());
        bindingContext.removeBindings(businessFunctionField);
        bindingContext.bindContent(businessFunctionField, getBFElement(), IActionBFE.PROPERTY_TARGET);
        bindingContext.updateUI();
    }

}
