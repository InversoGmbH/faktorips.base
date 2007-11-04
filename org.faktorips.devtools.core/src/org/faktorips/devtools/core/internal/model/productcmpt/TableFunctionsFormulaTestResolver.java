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

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestCase;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.FlFunction;

/**
 * Resolver to resolve functions for the table formula test fl function adapter.
 */
public class TableFunctionsFormulaTestResolver extends TableUsageFunctionsResolver {

    private IFormulaTestCase formulaTestCase;
    
    public TableFunctionsFormulaTestResolver(
            IIpsProject ipsProject, 
            ITableContentUsage[] tableContentUsages) {
        
        super(ipsProject, tableContentUsages);
    }

    public TableFunctionsFormulaTestResolver(IIpsProject ipsProject, ITableContentUsage[] tableContentUsages, IFormulaTestCase formulaTestCase) {
        super(ipsProject, tableContentUsages);
        this.formulaTestCase = formulaTestCase;
    }

    /**
     * {@inheritDoc}
     */
    protected FlFunction createFlFunctionAdapter(
            ITableContents tableContents, 
            ITableAccessFunction function, 
            String roleName){
        
        return new TableFunctionFormulaTestFlFunctionAdapter(tableContents, function, formulaTestCase, roleName, getIpsProject());
    }
}
