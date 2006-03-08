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

package org.faktorips.fl.functions;

import org.faktorips.fl.CompilationResult;
import org.faktorips.values.Decimal;

/**
 *
 */
public class DecimalTestArrayFctTest extends FunctionAbstractTest {
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void test() throws Exception {
        DecimalTestArrayFct testFct = new DecimalTestArrayFct();
        testFct.setValues(new Decimal[]{Decimal.valueOf(10, 0), Decimal.valueOf(32, 0)});
        registerFunction(testFct);
        CompilationResult result = compiler.compile("DECIMALTESTARRAY()");
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
    }
    
    public void testNull() throws Exception {
        DecimalTestArrayFct testFct = new DecimalTestArrayFct();
        testFct.setValues(null);
        registerFunction(testFct);
        CompilationResult result = compiler.compile("DECIMALTESTARRAY()");
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
    }
    
    
}
