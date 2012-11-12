/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
import org.faktorips.util.LocalizedStringsSet;

public class ProductCmptGenerationClassBuilder extends ProductClassBuilder<XProductCmptGenerationClass> {

    private final IJavaClassNameProvider javaClassNameProvider;

    public ProductCmptGenerationClassBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet,
            GeneratorModelContext modelContext, ModelService modelService) {
        super(interfaceBuilder, builderSet, modelContext, modelService, new LocalizedStringsSet(
                ProductCmptGenerationClassBuilder.class));
        javaClassNameProvider = XProductCmptGenerationClass.createProductCmptGenJavaClassNaming(
                modelContext.isGeneratePublishedInterfaces(), getLanguageUsedInGeneratedSourceCode());
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.PRODUCT_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    protected Class<XProductCmptGenerationClass> getGeneratorModelNodeClass() {
        return XProductCmptGenerationClass.class;
    }

    @Override
    public String getTemplate() {
        if (isInterfaceBuilder()) {
            return "org::faktorips::devtools::stdbuilder::xpand::productcmpt::template::ProductComponentGenInterface::main";
        } else {
            return "org::faktorips::devtools::stdbuilder::xpand::productcmpt::template::ProductComponentGen::main";
        }
    }

    @Override
    protected boolean generatesInterface() {
        return isInterfaceBuilder();
    }

}