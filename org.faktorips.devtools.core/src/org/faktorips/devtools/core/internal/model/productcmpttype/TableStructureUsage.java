/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of ITableStructureUsage.
 * 
 * @author Joerg Ortmann
 */
public class TableStructureUsage extends IpsObjectPart implements ITableStructureUsage {
    
    final static String TAG_NAME = "TableStructureUsage"; //$NON-NLS-1$
    
    final static String TAG_NAME_TABLE_STRUCTURE = "TableStructure"; //$NON-NLS-1$
    
    private boolean mandatoryTableContent = false;
    
    // Contains the related table structures identified by the full qualified name
    private List tableStructures = new ArrayList();
    
    public TableStructureUsage(IProductCmptType pcType, int id) {
        super(pcType, id);
    }

    /**
     * Constructor for testing purposes.
     */
    public TableStructureUsage() {
    }
    
    IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        int numOfChildren = tableStructures.size();
        IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
        List childrenList = new ArrayList(numOfChildren);
        childrenList.addAll(tableStructures);
        childrenList.toArray(childrenArray);
        return childrenArray;
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
    public IIpsObjectPart newPart(Class partType) {
        if (partType.equals(TableStructureReference.class)){
            return newTableStructureReference();
        }
        throw new RuntimeException("Unkown part type " + partType); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof TableStructureReference) {
            this.tableStructures.remove(part);
        }
        throw new RuntimeException("Unkown part type " + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(TAG_NAME_TABLE_STRUCTURE)){
            return newTableStructureReferenceInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name" + xmlTagName); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("TableStructure.gif"); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        tableStructures.clear();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
       if (part instanceof TableStructureReference){
            tableStructures.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_ROLENAME);
        mandatoryTableContent = Boolean.valueOf(element.getAttribute(PROPERTY_MANDATORY_TABLE_CONTENT)).booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_ROLENAME, name);
        element.setAttribute(PROPERTY_MANDATORY_TABLE_CONTENT, "" + mandatoryTableContent); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getRoleName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public void setRoleName(String newRoleName) {
        String oldRoleName = name;
        name = newRoleName;
        valueChanged(oldRoleName, newRoleName);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMandatoryTableContent() {
        return mandatoryTableContent;
    }

    /**
     * {@inheritDoc}
     */
    public void setMandatoryTableContent(boolean mandatoryTableContent) {
        boolean oldIsMandatory = this.mandatoryTableContent;
        this.mandatoryTableContent = mandatoryTableContent;
        valueChanged(oldIsMandatory, mandatoryTableContent);
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getTableStructures() {
        String[] result = new String[tableStructures.size()];
        for (int i = 0; i < result.length; i++) {
            TableStructureReference tsr = (TableStructureReference) tableStructures.get(i);
            result[i] = tsr.getTableStructure();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void addTableStructure(String tableStructure) {
        if (getTableStructureReference(tableStructure) != null){
            // the table structure is already assign, do nothing
            return;
        }
        TableStructureReference tsr = newTableStructureReferenceInternal(getNextPartId());
        tsr.setTableStructure(tableStructure);
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUsed(String tableStructure) {
        if (tableStructure==null) {
            return false;
        }
        for (Iterator it = tableStructures.iterator(); it.hasNext();) {
            TableStructureReference tsr = (TableStructureReference )it.next();
            if (tableStructure.equals(tsr.tableStructure)) {
                return true;
            }
        }
        return false;
    }

    private TableStructureReference newTableStructureReference() {
        TableStructureReference tsr = newTableStructureReferenceInternal(getNextPartId());
        objectHasChanged();
        return tsr;
    }
    
    /*
     * Creates a new table structure usage without updating the src file.
     */
    private TableStructureReference newTableStructureReferenceInternal(int id) {
        TableStructureReference tsr = new TableStructureReference(this, id);
        tableStructures.add(tsr);
        return tsr;
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeTableStructure(String tableStructure) {
        TableStructureReference toBeDeleted = getTableStructureReference(tableStructure);
        if (toBeDeleted != null){
            tableStructures.remove(toBeDeleted);
            objectHasChanged();
        }
    }

    /*
     * Returns the table structure assignment object by the given name, if there is not table
     * structure assignet return <code>null</code>
     */
    private TableStructureReference getTableStructureReference(String tableStructure) {
        for (Iterator iter = tableStructures.iterator(); iter.hasNext();) {
            TableStructureReference tsr = (TableStructureReference)iter.next();
            if (tsr.getTableStructure() != null && tsr.getTableStructure().equals(tableStructure)) {
                return tsr;
            }
        }
        return null;
    }
    
    /*
     * {@inheritDoc}
     */
    public void removeTableStructure(TableStructureReference tableStructureAssignment) {
        if (tableStructures.remove(tableStructureAssignment)) {
            objectHasChanged();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int[] moveTableStructure(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(tableStructures);
        return mover.move(indexes, up);
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
                
        // check the correct name format
        IStatus status = JavaConventions.validateFieldName(name);
        if (!status.isOK()){
            String text = NLS.bind(Messages.TableStructureUsage_msgInvalidRoleName, name);
            Message msg = new Message(MSGCODE_INVALID_ROLE_NAME, text, Message.ERROR, this, PROPERTY_ROLENAME);
            list.add(msg);
        }
        
        // check that at least one table structure is referenced
        if (tableStructures.size() == 0){
            String text = Messages.TableStructureUsage_msgAtLeastOneStructureMustBeReferenced;
            Message msg = new Message(MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE, text, Message.ERROR, this, PROPERTY_TABLESTRUCTURE);
            list.add(msg);
        }

        validateRoleNameInSupertypeHierarchy(list);
        validateDuplicateRoleName(list);
    }
    
    private void validateDuplicateRoleName(MessageList msgList){
        ITableStructureUsage[] tabeStructureUsages = getProductCmptType().getTableStructureUsages();
        for (int i = 0; i < tabeStructureUsages.length; i++) {
            if(!(tabeStructureUsages[i].getId() == getId()) && 
                    tabeStructureUsages[i].getRoleName().equals(getRoleName())){
                String text = NLS.bind(Messages.TableStructureUsage_msgSameRoleName, getRoleName());
                msgList.add(new Message(MSGCODE_SAME_ROLENAME, text, Message.ERROR));
            }
        }
    }

    private void validateRoleNameInSupertypeHierarchy(MessageList msgList) throws CoreException{
        IProductCmptType supertype = getProductCmptType().findSuperProductCmptType(getIpsProject());
        if (supertype==null) {
            return;
        }
        ITableStructureUsage tsu = supertype.findTableStructureUsage(name, getIpsProject());
        if(tsu != null){
            String msg = NLS.bind(Messages.TableStructureUsage_msgRoleNameAlreadyInSupertype, getRoleName());
            msgList.add(new Message(MSGCODE_ROLE_NAME_ALREADY_IN_SUPERTYPE, 
                    msg, Message.ERROR));
        }
    }

    private class TableStructureReference extends AtomicIpsObjectPart {

        private String tableStructure = ""; //$NON-NLS-1$
        
        public TableStructureReference(ITableStructureUsage tableStructureUsage, int id) {
            super(tableStructureUsage, id);
        }
        
        /**
         * {@inheritDoc}
         */
        protected Element createElement(Document doc) {
            return doc.createElement(TAG_NAME_TABLE_STRUCTURE);
        }

        /**
         * {@inheritDoc}
         */
        public Image getImage() {
            return IpsPlugin.getDefault().getImage("TableStructure.gif"); //$NON-NLS-1$
        }
        
        /**
         * {@inheritDoc}
         */
        protected void initPropertiesFromXml(Element element, Integer id) {
            super.initPropertiesFromXml(element, id);
            tableStructure = element.getAttribute(PROPERTY_TABLESTRUCTURE);
        }

        /**
         * {@inheritDoc}
         */
        protected void propertiesToXml(Element element) {
            super.propertiesToXml(element);
            element.setAttribute(PROPERTY_TABLESTRUCTURE, tableStructure);
        }

        /**
         * Returns the table structure.
         */
        public String getTableStructure() {
            return tableStructure;
        }

        /**
         * Sets the table structure.
         */
        public void setTableStructure(String tableStructure) {
            this.tableStructure = tableStructure;
        }

        /**
         * {@inheritDoc}
         */
        protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
            super.validateThis(list, ipsProject);
            if (getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, getTableStructure()) == null){
                String text = NLS.bind(Messages.TableStructureUsage_msgTableStructureNotExists, getTableStructure());
                Message msg = new Message(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND, text, Message.ERROR, this);
                list.add(msg);
            }
        }
    }


    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyName() {
        return name;
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public ProdDefPropertyType getProdDefPropertyType() {
        return ProdDefPropertyType.TABLE_CONTENT_USAGE;
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyDatatype() {
        return ""; //$NON-NLS-1$
    }
    
}
