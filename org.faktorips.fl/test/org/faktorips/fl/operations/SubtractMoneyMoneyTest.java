/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.CompilerAbstractTest;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.values.Money;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class SubtractMoneyMoneyTest extends CompilerAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[] { new SubtractMoneyMoney() });
    }

    @Test
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("10.12EUR - 8.10EUR", Money.valueOf("2.02EUR"), Datatype.MONEY);
        execAndTestSuccessfull("8.10EUR - 10.12EUR", Money.valueOf("-2.02EUR"), Datatype.MONEY);
    }

    @Test
    public void testLhsError() throws Exception {
        execAndTestFail("a a - 8.10EUR", ExprCompiler.SYNTAX_ERROR);
    }

    @Test
    public void testRhsError() throws Exception {
        execAndTestFail("8.10EUR - a a", ExprCompiler.SYNTAX_ERROR);
    }

}
