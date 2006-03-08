/*******************************************************************************
�* Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
�*
�* Alle Rechte vorbehalten.
�*
�* Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
�* Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
�* Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
�* genutzt werden, die Bestandteil der Auslieferung ist und auch unter
�* � http://www.faktorips.org/legal/cl-v01.html
�* eingesehen werden kann.
�*
�* Mitwirkende:
�* � Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
�*
�*******************************************************************************/

package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.datatype.Datatype;


/**
 *
 */
public class NotEqualsTest extends CompilerAbstractTest {
    
    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("7.45 != 7.45", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.45 != 3.45", Boolean.TRUE, Datatype.BOOLEAN);
    }

    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("7.0 != 7", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.0 != 8", Boolean.TRUE, Datatype.BOOLEAN);
    }
    
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("7 != 7.0", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 != 7.1", Boolean.TRUE, Datatype.BOOLEAN);
    }
    
    public void testDecimalInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7.0 != WHOLENUMBER(7.0)", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.1 != WHOLENUMBER(8.0)", Boolean.TRUE, Datatype.BOOLEAN);
    }
    
    public void testIntegerDecimal() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.0) != 7.0", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7.0) != 8.0", Boolean.TRUE, Datatype.BOOLEAN);
    }
    
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("7 != 7", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 != 8", Boolean.TRUE, Datatype.BOOLEAN);
    }
    
    public void testIntInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7 != WHOLENUMBER(7)", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 != WHOLENUMBER(8)", Boolean.TRUE, Datatype.BOOLEAN);
    }
    
    public void testIntegerInt() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7) != 7", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) != 8", Boolean.TRUE, Datatype.BOOLEAN);
    }
    
    public void testIntegerInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7) != WHOLENUMBER(7)", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) != WHOLENUMBER(8)", Boolean.TRUE, Datatype.BOOLEAN);
    }
    
    public void testMoneyMoney() throws Exception {
        execAndTestSuccessfull("3.50EUR != 3.50EUR", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("3.50EUR != 2.40EUR", Boolean.TRUE, Datatype.BOOLEAN);
    }
    
    
}
