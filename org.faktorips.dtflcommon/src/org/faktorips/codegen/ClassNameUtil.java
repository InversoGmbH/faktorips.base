/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.codegen;

import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Jan Ortmann
 */
public class ClassNameUtil {

    /** 
     * Takes a name like a class name and removes the package information from the beginning.
     */
    public final static String unqualifiedName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(".");
        if (index == -1)
        {
            return qualifiedName;
        }
        return qualifiedName.substring(index+1);
    }
    
    /**
     * Returns the qualified name for the given package name and unqualified name.
     * If packageName is <code>null</code> or the empty String the unqualified name
     * is returned.
     * 
     * @throws NullPointerException if unqualifiedName is <code>null</code>.
     */
    public final static String qualifiedName(String packageName, String unqualifiedName) {
        ArgumentCheck.notNull(unqualifiedName);
        if (packageName==null || packageName.equals("")) {
            return unqualifiedName;
        }
        return packageName + '.' + unqualifiedName;
    }
    
    /**
     * Returns the package name for a given class name. Returns an empty String
     * if the class name does not contain a package name.
     * 
     * @throws NullPointerException if the qualifiedClassName is null.
     */
    public final static String getPackageName(String qualifiedClassName)
    {
        int index = qualifiedClassName.lastIndexOf(".");
        if (index == -1)
        {
            return "";
        }
        return qualifiedClassName.substring(0, index);
    }
    
    private ClassNameUtil() {
    }

}
