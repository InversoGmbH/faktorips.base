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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsPackageFragmentRoot extends IpsElement implements IIpsPackageFragmentRoot {

    /**
     * @param parent
     * @param name
     */
    public AbstractIpsPackageFragmentRoot(IIpsProject parent, String name) {
        super(parent, name);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBasedOnSourceFolder() {
        try {
            return getIpsObjectPathEntry().getType()==IIpsObjectPathEntry.TYPE_SRC_FOLDER;
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBasedOnIpsArchive() {
        try {
            return getIpsObjectPathEntry().getType()==IIpsObjectPathEntry.TYPE_ARCHIVE;
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject() {
        return (IIpsProject)parent;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment getDefaultIpsPackageFragment() {
        return getIpsPackageFragment(""); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPathEntry getIpsObjectPathEntry() throws CoreException {
        return ((IpsProject)getIpsProject()).getIpsObjectPathInternal().getEntry(getName());
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment getIpsPackageFragment(String name) {
        if (isValidIpsPackageFragmentName(name)) {
            return newIpsPackageFragment(name);
        }
        return null;
    }
    
    /**
     * A valid IPS package fragment name is either the empty String for the default package fragment or a valid
     * package package fragment name according to <code>JavaConventions.validatePackageName</code>.
     */
    protected boolean isValidIpsPackageFragmentName(String name){
        try {
            return !getIpsProject().getNamingConventions().validateIpsPackageName(name).containsErrorMsg();
        }
        catch (CoreException e) {
            // nothing to do, will return false
        }
        return false;
    }

    protected abstract IIpsPackageFragment newIpsPackageFragment(String name);
    
    /**
     * {@inheritDoc}
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        IIpsSrcFile file = findIpsSrcFile(new QualifiedNameType(qualifiedName, type));
        if (file==null) {
            return null;
        }
        return file.getIpsObject();
    }

    /**
     * {@inheritDoc}
     */
    public final IIpsSrcFile findIpsSrcFile(QualifiedNameType qnt) throws CoreException {
        IIpsObjectPathEntry entry = getIpsObjectPathEntry();
        if (entry==null) {
            return null;
        }
        return entry.findIpsSrcFile(qnt);
    }

    
}
