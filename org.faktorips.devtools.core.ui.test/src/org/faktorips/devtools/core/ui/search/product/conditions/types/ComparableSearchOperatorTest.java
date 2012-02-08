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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ComparableSearchOperator;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ComparableSearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.IOperandProvider;
import org.junit.Test;

public class ComparableSearchOperatorTest {

    @Test
    public void testInteger() {
        ComparableSearchOperatorType searchOperatorType = ComparableSearchOperatorType.LESS;
        String fuenf = "5";
        String vier = "4";

        IOperandProvider operandProvider = new IOperandProvider() {

            @Override
            public String getSearchOperand(IProductCmptGeneration productComponentGeneration) {
                return null;
            }
        };

        ComparableSearchOperator searchOperator = new ComparableSearchOperator(new IntegerDatatype(),
                searchOperatorType, operandProvider, fuenf);
        assertTrue(searchOperator.check(vier, null));
        assertFalse(searchOperator.check(fuenf, null));

        searchOperatorType = ComparableSearchOperatorType.GREATER_OR_EQUALS;

        searchOperator = new ComparableSearchOperator(new IntegerDatatype(), searchOperatorType, operandProvider, vier);
        assertTrue(searchOperator.check(vier, null));
        assertTrue(searchOperator.check(fuenf, null));
    }
}
