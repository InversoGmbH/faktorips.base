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

package org.faktorips.devtools.core.internal.model.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.model.testcasetype.TestValueParameter;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestCaseTestCaseTypeDelta;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.ui.editors.testcase.TestCaseHierarchyPath;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Test case class. Defines a concrete test case based on a test case type definition.
 * 
 * @author Joerg Ortmann
 */
public class TestCase extends IpsObject implements ITestCase {

    /* Name of corresponding test case type */
    private String testCaseType = ""; //$NON-NLS-1$

    /* Children */
    private List testObjects = new ArrayList();

    public TestCase(IIpsSrcFile file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        return (IIpsElement[])testObjects.toArray(new IIpsElement[0]);
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        this.testObjects = new ArrayList();
    }

    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof ITestObject) {
            testObjects.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof ITestObject) {
            try {
                removeTestObject((ITestObject)part);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        String xmlTagName = xmlTag.getNodeName();
        if (TestPolicyCmpt.TAG_NAME.equals(xmlTagName)) {
            return newTestPolicyCmptInternal(id);
        } else if (TestValue.TAG_NAME.equals(xmlTagName)) {
            return newTestValueInternal(id);
        } else if (TestRule.TAG_NAME.equals(xmlTagName)) {
            return newTestRuleInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TEST_CASE;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        throw new IllegalArgumentException("Unknown part type: " + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        testCaseType = element.getAttribute(PROPERTY_TEST_CASE_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TEST_CASE_TYPE, testCaseType);
    }

    /**
     * {@inheritDoc}
     */
    public IDependency[] dependsOn() throws CoreException {
        Set dependencies = new HashSet();
        // the test case depends on the test case type
        if (StringUtils.isNotEmpty(testCaseType)) {
            dependencies.add(IpsObjectDependency.createInstanceOfDependency(this.getQualifiedNameType(), new QualifiedNameType(testCaseType,
                    IpsObjectType.TEST_CASE_TYPE)));
        }
        // add dependency to product cmpts
        ITestPolicyCmpt[] testCmpts = getTestPolicyCmpts();
        for (int i = 0; i < testCmpts.length; i++) {
            addQualifiedNameTypesForTestPolicyCmpt(dependencies, testCmpts[i]);
        }
        return (IDependency[])dependencies.toArray(new IDependency[dependencies.size()]);
    }

    /*
     * Adds the dependencies to the given list for the given test policy cmpt and their childs
     */
    private void addQualifiedNameTypesForTestPolicyCmpt(Set dependencies, ITestPolicyCmpt cmpt) throws CoreException {
        if (cmpt == null) {
            return;
        }
        if (StringUtils.isNotEmpty(cmpt.getProductCmpt())) {
            dependencies.add(IpsObjectDependency.createReferenceDependency(this.getQualifiedNameType(), new QualifiedNameType(
                    cmpt.getProductCmpt(), IpsObjectType.PRODUCT_CMPT)));
        }
        ITestPolicyCmptRelation[] testRelations = cmpt.getTestPolicyCmptRelations();
        for (int i = 0; i < testRelations.length; i++) {
            // get the dependencies for the childs of the given test policy cmpt
            if (testRelations[i].isComposition()) {
                addQualifiedNameTypesForTestPolicyCmpt(dependencies, testRelations[i].findTarget());
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt[] getAllTestPolicyCmpt() throws CoreException{
        List allPolicyCmpts = new ArrayList();
        ITestPolicyCmpt[] testCmpts = getTestPolicyCmpts();
        for (int i = 0; i < testCmpts.length; i++) {
            addChildTestPolicyCmpt(allPolicyCmpts, testCmpts[i]);
        }
        return (ITestPolicyCmpt[]) allPolicyCmpts.toArray(new ITestPolicyCmpt[allPolicyCmpts.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestObject[] getAllTestObjects() throws CoreException{
        ITestPolicyCmpt[] testPolicyCmpts = getAllTestPolicyCmpt();
        ITestRule[] testRuleObjects = getTestRuleObjects();
        ITestValue[] testValues = getTestValues();
        
        ITestObject[] result = new ITestObject[testPolicyCmpts.length + testRuleObjects.length + testValues.length];
        System.arraycopy(testPolicyCmpts, 0, result, 0, testPolicyCmpts.length);
        System.arraycopy(testRuleObjects, 0, result, testPolicyCmpts.length, testRuleObjects.length);
        System.arraycopy(testValues, 0, result, (testRuleObjects.length + testPolicyCmpts.length), testValues.length);
        
        return result;
    }
    
    /*
     * Adds all test policy cmpts and its child test policy cmpts to the given list.
     */
    private void addChildTestPolicyCmpt(List allPolicyCmpts, ITestPolicyCmpt cmpt) throws CoreException {
        allPolicyCmpts.add(cmpt);
        ITestPolicyCmptRelation[] testRelations = cmpt.getTestPolicyCmptRelations();
        for (int i = 0; i < testRelations.length; i++) {
            // get the dependencies for the childs of the given test policy cmpt
            if (testRelations[i].isComposition()){
                addChildTestPolicyCmpt(allPolicyCmpts, testRelations[i].findTarget());
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getReferencedProductCmpts() throws CoreException {
        List relatedProductCmpts = new ArrayList();
        ITestPolicyCmpt[] allTestPolicyCmpt = getAllTestPolicyCmpt();
        for (int i = 0; i < allTestPolicyCmpt.length; i++) {
            if (StringUtils.isNotEmpty(allTestPolicyCmpt[i].getProductCmpt())){
                relatedProductCmpts.add(allTestPolicyCmpt[i].getProductCmpt());
            }
        }
        return (String[])relatedProductCmpts.toArray(new String[relatedProductCmpts.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getTestCaseType() {
        return testCaseType;
    }

    /**
     * {@inheritDoc}
     */
    public void setTestCaseType(String testCaseType) {
        String oldTestCaseType = this.testCaseType;
        this.testCaseType = testCaseType;
        valueChanged(oldTestCaseType, testCaseType);
    }

    /**
     * {@inheritDoc}
     */
    public ITestCaseType findTestCaseType(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(testCaseType))
            return null;
        return (ITestCaseType)ipsProject.findIpsObject(IpsObjectType.TEST_CASE_TYPE, testCaseType);
    }

    /**
     * {@inheritDoc}
     */
    public ITestCaseTestCaseTypeDelta computeDeltaToTestCaseType() throws CoreException {
        ITestCaseType testCaseTypeFound = findTestCaseType(getIpsProject());
        if (testCaseTypeFound != null) {
            return new TestCaseTestCaseTypeDelta(this, testCaseTypeFound);
        }
        // type not found, therefore no delta could be computed
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void fixDifferences(ITestCaseTestCaseTypeDelta delta) throws CoreException {
        // TODO Joerg, Methodenlaenge
        // Test case side
        ITestValue[] testValuesWithMissingTestValueParam = delta.getTestValuesWithMissingTestValueParam();
        ITestPolicyCmpt[] testPolicyCmptsWithMissingTypeParam = delta.getTestPolicyCmptsWithMissingTypeParam();
        ITestPolicyCmptRelation[] testPolicyCmptRelationsWithMissingTypeParam = delta.getTestPolicyCmptRelationsWithMissingTypeParam();
        ITestAttributeValue[] testAttributeValuesWithMissingTestAttribute = delta.getTestAttributeValuesWithMissingTestAttribute();
        ITestRule[] testRulesWithMissingTestRuleParam = delta.getTestRulesWithMissingTestValueParam();
        
        // Test case type side
        ITestValueParameter[] testValueParametersWithMissingTestValue = delta.getTestValueParametersWithMissingTestValue();
        ITestPolicyCmptTypeParameter[] testPolicyCmptTypeParametersWithMissingTestPolicyCmpt = delta.getTestPolicyCmptTypeParametersWithMissingTestPolicyCmpt();
        ITestAttribute[] testAttributesWithMissingTestAttributeValue = delta.getTestAttributesWithMissingTestAttributeValue();

        /* Test case side */
        
        // delete test values
        for (int i = 0; i < testValuesWithMissingTestValueParam.length; i++) {
            testValuesWithMissingTestValueParam[i].delete();
        }
        // delta test rules
        for (int i = 0; i < testRulesWithMissingTestRuleParam.length; i++) {
            testRulesWithMissingTestRuleParam[i].delete();
        }
        // delete root and child test policy cmpts
        for (int i = 0; i < testPolicyCmptsWithMissingTypeParam.length; i++) {
            testPolicyCmptsWithMissingTypeParam[i].delete();
        }
        // delete test policy cmpt relations
        for (int i = 0; i < testPolicyCmptRelationsWithMissingTypeParam.length; i++) {
            testPolicyCmptRelationsWithMissingTypeParam[i].delete();
        }
        // delete test attribute values
        for (int i = 0; i < testAttributeValuesWithMissingTestAttribute.length; i++) {
            testAttributeValuesWithMissingTestAttribute[i].delete();
        }
        
        /* Test case type side */
        
        // add missing test value parameters
        for (int i = 0; i < testValueParametersWithMissingTestValue.length; i++) {
            ITestValue testValue = newTestValue();
            testValue.setTestValueParameter(testValueParametersWithMissingTestValue[i].getName());
            // set default value to default
            ValueDatatype valueDatatype = ((TestValueParameter)testValueParametersWithMissingTestValue[i]).findValueDatatype(getIpsProject());
            if (valueDatatype != null){
                testValue.setValue(valueDatatype.getDefaultValue());
            }            
        }
        
        // add missing test policy cmpt type parameters
        for (int i = 0; i < testPolicyCmptTypeParametersWithMissingTestPolicyCmpt.length; i++) {
            if (testPolicyCmptTypeParametersWithMissingTestPolicyCmpt[i].isRoot()) {
                String name = testPolicyCmptTypeParametersWithMissingTestPolicyCmpt[i].getName();
                ITestPolicyCmpt testPolicyCpmt = newTestPolicyCmpt();
                testPolicyCpmt.setTestPolicyCmptTypeParameter(name);
                testPolicyCpmt.setName(name);
                // add test attributes values
                ITestAttribute[] attrs = testPolicyCmptTypeParametersWithMissingTestPolicyCmpt[i].getTestAttributes();
                for (int j = 0; j < attrs.length; j++) {
                    ITestAttributeValue testAttributeValue = testPolicyCpmt.newTestAttributeValue();
                    testAttributeValue.setTestAttribute(attrs[j].getName());
                    // set default for the added test attribute value
                    testAttributeValue.updateDefaultTestAttributeValue();
                }
            } else {
                throw new RuntimeException("Merge of child test test policy cmpts is not supported!"); //$NON-NLS-1$
            }
        }
        
        // add missing test attributes
        for (int i = 0; i < testAttributesWithMissingTestAttributeValue.length; i++) {
            ITestPolicyCmpt[] testPolicyCmpts = delta.getTestPolicyCmptForMissingTestAttribute(testAttributesWithMissingTestAttributeValue[i]);
            for (int j = 0; j < testPolicyCmpts.length; j++) {
                ITestAttributeValue testAttributeValue = testPolicyCmpts[j].newTestAttributeValue();
                testAttributeValue.setTestAttribute(testAttributesWithMissingTestAttributeValue[i].getName());
                // set default for the new added test attribute value only
                IProductCmptGeneration generation = ((TestPolicyCmpt)testPolicyCmpts[j]).findProductCmpsCurrentGeneration();
                ((TestAttributeValue)testAttributeValue).setDefaultTestAttributeValueInternal(generation);
            }
        }
        
        if (delta.isDifferentTestParameterOrder()){
            // fix the order of the root test objects
            sortTestObjects();
            
            // fix childs
            //  order relations in order of the test parameter
            ITestPolicyCmpt[] cmpts = delta.getTestPolicyCmptWithDifferentSortOrder();
            for (int i = 0; i < cmpts.length; i++) {
                ((TestPolicyCmpt)cmpts[i]).fixDifferentChildSortOrder();
            }

            // order test attributes
            cmpts = delta.getTestPolicyCmptWithDifferentSortOrderTestAttr();
            for (int i = 0; i < cmpts.length; i++) {
                ((TestPolicyCmpt)cmpts[i]).fixDifferentTestAttrValueSortOrder();
            }
            
            objectHasChanged();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void sortTestObjects() throws CoreException {
        List orderedTestObject = getCorrectSortOrderOfRootObjects(getIpsProject());
        if (orderedTestObject != null){
            testObjects = orderedTestObject;
            objectHasChanged();
        }
    }

    /*
     * Returns all root objects in the correct sort order compared to the test case type parameters.
     * If the test parameter doesn't exist order the test object to the end of the test object list.
     */
    private List getCorrectSortOrderOfRootObjects(IIpsProject ipsProject) throws CoreException {
        List newTestObjectOrder = new ArrayList(testObjects.size());
        HashMap oldTestObject = new HashMap(testObjects.size());
        for (Iterator iter = testObjects.iterator(); iter.hasNext();) {
            ITestObject testObject = (ITestObject)iter.next();
            String testParameterName = ""; //$NON-NLS-1$
            ITestParameter testParameter = null;
            if (testObject instanceof ITestPolicyCmpt){
                testParameterName = ((ITestPolicyCmpt)testObject).getTestPolicyCmptTypeParameter();
                testParameter = ((ITestPolicyCmpt)testObject).findTestPolicyCmptTypeParameter(ipsProject);
            } else if (testObject instanceof ITestValue){
                testParameterName = ((ITestValue)testObject).getTestValueParameter();
                testParameter = ((ITestValue)testObject).findTestValueParameter(ipsProject);
            } else if (testObject instanceof ITestRule){
                testParameterName = ((ITestRule)testObject).getTestRuleParameter();
                testParameter = ((ITestRule)testObject).findTestRuleParameter(ipsProject);
            } else {
                throw new RuntimeException("Unsupported test object type: " + testObject.getClass()); //$NON-NLS-1$
            }
            if (testParameter == null)
                throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCase_Error_TestParameterNotFound, testParameterName)));
            
            List oldObjectsToTestParam = (List) oldTestObject.get(testParameter);
            if (oldObjectsToTestParam == null){
                oldObjectsToTestParam = new ArrayList(1);
                oldObjectsToTestParam.add(testObject);
            } else {
                oldObjectsToTestParam.add(testObject);
            }
            oldTestObject.put(testParameter, oldObjectsToTestParam);
        }
        
        ITestCaseType testCaseType = findTestCaseType(ipsProject);
        ITestParameter[] testParameters = testCaseType.getTestParameters();
        for (int i = 0; i < testParameters.length; i++) {
            List oldObjectsToTestParam = (List) oldTestObject.get(testParameters[i]);
            if (oldObjectsToTestParam == null && ! (testParameters[i] instanceof ITestRuleParameter)){
                // throw runtime exception for non test rule parameter only, because the rule params could
                // be added by the test case
                throw new RuntimeException("Test objects not found for test parameter: " + testParameters[i].getName() + "!"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            // add all elements without a test parameter to the end
            if (oldObjectsToTestParam != null){
                newTestObjectOrder.addAll(oldObjectsToTestParam);
            }
        }
        return newTestObjectOrder;
    }

    /**
     * {@inheritDoc}
     */
    public ITestValue newTestValue() {
        ITestValue v = newTestValueInternal(getNextPartId());
        objectHasChanged();
        return v;
    }

    /**
     * {@inheritDoc}
     */
    public ITestRule newTestRule() {
        ITestRule v = newTestRuleInternal(getNextPartId());
        objectHasChanged();
        return v;
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt newTestPolicyCmpt() {
        ITestPolicyCmpt p = newTestPolicyCmptInternal(getNextPartId());
        objectHasChanged();
        return p;
    }

    //
    // Getters for test objects
    //

    /**
     * {@inheritDoc}
     */
    public ITestObject[] getTestObjects() {
        List foundTestObjects = getTestObjects(null, null, null);
        if (foundTestObjects.size() == 0)
            return new ITestObject[0];

        return (ITestObject[]) foundTestObjects.toArray(new ITestObject[0]);
    }    
    
    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt[] getTestPolicyCmpts() {
        return (ITestPolicyCmpt[])getTestObjects(null, TestPolicyCmpt.class, null).toArray(
                new ITestPolicyCmpt[0]);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestValue[] getTestValues() {
        return (ITestValue[])getTestObjects(null, TestValue.class, null).toArray(new ITestValue[0]);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestRule[] getTestRule(String testRuleParameter) {
        List testRules = getTestObjects(null, TestRule.class, null); 
        List result = new ArrayList();
        for (Iterator iter = testRules.iterator(); iter.hasNext();) {
            ITestRule element = (ITestRule)iter.next();
            if (element.getTestParameterName().equals(testRuleParameter)){
                result.add(element);
            }
        }
        return (ITestRule[])result.toArray(new ITestRule[0]);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestRule[] getTestRuleObjects() {
        return (ITestRule[])getTestObjects(null, TestRule.class, null).toArray(new ITestRule[0]);
    }    
    
    //
    // Getters for input objects
    //

    /**
     * {@inheritDoc}
     */
    public ITestObject[] getInputTestObjects() {
        return (ITestObject[])getTestObjects(TestParameterType.INPUT, null, null).toArray(new ITestObject[0]);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestValue[] getInputTestValues() {
        return (ITestValue[])getTestObjects(TestParameterType.INPUT, TestValue.class, null).toArray(new ITestValue[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt[] getInputTestPolicyCmpts() {
        return (ITestPolicyCmpt[])getTestObjects(TestParameterType.INPUT, TestPolicyCmpt.class, null).toArray(
                new ITestPolicyCmpt[0]);
    }

    //
    // Getters for expected result objects
    //    

    /**
     * {@inheritDoc}
     */
    public ITestObject[] getExpectedResultTestObjects() {
        return (ITestObject[])getTestObjects(TestParameterType.EXPECTED_RESULT, null, null).toArray(
                new ITestObject[0]);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestValue[] getExpectedResultTestValues() {
        return (ITestValue[])getTestObjects(TestParameterType.EXPECTED_RESULT, TestValue.class, null).toArray(
                new ITestValue[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestRule[] getExpectedResultTestRules() {
        return (ITestRule[])getTestObjects(TestParameterType.EXPECTED_RESULT, TestRule.class, null).toArray(
                new ITestRule[0]);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt[] getExpectedResultTestPolicyCmpts() {
        return (ITestPolicyCmpt[])getTestObjects(TestParameterType.EXPECTED_RESULT, TestPolicyCmpt.class, null)
                .toArray(new ITestPolicyCmpt[0]);
    }

    /**
     * {@inheritDoc}
     */
    public void removeTestObject(ITestObject testObject) throws CoreException {
        if (testObject.isRoot()){
            testObjects.remove(testObject);
        } else {
            remove(testObject);
        }
        objectHasChanged();
    }

    //
    // Finder methods to search inside the complete test case structure.
    //

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmpt testPolicyCmpt, IIpsProject ipsProject)
            throws CoreException {
        
        return findTestPolicyCmptTypeParameter(testPolicyCmpt, null, ipsProject);
    }

     /**
     * {@inheritDoc}
     */
     public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(ITestPolicyCmptRelation relation, IIpsProject ipsProject) throws CoreException {
         return findTestPolicyCmptTypeParameter(null, relation, ipsProject);
     }

    /**
     * Returns the corresponing test policy componnet type parameter of the given test policy
     * component or the given relation. Either the test policy component or the relation must be
     * given, but not both together. Returns <code>null</code> if the parameter not found.
     * 
     * @param testPolicyCmptBase The test policy component which policy component type parameter
     *            will be returned.
     * @param relation The test policy component relation which test relation will be returned
     * 
     * @throws CoreException if an error occurs while searching for the object.
     */
    private ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(
            ITestPolicyCmpt testPolicyCmptBase,
            ITestPolicyCmptRelation relation,
            IIpsProject ipsProject) throws CoreException {
        
        ArgumentCheck.isTrue(testPolicyCmptBase != null || relation != null);
        ArgumentCheck.isTrue(!(testPolicyCmptBase != null && relation != null));

        ITestCaseType testCaseTypeFound = findTestCaseType(ipsProject);
        if (testCaseTypeFound == null) {
            return null;
        }

        // Create a helper path obejct to search the given string path
        TestCaseHierarchyPath hierarchyPath = null;
        if (testPolicyCmptBase != null) {
            hierarchyPath = new TestCaseHierarchyPath(testPolicyCmptBase, false);
        } else if (relation != null) {
            hierarchyPath = new TestCaseHierarchyPath(relation, false);
        } else {
            throw new CoreException(new IpsStatus(Messages.TestCase_Error_NoRelationOrPolicyCmptGiven));
        }

        // find the root test policy component parameter type
        String testPolicyCmptTypeName = hierarchyPath.next();
        ITestParameter testParam = testCaseTypeFound.getTestParameterByName(testPolicyCmptTypeName);
        if (testParam == null) {
            return null;
        }

        // check the correct instance of the found object 
        if (! (testParam instanceof ITestPolicyCmptTypeParameter)) {
            throw new CoreException(
                    new IpsStatus(NLS.bind(Messages.TestCase_Error_WrongInstanceParam, testPolicyCmptTypeName, testParam.getClass().getName())));
        }
        if (!testPolicyCmptTypeName.equals(testParam.getName())) {
            // incosistence between test case and test case type
            return null;
        }

        // now search the given path until the test policy component is found
        ITestPolicyCmptTypeParameter policyCmptTypeParam = (ITestPolicyCmptTypeParameter) testParam;
        while (hierarchyPath.hasNext()) {
            testPolicyCmptTypeName = hierarchyPath.next();
            policyCmptTypeParam = policyCmptTypeParam.getTestPolicyCmptTypeParamChild(testPolicyCmptTypeName);
            if (policyCmptTypeParam == null || !testPolicyCmptTypeName.equals(policyCmptTypeParam.getName())) {
                // incosistence between test case and test case type
                return null;
            }
        }

        return policyCmptTypeParam;
    }

     /**
      * Removes the given test parameter object from the parameter list
      */
    private void remove(ITestObject testObject) throws CoreException {
        if (testObject instanceof ITestPolicyCmpt) {
            ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt) testObject;
            if (testPolicyCmpt.isRoot()) {
                removeTestObject(testObject);
            } else {
                TestCaseHierarchyPath hierarchyPath = new TestCaseHierarchyPath(testPolicyCmpt);
                testPolicyCmpt = findTestPolicyCmpt(hierarchyPath.toString());
                if (testPolicyCmpt == null) {
                    throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCase_Error_TestPolicyCmptNotFound,
                            hierarchyPath.toString())));
                }

                ITestPolicyCmptRelation relation = (ITestPolicyCmptRelation) testPolicyCmpt.getParent();
                if (relation != null) {
                    ((ITestPolicyCmpt)relation.getParent()).removeRelation(relation);
                }
            }
        } else {
            removeTestObject(testObject);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt findTestPolicyCmpt(String testPolicyCmptPath) throws CoreException {
         TestCaseHierarchyPath path = new TestCaseHierarchyPath(testPolicyCmptPath);
         ITestPolicyCmpt pc = null;
         String currElem = path.next();

         List testPoliyCmpts = getTestObjects(null, TestPolicyCmpt.class, currElem);
         if (testPoliyCmpts.size() == 1){
             assertInstanceOfTestPolicyCmpt(currElem, (ITestObject) testPoliyCmpts.get(0));
             pc = searchChildTestPolicyCmpt((ITestPolicyCmpt) testPoliyCmpts.get(0), path);
         } else if (testPoliyCmpts.size() == 0) {
             return null;
         } else {
             throw new CoreException(new IpsStatus(NLS.bind(
                     Messages.TestCase_Error_MoreThanOneObject, currElem)));
         }
         return pc;
    }

    /*
     * Assert the correct instance of ITestPolicyCmpt for the given testObject.
     * @throws CoreException if the check fails.
     */
    private void assertInstanceOfTestPolicyCmpt(String currElem, ITestObject testObject) throws CoreException {
        if (! ( testObject instanceof ITestPolicyCmpt))
             throw new CoreException(
                     new IpsStatus(NLS.bind(Messages.TestCase_Error_WrongInstanceTestPolicyCmpt, currElem, testObject.getClass().getName())));
    }
    
    /*
     * Search the test policy component by the given path.
     */
     private ITestPolicyCmpt searchChildTestPolicyCmpt(ITestPolicyCmpt pc, TestCaseHierarchyPath path)
            throws CoreException {
        String searchedPath = path.toString();
        while (pc != null && path.hasNext()) {
            boolean found = false;
            String currElem = path.next();

            ITestPolicyCmptRelation[] prs;
            prs = pc.getTestPolicyCmptRelations(currElem);

            currElem = path.next();
            pc = null;
            for (int i = 0; i < prs.length; i++) {
                ITestPolicyCmptRelation relation = prs[i];
                ITestPolicyCmpt pcTarget = relation.findTarget();
                if (pcTarget == null)
                    return null;

                if (currElem.equals(pcTarget.getName())) {
                     if (found){
                         // exception more than one element found with the given path
                         throw new CoreException(new IpsStatus(NLS.bind(
                                 Messages.TestCase_Error_MoreThanOneObject, searchedPath)));
                     }
                     found = true;                        
                     pc = pcTarget;
                }
            }
        }
        return pc;
    }

     /**
      * {@inheritDoc}
      */
    public String generateUniqueNameForTestPolicyCmpt(ITestPolicyCmpt newTestPolicyCmpt, String name) {
        String uniqueLabel = name;

        // eval the unique idx of new component
        int idx = 1;
        String newUniqueLabel = uniqueLabel;
        if (newTestPolicyCmpt.isRoot()) {
            ITestPolicyCmpt[] testPolicyCmpts = getTestPolicyCmpts();
            for (int i = 0; i < testPolicyCmpts.length; i++) {
                ITestPolicyCmpt cmpt = testPolicyCmpts[i];
                if (newUniqueLabel.equals(cmpt.getName()) && ! cmpt.equals(newTestPolicyCmpt) ) {
                    idx++;
                    newUniqueLabel = uniqueLabel + " (" + idx + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                    i = -1;
                }
            }
        } else {
            ITestPolicyCmpt parent = newTestPolicyCmpt.getParentPolicyCmpt();
            ITestPolicyCmptRelation[] relations = parent.getTestPolicyCmptRelations();
            ArrayList names = new ArrayList();
            for (int i = 0; i < relations.length; i++) {
                ITestPolicyCmptRelation relation = relations[i];
                if (relation.isComposition()) {
                    try {
                        ITestPolicyCmpt child = relation.findTarget();
                        if (! child.equals(newTestPolicyCmpt)){
                            names.add(child.getName());
                        }
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            while (names.contains(newUniqueLabel)) {
                idx++;
                newUniqueLabel = uniqueLabel + " (" + idx + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return newUniqueLabel;
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule[] getTestRuleCandidates(IIpsProject ipsProject) throws CoreException {
        Set result = new HashSet();
        ITestCaseType testCaseTypeFound = findTestCaseType(ipsProject);
        if (testCaseTypeFound != null){
            result.addAll(Arrays.asList(testCaseTypeFound.getTestRuleCandidates()));
            result.addAll(getTestCaseTestRuleCandidates(ipsProject));
        }
        return (IValidationRule[]) result.toArray(new IValidationRule[result.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    public IValidationRule findValidationRule(String validationRuleName, IIpsProject ipsProject) throws CoreException {
        IValidationRule[] validationRules = getTestRuleCandidates(ipsProject);
        for (int i = 0; i < validationRules.length; i++) {
            if (validationRules[i].getName().equals(validationRuleName)){
                return validationRules[i];
            }
        }
        return null;
    }

    /*
     * Returns all validation rules of the policy cmpt types of the product cmpt inside this test
     * case.
     */
    private Collection getTestCaseTestRuleCandidates(IIpsProject ipsProject) throws CoreException {
        List result = new ArrayList();
        getValidationRules(getTestPolicyCmpts(), result, ipsProject);
        return result;
    }

    /*
     * Adds all validation rules - of policy cmpts related by the given test policy cmpts - to the given list
     */
    private void getValidationRules(ITestPolicyCmpt[] testPolicyCmpts, List validationRules, IIpsProject ipsProject) throws CoreException {
        for (int i = 0; i < testPolicyCmpts.length; i++) {
            getValidationRules(testPolicyCmpts[i], validationRules, ipsProject);
        }
    }

    /*
     * Add all validaton rules of the corresponding policy cmpt and childs
     */
    private void getValidationRules(
            ITestPolicyCmpt testPolicyCmpt, 
            List validationRules,
            IIpsProject ipsProject) throws CoreException {
        
        // add rules of childs, ignore if the corresponding objects are not found (validation errors)
        ITestPolicyCmptRelation[] rs = testPolicyCmpt.getTestPolicyCmptRelations();
        for (int i = 0; i < rs.length; i++) {
            ITestPolicyCmpt tpc = rs[i].findTarget();
            if (tpc == null){
                continue;
            }
            getValidationRules(tpc, validationRules, ipsProject);
        }
        ITestPolicyCmptTypeParameter typeParam = testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject);
        if (typeParam == null){
            return;
        }
        IPolicyCmptType pct = typeParam.findPolicyCmptType();
        if (pct == null){
            return;
        }
        validationRules.addAll(Arrays.asList(pct.getRules()));
        IProductCmpt pc = testPolicyCmpt.findProductCmpt();
        if (pc == null){
            return;
        }
        IPolicyCmptType pctOfPc = pc.findPolicyCmptType(ipsProject);
        if (pctOfPc == null){
            return;
        }
        if (!pctOfPc.equals(pct)){
            // add all rules inside the supertype hierarchy
            ITypeHierarchy supertypeHierarchy = pctOfPc.getSupertypeHierarchy();
            validationRules.addAll(Arrays.asList(supertypeHierarchy.getAllRules(pctOfPc)));
        }
    }
    
    /*
     * Creates a new test policy component without updating the src file.
     */
    private ITestPolicyCmpt newTestPolicyCmptInternal(int id) {
        ITestPolicyCmpt p = new TestPolicyCmpt(this, id);
        testObjects.add(p);
        return p;
    }

    /*
     * Creates a new test value without updating the src file.
     */
    private ITestValue newTestValueInternal(int id) {
        ITestValue v = new TestValue(this, id);
        testObjects.add(v);
        return v;
    }

    /*
     * Creates a new test rule without updating the src file.
     */
    private ITestRule newTestRuleInternal(int id) {
        ITestRule v = new TestRule(this, id);
        testObjects.add(v);
        return v;
    }
    
    /*
     * Returns the test objects which matches the given type, is instance of the given class and
     * matches the given name. The particular object aspect will only check if the particular field
     * is not <code>null</code>. For instance if all parameter are <code>null</code> then all
     * parameters are returned.
     */
    private List getTestObjects(TestParameterType type, Class parameterClass, String name) {
        List result = new ArrayList(testObjects.size());
        for (Iterator iter = testObjects.iterator(); iter.hasNext();) {
            TestObject testObject = (TestObject)iter.next();
            boolean addParameter = true;
            
            if (type != null && ! isTypeOrDefault(testObject.getTestParameterName(), type, TestObject.DEFAULT_TYPE)){
                addParameter = false;
                continue;
            }
            
            if (parameterClass != null && !testObject.getClass().equals(parameterClass)) {
                addParameter = false;
                continue;
            }
            if (name != null && !name.equals(testObject.getName())) {
                addParameter = false;
                continue;
            }
            if (addParameter)
                result.add(testObject);
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the given type is the type of the corresponding test
     * parameter. If the test parameter couldn't determined return <code>true</code> if the given
     * type is the default type otherwise <code>false</code>.<br>
     * Return <code>false</code> if an error occurs.<br>
     * (Packageprivate helper method.)
     */
    boolean isTypeOrDefault(
            String testParameterName, 
            TestParameterType type, 
            TestParameterType defaultType) {
            
        
        try {
            ITestCaseType testCaseTypeFound = findTestCaseType(getIpsProject());
            if (testCaseTypeFound == null)
                return type.equals(defaultType);

            ITestParameter testParameter = testCaseTypeFound.getTestParameterByName(testParameterName);
            if (testParameter == null){
                return type.equals(defaultType);
            }
            return isTypeOrDefault(testParameter, type, defaultType);
        } catch (CoreException e) {
            // ignore exceptions
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the given type is the type of the corresponding test
     * parameter.<br>
     * Return <code>false</code> if an error occurs.<br>
     * (Packageprivate helper method.)
     */
    boolean isTypeOrDefault(ITestParameter testParameter, TestParameterType type, TestParameterType defaultType) {
        // TODO Joerg: aufraeumen, Verwendung von TestParameterType.isTypeMatching
        try {
            // compare the paramters type and return if the type matches the given type
            if (testParameter.isInputParameter() && type.equals(TestParameterType.INPUT)) {
                return true;
            }
            if (testParameter.isExpextedResultParameter() && type.equals(TestParameterType.EXPECTED_RESULT)) {
                return true;
            }
            if (testParameter.isCombinedParameter() && type.equals(TestParameterType.COMBINED)) {
                return true;
            }
        } catch (Exception e) {
            // ignore exceptions
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList messageList, IIpsProject ipsProject) throws CoreException {
        super.validateThis(messageList, ipsProject);
        ITestCaseType testCaseTypeFound = findTestCaseType(ipsProject);
        if (testCaseTypeFound == null) {
            String text = NLS.bind(Messages.TestCase_ValidateError_TestCaseTypeNotFound, testCaseType);
            Message msg = new Message(MSGCODE_TEST_CASE_TYPE_NOT_FOUND, text, Message.ERROR, this,
                    ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
            messageList.add(msg);
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsDifferenceToModel() throws CoreException {
        ITestCaseTestCaseTypeDelta delta = computeDeltaToTestCaseType();
        if (delta != null && !delta.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void fixAllDifferencesToModel() throws CoreException {
        fixDifferences(computeDeltaToTestCaseType());
    }
}
