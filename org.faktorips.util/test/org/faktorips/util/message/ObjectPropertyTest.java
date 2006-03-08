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

package org.faktorips.util.message;

import junit.framework.TestCase;

public class ObjectPropertyTest extends TestCase {

    /*
     * Test method for 'org.faktorips.util.message.ObjectProperty.hashCode()'
     */
    public void testHashCode() {
        ObjectProperty op1 = new ObjectProperty(new Integer(1), "toString");
        ObjectProperty op2 = new ObjectProperty(new Integer(1), "toString");
        assertEquals(op1.hashCode(), op2.hashCode());

        ObjectProperty op3 = new ObjectProperty(new Integer(2), "toString");
        assertFalse(op1.hashCode() == op3.hashCode());
        
    }

    /*
     * Test method for 'org.faktorips.util.message.ObjectProperty.equals(Object)'
     */
    public void testEqualsObject() {
        ObjectProperty op1 = new ObjectProperty(new Integer(1), "toString");
        ObjectProperty op2 = new ObjectProperty(new Integer(1), "toString");
        assertEquals(op1, op2);
        
        ObjectProperty op3 = new ObjectProperty(new Integer(2), "toString");
        assertTrue(!op1.equals(op3));
        
    }

}
