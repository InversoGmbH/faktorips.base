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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.AbstractCheckbox;
import org.faktorips.devtools.core.ui.editors.type.MethodEditDialog;

public class ProductCmptTypeMethodEditDialog extends MethodEditDialog {

    public ProductCmptTypeMethodEditDialog(IProductCmptTypeMethod method, Shell parentShell) {
        super(method, parentShell);
    }
    
    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        
        Composite c = super.createWorkArea(parent);
        
        nameText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(method.getName())) {
                    method.setName(getProductCmptTypeMethod().getDefaultMethodName());
                }
            }
        });
        
        return c;
    }

    private IProductCmptTypeMethod getProductCmptTypeMethod(){
        return (IProductCmptTypeMethod)method;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void createAdditionalControlsOnGeneralPage(Composite parent, UIToolkit toolkit) {
        Composite group = toolkit.createGroup(parent, Messages.ProductCmptTypeMethodEditDialog_formulaGroup);
        AbstractCheckbox checkbox = toolkit.createCheckbox(group, Messages.ProductCmptTypeMethodEditDialog_formulaCheckbox);
        bindingContext.bindContent(checkbox, method, IProductCmptTypeMethod.PROPERTY_FORMULA_SIGNATURE_DEFINITION);

        AbstractCheckbox overloadsFormula = toolkit.createCheckbox(group, Messages.ProductCmptTypeMethodEditDialog_labelOverloadsFormula);
        bindingContext.bindContent(overloadsFormula, method, IProductCmptTypeMethod.PROPERTY_OVERLOADS_FORMULA);

        Composite area = uiToolkit.createLabelEditColumnComposite(group);
        toolkit.createLabel(area, Messages.ProductCmptTypeMethodEditDialog_formulaNameLabel);
        Text formulaNameText = toolkit.createText(area);
        bindingContext.bindContent(formulaNameText, method, IProductCmptTypeMethod.PROPERTY_FORMULA_NAME);

    }
    
    public void contentsChanged(ContentChangeEvent event) {
        super.contentsChanged(event);
        if (event.getIpsSrcFile().equals(getIpsPart().getIpsSrcFile())) {
            IProductCmptTypeMethod tMethod = (IProductCmptTypeMethod)method;
            datatypeControl.setVoidAllowed(!tMethod.isFormulaSignatureDefinition());
            datatypeControl.setOnlyValueDatatypesAllowed(tMethod.isFormulaSignatureDefinition());
        }
    }

}
