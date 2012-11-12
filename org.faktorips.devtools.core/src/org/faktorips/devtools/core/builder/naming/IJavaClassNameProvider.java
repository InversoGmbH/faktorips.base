/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder.naming;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * Implementations of this interface provide an unqualified java class name. This name may differs
 * from several aspects as building interface or not.
 * 
 * @author dirmeier
 */
public interface IJavaClassNameProvider {

    /**
     * The name of the implementation that is generated for the given {@link IIpsSrcFile}.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} you want to get the implementation name for
     * @return the unqualified name of the generated implementation class
     */
    public String getImplClassName(IIpsSrcFile ipsSrcFile);

    /**
     * Returns whether the implementation is a internal artifact or not.
     * 
     * @return True if the implementation class is a internal artifact, false if not
     */
    public boolean isImplClassInternalArtifact();

    /**
     * Returns the name of the interface generated for this {@link IIpsSrcFile} if there is any
     * interface generated.
     * <p>
     * If there is no interface generated for this source file at all (for example for enumerations)
     * the behavior of this method is not specified. So simply do not call!
     * <p>
     * If the generator is configured to not generate published interfaces this method simply
     * returns the implementation name. This makes writing the code generator more easily because
     * you do not have to matter whether interfaces are generated or not. Simply always call this
     * method if you want to get the published interface or the implementation if no published
     * interface is generated.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} you want to get the interface name for
     * @return The unqualified name of the published interface
     */
    public String getInterfaceName(IIpsSrcFile ipsSrcFile);

    /**
     * Returns whether the interface is a internal artifact or not.
     * 
     * @return True if the interface class is a internal artifact, false if not
     */
    public boolean isInterfaceInternalArtifact();

}