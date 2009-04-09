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

package org.faktorips.devtools.stdbuilder.enumtype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Builder that generates the java source for <code>EnumType</code> ips objects.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeBuilder extends DefaultJavaSourceFileBuilder {

    /** The package id identifiying the builder */
    public final static String PACKAGE_STRUCTURE_KIND_ID = "EnumTypeBuilder.enumtype.stdbuilder.devtools.faktorips.org"; //$NON-NLS-1$

    /** The builder config property name that indicates whether to use java 5 enum types. */
    private final static String USE_JAVA_ENUM_TYPES_CONFIG_PROPERTY = "useJavaEnumTypes";

    /**
     * Creates a new <code>EnumTypeBuilder</code> that will belong to the given ips artefact builder
     * set.
     * 
     * @param builderSet The ips artefact builder set this builder shall be a part of.
     */
    public EnumTypeBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet, PACKAGE_STRUCTURE_KIND_ID, new LocalizedStringsSet(EnumTypeBuilder.class));
        setMergeEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (ipsSrcFile.getIpsObjectType().equals(IpsObjectType.ENUM_TYPE)) {
            return true;
        }

        return false;
    }

    /** Returns <code>true</code> if Java5 Enums are available. */
    private boolean java5EnumsAvailable() {
        return ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject())
                && getIpsProject().getIpsArtefactBuilderSet().getConfig().getPropertyValueAsBoolean(
                        USE_JAVA_ENUM_TYPES_CONFIG_PROPERTY);
    }

    /** Returns whether to generate an enum. */
    private boolean useEnumGeneration() {
        IEnumType enumType = getEnumType();

        if (enumType.isAbstract()) {
            return false;
        }

        if (java5EnumsAvailable() && enumType.getValuesArePartOfModel()) {
            return true;
        }

        return false;
    }

    /** Returns whether to generate a class. */
    private boolean useClassGeneration() {
        IEnumType enumType = getEnumType();

        if (enumType.isAbstract() && java5EnumsAvailable()) {
            return false;
        }

        if (!(enumType.getValuesArePartOfModel()) || !(java5EnumsAvailable())) {
            return true;
        }

        return false;
    }

    /** Returns whether to generate an interface. */
    private boolean useInterfaceGeneration() {
        if (!(useEnumGeneration()) && !(useClassGeneration())) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateCodeForJavatype() throws CoreException {
        IEnumType enumType = getEnumType();

        // Set general properties
        TypeSection mainSection = getMainTypeSection();
        mainSection.setClass(useClassGeneration());
        mainSection.setEnum(useEnumGeneration());
        int classModifier = (useClassGeneration() && enumType.isAbstract() ? Modifier.PUBLIC | Modifier.ABSTRACT
                : Modifier.PUBLIC);
        mainSection.setClassModifier(classModifier);
        mainSection.setUnqualifiedName(getJavaNamingConvention().getTypeName(enumType.getName()));
        mainSection.getJavaDocForTypeBuilder().javaDoc(enumType.getDescription(), ANNOTATION_GENERATED);

        // Set supertype / implemented interface
        if (enumType.hasSuperEnumType()) {
            IEnumType superEnumType = enumType.findSuperEnumType();
            if (superEnumType != null) {
                if (useEnumGeneration() || useInterfaceGeneration() || (useClassGeneration() && java5EnumsAvailable())) {
                    mainSection.setExtendedInterfaces(new String[] { getQualifiedClassName(superEnumType) });
                } else {
                    mainSection.setSuperClass(getQualifiedClassName(superEnumType));
                }
            }
        }

        generateCodeForEnumValues(mainSection, useEnumGeneration());

        // Generate the attributes and the constructor
        if (useEnumGeneration() || useClassGeneration()) {
            generateCodeForEnumAttributes(mainSection.getMemberVarBuilder());
            generateCodeForConstructor(mainSection.getConstructorBuilder());
        }

        // Generate the methods
        generateCodeForMethods(mainSection.getMethodBuilder());
    }

    /** Generates the java code for the enum values. */
    private void generateCodeForEnumValues(TypeSection mainSection, boolean javaAtLeast5) throws CoreException {
        IEnumType enumType = getEnumType();
        /*
         * If no enum value definition is added we need to make the semicolon anyway telling the
         * compiler that now the attribute section begins.
         */
        boolean appendSemicolon = javaAtLeast5;
        boolean lastEnumValueGenerated = false;

        /*
         * Generate the enum values if they are part of the model and if the enum type is not
         * abstract.
         */
        IEnumAttribute literalNameAttribute = enumType.findLiteralNameAttribute();
        if (enumType.getValuesArePartOfModel() && !(enumType.isAbstract()) && literalNameAttribute != null) {
            // Go over all model side defined enum values
            List<IEnumValue> enumValues = enumType.getEnumValues();
            for (int i = 0; i < enumValues.size(); i++) {
                IEnumValue currentEnumValue = enumValues.get(i);
                // Generate only for valid enum values
                if (currentEnumValue.isValid()) {
                    IEnumAttributeValue currentLiteralNameEnumAttributeValue = currentEnumValue
                            .findEnumAttributeValue(literalNameAttribute);
                    List<IEnumAttributeValue> currentEnumAttributeValues = currentEnumValue.getEnumAttributeValues();
                    if (javaAtLeast5) {
                        lastEnumValueGenerated = (i == enumValues.size() - 1);
                        createEnumValueAsEnumDefinition(currentEnumAttributeValues,
                                currentLiteralNameEnumAttributeValue, lastEnumValueGenerated, mainSection
                                        .getEnumDefinitionBuilder());
                        appendSemicolon = false;
                    } else {
                        createEnumValueAsConstant(currentEnumAttributeValues, currentLiteralNameEnumAttributeValue,
                                mainSection.getConstantBuilder());
                    }
                }

            }
        }

        if (appendSemicolon || (!lastEnumValueGenerated && javaAtLeast5)) {
            mainSection.getEnumDefinitionBuilder().append(';');
        }
    }

    /**
     * Returns a java code fragment that contains the code for the instantiation of the provided
     * enum value of the provided enum type. Code snippet:
     * 
     * <pre>
     *  [javadoc]
     *  Gender.MALE
     * </pre>
     * 
     * @throws CoreException If an exception occurs while processing.
     * @throws NullPointerException If <code>enumType</code> or <code>enumValue</code> is
     *             <code>null</code>.
     */
    public JavaCodeFragment getNewInstanceCodeFragement(IEnumType enumType, IEnumValue enumValue) throws CoreException {
        ArgumentCheck.notNull(new Object[] { enumType, enumValue });

        IEnumAttributeValue attrValue = enumValue.findEnumAttributeValue(enumType.findLiteralNameAttribute());
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(getQualifiedClassName(enumType));
        fragment.append('.');
        fragment.append(getConstantNameForEnumAttributeValue(attrValue));

        return fragment;
    }

    /**
     * Returns the code fragment for a <code>getValueByXXX()</code> method call expression.<br />
     * Code snippet:
     * 
     * <pre>
     *  [javadoc]
     *  Gender.getValueById(&quot;male&quot;)
     * </pre>
     * 
     * @param enumAttribute The enum attribute by that the enum value is searched.
     * @param expressionValue The value of the enum attribute to search the enum value for.
     * 
     * @throws CoreException If an exception occurs while processing.
     * @throws NullPointerException If <code>enumAttribute</code> is <code>null</code>.
     */
    public JavaCodeFragment getValueByXXXCodeFragment(IEnumAttribute enumAttribute, String expressionValue)
            throws CoreException {

        ArgumentCheck.notNull(enumAttribute);

        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(getQualifiedClassName(enumAttribute.getEnumType()));
        fragment.append('.');
        fragment.append(getMethodNameGetValueByXXX(enumAttribute));
        fragment.append("(");
        fragment.append(expressionValue);
        fragment.append(")");

        return fragment;
    }

    /**
     * Returns the method name for the <code>getValueByXXX()</code> method where <code>xxx</code> is
     * a placeholder for the name of a unique identifier enum attribute.
     * 
     * @param uniqueIdentifierEnumAttribute The unique identifier enum attribute.
     * 
     * @throws NullPointerException If <code>uniqueIdentifierAttributeName</code> is
     *             <code>null</code>.
     */
    // TODO aw: why needs this to be public?
    public String getMethodNameGetValueByXXX(IEnumAttribute uniqueIdentifierEnumAttribute) {
        ArgumentCheck.notNull(uniqueIdentifierEnumAttribute);

        String attributeName = uniqueIdentifierEnumAttribute.getName();
        char[] charArray = attributeName.toCharArray();
        charArray[0] = Character.toUpperCase(attributeName.charAt(0));
        return "getValueBy" + String.copyValueOf(charArray);
    }

    /**
     * This method expects the literal name attribute value of an enum value as parameter to provide
     * the accurate constant name for it.
     */
    private String getConstantNameForEnumAttributeValue(IEnumAttributeValue literalNameAttributeValue) {
        if (literalNameAttributeValue.getValue() == null) {
            return "";
        }

        return literalNameAttributeValue.getValue().toUpperCase();
    }

    /**
     * Generates the java source code for an enum value as enum definition (java at least 5).
     * 
     * @see #generateConstant(IEnumType, List, IEnumAttributeValue, JavaCodeFragmentBuilder)
     * 
     * @param enumAttributeValues The enum attribute values of the enum value to create.
     * @param literalEnumAttributeValue The enum attribute value that refers to the literal name
     *            enum attribute.
     * @param lastEnumDefinition Flag indicating whether this enum definition will be the last one.
     * @param enumDefinitionBuilder The java source code builder to use for creating enum
     *            definitions.
     */
    private void createEnumValueAsEnumDefinition(List<IEnumAttributeValue> enumAttributeValues,
            IEnumAttributeValue literalEnumAttributeValue,
            boolean lastEnumDefinition,
            JavaCodeFragmentBuilder enumDefinitionBuilder) throws CoreException {

        // Create enum definition source fragment
        appendLocalizedJavaDoc("ENUMVALUE", getEnumType(), enumDefinitionBuilder);
        JavaCodeFragment enumDefinitionFragment = new JavaCodeFragment();
        enumDefinitionFragment.append(literalEnumAttributeValue.getValue().toUpperCase());
        enumDefinitionFragment.append(" (");
        appendEnumValueParameters(enumAttributeValues, enumDefinitionFragment);
        enumDefinitionFragment.append(')');
        if (!(lastEnumDefinition)) {
            enumDefinitionFragment.append(", ");
            enumDefinitionFragment.appendln(' ');
            enumDefinitionFragment.appendln(' ');
        } else {
            enumDefinitionFragment.append(';');
        }

        enumDefinitionBuilder.append(enumDefinitionFragment);
        enumDefinitionBuilder.appendln();
    }

    /**
     * Generates the java source code for an enum value as constant (java less than 5).
     * 
     * @see #generateEnumDefinition(IEnumType, List, IEnumAttributeValue, boolean,
     *      JavaCodeFragmentBuilder)
     * 
     * @param enumAttributeValues The enum attribute values of the enum value to create.
     * @param literalEnumAttributeValue The enum attribute value that refers to the literal name
     *            enum attribute.
     * @param constantBuilder The java source code builder to use for creating constants.
     */
    private void createEnumValueAsConstant(List<IEnumAttributeValue> enumAttributeValues,
            IEnumAttributeValue literalEnumAttributeValue,
            JavaCodeFragmentBuilder constantBuilder) throws CoreException {

        IEnumType enumType = getEnumType();

        // Build constant source
        appendLocalizedJavaDoc("ENUMVALUE", enumType, constantBuilder);
        JavaCodeFragment initExpression = new JavaCodeFragment();
        initExpression.append("new ");
        initExpression.append(enumType.getName());
        initExpression.append('(');
        appendEnumValueParameters(enumAttributeValues, initExpression);
        initExpression.append(')');

        constantBuilder.varDeclaration(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, enumType.getName(),
                literalEnumAttributeValue.getValue().toUpperCase(), initExpression);
        constantBuilder.appendLn(' ');
    }

    /** Appends the parameter values to an enum value creation code fragment. */
    private void appendEnumValueParameters(List<IEnumAttributeValue> enumAttributeValues,
            JavaCodeFragment javaCodeFragment) throws CoreException {

        int numberEnumAttributeValues = enumAttributeValues.size();
        for (int i = 0; i < numberEnumAttributeValues; i++) {
            IEnumAttributeValue currentEnumAttributeValue = enumAttributeValues.get(i);
            IEnumAttribute referencedEnumAttribute = currentEnumAttributeValue.findEnumAttribute();
            DatatypeHelper datatypeHelper = getIpsProject().findDatatypeHelper(referencedEnumAttribute.getDatatype());
            if (datatypeHelper != null) {
                javaCodeFragment.append(datatypeHelper.newInstance(currentEnumAttributeValue.getValue()));
            }
            if (i < numberEnumAttributeValues - 1) {
                javaCodeFragment.append(", ");
            }
        }
    }

    /** Generates the java code for the attributes. */
    private void generateCodeForEnumAttributes(JavaCodeFragmentBuilder attributeBuilder) throws CoreException {
        for (IEnumAttribute currentEnumAttribute : getEnumType().findAllEnumAttributes()) {
            String attributeName = currentEnumAttribute.getName();
            // The first character will be made lower case
            String codeName = getJavaNamingConvention().getMemberVarName(attributeName);

            if (currentEnumAttribute.isValid()) {
                /*
                 * If the generation artefact is a class and the attribute is inherited do not
                 * generate source code for this attribute because it is also inherited in the
                 * source code. An exception to this is if the values are not defined in the model.
                 */
                if (!(useClassGeneration() && currentEnumAttribute.isInherited())
                        || (useClassGeneration() && !(getEnumType().getValuesArePartOfModel()))) {
                    appendLocalizedJavaDoc("ATTRIBUTE", attributeName, currentEnumAttribute, attributeBuilder);
                    attributeBuilder.varDeclaration(Modifier.PRIVATE | Modifier.FINAL, currentEnumAttribute
                            .getDatatype(), codeName);
                    attributeBuilder.appendLn(' ');
                }
            }
        }
    }

    /** Generates the java code for the constructor. */
    private void generateCodeForConstructor(JavaCodeFragmentBuilder constructorBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        List<IEnumAttribute> enumAttributes = enumType.findAllEnumAttributes();
        List<IEnumAttribute> validEnumAttributes = new ArrayList<IEnumAttribute>();
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {
            if (currentEnumAttribute.isValid()) {
                validEnumAttributes.add(currentEnumAttribute);
            }
        }

        // Build method arguments
        String[] argumentNames = new String[validEnumAttributes.size()];
        String[] argumentClasses = new String[validEnumAttributes.size()];
        JavaNamingConvention javaNamingConvention = getJavaNamingConvention();
        for (int i = 0; i < validEnumAttributes.size(); i++) {
            IEnumAttribute currentEnumAttribute = validEnumAttributes.get(i);
            String attributeName = currentEnumAttribute.getName();
            argumentNames[i] = javaNamingConvention.getMemberVarName(attributeName);
            argumentClasses[i] = currentEnumAttribute.getDatatype();
        }

        // Build method body
        JavaCodeFragment methodBody = new JavaCodeFragment();
        if (useClassGeneration() && enumType.hasSuperEnumType()
                && !(java5EnumsAvailable() && !(enumType.getValuesArePartOfModel()))) {
            createSuperConstructorCall(methodBody);
        }
        createAttributeInitialization(methodBody);

        appendLocalizedJavaDoc("CONSTRUCTOR", enumType.getName(), enumType, constructorBuilder);
        int constructorVisibility = (useClassGeneration() && enumType.isAbstract()) ? Modifier.PROTECTED
                : Modifier.PRIVATE;
        constructorBuilder.methodBegin(constructorVisibility, null, getJavaNamingConvention().getTypeName(
                enumType.getName()), argumentNames, argumentClasses);
        constructorBuilder.append(methodBody);
        constructorBuilder.methodEnd();
    }

    /** Creates the method call of the super constructor. */
    private void createSuperConstructorCall(JavaCodeFragment constructorMethodBody) throws CoreException {
        IEnumType enumType = getEnumType();

        constructorMethodBody.append("super(");

        List<IEnumAttribute> validSupertypeAttributes = new ArrayList<IEnumAttribute>();
        for (IEnumAttribute currentEnumAttribute : enumType.findSuperEnumType().findAllEnumAttributes()) {
            if (currentEnumAttribute.isValid()) {
                validSupertypeAttributes.add(currentEnumAttribute);
            }
        }

        for (int i = 0; i < validSupertypeAttributes.size(); i++) {
            String name = validSupertypeAttributes.get(i).getName();
            constructorMethodBody.append(getJavaNamingConvention().getMemberVarName(name));
            if (i < validSupertypeAttributes.size() - 1) {
                constructorMethodBody.append(", ");
            }
        }

        constructorMethodBody.append(");");
        constructorMethodBody.appendln(' ');
        constructorMethodBody.appendln(' ');
    }

    /** Creates the attribute initialization code for the constructor. */
    private void createAttributeInitialization(JavaCodeFragment constructorMethodBody) throws CoreException {
        /*
         * If an enum is being generated we need to initialize all attributes, on the other hand, if
         * a class is being generated we only initialize the attributes that are not inherited. A
         * class might also be generated when java 5 enums are available but the values are not
         * defined in the model. In that case all attributes need to be initialized, too. Every
         * attribute that shall be initialized must be valid.
         */
        List<IEnumAttribute> attributesToInit = new ArrayList<IEnumAttribute>();
        for (IEnumAttribute currentEnumAttribute : getEnumType().findAllEnumAttributes()) {
            if (currentEnumAttribute.isValid()) {
                if (useEnumGeneration() || (useClassGeneration() && java5EnumsAvailable())
                        || !(currentEnumAttribute.isInherited())) {
                    attributesToInit.add(currentEnumAttribute);
                }
            }
        }

        for (IEnumAttribute currentEnumAttribute : attributesToInit) {
            String currentName = getJavaNamingConvention().getMemberVarName(currentEnumAttribute.getName());
            constructorMethodBody.append("this.");
            constructorMethodBody.append(currentName);
            constructorMethodBody.append(" = ");
            constructorMethodBody.append(currentName);
            constructorMethodBody.append(';');
            constructorMethodBody.appendln();
        }
    }

    /** Generates the java code for the methods. */
    private void generateCodeForMethods(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        generateCodeForGetterMethods(methodBuilder);
        generateCodeForGetValueByMethods(methodBuilder);
    }

    /** Generates the java code for the getter methods. */
    private void generateCodeForGetterMethods(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        List<IEnumAttribute> enumAttributes = enumType.findAllEnumAttributes();

        // Create getters for each attribute
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {

            if (currentEnumAttribute.isValid()) {
                String attributeName = currentEnumAttribute.getName();
                String description = currentEnumAttribute.getDescription();

                Datatype datatype = getIpsProject().findDatatype(currentEnumAttribute.getDatatype());
                String methodName = getJavaNamingConvention().getGetterMethodName(attributeName, datatype);

                // If an interface is to be generated then only generate the method signatures.
                if (useInterfaceGeneration()) {
                    if (!(currentEnumAttribute.isInherited())) {
                        appendLocalizedJavaDoc("GETTER", attributeName, description, currentEnumAttribute,
                                methodBuilder);
                        methodBuilder.signature(Modifier.PUBLIC, currentEnumAttribute.getDatatype(), methodName, null,
                                null);
                        methodBuilder.appendLn(';');
                    }

                } else {
                    if (useEnumGeneration() || !(useClassGeneration() && currentEnumAttribute.isInherited())
                            || useClassGeneration() && !(enumType.getValuesArePartOfModel())) {
                        appendLocalizedJavaDoc("GETTER", attributeName, description, currentEnumAttribute,
                                methodBuilder);

                        // Build method body
                        JavaCodeFragment methodBody = new JavaCodeFragment();
                        methodBody.append("return ");
                        methodBody.append(getJavaNamingConvention().getMemberVarName(attributeName));
                        methodBody.append(';');

                        methodBuilder.methodBegin(Modifier.PUBLIC, currentEnumAttribute.getDatatype(), methodName,
                                null, null);
                        methodBuilder.append(methodBody);
                        methodBuilder.methodEnd();
                    }
                }

            }
        }
    }

    /**
     * Generates the java code for <code>getValueByXXX()</code> methods for each enum value.
     * 
     * Code snippet:
     * 
     * <pre>
     *  [javadoc]
     *  public final static Gender getValueById(String id) {
     *      if (id == null) {
     *          return null;
     *      }
     *      
     *      if (id.equals(&quot;male&quot;)) {
     *          return MALE;
     *      }
     *      if (id.equals(&quot;female&quot;)) {
     *          return FEMALE;
     *      }
     *      
     *      return null;
     *  }
     * </pre>
     */
    private void generateCodeForGetValueByMethods(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        IEnumType enumType = getEnumType();
        if (!(enumType.getValuesArePartOfModel())) {
            return;
        }

        List<IEnumAttribute> uniqueIdentifierAttributes = enumType.findAllEnumAttributes();
        List<IEnumValue> enumValues = enumType.getEnumValues();
        IEnumAttribute literalNameAttribute = enumType.findLiteralNameAttribute();
        if (literalNameAttribute == null) {
            return;
        }

        for (IEnumAttribute currentEnumAttribute : uniqueIdentifierAttributes) {
            if (currentEnumAttribute.isValid()) {
                if (currentEnumAttribute.isUniqueIdentifier()) {

                    Datatype datatype = currentEnumAttribute.findDatatype(getIpsProject());
                    DatatypeHelper helper = getIpsProject().getDatatypeHelper(datatype);

                    JavaCodeFragment body = new JavaCodeFragment();
                    String parameterName = currentEnumAttribute.getName();
                    body.append("if(");
                    body.append(parameterName);
                    body.append(" == null)");
                    body.appendOpenBracket();
                    body.appendln("return null;");
                    body.appendCloseBracket();
                    body.appendln();

                    for (IEnumValue currentEnumValue : enumValues) {
                        if (!(currentEnumValue.isValid())) {
                            continue;
                        }

                        IEnumAttributeValue attributeValue = currentEnumValue
                                .findEnumAttributeValue(currentEnumAttribute);
                        body.append("if (");
                        body.append(parameterName);
                        body.append(".equals(\"");
                        body.append(attributeValue.getValue());
                        body.append("\"))");
                        body.appendOpenBracket();
                        body.append("return ");
                        body.append(getConstantNameForEnumAttributeValue(currentEnumValue
                                .findEnumAttributeValue(literalNameAttribute)));
                        body.append(";");
                        body.appendln();
                        body.appendCloseBracket();
                    }

                    body.appendln();
                    body.append("return null;");

                    appendLocalizedJavaDoc("GETVALUEBY", parameterName, enumType, methodBuilder);
                    methodBuilder.methodBegin(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL,
                            getQualifiedClassName(enumType), getMethodNameGetValueByXXX(currentEnumAttribute),
                            new String[] { parameterName }, new String[] { helper.getJavaClassName() });
                    methodBuilder.append(body);
                    methodBuilder.methodEnd();

                }
            }
        }
    }

    /** Returns the enum type for that code is being generated. */
    private IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

}
