/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsPackageFragment extends IpsElement implements IIpsPackageFragment {

    public AbstractIpsPackageFragment(IIpsElement parent, String name) {
        super(parent, name);
    }

    public AbstractIpsPackageFragment() {
        super();
    }

    @Override
    public IIpsPackageFragmentRoot getRoot() {
        return (IIpsPackageFragmentRoot)getParent();
    }

    @Override
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsSrcFiles();
    }

    @Override
    public IIpsPackageFragment getParentIpsPackageFragment() {
        int lastIndex = getName().lastIndexOf(SEPARATOR);
        if (lastIndex < 0) {
            if (isDefaultPackage()) {
                return null;
            } else {
                return getRoot().getDefaultIpsPackageFragment();
            }
        } else {
            String parentPath = getName().substring(0, lastIndex);
            return new IpsPackageFragment(getParent(), parentPath);
        }
    }

    @Override
    public IPath getRelativePath() {
        return new Path(getName().replace(SEPARATOR, IPath.SEPARATOR));
    }

    @Override
    public boolean isDefaultPackage() {
        return getName().equals(NAME_OF_THE_DEFAULT_PACKAGE);
    }

    @Override
    public IIpsSrcFile getIpsSrcFile(String name) {
        IpsObjectType type = IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
        if (type != null) {
            return new IpsSrcFile(this, name);
        }
        return null;
    }

    @Override
    public IIpsSrcFile getIpsSrcFile(String filenameWithoutExtension, IpsObjectType type) {
        return new IpsSrcFile(this, filenameWithoutExtension + SEPARATOR + type.getFileExtension());
    }

    @Override
    public String getLastSegmentName() {
        int index = getName().lastIndexOf(SEPARATOR);
        if (index == -1) {
            return getName();
        } else {
            return getName().substring(index + 1);
        }
    }

    protected String getSubPackageName(String subPackageName) {
        return isDefaultPackage() ? subPackageName : getName() + SEPARATOR + subPackageName;
    }

    @Override
    public IIpsPackageFragment getSubPackage(String subPackageFragmentName) {
        String packageName = getSubPackageName(subPackageFragmentName);
        return getRoot().getIpsPackageFragment(packageName);
    }

    /**
     * Searches all objects of the given type and adds them to the result.
     * 
     * @throws CoreException if an error occurs while searching
     */
    public abstract void findIpsObjects(IpsObjectType type, List<IIpsObject> result) throws CoreException;

    /**
     * Searches all ips source files of the given type and adds them to the result.
     * 
     * @throws CoreException if an error occurs while searching
     */
    public abstract void findIpsSourceFiles(IpsObjectType type, List<IIpsSrcFile> result) throws CoreException;

}
