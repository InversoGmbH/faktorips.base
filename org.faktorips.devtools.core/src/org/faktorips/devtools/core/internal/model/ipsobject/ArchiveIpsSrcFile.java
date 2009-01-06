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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.ArchiveIpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.ArchiveIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.w3c.dom.Document;

/**
 * 
 * @author Jan Ortmann
 */
public class ArchiveIpsSrcFile extends AbstractIpsSrcFile implements IIpsSrcFile {

    /**
     * @param parent
     * @param name
     */
    public ArchiveIpsSrcFile(ArchiveIpsPackageFragment pack, String name) {
        super(pack, name);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isDirty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void markAsClean() {
        // never dirty => nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void markAsDirty() {
        throw new RuntimeException("Can't mark an file in an archive as dirty!"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void discardChanges() {
        // never dirty => nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFileMemento newMemento() throws CoreException {
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        return new IIpsSrcFileMemento(this, getIpsObject().toXml(doc), false);
    }

    /**
     * {@inheritDoc}
     */
    public void setMemento(IIpsSrcFileMemento memento) throws CoreException {
        // never dirty => nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void save(boolean force, IProgressMonitor monitor) throws CoreException {
        
    }

    /**
     * {@inheritDoc}
     */
    public boolean isHistoric() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMutable() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IFile getCorrespondingFile() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getContentFromEnclosingResource() throws CoreException {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getIpsPackageFragment().getRoot();
        IIpsArchive archive = root.getIpsArchive();
        if (archive==null) {
            return null;
        }
        return archive.getContent(getQualifiedNameType());
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForGeneratedJavaClass() throws CoreException {
        IIpsArchive archive = getIpsPackageFragment().getRoot().getIpsArchive();
        return archive.getBasePackageNameForGeneratedJavaClass(getQualifiedNameType());
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForExtensionJavaClass() throws CoreException {
        IIpsArchive archive = getIpsPackageFragment().getRoot().getIpsArchive();
        return archive.getBasePackageNameForExtensionJavaClass(getQualifiedNameType());
    }

}
