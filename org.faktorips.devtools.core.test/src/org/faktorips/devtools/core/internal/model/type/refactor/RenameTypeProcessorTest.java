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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.refactor.IIpsRenameProcessor;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class RenameTypeProcessorTest extends RenameTypeMoveTypeTest {

    public void testCheckInitialConditionsValid() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getRenameRefactoring();
        RefactoringStatus status = refactoring.getProcessor().checkInitialConditions(new NullProgressMonitor());
        assertFalse(status.hasError());
    }

    public void testCheckInitialConditionsInvalid() throws CoreException {
        policyCmptType.setProductCmptType("abc");

        ProcessorBasedRefactoring refactoring = policyCmptType.getRenameRefactoring();
        RefactoringStatus status = refactoring.getProcessor().checkInitialConditions(new NullProgressMonitor());
        assertTrue(status.hasFatalError());
    }

    public void testCheckFinalConditionsValid() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getRenameRefactoring();
        IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)refactoring.getProcessor();
        renameProcessor.setNewName("test");
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertFalse(status.hasError());
    }

    public void testCheckFinalConditionsFileAlreadyExists() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getRenameRefactoring();
        IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)refactoring.getProcessor();
        renameProcessor.setNewName(PRODUCT_NAME);
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasFatalError());
    }

    public void testCheckFinalConditionsInvalidTypeName() throws CoreException {
        ProcessorBasedRefactoring refactoring = policyCmptType.getRenameRefactoring();
        IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)refactoring.getProcessor();
        renameProcessor.setNewName("$§§  $");
        RefactoringStatus status = refactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasFatalError());
    }

    public void testRenamePolicyCmptType() throws CoreException {
        String newElementName = "NewPolicy";
        performRenameRefactoring(policyCmptType, newElementName);
        String qualifiedNewName = PACKAGE + "." + newElementName;

        // Find the new policy component type.
        IIpsSrcFile ipsSrcFile = policyCmptType.getIpsPackageFragment().getIpsSrcFile(newElementName,
                policyCmptType.getIpsObjectType());
        assertTrue(ipsSrcFile.exists());
        IPolicyCmptType newPolicyCmptType = (IPolicyCmptType)ipsSrcFile.getIpsObject();
        assertEquals(newElementName, newPolicyCmptType.getName());

        // Check for product component type configuration update.
        assertEquals(qualifiedNewName, productCmptType.getPolicyCmptType());

        // Check for test parameter and test attribute update.
        assertEquals(qualifiedNewName, testPolicyCmptTypeParameter.getPolicyCmptType());
        assertEquals(qualifiedNewName, testAttribute.getPolicyCmptType());
        assertEquals(qualifiedNewName, testParameterChild1.getPolicyCmptType());
        assertEquals(qualifiedNewName, testParameterChild2.getPolicyCmptType());
        assertEquals(qualifiedNewName, testParameterChild3.getPolicyCmptType());

        // Check for method parameter update.
        assertEquals(Datatype.INTEGER.getQualifiedName(), policyMethod.getParameters()[0].getDatatype());
        assertEquals(qualifiedNewName, policyMethod.getParameters()[1].getDatatype());
        assertEquals(qualifiedNewName, productMethod.getParameters()[2].getDatatype());

        // Check for association update.
        assertEquals(qualifiedNewName, otherPolicyToPolicyAssociation.getTarget());
    }

    public void testRenameSuperPolicyCmptType() throws CoreException {
        String newElementName = "NewSuperPolicy";
        performRenameRefactoring(superPolicyCmptType, newElementName);

        // Check for test attribute update.
        assertEquals(newElementName, superTestAttribute.getPolicyCmptType());

        // Check for subtype update.
        assertEquals(newElementName, policyCmptType.getSupertype());
    }

    public void testRenameProductCmptType() throws CoreException {
        String newElementName = "NewProduct";
        performRenameRefactoring(productCmptType, newElementName);
        String qualifiedNewName = PACKAGE + "." + newElementName;

        // Find the new product component type.
        IIpsSrcFile ipsSrcFile = productCmptType.getIpsPackageFragment().getIpsSrcFile(newElementName,
                productCmptType.getIpsObjectType());
        assertTrue(ipsSrcFile.exists());
        IProductCmptType newProductCmptType = (IProductCmptType)ipsSrcFile.getIpsObject();
        assertEquals(newElementName, newProductCmptType.getName());

        // Check for policy component type configuration update.
        assertEquals(qualifiedNewName, policyCmptType.getProductCmptType());

        // Check for product component reference update.
        assertEquals(qualifiedNewName, productCmpt.getProductCmptType());

        // Check for method parameter update.
        assertEquals(Datatype.INTEGER.getQualifiedName(), policyMethod.getParameters()[0].getDatatype());
        assertEquals(qualifiedNewName, productMethod.getParameters()[1].getDatatype());
        assertEquals(qualifiedNewName, policyMethod.getParameters()[2].getDatatype());

        // Check for association update.
        assertEquals(qualifiedNewName, otherProductToProductAssociation.getTarget());
    }

    public void testRenameSuperProductCmptType() throws CoreException {
        String newElementName = "NewSuperProduct";
        performRenameRefactoring(superProductCmptType, newElementName);

        // Check for subtype update.
        assertEquals(newElementName, productCmptType.getSupertype());
    }

}
