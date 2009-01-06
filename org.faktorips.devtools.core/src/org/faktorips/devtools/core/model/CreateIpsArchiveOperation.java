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

package org.faktorips.devtools.core.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * An operation to create an ips archive.
 * 
 * @author Jan Ortmann
 */
public class CreateIpsArchiveOperation implements IWorkspaceRunnable {

    private IIpsPackageFragmentRoot[] roots;
    private File archive;
    
    private boolean inclJavaSources;
    private boolean inclJavaBinaries;
    private HashSet handledRootFolders = new HashSet();
    private HashMap handledEntries = new HashMap(1000);
    
    /**
     * Creates a new operation to create an ips archive. From the given project the content from all source folders
     * are packed into the new archive.
     * 
     * @throws CoreException
     */
    public CreateIpsArchiveOperation(
            IIpsProject projectToArchive, 
            File archive) throws CoreException {
        this.archive = archive;
        List rootsInt = new ArrayList();
        IIpsPackageFragmentRoot[] candidateRoots = projectToArchive.getIpsPackageFragmentRoots();
        for (int i = 0; i < candidateRoots.length; i++) {
            if (candidateRoots[i].isBasedOnSourceFolder()) {
                rootsInt.add(candidateRoots[i]);
            }
        }
        roots = (IIpsPackageFragmentRoot[])rootsInt.toArray(new IIpsPackageFragmentRoot[rootsInt.size()]);
    }

    public CreateIpsArchiveOperation(IIpsPackageFragmentRoot rootToArchive, File archive) throws CoreException {
        this(new IIpsPackageFragmentRoot[]{rootToArchive}, archive);
    }

    public CreateIpsArchiveOperation(IIpsPackageFragmentRoot[] rootsToArchive, File archive) {
        this.roots = rootsToArchive;
        this.archive = archive;
    }
    
    /**
     * {@inheritDoc}
     */
    public void run(IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        monitor.beginTask(Messages.CreateIpsArchiveOperation_Task_CreateArchive, getWorkload());
        
        IFile workspaceFile = getWorkspaceFile(archive);
        if (workspaceFile != null && workspaceFile.getLocalTimeStamp()==archive.lastModified()) {
            try {
                // windows file system does not return milliseconde, only seconds
                // if the cached timestamp is equal to the file's timestamp on disk,
                // we wait for 2 seconds before creating the file. After that time we
                // are sure that the file on disk gets a differnt time stamp when writing
                // the file. This hack has to be done, because we write using java.io.OutputStream
                // (because we have to use the JarOutputStream to zip) and therefore have to
                // call refreshLocal() afterwards. RefreshLocal refreshes only if we have a
                // different time stamp.
                Thread.sleep(2000);   
            } catch (InterruptedException e) {
                throw new CoreException(new IpsStatus(e));
            }
        }
        
        JarOutputStream os;
        try {
            os = new JarOutputStream(new FileOutputStream(archive));
        }
        catch (IOException e) {
            throw new CoreException(new IpsStatus("Error opening output stream for jar file " + archive, e)); //$NON-NLS-1$
        }
        Properties ipsObjectsProperties = new Properties();
        for (int i = 0; i < roots.length; i++) {
            addToArchive(roots[i], os, ipsObjectsProperties, monitor);
        }
        createIpsObjectsPropertiesEntry(os, ipsObjectsProperties);
        try {
            os.close();
        }
        catch (Exception e) {
            throw new CoreException(new IpsStatus("Error closing output stream for jar file " + archive, e)); //$NON-NLS-1$
        }
        
        refreshInWorkspaceIfNecessary();

        monitor.done();
    }

    /*
     * If the file exists in the workspace then refresh it.
     */
    private void refreshInWorkspaceIfNecessary() throws CoreException {
        IFile fileInWorkspace = getWorkspaceFile(archive);
        if (fileInWorkspace == null){
            // nothing to do, because the file dosn't exists in the workspace
            return;
        }
        if (fileInWorkspace.exists()){
            fileInWorkspace.refreshLocal(IResource.DEPTH_ZERO, null);
        }
        // refresh parent, thus the file is new then the file will be visible in the workspace
        IContainer parent = fileInWorkspace.getParent();
        if (parent != null){
            parent.refreshLocal(IResource.DEPTH_ONE, null);
        }
    }

    /*
     * Search and return the given file in the workspace, if the file isn't in the workspace
     * return <code>null</code>.
     */
    private IFile getWorkspaceFile(File file){
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (int i = 0; i < projects.length; i++) {
            IPath projectPath = projects[i].getLocation();
            IPath filePath = new Path(archive.getAbsolutePath());
            if (projectPath.isPrefixOf(filePath)){
                IPath filePathInProject = filePath.removeFirstSegments(filePath.matchingFirstSegments(projectPath));
                return projects[i].getFile(filePathInProject);
            }
        }
        return null;
    }
    
    private void addToArchive(IIpsPackageFragmentRoot root, JarOutputStream os, Properties ipsObjectsProperties, IProgressMonitor monitor) throws CoreException {
        IIpsPackageFragment[] packs = root.getIpsPackageFragments();
        for (int i = 0; i < packs.length; i++) {
            addToArchive(packs[i], os, ipsObjectsProperties, monitor);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
        }
        addJavaFiles(root, os, monitor);
    }
    
    private void addToArchive(IIpsPackageFragment pack, JarOutputStream os, Properties ipsObjectsProperties, IProgressMonitor monitor) throws CoreException {
        IIpsElement[] elements = pack.getChildren();
        for (int i = 0; i < elements.length; i++) {
            addToArchive((IIpsSrcFile)elements[i], os, ipsObjectsProperties);
            monitor.worked(1);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
        }
    }
    
    /*
     * Check if the archive contains the given entry specified by the name, e.g. objects which were
     * copied from the src folder to the bin folder
     */
    private boolean isDuplicateEnty(String entryName){
        if (handledEntries.containsKey(entryName)){
            return true;
        }
        handledEntries.put(entryName, entryName);
        return false;
    }
    
    private void addToArchive(IIpsSrcFile file, JarOutputStream os, Properties ipsObjectsProperties) throws CoreException {
        InputStream content = file.getContentFromEnclosingResource();
        String entryName = IIpsArchive.IPSOBJECTS_FOLDER + IPath.SEPARATOR + file.getQualifiedNameType().toPath().toString();
        if (isDuplicateEnty(entryName)){
            return;
        }
        
        JarEntry newEntry = new JarEntry(entryName);
        try {
            os.putNextEntry(newEntry);
            int nextByte = content.read();
            while(nextByte != -1){
                os.write(nextByte);
                nextByte = content.read();
            }
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error writing archive entry for ips src file " + file, e)); //$NON-NLS-1$
        } finally{
            if(content != null){
                try {
                    content.close();
                } catch (IOException e) {
                    throw new CoreException(new IpsStatus("Unable to close steam.", e)); //$NON-NLS-1$
                }
            }
        }
        String path = file.getQualifiedNameType().toPath().toString();
        String basePackageProperty = path + IIpsArchive.QNT_PROPERTY_POSTFIX_SEPARATOR + IIpsArchive.PROPERTY_POSTFIX_BASE_PACKAGE;
        ipsObjectsProperties.setProperty(basePackageProperty, file.getBasePackageNameForGeneratedJavaClass());
        String extensionPackageProperty = path + IIpsArchive.QNT_PROPERTY_POSTFIX_SEPARATOR + IIpsArchive.PROPERTY_POSTFIX_EXTENSION_PACKAGE;
        ipsObjectsProperties.setProperty(extensionPackageProperty, file.getBasePackageNameForExtensionJavaClass());
    }
    
    private void createIpsObjectsPropertiesEntry(JarOutputStream os, Properties ipsObjectsProperties) throws CoreException {
        JarEntry newEntry = new JarEntry(IIpsArchive.JAVA_MAPPING_ENTRY_NAME);
        try {
           os.putNextEntry(newEntry);
           ipsObjectsProperties.store(os, null);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error creating entry ipsobjects.properties", e)); //$NON-NLS-1$
        }
    }
    
    private void addJavaFiles(IIpsPackageFragmentRoot root, JarOutputStream os, IProgressMonitor monitor)throws CoreException {
        IFolder javaSrcFolder = root.getArtefactDestination(false);
        IPackageFragmentRoot javaRoot = root.getIpsProject().getJavaProject().findPackageFragmentRoot(javaSrcFolder.getFullPath());
        if (javaRoot==null) {
            throw new CoreException(new IpsStatus("Can't find file Java root for IPS root " + root.getName())); //$NON-NLS-1$
        }
        if (inclJavaBinaries) {
            IPath path = javaRoot.getRawClasspathEntry().getOutputLocation();
            if (path==null) {
                path = javaRoot.getJavaProject().getOutputLocation();
            }
            IFolder outFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
            if (!handledRootFolders.contains(outFolder)) {
                addFiles(outFolder, outFolder, os, monitor);
                handledRootFolders.add(outFolder);
            }
        }
        // Java sourcen
        if (inclJavaSources) {
            IPath path = javaRoot.getRawClasspathEntry().getPath();
            IFolder srcFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
            if (handledRootFolders.contains(srcFolder)) {
                return;
            }
            addFiles(srcFolder, srcFolder, os, monitor);
            handledRootFolders.add(srcFolder);
        }
    }
    
    private void addFiles(IFolder rootFolder, IFolder folder, JarOutputStream os, IProgressMonitor monitor)throws CoreException {
        IResource[] members = folder.members();
        for (int i = 0; i < members.length; i++) {
            if (members[i] instanceof IFile) {
                addFiles(rootFolder, (IFile)members[i], os, monitor);
            } else if (members[i] instanceof IFolder) {
                addFiles(rootFolder, (IFolder)members[i], os, monitor);
            }
        }
    }

    private void addFiles(IFolder rootFolder, IFile fileToAdd, JarOutputStream os, IProgressMonitor monitor)throws CoreException {
        String name = fileToAdd.getFullPath().removeFirstSegments(rootFolder.getFullPath().segmentCount()).toString();
        if (isDuplicateEnty(name)){
            return;
        }
        JarEntry newEntry = new JarEntry(name);
        try {
           os.putNextEntry(newEntry);
           byte[] contents = getContent(fileToAdd.getContents(true));
           os.write(contents);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Error creating entry ipsobjects.properties", e)); //$NON-NLS-1$
        }
    }

    private byte[] getContent(InputStream contents) throws CoreException {
        try {
            byte[] content = new byte[contents.available()];
            contents.read(content);
            contents.close();
            return content;
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    private int getWorkload() throws CoreException {
        int load = 0;
        for (int i = 0; i < roots.length; i++) {
            load =+ roots[i].getIpsPackageFragments().length;
        }
        return load;
    }

    public boolean isInclJavaBinaries() {
        return inclJavaBinaries;
    }

    public void setInclJavaBinaries(boolean inclJavaBinaries) {
        this.inclJavaBinaries = inclJavaBinaries;
    }

    public boolean isInclJavaSources() {
        return inclJavaSources;
    }

    public void setInclJavaSources(boolean inclJavaSources) {
        this.inclJavaSources = inclJavaSources;
    }
}
