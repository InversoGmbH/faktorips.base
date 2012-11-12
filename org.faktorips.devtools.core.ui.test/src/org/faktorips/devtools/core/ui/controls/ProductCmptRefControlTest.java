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

package org.faktorips.devtools.core.ui.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.junit.Test;

public class ProductCmptRefControlTest extends AbstractIpsPluginTest {

    @Test
    public void testGetSrcFiles_SingleProject() throws CoreException {
        IIpsProject project = newIpsProject("BaseProject");

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");

        ProductCmpt productCmpt = newProductCmpt(project, "Product");
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());
        productCmpt.newGeneration();

        ProductCmpt secondProductCmpt = newProductCmpt(project, "SecondProduct");
        secondProductCmpt.setProductCmptType(productCmptType.getQualifiedName());
        secondProductCmpt.newGeneration();

        ProductCmptRefControl productCmptRefControl = new ProductCmptRefControl(project, new Shell(), new UIToolkit(
                null));

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertTrue(list.contains(productCmpt.getIpsSrcFile()));
        assertTrue(list.contains(secondProductCmpt.getIpsSrcFile()));

        assertEquals(2, list.size());
    }

    @Test
    public void testGetSrcFilesExclude() throws CoreException {
        IIpsProject project = newIpsProject("BaseProject");

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");

        ProductCmpt productCmpt = newProductCmpt(project, "Product");
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());
        productCmpt.newGeneration();

        ProductCmpt secondProductCmpt = newProductCmpt(project, "SecondProduct");
        secondProductCmpt.setProductCmptType(productCmptType.getQualifiedName());
        secondProductCmpt.newGeneration();

        ProductCmptRefControl productCmptRefControl = new ProductCmptRefControl(project, new Shell(), new UIToolkit(
                null));

        productCmptRefControl.setProductCmptsToExclude(new IProductCmpt[] { productCmpt });

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertFalse(list.contains(productCmpt.getIpsSrcFile()));
        assertTrue(list.contains(secondProductCmpt.getIpsSrcFile()));

        assertEquals(1, list.size());
    }

    @Test
    public void testGetSrcFiles_MultiProject() throws CoreException {
        IIpsProject project = newIpsProject("BaseProject");
        IIpsProject subProject = newIpsProject("SubProject");
        IIpsProject anotherProject = newIpsProject("AnotherProject");

        IIpsObjectPath subIpsObjectPath = subProject.getIpsObjectPath();
        subIpsObjectPath.newIpsProjectRefEntry(project);
        subProject.setIpsObjectPath(subIpsObjectPath);

        IIpsObjectPath anotherIpsObjectPath = anotherProject.getIpsObjectPath();
        anotherIpsObjectPath.newIpsProjectRefEntry(project);
        anotherProject.setIpsObjectPath(anotherIpsObjectPath);

        List<IIpsProject> projects = Arrays.asList(project, subProject);

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");

        ProductCmpt productCmpt = newProductCmpt(project, "Product");
        productCmpt.setProductCmptType(productCmptType.getQualifiedName());
        productCmpt.newGeneration();
        IIpsSrcFile productIpsSrcFile = productCmpt.getIpsSrcFile();

        ProductCmpt subProductCmpt = newProductCmpt(subProject, "SubProduct");
        subProductCmpt.setProductCmptType(productCmptType.getQualifiedName());
        subProductCmpt.newGeneration();
        IIpsSrcFile subProductIpsSrcFile = subProductCmpt.getIpsSrcFile();

        ProductCmpt anotherProductCmpt = newProductCmpt(anotherProject, "AnotherProduct");
        anotherProductCmpt.setProductCmptType(productCmptType.getQualifiedName());
        anotherProductCmpt.newGeneration();
        IIpsSrcFile anotherProductIpsSrcFile = anotherProductCmpt.getIpsSrcFile();

        ProductCmptRefControl productCmptRefControl = new ProductCmptRefControl(projects, new Shell(), new UIToolkit(
                null));

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertTrue(list.contains(productIpsSrcFile));
        assertTrue(list.contains(subProductIpsSrcFile));
        assertFalse(list.contains(anotherProductIpsSrcFile));

        assertEquals(2, list.size());
    }
}