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

package org.faktorips.runtime;

/**
 * Visitor to visit a model object delta.
 * 
 * @author Jan Ortmann
 */
public interface IModelObjectDeltaVisitor {

    /**
     * Visits the given model object delta.
     * 
     * @param delta The delta to visit
     * 
     * @return <code>true</code> if the delta's children should be visited; <code>false</code> if
     *         they should be skipped.
     */
    public boolean visit(IModelObjectDelta delta);

}
