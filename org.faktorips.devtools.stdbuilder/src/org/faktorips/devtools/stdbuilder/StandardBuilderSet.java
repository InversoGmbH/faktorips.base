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

package org.faktorips.devtools.stdbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractParameterIdentifierResolver;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.bf.BusinessFunctionBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumXmlAdapterBuilder;
import org.faktorips.devtools.stdbuilder.formulatest.FormulaTestBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.table.TableContentBuilder;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.devtools.stdbuilder.table.TableRowBuilder;
import org.faktorips.devtools.stdbuilder.testcase.TestCaseBuilder;
import org.faktorips.devtools.stdbuilder.testcasetype.TestCaseTypeClassBuilder;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.ArgumentCheck;

/**
 * An <code>IpsArtefactBuilderSet</code> implementation that assembles the standard Faktor-IPS
 * <tt>IIpsArtefactBuilder</tt>s.
 * 
 * @author Peter Erzberger
 */
public class StandardBuilderSet extends DefaultBuilderSet {

    /**
     * Configuration property that enables/disables the generation of a copy method.
     * 
     * @see ICopySupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_COPY_SUPPORT = "generateCopySupport";

    /**
     * Configuration property that enables/disables the generation of delta computation.
     * 
     * @see IDeltaSupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_DELTA_SUPPORT = "generateDeltaSupport";

    /**
     * Configuration property that enables/disables the generation of the visitor support.
     * 
     * @see IDeltaSupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_VISITOR_SUPPORT = "generateVisitorSupport";

    /**
     * Configuration property that is supposed to be used to read a configuration value from the
     * IIpsArtefactBuilderSetConfig object provided by the initialize method of an
     * IIpsArtefactBuilderSet instance.
     */
    public final static String CONFIG_PROPERTY_GENERATE_CHANGELISTENER = "generateChangeListener";

    /**
     * Configuration property that enables/disables the use of typesafe collections, if supported by
     * the target java version.
     */
    public final static String CONFIG_PROPERTY_USE_TYPESAFE_COLLECTIONS = "useTypesafeCollections";

    /**
     * Configuration property that enables/disables the use of enums, if supported by the target
     * java version.
     */
    public final static String CONFIG_PROPERTY_USE_ENUMS = "useJavaEnumTypes";

    /**
     * Configuration property that enables/disables the generation of JAXB support.
     */
    public final static String CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT = "generateJaxbSupport";

    private TableImplBuilder tableImplBuilder;

    private TableRowBuilder tableRowBuilder;

    private PolicyCmptInterfaceBuilder policyCmptInterfaceBuilder;

    private PolicyCmptImplClassBuilder policyCmptImplClassBuilder;

    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;

    private ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder;

    private ProductCmptInterfaceBuilder productCmptInterfaceBuilder;

    private ProductCmptImplClassBuilder productCmptImplClassBuilder;

    private EnumTypeBuilder enumTypeBuilder;

    private final String version;

    private final Map<IType, GenType> ipsObjectTypeGenerators;

    public StandardBuilderSet() {
        ipsObjectTypeGenerators = new HashMap<IType, GenType>(1000);
        version = "2.4.0";
        // Following code sections sets the version to the stdbuilder-plugin/bundle version.
        // Most of the time we hardwire the version of the generated code here, but from time to
        // time
        // we want to sync it with the plugin version, so the code remains here.
        // 
        // Version versionObj =
        // Version.parseVersion((String)StdBuilderPlugin.getDefault().getBundle(
        // ).getHeaders().get(org
        // .osgi.framework.Constants.BUNDLE_VERSION));
        // StringBuffer buf = new StringBuffer();
        // buf.append(versionObj.getMajor());
        // buf.append('.');
        // buf.append(versionObj.getMinor());
        // buf.append('.');
        // buf.append(versionObj.getMicro());
        // version = buf.toString();
    }

    @Override
    public void afterBuildProcess(int buildKind) throws CoreException {
        clearGenerators();
    }

    @Override
    public void beforeBuildProcess(int buildKind) throws CoreException {
        clearGenerators();
    }

    @Override
    public boolean isSupportTableAccess() {
        return true;
    }

    private void clearGenerators() {
        ipsObjectTypeGenerators.clear();
    }

    public GenType getGenerator(IType type) throws CoreException {
        if (type == null) {
            return null;
        }

        if (type instanceof IPolicyCmptType) {
            return getGenerator((IPolicyCmptType)type);
        }
        if (type instanceof IProductCmptType) {
            return getGenerator((IProductCmptType)type);
        }

        throw new CoreException(new IpsStatus("Unkown subclass " + type.getClass()));
    }

    public GenPolicyCmptType getGenerator(IPolicyCmptType policyCmptType) throws CoreException {
        if (policyCmptType == null) {
            return null;
        }

        GenPolicyCmptType generator = (GenPolicyCmptType)ipsObjectTypeGenerators.get(policyCmptType);
        if (generator == null) {
            generator = new GenPolicyCmptType(policyCmptType, this);
            ipsObjectTypeGenerators.put(policyCmptType, generator);
        }

        return generator;
    }

    public GenProductCmptType getGenerator(IProductCmptType productCmptType) throws CoreException {
        if (productCmptType == null) {
            return null;
        }

        GenProductCmptType generator = (GenProductCmptType)ipsObjectTypeGenerators.get(productCmptType);
        if (generator == null) {
            generator = new GenProductCmptType(productCmptType, this);
            ipsObjectTypeGenerators.put(productCmptType, generator);
        }

        return generator;
    }

    @Override
    public CompilationResult getTableAccessCode(ITableContents tableContents,
            ITableAccessFunction fct,
            CompilationResult[] argResults) throws CoreException {

        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        JavaCodeFragment code = new JavaCodeFragment();
        ITableStructure tableStructure = fct.getTableStructure();

        CompilationResultImpl result = new CompilationResultImpl(code, returnType);
        result.addAllIdentifierUsed(argResults);
        code.appendClassName(tableImplBuilder.getQualifiedClassName(tableStructure.getIpsSrcFile()));
        // create get instance method by using the qualified name of the table content
        code.append(".getInstance(" + MethodNames.GET_REPOSITORY + "(), \"" + tableContents.getQualifiedName() //$NON-NLS-1$ //$NON-NLS-2$
                + "\").findRowNullRowReturnedForEmtpyResult(");

        // TODO pk: findRow is not correct in general. JO: Why?
        for (int i = 0; i < argResults.length; i++) {
            if (i > 0) {
                code.append(", ");
            }
            code.append(argResults[i].getCodeFragment());
            result.addMessages(argResults[i].getMessages());
        }
        code.append(").get");
        code.append(StringUtils.capitalize(fct.findAccessedColumn().getName()));
        code.append("()");

        return result;
    }

    @Override
    public IdentifierResolver createFlIdentifierResolver(IFormula formula, ExprCompiler exprCompiler)
            throws CoreException {
        return new AbstractParameterIdentifierResolver(formula, exprCompiler) {

            @Override
            protected void addNewInstanceForEnumType(JavaCodeFragment fragment,
                    EnumTypeDatatypeAdapter datatype,
                    ExprCompiler exprCompiler,
                    String value) throws CoreException {
                ExtendedExprCompiler compiler = (ExtendedExprCompiler)exprCompiler;
                fragment.append(enumTypeBuilder.getNewInstanceCodeFragement(datatype, value, compiler
                        .getRuntimeRepositoryExpression()));
            }

            @Override
            protected String getParameterAttributGetterName(IAttribute attribute, Datatype datatype) {
                try {

                    if (datatype instanceof IPolicyCmptType) {
                        return getGenerator((IPolicyCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }
                    if (datatype instanceof IProductCmptType) {
                        return getGenerator((IProductCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }

                } catch (CoreException e) {
                    return null;
                }

                return null;
            }
        };
    }

    @Override
    public IdentifierResolver createFlIdentifierResolverForFormulaTest(IFormula formula, ExprCompiler exprCompiler)
            throws CoreException {
        return new AbstractParameterIdentifierResolver(formula, exprCompiler) {

            @Override
            protected void addNewInstanceForEnumType(JavaCodeFragment fragment,
                    EnumTypeDatatypeAdapter datatype,
                    ExprCompiler exprCompiler,
                    String value) throws CoreException {
                ExtendedExprCompiler compiler = (ExtendedExprCompiler)exprCompiler;
                fragment.append(enumTypeBuilder.getNewInstanceCodeFragement(datatype, value, compiler
                        .getRuntimeRepositoryExpression()));
            }

            @Override
            protected String getParameterAttributGetterName(IAttribute attribute, Datatype datatype) {
                try {

                    if (datatype instanceof IPolicyCmptType) {
                        return getGenerator((IPolicyCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }
                    if (datatype instanceof IProductCmptType) {
                        return getGenerator((IProductCmptType)datatype).getMethodNameGetPropertyValue(
                                attribute.getName(), datatype);
                    }

                } catch (CoreException e) {
                    return null;
                }

                return null;
            }

            @Override
            protected CompilationResult compile(IParameter param, String attributeName, Locale locale) {
                CompilationResult compile = super.compile(param, attributeName, locale);
                try {
                    Datatype datatype = param.findDatatype(getIpsProject());
                    if (datatype instanceof IType) {
                        /*
                         * instead of using the types getter method to get the value for an
                         * identifier, the given datatype plus the attribute will be used as new
                         * parameter identifier, this parameter identifier will also be used as
                         * parameter inside the formula method which uses this code fragment
                         */
                        String code = param.getName() + "_" + attributeName;
                        return new CompilationResultImpl(code, compile.getDatatype());
                    }
                } catch (CoreException ignored) {
                    // the exception was already handled in the compile method of the super class
                }
                return compile;
            }

        };
    }

    /**
     * Returns the package name for the artefacts generated by the given builder. The builder MUST
     * also implement the {@link IIpsStandardArtefactBuilder} interface.
     * 
     * @throws IllegalArgumentException if builder does not implement
     *             {@link IIpsStandardArtefactBuilder}
     * @throws NullPointerException if <code>builder</code> or <code>ipsSrcFile</code> is
     *             <code>null</code>.
     * @throws CoreException if an error occurs while accessing the ips object path.
     */
    public String getPackageNameForGeneratedArtefacts(IIpsArtefactBuilder builder, IIpsSrcFile ipsSrcFile)
            throws CoreException {
        ArgumentCheck.isSubclassOf(builder.getClass(), IIpsStandardArtefactBuilder.class);
        boolean mergable = !builder.buildsDerivedArtefacts();
        boolean published = ((IIpsStandardArtefactBuilder)builder).buildsPublishedArtefacts();
        return getPackageNameForGeneratedArtefacts(ipsSrcFile, published, mergable);
    }

    @Override
    public String getPackage(String kind, IIpsSrcFile ipsSrcFile) throws CoreException {
        String returnValue = super.getPackage(kind, ipsSrcFile);
        if (returnValue != null) {
            return returnValue;
        }

        IpsObjectType objectType = ipsSrcFile.getIpsObjectType();
        if (BusinessFunctionIpsObjectType.getInstance().equals(objectType)) {
            return getPackageNameForMergablePublishedArtefacts(ipsSrcFile);
        }
        if (IpsObjectType.ENUM_TYPE.equals(objectType) && EnumXmlAdapterBuilder.PACKAGE_STRUCTURE_KIND_ID.equals(kind)) {
            return getPackageNameForMergableInternalArtefacts(ipsSrcFile);
        }
        if (IpsObjectType.ENUM_TYPE.equals(objectType)) {
            return getPackageNameForMergablePublishedArtefacts(ipsSrcFile);
        }

        throw new IllegalArgumentException("Unexpected kind id " + kind + " for the IpsObjectType: " + objectType);
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return true;
    }

    @Override
    protected IIpsArtefactBuilder[] createBuilders() throws CoreException {
        // create policy component type builders
        policyCmptImplClassBuilder = new PolicyCmptImplClassBuilder(this, KIND_POLICY_CMPT_IMPL);
        policyCmptInterfaceBuilder = new PolicyCmptInterfaceBuilder(this, KIND_POLICY_CMPT_INTERFACE);

        // create product component type builders
        productCmptInterfaceBuilder = new ProductCmptInterfaceBuilder(this, KIND_PRODUCT_CMPT_INTERFACE);
        productCmptImplClassBuilder = new ProductCmptImplClassBuilder(this, KIND_PRODUCT_CMPT_IMPL);
        productCmptGenInterfaceBuilder = new ProductCmptGenInterfaceBuilder(this,
                DefaultBuilderSet.KIND_PRODUCT_CMPT_GENERATION_INTERFACE);
        productCmptGenImplClassBuilder = new ProductCmptGenImplClassBuilder(this,
                DefaultBuilderSet.KIND_PRODUCT_CMPT_GENERATION_IMPL);

        // product component builders
        ProductCmptBuilder productCmptGenerationImplBuilder = new ProductCmptBuilder(this,
                KIND_PRODUCT_CMPT_GENERATION_IMPL);
        IIpsArtefactBuilder productCmptContentCopyBuilder = new ProductCmptXMLBuilder(IpsObjectType.PRODUCT_CMPT, this,
                KIND_PRODUCT_CMPT_CONTENT);

        // table structure builders
        tableImplBuilder = new TableImplBuilder(this, KIND_TABLE_IMPL);
        tableRowBuilder = new TableRowBuilder(this, KIND_TABLE_ROW);
        tableImplBuilder.setTableRowBuilder(tableRowBuilder);

        // table content builders
        IIpsArtefactBuilder tableContentCopyBuilder = new TableContentBuilder(this, KIND_TABLE_CONTENT);

        // test case type builders
        TestCaseTypeClassBuilder testCaseTypeClassBuilder = new TestCaseTypeClassBuilder(this,
                KIND_TEST_CASE_TYPE_CLASS);

        // test case builder
        TestCaseBuilder testCaseBuilder = new TestCaseBuilder(this);

        // formula test builder
        FormulaTestBuilder formulaTestBuilder = new FormulaTestBuilder(this, KIND_FORMULA_TEST_CASE);

        // toc file builder
        TocFileBuilder tocFileBuilder = new TocFileBuilder(this);

        BusinessFunctionBuilder businessFunctionBuilder = new BusinessFunctionBuilder(this,
                BusinessFunctionBuilder.PACKAGE_STRUCTURE_KIND_ID);
        //
        // wire up the builders
        //

        // policy component type builders

        // New enum type builder
        enumTypeBuilder = new EnumTypeBuilder(this);
        EnumXmlAdapterBuilder enumXmlAdapterBuilder = new EnumXmlAdapterBuilder(this, enumTypeBuilder);

        IIpsArtefactBuilder enumContentBuilder = new XmlContentFileCopyBuilder(IpsObjectType.ENUM_CONTENT, this,
                KIND_ENUM_CONTENT);

        // product component builders.
        productCmptGenerationImplBuilder.setProductCmptImplBuilder(productCmptImplClassBuilder);
        productCmptGenerationImplBuilder.setProductCmptGenImplBuilder(productCmptGenImplClassBuilder);

        // test case builder
        testCaseBuilder.setJavaSourceFileBuilder(policyCmptImplClassBuilder);

        // formula test builder
        formulaTestBuilder.setProductCmptInterfaceBuilder(productCmptInterfaceBuilder);
        formulaTestBuilder.setProductCmptBuilder(productCmptGenerationImplBuilder);
        formulaTestBuilder.setProductCmptGenImplClassBuilder(productCmptGenImplClassBuilder);

        // toc file builders
        tocFileBuilder.setPolicyCmptImplClassBuilder(policyCmptImplClassBuilder);
        tocFileBuilder.setProductCmptTypeImplClassBuilder(productCmptImplClassBuilder);
        tocFileBuilder.setProductCmptBuilder(productCmptGenerationImplBuilder);
        tocFileBuilder.setProductCmptGenImplClassBuilder(productCmptGenImplClassBuilder);
        tocFileBuilder.setTableImplBuilder(tableImplBuilder);
        tocFileBuilder.setTestCaseTypeClassBuilder(testCaseTypeClassBuilder);
        tocFileBuilder.setTestCaseBuilder(testCaseBuilder);
        tocFileBuilder.setFormulaTestBuilder(formulaTestBuilder);
        tocFileBuilder.setEnumTypeBuilder(enumTypeBuilder);
        tocFileBuilder.setEnumXmlAdapterBuilder(enumXmlAdapterBuilder);

        if (ComplianceCheck.isComplianceLevelAtLeast5(getIpsProject())) {
            ModelTypeXmlBuilder policyModelTypeBuilder = new ModelTypeXmlBuilder(IpsObjectType.POLICY_CMPT_TYPE, this,
                    KIND_MODEL_TYPE);
            ModelTypeXmlBuilder productModelTypeBuilder = new ModelTypeXmlBuilder(IpsObjectType.PRODUCT_CMPT_TYPE,
                    this, KIND_MODEL_TYPE);
            tocFileBuilder.setPolicyModelTypeXmlBuilder(policyModelTypeBuilder);
            tocFileBuilder.setProductModelTypeXmlBuilder(productModelTypeBuilder);
            tocFileBuilder.setGenerateEntriesForModelTypes(true);
            return new IIpsArtefactBuilder[] { tableImplBuilder, tableRowBuilder, productCmptGenInterfaceBuilder,
                    productCmptGenImplClassBuilder, productCmptInterfaceBuilder, productCmptImplClassBuilder,
                    policyCmptImplClassBuilder, policyCmptInterfaceBuilder, productCmptGenerationImplBuilder,
                    tableContentCopyBuilder, productCmptContentCopyBuilder, testCaseTypeClassBuilder, testCaseBuilder,
                    formulaTestBuilder, tocFileBuilder, policyModelTypeBuilder, productModelTypeBuilder,
                    businessFunctionBuilder, enumTypeBuilder, enumContentBuilder, enumXmlAdapterBuilder };
        } else {
            tocFileBuilder.setGenerateEntriesForModelTypes(false);
            return new IIpsArtefactBuilder[] { tableImplBuilder, tableRowBuilder, productCmptGenInterfaceBuilder,
                    productCmptGenImplClassBuilder, productCmptInterfaceBuilder, productCmptImplClassBuilder,
                    policyCmptImplClassBuilder, policyCmptInterfaceBuilder, productCmptGenerationImplBuilder,
                    tableContentCopyBuilder, productCmptContentCopyBuilder, testCaseTypeClassBuilder, testCaseBuilder,
                    formulaTestBuilder, tocFileBuilder, businessFunctionBuilder, enumTypeBuilder, enumContentBuilder };
        }
    }

    @Override
    public DatatypeHelper getDatatypeHelperForEnumType(EnumTypeDatatypeAdapter datatypeAdapter) {
        return new EnumTypeDatatypeHelper(enumTypeBuilder, datatypeAdapter);
    }

    /**
     * Returns the standard builder plugin version in the format [major.minor.micro]. The version
     * qualifier is not included in the version string.
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Returns whether Java5 enums shall be used in the code generated by this builder.
     */
    public boolean isUseEnums() {
        return getConfig().getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_USE_ENUMS).booleanValue();
    }

    /**
     * Returns if Java 5 typesafe collections shall be used in the code generated by this builder.
     */
    public boolean isUseTypesafeCollections() {
        return getConfig().getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_USE_TYPESAFE_COLLECTIONS)
                .booleanValue();
    }

    /**
     * Returns whether JAXB support is to be generated by this builder.
     */
    public boolean isGenerateJaxbSupport() {
        return getConfig().getPropertyValueAsBoolean(CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT);
    }

    public String getJavaClassName(Datatype datatype) throws CoreException {
        if (datatype instanceof IPolicyCmptType) {
            return getGenerator((IPolicyCmptType)datatype).getQualifiedName(true);
        }

        if (datatype instanceof IProductCmptType) {
            return getGenerator((IProductCmptType)datatype).getQualifiedName(true);
        }

        return datatype.getJavaClassName();
    }

    /**
     * Returns a list containing all <tt>IJavaElement</tt>s this builder set generates for the given
     * <tt>IIpsElement</tt>.
     * <p>
     * Returns an empty list if no <tt>IJavaElement</tt>s are generated for the provided
     * <tt>IIpsElement</tt>.
     * <p>
     * The IPS model should be completely valid if calling this method or else the results may not
     * be exhaustive.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to obtain the generated <tt>IJavaElement</tt>s
     *            for.
     * 
     * @throws NullPointerException If <tt>ipsElement</tt> is <tt>null</tt>.
     */
    public List<IJavaElement> getGeneratedJavaElements(IIpsElement ipsElement) {
        ArgumentCheck.notNull(ipsElement);

        List<IJavaElement> javaElements = new ArrayList<IJavaElement>();
        for (IIpsArtefactBuilder builder : getArtefactBuilders()) {
            if (!(builder instanceof JavaSourceFileBuilder)) {
                continue;
            }
            JavaSourceFileBuilder javaBuilder = (JavaSourceFileBuilder)builder;
            javaElements.addAll(javaBuilder.getGeneratedJavaElements(ipsElement));
        }

        return javaElements;
    }

    /**
     * Returns the <tt>ProductCmptGenImplClassBuilder</tt> or <tt>null</tt> if non has been
     * assembled yet.
     */
    public final ProductCmptGenImplClassBuilder getProductCmptGenImplClassBuilder() {
        return productCmptGenImplClassBuilder;
    }

    /**
     * Returns the <tt>ProductCmptGenInterfaceBuilder</tt> or <tt>null</tt> if non has been
     * assembled yet.
     */
    public final ProductCmptGenInterfaceBuilder getProductCmptGenInterfaceBuilder() {
        return productCmptGenInterfaceBuilder;
    }

    /**
     * Returns the <tt>PolicyCmptImplClassBuilder</tt> or <tt>null</tt> if non has been assembled
     * yet.
     */
    public final PolicyCmptImplClassBuilder getPolicyCmptImplClassBuilder() {
        return policyCmptImplClassBuilder;
    }

    /**
     * Returns the <tt>PolicyCmptInterfaceBuilder</tt> or <tt>null</tt> if non has been assembled
     * yet.
     */
    public final PolicyCmptInterfaceBuilder getPolicyCmptInterfaceBuilder() {
        return policyCmptInterfaceBuilder;
    }

    /**
     * Returns the <tt>ProductCmptImplClassBuilder</tt> or <tt>null</tt> if non has been assembled
     * yet.
     */
    public final ProductCmptImplClassBuilder getProductCmptImplClassBuilder() {
        return productCmptImplClassBuilder;
    }

    /**
     * Returns the <tt>ProductCmptInterfaceBuilder</tt> or <tt>null</tt> if non has been assembled
     * yet.
     */
    public final ProductCmptInterfaceBuilder getProductCmptInterfaceBuilder() {
        return productCmptInterfaceBuilder;
    }

}
