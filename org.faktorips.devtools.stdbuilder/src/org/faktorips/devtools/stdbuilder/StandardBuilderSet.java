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

package org.faktorips.devtools.stdbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractParameterIdentifierResolver;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.bf.BusinessFunctionBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumContentBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.devtools.stdbuilder.enumtype.EnumXmlAdapterBuilder;
import org.faktorips.devtools.stdbuilder.persistence.EclipseLink1PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.GenericJPA2PersistenceProvider;
import org.faktorips.devtools.stdbuilder.persistence.IPersistenceProvider;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassJaxbAnnGenFactory;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.persistence.PolicyCmptImplClassJpaAnnGenFactory;
import org.faktorips.devtools.stdbuilder.policycmpttype.validationrule.ValidationRuleMessagesPropertiesBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptBuilder;
import org.faktorips.devtools.stdbuilder.productcmpt.ProductCmptXMLBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.GenProductCmptType;
import org.faktorips.devtools.stdbuilder.table.TableContentBuilder;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.devtools.stdbuilder.table.TableRowBuilder;
import org.faktorips.devtools.stdbuilder.testcase.TestCaseBuilder;
import org.faktorips.devtools.stdbuilder.testcasetype.TestCaseTypeClassBuilder;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptGenerationImplClassBuilder;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptImplClassBuilder;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.ClassToInstancesMap;

/**
 * An <code>IpsArtefactBuilderSet</code> implementation that assembles the standard Faktor-IPS
 * <tt>IIpsArtefactBuilder</tt>s.
 * 
 * @author Peter Erzberger
 */
public class StandardBuilderSet extends DefaultBuilderSet {

    private static final String EXTENSION_POINT_ARTEFACT_BUILDER_FACTORY = "artefactBuilderFactory";

    public final static String ID = "org.faktorips.devtools.stdbuilder.ipsstdbuilderset";

    /**
     * Configuration property that enables/disables the generation of a copy method.
     * 
     * @see ICopySupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_COPY_SUPPORT = "generateCopySupport"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of delta computation.
     * 
     * @see IDeltaSupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_DELTA_SUPPORT = "generateDeltaSupport"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of the visitor support.
     * 
     * @see IDeltaSupport
     */
    public final static String CONFIG_PROPERTY_GENERATE_VISITOR_SUPPORT = "generateVisitorSupport"; //$NON-NLS-1$

    /**
     * Configuration property that is supposed to be used to read a configuration value from the
     * IIpsArtefactBuilderSetConfig object provided by the initialize method of an
     * IIpsArtefactBuilderSet instance.
     */
    public final static String CONFIG_PROPERTY_GENERATE_CHANGELISTENER = "generateChangeListener"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the use of enums, if supported by the target
     * java version.
     */
    public final static String CONFIG_PROPERTY_USE_ENUMS = "useJavaEnumTypes"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of JAXB support.
     */
    public final static String CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT = "generateJaxbSupport"; //$NON-NLS-1$

    /**
     * Configuration property contains the persistence provider implementation.
     */
    public final static String CONFIG_PROPERTY_PERSISTENCE_PROVIDER = "persistenceProvider"; //$NON-NLS-1$

    /**
     * Configuration property contains the kind of formula compiling.
     */
    public final static String CONFIG_PROPERTY_FORMULA_COMPILING = "formulaCompiling"; //$NON-NLS-1$

    /**
     * Name of the configuration property that indicates whether toXml() methods should be
     * generated.
     */
    public final static String CONFIG_PROPERTY_TO_XML_SUPPORT = "toXMLSupport"; //$NON-NLS-1$

    /**
     * Name of the configuration property that indicates whether to generate camel case constant
     * names with underscore separator or without. For example if this property is true, the
     * constant for the name checkAnythingRule would be generated as CHECK_ANYTHING_RULE, if the
     * property is false the constant name would be CHECKANYTHINGRUL.
     */
    public final static String CONFIG_PROPERTY_CAMELCASE_SEPARATED = "camelCaseSeparated"; //$NON-NLS-1$

    private ModelService modelService;

    private GeneratorModelContext generatorModelContext;

    private Map<String, CachedPersistenceProvider> allSupportedPersistenceProvider;

    private final String version;

    private final Map<IType, GenType> ipsObjectTypeGenerators;
    private final AnnotationGeneratorFactory[] annotationGeneratorFactories;

    private Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorsMap;

    public StandardBuilderSet() {
        ipsObjectTypeGenerators = new HashMap<IType, GenType>(1000);

        annotationGeneratorFactories = new AnnotationGeneratorFactory[] { new PolicyCmptImplClassJpaAnnGenFactory(), // JPA
                                                                                                                     // support
                new PolicyCmptImplClassJaxbAnnGenFactory() }; // Jaxb support

        initSupportedPersistenceProviderMap();

        version = "3.0.0"; //$NON-NLS-1$
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
    public void clean(IProgressMonitor monitor) {
        super.clean(monitor);
        modelService = new ModelService();
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

        throw new CoreException(new IpsStatus("Unkown subclass " + type.getClass())); //$NON-NLS-1$
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
        code.appendClassName(getTableImplBuilder().getQualifiedClassName(tableStructure.getIpsSrcFile()));
        // create get instance method by using the qualified name of the table content
        code.append(".getInstance(" + MethodNames.GET_THIS_REPOSITORY + "(), \"" + tableContents.getQualifiedName() //$NON-NLS-1$ //$NON-NLS-2$
                + "\").findRowNullRowReturnedForEmtpyResult("); //$NON-NLS-1$

        // TODO pk: findRow is not correct in general. JO: Why?
        for (int i = 0; i < argResults.length; i++) {
            if (i > 0) {
                code.append(", "); //$NON-NLS-1$
            }
            code.append(argResults[i].getCodeFragment());
            result.addMessages(argResults[i].getMessages());
        }
        code.append(").get"); //$NON-NLS-1$
        code.append(StringUtils.capitalize(fct.findAccessedColumn().getName()));
        code.append("()"); //$NON-NLS-1$

        return result;
    }

    @Override
    public IdentifierResolver createFlIdentifierResolver(IExpression formula, ExprCompiler exprCompiler)
            throws CoreException {
        return new StandardParameterIdentifierResolver(formula, exprCompiler);
    }

    @Override
    public IdentifierResolver createFlIdentifierResolverForFormulaTest(IExpression formula, ExprCompiler exprCompiler)
            throws CoreException {
        return new StandardParameterIdentifierResolverForFormulaTest(formula, exprCompiler);
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return true;
    }

    @Override
    public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreException {
        createAnnotationGeneratorMap();
        modelService = new ModelService();
        generatorModelContext = new GeneratorModelContext(config, this, getAnnotationGenerators());
        super.initialize(config);
    }

    @Override
    protected ClassToInstancesMap<IIpsArtefactBuilder> createBuilders() throws CoreException {
        // create policy component type builders
        ClassToInstancesMap<IIpsArtefactBuilder> builders = new ClassToInstancesMap<IIpsArtefactBuilder>();
        builders.put(new PolicyCmptImplClassBuilder(true, this, generatorModelContext, modelService) {
        });
        PolicyCmptImplClassBuilder policyCmptImplClassBuilder = new PolicyCmptImplClassBuilder(false, this,
                generatorModelContext, modelService);
        builders.put(policyCmptImplClassBuilder);

        // create product component type builders
        builders.put(new ProductCmptImplClassBuilder(true, this, generatorModelContext, modelService) {
        });
        builders.put(new ProductCmptGenerationImplClassBuilder(true, this, generatorModelContext, modelService) {
        });
        ProductCmptGenerationImplClassBuilder productCmptGenerationImplClassBuilder = new ProductCmptGenerationImplClassBuilder(
                false, this, generatorModelContext, modelService);
        builders.put(productCmptGenerationImplClassBuilder);
        ProductCmptImplClassBuilder productCmptImplClassBuilder = new ProductCmptImplClassBuilder(false, this,
                generatorModelContext, modelService);
        builders.put(productCmptImplClassBuilder);

        // table structure builders
        TableImplBuilder tableImplBuilder = new TableImplBuilder(this);
        builders.put(tableImplBuilder);
        TableRowBuilder tableRowBuilder = new TableRowBuilder(this);
        builders.put(tableRowBuilder);
        tableImplBuilder.setTableRowBuilder(tableRowBuilder);

        // table content builders
        builders.put(new TableContentBuilder(this));

        // test case type builders
        builders.put(new TestCaseTypeClassBuilder(this));

        // test case builder
        TestCaseBuilder testCaseBuilder = new TestCaseBuilder(this);
        builders.put(testCaseBuilder);

        // toc file builder
        TocFileBuilder tocFileBuilder = new TocFileBuilder(this);
        builders.put(tocFileBuilder);

        builders.put(new BusinessFunctionBuilder(this));
        // New enum type builder
        EnumTypeBuilder enumTypeBuilder = new EnumTypeBuilder(this);
        builders.put(enumTypeBuilder);
        builders.put(new EnumXmlAdapterBuilder(this, enumTypeBuilder));
        builders.put(new EnumContentBuilder(this));

        // product component builders
        ProductCmptBuilder productCmptBuilder = new ProductCmptBuilder(this);
        builders.put(productCmptBuilder);
        IIpsArtefactBuilder productCmptXmlBuilder = new ProductCmptXMLBuilder(IpsObjectType.PRODUCT_CMPT, this);
        builders.put(productCmptXmlBuilder);

        productCmptBuilder.setProductCmptImplBuilder(productCmptImplClassBuilder);
        productCmptBuilder.setProductCmptGenImplBuilder(productCmptGenerationImplClassBuilder);

        // test case builder
        testCaseBuilder.setJavaSourceFileBuilder(policyCmptImplClassBuilder);

        builders.put(new ValidationRuleMessagesPropertiesBuilder(this));

        List<IIpsArtefactBuilder> extendingBuilders = getExtendingArtefactBuilders();
        for (IIpsArtefactBuilder ipsArtefactBuilder : extendingBuilders) {
            builders.put(ipsArtefactBuilder);
        }

        builders.put(new PolicyModelTypeXmlBuilder(this));
        builders.put(new ProductModelTypeXmlBuilder(this));
        tocFileBuilder.setGenerateEntriesForModelTypes(true);

        return builders;
    }

    /**
     * Returns all builders registered with the standard builder set through the extension point
     * "artefactBuilder".
     * 
     * @return a list containing all builders that extend this builder set.
     */
    private List<IIpsArtefactBuilder> getExtendingArtefactBuilders() {
        List<IIpsArtefactBuilder> builders = new ArrayList<IIpsArtefactBuilder>();

        ExtensionPoints extensionPoints = new ExtensionPoints(StdBuilderPlugin.PLUGIN_ID);
        IExtension[] extensions = extensionPoints.getExtension(EXTENSION_POINT_ARTEFACT_BUILDER_FACTORY);
        for (IExtension extension : extensions) {
            IConfigurationElement[] configurationElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configurationElements) {
                if (EXTENSION_POINT_ARTEFACT_BUILDER_FACTORY.equals(configElement.getName())) {
                    IIpsArtefactBuilderFactory builderFactory = ExtensionPoints.createExecutableExtension(extension,
                            configElement, "class", IIpsArtefactBuilderFactory.class); //$NON-NLS-1$
                    IIpsArtefactBuilder builder = builderFactory.createBuilder(this);
                    builders.add(builder);
                }
            }
        }
        return builders;
    }

    private void createAnnotationGeneratorMap() throws CoreException {
        annotationGeneratorsMap = new HashMap<AnnotatedJavaElementType, List<IAnnotationGenerator>>();
        List<AnnotationGeneratorFactory> factories = getAnnotationGeneratorFactoriesRequiredForProject();

        for (AnnotatedJavaElementType type : AnnotatedJavaElementType.values()) {
            ArrayList<IAnnotationGenerator> annotationGenerators = new ArrayList<IAnnotationGenerator>();
            for (AnnotationGeneratorFactory annotationGeneratorFactory : factories) {
                IAnnotationGenerator annotationGenerator = annotationGeneratorFactory.createAnnotationGenerator(type);
                if (annotationGenerator == null) {
                    continue;
                }
                annotationGenerators.add(annotationGenerator);
            }
            annotationGeneratorsMap.put(type, annotationGenerators);
        }
    }

    private List<AnnotationGeneratorFactory> getAnnotationGeneratorFactoriesRequiredForProject() {
        List<AnnotationGeneratorFactory> factories = new ArrayList<AnnotationGeneratorFactory>();
        for (AnnotationGeneratorFactory annotationGeneratorFactorie : annotationGeneratorFactories) {
            if (annotationGeneratorFactorie.isRequiredFor(getIpsProject())) {
                factories.add(annotationGeneratorFactorie);
            }
        }
        return factories;
    }

    /**
     * Returns a code fragment containing all annotations to the given Java Element Type and
     * IpsElement.
     * 
     * @param type Determines the type of annotation to generate. See
     *            {@link AnnotatedJavaElementType} for a list of possible types.
     * @param ipsElement The IPS element to create the annotations for.
     */
    public JavaCodeFragment addAnnotations(AnnotatedJavaElementType type, IIpsElement ipsElement) {
        JavaCodeFragment code = new JavaCodeFragment();
        List<IAnnotationGenerator> generators = annotationGeneratorsMap.get(type);
        if (generators == null) {
            return code;
        }
        for (IAnnotationGenerator generator : generators) {
            // TODO remove the not needed part of annotation handling
            // code.append(generator.createAnnotation(ipsElement));
        }
        return code;
    }

    /**
     * Returns the map of annotation generators used to provide annotations to generated elements.
     * 
     * @return The annotation generator map.
     */
    public Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> getAnnotationGenerators() {
        return annotationGeneratorsMap;
    }

    /**
     * Returns a code fragment containing all annotations to the given Java Element Type and
     * IpsElement using the given builder.
     * 
     * @param type Determines the type of annotation to generate. See
     *            {@link AnnotatedJavaElementType} for a list of possible types.
     * @param ipsElement The IPS element to create the annotations for. <br/>
     *            <code>Null</code> is permitted for certain AnnotatedJavaElementTypes which do not
     *            need further information. This is the case if <code>type</code> is
     *            POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD.
     * 
     * @param builder The builder for the Java Code Fragment to be generated.
     */
    public void addAnnotations(AnnotatedJavaElementType type, IIpsElement ipsElement, JavaCodeFragmentBuilder builder) {
        List<IAnnotationGenerator> generators = annotationGeneratorsMap.get(type);
        if (generators == null) {
            return;
        }
        for (IAnnotationGenerator generator : generators) {
            if (!generator.isGenerateAnnotationFor(ipsElement)) {
                continue;
            }
            // TODO remove the not needed part of annotation handling
            // builder.append(generator.createAnnotation(ipsElement));
            builder.appendln();
        }
        return;
    }

    @Override
    public DatatypeHelper getDatatypeHelperForEnumType(EnumTypeDatatypeAdapter datatypeAdapter) {
        return new EnumTypeDatatypeHelper(getEnumTypeBuilder(), datatypeAdapter);
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
        return true;
    }

    /**
     * Returns whether JAXB support is to be generated by this builder.
     */
    public boolean isGenerateJaxbSupport() {
        return getConfig().getPropertyValueAsBoolean(CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT);
    }

    /**
     * Returns whether toXml() methods are to be generated.
     */
    public boolean isGenerateToXmlSupport() {
        return generatorModelContext.isGenerateToXmlSupport();
    }

    /**
     * Returns whether to generate camel case constant names with underscore separator or without.
     * For example if this property is true, the constant for the property
     * checkAnythingAndDoSomething would be generated as CHECK_ANYTHING_AND_DO_SOMETHING, if the
     * property is false the constant name would be CHECKANYTHINGANDDOSOMETHING.
     */
    public boolean isGenerateSeparatedCamelCase() {
        return generatorModelContext.isGenerateSeparatedCamelCase();
    }

    public FormulaCompiling getFormulaCompiling() {
        String kind = getConfig().getPropertyValueAsString(CONFIG_PROPERTY_FORMULA_COMPILING);
        try {
            return FormulaCompiling.valueOf(kind);
        } catch (Exception e) {
            // if value is not set correctly we use Both as default value
            return FormulaCompiling.Both;
        }
    }

    private void initSupportedPersistenceProviderMap() {
        allSupportedPersistenceProvider = new HashMap<String, CachedPersistenceProvider>(2);
        allSupportedPersistenceProvider.put(IPersistenceProvider.PROVIDER_IMPLEMENTATION_ECLIPSE_LINK_1_1,
                CachedPersistenceProvider.create(EclipseLink1PersistenceProvider.class));
        allSupportedPersistenceProvider.put(IPersistenceProvider.PROVIDER_IMPLEMENTATION_GENERIC_JPA_2_0,
                CachedPersistenceProvider.create(GenericJPA2PersistenceProvider.class));
    }

    @Override
    public boolean isPersistentProviderSupportConverter() {
        IPersistenceProvider persistenceProviderImpl = getPersistenceProviderImplementation();
        return persistenceProviderImpl != null && getPersistenceProviderImplementation().isSupportingConverters();
    }

    @Override
    public boolean isPersistentProviderSupportOrphanRemoval() {
        IPersistenceProvider persistenceProviderImpl = getPersistenceProviderImplementation();
        return persistenceProviderImpl != null && getPersistenceProviderImplementation().isSupportingOrphanRemoval();
    }

    /**
     * Returns the persistence provider or <code>null</code> if no
     */
    public IPersistenceProvider getPersistenceProviderImplementation() {
        String persistenceProviderKey = (String)getConfig().getPropertyValue(CONFIG_PROPERTY_PERSISTENCE_PROVIDER);
        if (StringUtils.isEmpty(persistenceProviderKey) || "none".equalsIgnoreCase(persistenceProviderKey)) {
            return null;
        }
        CachedPersistenceProvider pProviderCached = allSupportedPersistenceProvider.get(persistenceProviderKey);
        if (pProviderCached == null) {
            StdBuilderPlugin.log(new IpsStatus(IStatus.WARNING,
                    "Unknow persistence provider  \"" + persistenceProviderKey //$NON-NLS-1$
                            + "\". Supported provider are: " + allSupportedPersistenceProvider.keySet().toString()));
            return null;
        }

        if (pProviderCached.cachedProvider == null) {
            try {
                pProviderCached.cachedProvider = pProviderCached.persistenceProviderClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return pProviderCached.cachedProvider;
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
     * <tt>IIpsObjectPartContainer</tt>.
     * <p>
     * Returns an empty list if no <tt>IJavaElement</tt>s are generated for the provided
     * <tt>IIpsObjectPartContainer</tt>.
     * <p>
     * The IPS model should be completely valid if calling this method or else the results may not
     * be exhaustive.
     * 
     * @param ipsObjectPartContainer The <tt>IIpsObjectPartContainer</tt> to obtain the generated
     *            <tt>IJavaElement</tt>s for.
     * 
     * @throws NullPointerException If the parameter is null
     */
    public List<IJavaElement> getGeneratedJavaElements(IIpsObjectPartContainer ipsObjectPartContainer) {
        ArgumentCheck.notNull(ipsObjectPartContainer);

        List<IJavaElement> javaElements = new ArrayList<IJavaElement>();
        for (IIpsArtefactBuilder builder : getArtefactBuilders()) {
            if (builder instanceof ProductCmptBuilder) {
                builder = ((ProductCmptBuilder)builder).getGenerationBuilder();
            }
            if (!(builder instanceof JavaSourceFileBuilder)) {
                continue;
            }
            JavaSourceFileBuilder javaBuilder = (JavaSourceFileBuilder)builder;
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsObjectPartContainer.getAdapter(IIpsSrcFile.class);
            if (javaBuilder.isGeneratsArtifactsFor(ipsSrcFile)) {
                javaElements.addAll(javaBuilder.getGeneratedJavaElements(ipsObjectPartContainer));
            }
        }

        return javaElements;
    }

    /**
     * Returns the <tt>ProductCmptGenImplClassBuilder</tt> or <tt>null</tt> if non has been
     * assembled yet.
     */
    public final ProductCmptGenerationImplClassBuilder getProductCmptGenImplClassBuilder() {
        return getBuilderByClass(ProductCmptGenerationImplClassBuilder.class);
    }

    public final ProductCmptBuilder getProductCmptBuilder() {
        return getBuilderByClass(ProductCmptBuilder.class);
    }

    /**
     * Returns the <tt>PolicyCmptImplClassBuilder</tt> or <tt>null</tt> if non has been assembled
     * yet.
     */
    public final PolicyCmptImplClassBuilder getPolicyCmptImplClassBuilder() {
        return getBuilderByClass(PolicyCmptImplClassBuilder.class);
    }

    /**
     * Returns the <tt>ProductCmptImplClassBuilder</tt> or <tt>null</tt> if non has been assembled
     * yet.
     */
    public final ProductCmptImplClassBuilder getProductCmptImplClassBuilder() {
        return getBuilderByClass(ProductCmptImplClassBuilder.class);
    }

    public TableImplBuilder getTableImplBuilder() {
        return getBuilderByClass(TableImplBuilder.class);
    }

    public TableRowBuilder getTableRowBuilder() {
        return getBuilderByClass(TableRowBuilder.class);
    }

    public EnumTypeBuilder getEnumTypeBuilder() {
        return getBuilderByClass(EnumTypeBuilder.class);
    }

    public String getValidationMessageBundleBaseName(IIpsSrcFolderEntry entry) {
        return generatorModelContext.getValidationMessageBundleBaseName(entry);
    }

    private final class StandardParameterIdentifierResolverForFormulaTest extends StandardParameterIdentifierResolver {
        private StandardParameterIdentifierResolverForFormulaTest(IExpression formula2, ExprCompiler exprCompiler) {
            super(formula2, exprCompiler);
        }

        @Override
        protected String getParameterAttributGetterName(IAttribute attribute, Datatype datatype) {
            IType type = attribute.getType();
            try {
                if (type instanceof IPolicyCmptType) {
                    return getGenerator((IPolicyCmptType)type).getMethodNameGetPropertyValue(attribute.getName(),
                            datatype);
                }
                if (type instanceof IProductCmptType) {
                    return getGenerator((IProductCmptType)type).getMethodNameGetPropertyValue(attribute.getName(),
                            datatype);
                }
            } catch (CoreException e) {
                return null;
            }
            return null;
        }

        @Override
        protected CompilationResult compile(IParameter param, String attributeName) {
            CompilationResult compile = super.compile(param, attributeName);
            try {
                Datatype datatype = param.findDatatype(getIpsProject());
                if (datatype instanceof IType) {
                    /*
                     * instead of using the types getter method to get the value for an identifier,
                     * the given datatype plus the attribute will be used as new parameter
                     * identifier, this parameter identifier will also be used as parameter inside
                     * the formula method which uses this code fragment
                     */
                    String code = param.getName() + "_" + attributeName; //$NON-NLS-1$
                    return new CompilationResultImpl(code, compile.getDatatype());
                }
            } catch (CoreException ignored) {
                // the exception was already handled in the compile method of the super class
            }
            return compile;
        }
    }

    private class StandardParameterIdentifierResolver extends AbstractParameterIdentifierResolver {
        private StandardParameterIdentifierResolver(IExpression formula2, ExprCompiler exprCompiler) {
            super(formula2, exprCompiler);
        }

        @Override
        protected void addNewInstanceForEnumType(JavaCodeFragment fragment,
                EnumTypeDatatypeAdapter datatype,
                ExprCompiler exprCompiler,
                String value) throws CoreException {
            getEnumTypeBuilder().setExtendedExprCompiler((ExtendedExprCompiler)exprCompiler);
            fragment.append(getEnumTypeBuilder().getNewInstanceCodeFragement(datatype, value));
        }

        @Override
        protected String getParameterAttributGetterName(IAttribute attribute, Datatype datatype) {
            IType type = attribute.getType();
            try {
                if (type instanceof IPolicyCmptType) {
                    return getGenerator((IPolicyCmptType)type).getMethodNameGetPropertyValue(attribute.getName(),
                            datatype);
                }
                if (type instanceof IProductCmptType) {
                    GenProductCmptType generator = getGenerator((IProductCmptType)type);
                    String parameterAttributeGetter = generator.getMethodNameGetPropertyValue(attribute.getName(),
                            datatype);
                    if (attribute instanceof IProductCmptTypeAttribute) {
                        if (!((IProductCmptTypeAttribute)attribute).isChangingOverTime()) {
                            return generator.getMethodNameGetProductCmpt() + "()." + parameterAttributeGetter;
                        }
                    }
                    return parameterAttributeGetter;
                }
            } catch (CoreException e) {
                return null;
            }
            return null;
        }

        @Override
        protected String getParameterAttributDefaultValueGetterName(IAttribute attribute, Datatype datatype) {
            try {
                GenPolicyCmptType genPolicyCmptType = getGenerator((PolicyCmptType)attribute.getType());
                String getProductCmptGeneration = genPolicyCmptType.getGenProductCmptType()
                        .getMethodNameGetProductCmptGeneration();
                GenPolicyCmptTypeAttribute genPolicyCmptTypeAttribute = genPolicyCmptType
                        .getGenerator((IPolicyCmptTypeAttribute)attribute);
                if (genPolicyCmptTypeAttribute instanceof GenChangeableAttribute) {
                    String methodNameGetDefaultValue = ((GenChangeableAttribute)genPolicyCmptTypeAttribute)
                            .getMethodNameGetDefaultValue();
                    return getProductCmptGeneration + "()." + methodNameGetDefaultValue;
                } else {
                    throw new IllegalStateException("Could not generate default method access. Attribute "
                            + attribute.getName() + " is not changeable.");
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e.getMessage(), e);
            }
        }

        @Override
        protected String getAssociationTargetGetterName(IAssociation association, IPolicyCmptType policyCmptType) {
            try {
                return getGenerator(policyCmptType).getGenerator((IPolicyCmptTypeAssociation)association)
                        .getMethodNameGetRefObject();
            } catch (CoreException e) {
                return null;
            }
        }

        @Override
        protected String getAssociationTargetAtIndexGetterName(IAssociation association, IPolicyCmptType policyCmptType) {
            try {
                return getGenerator(policyCmptType).getGenerator((IPolicyCmptTypeAssociation)association)
                        .getMethodNameGetRefObjectAtIndex();
            } catch (CoreException e) {
                return null;
            }
        }

        @Override
        protected String getAssociationTargetsGetterName(IAssociation association, IPolicyCmptType policyCmptType) {
            try {
                return getGenerator(policyCmptType).getGenerator((IPolicyCmptTypeAssociation)association)
                        .getMethodNameGetAllRefObjects();
            } catch (CoreException e) {
                return null;
            }
        }

        @Override
        protected String getJavaClassName(IType type) {
            try {
                return getGenerator(type).getQualifiedName(true);
            } catch (CoreException e) {
                return null;
            }
        }
    }

    private static class CachedPersistenceProvider {
        Class<? extends IPersistenceProvider> persistenceProviderClass;
        IPersistenceProvider cachedProvider = null;

        private static CachedPersistenceProvider create(Class<? extends IPersistenceProvider> pPClass) {
            CachedPersistenceProvider providerCache = new CachedPersistenceProvider();
            providerCache.persistenceProviderClass = pPClass;
            return providerCache;
        }
    }

    public enum FormulaCompiling {

        Subclass,
        XML,
        Both;

        public boolean isCompileToSubclass() {
            return this == Subclass || this == Both;
        }

        public boolean isCompileToXml() {
            return this == XML || this == Both;
        }
    }
}
