/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.extsystems.excel;

import junit.framework.TestCase;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Thorsten Guenther
 */
public class BooleanValueConverterTest extends TestCase {

    public void testGetIpsValue() {
        MessageList ml = new MessageList();
        BooleanValueConverter converter = new BooleanValueConverter();
        String value = converter.getIpsValue(Boolean.FALSE, ml);
        assertTrue(Datatype.BOOLEAN.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(Boolean.TRUE, ml);
        assertTrue(Datatype.BOOLEAN.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue("true", ml);
        assertTrue(Datatype.BOOLEAN.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(new Integer(0), ml);
        assertFalse(ml.isEmpty());
        assertEquals(value, new Integer(0).toString());
    }

    public void testGetExternalDataValue() {
        MessageList ml = new MessageList();
        BooleanValueConverter converter = new BooleanValueConverter();
        final String TRUE = "true";
        final String FALSE = "false";
        final String INVALID = "invalid";

        assertTrue(Datatype.BOOLEAN.isParsable(TRUE));
        assertTrue(Datatype.BOOLEAN.isParsable(FALSE));

        Boolean value = (Boolean)converter.getExternalDataValue(TRUE, ml);
        assertTrue(value.booleanValue());
        assertTrue(ml.isEmpty());

        value = (Boolean)converter.getExternalDataValue(FALSE, ml);
        assertFalse(value.booleanValue());
        assertTrue(ml.isEmpty());

        value = (Boolean)converter.getExternalDataValue(INVALID, ml);
        assertFalse(value.booleanValue());
        assertTrue(ml.isEmpty());

        value = (Boolean)converter.getExternalDataValue(null, ml);
        assertNull(value);
        assertTrue(ml.isEmpty());
    }

}
