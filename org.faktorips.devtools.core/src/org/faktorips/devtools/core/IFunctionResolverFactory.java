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

package org.faktorips.devtools.core;

import java.util.Locale;

import org.faktorips.fl.FunctionResolver;

/**
 * FunctionResolverFactories can be registered with the <i>flfunctionResolverFactory</i> extension
 * point. The function resolvers of the registered factories augment the set of available formula
 * language functions.
 * 
 * @author Peter Erzberger
 */
public interface IFunctionResolverFactory {

    /**
     * Creates a new FunctionResolver with respect to the provided local. It is in the
     * responsibility of the factory provider if the locale is considered.
     */
    public FunctionResolver newFunctionResolver(Locale locale);

}
