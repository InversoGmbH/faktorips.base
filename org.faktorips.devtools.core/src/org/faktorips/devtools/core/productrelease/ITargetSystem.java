/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.productrelease;

/**
 * This object represents a target system for the release and deployment extension. It is used by
 * the {@link IReleaseAndDeploymentOperation}.
 * 
 * The intreface only provides the method {@link #getName()}, returning a readable name of this
 * system that is displayed to the user.
 * 
 * @author dirmeier
 */
public interface ITargetSystem {

    /**
     * Getting a readable name of this target system
     * 
     * return the name of the target system
     */
    public String getName();

}