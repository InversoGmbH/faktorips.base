/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.util.ArgumentCheck;

/**
 * Content provider for the test case domain.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseContentProvider implements ITreeContentProvider {
	private static Object[] EMPTY_ARRAY = new Object[0];
	
	/** Defines the type of the content which will be currently provided: 
	 *    the input objects, the expected result objects, or both could be provided */
	public static final int COMBINED = 0;
	public static final int INPUT = 1;
	public static final int EXPECTED_RESULT = 2;
	private int contentType = COMBINED;
	
	/** Sort functions */
	public static TestPolicyCmptSorter TESTPOLICYCMPT_SORTER = new TestPolicyCmptSorter();
	public static TestValueSorter TESTVALUE_SORTER = new TestValueSorter();
	
	// Contains the test case for which the content will be provided
	private ITestCase testCase;
	
	// Indicates if the structure should be displayed without association layer
	private boolean withoutAssociations = false;
	
    // Cache containing the dummy objects, to display the association and rules.
    // This kind of objects are only used in the ui to adapt the model objects to the correct
    // content in the tree view
    private HashMap dummyObjects = new HashMap();

    // ips project used to search
    private IIpsProject ipsProject;
    
	public TestCaseContentProvider(int contentType, ITestCase testCase){
		ArgumentCheck.notNull(testCase);
		this.contentType = contentType;
		this.testCase = testCase;
        this.ipsProject = testCase.getIpsProject();
	}
	
	/**
	 * Returns the test case.
	 */
	public ITestCase getTestCase(){
		return testCase;
	}
	
    /**
     * Set the test case to be displayed.
     */
    public void setTestCase(ITestCase testCase){
        this.testCase = testCase;
    }
    
	/**
	 * Returns <code>true</code> if the content will be provided without the association layer.
	 * If the complete structure will be displayed (with associations) then <code>false</code> will be returned.
	 */
	public boolean isWithoutAssociations() {
		return withoutAssociations;
	}

	/**
	 * Set if the association layer will be shown <code>false</code> 
	 * or if the association should be hidden <code>true</code>.
	 */
	public void setWithoutAssociations(boolean withoutAssociations) {
		this.withoutAssociations = withoutAssociations;
	}
	
	/**
	 * Returns the int value for the corresponding type, input or expected result.
	 */
	public int getContentType() {
		return contentType;
	}

	/**
	 * Returns the corresponding test policy component objects.<br>
	 * Input, expected result or both objects.<br>
	 * Rerurns <code>null</code> if this content provider has an unknown type.
	 */
	public ITestPolicyCmpt[] getTestPolicyCmpts(){
		if (isInput()){
			return testCase.getInputTestPolicyCmpts();
		}else if (isExpectedResult()){
			return testCase.getExpectedResultTestPolicyCmpts();
		}else if (isCombined()){
		    return testCase.getTestPolicyCmpts();
        }
		return null;
	}
	
	/**
	 * Returns the corresponding test value objects.<br>
	 * Input or expected result objects.<br>
	 * Rerurns <code>null</code> if this content provider has an unknown type.
	 */
	public ITestValue[] getTestValues(){
		if (isCombined()){
            return testCase.getTestValues();
		}else if (isExpectedResult()){
			return testCase.getExpectedResultTestValues();
		}else if (isInput()){
		    return testCase.getInputTestValues();
        }
		return null;
	}

    /**
     * Returns the all test objects.<br>
     * Input or expected result objects.<br>
     * Rerurns <code>null</code> if this content provider has no test objects.
     */
    public ITestObject[] getTestObjects(){
        if (isCombined()){
            return testCase.getTestObjects();
        }else if (isExpectedResult()){
            return testCase.getExpectedResultTestObjects();
        }else if (isInput()){
            return testCase.getInputTestObjects();
        }
        return null;
    }
	
	/**
	 * Returns <code>true</code> if this content provider provides the input objetcs of the test case.
	 */
	public boolean isInput(){
		return contentType == INPUT || contentType == COMBINED;
	}
	
	/**
	 * Returns <code>true</code> if this content provider provides the expected result objects of the test case.
	 */	
	public boolean isExpectedResult(){
		return contentType == EXPECTED_RESULT || contentType == COMBINED;
	}

    /**
	 * Returns <code>true</code> if this content provider provides the expected result and input objects of the test case.
	 */	
	public boolean isCombined(){
	    return contentType == COMBINED;
	}
	
    /**
     * Sets the content type of the content provider.
     */
    public void setContentType(int contentType){
        this.contentType = contentType;
    }
    
	/**
	 * {@inheritDoc}
	 */
	public Object[] getChildren(Object parentElement) {
	    if(parentElement instanceof ITestPolicyCmpt) {
	        return getChildsForTestPolicyCmpt((ITestPolicyCmpt)parentElement);
	    }else if(parentElement instanceof ITestPolicyCmptLink){
	    	return getChildsForTestPolicyCmptAssociation((ITestPolicyCmptLink) parentElement);
	    }else if(parentElement instanceof TestCaseTypeAssociation){
	    	return getChildsForTestCaseTypeAssociation((TestCaseTypeAssociation) parentElement);
	    }else if (parentElement instanceof TestCaseTypeRule){
            return testCase.getTestRule(((TestCaseTypeRule)parentElement).getName());
        }
	    return EMPTY_ARRAY;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object element) {
	    if(element instanceof ITestPolicyCmpt) {
	        return ((ITestPolicyCmpt)element).getParent();
	    }else if(element instanceof ITestPolicyCmptLink){
	    	return ((ITestPolicyCmptLink) element).getParent();
	    }else if(element instanceof TestCaseTypeAssociation){
	    	return ((TestCaseTypeAssociation) element).getParentTestPolicyCmpt();
	    }
	    // only the objects above have parents, in other case no parent necessary
	    return null;
	}

	/**
	 * {@inheritDoc}
	 */	
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		if (children==null) {
			return false;
		}
		return children.length > 0;
	}

    /**
     * Returns the content of the test case this provider belongs to.
     */
    public Object[] getElements() {
        return getElements(testCase);
    }

    /**
	 * {@inheritDoc}
	 */
	public Object[] getElements(Object inputElement) {
		// TODO Joerg: Methodenlaenge
        List elements = new ArrayList();
        if (inputElement instanceof ITestCase){
			addElementsFor((ITestCase)inputElement, elements);
		}
        List orderedList = new ArrayList();
        HashMap name2elements = new HashMap();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            ITestObject element = (ITestObject)iter.next();
            List existingElements = (List) name2elements.get(element.getTestParameterName());
            if (existingElements == null){
                existingElements = new ArrayList(1);
            }
            existingElements.add(element);
            name2elements.put(element.getTestParameterName(), existingElements);
        }
        
        // return the ordered list, the ordered list depends on the test case type,
        // because the test rule objects displayed as group, iterate the list of test case type parameter
        // and add the corresponding elements
        
        // furthermore show the test rule objects as childs of a dummy test rule parameter node 
        ITestCaseType testCaseType = null;
        try {
            testCaseType = testCase.findTestCaseType(ipsProject);
        } catch (CoreException e) {
            // ignore exception while retrieving the test rule parameter
        }
        if (testCaseType != null){
            ITestParameter[] params = testCaseType.getTestParameters();
            for (int i = 0; i < params.length; i++) {
                List testObjects = (List) name2elements.get(params[i].getName());
                if (testObjects != null && ! (params[i] instanceof ITestRuleParameter)){
                    orderedList.addAll(testObjects);
                } else if (params[i] instanceof ITestRuleParameter) {
                    if (isCombined() || isExpectedResult()){
                        // test rule objects are not visible if the input filter is chosen
                        orderedList.add(getDummyObject(params[i], null));
                    }
                }
                name2elements.remove(params[i].getName());
            }
            // add all elements which are not in the test parameter on the end
            for (Iterator iter = name2elements.values().iterator(); iter.hasNext();) {
                List elementsWithNoParams = (List)iter.next();
                orderedList.addAll(elementsWithNoParams);
            }
        } else {
            // ignore the sort order of the test case type if the test case type not exists
            orderedList.addAll(elements);
        }
		return (Object[]) orderedList.toArray(new Object[0]);
	}

    private void addElementsFor(ITestCase testCase, List elements) {
        if (isCombined()){
            // return input and expected result objects
            elements.addAll(Arrays.asList(testCase.getTestObjects()));
        }else if(isExpectedResult()){
        	// return expected result objects
            elements.addAll(Arrays.asList(testCase.getExpectedResultTestObjects()));
        }else if(isInput()){
            // return input objects
            elements.addAll(Arrays.asList(testCase.getInputTestObjects()));
        }
    }
	
	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to do
	}

	/**
	 * Returns all test policy component objects which are provided by this provider.
	 */
	public ITestPolicyCmpt[] getPolicyCmpts() {
	    if(isCombined()){
	        return testCase.getTestPolicyCmpts();
        }else if (isInput()){
			return testCase.getInputTestPolicyCmpts();
		}else if (isExpectedResult()){
            return testCase.getExpectedResultTestPolicyCmpts();
        }else{
			return new ITestPolicyCmpt[0];
		}
	}

	/**
	 * Finds the test policy component by the given path.
	 */
	public ITestPolicyCmpt findPolicyCmpt(String path) throws CoreException {
		return testCase.findTestPolicyCmpt(path);
	}
	
	/*
	 * Returns all child of the given test case type association parameter 
	 * (dummy association based on the test case type definition)
	 */
	private Object[] getChildsForTestCaseTypeAssociation(TestCaseTypeAssociation dummyAssociation) {
		// show instances of this test policy component type parameter
		ArrayList childs = new ArrayList();
		
		ITestPolicyCmpt parent = dummyAssociation.getParentTestPolicyCmpt();
		if (parent != null){
			ITestPolicyCmptLink[] associations = parent.getTestPolicyCmptLinks(dummyAssociation.getName());
			for (int i = 0; i < associations.length; i++) {
				ITestPolicyCmptLink association = associations[i];
				if (association.isComposition()){            
					try {
                        if ((isExpectedResult() && association.findTarget().isExpectedResult())
                         || (isInput() && association.findTarget().isInput()))
                            childs.add(association.findTarget());
                    } catch (CoreException e) {
                        // ignore exception, the failure will be displayed by the validation
                    }
				}else{
					childs.add(association);
				}
			}
		}
		return childs.toArray(new IIpsElement[0]);
	}

	/*
	 * Returns all child of the given test case association.
	 */
	private Object[] getChildsForTestPolicyCmptAssociation(ITestPolicyCmptLink testPcAssociation) {
		if (testPcAssociation.isAccoziation()){
			return EMPTY_ARRAY;
		}else{
			ITestPolicyCmpt[] childs = new ITestPolicyCmpt[1];
			try {
				childs[0] = testPcAssociation.findTarget();
			} catch (CoreException e) {
				return EMPTY_ARRAY;
			}
			return childs;
		}
	}

	/*
	 * Returns childs of the test policy component.
	 */
	private Object[] getChildsForTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) {
	    // TODO Joerg: Methodenlaenge
        ITestPolicyCmptLink[] links = testPolicyCmpt.getTestPolicyCmptLinks();
		if (withoutAssociations){
			// show childs without association layer
			List childTestPolicyCmpt = new ArrayList(links.length);
			for (int i = 0; i < links.length; i++) {
				ITestPolicyCmptLink link = links[i];
				if (link.isComposition()){
					ITestPolicyCmpt target=null;
					try {
						target = link.findTarget();
					} catch (CoreException e) {
						IpsPlugin.logAndShowErrorDialog(e);
					}
                    if ((isInput() && target.isInput()) || (isExpectedResult() && target.isExpectedResult()))
                        childTestPolicyCmpt.add(target);
				}else{
                    // assoziation will be added
                    childTestPolicyCmpt.add(links[i]);
				}
			}
			return (IIpsElement[]) childTestPolicyCmpt.toArray(new IIpsElement[0]);
		}else{
			// group childs using the test policy component type
			ArrayList childs = new ArrayList();
			ArrayList childNames = new ArrayList();
			try {
				// get all childs from the test case type definition
				ITestPolicyCmptTypeParameter typeParam = testPolicyCmpt.findTestPolicyCmptTypeParameter(ipsProject);
				if (typeParam != null){
					ITestPolicyCmptTypeParameter[] children = typeParam.getTestPolicyCmptTypeParamChilds();
					for (int i = 0; i < children.length; i++) {
					    ITestPolicyCmptTypeParameter parameter = children[i];
						if (parameterMatchesType(parameter)){
						    childs.add(getDummyObject(parameter, testPolicyCmpt));
                        }
						childNames.add(parameter.getName());
					}
				}
				// add links which are not added by the test case parameter
                //   association with missing test case type parameter
				ITestPolicyCmptLink[] linksInTestCase = testPolicyCmpt.getTestPolicyCmptLinks();
				for (int i = 0; i < linksInTestCase.length; i++) {
					ITestPolicyCmptLink link = linksInTestCase[i];
					if (! childNames.contains(link.getTestPolicyCmptTypeParameter())){
						childs.add(link);
					}
				}
				return childs.toArray(new Object[0]);	
			} catch (CoreException e) {
				// ignore model error, the model consitence between the test case type and the test case
				// will be check when openening the editor, therefore it will be ignored  is here
				return EMPTY_ARRAY;
			}
		}
	}

    /*
     * Returns a chached dummy object. To adapt the model object to the corresponding object which 
     * will be displayed in the ui.
     */
    private Object getDummyObject(ITestParameter parameter, ITestObject testObject) {
        String id = ""; //$NON-NLS-1$
        if (testObject instanceof ITestPolicyCmpt){
            id = parameter.getName() + "#" + new TestCaseHierarchyPath((ITestPolicyCmpt)testObject).toString(); //$NON-NLS-1$
        } else {
            id = parameter.getName() + "#" + (testObject==null?"":testObject.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        Object dummyObject = dummyObjects.get(id);
        if (dummyObject == null) {
            if (testObject instanceof ITestPolicyCmpt) {
                dummyObject = new TestCaseTypeAssociation((ITestPolicyCmptTypeParameter)parameter,
                        (ITestPolicyCmpt)testObject);
                dummyObjects.put(id, dummyObject);
            } else if (parameter instanceof ITestRuleParameter) {
                dummyObject = new TestCaseTypeRule(testCase, (ITestRuleParameter)parameter);
                dummyObjects.put(id, dummyObject);
            }
        }
        return dummyObject;
    }

    /*
     * Returns <code>true</code> if the given paramter matches the current type which the content
     * provider provides.
     */
    private boolean parameterMatchesType(ITestPolicyCmptTypeParameter parameter) {
        return (isExpectedResult() && parameter.isExpextedResultOrCombinedParameter())
                || (isInput() && parameter.isInputOrCombinedParameter());
    }

    /*
	 * Helper class to sort test policy component objecs.
	 */
	private static class TestPolicyCmptSorter implements Comparator{
		public int compare(Object o1, Object o2) {
			ITestPolicyCmpt testPolicyCmpt1 = (ITestPolicyCmpt) o1;
			ITestPolicyCmpt testPolicyCmpt2 = (ITestPolicyCmpt) o2;
			return testPolicyCmpt1.getProductCmpt().compareTo(testPolicyCmpt2.getProductCmpt());
		}
	}
	
	/*
	 * Helper class to sort test value objecs.
	 */
	private static class TestValueSorter implements Comparator{
		public int compare(Object o1, Object o2) {
			ITestValue testValue1 = (ITestValue) o1;
			ITestValue testValue2 = (ITestValue) o2;
			return testValue1.getTestValueParameter().compareTo(testValue2.getTestValueParameter());
		}
	}
}
