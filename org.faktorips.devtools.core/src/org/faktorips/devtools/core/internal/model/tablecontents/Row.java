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

package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 *
 */
public class Row extends AtomicIpsObjectPart implements IRow {
    
    final static String TAG_NAME = "Row"; //$NON-NLS-1$
    final static String VALUE_TAG_NAME = "Value"; //$NON-NLS-1$
    
    private ArrayList values;
    
    private int rowNumber= 0;

    Row(TableContentsGeneration parent, int rowNum) {
        super(parent, rowNum);
        initValues();
    }
    
    /**
     * {@inheritDoc}
     */
    public ITableContents getTableContents() {
        return (ITableContents)getParent().getParent();
    }


    private int getNumOfColumnsViaTableContents() {
        return ((ITableContents)getParent().getParent()).getNumOfColumns();
    }
    
    private void initValues() {
        initValues(getNumOfColumnsViaTableContents());
    }
    
    /*
     * Initializes the row's values with blanks
     */
    private void initValues(int numOfColumns) {
        values = new ArrayList(numOfColumns+5);
        for (int i=0; i<numOfColumns; i++) {
            values.add(""); //$NON-NLS-1$
        }
    }
    
	/**
	 * {@inheritDoc}
	 */
    public String getName() {
        return String.valueOf(getId());
    }

	/**
	 * {@inheritDoc}
	 */
    public int getRowNumber() {
        return rowNumber;
    }
    
    /**
     * Sets the rownumber of this row. To keep row numbers up to date the tableContents object 
     * calls this method every time the list of rows changes.
     * @param rowNumber
     */
    void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }
    
	/**
	 * {@inheritDoc}
	 */
    public String getValue(int column) {
        return (String)values.get(column);
    }

	/**
	 * {@inheritDoc}
	 */
    public void setValue(int column, String newValue) {
        setValueInternal(column, newValue);
        objectHasChanged();
    }

    public void setValueInternal(int column, String newValue) {
        values.set(column, newValue);
    }
    
    void newColumn(int insertAfter, String defaultValue) {
    	if (insertAfter < values.size()) {
    		values.add(Math.max(0, insertAfter), defaultValue);
    	}
    	else {
    		values.add(defaultValue);
    	}
        objectHasChanged();
    }
    
    void removeColumn(int column) {
    	column = Math.max(0, column);
    	column = Math.min(values.size(), column);
    	values.remove(column);
    }
    
	/**
	 * {@inheritDoc}
	 */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("TableRow.gif"); //$NON-NLS-1$
    }

    /*
     * Returns the element's first text node.
     */
    private Text getTextNode(Element valueElement) {
        NodeList nl = valueElement.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i) instanceof Text) {
                return (Text)nl.item(i);
            }
        }
        return null;
    }
    
	/**
     * Validates the values in this row against unique-keys and datatypes defined by the TableStructure
     * of this row's TableContents.
     * <p>
     * For every unique key the TableStructure defines all columns that are part of the unique key are 
     * processed. If a column of this row does not contain a value as dictated by unique keys, a new
     * <code>ERROR</code>-<code>Message</code> is added to the given <code>MessageList</code>.
     * <p>
     * The datatype for every column is retrieved and the corresponding value is tested. If the value
     * does not match the datatype (is not parsable) a new <code>ERROR</code>-<code>Message</code> is 
     * added to the given <code>MessageList</code>.
	 * {@inheritDoc}
	 */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        ITableStructure tableStructure = ((TableContents)getTableContents()).findTableStructure(ipsProject);
        if(tableStructure == null){
            return;
        }
        ValueDatatype[] datatypes= ((TableContents)getTableContents()).findColumnDatatypes(tableStructure, ipsProject);
        validateThis(list, tableStructure, datatypes);
    }
    
    MessageList validateThis(ITableStructure tableStructure, ValueDatatype[] datatypes, IIpsProject ipsProject) throws CoreException {
        MessageList result = beforeValidateThis();
        if (result != null){
            return result;
        }
        
        result = new MessageList();
        validateThis(result, tableStructure, datatypes);
        
        afterValidateThis(result, ipsProject);
        
        return result;
    }

    private void validateThis(MessageList result, ITableStructure tableStructure, ValueDatatype[] datatypes) throws CoreException {
        IUniqueKey[] uniqueKeys = tableStructure.getUniqueKeys();
        validateMissingUniqueKeyValue(result, datatypes, tableStructure, uniqueKeys);
        validateRowValue(result, tableStructure, datatypes);
        if(result.isEmpty()){
            validateNameColumnIfTableBasedEnum(result, tableStructure, uniqueKeys);
        }
    }
    
    private void validateNameColumnIfTableBasedEnum(MessageList msgList, ITableStructure structure, IUniqueKey[] uniqueKeys){
        if(!structure.isModelEnumType()){
            return;
        }
        
        if (!structure.getIpsProject().getIpsArtefactBuilderSet().isTableBasedEnumValidationRequired()) {
        	return;
        }
        
       //TODO pk: this is already implemented in UniqueKey. Can we get rid of this?
        if(uniqueKeys.length < 2){
            return;
        }
        IKeyItem[] items = uniqueKeys[1].getKeyItems();
        if(items.length != 1){
            return;
        }
        String value = getValue(1);
        if(!JavaConventions.validateIdentifier(value).isOK()){
            msgList.add(new Message(MSGCODE_VALID_NAME_WHEN_TABLE_ENUM_TYPE, Messages.Row_NameMustBeValidJavaIdentifier, Message.ERROR, new ObjectProperty(this, IRow.PROPERTY_VALUE, 1)));
        }
    }
    
    /*
     * Validates this row using the given list of datatypes.
     */
    private void validateMissingUniqueKeyValue(MessageList list, ValueDatatype[] datatypes, 
            ITableStructure structure, IUniqueKey[] uniqueKeys) throws CoreException {

        //this validation can only be applied if the colum sizes of the structure and content are consistent.
        //there must be a different rule that validates this consistency
        if(structure.getNumOfColumns() != getTableContents().getNumOfColumns()){
            return;
        }
        for (int i = 0; i < uniqueKeys.length; i++) {
            IUniqueKey uniqueKey= uniqueKeys[i];
            IKeyItem[] keyItems= uniqueKey.getKeyItems();
            for (int j = 0; j < keyItems.length; j++) {
                IKeyItem keyItem= keyItems[j];
                if(keyItem instanceof IColumn){
                    IColumn column= (IColumn) keyItem;
                    int columnIndex= structure.getColumnIndex(column);
                    String value= getValue(columnIndex);
                    if(value!=null && StringUtils.isEmpty(value.trim()) || value==null){
                        String text = NLS.bind(Messages.Row_MissingValueForUniqueKey, column.getName());
                        Message message= new Message(MSGCODE_UNDEFINED_UNIQUEKEY_VALUE, text, Message.ERROR, new ObjectProperty(this, IRow.PROPERTY_VALUE, columnIndex));
                        list.add(message);
                    }
                }
            }
        }
    }

    private void validateRowValue(MessageList list, ITableStructure structure, ValueDatatype[] datatypes){
        int numOfColumnsInStructure = structure.getNumOfColumns();
        for (int i=0; i< getNumOfColumnsViaTableContents(); i++) {
            if (i >= numOfColumnsInStructure){
                // datatypes couldn't be checked because structure contains no more columns
                return;
            }
            IColumn column= structure.getColumn(i);
            
            ValueDatatype dataType= datatypes[i];
            if(dataType == null || !dataType.isParsable(getValue(i))){
                String text = NLS.bind(Messages.Row_ValueNotParsable, column.getName(), dataType);
                Message message= new Message(MSGCODE_VALUE_NOT_PARSABLE, text, Message.ERROR, new ObjectProperty(this, IRow.PROPERTY_VALUE, i));
                list.add(message);
            }
        }
    }
    
	/**
	 * {@inheritDoc}
	 */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
	/**
	 * {@inheritDoc}
	 */
    protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		NodeList nl = element.getElementsByTagName(VALUE_TAG_NAME);
		initValues();
		for (int i = 0; i < values.size(); i++) {
			if (i < nl.getLength()) {
				Element valueElement = (Element) nl.item(i);
				String isNull = valueElement.getAttribute("isNull"); //$NON-NLS-1$
				if (Boolean.valueOf(isNull).booleanValue()) {
					values.set(i, null);
				} else {
					Text textNode = getTextNode(valueElement);
					if (textNode == null) {
						values.set(i, ""); //$NON-NLS-1$
					} else {
						values.set(i, textNode.getNodeValue());
					}
				}
			}
		}
	}
    
	/**
	 * {@inheritDoc}
	 */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        for (int i=0; i<values.size(); i++) {
            Element valueElement = doc.createElement(VALUE_TAG_NAME);
            valueElement.setAttribute("isNull", values.get(i)==null?"true":"false");     //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (values.get(i)!=null) {
                valueElement.appendChild(doc.createTextNode((String)values.get(i)));
            }
            element.appendChild(valueElement);
        }
    }

}
