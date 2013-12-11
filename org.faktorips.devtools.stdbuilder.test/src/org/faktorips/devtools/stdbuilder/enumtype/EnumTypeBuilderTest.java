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

package org.faktorips.devtools.stdbuilder.enumtype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.ProjectConfigurationUtil;
import org.junit.Before;
import org.junit.Test;

public class EnumTypeBuilderTest extends AbstractStdBuilderTest {

    private final static String ENUM_TYPE_NAME = "TestEnumType";

    private EnumTypeBuilder builder;

    private IEnumType enumType;

    private IEnumAttribute idAttribute;

    private IEnumAttribute nameAttribute;

    private IEnumAttribute inheritedAttribute;

    private IType javaEnum;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        builder = new EnumTypeBuilder(builderSet);

        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        superEnumType.setAbstract(true);
        IEnumAttribute superEnumAttribute = superEnumType.newEnumAttribute();
        superEnumAttribute.setName("superAttribute");
        superEnumAttribute.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        superEnumType.getIpsSrcFile().save(true, null);

        enumType = newEnumType(ipsProject, ENUM_TYPE_NAME);
        enumType.setAbstract(true);
        enumType.setExtensible(false);
        enumType.setSuperEnumType(superEnumType.getQualifiedName());
        idAttribute = enumType.newEnumAttribute();
        idAttribute.setName("id");
        idAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        idAttribute.setUnique(true);
        idAttribute.setIdentifier(true);
        nameAttribute = enumType.newEnumAttribute();
        nameAttribute.setName("name");
        nameAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);
        inheritedAttribute = enumType.newEnumAttribute();
        inheritedAttribute.setName(superEnumAttribute.getName());
        inheritedAttribute.setInherited(true);

        javaEnum = getGeneratedJavaClass(enumType, true, false, ENUM_TYPE_NAME);
    }

    @Test
    public void testBuildEnumTypeWithMissingSupertype() throws CoreException {
        idAttribute.setDatatype("");
        nameAttribute.setDatatype("");
        enumType.setSuperEnumType("MissingType");
        enumType.getIpsSrcFile().save(true, null);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

    @Test
    public void testGetGeneratedJavaElementsForType() {
        generatedJavaElements = builder.getGeneratedJavaElements(enumType);
        assertTrue(generatedJavaElements.contains(javaEnum));
    }

    @Test
    public void testGetGeneratedJavaElementsForAttributeAbstractJava5Enums() throws CoreException {
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, true);

        generatedJavaElements = builder.getGeneratedJavaElements(idAttribute);
        expectMemberVar(idAttribute, false);
        expectGetterMethod(idAttribute, true);
        expectGetValueByMethod(idAttribute, false);
        expectIsValueByMethod(idAttribute, false);

        generatedJavaElements = builder.getGeneratedJavaElements(nameAttribute);
        expectMemberVar(nameAttribute, false);
        expectGetterMethod(nameAttribute, true);
        expectGetValueByMethod(nameAttribute, false);
        expectIsValueByMethod(nameAttribute, false);
    }

    @Test
    public void testGetGeneratedJavaElementsForAttributeAbstract() throws CoreException {
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, false);

        generatedJavaElements = builder.getGeneratedJavaElements(idAttribute);
        expectMemberVar(idAttribute, true);
        expectGetterMethod(idAttribute, true);
        expectGetValueByMethod(idAttribute, false);
        expectIsValueByMethod(idAttribute, false);

        generatedJavaElements = builder.getGeneratedJavaElements(nameAttribute);
        expectMemberVar(nameAttribute, true);
        expectGetterMethod(nameAttribute, true);
        expectGetValueByMethod(nameAttribute, false);
        expectIsValueByMethod(nameAttribute, false);
    }

    @Test
    public void testGetGeneratedJavaElementsForAttributeNotAbstractJava5Enums() throws CoreException {
        enumType.setAbstract(false);
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, true);

        generatedJavaElements = builder.getGeneratedJavaElements(idAttribute);
        expectMemberVar(idAttribute, true);
        expectGetterMethod(idAttribute, true);
        expectGetValueByMethod(idAttribute, true);
        expectIsValueByMethod(idAttribute, true);

        generatedJavaElements = builder.getGeneratedJavaElements(nameAttribute);
        expectMemberVar(nameAttribute, true);
        expectGetterMethod(nameAttribute, true);
        expectGetValueByMethod(nameAttribute, false);
        expectIsValueByMethod(nameAttribute, false);
    }

    @Test
    public void testGetGeneratedJavaElementsForAttributeNotAbstract() throws CoreException {
        enumType.setAbstract(false);
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, false);

        generatedJavaElements = builder.getGeneratedJavaElements(idAttribute);
        expectMemberVar(idAttribute, true);
        expectGetterMethod(idAttribute, true);
        expectGetValueByMethod(idAttribute, true);
        expectIsValueByMethod(idAttribute, true);

        generatedJavaElements = builder.getGeneratedJavaElements(nameAttribute);
        expectMemberVar(nameAttribute, true);
        expectGetterMethod(nameAttribute, true);
        expectGetValueByMethod(nameAttribute, false);
        expectIsValueByMethod(nameAttribute, false);

        generatedJavaElements = builder.getGeneratedJavaElements(inheritedAttribute);
        expectMemberVar(inheritedAttribute, false);
        expectGetterMethod(inheritedAttribute, false);
        expectGetValueByMethod(inheritedAttribute, false);
        expectIsValueByMethod(inheritedAttribute, false);
    }

    @Test
    public void testGetGeneratedJavaElementsForLiteralNameAttributeValue() throws CoreException {
        enumType.setAbstract(false);

        IEnumLiteralNameAttribute literalAttribute = enumType.newEnumLiteralNameAttribute();
        literalAttribute.setDefaultValueProviderAttribute(nameAttribute.getName());
        IEnumValue enumValue = enumType.newEnumValue();
        enumValue.setEnumAttributeValue(0, ValueFactory.createStringValue("id"));
        enumValue.setEnumAttributeValue(1, ValueFactory.createStringValue("name"));
        enumValue.setEnumAttributeValue(2, ValueFactory.createStringValue("false"));
        enumValue.setEnumAttributeValue(3, ValueFactory.createStringValue("NAME"));

        IEnumLiteralNameAttributeValue literalNameAttributeValue = enumValue.getEnumLiteralNameAttributeValue();
        generatedJavaElements = builder.getGeneratedJavaElements(literalNameAttributeValue);
        assertEquals(1, generatedJavaElements.size());
        assertTrue(generatedJavaElements.contains(javaEnum.getField("NAME")));
    }

    @Test
    public void testGenerateMethodeCompareToOtherEnum_withoutContentSeperatedEnum() throws Exception {
        builder.beforeBuild(enumType.getIpsSrcFile(), null);

        assertFalse(builder.isGenerateMethodCompareTo());
    }

    @Test
    public void testGenerateMethodeCompareToOtherEnum_withContentSeperatedEnum() throws Exception {
        enumType.setAbstract(false);
        enumType.setExtensible(true);
        builder.beforeBuild(enumType.getIpsSrcFile(), null);

        assertTrue(builder.isGenerateMethodCompareTo());
    }

    private void expectMemberVar(IEnumAttribute enumAttribute, boolean shallExist) {
        String memberVarName = builder.getMemberVarName(enumAttribute.getName());
        IField memberVar = javaEnum.getField(memberVarName);
        if (shallExist) {
            assertTrue(generatedJavaElements.contains(memberVar));
        } else {
            assertFalse(generatedJavaElements.contains(memberVar));
        }
    }

    private void expectGetterMethod(IEnumAttribute enumAttribute, boolean shallExist) {
        String methodName = builder.getMethodNameGetter(enumAttribute);
        IMethod getterMethod = javaEnum.getMethod(methodName, new String[0]);
        if (shallExist) {
            assertTrue(generatedJavaElements.contains(getterMethod));
        } else {
            assertFalse(generatedJavaElements.contains(getterMethod));
        }
    }

    private void expectGetValueByMethod(IEnumAttribute enumAttribute, boolean shallExist) throws CoreException {
        String methodName = builder.getMethodNameGetValueBy(enumAttribute);
        ValueDatatype datatype = enumAttribute.findDatatype(ipsProject);
        IMethod method = javaEnum.getMethod(methodName, new String[] { "Q" + datatype.getQualifiedName() + ";" });
        if (shallExist) {
            assertTrue(generatedJavaElements.contains(method));
        } else {
            assertFalse(generatedJavaElements.contains(method));
        }
    }

    private void expectIsValueByMethod(IEnumAttribute enumAttribute, boolean shallExist) throws CoreException {
        String methodName = builder.getMethodNameIsValueBy(enumAttribute);
        ValueDatatype datatype = enumAttribute.findDatatype(ipsProject);
        IMethod method = javaEnum.getMethod(methodName, new String[] { "Q" + datatype.getQualifiedName() + ";" });
        if (shallExist) {
            assertTrue(generatedJavaElements.contains(method));
        } else {
            assertFalse(generatedJavaElements.contains(method));
        }
    }

    @Test
    public void testGetLiteralNamesForConstant_empty() throws Exception {
        List<String> literalNames = builder.getLiteralNamesForConstant(new ArrayList<IEnumValue>());

        assertTrue(literalNames.isEmpty());
    }

    @Test
    public void testGetLiteralNamesForConstant_noDublicates() throws Exception {
        ArrayList<IEnumValue> enumValues = new ArrayList<IEnumValue>();
        enumValues.add(mockEnumValue("abc"));
        enumValues.add(mockEnumValue("xyz"));
        List<String> literalNames = builder.getLiteralNamesForConstant(enumValues);

        assertEquals(Arrays.asList("ABC", "XYZ"), literalNames);
    }

    @Test
    public void testGetLiteralNamesForConstant_withTwoDublicates() throws Exception {
        ArrayList<IEnumValue> enumValues = new ArrayList<IEnumValue>();
        enumValues.add(mockEnumValue("_123"));
        enumValues.add(mockEnumValue("-123"));
        List<String> literalNames = builder.getLiteralNamesForConstant(enumValues);

        assertEquals(Arrays.asList("_123_1", "_123_2"), literalNames);
    }

    @Test
    public void testGetLiteralNamesForConstant_withMoreDublicates() throws Exception {
        ArrayList<IEnumValue> enumValues = new ArrayList<IEnumValue>();
        enumValues.add(mockEnumValue("_123"));
        enumValues.add(mockEnumValue("-123"));
        enumValues.add(mockEnumValue("+123"));
        List<String> literalNames = builder.getLiteralNamesForConstant(enumValues);

        assertEquals(Arrays.asList("_123_1", "_123_2", "_123_3"), literalNames);
    }

    @Test
    public void testGetLiteralNamesForConstant_withMoreDublicatesAndOtherValues() throws Exception {
        ArrayList<IEnumValue> enumValues = new ArrayList<IEnumValue>();
        enumValues.add(mockEnumValue("abc"));
        enumValues.add(mockEnumValue("_123"));
        enumValues.add(mockEnumValue("xyz"));
        enumValues.add(mockEnumValue("-123"));
        enumValues.add(mockEnumValue("foo"));
        enumValues.add(mockEnumValue("bar"));
        enumValues.add(mockEnumValue("123"));
        enumValues.add(mockEnumValue("+123"));
        enumValues.add(mockEnumValue("blub"));
        List<String> literalNames = builder.getLiteralNamesForConstant(enumValues);

        assertEquals(Arrays.asList("ABC", "_123_1", "XYZ", "_123_2", "FOO", "BAR", "_123_3", "_123_4", "BLUB"),
                literalNames);
    }

    private IEnumValue mockEnumValue(String id) {
        IEnumValue enumValue = mock(IEnumValue.class);
        IEnumAttributeValue enumAttributeValue = mock(IEnumAttributeValue.class);
        doReturn(ValueFactory.createStringValue(id)).when(enumAttributeValue).getValue();
        when(enumValue.getEnumAttributeValue(null)).thenReturn(enumAttributeValue);
        return enumValue;
    }

    @Test
    public void testGetNewInstanceCodeFragement_inextensibleEnum() throws Exception {
        EnumTypeDatatypeAdapter enumTypeAdapter = new EnumTypeDatatypeAdapter(enumType, null);
        IEnumValue enumValue = enumType.newEnumValue();
        enumType.newEnumLiteralNameAttribute();
        enumValue.getEnumAttributeValues().get(0).setValue(ValueFactory.createStringValue("1"));
        enumValue.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue("name"));
        enumValue.getEnumAttributeValues().get(3).setValue(ValueFactory.createStringValue("ABC"));

        JavaCodeFragment codeFragement = builder.getNewInstanceCodeFragement(enumTypeAdapter, "1");

        assertEquals(ENUM_TYPE_NAME + ".ABC", codeFragement.getSourcecode());
    }

    @Test
    public void testGetNewInstanceCodeFragement_extensibleEnum() throws Exception {
        enumType.setExtensible(true);
        EnumContent enumContent = newEnumContent(enumType, ENUM_TYPE_NAME + "Content");
        EnumTypeDatatypeAdapter enumTypeAdapter = new EnumTypeDatatypeAdapter(enumType, enumContent);
        IEnumValue enumValue = enumType.newEnumValue();
        enumType.newEnumLiteralNameAttribute();
        enumValue.getEnumAttributeValues().get(0).setValue(ValueFactory.createStringValue("1"));
        enumValue.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue("name"));
        enumValue.getEnumAttributeValues().get(3).setValue(ValueFactory.createStringValue("ABC"));

        JavaCodeFragment codeFragement = builder.getNewInstanceCodeFragement(enumTypeAdapter, "1");

        assertEquals(
                "this.getRepository().getEnumValue(TestEnumType.class, (\"1\"==null || \"1\".equals(\"\")) ? null : new Integer(\"1\"))",
                codeFragement.getSourcecode());
    }

    @Test
    public void testGetNewInstanceCodeFragement_extensibleEnumWithoutContent() throws Exception {
        enumType.setExtensible(true);
        EnumTypeDatatypeAdapter enumTypeAdapter = new EnumTypeDatatypeAdapter(enumType, null);
        IEnumValue enumValue = enumType.newEnumValue();
        enumType.newEnumLiteralNameAttribute();
        enumValue.getEnumAttributeValues().get(0).setValue(ValueFactory.createStringValue("1"));
        enumValue.getEnumAttributeValues().get(1).setValue(ValueFactory.createStringValue("name"));
        enumValue.getEnumAttributeValues().get(3).setValue(ValueFactory.createStringValue("ABC"));

        JavaCodeFragment codeFragement = builder.getNewInstanceCodeFragement(enumTypeAdapter, "1");

        assertEquals(ENUM_TYPE_NAME + ".ABC", codeFragement.getSourcecode());
    }

}
