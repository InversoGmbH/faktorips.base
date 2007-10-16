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

package org.faktorips.devtools.core.ui.actions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPartState;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.xml.sax.SAXException;


/**
 * Test for IpsCutAction.
 * 
 * @author Thorsten Guenther
 */
public class IpsPasteActionTest extends AbstractIpsPluginTest {
    
    IIpsProject project;
	IpsCutAction cutAction;
    IpsPasteAction pasteAction;
	IAttribute attribute;
	IPolicyCmptType pcType;
    IPolicyCmptType pcType2;
    IIpsPackageFragment pack;
    IIpsPackageFragmentRoot root;
    IFolder folder;
    
    protected void setUp() throws Exception {
        super.setUp();

        project = this.newIpsProject("TestProject");
        root = project.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);

        pcType = newPolicyCmptTypeWithoutProductCmptType(project, "products.folder.TestPolicy");
        attribute = pcType.newAttribute();

        pcType2 = newPolicyCmptTypeWithoutProductCmptType(project, "products.folder.TestPolicy2");

        IFile archiveFile = createIpsArchiveFile(root);
        IIpsObjectPath objectPath = project.getIpsObjectPath();
        objectPath.newArchiveEntry(archiveFile);
        project.setIpsObjectPath(objectPath);
        
        folder = project.getProject().getFolder("testFolder");
        folder.create(true, true, null);
    }
    
    private IFile createIpsArchiveFile(IIpsPackageFragmentRoot targetRoot) throws Exception{
        IIpsProject tempProject = this.newIpsProject("TempProject");
        newPolicyCmptTypeWithoutProductCmptType(tempProject, "test.PolicyInArchive1");
        newPolicyCmptTypeWithoutProductCmptType(tempProject, "test.PolicyInArchive2");
        
        IIpsPackageFragmentRoot tempRoot = tempProject.getSourceIpsPackageFragmentRoots()[0];
        IFile archiveFile = targetRoot.getIpsProject().getProject().getFile("test.ipsar");
        createArchive(tempRoot, archiveFile);
        
        tempProject.getProject().delete(true, true, null);
        return archiveFile;
    }
    
    public void testRun() throws CoreException, SAXException, IOException, TransformerException {
        cutAction = newIpsCutAction(attribute);
        pasteAction = newIpsPasteAction(pcType);
        
        String old = new IpsObjectPartState(attribute).toString();
        assertEquals(1, pcType.getNumOfAttributes());
        cutAction.run();
        assertEquals(0, pcType.getNumOfAttributes());
        pasteAction.run();
        assertEquals(1, pcType.getNumOfAttributes());
        assertEquals(old, new IpsObjectPartState(pcType.getAttributes()[0]).toString());
        
        IpsCopyAction copyAction = newIpsCopyAction(pack);
        pasteAction = newIpsPasteAction(root);
        copyAction.run();
        assertEquals(1, root.getDefaultIpsPackageFragment().getChildIpsPackageFragments().length);
        pasteAction.run();
        assertEquals(2, root.getDefaultIpsPackageFragment().getChildIpsPackageFragments().length);
    }
    
    //
    // Tests from package fragments and ips elements
    //
    
    public void testCopyPasteIpsObject2IpsPackageFragment() throws CoreException{
        IIpsPackageFragment fragment = root.createPackageFragment("testTarget", true, null);
        
        newIpsCopyAction(pcType).run();
        assertEquals(0, fragment.getChildren().length);
        
        newIpsPasteAction(fragment).run();
        assertEquals(1, fragment.getChildren().length);
        
        newIpsCopyAction(new IIpsElement[]{pcType, pcType2}).run();
        newIpsPasteAction(fragment).run();
        assertEquals(3, fragment.getChildren().length);
    }
    
    public void testCopyPasteIpsPackageFragment2IpsPackageFragment() throws CoreException{
        IIpsPackageFragment fragment = root.createPackageFragment("testTarget", true, null);
        
        newIpsCopyAction(pack).run();
        assertEquals(0, fragment.getChildren().length);
        
        newIpsPasteAction(fragment).run();
        assertEquals(0, fragment.getChildren().length);
        assertEquals(1, fragment.getChildIpsPackageFragments().length);
        
        IIpsPackageFragment newFragment = fragment.getChildIpsPackageFragments()[0];
        assertEquals(2, newFragment.getChildren().length);
    }
    
    public void testCopyPasteIpsObject2Folder() throws CoreException{
        newIpsCopyAction(pcType).run();
        assertEquals(0, folder.members().length);
        
        newIpsPasteAction(folder).run();
        assertEquals(1, folder.members().length);
        
        newIpsCopyAction(new IIpsElement[]{pcType, pcType2}).run();
        newIpsPasteAction(folder).run();
        assertEquals(3, folder.members().length);
    }    
    
    public void testCopyPasteIpsPackageFragment2Folder() throws CoreException{
        newIpsCopyAction(pack).run();
        assertEquals(0, folder.members().length);
        
        newIpsPasteAction(folder).run();
        assertEquals(1, folder.members().length);
        assertEquals(2, ((IFolder)folder.members()[0]).members().length);
    }    
    
    //
    // Tests copy from ips archives
    //
    
    public void testCopyPasteIpsObjectFromArchive2IpsPackageFragment() throws CoreException{
        IIpsPackageFragment fragment = root.createPackageFragment("test", true, null);
        
        IIpsObject obj1 = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "test.PolicyInArchive1");
        IIpsObject obj2 = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "test.PolicyInArchive2");
        
        newIpsCopyAction(obj1).run();
        assertEquals(0, fragment.getChildren().length);
        
        newIpsPasteAction(fragment).run();
        assertEquals(1, fragment.getChildren().length);
        
        newIpsCopyAction(new IIpsElement[]{obj1, obj2}).run();
        newIpsPasteAction(fragment).run();
        assertEquals(3, fragment.getChildren().length);
    }
    
    public void testCopyPasteIpsPackageFragmentFromArchive2IpsPackageFragment() throws CoreException{
        IIpsPackageFragment fragment = root.createPackageFragment("test", true, null);
        
        IIpsPackageFragmentRoot root = project.findIpsPackageFragmentRoot("test.ipsar");
        IIpsPackageFragment packageFrgmt = root.getIpsPackageFragment("test");
        
        newIpsCopyAction(packageFrgmt).run();
        assertEquals(0, fragment.getChildren().length);
        
        newIpsPasteAction(fragment).run();
        assertEquals(0, fragment.getChildren().length);
        assertEquals(1, fragment.getChildIpsPackageFragments().length);
        assertEquals(2, fragment.getChildIpsPackageFragments()[0].getChildren().length);
    }
    
    public void testCopyPasteIpsObjectFromArchive2Folder() throws CoreException{
        IIpsObject obj1 = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "test.PolicyInArchive1");
        IIpsObject obj2 = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "test.PolicyInArchive2");
        
        newIpsCopyAction(obj1).run();
        assertEquals(0, folder.members().length);
        
        newIpsPasteAction(folder).run();
        assertEquals(1, folder.members().length);
        
        newIpsCopyAction(new IIpsElement[]{obj1, obj2}).run();
        newIpsPasteAction(folder).run();
        assertEquals(3, folder.members().length);
    }
    
    public void testCopyPasteIpsPackageFragmentFromArchive2Folder() throws CoreException{
        IIpsPackageFragmentRoot root = project.findIpsPackageFragmentRoot("test.ipsar");
        IIpsPackageFragment packageFrgmt = root.getIpsPackageFragment("test");
        
        newIpsCopyAction(packageFrgmt).run();
        assertEquals(0, folder.members().length);
        
        newIpsPasteAction(folder).run();
        assertEquals(1, folder.members().length);
        assertEquals(2, ((IFolder)folder.members()[0]).members().length);
    }    
    
    public void testCopyPasteUnknown() throws Exception {
        IIpsPackageFragment destFragment = root.createPackageFragment("testTarget", true, null);
        
        // create source file
        String folderPath = folder.getRawLocation().toOSString();
        File file = new File(folderPath + "/unknown.xyz");
        assertTrue(file.createNewFile());
        FileWriter writer = new FileWriter(file);
        writer.write("dummy test file");
        writer.close();
        
        // add source file to clipboard
        Clipboard clipboard = new Clipboard(IpsPlugin.getDefault()
                .getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay());
        FileTransfer fileTransfer = FileTransfer.getInstance();
        Transfer[] transfers = new Transfer[]{fileTransfer};
        Object[] data = new Object[]{new String[]{file.getAbsolutePath()}};
        clipboard.setContents(data, transfers);

        // paste clipboard
        newIpsPasteAction(destFragment).run();
        assertEquals(0, destFragment.getChildren().length);
        clipboard.dispose();
        
        IFolder destFolder = (IFolder)destFragment.getEnclosingResource();
        assertEquals(1, destFolder.members().length);
    }  
    
    private IpsCopyAction newIpsCopyAction(Object selection) {
        return new IpsCopyAction(new TestSelectionProvider(selection), IpsPlugin.getDefault().getWorkbench()
                .getActiveWorkbenchWindow().getShell());
    }
    
    private IpsPasteAction newIpsPasteAction(Object selection) {
        IpsPasteAction pasteAction = new IpsPasteAction(new TestSelectionProvider(selection), IpsPlugin.getDefault()
                .getWorkbench().getActiveWorkbenchWindow().getShell());
        pasteAction.setForceUseNameSuggestionIfFileExists(true);
        return pasteAction;
    }
    
    private IpsCutAction newIpsCutAction(Object selection) {
        return new IpsCutAction(new TestSelectionProvider(selection), IpsPlugin.getDefault().getWorkbench()
                .getActiveWorkbenchWindow().getShell());
    }
    
    private class TestSelectionProvider implements ISelectionProvider {
        Object selected;
        
        public TestSelectionProvider(Object selected) {
            this.selected = selected; 
        }
        
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
		}

		public ISelection getSelection() {
			if (selected instanceof IIpsElement[]){
                return new StructuredSelection(Arrays.asList((IIpsElement[])selected));
            }
			return new StructuredSelection(selected);
		}

		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		}

		public void setSelection(ISelection selection) {
		}    	
    }
}
