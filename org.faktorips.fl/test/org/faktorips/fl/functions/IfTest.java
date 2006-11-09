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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.DefaultIdentifierResolver;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;

/**
 *
 */
public class IfTest extends FunctionAbstractTest {
    
    protected void setUp() throws Exception {
        super.setUp();
        registerFunction(new If("IF", ""));
    }
    
    public void testBooleanCondition() throws Exception {
        registerFunction(new BooleanFct("TRUEOBJ", Boolean.TRUE));
        registerFunction(new BooleanFct("FALSEOBJ", Boolean.FALSE));
        execAndTestSuccessfull("IF(1=1; 2.1; 3.2)", Decimal.valueOf("2.1"), Datatype.DECIMAL);
        execAndTestSuccessfull("IF(TRUEOBJ(); 2; 3)", new Integer(2), Datatype.INTEGER);
        execAndTestSuccessfull("IF(FALSEOBJ(); 2; 3)", new Integer(3), Datatype.INTEGER);
    }
    
    public void testDifferentDatatypes() throws Exception {
        execAndTestFail("IF(1=2; 10EUR; 1)", If.ERROR_MESSAGE_CODE);
    }

    public void testDecimal() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2.1; 3.2)", Decimal.valueOf("2.1"), Datatype.DECIMAL);
        execAndTestSuccessfull("IF(1=2; 2.1; 3.2)", Decimal.valueOf("3.2"), Datatype.DECIMAL);
    }
    
    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2.1; 3)", Decimal.valueOf("2.1"), Datatype.DECIMAL);
        execAndTestSuccessfull("IF(1=2; 2.1; 3)", Decimal.valueOf("3"), Datatype.DECIMAL);
    }
    
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2; 3.1)", Decimal.valueOf("2"), Datatype.DECIMAL);
        execAndTestSuccessfull("IF(1=2; 2; 3.1)", Decimal.valueOf("3.1"), Datatype.DECIMAL);
    }
    
    public void testInt() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2; 3)", new Integer(2), Datatype.INTEGER);
        execAndTestSuccessfull("IF(1=2; 2; 3)", new Integer(3), Datatype.INTEGER);
    }
    
    public void testInteger() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2; 3)", new Integer(2), Datatype.INTEGER);
        execAndTestSuccessfull("IF(1=2; 2; 3)", new Integer(3), Datatype.INTEGER);
    }
    
    public void testMoney() throws Exception {
        execAndTestSuccessfull("IF(1=1; 10EUR; 20EUR)", Money.euro(10, 0), Datatype.MONEY);
        execAndTestSuccessfull("IF(1=2; 10EUR; 20EUR)", Money.euro(20, 0), Datatype.MONEY);
    }
    
    public void testString() throws Exception {
        execAndTestSuccessfull("IF(1=1; \"a\"; \"b\")", "a", Datatype.STRING);
        execAndTestSuccessfull("IF(1=2; \"a\"; \"b\")", "b", Datatype.STRING);
    }
    
    public void testCombinations() throws Exception {
        execAndTestSuccessfull("IF(1=1; 2; 3) + 1", new Integer(3), Datatype.INTEGER);
        execAndTestSuccessfull("IF(1=1; 2.1 + 1; 3) + 2", Decimal.valueOf("5.1"), Datatype.DECIMAL);
        execAndTestSuccessfull("IF(1=1; 2; 3) + 10 + IF(1=2; 2; 3) ", new Integer(15), Datatype.INTEGER);
        execAndTestSuccessfull("IF(1=1; IF(1=2; 2; 30); 3) + 1", new Integer(31), Datatype.INTEGER);
    }
    
    public void testIdentifierInFunction() throws Exception{
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        compiler.setIdentifierResolver(resolver);
        resolver.register("param1", new JavaCodeFragment(StringUtils.escape("1")), Datatype.PRIMITIVE_INT);
        resolver.register("param2", new JavaCodeFragment(StringUtils.escape("2")), Datatype.PRIMITIVE_INT);
        resolver.register("param3", new JavaCodeFragment(StringUtils.escape("3")), Datatype.PRIMITIVE_INT);
        resolver.register("param4", new JavaCodeFragment(StringUtils.escape("4")), Datatype.PRIMITIVE_INT);
        CompilationResult result = execAndTestSuccessfull("IF(param1=param2; param3; param4)", new Integer(4), Datatype.INTEGER);
        List identifier = Arrays.asList(result.getIdentifiersUsed());
        assertTrue(identifier.contains("param1"));
        assertTrue(identifier.contains("param2"));
        assertTrue(identifier.contains("param3"));
        assertTrue(identifier.contains("param4"));        
    }
}
