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

package org.faktorips.devtools.core.internal.model.tablestructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ForeignKeyTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private TableStructure table;
    private ForeignKey key;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        ipsSrcFile = table.getIpsSrcFile();
        key = (ForeignKey)table.newForeignKey();
        ipsSrcFile.save(true, null);
    }

    @Test
    public void testRemove() {
        key.delete();
        assertEquals(0, table.getNumOfForeignKeys());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testGetName() {
        assertEquals("()", key.getName());
        key.setReferencedTableStructure("RefTable");
        assertEquals("RefTable()", key.getName());

        key.setReferencedUniqueKey("age");
        assertEquals("RefTable(age)", key.getName());
    }

    @Test
    public void testGetKeyItems() {
        assertEquals(0, key.getKeyItemNames().length);
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);
        assertNotSame(items, key.getKeyItemNames()); // defensive copy should be made
        assertEquals(2, key.getKeyItemNames().length);
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

    @Test
    public void testSetKeyItems() {
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testToXml() {
        key = (ForeignKey)table.newForeignKey();
        key.setReferencedTableStructure("RefTable");
        key.setReferencedUniqueKey("key");
        String[] items = new String[] { "age", "gender" };
        key.setKeyItems(items);
        Element element = key.toXml(newDocument());

        ForeignKey copy = new ForeignKey();
        copy.initFromXml(element);
        assertEquals(key.getId(), copy.getId());
        assertEquals("RefTable", copy.getReferencedTableStructure());
        assertEquals("key", copy.getReferencedUniqueKey());
        assertEquals(2, copy.getNumOfKeyItems());
        assertEquals("age", copy.getKeyItemNames()[0]);
        assertEquals("gender", copy.getKeyItemNames()[1]);
    }

    @Test
    public void testInitFromXml() {
        key.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("42", key.getId());
        assertEquals("RefTable", key.getReferencedTableStructure());
        assertEquals("key", key.getReferencedUniqueKey());
        assertEquals(2, key.getNumOfKeyItems());
        assertEquals("age", key.getKeyItemNames()[0]);
        assertEquals("gender", key.getKeyItemNames()[1]);
    }

}
