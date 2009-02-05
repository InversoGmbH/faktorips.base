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

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 * 
 * @author Jan Ortmann
 */
public class BehaviourPage extends IpsObjectEditorPage {

    final static String PAGE_ID = "BehaviourPage"; //$NON-NLS-1$

    /**
     * @param editor
     * @param id
     * @param tabPageName
     */
    public BehaviourPage(ProductCmptTypeEditor editor) {
        super(editor, PAGE_ID, Messages.BehaviourPage_title_behaviour);
    }
    
    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(2, true));
        new MethodsAndFormulaSection(getProductCmptType(), formBody, toolkit);
        new TableStructureUsageSection(getProductCmptType(), formBody, toolkit);
    }

}
