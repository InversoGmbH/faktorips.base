/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.runtime;


/**
 * Cache for objects in the repository.
 *  
 * @author Jan Ortmann
 */
public interface ICache {
    
    /**
     * Removes all data from the cache.
     */
    public void clear();

    /**
     * Returns the object identified by the given key or <code>null</code>
     * if the cache does not contain an object for the provided key.
     * 
     * @param key The key object that identifies the object in the cache. Note that
     * the key class must implement a proper hashCode() and equals() function.
     */
    public Object getObject(Object key);
    
    /**
     * Puts the object under the given key in the cache. Replaces any object that has been
     * put in the cache with the same key.
     */
    public void put(Object key, Object o);
}
