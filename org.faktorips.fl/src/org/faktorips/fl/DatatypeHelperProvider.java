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

package org.faktorips.fl;

import org.faktorips.codegen.BaseDatatypeHelper;
import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;

/**
 * Provides code generation helpers for the datatypes.
 * 
 * @author Jan Ortmann
 */
public interface DatatypeHelperProvider<T extends CodeFragment> {

    /**
     * Returns the code generation helper for the given datatype or <code>null</code> if either
     * datatype is <code>null</code> or the provide can't provide a helper.
     */
    public BaseDatatypeHelper<T> getDatatypeHelper(Datatype datatype);

}
