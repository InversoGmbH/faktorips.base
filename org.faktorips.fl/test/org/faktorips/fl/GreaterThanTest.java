package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.datatype.Datatype;


/**
 *
 */
public class GreaterThanTest extends CompilerAbstractTest {
    
    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("7.45 > 3.45", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.45 > 7.45", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.45 > 7.46", Boolean.FALSE, Datatype.BOOLEAN);
    }

    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("7.1 > 7", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.0 > 7", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.0 > 8", Boolean.FALSE, Datatype.BOOLEAN);
    }
    
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("7 > 6.5", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 > 7.0", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 > 7.1", Boolean.FALSE, Datatype.BOOLEAN);
    }
    
    public void testDecimalInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7.2 > WHOLENUMBER(7.0)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.0 > WHOLENUMBER(7.0)", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.1 > WHOLENUMBER(8.0)", Boolean.FALSE, Datatype.BOOLEAN);
    }
    
    public void testIntegerDecimal() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.0) > 6.5", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7.0) > 7.0", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7.0) > 8.0", Boolean.FALSE, Datatype.BOOLEAN);
    }
    
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("7 > 3", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 > 7", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 > 8", Boolean.FALSE, Datatype.BOOLEAN);
    }
    
    public void testIntInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7 > WHOLENUMBER(3)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 > WHOLENUMBER(7)", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 > WHOLENUMBER(8)", Boolean.FALSE, Datatype.BOOLEAN);
    }
    
    public void testIntegerInt() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7 > WHOLENUMBER(3)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 > WHOLENUMBER(7)", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 > WHOLENUMBER(8)", Boolean.FALSE, Datatype.BOOLEAN);
    }
    
    public void testMoneyMoney() throws Exception {
        execAndTestSuccessfull("3.50EUR > 2.40EUR", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("3.50EUR > 3.50EUR", Boolean.FALSE, Datatype.BOOLEAN);
    }
    
    
}
