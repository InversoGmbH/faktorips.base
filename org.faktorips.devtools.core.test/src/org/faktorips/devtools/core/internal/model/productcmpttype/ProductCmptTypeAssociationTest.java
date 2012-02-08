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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAssociationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmptType productType;
    private IProductCmptType coverageTypeType;
    private IProductCmptTypeAssociation association;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productType = newProductCmptType(ipsProject, "Product");
        coverageTypeType = newProductCmptType(ipsProject, "CoverageType");
        association = productType.newProductCmptTypeAssociation();
    }

    @Test
    public void testFindPolicyCmptTypeAssociation() throws CoreException {
        assertNull(association.findMatchingPolicyCmptTypeAssociation(ipsProject));

        association.setTarget(coverageTypeType.getQualifiedName());
        assertNull(association.findMatchingPolicyCmptTypeAssociation(ipsProject));

        IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "Policy");
        productType.setPolicyCmptType(policyType.getQualifiedName());
        policyType.setProductCmptType(productType.getQualifiedName());

        IPolicyCmptTypeAssociation policyTypeAssociation = policyType.newPolicyCmptTypeAssociation();
        policyTypeAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertNull(association.findMatchingPolicyCmptTypeAssociation(ipsProject));

        IPolicyCmptType coverageType = newPolicyCmptType(ipsProject, "Coverage");
        policyTypeAssociation.setTarget(coverageType.getQualifiedName());
        assertNull(association.findMatchingPolicyCmptTypeAssociation(ipsProject));

        IPolicyCmptTypeAssociation detailToMasterAssoc = policyType.newPolicyCmptTypeAssociation();
        detailToMasterAssoc.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        detailToMasterAssoc.setTarget(coverageType.getQualifiedName());

        coverageTypeType.setPolicyCmptType(coverageType.getQualifiedName());
        assertEquals(policyTypeAssociation, association.findMatchingPolicyCmptTypeAssociation(ipsProject));

        IProductCmptTypeAssociation association2 = productType.newProductCmptTypeAssociation();
        association2.setTargetRoleSingular("otherAssociation");
        association2.setTarget(coverageTypeType.getQualifiedName());
        assertNull(association2.findMatchingPolicyCmptTypeAssociation(ipsProject));
    }

    /**
     * This is testing the special combination of product and policy type associations discussed in
     * FIPS-563
     * 
     */
    @Test
    public void testFindPolicyCmptTypeAssociation2() throws CoreException {
        PolicyCmptType police = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = police.findProductCmptType(ipsProject);

        PolicyCmptType versPerson = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "VersPerson");

        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation policeToVersPerson = police.newPolicyCmptTypeAssociation();
        policeToVersPerson.setTargetRoleSingular("VersPers");
        policeToVersPerson.setTarget(versPerson.getQualifiedName());

        IPolicyCmptTypeAssociation versPersonToTarifvereinbarung = versPerson.newPolicyCmptTypeAssociation();
        versPersonToTarifvereinbarung.setTargetRoleSingular("versPersonToTarifvereinbarung");
        versPersonToTarifvereinbarung.setTarget(tarifvereinbarung.getQualifiedName());

        IProductCmptTypeAssociation produktToTarif = produkt.newProductCmptTypeAssociation();
        produktToTarif.setTargetRoleSingular("produktToTarif");
        produktToTarif.setTarget(tarif.getQualifiedName());

        assertNull(produktToTarif.findMatchingPolicyCmptTypeAssociation(ipsProject));

        versPersonToTarifvereinbarung.setMatchingAssociationSource(produkt.getQualifiedName());
        versPersonToTarifvereinbarung.setMatchingAssociationName(produktToTarif.getName());

        assertNull(produktToTarif.findMatchingPolicyCmptTypeAssociation(ipsProject));

        produktToTarif.setMatchingAssociationSource(police.getQualifiedName());
        produktToTarif.setMatchingAssociationName(policeToVersPerson.getName());

        assertEquals(policeToVersPerson, produktToTarif.findMatchingPolicyCmptTypeAssociation(ipsProject));

        produktToTarif.setMatchingAssociationSource(versPerson.getQualifiedName());
        produktToTarif.setMatchingAssociationName(versPersonToTarifvereinbarung.getName());
        assertEquals(versPersonToTarifvereinbarung, produktToTarif.findMatchingPolicyCmptTypeAssociation(ipsProject));
    }

    /**
     * Test for FIPS-714
     */
    @Test
    public void shouldNotFindMatchingAssociationForDifferingHierarchy() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Policy", "MyProduct");
        IProductCmptType product = policy.findProductCmptType(ipsProject);
        ProductCmptType subProduct = newProductCmptType(ipsProject, "SubProduct");
        subProduct.setSupertype(product.getQualifiedName());
        subProduct.setPolicyCmptType(policy.getQualifiedName());

        PolicyCmptType cover = newPolicyAndProductCmptType(ipsProject, "Coverage", "MyCoverageType");
        IProductCmptType coverType = cover.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation policyToCover = policy.newPolicyCmptTypeAssociation();
        policyToCover.setTarget(cover.getQualifiedName());

        IProductCmptTypeAssociation productToCoverType = product.newProductCmptTypeAssociation();
        productToCoverType.setTarget(coverType.getQualifiedName());

        assertEquals(policyToCover, productToCoverType.findDefaultPolicyCmptTypeAssociation(ipsProject));
        assertEquals(productToCoverType, policyToCover.findDefaultMatchingProductCmptTypeAssociation(ipsProject));

        IProductCmptTypeAssociation subProductToCoverType = subProduct.newProductCmptTypeAssociation();
        subProductToCoverType.setTarget(coverType.getQualifiedName());

        assertNull(subProductToCoverType.findDefaultPolicyCmptTypeAssociation(ipsProject));
    }

    /**
     * Test scenario described in FIPS-563
     */
    @Test
    public void testFindPossibleMatchingPolicyCmptTypeAssociations() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = policy.findProductCmptType(ipsProject);

        PolicyCmptType versPerson = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "VersPerson");

        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IProductCmptTypeAssociation produktToTarif = newAggregation(produkt, tarif);

        Set<IPolicyCmptTypeAssociation> possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(0, possibleMatchingPolicyCmptTypeAssociations.size());

        IPolicyCmptTypeAssociation policyToVersPerson = newComposition(policy, versPerson);

        possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(0, possibleMatchingPolicyCmptTypeAssociations.size());

        IPolicyCmptTypeAssociation versPersonToTarifvereinbarung = newComposition(versPerson, tarifvereinbarung);

        possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(2, possibleMatchingPolicyCmptTypeAssociations.size());
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToVersPerson));
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(versPersonToTarifvereinbarung));

        versPerson.delete();

        possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(0, possibleMatchingPolicyCmptTypeAssociations.size());
    }

    @Test
    public void testFindPossibleMatchingPolicyCmptTypeAssociations2() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = policy.findProductCmptType(ipsProject);
        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IPolicyCmptTypeAssociation policyToTarifvereinbarung = newComposition(policy, tarifvereinbarung);
        IProductCmptTypeAssociation produktToTarif = newAggregation(produkt, tarif);

        Set<IPolicyCmptTypeAssociation> possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(1, possibleMatchingPolicyCmptTypeAssociations.size());
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToTarifvereinbarung));

        PolicyCmptType versPerson = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "VersPerson");

        possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(1, possibleMatchingPolicyCmptTypeAssociations.size());
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToTarifvereinbarung));

        IPolicyCmptTypeAssociation versPersonToTarifvereinbarung = newComposition(versPerson, tarifvereinbarung);
        IPolicyCmptTypeAssociation policyToVersPerson = newComposition(policy, versPerson);

        possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(3, possibleMatchingPolicyCmptTypeAssociations.size());
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToTarifvereinbarung));
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToVersPerson));
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(versPersonToTarifvereinbarung));

    }

    @Test
    public void testFindPossibleMatchingPolicyCmptTypeAssociations3() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = policy.findProductCmptType(ipsProject);

        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IProductCmptTypeAssociation produktToTarif = newAggregation(produkt, tarif);

        PolicyCmptType vp1 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "VP1");
        PolicyCmptType vp2 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "VP2");

        IPolicyCmptTypeAssociation policyToVp1 = newComposition(policy, vp1);
        IPolicyCmptTypeAssociation vp1ToVp2 = newComposition(vp1, vp2);
        IPolicyCmptTypeAssociation vp2ToTarifvereinbarung = newComposition(vp2, tarifvereinbarung);

        Set<IPolicyCmptTypeAssociation> possibleMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(3, possibleMatchingPolicyCmptTypeAssociations.size());
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(policyToVp1));
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(vp1ToVp2));
        assertTrue(possibleMatchingPolicyCmptTypeAssociations.contains(vp2ToTarifvereinbarung));
    }

    @Test
    public void testFindPossibleMatchingAssociationsCycle() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Police", "Produkt");
        IProductCmptType produkt = policy.findProductCmptType(ipsProject);

        PolicyCmptType tarifvereinbarung = newPolicyAndProductCmptType(ipsProject, "Tarifvereinbarung", "Tarif");
        IProductCmptType tarif = tarifvereinbarung.findProductCmptType(ipsProject);

        IProductCmptTypeAssociation produktToTarif = newAggregation(produkt, tarif);

        PolicyCmptType polRef = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "PolRef");

        newComposition(policy, polRef);
        newComposition(polRef, polRef);

        Set<IPolicyCmptTypeAssociation> possiblyMatchingPolicyCmptTypeAssociations = produktToTarif
                .findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        assertEquals(0, possiblyMatchingPolicyCmptTypeAssociations.size());
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer#toXml(org.w3c.dom.Document)}
     * .
     */
    @Test
    public void testToXml() {
        association.setTarget("pack1.CoverageType");
        association.setTargetRoleSingular("CoverageType");
        association.setTargetRolePlural("CoverageTypes");
        association.setMinCardinality(2);
        association.setMaxCardinality(4);
        association.setDerivedUnion(true);
        association.setSubsettedDerivedUnion("BaseCoverageType");
        association.setAssociationType(AssociationType.AGGREGATION);

        Element el = association.toXml(newDocument());
        association = productType.newProductCmptTypeAssociation();
        association.initFromXml(el);

        assertEquals(AssociationType.AGGREGATION, association.getAssociationType());
        assertEquals("pack1.CoverageType", association.getTarget());
        assertEquals("CoverageType", association.getTargetRoleSingular());
        assertEquals("CoverageTypes", association.getTargetRolePlural());
        assertEquals(2, association.getMinCardinality());
        assertEquals(4, association.getMaxCardinality());
        assertTrue(association.isDerivedUnion());
        assertEquals("BaseCoverageType", association.getSubsettedDerivedUnion());
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer#initFromXml(org.w3c.dom.Element)}
     * .
     */
    @Test
    public void testInitFromXmlElement() {
        Element docEl = getTestDocument().getDocumentElement();
        Element el = XmlUtil.getElement(docEl, 0);
        association.initFromXml(el);
        assertEquals(AssociationType.AGGREGATION, association.getAssociationType());
        assertEquals("pack1.CoverageType", association.getTarget());
        assertEquals("CoverageType", association.getTargetRoleSingular());
        assertEquals("CoverageTypes", association.getTargetRolePlural());
        assertEquals(1, association.getMinCardinality());
        assertEquals(Integer.MAX_VALUE, association.getMaxCardinality());
        assertTrue(association.isDerivedUnion());
        assertEquals("BaseCoverageType", association.getSubsettedDerivedUnion());
        assertEquals("blabla", association.getDescriptionText(Locale.US));
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#findTarget(IIpsProject)
     * )} .
     */
    @Test
    public void testFindTarget() throws CoreException {
        association.setTarget("");
        assertNull(association.findTarget(ipsProject));

        association.setTarget("unknown");
        assertNull(association.findTarget(ipsProject));

        association.setTarget(coverageTypeType.getQualifiedName());
        assertEquals(coverageTypeType, association.findTarget(ipsProject));
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setTarget(java.lang.String)}
     * .
     */
    @Test
    public void testSetTarget() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_TARGET, association, "newTarget");
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setTargetRoleSingular(java.lang.String)}
     * .
     */
    @Test
    public void testSetTargetRoleSingular() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR, association, "newRole");
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setTargetRolePlural(java.lang.String)}
     * .
     */
    @Test
    public void testSetTargetRolePlural() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL, association, "newRoles");
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setMinCardinality(int)}
     * .
     */
    @Test
    public void testSetMinCardinality() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY, association, new Integer(42));
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setMaxCardinality(int)}
     * .
     */
    @Test
    public void testSetMaxCardinality() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY, association, new Integer(42));
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#isSubsetOfADerivedUnion()}
     * .
     */
    @Test
    public void testIsSubsetOfADerivedUnion() {
        association.setSubsettedDerivedUnion("");
        assertFalse(association.isSubsetOfADerivedUnion());
        association.setSubsettedDerivedUnion("someContainerRelation");
        assertTrue(association.isSubsetOfADerivedUnion());
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation#setSubsettedDerivedUnion(java.lang.String)}
     * .
     */
    @Test
    public void testSetSubsettedDerivedUnion() {
        super.testPropertyAccessReadWrite(ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.PROPERTY_SUBSETTED_DERIVED_UNION, association, "SomeUnion");
    }

}
