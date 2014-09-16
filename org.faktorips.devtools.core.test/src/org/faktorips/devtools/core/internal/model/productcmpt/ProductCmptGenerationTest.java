/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductCmptGenerationTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IIpsProject ipsProject;

    private IPolicyCmptType targetPolicyType;
    private IProductCmptType targetProductType;
    private IProductCmptTypeAssociation association;
    private IProductCmpt target;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);

        targetPolicyType = newPolicyAndProductCmptType(ipsProject, "TargetPolicyType", "TargetProductType");
        targetProductType = targetPolicyType.findProductCmptType(ipsProject);
        target = newProductCmpt(targetProductType, "TargetProduct");

        association = productCmptType.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget(targetProductType.getQualifiedName());
        association.setTargetRoleSingular("testRelationProductSide");
        association.setTargetRolePlural("testRelationsProductSide");
    }

    @Test
    public void testGetAttributeValue() {
        IAttributeValue value1 = generation.newAttributeValue();
        value1.setAttribute("a1");
        IAttributeValue value2 = generation.newAttributeValue();
        value2.setAttribute("a2");

        assertEquals(value1, generation.getAttributeValue("a1"));
        assertEquals(value2, generation.getAttributeValue("a2"));

        assertNull(generation.getAttributeValue("unknwon"));
        assertNull(generation.getAttributeValue(null));
    }

    @Test
    public void testGetFormula() {
        IFormula formula1 = generation.newFormula();
        formula1.setFormulaSignature("f1");
        IFormula formula2 = generation.newFormula();
        formula2.setFormulaSignature("f2");

        assertEquals(formula1, generation.getFormula("f1"));
        assertEquals(formula2, generation.getFormula("f2"));

        assertNull(generation.getFormula("unknwon"));
        assertNull(generation.getFormula(null));
    }

    @Test
    public void testGetPropertyValue() {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("a1");
        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setRoleName("RateTable");
        IProductCmptTypeMethod signature = productCmptType.newFormulaSignature("calculation");
        IPolicyCmptTypeAttribute policyAttr = policyCmptType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttr.setProductRelevant(true);

        IAttributeValue value = generation.newAttributeValue();
        value.setAttribute("a1");
        IFormula formula = generation.newFormula();
        formula.setFormulaSignature("calculation");
        ITableContentUsage contentUsage = generation.newTableContentUsage();
        contentUsage.setStructureUsage("RateTable");
        IConfigElement element = generation.newConfigElement();
        element.setPolicyCmptTypeAttribute("policyAttribute");

        assertEquals(value, generation.getPropertyValue(attribute));
        assertEquals(formula, generation.getPropertyValue(signature));
        assertEquals(contentUsage, generation.getPropertyValue(structureUsage));
        assertEquals(element, generation.getPropertyValue(policyAttr));
    }

    @Test
    public void testHasPropertyValue() {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("productAttribute");

        assertFalse(generation.hasPropertyValue(attribute));

        generation.newAttributeValue(attribute);
        assertTrue(generation.hasPropertyValue(attribute));
    }

    @Test
    public void testNewFormula_FormulaSignature() {
        IProductCmptTypeMethod signature = productCmptType.newFormulaSignature("Calc");
        IFormula formula = generation.newFormula(signature);
        assertEquals("Calc", formula.getFormulaSignature());

        formula = generation.newFormula();
        assertEquals("", formula.getFormulaSignature());
    }

    @Test
    public void testNewTableContentUsage_TableStructure() {
        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setRoleName("RateTable");
        ITableContentUsage contentUsage = generation.newTableContentUsage(structureUsage);
        assertEquals("RateTable", contentUsage.getStructureUsage());

        contentUsage = generation.newTableContentUsage();
        assertEquals("", contentUsage.getStructureUsage());
    }

    @Test
    public void testNewAttributeValue_Attribute() {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("premium");
        attribute.setDefaultValue("123");
        IAttributeValue value = generation.newAttributeValue(attribute);
        assertEquals("123", value.getPropertyValue());
        assertEquals("premium", value.getAttribute());

        value = generation.newAttributeValue();
        assertNull(value.getPropertyValue());
        assertEquals("", value.getAttribute());
    }

    @Test
    public void testNewConfigElement_PolicyAttribute() {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("a1");
        attribute.setProductRelevant(true);
        attribute.setDefaultValue("10");
        attribute.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attribute.getValueSet();
        range.setLowerBound("1");
        range.setUpperBound("42");

        IConfigElement el = generation.newConfigElement(attribute);
        assertEquals("a1", el.getPolicyCmptTypeAttribute());
        assertEquals("10", el.getValue());
        range = (IRangeValueSet)el.getValueSet();
        assertEquals("1", range.getLowerBound());
        assertEquals("42", range.getUpperBound());
    }

    @Test
    public void testGetPropertyValues() {
        IAttributeValue value1 = generation.newAttributeValue();
        IFormula formula1 = generation.newFormula();
        IFormula formula2 = generation.newFormula();
        ITableContentUsage tcu1 = generation.newTableContentUsage();
        ITableContentUsage tcu2 = generation.newTableContentUsage();
        ITableContentUsage tcu3 = generation.newTableContentUsage();
        IConfigElement ce1 = generation.newConfigElement();
        IConfigElement ce2 = generation.newConfigElement();
        IConfigElement ce3 = generation.newConfigElement();
        IConfigElement ce4 = generation.newConfigElement();

        List<? extends IPropertyValue> values = generation
                .getPropertyValues(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE.getValueClass());
        assertEquals(1, values.size());
        assertEquals(value1, values.get(0));

        values = generation.getPropertyValues(ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION.getValueClass());
        assertEquals(2, values.size());
        assertEquals(formula1, values.get(0));
        assertEquals(formula2, values.get(1));

        values = generation.getPropertyValues(ProductCmptPropertyType.TABLE_STRUCTURE_USAGE.getValueClass());
        assertEquals(3, values.size());
        assertEquals(tcu1, values.get(0));
        assertEquals(tcu2, values.get(1));
        assertEquals(tcu3, values.get(2));

        values = generation.getPropertyValues(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE.getValueClass());
        assertEquals(4, values.size());
        assertEquals(ce1, values.get(0));
        assertEquals(ce2, values.get(1));
        assertEquals(ce3, values.get(2));
        assertEquals(ce4, values.get(3));
    }

    @Test
    public void testNewLink() {
        IProductCmptLink link = generation.newLink("coverage");
        assertEquals(generation, link.getParent());
        assertEquals(1, generation.getNumOfLinks());
        assertEquals(link, generation.getLinks()[0]);

        IProductCmptLink link2 = generation.newLink("covergae");
        assertEquals(generation, link2.getParent());
        assertEquals(2, generation.getNumOfLinks());
        assertEquals(link, generation.getLinks()[0]);
        assertEquals(link2, generation.getLinks()[1]);
    }

    @Test
    public void testToXmlElement() {
        generation.setValidFrom(new GregorianCalendar(2005, 0, 1));
        generation.newConfigElement();
        generation.newConfigElement();
        generation.newLink("coverage");
        generation.newLink("coverage");
        generation.newLink("coverage");
        generation.newFormula();
        generation.newFormula();
        generation.newAttributeValue();
        generation.newTableContentUsage();
        newValidationRuleConfig();
        newValidationRuleConfig();

        Element element = generation.toXml(newDocument());

        IProductCmptGeneration copy = new ProductCmptGeneration();
        copy.initFromXml(element);
        assertEquals(2, copy.getNumOfConfigElements());
        assertEquals(3, copy.getNumOfLinks());
        assertEquals(2, copy.getNumOfFormulas());
        assertEquals(1, copy.getNumOfAttributeValues());
        assertEquals(2, copy.getNumOfValidationRules());
        assertEquals(1, copy.getNumOfTableContentUsages());
    }

    @Test
    public void testInitFromXml() {
        generation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(new GregorianCalendar(2005, 0, 1), generation.getValidFrom());

        IAttributeValue[] attrValues = generation.getAttributeValues();
        assertEquals(1, attrValues.length);

        IConfigElement[] configElements = generation.getConfigElements();
        assertEquals(1, configElements.length);

        IProductCmptLink[] relations = generation.getLinks();
        assertEquals(1, relations.length);

        IFormula[] formulas = generation.getFormulas();
        assertEquals(2, formulas.length);

        List<IValidationRuleConfig> rules = generation.getValidationRuleConfigs();
        assertEquals(1, rules.size());

        ITableContentUsage[] tableContentUsages = generation.getTableContentUsages();
        assertEquals(1, tableContentUsages.length);
    }

    @Test
    public void testValidateDuplicateRelationTarget() throws Exception {
        MessageList ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_DUPLICATE_RELATION_TARGET));

        generation.newLink(association.getName()).setTarget(target.getQualifiedName());
        ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_DUPLICATE_RELATION_TARGET));

        generation.newLink(association).setTarget(target.getQualifiedName());

        ml = generation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_DUPLICATE_RELATION_TARGET));
    }

    @Test
    public void testValidateNotEnoughRelations() throws Exception {
        validateNotEnoughRelationsTest(generation);
    }

    private void validateNotEnoughRelationsTest(IProductCmptGeneration baseGeneration) throws Exception {
        association.setMinCardinality(1);
        association.setMaxCardinality(2);

        MessageList ml = baseGeneration.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_NOT_ENOUGH_RELATIONS));

        baseGeneration.newLink(association.getTargetRoleSingular());
        ml = baseGeneration.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_NOT_ENOUGH_RELATIONS));

        baseGeneration.newLink(association.getTargetRoleSingular());
        ml = baseGeneration.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_NOT_ENOUGH_RELATIONS));

    }

    @Test
    public void testValidateTooManyRelations() throws Exception {
        validateTooManyRelationsTest(generation);
    }

    private void validateTooManyRelationsTest(IProductCmptGeneration baseGeneration) throws Exception {
        association.setMinCardinality(0);
        association.setMaxCardinality(1);

        MessageList ml = baseGeneration.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_TOO_MANY_RELATIONS));

        baseGeneration.newLink(association.getTargetRoleSingular());
        ml = baseGeneration.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_TOO_MANY_RELATIONS));

        baseGeneration.newLink(association.getTargetRoleSingular());
        ml = baseGeneration.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_TOO_MANY_RELATIONS));
    }

    @Test
    public void testValidateNotEnoughRelationsHierarchy() throws Exception {
        validateNotEnoughRelationsTest(getSubGenertation());
    }

    @Test
    public void testValidateTooManyRelationsHierarchy() throws Exception {
        validateTooManyRelationsTest(getSubGenertation());
    }

    private IProductCmptGeneration getSubGenertation() throws CoreException {
        PolicyCmptType subPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SubPolicy", "SubProduct");
        subPolicyCmptType.setSupertype(policyCmptType.getName());
        IProductCmptType subProductCmptType = subPolicyCmptType.findProductCmptType(ipsProject);
        subProductCmptType.setSupertype(productCmptType.getName());
        ProductCmpt subProductCmpt = newProductCmpt(subProductCmptType, "SubTestProduct");
        IProductCmptGeneration subGeneration = subProductCmpt.getProductCmptGeneration(0);
        return subGeneration;
    }

    @Test
    public void testValidateAttributeWithMissingConfigElement() throws Exception {
        IProductCmpt product = newProductCmpt(productCmptType, "EmptyTestProduct");
        IProductCmptGeneration gen = product.getProductCmptGeneration(0);
        MessageList msgList = gen.validate(ipsProject);
        assertTrue(msgList.isEmpty());

        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setProductRelevant(true);
        attribute.setName("test");
        msgList = gen.validate(ipsProject);
        assertFalse(msgList.isEmpty());
        assertNotNull(msgList.getMessageByCode(IProductCmptGeneration.MSGCODE_ATTRIBUTE_WITH_MISSING_CONFIG_ELEMENT));
    }

    @Test
    public void testCanCreateValidRelation() throws Exception {
        assertFalse(generation.canCreateValidLink(null, null, ipsProject));
        assertFalse(generation.canCreateValidLink(productCmpt, null, ipsProject));

        assertTrue(generation.canCreateValidLink(target, association, ipsProject));
    }

    /**
     * test for bug #829
     */
    @Test
    public void testCanCreateValidRelation_RelationDefinedInSupertypeHierarchyOfSourceType() throws Exception {
        // create a subtype of the existing policy component type
        IPolicyCmptType subpolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SubPolicyType", "SubProductType");
        IProductCmptType subProductCmptType = subpolicyCmptType.findProductCmptType(ipsProject);
        subpolicyCmptType.setSupertype(policyCmptType.getQualifiedName());
        subProductCmptType.setSupertype(productCmptType.getQualifiedName());

        IProductCmpt productCmpt2 = newProductCmpt(subProductCmptType, "TestProduct2");
        IProductCmptGeneration generation2 = productCmpt2.getProductCmptGeneration(0);

        assertTrue(generation2.canCreateValidLink(target, association, ipsProject));
    }

    @Test
    public void testGetChildren() throws CoreException {
        IConfigElement element = generation.newConfigElement();
        IProductCmptLink link = generation.newLink("targetRole");
        ITableContentUsage usage = generation.newTableContentUsage();
        IFormula formula = generation.newFormula();

        IValidationRule rule = mock(IValidationRule.class);
        when(rule.getPropertyName()).thenReturn("newRule");
        when(rule.isActivatedByDefault()).thenReturn(false);
        when(rule.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.VALIDATION_RULE);
        IValidationRuleConfig ruleConfig = generation.newValidationRuleConfig(rule);

        IIpsElement[] children = generation.getChildren();
        List<IIpsElement> childrenList = Arrays.asList(children);
        assertTrue(childrenList.contains(element));
        assertTrue(childrenList.contains(usage));
        assertTrue(childrenList.contains(formula));
        assertTrue(childrenList.contains(link));
        assertTrue(childrenList.contains(ruleConfig));
    }

    @Test
    public void testGetConfigElements() {
        assertEquals(0, generation.getNumOfConfigElements());

        IConfigElement ce1 = generation.newConfigElement();
        assertEquals(ce1, generation.getConfigElements()[0]);

        IConfigElement ce2 = generation.newConfigElement();
        assertEquals(ce1, generation.getConfigElements()[0]);
        assertEquals(ce2, generation.getConfigElements()[1]);
    }

    @Test
    public void testGetConfigElements_Type() {
        IConfigElement ce1 = generation.newConfigElement();
        IConfigElement ce2 = generation.newConfigElement();

        IConfigElement[] elements = generation.getConfigElements();
        assertEquals(2, elements.length);
        assertEquals(ce1, elements[0]);
        assertEquals(ce2, elements[1]);
    }

    @Test
    public void testGetConfigElement_AttributeName() {
        generation.newConfigElement();
        IConfigElement ce2 = generation.newConfigElement();
        ce2.setPolicyCmptTypeAttribute("a2");

        assertEquals(ce2, generation.getConfigElement("a2"));
        assertNull(generation.getConfigElement("unkown"));
    }

    @Test
    public void testGetNumOfConfigElements() {
        assertEquals(0, generation.getNumOfConfigElements());

        generation.newConfigElement();
        assertEquals(1, generation.getNumOfConfigElements());

        generation.newConfigElement();
        assertEquals(2, generation.getNumOfConfigElements());
    }

    @Test
    public void testNewConfigElement() {
        IConfigElement ce = generation.newConfigElement();
        assertEquals(generation, ce.getParent());
        assertEquals(1, generation.getNumOfConfigElements());
    }

    @Test
    public void testGetRelations() {
        IProductCmptLink r1 = generation.newLink("coverage");
        assertEquals(r1, generation.getLinks()[0]);

        IProductCmptLink r2 = generation.newLink("risk");
        assertEquals(r1, generation.getLinks()[0]);
        assertEquals(r2, generation.getLinks()[1]);
    }

    @Test
    public void testGetRelations_String() {
        IProductCmptLink r1 = generation.newLink("coverage");
        generation.newLink("risk");
        IProductCmptLink r3 = generation.newLink("coverage");

        IProductCmptLink[] relations = generation.getLinks("coverage");
        assertEquals(2, relations.length);
        assertEquals(r1, relations[0]);
        assertEquals(r3, relations[1]);

        relations = generation.getLinks("unknown");
        assertEquals(0, relations.length);
    }

    @Test
    public void testGetNumOfLinks() {
        assertEquals(0, generation.getNumOfLinks());

        generation.newLink("coverage");
        assertEquals(1, generation.getNumOfLinks());

        generation.newLink("risk");
        assertEquals(2, generation.getNumOfLinks());
    }

    @Test
    public void testValidateNoTemplate() throws Exception {
        generation.getProductCmpt().setProductCmptType("");
        MessageList ml = generation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NO_TEMPLATE));
    }

    @Test
    public void testValidateValidFrom() throws Exception {
        generation.getProductCmpt().setValidTo(new GregorianCalendar(2000, 10, 1));
        generation.setValidFrom(new GregorianCalendar(2000, 10, 2));

        MessageList ml = generation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IIpsObjectGeneration.MSGCODE_INVALID_VALID_FROM));

        generation.setValidFrom(new GregorianCalendar(2000, 9, 1));
        ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IIpsObjectGeneration.MSGCODE_INVALID_VALID_FROM));
    }

    @Test
    public void testValidateIfReferencedProductComponentsAreValidOnThisGenerationsValidFromDate() throws CoreException,
            Exception {
        generation.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));
        IProductCmptLink link = generation.newLink(association);
        link.setTarget(target.getQualifiedName());
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        IProductCmptGeneration targetGeneration = (IProductCmptGeneration)target.getGeneration(0);
        targetGeneration.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2008-01-01"));

        MessageList msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IProductCmptGeneration.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));

        // assert that there is no validation error if the optional constraint
        // "referencedProductComponentsAreValidOnThisGenerationsValidFromDate" is turned off
        IIpsProjectProperties oldProps = ipsProject.getProperties();
        IIpsProjectProperties newProps = new IpsProjectProperties(ipsProject, (IpsProjectProperties)oldProps);
        newProps.setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(false);
        ipsProject.setProperties(newProps);
        msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNull(msgList.getMessageByCode(IProductCmptGeneration.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));
        ipsProject.getProperties()
                .setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(true);
        ipsProject.setProperties(oldProps);

        targetGeneration.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));
        msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNull(msgList.getMessageByCode(IProductCmptGeneration.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));

        targetGeneration.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2006-01-01"));
        msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNull(msgList.getMessageByCode(IProductCmptGeneration.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));
    }

    @Test
    public void testGetValidationRules() {
        List<IValidationRuleConfig> rules = generation.getValidationRuleConfigs();
        assertEquals(0, rules.size());

        newValidationRuleConfig();
        rules = generation.getValidationRuleConfigs();
        assertEquals(1, rules.size());

        newValidationRuleConfig();
        rules = generation.getValidationRuleConfigs();
        assertEquals(2, rules.size());
    }

    private IValidationRuleConfig newValidationRuleConfig() {
        IValidationRule rule = mock(IValidationRule.class);
        when(rule.getPropertyName()).thenReturn("newRule");
        when(rule.isActivatedByDefault()).thenReturn(false);
        when(rule.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.VALIDATION_RULE);
        return generation.newValidationRuleConfig(rule);
    }

    @Test
    public void testGetNumValidationRules() {
        assertEquals(0, generation.getNumOfValidationRules());

        newValidationRuleConfig();
        assertEquals(1, generation.getNumOfValidationRules());

        newValidationRuleConfig();
        assertEquals(2, generation.getNumOfValidationRules());
    }

    @Test
    public void testNewValidationRule() throws CoreException {
        assertEquals(0, generation.getNumOfValidationRules());

        newValidationRuleConfig();
        assertEquals(1, generation.getChildren().length);

        newValidationRuleConfig();
        assertEquals(2, generation.getChildren().length);
    }

    @Test
    public void testGetValidationRuleByName() {
        IValidationRule rule;

        rule = policyCmptType.newRule();
        rule.setName("rule1");
        generation.newValidationRuleConfig(rule);

        rule = policyCmptType.newRule();
        rule.setName("ruleTwo");
        generation.newValidationRuleConfig(rule);

        rule = policyCmptType.newRule();
        rule.setName("ruleThree");
        generation.newValidationRuleConfig(rule);

        assertEquals(3, generation.getValidationRuleConfigs().size());

        assertNotNull(generation.getValidationRuleConfig("rule1"));
        assertNotNull(generation.getValidationRuleConfig("ruleTwo"));
        assertNotNull(generation.getValidationRuleConfig("ruleThree"));
        assertNull(generation.getValidationRuleConfig("nonExistingRule"));
        assertNull(generation.getValidationRuleConfig(null));
    }

    @Test
    public void testIsContainerForChangingAssociation() {
        IProductCmptTypeAssociation changingAssoc = productCmptType.newProductCmptTypeAssociation();
        changingAssoc.setChangingOverTime(true);

        assertTrue(generation.isContainerFor(changingAssoc));
    }

    @Test
    public void testIsContainerForStaticAssociation() {
        IProductCmptTypeAssociation staticAssoc = productCmptType.newProductCmptTypeAssociation();
        staticAssoc.setChangingOverTime(false);

        assertFalse(generation.isContainerFor(staticAssoc));
    }

    @Test
    public void testGetLinksIncludingProductCmpt() throws Exception {
        IProductCmptGeneration generation1 = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(
                2010, 0, 1));
        IProductCmptGeneration generation2 = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(
                2011, 0, 1));
        ArrayList<IProductCmptLink> links = new ArrayList<IProductCmptLink>();
        links.add(productCmpt.newLink("asdff"));
        links.add(productCmpt.newLink("asdff2"));
        links.add(generation1.newLink("asd1"));
        links.add(generation1.newLink("asd2"));
        generation2.newLink("notExpected1");
        generation2.newLink("notExpected2");

        List<IProductCmptLink> linksIncludingGenerations = generation1.getLinksIncludingProductCmpt();
        assertEquals(links, linksIncludingGenerations);
    }

    @Test
    public void testIsContainingAvailableFormula_noFormula() throws Exception {
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(
                2010, 0, 1));

        assertFalse(generation.isContainingAvailableFormula());
    }

    @Test
    public void testIsContainingAvailableFormula_anyEmptyFormula() throws Exception {
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(
                2010, 0, 1));
        generation.newFormula();

        assertFalse(generation.isContainingAvailableFormula());
    }

    @Test
    public void testIsContainingAvailableFormula_anyAvailableFormula() throws Exception {
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(
                2010, 0, 1));
        IFormula newFormula = generation.newFormula();
        newFormula.setExpression("anyExpression");

        assertTrue(generation.isContainingAvailableFormula());
    }

    @Test
    public void testIsContainingAvailableFormula_twoFormulas() throws Exception {
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(
                2010, 0, 1));
        generation.newFormula();
        IFormula newFormula = generation.newFormula();
        newFormula.setExpression("anyExpression");

        assertTrue(generation.isContainingAvailableFormula());
    }

    @Test
    public void testAddDependenciesFromFormulaExpressions() throws Exception {
        ProductCmptGeneration generationSpy = spy((ProductCmptGeneration)generation);
        IDependency dependency = mock(IDependency.class);
        ExpressionDependencyDetail dependencyDetail1 = mock(ExpressionDependencyDetail.class);
        ExpressionDependencyDetail dependencyDetail2 = mock(ExpressionDependencyDetail.class);
        IFormula formula1 = mock(IFormula.class);
        IFormula formula2 = mock(IFormula.class);
        when(generationSpy.getFormulas()).thenReturn(new IFormula[] { formula1, formula2 });
        Map<IDependency, ExpressionDependencyDetail> dependencyMap1 = new HashMap<IDependency, ExpressionDependencyDetail>();
        dependencyMap1.put(dependency, dependencyDetail1);
        Map<IDependency, ExpressionDependencyDetail> dependencyMap2 = new HashMap<IDependency, ExpressionDependencyDetail>();
        dependencyMap2.put(dependency, dependencyDetail2);
        when(formula1.dependsOn()).thenReturn(dependencyMap1);
        when(formula2.dependsOn()).thenReturn(dependencyMap2);

        Set<IDependency> dependenciesResult = new HashSet<IDependency>();
        Map<IDependency, List<IDependencyDetail>> detailsResult = new HashMap<IDependency, List<IDependencyDetail>>();
        generationSpy.dependsOn(dependenciesResult, detailsResult);

        assertEquals(1, dependenciesResult.size());
        assertThat(dependenciesResult, hasItem(dependency));
        assertEquals(1, detailsResult.size());
        assertThat(detailsResult.keySet(), hasItem(dependency));
        List<? extends IDependencyDetail> detailList = detailsResult.get(dependency);
        assertEquals(2, detailList.size());
        assertEquals(dependencyDetail1, detailList.get(0));
        assertEquals(dependencyDetail2, detailList.get(1));
    }

    @Test
    public void testAddRelatedTableContentsQualifiedNameTypes() {
        ProductCmptGeneration generationSpy = spy((ProductCmptGeneration)generation);
        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setRoleName("RateTable");
        ITableContentUsage contentUsage = generation.newTableContentUsage(structureUsage);
        assertEquals("RateTable", contentUsage.getStructureUsage());

        Set<IDependency> dependenciesResult = new HashSet<IDependency>();
        Map<IDependency, List<IDependencyDetail>> detailsResult = new HashMap<IDependency, List<IDependencyDetail>>();
        generationSpy.dependsOn(dependenciesResult, detailsResult);

        assertEquals(1, dependenciesResult.size());
        assertEquals(1, detailsResult.size());
    }

}
