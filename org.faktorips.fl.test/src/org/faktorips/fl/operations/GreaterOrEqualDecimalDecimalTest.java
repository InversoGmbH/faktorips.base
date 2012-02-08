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

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.CompilerAbstractTest;
import org.faktorips.fl.ExprCompiler;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class GreaterOrEqualDecimalDecimalTest extends CompilerAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[] { new GreaterThanOrEqualDecimalDecimal() });
    }

    @Test
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("3.5 >= 3.4", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("3.5 >= 3.5", Boolean.TRUE, Datatype.BOOLEAN);
    }

    @Test
    public void testLhsError() throws Exception {
        execAndTestFail("a a >= 3.5", ExprCompiler.SYNTAX_ERROR);
    }

    @Test
    public void testRhsError() throws Exception {
        execAndTestFail("3 >= a a", ExprCompiler.SYNTAX_ERROR);
    }

}
