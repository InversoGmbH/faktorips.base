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

package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.datatype.Datatype;
import org.junit.Test;

/**
 *
 */
public class GreaterThanOrEqualTest extends CompilerAbstractTest {
    @Test
    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("7.45 >= 3.45", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.45 >= 7.45", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.45 >= 7.46", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("7.1 >= 7", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.0 >= 7", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("6.9 >= 7", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("7 >= 6.5", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 >= 7.0", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 >= 7.1", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testDecimalInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7.2 >= WHOLENUMBER(7.1)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.0 >= WHOLENUMBER(7.1)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("6.9 >= WHOLENUMBER(7.1)", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntegerDecimal() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.1) >= 6.5", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7.1) >= 7.0", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7.1) >= 7.1", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("7 >= 3", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 >= 7", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 >= 8", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7 >= WHOLENUMBER(3)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 >= WHOLENUMBER(7)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 >= WHOLENUMBER(8)", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntegerInt() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7) >= 3", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) >= 7", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) >= 8", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testIntegerInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7) >= WHOLENUMBER(3)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) >= WHOLENUMBER(7)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) >= WHOLENUMBER(8)", Boolean.FALSE, Datatype.BOOLEAN);
    }

    @Test
    public void testMoneyMoney() throws Exception {
        execAndTestSuccessfull("3.50EUR >= 2.40EUR", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("3.50EUR >= 3.50EUR", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("3.50EUR >= 3.51EUR", Boolean.FALSE, Datatype.BOOLEAN);
    }

}