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

package org.faktorips.runtime.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.IEnumValueLookupService;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.ProductCmptGenerationNotFoundException;
import org.faktorips.runtime.ProductCmptNotFoundException;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;
import org.faktorips.runtime.jaxb.IpsJAXBContext;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.internal.ModelType;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.faktorips.runtime.test.IpsTestSuite;

/**
 * Abstract implementation of runtime repository.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractRuntimeRepository implements IRuntimeRepository {

    private static final String ROOTIPSTESTSUITENAME = "ipstest"; //$NON-NLS-1$

    private static final ConcurrentHashMap<Class<?>, List<?>> ENUMVALUECACHE = new ConcurrentHashMap<Class<?>, List<?>>();

    // The name of the repository
    private String name;

    // list of repositories this one directly depends on
    private List<IRuntimeRepository> repositories = new ArrayList<IRuntimeRepository>(0);

    // a list of all repositories this one depends on directly or indirectly
    // see getAllRepositories() for further information
    private List<IRuntimeRepository> allRepositories = null;

    private Map<Class<?>, IModelType> modelTypes = new ConcurrentHashMap<Class<?>, IModelType>();

    private Map<String, IModelType> modelTypesByName = new ConcurrentHashMap<String, IModelType>();

    private Map<Class<?>, IEnumValueLookupService<?>> enumValueLookups = new ConcurrentHashMap<Class<?>, IEnumValueLookupService<?>>();

    private IFormulaEvaluatorFactory formulaEvaluatorFactory;

    public AbstractRuntimeRepository(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This default implementation returns the class loader with which this repository class has
     * been loaded.
     */
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public final void addDirectlyReferencedRepository(IRuntimeRepository repository) {
        if (!(repository instanceof AbstractRuntimeRepository)) {
            throw new IllegalArgumentException(
                    "AbstractRuntimeRepository does not support Repositories not derived from AbstractRuntimeRepository!"); //$NON-NLS-1$
        }
        repositories.add(repository);
    }

    public List<IRuntimeRepository> getDirectlyReferencedRepositories() {
        return Collections.unmodifiableList(repositories);
    }

    public List<IRuntimeRepository> getAllReferencedRepositories() {
        if (allRepositories != null) {
            return allRepositories;
        }
        List<IRuntimeRepository> result = new ArrayList<IRuntimeRepository>(repositories.size());
        // list is so small, linear search is ok.
        LinkedList<IRuntimeRepository> candidates = new LinkedList<IRuntimeRepository>();
        candidates.add(this);
        while (!candidates.isEmpty()) {
            IRuntimeRepository candidate = candidates.get(0);
            candidates.remove(0);
            if (candidate != this && !result.contains(candidate)) {
                result.add(candidate);
            }
            for (IRuntimeRepository newCandidate : candidate.getDirectlyReferencedRepositories()) {
                candidates.add(newCandidate);
            }
        }
        allRepositories = Collections.unmodifiableList(result);
        return allRepositories;
    }

    public final IProductComponent getProductComponent(String id) {
        IProductComponent pc = getProductComponentInternal(id);
        if (pc != null) {
            return pc;
        }
        for (IRuntimeRepository repository : repositories) {
            pc = repository.getProductComponent(id);
            if (pc != null) {
                return pc;
            }
        }
        return null;
    }

    public final IProductComponent getExistingProductComponent(String id) {
        if (id == null) {
            return null;
        }
        IProductComponent pc = getProductComponent(id);
        if (pc == null) {
            throw new ProductCmptNotFoundException(name, id);
        }
        return pc;
    }

    /**
     * Same as getProductComponent(String id) but searches only in this repository and not the ones,
     * this repository depends on.
     */
    protected abstract IProductComponent getProductComponentInternal(String id);

    public final IProductComponent getProductComponent(String kindId, String versionId) {
        IProductComponent pc = getProductComponentInternal(kindId, versionId);
        if (pc != null) {
            return pc;
        }
        for (IRuntimeRepository repository : repositories) {
            pc = repository.getProductComponent(kindId, versionId);
            if (pc != null) {
                return pc;
            }
        }
        return null;
    }

    /**
     * Same as getProductComponent(String kindId, String versionId) but searches only in this
     * repository and not the ones, this repository depends on.
     */
    protected abstract IProductComponent getProductComponentInternal(String kindId, String versionId);

    public final List<IProductComponent> getAllProductComponents(String kindId) {
        List<IProductComponent> result = new ArrayList<IProductComponent>();
        if (kindId == null) {
            return result;
        }
        getAllProductComponents(kindId, result);
        for (IRuntimeRepository runtimeRepository : getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)runtimeRepository;
            refRepository.getAllProductComponents(kindId, result);
        }
        return result;
    }

    /**
     * Same as getAllProductComponent(String kindId) but searches only in this repository and not
     * the ones, this repository depends on. Adds the components found to the given result list.
     */
    protected abstract void getAllProductComponents(String kindId, List<IProductComponent> result);

    public IProductComponentGeneration getExistingProductComponentGeneration(String id, Calendar effectiveDate) {
        IProductComponentGeneration gen = getProductComponentGeneration(id, effectiveDate);
        if (gen != null) {
            return gen;
        }
        IProductComponent cmpt = getProductComponent(id);
        if (cmpt == null) {
            throw new ProductCmptGenerationNotFoundException(name, id, effectiveDate, false);
        }
        throw new ProductCmptGenerationNotFoundException(name, id, effectiveDate, true);
    }

    public final IProductComponentGeneration getProductComponentGeneration(String id, Calendar effectiveDate) {

        IProductComponentGeneration pcGen = getProductComponentGenerationInternal(id, effectiveDate);
        if (pcGen != null) {
            DateTime validTo = pcGen.getProductComponent().getValidTo();
            if (validTo != null
                    && validTo.toTimeInMillisecs(effectiveDate.getTimeZone()) < effectiveDate.getTimeInMillis()) {
                // If validTo is set and is before effectiveDate, the generation is invalid
                return null;
            }
            return pcGen;
        }
        for (IRuntimeRepository repository : repositories) {
            pcGen = repository.getProductComponentGeneration(id, effectiveDate);
            if (pcGen != null) {
                return pcGen;
            }
        }
        return null;
    }

    /**
     * Same as getProductComponentGeneration(String id, Calendar effectiveDate) but searches only in
     * this repository and not the ones, this repository depends on.
     */
    protected abstract IProductComponentGeneration getProductComponentGenerationInternal(String id,
            Calendar effectiveDate);

    public final <T extends IProductComponent> List<T> getAllProductComponents(Class<T> productCmptClass) {
        List<T> result = new ArrayList<T>();
        getAllProductComponentsInternal(productCmptClass, result);
        for (IRuntimeRepository runtimeRepository : getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)runtimeRepository;
            refRepository.getAllProductComponentsInternal(productCmptClass, result);
        }
        return result;
    }

    /**
     * Same as {@link #getAllProductComponents(Class)} but only searches in own repository not in
     * referenced ones and adding result to parameter result
     * 
     * @param productCmptClass The class you want to search product components for
     * @param result adding the found product components to result list
     */
    protected <T extends IProductComponent> void getAllProductComponentsInternal(Class<T> productCmptClass,
            List<T> result) {
        List<IProductComponent> allPCmpsOfThisRepos = new ArrayList<IProductComponent>();
        getAllProductComponents(allPCmpsOfThisRepos);
        for (IProductComponent productCmpt : allPCmpsOfThisRepos) {
            if (productCmptClass.isAssignableFrom(productCmpt.getClass())) {
                // checked by isAssignableFrom
                @SuppressWarnings("unchecked")
                T castedProductCmpt = (T)productCmpt;
                result.add(castedProductCmpt);
            }
        }
    }

    public final List<IProductComponent> getAllProductComponents() {
        List<IProductComponent> result = new ArrayList<IProductComponent>();
        getAllProductComponents(result);
        for (IRuntimeRepository runtimeRepository : getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)runtimeRepository;
            refRepository.getAllProductComponents(result);
        }
        return result;
    }

    /**
     * Same as getAllProductComponents() but searches only in this repository and not the ones, this
     * repository depends on. Adds the components found to the given result list.
     */
    protected abstract void getAllProductComponents(List<IProductComponent> result);

    public final List<IProductComponentGeneration> getProductComponentGenerations(IProductComponent productCmpt) {
        List<IProductComponentGeneration> result = new ArrayList<IProductComponentGeneration>();
        getProductComponentGenerations(productCmpt, result);
        for (IRuntimeRepository runtimeRepository : getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)runtimeRepository;
            refRepository.getProductComponentGenerations(productCmpt, result);
        }
        return result;
    }

    /**
     * Same as getProductComponentGenerations() but searches only in this repository and not the
     * ones, this repository depends on. Adds the components found to the given result list.
     */
    public abstract void getProductComponentGenerations(IProductComponent productCmpt,
            List<IProductComponentGeneration> result);

    public final List<String> getAllProductComponentIds() {
        List<String> result = new ArrayList<String>();
        getAllProductComponentIds(result);
        for (IRuntimeRepository runtimeRepository : getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)runtimeRepository;
            refRepository.getAllProductComponentIds(result);
        }
        return result;
    }

    /**
     * Same as getAllProductComponentIds() but searches only in this repository and not the ones,
     * this repository depends on. Adds the components found to the given result list.
     */
    protected abstract void getAllProductComponentIds(List<String> result);

    public List<ITable> getAllTables() {
        List<ITable> result = new ArrayList<ITable>();
        getAllTables(result);
        for (IRuntimeRepository runtimeRepository : getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)runtimeRepository;
            refRepository.getAllTables(result);
        }
        return result;
    }

    /**
     * Same as <code>getAllTables()</code> but searches only in this repository and not the ones,
     * this repository depends on. Adds the tables found to the given result list.
     */
    protected abstract void getAllTables(List<ITable> result);

    public final <T extends ITable> T getTable(Class<T> tableClass) {
        T table = getTableInternal(tableClass);
        if (table != null) {
            return table;
        }

        for (IRuntimeRepository repository : repositories) {
            table = repository.getTable(tableClass);
            if (table != null) {
                return table;
            }
        }

        return null;
    }

    /**
     * Same as {@link #getTable(Class)} but searches only in this repository and not the ones, this
     * repository depends on.
     */
    protected abstract <T extends ITable> T getTableInternal(Class<T> tableClass);

    public ITable getTable(String qualifiedTableName) {
        ITable table = getTableInternal(qualifiedTableName);
        if (table != null) {
            return table;
        }
        for (IRuntimeRepository repository : repositories) {
            table = repository.getTable(qualifiedTableName);
            if (table != null) {
                return table;
            }
        }
        return null;
    }

    /**
     * Same as {@link #getTable(String)}) but searches only in this repository and not the ones,
     * this repository depends on.
     */
    protected abstract ITable getTableInternal(String qualifiedTableName);

    public final List<IpsTest2> getAllIpsTestCases(IRuntimeRepository runtimeRepository) {
        List<IpsTest2> result = new ArrayList<IpsTest2>();
        getAllIpsTestCases(result, runtimeRepository);
        for (IRuntimeRepository refRuntimeRepository : getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)refRuntimeRepository;
            refRepository.getAllIpsTestCases(result, runtimeRepository);
        }
        return result;
    }

    public List<IpsTest2> getIpsTestCasesStartingWith(String qNamePrefix, IRuntimeRepository runtimeRepository) {
        List<IpsTest2> result = new ArrayList<IpsTest2>();
        getIpsTestCasesStartingWith(qNamePrefix, result, runtimeRepository);
        for (IRuntimeRepository refRuntimeRepository : getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)refRuntimeRepository;
            refRepository.getIpsTestCasesStartingWith(qNamePrefix, result, runtimeRepository);
        }
        return result;
    }

    /**
     * Same as {@link #getAllIpsTestCases(IRuntimeRepository)} but searches only in this repository
     * and not the ones, this repository depends on. Adds the components found to the given result
     * list.
     */
    protected abstract void getAllIpsTestCases(List<IpsTest2> result, IRuntimeRepository runtimeRepository);

    /**
     * Gets all ips test cases starting with the given qualified name prefix.
     */
    protected abstract void getIpsTestCasesStartingWith(String qNamePrefix,
            List<IpsTest2> result,
            IRuntimeRepository runtimeRepository);

    public IpsTest2 getIpsTest(String qName) {
        return getIpsTest(qName, this);
    }

    public IpsTest2 getIpsTest(String qName, IRuntimeRepository runtimeRepository) {
        IpsTest2 test = getIpsTestCase(qName, runtimeRepository);
        if (test != null) {
            return test;
        }
        return getIpsTestSuite(qName, runtimeRepository);
    }

    public IpsTestCaseBase getIpsTestCase(String qName) {
        return getIpsTestCase(qName, this);
    }

    public IpsTestCaseBase getIpsTestCase(String qName, IRuntimeRepository runtimeRepository) {
        if (qName == null) {
            throw new NullPointerException();
        }
        IpsTestCaseBase test = getIpsTestCaseInternal(qName, runtimeRepository);
        if (test != null) {
            // test case was found in this repository
            return test;
        }
        for (IRuntimeRepository repository : repositories) {
            test = repository.getIpsTestCase(qName, runtimeRepository);
            if (test != null) {
                // test case was found in depending repository
                return test;
            }
        }
        return null;
    }

    /**
     * Same as {@link #getIpsTestCase(String, IRuntimeRepository)} but searches only in this
     * repository and not the ones, this repository depends on. The given runtimeRepository
     * specifies the repository which will be used to instantiate the test case (e.g. the first
     * repository which contains all dependence repositories).
     */
    protected abstract IpsTestCaseBase getIpsTestCaseInternal(String qName, IRuntimeRepository runtimeRepository);

    public IpsTestSuite getIpsTestSuite(String qNamePrefix) {
        return getIpsTestSuite(qNamePrefix, this);
    }

    public IpsTestSuite getIpsTestSuite(String qNamePrefix, IRuntimeRepository runtimeRepository) {
        if (qNamePrefix == null) {
            throw new NullPointerException();
        }
        Map<String, IpsTestSuite> suites = new ConcurrentHashMap<String, IpsTestSuite>();
        String suiteName = removeLastSegment(qNamePrefix);
        suiteName = suiteName.length() == 0 ? ROOTIPSTESTSUITENAME : suiteName;
        IpsTestSuite rootSuite = new IpsTestSuite(suiteName);
        suites.put(suiteName, rootSuite);

        List<IpsTest2> testCases = getIpsTestCasesStartingWith(qNamePrefix, runtimeRepository);
        // sort list of test cases
        Collections.sort(testCases, new IpsTestComparator());

        for (IpsTest2 testCase : testCases) {
            addTest(suites, testCase);
        }
        return rootSuite;
    }

    private void addTest(Map<String, IpsTestSuite> suites, IpsTest2 test) {
        IpsTestSuite suite = getTestSuite(suites, test.getQualifiedName());
        suite.addTest(test);
    }

    private IpsTestSuite getTestSuite(Map<String, IpsTestSuite> suites, String testCaseQName) {
        String suiteQName = ""; //$NON-NLS-1$
        if (testCaseQName.indexOf(".") >= 0) { //$NON-NLS-1$
            suiteQName = removeLastSegment(testCaseQName);
        }

        if (StringUtils.isEmpty(suiteQName)) {
            suiteQName = ROOTIPSTESTSUITENAME;
        }

        IpsTestSuite suite = suites.get(suiteQName);
        if (suite == null) {
            suite = new IpsTestSuite(suiteQName);
            suites.put(suiteQName, suite);
            addTest(suites, suite);
        }
        return suite;
    }

    private String removeLastSegment(String qName) {
        int index = qName.lastIndexOf('.');
        if (!(index >= 0)) {
            return qName;
        }
        return qName.substring(0, index);
    }

    public IProductComponentGeneration getNextProductComponentGeneration(IProductComponentGeneration generation) {
        if (equals(generation.getRepository())) {
            return getNextProductComponentGenerationInternal(generation);
        }

        for (IRuntimeRepository refRepository : getAllReferencedRepositories()) {
            if (refRepository.equals(generation.getRepository())) {
                return ((AbstractRuntimeRepository)refRepository).getNextProductComponentGenerationInternal(generation);
            }
        }
        throw new IllegalArgumentException(
                "The provided product component generation instance is not hosted in this repository or in the referenced repositories"); //$NON-NLS-1$
    }

    protected abstract IProductComponentGeneration getNextProductComponentGenerationInternal(IProductComponentGeneration generation);

    public int getNumberOfProductComponentGenerations(IProductComponent productCmpt) {
        if (equals(productCmpt.getRepository())) {
            return getNumberOfProductComponentGenerationsInternal(productCmpt);
        }

        for (IRuntimeRepository refRepository : getAllReferencedRepositories()) {
            if (refRepository.equals(productCmpt.getRepository())) {
                return ((AbstractRuntimeRepository)refRepository)
                        .getNumberOfProductComponentGenerationsInternal(productCmpt);
            }
        }
        throw new IllegalArgumentException(
                "The provided product component generation instance is not hosted in this repository or in the referenced repositories"); //$NON-NLS-1$
    }

    protected abstract int getNumberOfProductComponentGenerationsInternal(IProductComponent productCmpt);

    public final IProductComponentGeneration getPreviousProductComponentGeneration(IProductComponentGeneration generation) {
        if (equals(generation.getRepository())) {
            return getPreviousProductComponentGenerationInternal(generation);
        }

        for (IRuntimeRepository refRepository : getAllReferencedRepositories()) {
            if (refRepository.equals(generation.getRepository())) {
                return ((AbstractRuntimeRepository)refRepository)
                        .getPreviousProductComponentGenerationInternal(generation);
            }
        }
        throw new IllegalArgumentException(
                "The provided product component generation instance is not hosted in this repository or in the referenced repositories"); //$NON-NLS-1$
    }

    protected abstract IProductComponentGeneration getPreviousProductComponentGenerationInternal(IProductComponentGeneration generation);

    public final IProductComponentGeneration getLatestProductComponentGeneration(IProductComponent productCmpt) {
        if (equals(productCmpt.getRepository())) {
            return getLatestProductComponentGenerationInternal(productCmpt);
        }

        for (IRuntimeRepository refRepository : getAllReferencedRepositories()) {
            if (refRepository.equals(productCmpt.getRepository())) {
                return ((AbstractRuntimeRepository)refRepository)
                        .getLatestProductComponentGenerationInternal(productCmpt);
            }
        }
        throw new IllegalArgumentException(
                "The provided product component generation instance is not hosted in this repository or in the referenced repositories");
    }

    protected abstract IProductComponentGeneration getLatestProductComponentGenerationInternal(IProductComponent productCmpt);

    public IModelType getModelType(Class<?> modelObjectClass) {
        if (modelTypes.containsKey(modelObjectClass)) {
            return modelTypes.get(modelObjectClass);
        }
        URL xmlFile = modelObjectClass.getResource(modelObjectClass.getName().substring(
                modelObjectClass.getName().lastIndexOf('.') + 1)
                + ".xml"); //$NON-NLS-1$
        IModelType modelType = new ModelType(this);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            InputStream in = xmlFile.openStream();
            XMLStreamReader parser = factory.createXMLStreamReader(in);

            while (parser.hasNext() && parser.next() != XMLStreamConstants.START_ELEMENT) {
                // goto start element
            }

            modelType.initFromXml(parser);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error loading model type info for " + modelObjectClass.getName() + " from XML.", e); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (XMLStreamException e) {
            throw new RuntimeException(
                    "Error loading model type info for " + modelObjectClass.getName() + " from XML.", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        modelTypes.put(modelObjectClass, modelType);
        modelTypesByName.put(modelType.getName(), modelType);
        return modelType;
    }

    public IModelType getModelType(String qualifiedName) {
        if (modelTypesByName.containsKey(qualifiedName)) {
            return modelTypesByName.get(qualifiedName);
        }
        URL xmlFile = this.getClass().getResource(qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1) + ".xml"); //$NON-NLS-1$
        IModelType modelType = new ModelType(this);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            InputStream in = xmlFile.openStream();
            XMLStreamReader parser = factory.createXMLStreamReader(in);

            while (parser.hasNext() && parser.next() != XMLStreamConstants.START_ELEMENT) {
                // goto start element
            }

            modelType.initFromXml(parser);
        } catch (IOException e) {
            throw new RuntimeException("Error loading model type info for " + qualifiedName + " from XML.", e); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (XMLStreamException e) {
            throw new RuntimeException("Error loading model type info for " + qualifiedName + " from XML.", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        modelTypes.put(modelType.getClass(), modelType);
        modelTypesByName.put(qualifiedName, modelType);
        return modelType;
    }

    public IModelType getModelType(IModelObject modelObject) {
        return getModelType(modelObject.getClass());
    }

    public IModelType getModelType(IProductComponent modelObject) {
        return getModelType(modelObject.getClass());
    }

    public final Set<String> getAllModelTypeImplementationClasses() {
        Set<String> result = new HashSet<String>();
        getAllModelTypeImplementationClasses(result);
        for (IRuntimeRepository runtimeRepository : getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)runtimeRepository;
            refRepository.getAllModelTypeImplementationClasses(result);
        }
        return result;
    }

    /**
     * Same as getAllModelTypeImplementationClasses() but searches only in this repository and not
     * the ones, this repository depends on. Adds the types found to the given result list.
     */
    protected abstract void getAllModelTypeImplementationClasses(Set<String> result);

    /**
     * @deprecated This method does only return valid enums if the id attribute of the enum is of
     *             type {@link String}. You should never use this method! Use
     *             {@link #getEnumValue(Class, Object)} instead. This method may be returned in
     *             future releases.
     */
    @Deprecated
    public Object getEnumValue(String uniqueId) {
        int index = uniqueId.indexOf('#');
        if (index == -1) {
            return null;
        }
        String className = uniqueId.substring(0, index);
        String id = uniqueId.substring(index + 1);
        try {
            Class<?> clazz = getClassLoader().loadClass(className);
            return getEnumValue(clazz, id);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public final <T> T getEnumValue(Class<T> clazz, Object value) {
        if (value == null) {
            return null;
        }
        IEnumValueLookupService<T> lookup = getEnumValueLookupService(clazz);
        if (lookup != null) {
            return lookup.getEnumValue(value);
        }
        List<T> enumValues = getEnumValues(clazz);
        if (enumValues == null) {
            return null;
        }
        try {
            Method enumValueIdMethod = clazz.getDeclaredMethod("getEnumValueId", new Class[0]); //$NON-NLS-1$
            enumValueIdMethod.setAccessible(true);
            for (T enumValue : enumValues) {
                Object idValue = enumValueIdMethod.invoke(enumValue, new Object[0]);
                if (value.equals(idValue)) {
                    return enumValue;
                }
            }
        } catch (SecurityException e) {
            throwUnableToCallMethodException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "The provided enumeration class doesn't provide an identifing method getEnumValueId.", e); //$NON-NLS-1$
        } catch (IllegalAccessException e) {
            throwUnableToCallMethodException(e);
        } catch (InvocationTargetException e) {
            throwUnableToCallMethodException(e);
        }
        return null;
    }

    private void throwUnableToCallMethodException(Exception e) {
        throw new IllegalStateException("Unable to call the getEnumValueId of the provided enumeration value.", e); //$NON-NLS-1$
    }

    public final <T> List<T> getEnumValues(Class<T> clazz) {
        List<T> values = null;
        IEnumValueLookupService<T> lookup = getEnumValueLookupService(clazz);
        if (lookup != null) {
            return lookup.getEnumValues();
        } else {
            values = getEnumValuesInternal(clazz);
        }
        List<T> valuesFromType = getEnumValuesDefinedInType(clazz);
        ArrayList<T> allValues = new ArrayList<T>(valuesFromType);
        if (values != null) {
            allValues.addAll(values);
            return Collections.unmodifiableList(allValues);
        }
        for (IRuntimeRepository repository : repositories) {
            values = repository.getEnumValues(clazz);
            if (values != null) {
                return Collections.unmodifiableList(values);
            }
        }
        return allValues;
    }

    /**
     * Returns the list of enumeration values of the enumeration type that is identified by its
     * class which is provided to it.
     */
    protected abstract <T> List<T> getEnumValuesInternal(Class<T> clazz);

    /**
     * Returns the values that are defined in the type by a constant called 'VALUES'. If no such
     * constant is available an empty list is returned. If the constant is available but is either
     * not accessible or of wrong type an exception is thrown.
     * <p>
     * For performance optimization the values are cached in the static map {@link #ENUMVALUECACHE}.
     * We only check once if there is already a cached value. We disclaim a double checking with
     * synchronization because in worst case two threads simply getting the same result. The
     * {@link #ENUMVALUECACHE} is realized by a {@link ConcurrentHashMap}. Only the first evaluation
     * will be put into the cache using {@link ConcurrentHashMap#putIfAbsent(Object, Object)}.
     * 
     * @param enumClass The class of which you want to get the enumeration values
     * @return A list of instances of enumClass that are defined as enumeration values of the
     *         specified type.
     */
    protected <T> List<T> getEnumValuesDefinedInType(Class<T> enumClass) {
        if (ENUMVALUECACHE.containsKey(enumClass)) {
            return getCachedEnumValuesDefinedInType(enumClass);
        } else {
            return getEnumValuesDefinedInTypeByReflection(enumClass);
        }
    }

    private <T> List<T> getCachedEnumValuesDefinedInType(Class<T> enumClass) {
        @SuppressWarnings("unchecked")
        List<T> values = (List<T>)ENUMVALUECACHE.get(enumClass);
        return values;
    }

    private <T> List<T> getEnumValuesDefinedInTypeByReflection(Class<T> enumClass) {
        try {
            Field valuesField = enumClass.getDeclaredField("VALUES");
            @SuppressWarnings("unchecked")
            List<T> values = (List<T>)valuesField.get(null);
            List<?> previousValues = ENUMVALUECACHE.putIfAbsent(enumClass, values);
            if (previousValues != null) {
                @SuppressWarnings("unchecked")
                List<T> castedPreviousValues = (List<T>)previousValues;
                return castedPreviousValues;
            } else {
                return values;
            }
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            // No values are defined in the enum class
            ENUMVALUECACHE.putIfAbsent(enumClass, Collections.emptyList());
            return Collections.emptyList();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void addEnumValueLookupService(IEnumValueLookupService<?> lookup) {
        enumValueLookups.put(lookup.getEnumTypeClass(), lookup);
    }

    public <T> IEnumValueLookupService<T> getEnumValueLookupService(Class<T> enumClazz) {
        @SuppressWarnings("unchecked")
        // because we store every kind of enumValueLookupService in the same map we could not know
        // the type of the object. This is ugly design!
        IEnumValueLookupService<T> enumValueLookupService = (IEnumValueLookupService<T>)enumValueLookups.get(enumClazz);
        return enumValueLookupService;
    }

    public void removeEnumValueLookupService(IEnumValueLookupService<?> lookup) {
        enumValueLookups.remove(lookup.getEnumTypeClass());
    }

    /**
     * Returns all enumeration XmlAdapters available in this repository that generated by
     * Faktor-IPS.
     * 
     * @param repository the runtime repository that needs to be used by the XmlAdapters that are
     *            returned by this method
     */
    protected abstract List<XmlAdapter<?, ?>> getAllInternalEnumXmlAdapters(IRuntimeRepository repository);

    /**
     * Adds all enumeration XmlAdapters available in this repository to the provided list. These are
     * the internal adapters and the adapters specified by the enum value lookup services.
     * 
     * @param adapters the list where the adapters are added to
     * @param repository the runtime repository that needs to be used by the XmlAdapters that are
     *            collected by this method
     * 
     * @see #getAllInternalEnumXmlAdapters(IRuntimeRepository)
     * @see #addEnumValueLookupService(IEnumValueLookupService)
     * @see IEnumValueLookupService#getXmlAdapter()
     */
    private void addAllEnumXmlAdapters(List<XmlAdapter<?, ?>> adapters, IRuntimeRepository repository) {
        adapters.addAll(getAllInternalEnumXmlAdapters(repository));
        for (IEnumValueLookupService<?> lookupService : enumValueLookups.values()) {
            XmlAdapter<?, ?> adapter = lookupService.getXmlAdapter();
            if (adapter != null) {
                adapters.add(adapter);
            }
        }
    }

    /**
     * Creates a {@link JAXBContext} that wraps the provided context and extends the marshaling
     * methods to provide marshaling of Faktor-IPS enumerations and model objects configured by
     * product components.
     */
    public JAXBContext newJAXBContext(JAXBContext ctx) {
        LinkedList<XmlAdapter<?, ?>> adapters = new LinkedList<XmlAdapter<?, ?>>();
        addAllEnumXmlAdapters(adapters, this);
        for (IRuntimeRepository runtimeRepository : getAllReferencedRepositories()) {
            AbstractRuntimeRepository refRepository = (AbstractRuntimeRepository)runtimeRepository;
            refRepository.addAllEnumXmlAdapters(adapters, this);
        }
        return new IpsJAXBContext(ctx, adapters, this);
    }

    /**
     * Creates a new JAXBContext that can marshall / unmarshall all model classes defined in this
     * repository. If the repository references other repositories (directly or indirectly), the
     * context can also handle the classes defined in those.
     * 
     * @throws RuntimeException Exceptions that are thrown while trying to load a class from the
     *             class loader or creating the jaxb context are wrapped into a runtime exception
     */
    public JAXBContext newJAXBContext() {
        try {
            Set<String> classNames = getAllModelTypeImplementationClasses();
            List<Class<?>> classes = new ArrayList<Class<?>>(classNames.size());
            for (String className : classNames) {
                Class<?> clazz = getClassLoader().loadClass(className);
                if (AbstractModelObject.class.isAssignableFrom(clazz)) {
                    classes.add(clazz);
                }
            }
            classes.add(AbstractModelObject.class);
            classes.add(AbstractConfigurableModelObject.class);
            JAXBContext ctx = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
            return newJAXBContext(ctx);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * For default there is no formula evaluation supported.
     * <p>
     * If you want to support formula evaluation you have to override this method or use
     * {@link AbstractRuntimeRepository#setFormulaEvaluatorFactory(IFormulaEvaluatorFactory)}.
     */
    public IFormulaEvaluatorFactory getFormulaEvaluatorFactory() {
        return formulaEvaluatorFactory;
    }

    /**
     * If you want to support formula evaluation you can set a {@link IFormulaEvaluatorFactory}.
     */
    public void setFormulaEvaluatorFactory(IFormulaEvaluatorFactory formulaEvaluatorFactory) {
        this.formulaEvaluatorFactory = formulaEvaluatorFactory;
    }

    public <T> T getCustomRuntimeObject(Class<T> type, String ipsObjectQualifiedName) {
        T pc = getCustomRuntimeObjectInternal(type, ipsObjectQualifiedName);
        if (pc != null) {
            return pc;
        }
        for (IRuntimeRepository repository : repositories) {
            pc = repository.getCustomRuntimeObject(type, ipsObjectQualifiedName);
            if (pc != null) {
                return pc;
            }
        }
        return null;
    }

    /**
     * Same as getCustomRuntimeObject(Class<T> type, String id) but searches only in this repository
     * and not the ones this repository depends on.
     */
    protected abstract <T> T getCustomRuntimeObjectInternal(Class<T> type, String ipsObjectQualifiedName);

    /*
     * Comparator for IpsTest2 objects
     */
    private class IpsTestComparator implements Comparator<IpsTest2> {
        public int compare(IpsTest2 o1, IpsTest2 o2) {
            return (o1).getQualifiedName().compareTo((o2).getQualifiedName());
        }
    }

}
