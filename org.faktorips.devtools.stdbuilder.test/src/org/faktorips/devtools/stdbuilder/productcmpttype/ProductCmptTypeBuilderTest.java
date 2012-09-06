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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.junit.Before;

public abstract class ProductCmptTypeBuilderTest extends AbstractStdBuilderTest {

    protected final static String PRODUCT_NAME = "ProductCmptType";

    protected final static String POLICY_NAME = "PolicyCmptType";

    protected GenProductCmptType genProductCmptType;

    protected IProductCmptType productCmptType;

    protected IPolicyCmptType policyCmptType;

    protected IType javaClassConfiguredPolicy;

    protected IType javaInterfaceConfiguredPolicy;

    protected IType javaClassGeneration;

    protected IType javaInterfaceGeneration;

    protected IType javaClass;

    protected IType javaInterface;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        productCmptType = newProductCmptType(ipsProject, PRODUCT_NAME);
        policyCmptType = newPolicyCmptType(ipsProject, POLICY_NAME);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        genProductCmptType = new GenProductCmptType(productCmptType,
                (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet());

        javaClassConfiguredPolicy = getGeneratedJavaClass(policyCmptType, false,
                builderSet.getBuilderByClass(PolicyCmptImplClassBuilder.class).get(0), POLICY_NAME);
        javaInterfaceConfiguredPolicy = getGeneratedJavaInterface(policyCmptType, false,
                builderSet.getBuilderByClass(PolicyCmptInterfaceBuilder.class).get(0), POLICY_NAME);

        IChangesOverTimeNamingConvention changesOverTimeNamingConvention = ipsProject
                .getChangesInTimeNamingConventionForGeneratedCode();
        javaClassGeneration = getGeneratedJavaClass(productCmptType, false,
                builderSet.getBuilderByClass(ProductCmptGenImplClassBuilder.class).get(0), PRODUCT_NAME
                        + changesOverTimeNamingConvention.getGenerationConceptNameAbbreviation());
        javaInterfaceGeneration = getGeneratedJavaInterface(productCmptType, false,
                builderSet.getBuilderByClass(ProductCmptGenInterfaceBuilder.class).get(0), PRODUCT_NAME
                        + changesOverTimeNamingConvention.getGenerationConceptNameAbbreviation());

        javaClass = getGeneratedJavaClass(productCmptType, false,
                builderSet.getBuilderByClass(ProductCmptImplClassBuilder.class).get(0), PRODUCT_NAME);
        javaInterface = getGeneratedJavaInterface(productCmptType, false,
                builderSet.getBuilderByClass(ProductCmptInterfaceBuilder.class).get(0), PRODUCT_NAME);
    }
}
