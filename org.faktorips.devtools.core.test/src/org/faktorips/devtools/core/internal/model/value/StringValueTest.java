/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class StringValueTest extends AbstractIpsPluginTest {

    @Test
    public void testToXml() {
        StringValue stringValue = new StringValue("Versicherung");
        Node xml = stringValue.toXml(getTestDocument());
        assertNotNull(xml);

        StringValue copy = StringValue.createFromXml((Text)xml);
        assertNotNull(copy);
        assertEquals("Versicherung", copy.getContent());
    }

    @Test
    public void testGetContent() {
        assertEquals("Versicherung", new StringValue("Versicherung").getContent());
        assertNull(new StringValue(null).getContent());
    }

    @Test
    public void testGetContentAsString() {
        assertEquals("Versicherung", new StringValue("Versicherung").getContentAsString());
        assertNull(new StringValue(null).getContentAsString());
    }

    @Test
    public void testGetLocalizedContent() {
        assertEquals("Versicherung", new StringValue("Versicherung").getLocalizedContent());
    }

    @Test
    public void testGetLocalizedContentLocale() {
        assertEquals("Versicherung", new StringValue("Versicherung").getLocalizedContent(Locale.GERMAN));
    }

    @Test
    public void testGetDefaultLocalizedContent() throws CoreException {
        assertEquals("Versicherung", new StringValue("Versicherung").getDefaultLocalizedContent(newIpsProject()));
    }

    @Test
    public void testToString() {
        assertEquals("Versicherung", new StringValue("Versicherung").toString());
        assertNull(new StringValue(null).toString());
    }

    @Test
    public void testEquals() {
        StringValue stringValue = new StringValue("Versicherung");
        assertTrue(stringValue.equals(stringValue));
        assertFalse(stringValue.equals(null));
        assertFalse(stringValue.equals(new String("Versicherung")));
        StringValue copy = new StringValue("");
        assertFalse(stringValue.equals(copy));
        copy = new StringValue("Versicherung");
        assertTrue(stringValue.equals(copy));
        stringValue = new StringValue(null);
        assertFalse(stringValue.equals(copy));
        stringValue = new StringValue("");
        assertFalse(stringValue.equals(copy));
        copy = new StringValue("");
        assertTrue(stringValue.equals(copy));

    }
}
