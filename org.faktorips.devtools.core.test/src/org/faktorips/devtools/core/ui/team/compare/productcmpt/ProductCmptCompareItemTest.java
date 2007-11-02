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

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptLink;

public class ProductCmptCompareItemTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new ProductCmptCompareItemCreator();
    private IProductCmptGeneration generation1;
    private IProductCmptGeneration generation2;
    private IProductCmptGeneration generation3;
    private IIpsSrcFile srcFile;
    private IFile correspondingFile;
    
    private ProductCmptCompareItem compareItemRoot;
    private IProductCmpt product;
    private IIpsPackageFragmentRoot root;
    private IConfigElement configElement1;
    private IConfigElement configElement2;
    private IProductCmptLink relation1;
    private IProductCmptLink relation2;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject proj= (IpsProject)newIpsProject("TestProject");
        root = proj.getIpsPackageFragmentRoots()[0];
        product = newProductCmpt(root, "TestProductCmpt");
        IProductCmpt productReferenced = newProductCmpt(root, "TestProductCmptReferenced");
        
        GregorianCalendar calendar= new GregorianCalendar();
        generation1 = (IProductCmptGeneration) product.newGeneration(calendar);
        calendar= new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        generation2 = (IProductCmptGeneration) product.newGeneration(calendar);
        calendar= new GregorianCalendar();
        calendar.add(Calendar.MONTH, 2);
        generation3 = (IProductCmptGeneration) product.newGeneration(calendar);

        configElement1 = generation1.newConfigElement();
        configElement1.setPolicyCmptTypeAttribute("configElement1");    // set name to ensure sorting order
        configElement2 = generation1.newConfigElement();
        configElement2.setPolicyCmptTypeAttribute("configElement2");
        configElement2.setType(ConfigElementType.POLICY_ATTRIBUTE);
        configElement2.setValueSetType(ValueSetType.ENUM);
        relation1 = generation1.newLink(productReferenced.getQualifiedName());
        relation2 = generation1.newLink(productReferenced.getQualifiedName());
        
        srcFile = product.getIpsSrcFile();
        correspondingFile = srcFile.getCorrespondingFile();

        compareItemRoot = (ProductCmptCompareItem) structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.ProductCmptCompareItem.getChildren()'
     */
    public void testGetChildren() {
        Object[] children= compareItemRoot.getChildren();
        // Srcfile contains ProductComponent
        assertEquals(1, children.length);
        
        ProductCmptCompareItem compareItem= (ProductCmptCompareItem) children[0];
        children= compareItem.getChildren();
        // productcomponent contains 3 generations
        assertEquals(3, children.length);
        
        ProductCmptCompareItem compareItemGen= (ProductCmptCompareItem) children[0];
        children= compareItemGen.getChildren();
        // Generation has 2 ConfigElements and 2 Relations
        assertEquals(4, children.length);
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.ProductCmptCompareItem.getImage()'
     */
    public void testGetImage() {
        assertEquals(srcFile.getImage(), compareItemRoot.getImage());
        
        Object[] children= compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem= (ProductCmptCompareItem) children[0];
        assertEquals(product.getImage(), compareItem.getImage());
        
        children= compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1= (ProductCmptCompareItem) children[0];
        assertEquals(generation1.getImage(), compareItemGen1.getImage());
        ProductCmptCompareItem compareItemGen2= (ProductCmptCompareItem) children[1];
        assertEquals(generation2.getImage(), compareItemGen2.getImage());
        ProductCmptCompareItem compareItemGen3= (ProductCmptCompareItem) children[2];
        assertEquals(generation3.getImage(), compareItemGen3.getImage());
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.ProductCmptCompareItem.getType()'
     */
    public void testGetType() {
        assertEquals("ipsproduct", compareItemRoot.getType());
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.ProductCmptCompareItem.getParent()'
     */
    public void testGetParent() {
        assertNull(compareItemRoot.getParent());
        
        Object[] children= compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem= (ProductCmptCompareItem) children[0];
        assertNotNull(compareItem.getParent());
        assertEquals(compareItemRoot, compareItem.getParent());
        
        children= compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1= (ProductCmptCompareItem) children[0];
        ProductCmptCompareItem compareItemGen2= (ProductCmptCompareItem) children[1];
        ProductCmptCompareItem compareItemGen3= (ProductCmptCompareItem) children[2];
        
        assertNotNull(compareItemGen1.getParent());
        assertEquals(compareItem, compareItemGen1.getParent());
        assertNotNull(compareItemGen2.getParent());
        assertEquals(compareItem, compareItemGen2.getParent());
        assertNotNull(compareItemGen3.getParent());
        assertEquals(compareItem, compareItemGen3.getParent());
        
        // CompareItemComparator sorts Configelements above Relations  
        children= compareItemGen1.getChildren();
        ProductCmptCompareItem compareItemConfigElement1= (ProductCmptCompareItem) children[0];
        ProductCmptCompareItem compareItemConfigElement2= (ProductCmptCompareItem) children[1];
        ProductCmptCompareItem compareItemRelation1= (ProductCmptCompareItem) children[2];
        ProductCmptCompareItem compareItemRelation2= (ProductCmptCompareItem) children[3];

        assertNotNull(compareItemConfigElement1.getParent());
        assertEquals(compareItemGen1, compareItemConfigElement1.getParent());
        assertNotNull(compareItemConfigElement1.getParent());
        assertEquals(compareItemGen1, compareItemConfigElement2.getParent());
        assertNotNull(compareItemConfigElement1.getParent());
        assertEquals(compareItemGen1, compareItemRelation1.getParent());
        assertNotNull(compareItemConfigElement1.getParent());
        assertEquals(compareItemGen1, compareItemRelation2.getParent());
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.ProductCmptCompareItem.getIpsElement()'
     */
    public void testGetIpsElement(){
        assertEquals(srcFile, compareItemRoot.getIpsElement());
        
        Object[] children= compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem= (ProductCmptCompareItem) children[0];
        assertEquals(product, compareItem.getIpsElement());
        
        children= compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1= (ProductCmptCompareItem) children[0];
        ProductCmptCompareItem compareItemGen2= (ProductCmptCompareItem) children[1];
        ProductCmptCompareItem compareItemGen3= (ProductCmptCompareItem) children[2];
        
        assertEquals(generation1, compareItemGen1.getIpsElement());
        assertEquals(generation2, compareItemGen2.getIpsElement());
        assertEquals(generation3, compareItemGen3.getIpsElement());
        
        children= compareItemGen1.getChildren();
        ProductCmptCompareItem compareItemConfigElement1= (ProductCmptCompareItem) children[0];
        ProductCmptCompareItem compareItemConfigElement2= (ProductCmptCompareItem) children[1];
        ProductCmptCompareItem compareItemRelation1= (ProductCmptCompareItem) children[2];
        ProductCmptCompareItem compareItemRelation2= (ProductCmptCompareItem) children[3];

        assertEquals(configElement1, compareItemConfigElement1.getIpsElement());
        assertEquals(configElement2, compareItemConfigElement2.getIpsElement());
        assertEquals(relation1, compareItemRelation1.getIpsElement());
        assertEquals(relation2, compareItemRelation2.getIpsElement());
    }
    
    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.AbstractCompareItem.hashCode()'
     */
    public void testHashCode() {
        Object[] children= compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem= (ProductCmptCompareItem) children[0]; 
        children= compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1= (ProductCmptCompareItem) children[0];
        children= compareItemGen1.getChildren();
        ProductCmptCompareItem compareItemConfigElement1= (ProductCmptCompareItem) children[0];
        ProductCmptCompareItem compareItemRelation1= (ProductCmptCompareItem) children[2];
        
        assertEquals(compareItemRoot.getContentStringWithoutWhiteSpace().hashCode(), compareItemRoot.hashCode());
        assertEquals(compareItemGen1.getContentStringWithoutWhiteSpace().hashCode(), compareItemGen1.hashCode());
        assertEquals(compareItemConfigElement1.getContentStringWithoutWhiteSpace().hashCode(), compareItemConfigElement1.hashCode());
        assertEquals(compareItemRelation1.getContentStringWithoutWhiteSpace().hashCode(), compareItemRelation1.hashCode());
    }
}
