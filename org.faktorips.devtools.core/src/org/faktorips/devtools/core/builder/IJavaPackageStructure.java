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

package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * A <code>JavaSourceFileBuilder</code> needs an implementation of this interface. It provides the
 * package name of every kind of generated class within this package structure. Since it is possible
 * to generate multiple java classes for an IpsObject it is not enough to just provide the IpsObject
 * instance to the methods of the package structure. An addition parameter "kind" is necessary to
 * uniquely identify the java class in question.
 * 
 * @author Peter Erzberger
 */
public interface IJavaPackageStructure {

    /**
     * Returns the package string for the provided IpsObject and kind.
     * 
     * @param kind identifies the kind of java class that exists within this package structure for
     *            the provided IpsObject
     * @param ipsObject the IpsObject that identifies in conjunction with the kind parameter the
     *            package of the java class that exists within this package structure
     * @return the package string
     * 
     * @throws CoreException implementations can wrap rising checked exceptions into a CoreException
     */
    public String getPackage(String kind, IIpsSrcFile ipsSrcFile) throws CoreException;
}
