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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 *
 * @author Jan Ortmann
 */
public class ArchiveIpsPackageFragmentRootTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IpsArchiveEntry entry;
    private IFile archiveFile;
    private ArchiveIpsPackageFragmentRoot root;
    private IPolicyCmptType type;

    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        type = newPolicyCmptType(archiveProject, "motor.Policy");
        type.getIpsSrcFile().save(true, null);
        newPolicyCmptTypeWithoutProductCmptType(archiveProject, "motor.collision.CollisionCoverage").getIpsSrcFile().save(true, null);
        newProductCmpt(archiveProject, "motor.MotorProduct").getIpsSrcFile().save(true, null);

        project = newIpsProject();
        archiveFile = project.getProject().getFile("test.ipsar");

        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = project.getIpsObjectPath();
        entry = (IpsArchiveEntry)path.newArchiveEntry(archiveFile.getProjectRelativePath());
        project.setIpsObjectPath(path);
        root = (ArchiveIpsPackageFragmentRoot)project.getIpsPackageFragmentRoots()[1];
    }
    
    public void testExists_ArchiveInSameProject() throws CoreException {
        assertTrue(root.exists());
        archiveFile.delete(true, null);
        assertFalse(root.exists());
    }

    public void testExists_ArchiveInWorkspaceButDifferentProject() throws CoreException {
        IIpsProject project2 = newIpsProject("Project2");
        IIpsObjectPath path2 = project2.getIpsObjectPath();
        entry = (IpsArchiveEntry)path2.newArchiveEntry(archiveFile.getFullPath());
        project2.setIpsObjectPath(path2);
        root = (ArchiveIpsPackageFragmentRoot)project2.getIpsPackageFragmentRoots()[1];
        
        assertTrue(root.exists());
        archiveFile.delete(true, null);
        assertFalse(root.exists());
    }

    public void testExists_ArchiveOutsideWorkspace() throws Exception {

        File externalArchiveFile = File.createTempFile("externalArchiveFile", ".ipsar");
        externalArchiveFile.deleteOnExit();
        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(project, externalArchiveFile);
        ResourcesPlugin.getWorkspace().run(op, null);
        IPath externalArchivePath = new Path(externalArchiveFile.getAbsolutePath());
        
        IIpsObjectPath path = project.getIpsObjectPath();
        entry = (IpsArchiveEntry)path.newArchiveEntry(externalArchivePath);
        project.setIpsObjectPath(path);
        root = (ArchiveIpsPackageFragmentRoot)project.getIpsPackageFragmentRoots()[2];
        
        assertTrue(root.exists());
        
        externalArchiveFile.delete();
        assertFalse(root.exists());
    }

    public void testGetIpsObjectPathEntry() throws CoreException {
        assertEquals(entry.getArchivePath(), root.getIpsArchive().getArchivePath());
    }

    public void testGetParent() {
        assertEquals(project, root.getParent());
    }

    public void testGetIpsPackageFragments() throws CoreException {
        IIpsPackageFragment[] packs = root.getIpsPackageFragments();
        assertEquals(2, packs.length);
        assertEquals("motor", packs[0].getName());
        assertEquals("motor.collision", packs[1].getName());
    }

    public void testGetNonIpsResources() throws CoreException {
        IResource[] res = root.getNonIpsResources();
        assertEquals(0, res.length);
    }

    public void testGetCorrespondingResource() {
        assertEquals(archiveFile, root.getCorrespondingResource());
    }

    public void testGetEnclosingResource() {
        assertEquals(archiveFile, root.getEnclosingResource());
    }

    public void testGetSortedIpsPackageFragments() throws CoreException {
        IIpsPackageFragment[] packs = root.getSortedIpsPackageFragments();
        assertEquals(2, packs.length);
        assertEquals("motor", packs[0].getName());
        assertEquals("motor.collision", packs[1].getName());
    }

    public void testFindIpsSourceFiles() throws CoreException{
        List result = new ArrayList();
        root.findIpsSourceFiles(IpsObjectType.POLICY_CMPT_TYPE, result);
        assertEquals(2, result.size());
        List qualifiedNameTypes = new ArrayList();
        for (Iterator it = result.iterator(); it.hasNext();) {
            IIpsSrcFile pcTypeSrcFile = (IIpsSrcFile)it.next();
            qualifiedNameTypes.add(pcTypeSrcFile.getQualifiedNameType());
        }
        assertTrue(qualifiedNameTypes.contains(new QualifiedNameType("motor.Policy", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qualifiedNameTypes.contains(new QualifiedNameType("motor.collision.CollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE)));
        
        result = new ArrayList();
        root.findIpsSourceFiles(IpsObjectType.PRODUCT_CMPT_TYPE_V2, result);
        assertEquals(1, result.size());

        result = new ArrayList();
        root.findIpsSourceFiles(IpsObjectType.PRODUCT_CMPT, result);
        assertEquals(1, result.size());

    }
}
