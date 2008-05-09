/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.runtime.IModelObjectChangedEvent;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ModelObjectChangedEvent;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.valueset.EnumValueSet;

/**
 * Code generator for a changeable attribute.
 * 
 * @author Jan Ortmann
 */
public class GenChangeableAttribute extends GenAttribute {

    // if the attribute's datatype is a primitive type, this datatype is the wrapper datatype for
    // the primitive
    // otherwise its the attribute's original datatype.
    protected DatatypeHelper wrapperDatatypeHelper;

    public GenChangeableAttribute(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAttribute a,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(genPolicyCmptType, a, stringsSet);
        ArgumentCheck.isTrue(a.isChangeable());
        wrapperDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(a.getIpsProject(), datatypeHelper);
    }

    /**
     * Generates the source code for the ips object part this is a generator for.
     */
    public void generateCodeForProductCmptType(boolean generatesInterface,
            IIpsProject ipsProject,
            TypeSection mainSection) throws CoreException {
        if (!generatesInterface) {
            generateMemberVariablesForProductCmptType(mainSection.getMemberVarBuilder(), ipsProject, generatesInterface);
        }
        generateMethodsForProductCmptType(mainSection.getMethodBuilder(), ipsProject, generatesInterface);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (isOverwritten()) {
            return;
        }
        if (isPublished() == generatesInterface) {
            generateAttributeNameConstant(builder);
            if (isRangeValueSet()) {
                generateFieldMaxRange(builder);
            } else if (isEnumValueSet()) {
                generateFieldMaxAllowedValuesFor(builder);
            }
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public void setPremium(Money newValue)
     * </pre>
     */
    protected void generateSetterSignature(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getMethodNametSetPropertyValue(attributeName, datatypeHelper.getDatatype());
        String paramName = getParamNameForSetterMethod();
        methodsBuilder.signature(modifier, "void", methodName, new String[] { paramName },
                new String[] { getJavaClassName() });
    }

    protected String getMethodNametSetPropertyValue() {
        return getJavaNamingConvention().getSetterMethodName(attribute.getName(), datatypeHelper.getDatatype());
    }

    /**
     * Returns the name of the parameter in the setter method for a property, e.g. newValue.
     */
    protected String getParamNameForSetterMethod() {
        return getLocalizedText("PARAM_NEWVALUE_NAME", attributeName);
    }

    protected void generateFieldMaxRange(JavaCodeFragmentBuilder membersBuilder) {
        appendLocalizedJavaDoc("FIELD_MAX_RANGE_FOR", attributeName, membersBuilder);
        IRangeValueSet range = (IRangeValueSet)getPolicyCmptTypeAttribute().getValueSet();
        JavaCodeFragment containsNullFrag = new JavaCodeFragment();
        containsNullFrag.append(range.getContainsNull());
        JavaCodeFragment frag = wrapperDatatypeHelper.newRangeInstance(createCastExpression(range.getLowerBound()),
                createCastExpression(range.getUpperBound()), createCastExpression(range.getStep()), containsNullFrag,
                isUseTypesafeCollections());
        membersBuilder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                | java.lang.reflect.Modifier.STATIC, wrapperDatatypeHelper
                .getRangeJavaClassName(isUseTypesafeCollections()), getFieldNameMaxRange(), frag);
    }

    protected String getFieldNameMaxRange() {
        return getLocalizedText("FIELD_MAX_RANGE_FOR_NAME", StringUtils.upperCase(attributeName));
    }

    protected void generateFieldMaxAllowedValuesFor(JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc("FIELD_MAX_ALLOWED_VALUES_FOR", attributeName, attribute.getDescription(), builder);
        String[] valueIds = EMPTY_STRING_ARRAY;
        boolean containsNull = false;
        if (getPolicyCmptTypeAttribute().getValueSet() instanceof IEnumValueSet) {
            IEnumValueSet set = (IEnumValueSet)getPolicyCmptTypeAttribute().getValueSet();
            valueIds = set.getValues();
            containsNull = set.getContainsNull();
        } else if (getDatatype() instanceof EnumDatatype) {
            valueIds = ((EnumDatatype)getDatatype()).getAllValueIds(true);
            containsNull = true;
        } else {
            throw new IllegalArgumentException("This method can only be call with a value for parameter 'a' "
                    + "that is an IAttibute that bases on an EnumDatatype or contains an EnumValueSet.");
        }
        JavaCodeFragment frag = null;
        if (getDatatype().isPrimitive()) {
            containsNull = false;
        }
        frag = wrapperDatatypeHelper.newEnumValueSetInstance(valueIds, containsNull, isUseTypesafeCollections());
        builder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                | java.lang.reflect.Modifier.STATIC,
                isUseTypesafeCollections() ? Java5ClassNames.OrderedValueSet_QualifiedName + "<"
                        + wrapperDatatypeHelper.getJavaClassName() + ">" : EnumValueSet.class.getName(),
                getFieldNameMaxAllowedValues(), frag);
    }

    protected String getFieldNameMaxAllowedValues() {
        return getLocalizedText("FIELD_MAX_ALLOWED_VALUES_FOR_NAME", StringUtils.upperCase(attributeName));
    }

    private JavaCodeFragment createCastExpression(String bound) {
        JavaCodeFragment frag = new JavaCodeFragment();
        if (StringUtils.isEmpty(bound)) {
            frag.append('(');
            frag.appendClassName(wrapperDatatypeHelper.getJavaClassName());
            frag.append(')');
        }
        frag.append(wrapperDatatypeHelper.newInstance(bound));
        return frag;
    }

    protected boolean isRangeValueSet() {
        return ValueSetType.RANGE == getPolicyCmptTypeAttribute().getValueSet().getValueSetType();
    }

    protected boolean isEnumValueSet() {
        return ValueSetType.ENUM == getPolicyCmptTypeAttribute().getValueSet().getValueSetType();
    }

    protected boolean isAllValuesValueSet() {
        return ValueSetType.ALL_VALUES == getPolicyCmptTypeAttribute().getValueSet().getValueSetType();
    }

    protected boolean isNotAllValuesValueSet() {
        return ValueSetType.ALL_VALUES != getPolicyCmptTypeAttribute().getValueSet().getValueSetType();
    }

    /**
     * Generates the signature for the method to access an attribute's set of allowed values.
     * 
     * @param datatype
     */
    public void generateSignatureGetAllowedValuesFor(Datatype datatype, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String methodName = getMethodNameGetAllowedValuesFor(datatype);
        methodsBuilder.signature(Modifier.PUBLIC,
                isUseTypesafeCollections() ? Java5ClassNames.OrderedValueSet_QualifiedName + "<"
                        + datatype.getJavaClassName() + ">" : EnumValueSet.class.getName(), methodName,
                new String[] { "businessFunction" }, new String[] { String.class.getName() });
    }

    public String getMethodNameGetAllowedValuesFor(Datatype datatype) {
        return getJavaNamingConvention().getGetterMethodName(
                getLocalizedText("METHOD_GET_ALLOWED_VALUES_FOR_NAME", StringUtils
                        .capitalize(getPolicyCmptTypeAttribute().getName())), datatype);
    }

    public void generateMethodGetAllowedValuesFor(Datatype datatype, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_ALLOWED_VALUES_FOR", getPolicyCmptTypeAttribute().getName(), methodsBuilder);
        generateSignatureGetAllowedValuesFor(datatype, methodsBuilder);
        methodsBuilder.append(';');
    }

    public String getMethodNameGetRangeFor(Datatype datatype) {
        return getJavaNamingConvention().getGetterMethodName(
                getLocalizedText("METHOD_GET_RANGE_FOR_NAME", StringUtils.capitalize(getPolicyCmptTypeAttribute()
                        .getName())), datatype);
    }

    public void generateMethodGetRangeFor(DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_RANGE_FOR", getPolicyCmptTypeAttribute().getName(), methodsBuilder);
        generateSignatureGetRangeFor(helper, methodsBuilder);
        methodsBuilder.append(';');
    }

    public void generateSignatureGetRangeFor(DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String methodName = getMethodNameGetRangeFor(helper.getDatatype());
        String rangeClassName = helper.getRangeJavaClassName(isUseTypesafeCollections());
        methodsBuilder.signature(Modifier.PUBLIC, rangeClassName, methodName, new String[] { "businessFunction" },
                new String[] { String.class.getName() });
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public Integer getDefaultMinAge()
     * </pre>
     */
    void generateSignatureGetDefaultValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder)
            throws CoreException {
        String methodName = getMethodNameGetDefaultValue(datatypeHelper);
        builder.signature(Modifier.PUBLIC, datatypeHelper.getJavaClassName(), methodName, EMPTY_STRING_ARRAY,
                EMPTY_STRING_ARRAY);
    }

    /**
     * Returns the name of the method that returns the default value for the attribute.
     */
    public String getMethodNameGetDefaultValue(DatatypeHelper datatypeHelper) {
        return getJavaNamingConvention().getGetterMethodName(getPropertyNameDefaultValue(),
                datatypeHelper.getDatatype());
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        if (!generatesInterface) {
            if (isOverwritten()) {
                return;
            }
            generateField(builder);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariablesForProductCmptType(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        if (!generatesInterface) {
            generateFieldDefaultValue(datatypeHelper, builder);

            // if the datatype is a primitive datatype the datatypehelper will be switched to
            // the
            // helper of the
            // wrapper type
            wrapperDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(ipsProject, datatypeHelper);
            if (isRangeValueSet()) {
                generateFieldRangeFor(wrapperDatatypeHelper, builder);
            } else if (isEnumValueSet()) {
                generateFieldAllowedValuesFor(builder);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (!generatesInterface) {
            if (isOverwritten()) {
                return;
            }
            if (isRangeValueSet()) {
                generateMethodGetRange(builder, ipsProject);
            } else if (isEnumValueSet()) {
                generateMethodGetAllowedValues(builder, ipsProject);
            }
            generateGetterImplementation(builder);
            generateSetterMethod(builder);
        }

        if (generatesInterface) {
            if (isOverwritten()) {
                return;
            }
            generateGetterInterface(builder);
            generateSetterInterface(builder);
            if (isRangeValueSet()) {
                generateMethodGetRangeFor(wrapperDatatypeHelper, builder);
            } else if (isEnumValueSet()) {
                generateMethodGetAllowedValuesFor(wrapperDatatypeHelper.getDatatype(), builder);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMethodsForProductCmptType(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        if (!generatesInterface) {
            generateMethodGetDefaultValue(datatypeHelper, builder, generatesInterface);

            // if the datatype is a primitive datatype the datatypehelper will be switched to the
            // helper of the
            // wrapper type
            wrapperDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(ipsProject, datatypeHelper);
            if (isRangeValueSet()) {
                generateMethodGetRangeForProd(wrapperDatatypeHelper, builder);
            } else if (isEnumValueSet()) {
                generateMethodGetAllowedValuesForProd(wrapperDatatypeHelper.getDatatype(), builder);
            }
        }

        if (generatesInterface) {
            generateMethodGetDefaultValue(datatypeHelper, builder, generatesInterface);

            // if the datatype is a primitive datatype the datatypehelper will be switched to the
            // helper of the wrapper type
            wrapperDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(ipsProject, datatypeHelper);
            if (isEnumValueSet()) {
                generateMethodGetAllowedValuesFor(wrapperDatatypeHelper.getDatatype(), builder);
            } else if (isRangeValueSet()) {
                generateMethodGetRangeFor(wrapperDatatypeHelper, builder);
            }
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public Integer getDefaultMinAge() {
     *     return minAge;
     * </pre>
     */
    private void generateMethodGetDefaultValue(DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder methodsBuilder,
            boolean generatesInterface) throws CoreException {
        if (!generatesInterface) {
            methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
            generateSignatureGetDefaultValue(datatypeHelper, methodsBuilder);
            methodsBuilder.openBracket();
            methodsBuilder.append("return ");
            methodsBuilder.append(getFieldNameDefaultValue());
            methodsBuilder.append(';');
            methodsBuilder.closeBracket();
        }

        if (generatesInterface) {
            appendLocalizedJavaDoc("METHOD_GET_DEFAULTVALUE", getPolicyCmptTypeAttribute().getName(), methodsBuilder);
            generateSignatureGetDefaultValue(datatypeHelper, methodsBuilder);
            methodsBuilder.append(';');
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public void setPremium(Money newValue) {
     *     this.premium = newValue;
     * }
     * </pre>
     */
    protected void generateSetterMethod(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSetterSignature(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("this.");
        methodsBuilder.append(getMemberVarName());
        methodsBuilder.appendln(" = " + getParamNameForSetterMethod() + ";");
        generateChangeListenerSupport(methodsBuilder, IModelObjectChangedEvent.class.getName(),
                "MUTABLE_PROPERTY_CHANGED", getMemberVarName());
        methodsBuilder.closeBracket();
    }

    protected void generateChangeListenerSupport(JavaCodeFragmentBuilder methodsBuilder,
            String eventClassName,
            String eventConstant,
            String fieldName) {
        generateChangeListenerSupport(methodsBuilder, eventClassName, eventConstant, fieldName, null);
    }

    protected void generateChangeListenerSupport(JavaCodeFragmentBuilder methodsBuilder,
            String eventClassName,
            String eventConstant,
            String fieldName,
            String paramName) {
        methodsBuilder.appendln("if (" + MethodNames.EXISTS_CHANGE_LISTENER_TO_BE_INFORMED + "()) {");
        methodsBuilder.append(MethodNames.NOTIFIY_CHANGE_LISTENERS + "(new ");
        methodsBuilder.appendClassName(ModelObjectChangedEvent.class);
        methodsBuilder.append("(this, ");
        methodsBuilder.appendClassName(eventClassName);
        methodsBuilder.append('.');
        methodsBuilder.append(eventConstant);
        methodsBuilder.append(", ");
        methodsBuilder.appendQuoted(fieldName);
        if (paramName != null) {
            methodsBuilder.append(", ");
            methodsBuilder.append(paramName);
        }
        methodsBuilder.appendln("));");
        methodsBuilder.appendln("}");
    }

    private void generateMethodGetRange(JavaCodeFragmentBuilder methodBuilder, IIpsProject ipsProject)
            throws CoreException {
        methodBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRangeFor(wrapperDatatypeHelper, methodBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        if (getPolicyCmptTypeAttribute().isProductRelevant() && getProductCmptType(ipsProject) != null) {
            generateGenerationAccess(body, ipsProject);
            body.append(getMethodNameGetRangeFor(wrapperDatatypeHelper.getDatatype()));
            body.appendln("(businessFunction);");
        } else {
            body.append(getFieldNameMaxRange());
            body.appendln(";");

        }
        body.appendCloseBracket();
        methodBuilder.append(body);
    }

    private void generateGenerationAccess(JavaCodeFragment body, IIpsProject ipsProject) throws CoreException {
        GenProductCmptType genProductCmptType = getGenPolicyCmptType().getBuilderSet().getGenerator(
                getProductCmptType(ipsProject));
        if (isPublished()) {
            body.append(genProductCmptType.getMethodNameGetProductCmptGeneration());
            body.append("().");
        } else { // Public
            body.append("((");
            body.append(getProductCmptType(ipsProject).getName()
                    + getGenPolicyCmptType().getAbbreviationForGenerationConcept());
            body.append(")");
            body.append(genProductCmptType.getMethodNameGetProductCmptGeneration());
            body.append("()).");
        }
    }

    private void generateMethodGetAllowedValues(JavaCodeFragmentBuilder methodBuilder, IIpsProject ipsProject)
            throws CoreException {
        methodBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetAllowedValuesFor(wrapperDatatypeHelper.getDatatype(), methodBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        if (isNotAllValuesValueSet() && isConfigurableByProduct() && getProductCmptType(ipsProject) != null) {
            generateGenerationAccess(body, ipsProject);
            body.append(getMethodNameGetAllowedValuesFor(wrapperDatatypeHelper.getDatatype()));
            body.appendln("(businessFunction);");
        } else {
            body.append(getFieldNameMaxAllowedValues());
            body.appendln(";");
        }
        body.appendCloseBracket();
        methodBuilder.append(body);
    }

    private void generateMethodGetRangeForProd(DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRangeFor(helper, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameRangeFor());
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }

    private void generateMethodGetAllowedValuesForProd(Datatype datatype, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc("{@inheritDoc}", JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetAllowedValuesFor(datatype, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameAllowedValuesFor());
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }

    public String getFieldNameRangeFor(IPolicyCmptTypeAttribute a) {
        return getLocalizedText("FIELD_RANGE_FOR_NAME", StringUtils.capitalize(a.getName()));
    }

    private void generateFieldRangeFor(DatatypeHelper helper, JavaCodeFragmentBuilder memberVarBuilder) {
        appendLocalizedJavaDoc("FIELD_RANGE_FOR", getPolicyCmptTypeAttribute().getName(), memberVarBuilder);
        memberVarBuilder.varDeclaration(Modifier.PRIVATE, helper.getRangeJavaClassName(isUseTypesafeCollections()),
                getFieldNameRangeFor());
    }

    private void generateFieldAllowedValuesFor(JavaCodeFragmentBuilder memberVarBuilder) {
        appendLocalizedJavaDoc("FIELD_ALLOWED_VALUES_FOR", getPolicyCmptTypeAttribute().getName(), memberVarBuilder);
        memberVarBuilder.varDeclaration(Modifier.PRIVATE,
                isUseTypesafeCollections() ? Java5ClassNames.OrderedValueSet_QualifiedName + "<"
                        + wrapperDatatypeHelper.getJavaClassName() + ">" : EnumValueSet.class.getName(),
                getFieldNameAllowedValuesFor());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [javadoc]
     * private Integer minAge;
     * </pre>
     */
    private void generateFieldDefaultValue(DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder)
            throws CoreException {
        appendLocalizedJavaDoc("FIELD_DEFAULTVALUE", getPolicyCmptTypeAttribute().getName(), memberVarsBuilder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(getPolicyCmptTypeAttribute()
                .getDefaultValue());
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getFieldNameDefaultValue(), defaultValueExpression);
    }

    public void generateInitialization(JavaCodeFragmentBuilder builder, IIpsProject ipsProject) throws CoreException {
        builder.append(getMemberVarName());
        builder.append(" = ");
        JavaCodeFragment body = new JavaCodeFragment();
        generateGenerationAccess(body, ipsProject);
        body.append(getMethodNameGetDefaultValue(datatypeHelper));
        builder.append(body);
        builder.append("();");
        builder.appendln();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setPremium(Money newValue);
     * </pre>
     */
    protected void generateSetterInterface(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String description = StringUtils.isEmpty(attribute.getDescription()) ? "" : SystemUtils.LINE_SEPARATOR + "<p>"
                + SystemUtils.LINE_SEPARATOR + attribute.getDescription();
        String[] replacements = new String[] { attributeName, description };
        appendLocalizedJavaDoc("METHOD_SETVALUE", replacements, attributeName, methodsBuilder);
        generateSetterSignature(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    public void generateInitializationForOverrideAttributes(JavaCodeFragmentBuilder builder, IIpsProject ipsProject)
            throws CoreException {
        JavaCodeFragment initialValueExpression = datatypeHelper.newInstance(attribute.getDefaultValue());
        generateCallToMethodSetPropertyValue(initialValueExpression, builder);
    }

    private void generateCallToMethodSetPropertyValue(JavaCodeFragment value, JavaCodeFragmentBuilder builder) {
        builder.append(getMethodNametSetPropertyValue());
        builder.append('(');
        builder.append(value);
        builder.append(");");
    }

}
