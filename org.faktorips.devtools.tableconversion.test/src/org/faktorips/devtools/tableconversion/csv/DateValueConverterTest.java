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

import junit.framework.TestCase;

import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.devtools.tableconversion.csv.DateValueConverter;
import org.faktorips.util.message.MessageList;

public class DateValueConverterTest extends TestCase {
    private MessageList ml;
    private DateValueConverter converter;
    private DateDatatype datatype;
    private ITableFormat tableFormat;

    public void setUp() throws Exception {
        super.setUp();
        ml = new MessageList();
        tableFormat = new CSVTableFormat();
        converter = new DateValueConverter();
        converter.setTableFormat(tableFormat);
        datatype = new DateDatatype();
    }

    public void testGetIpsValueUsingCustomDateFormat() {
        tableFormat.setProperty(CSVTableFormat.PROPERTY_DATE_FORMAT, "dd.MM.yyyy");
        String value = converter.getIpsValue("08.10.2009", ml);
        assertTrue(ml.toString(), ml.isEmpty());
        assertTrue(value, '-' == value.charAt(4) && '-' == value.charAt(7));

        // now use only the year field
        tableFormat.setProperty(CSVTableFormat.PROPERTY_DATE_FORMAT, "yyyy");
        value = converter.getIpsValue("1999", ml);
        assertTrue(ml.isEmpty());

        tableFormat.setProperty(CSVTableFormat.PROPERTY_DATE_FORMAT, "dd.MM.yyyy");
        value = converter.getIpsValue("xx01.10.2009", ml);
        assertFalse(ml.isEmpty());
    }
    
    public void testGetExternalDataValue() {
        tableFormat.setProperty(CSVTableFormat.PROPERTY_DATE_FORMAT, "dd.MM.yyyy");
        Object extValue = converter.getExternalDataValue("2009-10-15", ml);
        assertTrue(extValue instanceof String);
        assertEquals("15.10.2009", (String) extValue);
    }
}
