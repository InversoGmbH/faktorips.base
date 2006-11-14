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

package org.faktorips.devtools.core.internal.model.tablecontents;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.w3c.dom.Element;


/**
 *
 */
public class TableContentsGenerationTest extends AbstractIpsPluginTest {

    private ITableContents table; 
    private TableContentsGeneration generation;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        table = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "TestTable");
        generation = (TableContentsGeneration)table.newGeneration();
        table.newColumn(null);
        table.newColumn(null);
        table.newColumn(null);
    }
    
    public void testGetChildren() {
        table.newColumn(null);
        table.newColumn(null);
        IRow row0 = generation.newRow();
        IRow row1 = generation.newRow();
        IIpsElement[] children = generation.getChildren();
        assertEquals(2, children.length);
        assertSame(row0, children[0]);
        assertSame(row1, children[1]);
    }
    
    public void testNewRow() {
        table.newColumn(null);
        table.newColumn(null);
        IRow row0 = generation.newRow();
        assertEquals(0, row0.getId());
        assertEquals(0, row0.getRowNumber());
        assertEquals("", row0.getValue(0));
        assertEquals("", row0.getValue(1));
        
        IRow row1 = generation.newRow();
        assertEquals(1, row1.getId());
        assertEquals(1, row1.getRowNumber());
    }
    
    public void testNewColumn() {
        IRow row1 = generation.newRow();
        IRow row2 = generation.newRow();
        generation.newColumn(3, "a");
        assertEquals("a", row1.getValue(3));
        assertEquals("a", row2.getValue(3));
    }
    
    public void testRemoveColumn() {
        IRow row1 = generation.newRow();
        IRow row2 = generation.newRow();
        row1.setValue(0, "row1,col1");
        row1.setValue(1, "row1,col2");
        row1.setValue(2, "row1,col3");
        row2.setValue(0, "row2,col1");
        row2.setValue(1, "row2,col2");
        row2.setValue(2, "row2,col3");
        generation.removeColumn(1);
        assertEquals("row1,col1", row1.getValue(0));
        assertEquals("row1,col3", row1.getValue(1));
        try {
            row1.getValue(2);
            fail();
        } catch (Exception e) {}
        assertEquals("row2,col1", row2.getValue(0));
        assertEquals("row2,col3", row2.getValue(1));
        try {
            row2.getValue(2);
            fail();
        } catch (Exception e) {}
        
    }

    public void testToXml() {
        IRow row1 = generation.newRow();
        IRow row2 = generation.newRow();
        Element element = generation.toXml(newDocument());
        row1.delete();
        row2.delete();
        generation.initFromXml(element);
        assertEquals(2, generation.getNumOfRows());
    }

    public void testInitFromXml() {
        generation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(2, generation.getNumOfRows());
    }

    public void testNewPart() {
        // test rownumber init within newPart()
        IRow row0 = (IRow) generation.newPart(IRow.class);
        assertEquals(0, row0.getId());
        assertEquals(0, row0.getRowNumber());

        IRow row1 = (IRow) generation.newPart(IRow.class);
        assertEquals(1, row1.getId());
        assertEquals(1, row1.getRowNumber());
        
        
    	try {
    		assertTrue(generation.newPart(IRow.class) instanceof IRow);
    		
    		generation.newPart(Object.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    public void testClear() {
        generation.newRow();
        generation.newRow();
        generation.clear();
        assertEquals(0, generation.getNumOfRows());
    }
    
    public void testGetRow(){
        IRow row1= generation.newRow();
        generation.newRow();
        IRow row2= generation.newRow();

        assertEquals(row1, generation.getRow(0));
        assertEquals(row2, generation.getRow(2));

        assertNull(generation.getRow(-1));
        assertNull(generation.getRow(42));
    }
}
