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

package org.faktorips.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class StreamUtilTest {
	
    @Test
    public void testCopy() throws Exception {
        byte[] bytes = new byte[8];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 1;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ByteArrayInputStream copiedStream = StreamUtil.copy(bis, 100);
        int value = copiedStream.read();
        int counter = 0;
        while (value != -1) {
            assertEquals("At postion: " + counter, 1, value);
            value = copiedStream.read();
            counter++;
        }
        assertEquals(8, counter);

        bytes = new byte[100];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 1;
        }
        bis = new ByteArrayInputStream(bytes);
        copiedStream = StreamUtil.copy(bis, 9);
        value = copiedStream.read();
        counter = 0;
        while (value != -1) {
            assertEquals("At postion: " + counter, 1, value);
            value = copiedStream.read();
            counter++;
        }
        assertEquals(100, counter);

        bytes = new byte[] { 1 };
        bis = new ByteArrayInputStream(bytes);
        copiedStream = StreamUtil.copy(bis, 1);
        value = copiedStream.read();
        counter = 0;
        while (value != -1) {
            assertEquals("At postion: " + counter, 1, value);
            value = copiedStream.read();
            counter++;
        }
        assertEquals(1, counter);
    }

}
