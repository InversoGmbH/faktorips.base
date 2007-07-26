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

package org.faktorips.devtools.core.internal.model.testcase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestRuleParameter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test rule class. Defines a validation rule for a specific test case.
 * @author Joerg Ortmann
 */
public class TestRule extends TestObject implements ITestRule {

    final static String TAG_NAME = "RuleObject"; //$NON-NLS-1$
    
    private String testRuleParameter = ""; //$NON-NLS-1$
    
    private String validationRule = ""; //$NON-NLS-1$
    
    private TestRuleViolationType violationType = TestRuleViolationType.VIOLATED;
    
    public TestRule(IIpsObject parent, int id) {
        super(parent, id);
    }
    
    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        testRuleParameter = element.getAttribute(PROPERTY_TEST_RULE_PARAMETER);
        validationRule = element.getAttribute(PROPERTY_VALIDATIONRULE);
        violationType = TestRuleViolationType.getTestRuleViolationType(element.getAttribute(PROPERTY_VIOLATED));
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TEST_RULE_PARAMETER, testRuleParameter);
        element.setAttribute(PROPERTY_VALIDATIONRULE, validationRule);
        element.setAttribute(PROPERTY_VIOLATED, violationType.getId());
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("ValidationRuleDef.gif"); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public String getTestParameterName(){
        return testRuleParameter;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRoot() {
        // no childs are supported, the test value parameter is always a root element        
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestObject getRoot() {
        // no childs are supported, the test rule is always a root element
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getTestRuleParameter() {
        return testRuleParameter;
    }

    /**
     * {@inheritDoc}
     */
    public void setTestRuleParameter(String testRuleParameter) {
        String oldTestRuleParameter = this.testRuleParameter;
        this.testRuleParameter = testRuleParameter;
        valueChanged(oldTestRuleParameter, testRuleParameter);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITestRuleParameter findTestRuleParameter() throws CoreException {
        if (StringUtils.isEmpty(testRuleParameter)) {
            return null;
        }

        ITestCaseType testCaseType = ((ITestCase)getParent()).findTestCaseType();
        if (testCaseType == null){
            return null;
        }
        ITestParameter param = testCaseType.getTestParameterByName(testRuleParameter);
        if (param instanceof ITestRuleParameter){
            return (ITestRuleParameter) param;
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getValidationRule() {
        return validationRule;
    }

    /**
     * {@inheritDoc}
     */
    public void setValidationRule(String validationRule) {
        String oldValidationRule = this.validationRule;
        this.validationRule = validationRule;
        valueChanged(oldValidationRule, validationRule);
    }
    
    /**
     * {@inheritDoc}
     */
    public IValidationRule findValidationRule() throws CoreException {
        ITestCase testCase = (ITestCase) getParent();
       return testCase.findValidationRule(validationRule);
    }

    /**
     * {@inheritDoc}
     */
    public TestRuleViolationType getViolationType() {
        return violationType;
    }

    /**
     * {@inheritDoc}
     */
    public void setViolationType(TestRuleViolationType violationType) {
        TestRuleViolationType oldViolationType = this.violationType;
        this.violationType = violationType;
        valueChanged(oldViolationType, violationType);
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);

        // check if the validation rule is inside the test case type structure (one rule of the test
        // policy cmpt type parameter)
        if (findValidationRule() == null) {
            String text = NLS.bind(Messages.TestRule_ValidationError_ValidationRuleNotAvailable, validationRule);
            Message msg = new Message(MSGCODE_VALIDATION_RULE_NOT_EXISTS, text, Message.ERROR, this, PROPERTY_VALIDATIONRULE);
            list.add(msg);
        }
        
        // check if the validation rule is unique in this test case
        ITestCase testCase = (ITestCase)getParent();
        ITestRule[] rules = testCase.getTestRuleObjects();
        for (int i = 0; i < rules.length; i++) {
            if (rules[i] != this && rules[i].getValidationRule().equals(validationRule)){
                String text = NLS.bind(Messages.TestRule_ValidationError_DuplicateValidationRule, validationRule);
                Message msg = new Message(MSGCODE_DUPLICATE_VALIDATION_RULE, text, Message.ERROR, this, PROPERTY_VALIDATIONRULE);
                list.add(msg);
                break;
            }
        }
        
        // check if the test rule parameter exists
        ITestParameter param = findTestRuleParameter();
        if (param == null){
            String text = NLS.bind(Messages.TestRule_ValidationError_TestRuleParameterNotFound, getTestRuleParameter());
            Message msg = new Message(MSGCODE_TEST_RULE_PARAM_NOT_FOUND, text, Message.ERROR, this, PROPERTY_TEST_RULE_PARAMETER);
            list.add(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
    }

    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return getTestRuleParameter() + "/" + getValidationRule();
    }
}
