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

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.devtools.stdbuilder.changelistener.ChangeEventType;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.runtime.internal.MethodNames;

/**
 * 
 * @author Jan Ortmann
 */
public class GenAssociationTo1 extends GenAssociation {

    public GenAssociationTo1(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAssociation association)
            throws CoreException {
        super(genPolicyCmptType, association);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String computeFieldName() {
        return getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public void setCoverage(ICoverage objectToTest)
     * </pre>
     */
    public void generateSignatureSetObject(JavaCodeFragmentBuilder methodsBuilder) {
        generateSignatureSetObject(methodsBuilder, false);
    }

    public void generateSignatureSetObject(JavaCodeFragmentBuilder methodsBuilder, boolean internalName) {
        String methodName = internalName ? getMethodNameSetObjectInternal() : getMethodNameSetObject();
        String paramName = getParamNameForSetObject();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[] { paramName },
                new String[] { targetInterfaceName });
    }

    @Override
    public String getMethodNameAddOrSetObject() {
        return getMethodNameSetObject();
    }

    @Override
    public String getMethodNameAddOrSetObjectInternal() {
        return getMethodNameSetObjectInternal();
    }

    /**
     * Returns the name of the method setting the referenced object. e.g. setCoverage(ICoverage
     * newObject)
     */
    @Override
    public String getMethodNameSetObject() {
        return getMethodNameSetObject(association);
    }

    private String getMethodNameSetObjectInternal() {
        return getMethodNameSetObject(association) + "Internal";
    }

    /**
     * Returns the name of the method setting the referenced object. e.g. setCoverage(ICoverage
     * newObject).
     */
    public String getMethodNameSetObject(IAssociation association) {
        return getLocalizedText("METHOD_SET_OBJECT_NAME", StringUtils.capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Returns the name of the parameter for the method that tests if an object is references in a
     * multi-value association, e.g. objectToTest.
     */
    public String getParamNameForSetObject() {
        return getLocalizedText("PARAM_OBJECT_TO_SET_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the method removing an object from a multi-value association, e.g.
     * removeCoverage().
     */
    public String getMethodNameRemoveObject() {
        return getLocalizedText("METHOD_REMOVE_OBJECT_NAME",
                StringUtils.capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Returns the name of the method removing an object from a multi-value association, e.g.
     * removeCoverage().
     */
    public String getMethodNameRemoveObject(IAssociation association) {
        return getLocalizedText("METHOD_REMOVE_OBJECT_NAME",
                StringUtils.capitalize(association.getTargetRoleSingular()));
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public ICoverage getCoverage()
     * </pre>
     */
    @Override
    public void generateSignatureGetRefObject(JavaCodeFragmentBuilder builder) throws CoreException {
        String methodName = getMethodNameGetRefObject();
        builder.signature(java.lang.reflect.Modifier.PUBLIC, targetInterfaceName, methodName, new String[] {},
                new String[] {});
    }

    /**
     * Returns the name of the method returning the single referenced object. e.g. getCoverage()
     */
    @Override
    public String getMethodNameGetRefObject() {
        return getMethodNameGetRefObject(association);
    }

    /**
     * Returns the name of the method returning the single referenced object. e.g. getCoverage()
     */
    public String getMethodNameGetRefObject(IPolicyCmptTypeAssociation association) {
        return getLocalizedText("METHOD_GET_REF_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns <code>true</code> if the association is the inverse of a derived union association,
     * otherwise <code>false</code>. Note that due to performance reason this method should be used
     * instead of calling PolicyCmptTypeAssociation#isInverseOfDerivedUnion because this method used
     * the cached inverse association instead searching the inverse again.
     */
    public boolean isInverseOfDerivedUnionAssociation() {
        if (inverseAssociation == null) {
            return false;
        }
        return inverseAssociation.isDerivedUnion();
    }

    /**
     * Returns the association generator of the corresponding inverse of a derived union. Returns
     * <code>null</code> if the inverse of this association is missing or inverse association is no
     * subset of a derived union. Note that more than one generators could be returned if the
     * derived union is itself a subset of a derived union.
     */
    public List<GenAssociation> getGeneratorForInverseOfDerivedUnion() throws CoreException {
        // generators are only created for shared associations OR associations that are the inverse
        // of derived union
        if (!association.isSharedAssociation()
                && (inverseAssociation == null || !inverseAssociation.isSubsetOfADerivedUnion() || !isCompositionDetailToMaster())) {
            return Collections.emptyList();
        }
        if (isSharedAssociationAlreadyGenerated()) {
            return Collections.emptyList();
        }
        // find the derived union the inverse of this (the master to detail association) is the
        // subset for
        IPolicyCmptTypeAssociation subsettedDerivedUnion;
        if (association.isSharedAssociation()) {
            // FIPS-85: for shared associations, the subsetted derived union is the same as the
            // inverse association
            // TODO null check
            IPolicyCmptTypeAssociation sharedAssociationHost = association.findSharedAssociationHost(getIpsProject());
            if (sharedAssociationHost == null) {
                return Collections.emptyList();
            }
            subsettedDerivedUnion = sharedAssociationHost.findInverseAssociation(getIpsProject());
        } else {
            subsettedDerivedUnion = (IPolicyCmptTypeAssociation)inverseAssociation
                    .findSubsettedDerivedUnion(getIpsProject());
        }
        if (subsettedDerivedUnion == null) {
            return Collections.emptyList();
        }

        // find generator for the policy component type of the derived union
        GenPolicyCmptType generatorForDerivedUnionHost = getGeneratorFor(subsettedDerivedUnion.getPolicyCmptType());
        if (generatorForDerivedUnionHost == null) {
            return Collections.emptyList();
        }

        // find the generator for the derived union association
        GenAssociation generatorForDerivedUnion = generatorForDerivedUnionHost.getGenerator(subsettedDerivedUnion);
        if (generatorForDerivedUnion == null) {
            return Collections.emptyList();
        }

        List<GenAssociation> result = new ArrayList<GenAssociation>();
        if (subsettedDerivedUnion.isSubsetOfADerivedUnion()) {
            // special case if the derived union is itself a subset of a derived union
            // then we need to add the corresponding generators too
            GenAssociation generatorForInverseAssociation = generatorForDerivedUnion
                    .getGeneratorForInverseAssociation();
            if (generatorForInverseAssociation instanceof GenAssociationTo1) {
                List<GenAssociation> generatorForInverseOfDerivedUnion = ((GenAssociationTo1)generatorForInverseAssociation)
                        .getGeneratorForInverseOfDerivedUnion();
                result.addAll(generatorForInverseOfDerivedUnion);
            }
        }

        GenAssociation generatorForInverseAssociation = generatorForDerivedUnion.getGeneratorForInverseAssociation();
        if (generatorForInverseAssociation != null) {
            result.add(generatorForInverseAssociation);
        }
        // returns the generators of the inverse association of the derived union
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        super.generateMemberVariables(builder, ipsProject, generatesInterface);

        if (!isDerivedUnion() && !isCompositionDetailToMaster()) {
            String comment = getLocalizedText("FIELD_ASSOCIATION_JAVADOC", association.getName());
            builder.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);

            getGenType().getBuilderSet().addAnnotations(
                    AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD, association, builder);
            getGenType().getBuilderSet().addAnnotations(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ASSOCIATION,
                    association, builder);

            builder.varDeclaration(java.lang.reflect.Modifier.PRIVATE, targetImplClassName, fieldName,
                    new JavaCodeFragment("null"));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        super.generateMethods(builder, ipsProject, generatesInterface);
        if (generatesInterface) {
            generateMethodGetRefObject(builder);
            if (!association.isDerivedUnion() && !association.getAssociationType().isCompositionDetailToMaster()) {
                generateMethodSetObjectInterface(builder);
                generateNewChildMethodsIfApplicable(builder, generatesInterface);
            }
        } else {
            if (association.isCompositionDetailToMaster()) {
                generateMethodGetTypesafeParentObject(builder);
                return;
            }
            if (!isDerivedUnion()) {
                generateMethodGetRefObjectBasedOnMemberVariable(builder);
                if (association.isAssoziation()) {
                    generateMethodSetRefObjectForAssociation(builder);
                } else if (association.isCompositionMasterToDetail()) {
                    generateMethodSetRefObjectForComposition(builder);
                }
                generateNewChildMethodsIfApplicable(builder, generatesInterface);
            }
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setCoverage(ICoverage newObject) {
     *     if (coverage != null) {
     *         ((Coverage)coverage).setContractInternal(null);
     *     }
     *     if (newObject != null) {
     *         ((Coverage)newObject).setContractInternal(this);
     *     }
     * }
     * </pre>
     */
    protected void generateMethodSetRefObjectForComposition(JavaCodeFragmentBuilder builder, boolean internal)
            throws CoreException {
        if (association.isCompositionDetailToMaster()) {
            return; // setter defined in base class.
        }

        if (internal) {
            appendLocalizedJavaDoc("METHOD_SET_OBJECT_INTERNAL", association.getTargetRoleSingular(), builder);
        } else {
            appendJavaDocAndOverrideAnnotation(builder, Overrides.INTERFACE_METHOD);
        }
        generateSignatureSetObject(builder, internal);

        builder.openBracket();

        String paramName = getParamNameForSetObject();
        if (target.isDependantType() && inverseAssociation != null) {
            builder.appendln("if(" + fieldName + " != null) {");
            builder.append(generateCodeToSynchronizeReverseComposition(fieldName, "null", false));
            builder.appendln("}");

            builder.appendln("if(" + paramName + " != null) {");
            builder.append(generateCodeToSynchronizeReverseComposition(paramName, "this", true));
            builder.appendln("}");
        }

        builder.append(fieldName);
        builder.append(" = (");
        builder.appendClassName(targetImplClassName);
        builder.append(")" + paramName + ";");

        builder.closeBracket();
    }

    protected void generateMethodSetRefObjectForComposition(JavaCodeFragmentBuilder builder) throws CoreException {
        if (!getGenPolicyCmptType().isGenerateChangeListenerSupport()) {
            generateMethodSetRefObjectForComposition(builder, false);
        } else {
            generateMethodSetRefObjectWithChangeListenerSupport(builder);
            generateMethodSetRefObjectForComposition(builder, true);
        }

    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setCoverage(ICoverage newObject) {
     *   setCoverageInternal(newObject);
     *   notifyChangeListeners(new AssociationChangedEvent(this, ASSOCIATION_COVERAGE, null, newObject));
     * }
     * </pre>
     */
    private void generateMethodSetRefObjectWithChangeListenerSupport(JavaCodeFragmentBuilder builder)
            throws CoreException {

        String paramName = getParamNameForSetObject();

        // generate set method which delegates to the internal set method
        // and notifies all change listener
        appendJavaDocAndOverrideAnnotation(builder, Overrides.INTERFACE_METHOD);
        generateSignatureSetObject(builder, false);
        builder.openBracket();

        generateChangeListenerSupportBeforeChange(builder, ChangeEventType.ASSOCIATION_OBJECT_CHANGED, paramName);

        builder.append(getMethodNameSetObjectInternal());
        builder.append("(");
        builder.append(paramName);
        builder.appendln(");");

        generateChangeListenerSupportAfterChange(builder, ChangeEventType.ASSOCIATION_OBJECT_CHANGED, paramName);

        builder.closeBracket();
    }

    protected void generateMethodSetRefObjectForAssociation(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        if (!getGenPolicyCmptType().isGenerateChangeListenerSupport()) {
            generateMethodSetRefObjectForAssociation(methodsBuilder, false);
        } else {
            generateMethodSetRefObjectWithChangeListenerSupport(methodsBuilder);

            // generate internal set method without change listener support
            generateMethodSetRefObjectForAssociation(methodsBuilder, true);
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setCoverage(ICoverage newObject) {
     *     if (refObject == homeContract)
     *         return;
     *     IHomeContract oldRefObject = homeContract;
     *     homeContract = null;
     *     if (oldRefObject != null) {
     *          oldRefObject.setHomePolicy(null);
     *     }
     *     homeContract = (HomeContract) refObject;
     *     if (refObject != null &amp;&amp; refObject.getHomePolicy() != this) {
     *         refObject.setHomePolicy(this);
     *     }
     * }
     * </pre>
     * 
     * If internal is set to <code>true</code> then the method name will be setCoverageInternal
     */
    protected void generateMethodSetRefObjectForAssociation(JavaCodeFragmentBuilder methodsBuilder, boolean internal)
            throws CoreException {
        if (internal) {
            methodsBuilder.javaDoc(null, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        } else {
            appendJavaDocAndOverrideAnnotation(methodsBuilder, Overrides.INTERFACE_METHOD);
        }
        generateSignatureSetObject(methodsBuilder, internal);
        String paramName = getParamNameForSetObject();
        methodsBuilder.openBracket();
        methodsBuilder.append("if(" + paramName + " == ");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(") return;");
        if (inverseAssociation != null) {
            methodsBuilder.appendClassName(targetInterfaceName);
            methodsBuilder.append(" oldRefObject = ");
            methodsBuilder.append(fieldName);
            methodsBuilder.append(';');
            methodsBuilder.append(fieldName);
            methodsBuilder.append(" = null;");
            methodsBuilder.append(generateCodeToCleanupOldReference("oldRefObject"));
        }
        methodsBuilder.append(fieldName);
        methodsBuilder.append(" = (");
        methodsBuilder.appendClassName(targetImplClassName);
        methodsBuilder.append(")" + paramName + ";");
        if (inverseAssociation != null) {
            GenPolicyCmptType generatorForTarget = getGenType().getBuilderSet().getGenerator(target);
            if (generatorForTarget != null) {
                GenAssociation generatorForInverse = generatorForTarget.getGenerator(inverseAssociation);
                if (generatorForInverse != null) {
                    methodsBuilder.append(generatorForInverse.generateCodeToSynchronizeReverseAssoziation(fieldName,
                            targetImplClassName));
                }
            }
        }
        methodsBuilder.closeBracket();
    }

    public JavaCodeFragment generateCodeToCleanupOldReference(String varToCleanUp) {
        JavaCodeFragment body = new JavaCodeFragment();
        if (!association.is1ToMany()) {
            body.append("if (" + varToCleanUp + "!=null) {");
        }
        if (inverseAssociation.is1ToMany()) {
            String removeMethod = getMethodNameRemoveObject(inverseAssociation);
            body.append(varToCleanUp + "." + removeMethod + "(this);");
        } else {
            String setMethod = getMethodNameSetObject(inverseAssociation);
            body.append(varToCleanUp);
            body.append("." + setMethod + "(null);");
        }
        if (!association.is1ToMany()) {
            body.append(" }");
        }
        return body;
    }

    @Override
    public JavaCodeFragment generateCodeToSynchronizeReverseAssoziation(String varName, String varClassName)
            throws CoreException {
        JavaCodeFragment code = new JavaCodeFragment();
        code.append("if(");
        if (!inverseAssociation.is1ToMany()) {
            code.append(varName + " != null && ");
        }
        code.append(varName + ".");
        code.append(getMethodNameGetRefObject());
        code.append("() != this");
        code.append(") {");
        if (!varClassName.equals(getGenType().getQualifiedName(false))) {
            code.append("((");
            code.appendClassName(getGenType().getQualifiedName(false));
            code.append(")" + varName + ").");
        } else {
            code.append(varName + ".");
        }
        code.append(getMethodNameSetObject());
        code.appendln("(this);");
        code.appendln("}");
        return code;
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     return coverage;
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectBasedOnMemberVariable(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        appendJavaDocAndOverrideAnnotation(methodsBuilder, Overrides.INTERFACE_METHOD);
        generateSignatureGetRefObject(methodsBuilder);
        methodsBuilder.openBracket();
        if (!association.isCompositionDetailToMaster()) {
            methodsBuilder.appendln("return " + fieldName + ";");
        } else {
            methodsBuilder.append("return (");
            methodsBuilder.appendClassName(targetInterfaceName);
            methodsBuilder.append(")" + MethodNames.GET_PARENT + "();");
        }
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * Note that if this is the inverse of a derived union then no code is generated (see comment
     * below).
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     return coverage; // if inverse is not derived union
     * }
     * 
     * </pre>
     */
    protected void generateMethodGetTypesafeParentObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        if (inverseAssociation != null && inverseAssociation.isDerivedUnion()) {
            // in case of inverse of derived union
            // we don't need to generate this method
            // because no field is generated for the parent
            return;
        }
        // in case of non derived union we can directly return the field
        // note this is necessary because this getter method is used
        // to assert that a child is only related to one parent
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        if (isAlreadyDefinedInSupertype()) {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), Overrides.CLASS_METHOD);
        } else {
            appendOverrideAnnotation(methodsBuilder, getIpsProject(), Overrides.INTERFACE_METHOD);
        }
        generateSignatureGetRefObject(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");

        methodsBuilder.append(getFieldNameForAssociation());
        methodsBuilder.append(";");
        methodsBuilder.closeBracket();
    }

    private boolean isAlreadyDefinedInSupertype() {
        try {
            IPolicyCmptType policyCmptType = getGenPolicyCmptType().getPolicyCmptType();
            if (policyCmptType == null) {
                return false;
            }
            IPolicyCmptType foundSupertype = (IPolicyCmptType)policyCmptType.findSupertype(getIpsProject());
            if (foundSupertype == null) {
                return false;
            }
            IPolicyCmptTypeAssociation superAssociation = (IPolicyCmptTypeAssociation)foundSupertype.findAssociation(
                    association.getTargetRoleSingular(), getIpsProject());
            if (superAssociation == null) {
                return false;
            }
            if (superAssociation.isInverseOfDerivedUnion()) {
                return false;
            }
            return true;
        } catch (CoreException e) {
            StdBuilderPlugin.log(e);
            return false;
        }
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public void setCoverage(ICoverage newObject);
     * </pre>
     */
    protected void generateMethodSetObjectInterface(JavaCodeFragmentBuilder methodsBuilder) {
        appendLocalizedJavaDoc("METHOD_SET_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureSetObject(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage();
     * </pre>
     */
    protected void generateMethodGetRefObject(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT", StringUtils.capitalize(association.getTargetRoleSingular()),
                builder);
        generateSignatureGetRefObject(builder);
        builder.appendln(";");
    }

    /**
     * Code sample for 1-1 composition
     * 
     * <pre>
     * if (child1 != null) {
     *     copy.child1 = (CpChild1)child1.newCopy();
     *     copy.child1.setParentModelObjectInternal(copy);
     * }
     * </pre>
     */
    @Override
    protected void generateCodeForCopyPropertiesForComposition(String paramName,
            String copyMapName,
            JavaCodeFragmentBuilder methodsBuilder,
            String field,
            org.faktorips.devtools.core.model.pctype.IPolicyCmptType targetType,
            String targetTypeQName) throws CoreException {

        methodsBuilder.appendln("if (");
        methodsBuilder.append(field);
        methodsBuilder.appendln("!=null) {");
        methodsBuilder.append(paramName);
        methodsBuilder.append(".");
        methodsBuilder.append(field);
        methodsBuilder.append(" = (");
        methodsBuilder.appendClassName(targetTypeQName);
        methodsBuilder.append(")");
        methodsBuilder.append(field);
        methodsBuilder.append(".");
        methodsBuilder.append(MethodNames.METHOD_NEW_COPY).append("(").append(copyMapName).append(");");
        if (targetType.isDependantType() && inverseAssociation != null) {
            methodsBuilder.append(paramName);
            methodsBuilder.append(".");
            methodsBuilder.append(field);
            methodsBuilder.append(".");
            methodsBuilder.append(getMethodNameSetParentObjectInternal(true));
            methodsBuilder.append("(");
            methodsBuilder.append(paramName);
            methodsBuilder.appendln(");");
        }
        methodsBuilder.append(copyMapName).append(".put(").append(field).append(", ").append(paramName).append('.')
                .append(field).appendln(");");
        methodsBuilder.appendln("}");
    }

    @Override
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        super.generateConstants(builder, ipsProject, generatesInterface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFieldNameForAssociation() throws CoreException {
        return getJavaNamingConvention().getMemberVarName(association.getTargetRoleSingular());
    }

    /**
     * Code sample for 1-1 composition
     * 
     * <pre>
     * copy.child1 = child1;
     * </pre>
     */
    @Override
    public void generateMethodCopyPropertiesForAssociation(String paramName, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String field = getFieldNameForAssociation();
        methodsBuilder.appendln(paramName + "." + field + " = " + field + ";");
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public void generateCodeForRemoveChildModelObjectInternal(JavaCodeFragmentBuilder methodsBuilder, String paramName)
            throws CoreException {
        String fieldName = getFieldNameForAssociation();
        methodsBuilder.appendln("if (" + fieldName + "==" + paramName + ") {");
        methodsBuilder.appendln(fieldName + " = null;");
        methodsBuilder.appendln("}");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage() {
     *     if(getTplCoverage()!=null) {
     *         return getTplCoverage();
     *     }
     *     if (getCollisionCoverage()!=null) {
     *         return getCollisionCoverage();
     *     }
     *     return null;
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectForContainerAssociationImplementation(List<IAssociation> subAssociations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRefObject(methodsBuilder);
        methodsBuilder.openBracket();
        for (int i = 0; i < subAssociations.size(); i++) {
            IPolicyCmptTypeAssociation subrel = (IPolicyCmptTypeAssociation)subAssociations.get(i);
            GenAssociation subrelGenerator = ((GenPolicyCmptType)getGenType()).getGenerator(subrel);
            String accessCode;
            accessCode = subrelGenerator.getMethodNameGetRefObject() + "()";
            methodsBuilder.appendln("if (" + accessCode + "!=null) {");
            methodsBuilder.appendln("return " + accessCode + ";");
            methodsBuilder.appendln("}");
        }
        methodsBuilder.append("return null;");
        methodsBuilder.closeBracket();
    }

    @Override
    public void generateCodeForContainerAssociationImplementation(List<IAssociation> associations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateMethodGetRefObjectForContainerAssociationImplementation(associations, methodsBuilder);
    }

    @Override
    public void generateCodeForValidateDependants(JavaCodeFragment body) throws CoreException {
        String field = getFieldNameForAssociation();
        body.append("if (" + field + "!=null) {");
        body.append("ml.add(" + field + ".validate(context));");
        body.append("}");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * if (coverage != null) {
     *     coverage.accept(visitor);
     * }
     * </pre>
     * 
     * {@inheritDoc}
     */
    @Override
    public void generateSnippetForAcceptVisitor(String paramName, JavaCodeFragmentBuilder builder) throws CoreException {
        builder.appendln("if (" + fieldName + " != null) {");
        builder.appendln(fieldName + "." + MethodNames.ACCEPT_VISITOR + "(" + paramName + ");");
        builder.appendln("}");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * if (copyMap.containsKey(policyHolder)) {
     *     copy.policyHolder = (Person)copyMap.get(policyHolder);
     * }
     * </pre>
     * 
     * {@inheritDoc}
     * 
     */
    @Override
    public void generateCodeForCopyAssociation(String varCopy, String varCopyMap, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.append("if (") //
                .append(varCopyMap).append(".containsKey(").append(getFieldNameForAssociation()) //
                .appendln("))").openBracket();
        methodsBuilder.append(varCopy).append('.').append(getFieldNameForAssociation()).append(" = ") //
                .append('(').append(getUnqualifiedClassName(getTargetPolicyCmptType(), false)).append(')') //
                .append(varCopyMap).append(".get(").append(getFieldNameForAssociation()).appendln(");");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample for associated person:
     * 
     * <pre>
     * if (person != null)
     *   Person copyPerson = (Person)copyMap.get(person);
     *   person.copyAssociationsInternal(copyPerson, copyMap);
     * }
     * </pre>
     * 
     * {@inheritDoc}
     * 
     */
    @Override
    public void generateCodeForCopyComposition(String varCopy, String varCopyMap, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.append("if (").append(fieldName).append(" != null)").openBracket();
        String unqTargetImplName = getUnqualifiedClassName(getTargetPolicyCmptType(), false);
        String varCopyTarget = "copy" + unqTargetImplName;
        methodsBuilder.append(unqTargetImplName).append(' ').append(varCopyTarget).append(" = ") //
                .append('(').append(unqTargetImplName).append(')').append(varCopyMap) //
                .append(".get(").append(fieldName).appendln(");");
        methodsBuilder.append(fieldName).append(".") //
                .append(MethodNames.METHOD_COPY_ASSOCIATIONS) //
                .append('(').append(varCopyTarget).append(", ").append(varCopyMap).append(");");
        methodsBuilder.closeBracket();
    }

    @Override
    public void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        AssociationType associationType = association.getAssociationType();
        if (associationType.equals(AssociationType.COMPOSITION_MASTER_TO_DETAIL)) {
            getGeneratedJavaElementsForPublishedInterfaceMasterToDetail(javaElements, generatedJavaType);
        } else if (associationType.equals(AssociationType.COMPOSITION_DETAIL_TO_MASTER)) {
            getGeneratedJavaElementsForPublishedInterfaceDetailToMaster(javaElements, generatedJavaType);
        } else if (associationType.equals(AssociationType.ASSOCIATION)) {
            getGeneratedJavaElementsForPublishedInterfaceAssociation(javaElements, generatedJavaType);
        }
    }

    private void getGeneratedJavaElementsForPublishedInterfaceMasterToDetail(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        addFieldAssociationNameToGeneratedJavaElements(javaElements, generatedJavaType);
        addFieldGetMaxCardinalityForToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetRefObjectToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodSetObjectToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodNewChildToGeneratedJavaElements(javaElements, generatedJavaType);

        if (target.isConfigurableByProductCmptType()) {
            try {
                IProductCmptType targetConfiguringProductCmptType = target.findProductCmptType(target.getIpsProject());
                if (targetConfiguringProductCmptType != null) {
                    addMethodNewChildConfiguredToGeneratedJavaElements(javaElements, generatedJavaType,
                            targetConfiguringProductCmptType);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void getGeneratedJavaElementsForPublishedInterfaceDetailToMaster(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        addFieldAssociationNameToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetRefObjectToGeneratedJavaElements(javaElements, generatedJavaType);
    }

    private void getGeneratedJavaElementsForPublishedInterfaceAssociation(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        addFieldAssociationNameToGeneratedJavaElements(javaElements, generatedJavaType);
        addFieldGetMaxCardinalityForToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetRefObjectToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodSetObjectToGeneratedJavaElements(javaElements, generatedJavaType);
    }

    @Override
    public void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement) {

        AssociationType associationType = association.getAssociationType();
        if (associationType.equals(AssociationType.COMPOSITION_MASTER_TO_DETAIL)) {
            getGeneratedJavaElementsForImplementationMasterToDetail(javaElements, generatedJavaType);
        } else if (associationType.equals(AssociationType.COMPOSITION_DETAIL_TO_MASTER)) {
            getGeneratedJavaElementsForImplementationDetailToMaster(javaElements, generatedJavaType);
        } else if (associationType.equals(AssociationType.ASSOCIATION)) {
            getGeneratedJavaElementsForImplementationAssociation(javaElements, generatedJavaType);
        }
    }

    private void getGeneratedJavaElementsForImplementationMasterToDetail(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        addFieldAssociationToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetRefObjectToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodSetObjectToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodSetObjectInternal(javaElements, generatedJavaType);
        addMethodNewChildToGeneratedJavaElements(javaElements, generatedJavaType);

        if (target.isConfigurableByProductCmptType()) {
            try {
                IProductCmptType targetConfiguringProductCmptType = target.findProductCmptType(target.getIpsProject());
                if (targetConfiguringProductCmptType != null) {
                    addMethodNewChildConfiguredToGeneratedJavaElements(javaElements, generatedJavaType,
                            targetConfiguringProductCmptType);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void getGeneratedJavaElementsForImplementationDetailToMaster(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        addFieldAssociationToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetRefObjectToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodSetObjectInternal(javaElements, generatedJavaType);
    }

    private void getGeneratedJavaElementsForImplementationAssociation(List<IJavaElement> javaElements,
            IType generatedJavaType) {

        addFieldAssociationToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodGetRefObjectToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodSetObjectToGeneratedJavaElements(javaElements, generatedJavaType);
        addMethodSetObjectInternal(javaElements, generatedJavaType);
    }

    private void addMethodGetRefObjectToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {
        addMethodToGeneratedJavaElements(javaElements, generatedJavaType, getMethodNameGetRefObject());
    }

    private void addMethodSetObjectToGeneratedJavaElements(List<IJavaElement> javaElements, IType generatedJavaType) {
        addMethodToGeneratedJavaElements(javaElements, generatedJavaType, getMethodNameAddOrSetObject(), "Q"
                + getUnqualifiedInterfaceClassNameForTarget() + ";");
    }

    private void addMethodSetObjectInternal(List<IJavaElement> javaElements, IType generatedJavaType) {
        addMethodToGeneratedJavaElements(javaElements, generatedJavaType, getMethodNameAddOrSetObjectInternal(), "Q"
                + getUnqualifiedInterfaceClassNameForTarget() + ";");
    }

    public boolean isSharedAssociationAlreadyGenerated() throws CoreException {
        if (!association.isSharedAssociation()) {
            return false;
        }
        IPolicyCmptTypeAssociation associationWithSameName = association
                .findSuperAssociationWithSameName(getIpsProject());
        if (associationWithSameName != null && associationWithSameName.isSharedAssociation()) {
            return true;
        }
        return false;
    }

}
