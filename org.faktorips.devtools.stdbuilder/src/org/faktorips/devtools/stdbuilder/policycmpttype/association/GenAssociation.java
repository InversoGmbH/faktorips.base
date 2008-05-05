/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.Java5ClassNames;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptTypePart;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.runtime.IModelObjectChangedEvent;
import org.faktorips.runtime.internal.DependantObject;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.IntegerRange;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class GenAssociation extends GenPolicyCmptTypePart {

    protected IPolicyCmptTypeAssociation association;
    protected IPolicyCmptTypeAssociation reverseAssociation;
    protected IPolicyCmptType target;

    // the qualified name of the target's published interface.
    protected String targetInterfaceName;

    // the qualified name of the target's implementation class name. null if used in the interface
    // builder!
    protected String targetImplClassName;

    protected String fieldName;

    public GenAssociation(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAssociation association,
            LocalizedStringsSet stringsSet) throws CoreException {

        super(genPolicyCmptType, association, stringsSet);
        this.association = association;
        this.reverseAssociation = association.findInverseAssociation(getGenPolicyCmptType().getIpsPart()
                .getIpsProject());
        this.target = association.findTargetPolicyCmptType(association.getIpsProject());
        this.targetInterfaceName = GenType.getQualifiedName(target, genPolicyCmptType.getBuilderSet(), true);
        this.targetImplClassName = GenType.getQualifiedName(target, genPolicyCmptType.getBuilderSet(), false);
        this.fieldName = computeFieldName();
    }

    /**
     * Returns the name of the field/member variable for this association.
     */
    protected abstract String computeFieldName();

    public boolean isDerivedUnion() {
        return association.isDerivedUnion();
    }

    public boolean isCompositionMasterToDetail() {
        return association.isCompositionMasterToDetail();
    }

    public boolean isCompositionDetailToMaster() {
        return association.isCompositionDetailToMaster();
    }

    public boolean isAssociation() {
        return association.isAssoziation();
    }

    /**
     * Gnerates a method to create a new child object if the association is a composite and the
     * target is not abstract. If the target is configurable by product a second method with the
     * product component type as argument is also generated.
     */
    protected void generateNewChildMethodsIfApplicable(JavaCodeFragmentBuilder methodsBuilder,
            boolean generatesInterface) throws CoreException {
        if (!association.getAssociationType().isCompositionMasterToDetail()) {
            return;
        }
        if (target.isAbstract()) {
            return;
        }
        generateMethodNewChild(generatesInterface, false, methodsBuilder);
        if (target.isConfigurableByProductCmptType() && target.findProductCmptType(getIpsProject()) != null) {
            generateMethodNewChild(generatesInterface, true, methodsBuilder);
        }
    }

    protected IIpsProject getIpsProject() {
        return getGenPolicyCmptType().getPolicyCmptType().getIpsProject();
    }

    private void generateMethodNewChild(boolean generatesInterface,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException {

        if (generatesInterface) {
            generateInterfaceMethodNewChild(inclProductCmptArg, builder);
        } else {
            generateImplMethodNewChild(inclProductCmptArg, builder);
        }
    }

    /**
     * Code sample without product component parameter:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage newCoverage();
     * </pre>
     * 
     * Code sample with product component parameter: [Javadoc]
     * 
     * <pre>
     * public ICoverage newCoverage(ICoverageType coverageType);
     * </pre>
     */
    protected void generateInterfaceMethodNewChild(boolean inclProductCmptArg, JavaCodeFragmentBuilder builder)
            throws CoreException {

        String targetTypeName = target.getName();
        String role = association.getTargetRoleSingular();
        if (inclProductCmptArg) {
            String replacements[] = new String[] { targetTypeName, role,
                    getParamNameForProductCmptInNewChildMethod(target.findProductCmptType(getIpsProject())) };
            appendLocalizedJavaDoc("METHOD_NEW_CHILD_WITH_PRODUCTCMPT_ARG", replacements, builder);
        } else {
            appendLocalizedJavaDoc("METHOD_NEW_CHILD", new String[] { targetTypeName, role }, builder);
        }
        generateSignatureNewChild(inclProductCmptArg, builder);
        builder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage newCoverage() {
     *     ICoverage newCoverage = new Coverage();
     *     addCoverage(newCoverage); // for toMany associations, setCoverage(newCoverage) for to1
     *     newCoverage.initialize();
     *     return newCoverage;
     * }
     * </pre>
     */
    public void generateImplMethodNewChild(boolean inclProductCmptArg, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureNewChild(inclProductCmptArg, methodsBuilder);
        String addOrSetMethod = getMethodNameAddOrSetObject();
        String varName = "new" + association.getTargetRoleSingular();
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(targetImplClassName);
        methodsBuilder.append(" " + varName + " = new ");
        methodsBuilder.appendClassName(targetImplClassName);
        if (inclProductCmptArg) {
            methodsBuilder.appendln("("
                    + getParamNameForProductCmptInNewChildMethod(target.findProductCmptType(getIpsProject())) + ");");
        } else {
            methodsBuilder.appendln("();");
        }
        methodsBuilder.appendln(addOrSetMethod + "(" + varName + ");");
        methodsBuilder.appendln(varName + "." + getGenPolicyCmptType().getMethodNameInitialize() + "();");
        methodsBuilder.appendln("return " + varName + ";");
        methodsBuilder.closeBracket();
    }

    /**
     * Returns the name of the method that adds an object to a toMany association or that sets the
     * object in a to1 association respectively.
     */
    public abstract String getMethodNameAddOrSetObject();

    /**
     * Code sample without product component argument:
     * 
     * <pre>
     * public Coverage newCoverage()
     * </pre>
     * 
     * Code sample with product component argument:
     * 
     * <pre>
     * public Coverage newCoverage(ICoverageType coverageType)
     * </pre>
     */
    protected void generateSignatureNewChild(boolean inclProductCmptArg, JavaCodeFragmentBuilder builder)
            throws CoreException {

        String methodName = getMethodNameNewChild(association);
        String[] argNames, argTypes;
        if (inclProductCmptArg) {
            IProductCmptType productCmptType = target.findProductCmptType(getIpsProject());
            argNames = new String[] { getParamNameForProductCmptInNewChildMethod(productCmptType) };
            argTypes = new String[] { getGenPolicyCmptType().getBuilderSet().getGenerator(productCmptType)
                    .getQualifiedName(true) };
        } else {
            argNames = EMPTY_STRING_ARRAY;
            argTypes = EMPTY_STRING_ARRAY;
        }
        builder.signature(java.lang.reflect.Modifier.PUBLIC, targetInterfaceName, methodName, argNames, argTypes);
    }

    /**
     * Returns the name of the method to create a new child object and add it to the parent.
     */
    public String getMethodNameNewChild(IPolicyCmptTypeAssociation association) {
        return getLocalizedText("METHOD_NEW_CHILD_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the parameter in the new child mthod, e.g. coverageType.
     */
    protected String getParamNameForProductCmptInNewChildMethod(IProductCmptType targetProductCmptType)
            throws CoreException {
        String targetProductCmptClass = getGenPolicyCmptType().getBuilderSet().getGenerator(targetProductCmptType)
                .getQualifiedName(true);
        return StringUtils.uncapitalize(StringUtil.unqualifiedName(targetProductCmptClass));
    }

    /**
     * <pre>
     * ((DependantObject)parentModelObject).setParentModelObjectInternal(this);
     * </pre>
     */
    protected JavaCodeFragment generateCodeToSynchronizeReverseComposition(String varName, String newValue)
            throws CoreException {

        JavaCodeFragment code = new JavaCodeFragment();
        code.append("((");
        code.appendClassName(DependantObject.class);
        code.append(')');
        code.append(varName);
        code.append(")." + MethodNames.SET_PARENT);
        code.append('(');
        code.append(newValue);
        code.appendln(");");
        return code;
    }

    protected void generateChangeListenerSupport(JavaCodeFragmentBuilder methodsBuilder,
            String eventConstant,
            String paramName) {
        getGenPolicyCmptType().generateChangeListenerSupport(methodsBuilder, IModelObjectChangedEvent.class.getName(),
                eventConstant, fieldName, paramName);
    }

    /**
     * Returns the name of the method setting the referenced object. e.g. setCoverage(ICoverage
     * newObject)
     */
    public String getMethodNameSetObject() {
        return getLocalizedText("METHOD_SET_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the method returning the single referenced object. e.g. getCoverage()
     */
    public String getMethodNameGetRefObject() {
        return getLocalizedText("METHOD_GET_REF_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ISubTypeB getSubTypeB(ISubTypeBConfig qualifier) {
     * //1ToMany public ISubTypeB[] getSubTypeB(ISubTypeBConfig qualifier) {
     *   if(qualifer == null) {
     *      return null;
     *   }
     *   //1ToMany List result = new ArrayList();
     *   for (Iterator it = subTypeBs.iterator(); it.hasNext();) {
     *     ISubTypeB subTypeB = (ISubTypeB) it.next();
     *     if(subTypeB.getSubTypeBConfig().equals(qualifier)){
     *       return subTypeB;
     *       //1ToMany result.add(subTypeB); 
     *     }
     *   }
     *   return null;
     *   //1ToMany return (ISubTypeB[]) result.toArray(new ISubTypeB[result.size()]);
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ISubTypeB getSubTypeB(ISubTypeBConfig qualifier) {
     * //1ToMany public List&lt;ISubTypeB&gt; getSubTypeB(ISubTypeBConfig qualifier) {
     *   if(qualifer == null) {
     *      return null;
     *   }
     *   //1ToMany List&lt;ISubTypeB&gt; result = new ArrayList&lt;ISubTypeB&gt;();
     *   for (Iterator it = subTypeBs.iterator(); it.hasNext();) {
     *     ISubTypeB subTypeB = (ISubTypeB) it.next();
     *     if(subTypeB.getSubTypeBConfig().equals(qualifier)){
     *       return subTypeB;
     *       //1ToMany result.add(subTypeB); 
     *     }
     *   }
     *   return null;
     *   //1ToMany return result;
     * }
     * </pre>
     */
    protected void generateMethodGetRefObjectsByQualifierForNonDerivedUnion(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRefObjectByQualifier(methodsBuilder);
        IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
        String className = getQualifiedClassName(target, true);
        String pcTypeLocalVariable = StringUtils.uncapitalize(getUnqualifiedClassName(target, false));
        methodsBuilder.openBracket();
        methodsBuilder.append("if(qualifier == null)");
        methodsBuilder.openBracket();
        methodsBuilder.append("return null;");
        methodsBuilder.closeBracket();
        if (association.is1ToManyIgnoringQualifier()) {
            methodsBuilder.appendClassName(List.class);
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("<");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append(">");
            }
            methodsBuilder.append(" result = new ");
            methodsBuilder.appendClassName(ArrayList.class);
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("<");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append(">");
            }
            methodsBuilder.append("();");
        }
        methodsBuilder.append("for (");
        methodsBuilder.appendClassName(Iterator.class);
        methodsBuilder.append(" it = ");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(".iterator(); it.hasNext();)");
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(className);
        methodsBuilder.append(' ');
        methodsBuilder.append(pcTypeLocalVariable);
        methodsBuilder.append(" = (");
        methodsBuilder.appendClassName(className);
        methodsBuilder.append(")");
        methodsBuilder.append(" it.next();");
        methodsBuilder.appendln();
        methodsBuilder.append("if(");
        methodsBuilder.append(pcTypeLocalVariable);
        methodsBuilder.append('.');
        methodsBuilder.append(getGenPolicyCmptType().getBuilderSet().getGenerator(
                association.findQualifier(getIpsProject())).getMethodNameGetProductCmpt());
        methodsBuilder.append("().equals(qualifier))");
        methodsBuilder.openBracket();
        if (association.is1ToManyIgnoringQualifier()) {
            methodsBuilder.append("result.add(");
            methodsBuilder.append(pcTypeLocalVariable);
            methodsBuilder.append(");");
        } else {
            methodsBuilder.append("return ");
            methodsBuilder.append(pcTypeLocalVariable);
            methodsBuilder.append(';');
        }
        methodsBuilder.closeBracket();
        methodsBuilder.closeBracket();
        if (association.is1ToManyIgnoringQualifier()) {
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("return result;");
            } else {
                methodsBuilder.append("return (");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append("[])");
                methodsBuilder.append("result.toArray(new ");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append("[result.size()]);");
            }
        } else {
            methodsBuilder.append("return null;");
        }
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IB getB(IBConfig qualifier) {
     * //1ToMany public IB getB(IBConfig qualifier) {
     *   if(qualifer == null) {
     *      return null;
     *   }
     *   IB[] bs = getBs();
     *   //1ToMany List result = new ArrayList();
     *   for (int i = 0; i &lt; bs.length; i++) {
     *     if (bs[i].getBConfig().equals(qualifier)) {
     *       return bs[i];
     *       //1ToMany result.add(bs[i]);
     *     }
     *   }
     *   return null;
     *   //1ToMany return (IB[]) result.toArray(new IB[result.size()]);
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IB getB(IBConfig qualifier) {
     * //1ToMany public IB getB(IBConfig qualifier) {
     *   if(qualifer == null) {
     *      return null;
     *   }
     *   List&lt;B&gt; bs = getBs();
     *   //1ToMany List result = new ArrayList();
     *   for (B b : bs) {
     *     if (b.getBConfig().equals(qualifier)) {
     *       return b;
     *       //1ToMany result.add(b);
     *     }
     *   }
     *   return null;
     *   //1ToMany return result;
     * }
     * </pre>
     * 
     * @throws CoreException
     */
    protected void generateMethodGetRefObjectsByQualifierForDerivedUnion(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureGetRefObjectByQualifier(methodsBuilder);
        IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
        String className = getQualifiedClassName(target, true);
        String allObjectsMethodName = getMethodNameGetAllRefObjects();
        String localVarName = "elements";
        methodsBuilder.openBracket();
        methodsBuilder.append("if(qualifier == null)");
        methodsBuilder.openBracket();
        methodsBuilder.append("return null;");
        methodsBuilder.closeBracket();
        if (isUseTypesafeCollections()) {
            methodsBuilder.appendClassName(List.class);
            methodsBuilder.append("<");
            methodsBuilder.appendClassName(className);
            methodsBuilder.append(">");
        } else {
            methodsBuilder.appendClassName(className);
            methodsBuilder.append("[] ");
        }
        methodsBuilder.append(localVarName);
        methodsBuilder.append(" = ");
        methodsBuilder.append(allObjectsMethodName);
        methodsBuilder.append("();");
        if (association.is1ToManyIgnoringQualifier()) {
            methodsBuilder.appendClassName(List.class);
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("<");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append(">");
            }
            methodsBuilder.append(" result = new ");
            methodsBuilder.appendClassName(ArrayList.class);
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("<");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append(">");
            }
            methodsBuilder.append("();");
        }
        if (isUseTypesafeCollections()) {
            methodsBuilder.append("for (");
            methodsBuilder.appendClassName(className);
            methodsBuilder.append(" element : ");
            methodsBuilder.append(localVarName);
            methodsBuilder.append(")");
            methodsBuilder.openBracket();
            methodsBuilder.append("if(");
            methodsBuilder.append("element.");
        } else {
            methodsBuilder.append("for (int i = 0; i < ");
            methodsBuilder.append(localVarName);
            methodsBuilder.append(".length; i++)");
            methodsBuilder.openBracket();
            methodsBuilder.append("if(");
            methodsBuilder.append(localVarName);
            methodsBuilder.append("[i].");
        }
        methodsBuilder.append(getGenPolicyCmptType().getBuilderSet().getGenerator(
                association.findQualifier(getIpsProject())).getMethodNameGetProductCmpt());
        methodsBuilder.append("().equals(qualifier))");
        methodsBuilder.openBracket();
        if (association.is1ToManyIgnoringQualifier()) {
            methodsBuilder.append("result.add(");
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("element");
            } else {
                methodsBuilder.append(localVarName);
                methodsBuilder.append("[i]");
            }
            methodsBuilder.append(");");
        } else {
            methodsBuilder.append("return ");
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("element;");
            } else {
                methodsBuilder.append(localVarName);
                methodsBuilder.append("[i];");
            }
        }
        methodsBuilder.closeBracket();
        methodsBuilder.closeBracket();
        if (association.is1ToManyIgnoringQualifier()) {
            if (isUseTypesafeCollections()) {
                methodsBuilder.append("return result;");
            } else {
                methodsBuilder.append("return (");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append("[])");
                methodsBuilder.append("result.toArray(new ");
                methodsBuilder.appendClassName(className);
                methodsBuilder.append("[result.size()]);");
            }
        } else {
            methodsBuilder.append("return null;");
        }
        methodsBuilder.closeBracket();
    }

    /**
     * Returns the name of the method returning the referenced objects, e.g. getCoverages()
     */
    public String getMethodNameGetAllRefObjects() {
        return getLocalizedText("METHOD_GET_ALL_REF_OBJECTS_NAME", StringUtils.capitalize(association
                .getTargetRolePlural()));
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public ICoverage[] getCoverages()
     * </pre>
     * 
     * Java 5 Code sample:
     * 
     * <pre>
     * public List&lt;ICoverage&gt; getCoverages()
     * </pre>
     */
    public void generateSignatureGetAllRefObjects(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String methodName = getMethodNameGetAllRefObjects();
        String returnType;
        if (isUseTypesafeCollections()) {
            returnType = List.class.getName() + "<"
                    + getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true) + ">";
        } else {
            returnType = getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true) + "[]";
        }

        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[] {},
                new String[] {});
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public ICoverage getCoverage(ICoverageType qualifier);
     * </pre>
     */
    protected void generateMethodGetRefObjectByQualifier(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT_BY_QUALIFIER", StringUtils.capitalize(association
                .getTargetRoleSingular()), methodsBuilder);
        generateSignatureGetRefObjectByQualifier(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public ICoverage getCoverage(ICoverageType qualifier)
     * </pre>
     */
    public void generateSignatureGetRefObjectByQualifier(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGetRefObject();
        String returnType = getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true);
        if (association.is1ToManyIgnoringQualifier()) {
            if (isUseTypesafeCollections()) {
                returnType = List.class.getName() + "<" + returnType + ">";
            } else {
                returnType = returnType + "[]";
            }
        }
        IProductCmptType qualifier = association.findQualifier(getIpsProject());
        String qualifierClassName = getGenPolicyCmptType().getBuilderSet().getGenerator(qualifier).getQualifiedName(
                true);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName,
                new String[] { "qualifier" }, new String[] { qualifierClassName });
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest) {
     *     ICoverage[] targets = getCoverages();
     *     for (int i = 0; i &lt; targets.length; i++) {
     *         if (targets[i] == objectToTest)
     *             return true;
     *     }
     *     return false;
     * }
     * </pre>
     * 
     * Java 5 code sample:
     * 
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest) {
     *     return getCoverages().contains(objectToTest);
     * }
     * </pre>
     */
    protected void generateMethodContainsObjectForContainerAssociation(JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        String paramName = getParamNameForContainsObject();

        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateSignatureContainsObject(methodsBuilder);

        methodsBuilder.openBracket();
        if (isUseTypesafeCollections()) {
            methodsBuilder.append("return ");
            methodsBuilder.append(getMethodNameGetAllRefObjects());
            methodsBuilder.append("().contains(" + paramName + ");");
        } else {
            methodsBuilder.appendClassName(getQualifiedClassName((IPolicyCmptType)association
                    .findTarget(getIpsProject()), true));
            methodsBuilder.append("[] targets = ");
            methodsBuilder.append(getMethodNameGetAllRefObjects());
            methodsBuilder.append("();");
            methodsBuilder.append("for(int i=0;i < targets.length;i++) {");
            methodsBuilder.append("if(targets[i] == " + paramName + ") return true; }");
            methodsBuilder.append("return false;");
        }
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public ICoverage getCoverage()
     * </pre>
     */
    public void generateSignatureGetRefObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String methodName = getMethodNameGetRefObject();
        String returnType = getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true);
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[] {},
                new String[] {});
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public int getNumOfCoverages()
     * </pre>
     */
    public void generateSignatureGetNumOfRefObjects(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String methodName = getMethodNameGetNumOfRefObjects();
        methodsBuilder
                .signature(java.lang.reflect.Modifier.PUBLIC, "int", methodName, new String[] {}, new String[] {});
    }

    /**
     * Returns the name of the method returning the number of referenced objects, e.g.
     * getNumOfCoverages()
     */
    public String getMethodNameGetNumOfRefObjects() {
        return getLocalizedText("METHOD_GET_NUM_OF_NAME", StringUtils.capitalize(association.getTargetRolePlural()));
    }

    /**
     * Returns the name of the paramter for the method removing an object from a multi-value
     * association, e.g. objectToRemove
     */
    public String getParamNameForRemoveObject() {
        return getLocalizedText("PARAM_OBJECT_TO_REMOVE_NAME", association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest);
     * </pre>
     */
    protected void generateMethodContainsObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_CONTAINS_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureContainsObject(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public boolean containsCoverage(ICoverage objectToTest)
     * </pre>
     */
    public void generateSignatureContainsObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        String methodName = getMethodNameContainsObject();
        String paramClass = getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true);
        String paramName = getParamNameForContainsObject();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "boolean", methodName, new String[] { paramName },
                new String[] { paramClass });
    }

    /**
     * Returns the name of the method returning the number of referenced objects, e.g.
     * getNumOfCoverages()
     */
    public String getMethodNameContainsObject() {
        return getLocalizedText("METHOD_CONTAINS_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the paramter for the method that tests if an object is references in a
     * multi-value association, e.g. objectToTest
     */
    public String getParamNameForContainsObject() {
        return getLocalizedText("PARAM_OBJECT_TO_TEST_NAME", association.getTargetRoleSingular());
    }

    public void generateSignatureGetMaxCardinalityFor(JavaCodeFragmentBuilder methodsBuilder) {
        String methodName = getMethodNameGetMaxCardinalityFor();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC,
                isUseTypesafeCollections() ? Java5ClassNames.IntegerRange_QualifiedName : IntegerRange.class.getName(),
                methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    public String getMethodNameGetMaxCardinalityFor() {
        return getLocalizedText("METHOD_GET_MAX_CARDINALITY_NAME", StringUtils.capitalize(association
                .getTargetRoleSingular()));
    }

    public void generateMethodGetMaxCardinalityFor(JavaCodeFragmentBuilder methodsBuilder) {
        String[] replacements = new String[] { association.getTargetRoleSingular() };
        appendLocalizedJavaDoc("METHOD_GET_MAX_CARDINALITY", replacements, methodsBuilder);
        generateSignatureGetMaxCardinalityFor(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    protected void generateFieldGetMaxCardinalityFor(JavaCodeFragmentBuilder attrBuilder) {
        appendLocalizedJavaDoc("FIELD_MAX_CARDINALITY", association.getTargetRoleSingular(), attrBuilder);
        String fieldName = getFieldNameGetMaxCardinalityFor();
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.append("new ");
        if (isUseTypesafeCollections()) {
            frag.appendClassName(Java5ClassNames.IntegerRange_QualifiedName);
        } else {
            frag.appendClassName(IntegerRange.class);
        }
        frag.append("(");
        frag.append(association.getMinCardinality());
        frag.append(", ");
        frag.append(association.getMaxCardinality());
        frag.append(")");
        attrBuilder.varDeclaration(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL
                | java.lang.reflect.Modifier.STATIC,
                isUseTypesafeCollections() ? Java5ClassNames.IntegerRange_QualifiedName : IntegerRange.class.getName(),
                fieldName, frag);
        attrBuilder.appendln();
    }

    /**
     * Returns the name for the field GetMaxCardinalityFor + single target role of the provided
     * association
     */
    public String getFieldNameGetMaxCardinalityFor() {
        return getLocalizedText("FIELD_MAX_CARDINALITY_NAME", StringUtils
                .upperCase(association.getTargetRoleSingular()));
    }

    /**
     * Returns the name of the method that returns a reference object at a specified index.
     */
    public String getMethodNameGetRefObjectAtIndex() {
        // TODO extend JavaNamingConvensions for association accessor an mutator methods
        return "get" + association.getTargetRoleSingular();
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorCoverage getMotorCoverage(int index)
     * </pre>
     */
    public void generateSignatureGetRefObjectAtIndex(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT_BY_INDEX", association.getTargetRoleSingular(), methodBuilder);
        methodBuilder.signature(java.lang.reflect.Modifier.PUBLIC, getQualifiedClassName((IPolicyCmptType)association
                .findTarget(getIpsProject()), true), getMethodNameGetRefObjectAtIndex(), new String[] { "index" },
                new String[] { Integer.TYPE.getName() });
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorCoverage getMotorCoverage(int index);
     * </pre>
     */
    protected void generateMethodGetRefObjectAtIndex(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        generateSignatureGetRefObjectAtIndex(methodBuilder);
        methodBuilder.append(';');
    }

    /**
     * Code sample without product component parameter:
     * 
     * <pre>
     * [Javadoc]
     * public Coverage newCoverage();
     * </pre>
     * 
     * Code sample with product component parameter: [Javadoc]
     * 
     * <pre>
     * public Coverage newCoverage(CoverageType coverageType);
     * </pre>
     */
    public void generateMethodNewChild(IPolicyCmptType target,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException {

        String targetTypeName = target.getName();
        String role = association.getTargetRoleSingular();
        if (inclProductCmptArg) {
            String replacements[] = new String[] { targetTypeName, role,
                    getParamNameForProductCmptInNewChildMethod(target.findProductCmptType(getIpsProject())) };
            appendLocalizedJavaDoc("METHOD_NEW_CHILD_WITH_PRODUCTCMPT_ARG", replacements, builder);
        } else {
            appendLocalizedJavaDoc("METHOD_NEW_CHILD", new String[] { targetTypeName, role }, builder);
        }
        generateSignatureNewChild(target, inclProductCmptArg, builder);
        builder.appendln(";");
    }

    /**
     * Code sample without product component argument:
     * 
     * <pre>
     * public Coverage newCoverage()
     * </pre>
     * 
     * Code sample with product component argument:
     * 
     * <pre>
     * public Coverage newCoverage(ICoverageType coverageType)
     * </pre>
     */
    public void generateSignatureNewChild(IPolicyCmptType target,
            boolean inclProductCmptArg,
            JavaCodeFragmentBuilder builder) throws CoreException {

        String methodName = getMethodNameNewChild(association);
        String returnType = getQualifiedClassName((IPolicyCmptType)association.findTarget(getIpsProject()), true);
        String[] argNames, argTypes;
        if (inclProductCmptArg) {
            IProductCmptType productCmptType = target.findProductCmptType(getIpsProject());
            argNames = new String[] { getParamNameForProductCmptInNewChildMethod(productCmptType) };
            argTypes = new String[] { getGenPolicyCmptType().getBuilderSet().getGenerator(productCmptType)
                    .getQualifiedName(true) };
        } else {
            argNames = EMPTY_STRING_ARRAY;
            argTypes = EMPTY_STRING_ARRAY;
        }
        builder.signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, argNames, argTypes);
    }

    /**
     * Returns the name of the method to create a new child object and add it to the parent.
     */
    public String getMethodNameNewChild() {
        return getLocalizedText("METHOD_NEW_CHILD_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of field/member var for the association.
     */
    public abstract String getFieldNameForAssociation() throws CoreException;

    /**
     * Code sample for 1-1 composition
     * 
     * <pre>
     * if (child1 != null) {
     *     copy.child1 = (CpChild1)child1.newCopy();
     *     copy.child1.setParentModelObjectInternal(copy);
     * }
     * </pre>
     * 
     * Code sample for 1-Many composition
     * 
     * <pre>
     * for (Iterator it = child2s.iterator(); it.hasNext();) {
     *     ICpChild2 cpChild2 = (ICpChild2)it.next();
     *     ICpChild2 copycpChild2 = (ICpChild2)cpChild2.newCopy();
     *     ((DependantObject)copycpChild2).setParentModelObjectInternal(copy);
     *     copy.child2s.add(copycpChild2);
     * }
     * </pre>
     */
    public void generateMethodCopyPropertiesForComposition(String paramName, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        String field = getFieldNameForAssociation();
        IPolicyCmptType targetType = association.findTargetPolicyCmptType(getIpsProject());
        String targetTypeQName = getQualifiedClassName(targetType, false);
        if (association.is1ToMany()) {
            ((GenAssociationToMany)this).generateCodeForCopyPropertiesForComposition(paramName, methodsBuilder, field,
                    targetType, targetTypeQName);
            return;
        }
        // 1-1
        methodsBuilder.appendln("if (" + field + "!=null) {");
        methodsBuilder.append(paramName + "." + field + " = (");
        methodsBuilder.appendClassName(targetTypeQName);
        methodsBuilder.appendln(")" + field + "." + MethodNames.NEW_COPY + "();");
        if (targetType.isDependantType()) {
            methodsBuilder.appendln(paramName + "." + field + "." + MethodNames.SET_PARENT + "(" + paramName + ");");
        }
        methodsBuilder.appendln("}");
    }

    public abstract void generateMethodCopyPropertiesForAssociation(String paramName,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    /**
     * @param methodsBuilder
     * @param paramName
     * @param associations
     * @param i
     * @throws CoreException
     */
    public abstract void generateCodeForRemoveChildModelObjectInternal(JavaCodeFragmentBuilder methodsBuilder,
            String paramName) throws CoreException;

    /**
     * {@inheritDoc}
     */
    protected void generateMethods(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (generatesInterface) {
            if (association.isQualified()) {
                generateMethodGetRefObjectByQualifier(builder);
            }
        } else {
            if (association.isQualified()) {
                if (association.isDerivedUnion()) {
                    generateMethodGetRefObjectsByQualifierForDerivedUnion(builder);
                } else {
                    generateMethodGetRefObjectsByQualifierForNonDerivedUnion(builder);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstants(JavaCodeFragmentBuilder builder, IIpsProject ipsProject, boolean generatesInterface)
            throws CoreException {
        if (generatesInterface) {
            if (!association.isDerivedUnion() && !association.getAssociationType().isCompositionDetailToMaster()) {
                generateFieldGetMaxCardinalityFor(builder);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
            IIpsProject ipsProject,
            boolean generatesInterface) throws CoreException {
        // nothing to do
    }

    public abstract void generateCodeForContainerAssociationImplementation(List associations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    public abstract void generateCodeForValidateDependants(JavaCodeFragment body) throws CoreException;

    /**
     * @return
     * @throws CoreException
     */
    public IPolicyCmptType getTargetPolicyCmptType() throws CoreException {
        return association.getIpsProject().findPolicyCmptType(association.getTarget());
    }
}
