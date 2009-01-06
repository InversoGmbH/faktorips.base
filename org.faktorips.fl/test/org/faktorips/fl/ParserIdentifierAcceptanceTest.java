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

package org.faktorips.fl;

import java.util.Locale;

import junit.framework.TestCase;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;

public class ParserIdentifierAcceptanceTest extends TestCase {

    private ExprCompiler compiler;
    
    public void setUp(){
        compiler = new ExprCompiler();
        Locale.setDefault(Locale.ENGLISH);
        compiler.setLocale(Locale.ENGLISH);
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register("Altersgruppe_1980-01-01", new JavaCodeFragment("Altersgruppe_1980-01-01"), Datatype.STRING);
        resolver.register("Altersgruppe_1980-01", new JavaCodeFragment("Altersgruppe_1980-01"), Datatype.STRING);
        resolver.register("Altersgruppe_1980", new JavaCodeFragment("Altersgruppe_1980"), Datatype.STRING);
        compiler.setIdentifierResolver(resolver);
    }
    
    public void testParserIdentifiers(){
        CompilationResult result = compiler.compile("Altersgruppe_1980-01-01");
        assertTrue(result.successfull());
        
        result = compiler.compile("Altersgruppe_1980-01");
        assertTrue(result.successfull());

        result = compiler.compile("Altersgruppe_1980");
        assertTrue(result.successfull());

        result = compiler.compile("Altersgruppe_1980-01-01-01");
        assertFalse(result.successfull());
    }
    
    public void testParserWithUmlaut() throws Exception{
        compiler = new ExprCompiler();
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register("�", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("�", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("�", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("�", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("�", new JavaCodeFragment("a"), Datatype.INTEGER);
        resolver.register("�", new JavaCodeFragment("a"), Datatype.INTEGER);
        compiler.setIdentifierResolver(resolver);

        CompilationResult result = compiler.compile("1 + � + � + � + � + � + �");
        MessageList msgList = result.getMessages();
        assertTrue(msgList.isEmpty());
    }
}
