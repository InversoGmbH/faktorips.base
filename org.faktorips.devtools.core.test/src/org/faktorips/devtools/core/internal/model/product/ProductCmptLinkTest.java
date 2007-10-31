/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptLinkTest extends AbstractIpsPluginTest {

	private IIpsSrcFile ipsSrcFile;
    private ProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IProductCmptLink link;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IIpsProject ipsProject;
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
    	super.setUp();
    	ipsProject = newIpsProject();
    	policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
    	productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
    	generation = productCmpt.getProductCmptGeneration(0);
    	link = generation.newLink("CoverageType");
    	ipsSrcFile = productCmpt.getIpsSrcFile();
    }
    
    public void testGetAssociation() throws CoreException {
    	assertEquals("CoverageType", link.getAssociation());
    }

    public void testFindAssociation() throws CoreException {
    	IProductCmptTypeAssociation assocation = productCmptType.newProductCmptTypeAssociation();

        assocation.setTargetRoleSingular("CoverageType");
    	assertEquals(assocation, link.findAssociation(ipsProject));
    	
    	assocation.setTargetRoleSingular("blabla");
    	assertNull(link.findAssociation(ipsProject));
    }

    public void testRemove() {
        link.delete();
        assertEquals(0, generation.getNumOfLinks());
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testSetTarget() {
        link.setTarget("newTarget");
        assertEquals("newTarget", link.getTarget());
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testToXml() {
        link = generation.newLink("coverage");
        link.setTarget("newTarget");
        link.setMinCardinality(2);
        link.setMaxCardinality(3);
        Element element = link.toXml(newDocument());
        
        IProductCmptLink copy = new ProductCmptLink();
        copy.initFromXml(element);
        assertEquals(1, copy.getId());
        assertEquals("newTarget", copy.getTarget());
        assertEquals("coverage", copy.getAssociation());
        assertEquals(2, copy.getMinCardinality());
        assertEquals(3, copy.getMaxCardinality());
        
        link.setMaxCardinality(Integer.MAX_VALUE);
        element = link.toXml(newDocument());
        copy.initFromXml(element);
        assertEquals(Integer.MAX_VALUE, copy.getMaxCardinality());
    }

    public void testInitFromXml() {
        link.initFromXml((Element)getTestDocument().getDocumentElement().getElementsByTagName(IProductCmptLink.TAG_NAME).item(0));
        assertEquals(42, link.getId());
        assertEquals("FullCoverage", link.getAssociation());
        assertEquals("FullCoveragePlus", link.getTarget());
        assertEquals(2, link.getMinCardinality());
        assertEquals(3, link.getMaxCardinality());

        link.initFromXml((Element)getTestDocument().getDocumentElement().getElementsByTagName(IProductCmptLink.TAG_NAME).item(1));
        assertEquals(43, link.getId());
        assertEquals(1, link.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, link.getMaxCardinality());
    }

    /**
     * Tests for the correct type of excetion to be thrown - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			link.newPart(IPolicyCmptTypeAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    public void testValidateUnknownAssociate() throws CoreException {
        MessageList ml = link.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_UNKNWON_ASSOCIATION));
    }
    
    public void testValidateUnknownTarget() throws CoreException {
        link.setTarget("unknown");
        MessageList ml = link.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_UNKNWON_TARGET));
        
        link.setTarget(productCmpt.getQualifiedName());
        ml = link.validate();
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_UNKNWON_TARGET));
    }
 
    public void testValidateCardinality() throws CoreException {
        IPolicyCmptType coverageType = newPolicyAndProductCmptType(ipsProject, "TestCoverage", "TestCoverageType");
        IProductCmptType coverageTypeType = coverageType.findProductCmptType(ipsProject);
        
        IProductCmptTypeAssociation productAssociation = productCmptType.newProductCmptTypeAssociation();
        productAssociation.setTarget(coverageTypeType.getQualifiedName());
        productAssociation.setTargetRoleSingular("CoverageType");
        
        IAssociation policyAssociation = policyCmptType.newAssociation();
        policyAssociation.setTarget(coverageType.getQualifiedName());
        policyAssociation.setTargetRoleSingular("Coverage");
        
        // test setup
        assertEquals(policyAssociation, productAssociation.findMatchingPolicyCmptTypeRelation(ipsProject));
        assertEquals(productAssociation, link.findAssociation(ipsProject));
        
    	link.setMaxCardinality(0);
    	MessageList ml = link.validate();
    	assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MISSING_MAX_CARDINALITY));
    	assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1));

    	link.setMaxCardinality(1);
    	ml = link.validate();
    	assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1));
    	
    	link.setMinCardinality(2);
    	ml = link.validate();
    	assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN));
    	
    	link.setMaxCardinality(3);
    	ml = link.validate();
    	assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN));
    	
        policyAssociation.setMaxCardinality(1);
    	ml = link.validate();
    	assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));

        policyAssociation.setMaxCardinality(3);
    	ml = link.validate();
    	assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX));
    }
    
    public void testValidateInvalidTarget() throws Exception{
        IPolicyCmptType targetType = newPolicyAndProductCmptType(ipsProject, "Coverage", "CoverageType");
        IProductCmptType targetProductType = targetType.findProductCmptType(ipsProject);
        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setTarget(targetProductType.getQualifiedName());
        association.setTargetRoleSingular("testRelation");
        
        IProductCmptLink link = generation.newLink(association.getName());
        IProductCmpt target = newProductCmpt(targetProductType, "target.Target");
        link.setTarget(productCmpt.getQualifiedName());
        
        MessageList ml = link.validate();
        assertNotNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_INVALID_TARGET));
        
        link.setTarget(target.getQualifiedName());
        
        ml = link.validate();
        assertNull(ml.getMessageByCode(IProductCmptLink.MSGCODE_INVALID_TARGET));
    }

    public void testIsMandatory(){
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        assertFalse(link.isMandatory());
        
        link.setMinCardinality(1);
        link.setMaxCardinality(1);
        assertTrue(link.isMandatory());

        link.setMinCardinality(2);
        link.setMaxCardinality(3);
        assertFalse(link.isMandatory());

        link.setMinCardinality(3);
        link.setMaxCardinality(2);
        assertFalse(link.isMandatory());
    }
    public void testIsOptional(){
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        assertTrue(link.isOptional());
        
        link.setMinCardinality(1);
        link.setMaxCardinality(1);
        assertFalse(link.isOptional());

        link.setMinCardinality(2);
        link.setMaxCardinality(3);
        assertFalse(link.isOptional());

        link.setMinCardinality(3);
        link.setMaxCardinality(2);
        assertFalse(link.isOptional());
    }
}
