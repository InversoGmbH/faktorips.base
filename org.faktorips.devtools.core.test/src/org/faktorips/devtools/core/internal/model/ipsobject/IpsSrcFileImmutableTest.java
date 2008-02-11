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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

public class IpsSrcFileImmutableTest extends AbstractIpsPluginTest {

    private IpsSrcFileImmutable srcFileImmutable;
    private IProductCmpt product;
    private IProductCmpt productImmutable;
    private IFile file;

    protected void setUp() throws Exception {
        super.setUp();
        
        // create srcfile with contents
        IIpsProject proj= (IpsProject)newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = proj.getIpsPackageFragmentRoots()[0];
        product = newProductCmpt(root, "TestProductCmpt");
        IProductCmptGeneration generation= (IProductCmptGeneration) product.newGeneration();
        generation.newConfigElement();
        generation.newLink("");
        
        // save contents
        product.getIpsSrcFile().save(true, null);
        
        file = product.getIpsSrcFile().getCorrespondingFile();
        srcFileImmutable = new IpsSrcFileImmutable("TestSrcFileImmutable.ipsproduct", file.getContents());
        productImmutable = (IProductCmpt) srcFileImmutable.getIpsObject();
    }

    /*
     * Test method for 'org.faktorips.devtools.core.internal.model.IpsSrcFileImmutable.getIpsObjectType()'
     */
    public void testGetIpsObjectType() {
        assertEquals(IpsObjectType.getTypeForExtension("ipsproduct"), srcFileImmutable.getIpsObjectType());
    }

    /*
     * Test method for 'org.faktorips.devtools.core.internal.model.IpsSrcFileImmutable.getCorrespondingResource()'
     */
    public void testGetCorrespondingResource() {
        assertNull(srcFileImmutable.getCorrespondingResource());
    }

    /*
     * Test method for 'org.faktorips.devtools.core.internal.model.IpsSrcFileImmutable.getCorrespondingFile()'
     */
    public void testGetCorrespondingFile() throws CoreException {
        assertNull(srcFileImmutable.getCorrespondingFile());
    }

    /*
     * Test method for 'org.faktorips.devtools.core.internal.model.IpsSrcFileImmutable.isDirty()'
     */
    public void testIsDirty() {
        assertFalse(srcFileImmutable.isDirty());
        productImmutable.newGeneration();
        assertFalse(srcFileImmutable.isDirty());
    }

    /*
     * Test method for 'org.faktorips.devtools.core.internal.model.IpsSrcFileImmutable.getIpsObject()'
     */
    public void testGetIpsObject() throws CoreException {
        IpsSrcFileImmutable srcFileImm2= new IpsSrcFileImmutable("TestSrcFileImmutable.ipsproduct", file.getContents());
        IProductCmpt prodImm2= (IProductCmpt) srcFileImm2.getIpsObject();
        assertEquals(productImmutable.getQualifiedName(), prodImm2.getQualifiedName());

        // compare generations
        List gens1= Arrays.asList(productImmutable.getGenerationsOrderedByValidDate());
        List gens2= Arrays.asList(prodImm2.getGenerationsOrderedByValidDate());
        assertEquals(gens1.size(), gens2.size());
        Iterator iterator1= gens1.iterator();
        Iterator iterator2= gens2.iterator();
        while(iterator1.hasNext()){
            IProductCmptGeneration gen1= (IProductCmptGeneration) iterator1.next();
            IProductCmptGeneration gen2= (IProductCmptGeneration) iterator2.next();
            assertEquals(gen1.getValidFrom(), gen2.getValidFrom());
        }
        
        IProductCmptGeneration generation1= (IProductCmptGeneration) productImmutable.getFirstGeneration();
        IProductCmptGeneration generation2= (IProductCmptGeneration) prodImm2.getFirstGeneration();
        
        // compare configelements in first generation
        List configElements1= Arrays.asList(generation1.getConfigElements());
        List configElements2= Arrays.asList(generation2.getConfigElements());
        assertEquals(configElements1.size(), configElements1.size());
        iterator1= configElements1.iterator();
        iterator2= configElements2.iterator();
        while(iterator1.hasNext()){
            IConfigElement configElement1= (IConfigElement) iterator1.next();
            IConfigElement configElement2= (IConfigElement) iterator2.next();
            assertEquals(configElement1.getId(), configElement2.getId());
            assertEquals(configElement1.getValue(), configElement2.getValue());
        }

        // compare relations in first generation
        List relations1= Arrays.asList(generation1.getLinks());
        List relations2= Arrays.asList(generation2.getLinks());
        assertEquals(relations1.size(), relations2.size());
        iterator1= relations1.iterator();
        iterator2= relations2.iterator();
        while(iterator1.hasNext()){
            IProductCmptLink relation1= (IProductCmptLink) iterator1.next();
            IProductCmptLink relation2= (IProductCmptLink) iterator2.next();
            assertEquals(relation1.getId(), relation2.getId());
            assertEquals(relation1.getTarget(), relation2.getTarget());
        }
    }
    
    /*
     * Test method for 'org.faktorips.devtools.core.internal.model.IpsSrcFileImmutable.save()'
     */
    public void testSave() throws CoreException {
        productImmutable.newGeneration();
        srcFileImmutable.save(true, null);
        
        // nothing was saved
        IProductCmpt prodImm2= (IProductCmpt) new IpsSrcFileImmutable("ProdImm2.ipsproduct", file.getContents()).getIpsObject();
        assertEquals(1, prodImm2.getNumOfGenerations());
    }

    /*
     * Test method for 'org.faktorips.devtools.core.internal.model.IpsSrcFileImmutable.isMutable()'
     */
    public void testIsMutable() {
        assertFalse(srcFileImmutable.isMutable());
    }

    /*
     * Test method for 'org.faktorips.devtools.core.internal.model.IpsSrcFileImmutable.isHistoric()'
     */
    public void testIsHistoric() throws CoreException {
        assertTrue(srcFileImmutable.isHistoric());
    }

    public void testIsContentParsable() throws CoreException {
        IpsSrcFileImmutable srcFile= new IpsSrcFileImmutable("TestSrcFileImmutable.ipsproduct", file.getContents());
        assertTrue(srcFile.isContentParsable());
        
        srcFile = new IpsSrcFileImmutable("Test.ipsproduct", new ByteArrayInputStream(new byte[100]));
        assertFalse(srcFile.isContentParsable());
    }
    
    public void testGetQualifiedNameType() {
        IpsSrcFileImmutable srcFile = new IpsSrcFileImmutable("Test.ipsproduct", new ByteArrayInputStream(new byte[100]));
        QualifiedNameType qnt = srcFile.getQualifiedNameType();
        assertEquals(IpsObjectType.PRODUCT_CMPT, qnt.getIpsObjectType());
        assertEquals("Test", qnt.getUnqualifiedName());
        assertEquals("", qnt.getPackageName());
        assertEquals(IpsObjectType.PRODUCT_CMPT, qnt.getIpsObjectType());
    }
    
}
