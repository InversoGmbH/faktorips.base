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

package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;

/**
 * A <tt>JavaSourceFileBuilder</tt> needs an implementation of this interface. It provides the
 * package name of every kind of generated class within this package structure. Since it is possible
 * to generate multiple Java classes for an <tt>IIpsObject</tt> it is not enough to just provide the
 * <tt>IIpsObject</tt> instance to the methods of the package structure. An additional parameter
 * "kind" is necessary to uniquely identify the Java class in question.
 * 
 * @author Peter Erzberger
 */
public interface IJavaPackageStructure {

    /**
     * Returns the package string for the provided IpsObject and kind.
     * 
     * @param builder the builder in which context the file should be generated
     * @param ipsSrcFile the IPS source file that identifies in conjunction with the kind parameter
     *            the package of the java class that exists within this package structure
     * 
     * @throws CoreException implementations can wrap rising checked exceptions into a CoreException
     */
    public String getPackage(IIpsArtefactBuilder builder, IIpsSrcFile ipsSrcFile) throws CoreException;

}
