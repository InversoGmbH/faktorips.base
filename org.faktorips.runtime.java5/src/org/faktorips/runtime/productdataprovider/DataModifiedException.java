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

package org.faktorips.runtime.productdataprovider;

/**
 * Exception thrown if the requested data has been modified since last correct modification check.
 * 
 * @author dirmeier
 */
public class DataModifiedException extends Exception {

    private static final long serialVersionUID = 1L;
    public final String oldVersion;
    public final String newVersion;

    public DataModifiedException(String message, String oldVersion, String newVersion) {
        super(message);
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " old: " + oldVersion + " new: " + newVersion;
    }

}
