/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.core.builder.DefaultBuilderSet;

public class ProductCmptGenImplClassBuilderTest extends ProductCmptTypeBuilderTest {

    private ProductCmptGenImplClassBuilder builder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        builder = new ProductCmptGenImplClassBuilder(builderSet,
                DefaultBuilderSet.KIND_PRODUCT_CMPT_TYPE_GENERATION_IMPL);
    }

    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(productCmptType);
        assertTrue(generatedJavaElements.contains(javaClassGeneration));
    }

}