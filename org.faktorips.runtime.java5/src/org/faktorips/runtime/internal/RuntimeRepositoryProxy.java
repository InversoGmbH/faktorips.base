/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

import java.lang.reflect.Constructor;

import org.faktorips.runtime.DefaultCacheFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductDataProvider;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.test.IpsTestCaseBase;

/**
 * 
 * @author Jan Ortmann
 */
public class RuntimeRepositoryProxy extends AbstractTocBasedRuntimeRepository {

    private IProductDataProvider dataProvider;
    
    public final static RuntimeRepositoryProxy create(IProductDataProvider dataProvider) {
        return new RuntimeRepositoryProxy(dataProvider);
    }
    
    private RuntimeRepositoryProxy(IProductDataProvider dataProvider) {
        super("RuntimeRepositoryProxy", new DefaultCacheFactory());
        this.dataProvider = dataProvider;
        reload();
    }

    /**
     * {@inheritDoc}
     */
    protected AbstractReadonlyTableOfContents loadTableOfContents() {
        return dataProvider.loadToc();
    }
    
    /**
     * {@inheritDoc}
     */
    protected IProductComponent createProductCmpt(TocEntryObject tocEntry) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClass().getClassLoader());
        ProductComponent productCmpt;
        try {
            Constructor<?> constructor = implClass.getConstructor(new Class[] {IRuntimeRepository.class, String.class, String.class, String.class});
            productCmpt = (ProductComponent)constructor.newInstance(new Object[]{this, tocEntry.getIpsObjectId(), tocEntry.getKindId(), tocEntry.getVersionId()});
        } catch (Exception e) {
            throw new RuntimeException("Can't create product component instance for toc entry " + tocEntry, e);
        }
        return productCmpt;
    }

    /**
     * {@inheritDoc}
     */
    protected IProductComponentGeneration createProductCmptGeneration(TocEntryGeneration tocEntryGeneration) {
        IProductComponentGeneration productCmptGeneration = dataProvider.getProductCmptGeneration(tocEntryGeneration.getParent().getIpsObjectId(), tocEntryGeneration.getValidFrom());

        ProductComponent productCmpt = (ProductComponent)getProductComponent(tocEntryGeneration.getParent().getIpsObjectId());
        if (productCmpt==null) {
            throw new RuntimeException("Can't get product component for toc entry " + tocEntryGeneration);
        }
        ((ProductComponentGeneration)productCmptGeneration).setProductCmpt(productCmpt);
        
        return productCmptGeneration;
    }

    /**
     * {@inheritDoc}
     */
    protected ITable createTable(TocEntryObject tocEntry) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    protected IpsTestCaseBase createTestCase(TocEntryObject tocEntry, IRuntimeRepository runtimeRepository) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isModifiable() {
        return false;
    }

}
