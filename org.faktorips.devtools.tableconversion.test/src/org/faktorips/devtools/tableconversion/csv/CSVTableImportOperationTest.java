/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import java.io.File;
import java.io.FileWriter;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.tableconversion.AbstractExternalTableFormat;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.faktorips.devtools.tableconversion.csv.CSVTableFormat;
import org.faktorips.devtools.tableconversion.csv.CSVTableImportOperation;
import org.faktorips.devtools.tableconversion.excel.BooleanValueConverter;
import org.faktorips.devtools.tableconversion.excel.DateValueConverter;
import org.faktorips.devtools.tableconversion.excel.DecimalValueConverter;
import org.faktorips.devtools.tableconversion.excel.DoubleValueConverter;
import org.faktorips.devtools.tableconversion.excel.IntegerValueConverter;
import org.faktorips.devtools.tableconversion.excel.LongValueConverter;
import org.faktorips.devtools.tableconversion.excel.MoneyValueConverter;
import org.faktorips.devtools.tableconversion.excel.StringValueConverter;
import org.faktorips.util.message.MessageList;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVTableImportOperationTest extends AbstractTableTest {

    ITableContentsGeneration importTarget;
    AbstractExternalTableFormat format;

    File file;

    private IIpsProject ipsProject;
    private ITableContents contents;
    
    
    protected void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("test");
        IIpsProjectProperties props = ipsProject.getProperties();
        String[] datatypes = getColumnDatatypes();
        props.setPredefinedDatatypesUsed(datatypes);
        ipsProject.setProperties(props);

        format = new CSVTableFormat();
        format.setName("CSV");
        format.setDefaultExtension(".csv");
        format.addValueConverter(new BooleanValueConverter());
        format.addValueConverter(new DecimalValueConverter());
        format.addValueConverter(new DoubleValueConverter());
        format.addValueConverter(new DateValueConverter());
        format.addValueConverter(new IntegerValueConverter());
        format.addValueConverter(new LongValueConverter());
        format.addValueConverter(new MoneyValueConverter());
        format.addValueConverter(new StringValueConverter());

        contents = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "importTarget");
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        contents.newColumn(null);
        importTarget = (ITableContentsGeneration)contents.newGeneration(new GregorianCalendar());

        file = new File("table" + format.getDefaultExtension());
        file.delete();
        assertTrue(file.createNewFile());
    }

    protected void tearDownExtension() throws Exception {
//         file.delete();
    }

    public void testImportValid() throws Exception {
        createValidExternalTable(ipsProject, format, true);

        MessageList ml = new MessageList();
        CSVTableImportOperation op = new CSVTableImportOperation(getStructure(), file.getName(), importTarget, format,
                "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertTrue(ml.isEmpty());
    }

    public void testImportValidRowMismatch() throws Exception {
        createValidExternalTable(ipsProject, format, true);

        // too many columns
        IColumn col = getStructure().newColumn();

        MessageList ml = new MessageList();
        CSVTableImportOperation op = new CSVTableImportOperation(getStructure(), file.getName(), importTarget, format,
                "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());

        // invalid structure
        ml.clear();
        col.delete();
        getStructure().getColumn(0).setDatatype("");
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());

        // too less columns
        ml.clear();
        getStructure().getColumn(0).delete();
        op = new CSVTableImportOperation(getStructure(), file.getName(), importTarget, format,
                "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertFalse(ml.isEmpty());
    }

    public void testImportFirstRowContainsNoColumnHeader() throws Exception {
        createValidExternalTable(ipsProject, format, false);

        MessageList ml = new MessageList();
        CSVTableImportOperation op = new CSVTableImportOperation(getStructure(), file.getName(), importTarget, format,
                "NULL", false, ml);
        op.run(new NullProgressMonitor());
        assertFalse(ml.containsErrorMsg());
        assertEquals(3, importTarget.getNumOfRows());
    }

    public void testImportFirstRowContainsColumnHeader() throws Exception {
        createValidExternalTable(ipsProject, format, true);

        MessageList ml = new MessageList();
        CSVTableImportOperation op = new CSVTableImportOperation(getStructure(), file.getName(), importTarget, format,
                "NULL", true, ml);
        op.run(new NullProgressMonitor());
        assertFalse(ml.containsErrorMsg());
        assertEquals(3, importTarget.getNumOfRows());
    }

    public void testImportInvalid() throws Exception {
        createInvalidCsvFile();
        
        MessageList ml = new MessageList();
        CSVTableImportOperation op = new CSVTableImportOperation(createTableStructure(ipsProject), 
                file.getName(), importTarget, format, "NULL", true, ml);
        
        op.run(new NullProgressMonitor());
        assertEquals(6, ml.getNoOfMessages());
    }
    
    private void createInvalidCsvFile() throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(file));
        
        String[] invalidLine = new String[] {
                "invalid is impossible", "INVALID", "INVALID", "INVALID",
                "INVALID", "INVALID", "INVALID", "invalid is impossible"
        };

        writer.writeNext(new String[] {"This", "is", "the", "header."});
        writer.writeNext(invalidLine);
        writer.close();
    }
}
