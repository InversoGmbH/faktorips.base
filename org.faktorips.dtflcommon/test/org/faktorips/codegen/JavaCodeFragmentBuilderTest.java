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

package org.faktorips.codegen;

import java.lang.reflect.Modifier;

import junit.framework.TestCase;

import org.apache.commons.lang.SystemUtils;

public class JavaCodeFragmentBuilderTest extends TestCase {

    public void testAnnotation_AsOneString() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute");
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@XmlAttribute" + SystemUtils.LINE_SEPARATOR, code.getSourcecode());
        
        builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute(name=\"parent-id\")");
        code = builder.getFragment();
        assertEquals("@XmlAttribute(name=\"parent-id\")" + SystemUtils.LINE_SEPARATOR, code.getSourcecode());
        ImportDeclaration imports = new ImportDeclaration();
        imports.add("javax.xml.bind.annotation.XmlAttribute");
        assertEquals(imports, code.getImportDeclaration());
    }

    public void testAnnotation_ClassName_Params() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute", "name=\"parent-id\"");
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@XmlAttribute(name=\"parent-id\")" + SystemUtils.LINE_SEPARATOR, code.getSourcecode());
        ImportDeclaration imports = new ImportDeclaration();
        imports.add("javax.xml.bind.annotation.XmlAttribute");
        assertEquals(imports, code.getImportDeclaration());
    }
    
    public void testAnnotation_AnnotationString_ParamName_ParamValue() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute", "name", "parent-id");
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@XmlAttribute(name=\"parent-id\")" + SystemUtils.LINE_SEPARATOR, code.getSourcecode());
        ImportDeclaration imports = new ImportDeclaration();
        imports.add("javax.xml.bind.annotation.XmlAttribute");
        assertEquals(imports, code.getImportDeclaration());
    }

    public void testAnnotation_Class_Params() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn(Override.class, "someParameters");
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@Override(someParameters)" + SystemUtils.LINE_SEPARATOR, code.getSourcecode());
        ImportDeclaration imports = new ImportDeclaration();
        imports.add(Override.class);
        assertEquals(imports, code.getImportDeclaration());
    }

    public final void testMethodBeginIntStringStringStringArrayStringArray() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.methodBegin(Modifier.PUBLIC, String[].class, "validate", new String[0], new Class[0]);
        StringBuffer buf = new StringBuffer();
        buf.append("public String[] validate()");
        buf.append(SystemUtils.LINE_SEPARATOR);
        buf.append("{");
        assertEquals(buf.toString(), builder.toString().trim());
    }

}
