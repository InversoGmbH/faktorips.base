/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.values.DateUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX event handler class for ips table contents.<br>
 * Note that the description is currently not supported for table contents.
 * 
 * @author Joerg Ortmann
 */
public class TableContentsSaxHandler extends DefaultHandler {
    private static final String TABLECONTENTS = IpsObjectType.TABLE_CONTENTS.getXmlElementName();
    private static final String VALUE = Row.VALUE_TAG_NAME;
    private static final String ROW = Row.TAG_NAME;
    private static final String GENERATION = IpsObjectGeneration.TAG_NAME;
    private static final String ATTRIBUTE_VALIDFROM = ITimedIpsObject.PROPERTY_VALID_TO;
    private static final String ATTRIBUTE_TABLESTRUCTURE = ITableContents.PROPERTY_TABLESTRUCTURE;
    private static final String ATTRIBUTE_NUMOFCOLUMNS = ITableContents.PROPERTY_NUMOFCOLUMNS;
    
    // extension properties support
    private static final String EXTENSIONPROPERTIES = TableContentsGeneration.getXmlExtPropertiesElementName();
    private static final String EXTENSIONPROPERTIES_VALUE = TableContentsGeneration.getXmlValueElement();
    private static final String EXTENSIONPROPERTIES_ID = TableContentsGeneration.getXmlAttributeExtpropertyid();
    private static final String EXTENSIONPROPERTIES_ATTRIBUTE_ISNULL = TableContentsGeneration.getXmlAttributeIsnull();
    
    // the table which will be filled
    private ITableContents tableContents;
    
    // contains all column values, 
    private List columns = new ArrayList(20);
    
    // buffer to store the characters inside the value node
    private StringBuffer textBuffer = null;
    
    // true if the parser is inside the row node
    private boolean insideRowNode;

    // true if the parser is inside the extension properties node
    private boolean insideExtensionPropertiesNode;
    
    // true if the parser is inside the value node
    private boolean insideValueNode;
    
    // true if the current value node represents the null value
    private boolean nullValue;
    
    // contains the id of the value node
    private String idValue;
    
    private TableContentsGeneration currentGeneration;
    
    public TableContentsSaxHandler(ITableContents tableContents) {
        this.tableContents = tableContents;
    }

    /**
     * {@inheritDoc}
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ROW.equals(qName)) {
            insideRowNode = false;
            currentGeneration.newRow(columns);
            columns.clear();
        } else if (EXTENSIONPROPERTIES.equals(qName)) {
            insideExtensionPropertiesNode = false;
        } else if (isColumnValueNode(qName)) {
            insideValueNode = false;
            columns.add(textBuffer == null && nullValue ? null : textBuffer == null ? new String("") : textBuffer //$NON-NLS-1$
                    .substring(0));
            textBuffer = null;
        } else if (isExtensionPropertiesValueNode(qName)) {
            insideValueNode = false;
            if (currentGeneration == null) {
                tableContents.addExtensionProperty(idValue, nullValue?null:textBuffer.substring(0));
            } else {
                throw new SAXNotSupportedException("Extension properties inside a generation node are not supported!");
            }
            textBuffer = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (TABLECONTENTS.equals(qName)) {
            ((TableContents)tableContents).setTableStructureInternal(attributes.getValue(ATTRIBUTE_TABLESTRUCTURE));
            ((TableContents)tableContents).setNumOfColumnsInternal(Integer.parseInt(attributes
                    .getValue(ATTRIBUTE_NUMOFCOLUMNS)));
        } else if (GENERATION.equals(qName)) {
            currentGeneration = (TableContentsGeneration)((TableContents)tableContents)
                    .createNewGenerationInternal(DateUtil.parseIsoDateStringToGregorianCalendar(attributes
                            .getValue(ATTRIBUTE_VALIDFROM)));
        } else if (EXTENSIONPROPERTIES.equals(qName)) {
            insideExtensionPropertiesNode = true;
        } else if (ROW.equals(qName)){
            insideRowNode = true;
        } else if (isColumnValueNode(qName)) {
            insideValueNode = true;
            nullValue = Boolean.valueOf(attributes.getValue("isNull")).booleanValue(); //$NON-NLS-1$
        } else if (isExtensionPropertiesValueNode(qName)) {
            insideValueNode = true;
            nullValue = Boolean.valueOf(attributes.getValue(EXTENSIONPROPERTIES_ATTRIBUTE_ISNULL)).booleanValue(); //$NON-NLS-1$
            idValue = attributes.getValue(EXTENSIONPROPERTIES_ID);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void characters(char[] buf, int offset, int len) throws SAXException {
        if (!insideValueNode) {
            // ignore characters which are not inside a value node
            return;
        }
        String s = new String(buf, offset, len);
        if (textBuffer == null){
            textBuffer = new StringBuffer(s);
        } else {
            textBuffer.append(s);
        }
    }
    
    /*
     * Returns <code>true</code> if the given node is the column value node otherwise <code>false</code>
     */
    private boolean isColumnValueNode(String nodeName){
        return VALUE.equals(nodeName) && insideRowNode;
    }    
    
    /*
     * Returns <code>true</code> if the given node is the extension properties value node otherwise <code>false</code>
     */
    private boolean isExtensionPropertiesValueNode(String nodeName){
        return EXTENSIONPROPERTIES_VALUE.equals(nodeName) && insideExtensionPropertiesNode;
    }     
}
