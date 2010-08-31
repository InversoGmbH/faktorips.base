/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
 * Interface indicating that the model object can create a copy of itself.
 * 
 * <p>
 * <strong> The copy support is experimental in this version. The API might change without notice
 * until it is finalized in a future version. </strong>
 * 
 * @author Jan Ortmann
 */
public interface ICopySupport {

    /**
     * Creates and returns new copy of this object.
     */
    public IModelObject newCopy();

}