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

package org.faktorips.devtools.stdbuilder.productcmpt;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptClassBuilder;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptGenerationClassBuilder;
import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Generates the compilation unit that represents the product component generation. Note that only
 * for product component's that contain a config element of type formula a Java compilation unit is
 * generated. This is necessary as the formula is compiled into Java sourcecode and this Java
 * sourcecode is placed in the compilation unit generated for a product component's generation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenerationCuBuilder extends DefaultJavaSourceFileBuilder {

    // property key for the constructor's Javadoc.
    private static final String CONSTRUCTOR_JAVADOC = "CONSTRUCTOR_JAVADOC"; //$NON-NLS-1$

    // the product component generation sourcecode is generated for.
    private IProductCmptGeneration generation;

    // builders needed
    private ProductCmptBuilder productCmptBuilder;
    private ProductCmptClassBuilder productCmptImplBuilder;
    private ProductCmptGenerationClassBuilder productCmptGenImplBuilder;

    private MultiStatus buildStatus;

    public ProductCmptGenerationCuBuilder(StandardBuilderSet builderSet, ProductCmptBuilder productCmptBuilder) {
        super(builderSet, new LocalizedStringsSet(ProductCmptGenerationCuBuilder.class));
        this.productCmptBuilder = productCmptBuilder;
    }

    /**
     * We need the {@link StandardBuilderSet} for formula compilation {@inheritDoc}
     */
    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    public void setProductCmptGeneration(IProductCmptGeneration generation) {
        ArgumentCheck.notNull(generation);
        this.generation = generation;
    }

    public void setProductCmptImplBuilder(ProductCmptClassBuilder builder) {
        productCmptImplBuilder = builder;
    }

    public void setProductCmptGenImplBuilder(ProductCmptGenerationClassBuilder builder) {
        productCmptGenImplBuilder = builder;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType());
    }

    public IProductCmptType getProductCmptType() {
        try {
            return generation.findProductCmptType(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected void generateCodeForJavatype() {
        if (generation == null) {
            addToBuildStatus(new IpsStatus(
                    "The generation needs to be set for this " + ProductCmptGenerationCuBuilder.class)); //$NON-NLS-1$
            return;
        }
        TypeSection mainSection = getMainTypeSection();
        mainSection.setClassModifier(Modifier.PUBLIC);
        try {
            IProductCmptType pcType = generation.getProductCmpt().findProductCmptType(getIpsProject());
            mainSection.setUnqualifiedName(getUnqualifiedClassName());
            mainSection.setSuperClass(productCmptGenImplBuilder.getQualifiedClassName(pcType.getIpsSrcFile()));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }

        buildConstructor(mainSection.getConstructorBuilder());
        IFormula[] formulas = generation.getFormulas();
        for (final IFormula formula : formulas) {
            if (isGenerateFormula(formula)) {
                generateMethodForFormula(formula, mainSection.getMethodBuilder());
            }
        }
    }

    private boolean isGenerateFormula(final IFormula formula) {
        try {
            if (!formula.isValid(getIpsProject())) {
                return false;
            }
        } catch (CoreException e) {
            StdBuilderPlugin.log(e);
            return false;
        }
        if (formula.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Generates the constructor.
     * <p>
     * Example:
     * <p>
     * 
     * <pre>
     * public MotorPolicyPk0(RuntimeRepository repository, String qName, Class policyComponentType) {
     *     super(registry, qName, policyComponentType);
     * }
     * </pre>
     */
    private void buildConstructor(JavaCodeFragmentBuilder codeBuilder) {
        Locale language = getLanguageUsedInGeneratedSourceCode();
        String genName = getChangesInTimeNamingConvention(generation).getGenerationConceptNameSingular(language);
        String javaDoc = getLocalizedText(getIpsSrcFile(), CONSTRUCTOR_JAVADOC, genName);
        try {
            String className = getUnqualifiedClassName();
            String[] argNames = new String[] { "productCmpt" }; //$NON-NLS-1$
            String[] argClassNames = new String[] { productCmptImplBuilder.getQualifiedClassName(generation
                    .getProductCmpt().findProductCmptType(getIpsProject())) };
            JavaCodeFragment body = new JavaCodeFragment("super(productCmpt);"); //$NON-NLS-1$
            codeBuilder.method(Modifier.PUBLIC, null, className, argNames, argClassNames, body, javaDoc);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Generates the method to compute a value as specified by a formula configuration element and
     */
    public void generateMethodForFormula(IFormula formula, JavaCodeFragmentBuilder builder) {
        generateMethodForFormula(formula, builder, true);
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) {
        try {
            super.beforeBuild(ipsSrcFile, status);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
        buildStatus = status;
    }

    private void generateMethodForFormula(IFormula formula,
            JavaCodeFragmentBuilder builder,
            boolean addOverrideAnnotationIfNecessary) {
        try {
            IProductCmptTypeMethod method = formula.findFormulaSignature(getIpsProject());
            if (method.validate(getIpsProject()).containsErrorMsg()) {
                return;
            }

            builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
            if (addOverrideAnnotationIfNecessary) {
                // if the formula is also compiled to XML we have a standard implementation of this
                // method
                appendOverrideAnnotation(builder, method.getModifier().isPublished()
                        && !getBuilderSet().getFormulaCompiling().isCompileToXml());
            }

            generateSignatureForModelMethod(method, builder);

            builder.openBracket();
            builder.append("try {"); //$NON-NLS-1$
            builder.append("return "); //$NON-NLS-1$
            builder.append(ExpressionBuilderHelper.compileFormulaToJava(formula, method, buildStatus));
            builder.appendln(";"); //$NON-NLS-1$
            builder.append("} catch (Exception e) {"); //$NON-NLS-1$
            builder.appendClassName(StringBuffer.class);
            builder.append(" parameterValues=new StringBuffer();"); //$NON-NLS-1$
            // in formula tests the input will not printed in case of an exception
            // because the input is stored in the formula test
            IParameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    builder.append("parameterValues.append(\", \");"); //$NON-NLS-1$
                }
                builder.append("parameterValues.append(\"" + parameters[i].getName() + "=\");"); //$NON-NLS-1$ //$NON-NLS-2$
                ValueDatatype valuetype = getIpsProject().findValueDatatype(parameters[i].getDatatype());
                if (valuetype != null && valuetype.isPrimitive()) {
                    // optimization: we search for value types only as only those can be primitives!
                    builder.append("parameterValues.append(" + parameters[i].getName() + ");"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    builder.append("parameterValues.append(" + parameters[i].getName() + " == null ? \"null\" : " + parameters[i].getName() + ".toString());"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
            builder.append("throw new "); //$NON-NLS-1$
            builder.appendClassName(FormulaExecutionException.class);
            builder.append("(toString(), "); //$NON-NLS-1$
            builder.appendQuoted(StringEscapeUtils.escapeJava(formula.getExpression()));
            builder.appendln(", parameterValues.toString(), e);"); //$NON-NLS-1$
            builder.appendln("}"); //$NON-NLS-1$

            builder.closeBracket();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    public void generateSignatureForModelMethod(IProductCmptTypeMethod method, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {

        IParameter[] parameters = method.getParameters();
        int modifier = method.getJavaModifier();
        boolean resolveTypesToPublishedInterface = method.getModifier().isPublished();
        String returnClass = StdBuilderHelper.transformDatatypeToJavaClassName(method.getDatatype(),
                resolveTypesToPublishedInterface, getBuilderSet(), method.getIpsProject());

        String[] parameterNames = null;
        parameterNames = BuilderHelper.extractParameterNames(parameters);
        String[] parameterTypes = StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters,
                resolveTypesToPublishedInterface, getBuilderSet(), method.getIpsProject());
        String[] parameterInSignatur = parameterNames;
        String[] parameterTypesInSignatur = parameterTypes;
        parameterInSignatur = parameterNames;
        parameterTypesInSignatur = parameterTypes;

        String methodName = method.getName();
        // extend the method signature with the given parameter names
        methodsBuilder
                .signature(modifier, returnClass, methodName, parameterInSignatur, parameterTypesInSignatur, true);

        methodsBuilder.append(" throws ");
        methodsBuilder.appendClassName(FormulaExecutionException.class);
    }

    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    @Override
    protected void getGeneratedJavaTypesThis(IIpsObject ipsObject, IPackageFragment fragment, List<IType> javaTypes) {
        IProductCmpt productCmpt = (IProductCmpt)ipsObject;
        for (IIpsObjectGeneration generation : productCmpt.getGenerations()) {
            IIpsSrcFile generationSrcFile = productCmptBuilder.getVirtualIpsSrcFile((IProductCmptGeneration)generation);
            try {
                String typeName = getUnqualifiedClassName(generationSrcFile);
                ICompilationUnit compilationUnit = fragment.getCompilationUnit(typeName
                        + JavaClassNaming.JAVA_EXTENSION);
                javaTypes.add(compilationUnit.getType(typeName));
            } catch (CoreException e) {
                throw new CoreRuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {
        // no IJavaElement generated here
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

    @Override
    protected boolean generatesInterface() {
        return false;
    }

}
