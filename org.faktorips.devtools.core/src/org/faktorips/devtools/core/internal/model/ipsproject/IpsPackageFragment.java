/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentArbitrarySortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.util.EclipseIOUtil;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IIpsPackageFragment<code>.
 */
public class IpsPackageFragment extends AbstractIpsPackageFragment {

    IpsPackageFragment(IIpsElement parent, String name) {
        super(parent, name);
    }

    @Override
    public IResource getCorrespondingResource() {
        String path = name.replace(SEPARATOR, IPath.SEPARATOR);
        IFolder folder = (IFolder)getParent().getCorrespondingResource();
        return folder.getFolder(new Path(path));
    }

    /**
     * {@inheritDoc} IpsPackageFragments are always returned, whether they are output locations of
     * the javaproject corresponding to this packagefragments IpsProject or not.
     */
    @Override
    public IIpsPackageFragment[] getChildIpsPackageFragments() throws CoreException {
        List<IIpsPackageFragment> list = getChildIpsPackageFragmentsAsList();
        return list.toArray(new IIpsPackageFragment[list.size()]);
    }

    @Override
    public IIpsPackageFragmentSortDefinition getSortDefinition() {
        IpsModel model = (IpsModel)getIpsModel();
        IIpsPackageFragmentSortDefinition sortDef = model.getSortDefinition(this);
        return sortDef.copy();
    }

    /**
     * Read the sort definition from the <code>SORT_ORDER_FILE_NAME</code>. Returns a
     * {@link IpsPackageFragmentDefaultSortDefinition} if no <code>SORT_ORDER_FILE_NAME</code> is
     * found.
     * 
     * @return Sort definition.
     */
    public IIpsPackageFragmentSortDefinition loadSortDefinition() throws CoreException {

        IFile file = getSortOrderFile();

        if (file.exists()) {

            try {
                String content = StringUtil.readFromInputStream(file.getContents(), getIpsProject()
                        .getPlainTextFileCharset());
                IpsPackageFragmentArbitrarySortDefinition sortDef = new IpsPackageFragmentArbitrarySortDefinition();
                sortDef.initPersistenceContent(content);
                return sortDef;
            } catch (IOException e) {
                throw new CoreException(new IpsStatus(e));
            }
        }

        return null;
    }

    /**
     * @return Handle to a sort order file. The folder/file doesn't need to exist!
     */
    @Override
    public IFile getSortOrderFile() {
        IFolder folder = null;

        if (isDefaultPackage()) {
            folder = (IFolder)getRoot().getCorrespondingResource();
        } else {
            folder = (IFolder)getParentIpsPackageFragment().getCorrespondingResource();
        }

        return folder.getFile(new Path(IIpsPackageFragment.SORT_ORDER_FILE_NAME));
    }

    @Override
    public IIpsPackageFragment[] getSortedChildIpsPackageFragments() throws CoreException {

        IpsPackageNameComparator comparator = new IpsPackageNameComparator(false);

        List<IIpsPackageFragment> sortedPacks = getChildIpsPackageFragmentsAsList();
        Collections.sort(sortedPacks, comparator);

        return sortedPacks.toArray(new IIpsPackageFragment[sortedPacks.size()]);
    }

    /**
     * Get all child IIpsPackageFragments as List.
     */
    private List<IIpsPackageFragment> getChildIpsPackageFragmentsAsList() throws CoreException {
        List<IIpsPackageFragment> list = new ArrayList<IIpsPackageFragment>();

        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] content = folder.members();

        for (int i = 0; i < content.length; i++) {
            if (content[i].getType() == IResource.FOLDER) {
                if (!getIpsProject().getNamingConventions().validateIpsPackageName(content[i].getName())
                        .containsErrorMsg()) {
                    String packageName = getSubPackageName(content[i].getName());
                    list.add(new IpsPackageFragment(getParent(), packageName));
                }
            }
        }

        return list;
    }

    @Override
    public void setSortDefinition(IIpsPackageFragmentSortDefinition newDefinition) throws CoreException {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsPackageFragment.setSortDefinition: pack=" + this); //$NON-NLS-1$
        }

        IFile file = getSortOrderFile();

        if (newDefinition == null) {
            if (file.exists()) {
                file.delete(true, null);
            }
            return;
        }

        if (newDefinition instanceof IIpsPackageFragmentArbitrarySortDefinition) {
            IIpsPackageFragmentArbitrarySortDefinition newSortDef = (IIpsPackageFragmentArbitrarySortDefinition)newDefinition;

            String content = newSortDef.toPersistenceContent();
            byte[] bytes;

            try {
                bytes = content.getBytes(getIpsProject().getPlainTextFileCharset());
            } catch (UnsupportedEncodingException e) {
                throw new CoreException(new IpsStatus(e));
            }

            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            // overwrite existing files
            if (!file.exists()) {
                file.create(is, true, null);
                return;
            }
            EclipseIOUtil.writeToFile(file, is, true, true, null);
        }
    }

    @Override
    public IResource[] getNonIpsResources() throws CoreException {
        IContainer cont = (IContainer)getCorrespondingResource();
        List<IResource> childResources = new ArrayList<IResource>();
        IResource[] children = cont.members();
        for (int i = 0; i < children.length; i++) {
            if (!isIpsContent(children[i])) {
                childResources.add(children[i]);
            }
        }
        IResource[] resArray = new IResource[childResources.size()];
        return childResources.toArray(resArray);
    }

    /**
     * Returns <code>true</code> if the given IResource is a file or folder that corresponds to an
     * IpsObject or IpsPackageFragment contained in this IpsPackageFragment, false otherwise.
     */
    private boolean isIpsContent(IResource res) throws CoreException {
        IIpsElement[] children = getChildIpsPackageFragments();
        for (IIpsElement element : children) {
            if (element.getCorrespondingResource().equals(res)) {
                return true;
            }
        }
        children = getChildren();
        for (IIpsElement element : children) {
            if (element.getCorrespondingResource().equals(res)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        IIpsSrcFile[] children = new IIpsSrcFile[members.length];
        int counter = 0;
        for (int i = 0; i < children.length; i++) {
            if (members[i].getType() == IResource.FILE) {
                IFile file = (IFile)members[i];
                if (IpsObjectType.getTypeForExtension(file.getFileExtension()) != null) {
                    children[counter] = new IpsSrcFile(this, file.getName());
                    counter++;
                }
            }
        }
        if (counter == children.length) {
            return children;
        }
        IIpsSrcFile[] shrinked = new IIpsSrcFile[counter];
        System.arraycopy(children, 0, shrinked, 0, counter);
        return shrinked;
    }

    @Override
    public IIpsSrcFile createIpsFile(String name, InputStream source, boolean force, IProgressMonitor monitor)
            throws CoreException {

        IIpsSrcFile ipsSrcFile = getIpsSrcFile(name);
        IpsModel model = (IpsModel)getIpsModel();
        model.removeIpsSrcFileContent(ipsSrcFile);

        IFolder folder = (IFolder)getCorrespondingResource();
        IFile file = folder.getFile(name);
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsPackageFragment.createIpsFile - begin: pack=" + this + ", newFile=" + name //$NON-NLS-1$ //$NON-NLS-2$
                    + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
        }
        file.create(source, force, monitor);

        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("IpsPackageFragment.createIpsFile - finished: pack=" + this + ", newFile=" + name //$NON-NLS-1$ //$NON-NLS-2$
                    + ", Thead: " + Thread.currentThread().getName()); //$NON-NLS-1$
        }

        // set the new evaluated runtime id for product components
        if (ipsSrcFile.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT) {
            try {
                model.stopBroadcastingChangesMadeByCurrentThread();
                IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
                IIpsProject project = getIpsProject();
                String runtimeId = project.getProductCmptNamingStrategy().getUniqueRuntimeId(project,
                        productCmpt.getName());
                productCmpt.setRuntimeId(runtimeId);
                ipsSrcFile.save(force, monitor);
            } finally {
                model.resumeBroadcastingChangesMadeByCurrentThread();
            }
        }

        return ipsSrcFile;
    }

    @Override
    public IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor)
            throws CoreException {
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes(getIpsProject().getXmlFileCharset()));
            return createIpsFile(name, is, force, monitor);
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    @Override
    public IIpsSrcFile createIpsFile(IpsObjectType type, String ipsObjectName, boolean force, IProgressMonitor monitor)
            throws CoreException {
        String filename = type.getFileName(ipsObjectName);
        IIpsObject ipsObject = type.newObject(getIpsSrcFile(filename));

        Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        Element element = ipsObject.toXml(doc);
        try {
            String encoding = getIpsProject().getXmlFileCharset();
            String contents = XmlUtil.nodeToString(element, encoding);
            return createIpsFile(filename, contents, force, monitor);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
            // This is a programing error, rethrow as runtime exception
        }
    }

    /**
     * @deprecated {@link IIpsPackageFragment#createIpsFileFromTemplate(String, IIpsObject, GregorianCalendar, GregorianCalendar, boolean, IProgressMonitor)}
     */
    @Override
    @Deprecated
    public IIpsSrcFile createIpsFileFromTemplate(String name,
            IIpsObject template,
            GregorianCalendar oldDate,
            GregorianCalendar newDate,
            boolean force,
            IProgressMonitor monitor) throws CoreException {

        IpsObjectType type = template.getIpsObjectType();
        String filename = type.getFileName(name);
        Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        Element element;

        IIpsSrcFile ipsSrcFile;
        element = template.toXml(doc);
        try {
            String encoding = getIpsProject().getXmlFileCharset();
            String contents = XmlUtil.nodeToString(element, encoding);
            ipsSrcFile = createIpsFile(filename, contents, force, monitor);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        if (template instanceof ITimedIpsObject) {
            ITimedIpsObject copyProductCmpt = ((ITimedIpsObject)ipsSrcFile.getIpsObject());
            IIpsObjectGeneration generationEffectiveOn = copyProductCmpt.getGenerationEffectiveOn(oldDate);
            if (generationEffectiveOn == null) {
                generationEffectiveOn = copyProductCmpt.getFirstGeneration();
            }
            for (IIpsObjectGeneration generation : copyProductCmpt.getGenerations()) {
                if (!generation.equals(generationEffectiveOn)) {
                    generation.delete();
                }
            }
            if (generationEffectiveOn == null) {
                generationEffectiveOn = copyProductCmpt.newGeneration(newDate);
            } else {
                generationEffectiveOn.setValidFrom(newDate);
            }
        }

        return ipsSrcFile;
    }

    @Override
    public void findIpsObjects(IpsObjectType type, List<IIpsObject> result) throws CoreException {
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        String extension = type.getFileExtension();
        for (IResource member : members) {
            if (member.getType() == IResource.FILE) {
                IFile file = (IFile)member;
                if (extension.equals(file.getFileExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject() != null) {
                        result.add(srcFile.getIpsObject());
                    }
                }
            }
        }
    }

    public void findIpsObjects(List<IIpsObject> result) throws CoreException {
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();

        IpsObjectType[] types = getIpsModel().getIpsObjectTypes();

        Set<String> fileExtensionNames = new HashSet<String>();
        for (IpsObjectType type : types) {
            fileExtensionNames.add(type.getFileExtension());
        }
        for (IResource member : members) {
            if (member.getType() == IResource.FILE) {
                IFile file = (IFile)member;
                if (fileExtensionNames.contains(file.getFileExtension())) {
                    IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                    if (srcFile.getIpsObject() != null) {
                        result.add(srcFile.getIpsObject());
                    }
                }
            }
        }
    }

    @Override
    public void findIpsSourceFiles(IpsObjectType type, List<IIpsSrcFile> result) throws CoreException {
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        for (IResource member : members) {
            if (member.getType() == IResource.FILE) {
                IFile file = (IFile)member;
                if (type.getFileExtension().equals(file.getFileExtension())) {
                    IpsSrcFile ipsSrcFile = new IpsSrcFile(this, file.getName());
                    result.add(ipsSrcFile);
                }
            }
        }
    }

    /**
     * Searches all objects of the given type starting with the given prefix and adds them to the
     * result.
     * 
     * @throws NullPointerException if either type, prefix or result is null.
     * @throws CoreException if an error occurs while searching.
     */
    public void findIpsSourceFilesStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result) throws CoreException {

        ArgumentCheck.notNull(type);
        ArgumentCheck.notNull(prefix);
        ArgumentCheck.notNull(result);
        if (!exists()) {
            return;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] members = folder.members();
        String newPrefix = ignoreCase ? prefix.toLowerCase() : prefix;
        for (IResource member : members) {
            if (member.getType() == IResource.FILE) {
                IFile file = (IFile)member;
                if (type.getFileExtension().equals(file.getFileExtension())) {
                    String filename = ignoreCase ? file.getName().toLowerCase() : file.getName();
                    if (filename.startsWith(newPrefix)) {
                        IIpsSrcFile srcFile = new IpsSrcFile(this, file.getName());
                        result.add(srcFile);
                    }
                }
            }
        }
    }

    @Override
    public IIpsPackageFragment createSubPackage(String name, boolean force, IProgressMonitor monitor)
            throws CoreException {
        if (getIpsProject().getNamingConventions().validateIpsPackageName(name).containsErrorMsg()) {
            throw new CoreException(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID, IStatus.ERROR, NLS.bind(
                    "{0} is not a valid package name.", name), null)); //$NON-NLS-1$
        }
        return getRoot().createPackageFragment(getSubPackageName(name), true, null);
    }

    @Override
    public boolean hasChildIpsPackageFragments() throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        IResource[] content = folder.members();

        for (int i = 0; i < content.length; i++) {
            if (content[i].getType() == IResource.FOLDER) {
                if (!getIpsProject().getNamingConventions().validateIpsPackageName(content[i].getName())
                        .containsErrorMsg()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void delete() throws CoreException {
        for (IIpsPackageFragment childPackage : getChildIpsPackageFragments()) {
            childPackage.delete();
        }
        for (IIpsSrcFile childSrcFile : getIpsSrcFiles()) {
            childSrcFile.delete();
        }
        getCorrespondingResource().delete(true, null);
    }

}
