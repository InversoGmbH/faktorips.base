/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.values;

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class ObjectUtilTest extends TestCase {

    /*
     * Test method for 'org.faktorips.values.ObjectUtil.isNull(Object)'
     */
    public void testIsNull() {
        assertTrue(ObjectUtil.isNull(null));
        assertTrue(ObjectUtil.isNull(Money.NULL));
        assertFalse(ObjectUtil.isNull(Money.euro(42, 0)));
    }

}
