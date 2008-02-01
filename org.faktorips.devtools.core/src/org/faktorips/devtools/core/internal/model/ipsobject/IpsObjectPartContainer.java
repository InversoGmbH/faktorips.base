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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.HashMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ValidationResultCache;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.memento.XmlMemento;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A container for ips object parts.
 * 
 * @author Jan Ortmann
 */
public abstract class IpsObjectPartContainer extends IpsElement implements IIpsObjectPartContainer, IExtensionPropertyAccess {

    /**
     * Name of the xml element the containing the elements for the extension property values.
     */
    protected final static String XML_EXT_PROPERTIES_ELEMENT = "ExtensionProperties"; //$NON-NLS-1$

    /**
     * Name of the xml element containing a property value.
     */
    protected final static String XML_VALUE_ELEMENT = "Value"; //$NON-NLS-1$

    /**
     * Name of the value element's attribute that stores the property id.
     */
    protected final static String XML_ATTRIBUTE_EXTPROPERTYID = "id"; //$NON-NLS-1$

    /**
     * Name of the value element's attribute that stores the information if the value is null or not.
     */
    protected final static String XML_ATTRIBUTE_ISNULL = "isNull"; //$NON-NLS-1$
    
    
    // map containing extension property ids as keys and their values.
    private HashMap extPropertyValues = null;

    // validation start time used for tracing in debug mode
    private long validationStartTime;

    public IpsObjectPartContainer(IIpsElement parent, String name) {
        super(parent, name);
    }

    /**
     * for testing purposes
     */
    public IpsObjectPartContainer() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile getIpsSrcFile() {
        IIpsObject obj = getIpsObject();
        if (obj==null) {
            return null;
        }
        return obj.getIpsSrcFile();
    }

    /**
     * {@inheritDoc}
     * 
     * The IpsObjectPartContainer version does not throw an exception as no
     * resource access is necessary.
     *
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    public abstract IIpsElement[] getChildren();
    
    /**
     * Returns the id that can be used for a new part, so that its id is unique.
     */
    protected int getNextPartId() {
        int maxId = -1;
        IIpsElement[] parts = getChildren();
        for (int i = 0; i < parts.length; i++) {
            IIpsObjectPart part = (IIpsObjectPart)parts[i];
            if (part.getId()>maxId) {
                maxId = part.getId();
            }
        }
        return ++maxId;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getExtPropertyValue(String propertyId) {
        checkExtProperty(propertyId);
        return extPropertyValues.get(propertyId);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExtPropertyDefinitionAvailable(String propertyId) {
        initExtPropertiesIfNotDoneSoFar();
        return extPropertyValues.containsKey(propertyId);
    }

    /**
     * {@inheritDoc}
     */
    public void setExtPropertyValue(String propertyId, Object value) {
        checkExtProperty(propertyId);
        IExtensionPropertyDefinition property = getIpsModel().getExtensionPropertyDefinition(getClass(), propertyId, true);
        if (!property.beforeSetValue(this, value)) {
            return; // veto to set the new value by the property definition
        }
        if (!ObjectUtils.equals(value, getExtPropertyValue(propertyId))) { 
            extPropertyValues.put(propertyId, value);
            objectHasChanged();
        }
        property.afterSetValue(this, value);
    }
    
    protected final boolean valueChanged(Object oldValue, Object newValue) {
        boolean changed = !ObjectUtils.equals(oldValue, newValue);
        if (changed) {
            objectHasChanged();
        }
        return changed;
    }
    
    protected final boolean valueChanged(boolean oldValue, boolean newValue) {
        boolean changed = oldValue != newValue;
        if (changed) {
            objectHasChanged();
        }
        return changed;
    }
    
    protected final boolean valueChanged(int oldValue, int newValue) {
        boolean changed = oldValue != newValue;
        if (changed) {
            objectHasChanged();
        }
        return changed;
    }
    
    /**
     * Called when the object's state has changed to inform about this.
     */
    protected abstract void objectHasChanged();
    
    /**
     * Hass to be called when a part was added to the container to trigger event
     * notification.
     */
    protected void partWasAdded(IIpsObjectPart part) {
        objectHasChanged(ContentChangeEvent.newPartAddedEvent(part));
    }

    protected void partsMoved(IIpsObjectPart[] parts) {
        ContentChangeEvent event = ContentChangeEvent.newPartsChangedPositionsChangedEvent(getIpsSrcFile(), parts);
        objectHasChanged(event);
    }
    
    protected void objectHasChanged(ContentChangeEvent event) {
        IpsModel model = (IpsModel)getIpsModel();
        IpsSrcFileContent content = model.getIpsSrcFileContent(getIpsSrcFile());
        if (content!=null) {
            content.ipsObjectChanged(event);
        } else {
            model.ipsSrcFileContentHasChanged(event);
        }
        
    }
    
    private void checkExtProperty(String propertyId) {
        if (!isExtPropertyDefinitionAvailable(propertyId)) {
            throw new IllegalArgumentException("Extension property " + propertyId + " is not defined for type " + getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    private void initExtPropertiesIfNotDoneSoFar() {
        if (extPropertyValues==null) {
            extPropertyValues = new HashMap();
            IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
            for (int i = 0; i < properties.length; i++) {
                extPropertyValues.put(properties[i].getPropertyId(), properties[i].getDefaultValue());
            }
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    public Element toXml(Document doc) {
        Element newElement = createElement(doc);
        propertiesToXml(newElement);
        extPropertiesToXml(newElement);
        partsToXml(doc, newElement);
        return newElement;
    }
    
    private void extPropertiesToXml(Element element) {
        IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
        if (properties.length==0) {
            return;
        }
        initExtPropertiesIfNotDoneSoFar();        
        Document doc = element.getOwnerDocument();
        Element extPropertiesEl = doc.createElement(XML_EXT_PROPERTIES_ELEMENT);
        element.appendChild(extPropertiesEl);
        for (int i = 0; i < properties.length; i++) {
            Element valueEl = doc.createElement(IpsObjectPartContainer.XML_VALUE_ELEMENT);
            extPropertiesEl.appendChild(valueEl);
            String propertyId = properties[i].getPropertyId();
            valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, propertyId);
            Object value = extPropertyValues.get(propertyId);
            valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, value==null ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
            if (value!=null) {
                properties[i].valueToXml(valueEl, value);
            }
        }
    }

    /**
     * Is called from the toXml() method to create the xml element for this container.
     */
    protected abstract Element createElement(Document doc);

    /*
     * Transforms the parts this container contains to xml elements and adds
     * them to the given xml element.
     * 
     * @param doc xml document used to create new element.
     * @param element the element to which the part elements should be added.
     */
    private void partsToXml(Document doc, Element element) {
        IIpsElement[] children = getChildren();
        for (int i = 0; i < children.length; i++) {
            IIpsObjectPart part = (IIpsObjectPart)children[i];
            Element newPartElement = part.toXml(doc);
            element.appendChild(newPartElement);
        }
    }

    /**
     * The method is called by the toXml() method, so that subclasses can store their properties in 
     * the xml element passed as parameter.
     * 
     * @param element
     */
    protected abstract void propertiesToXml(Element element);

    /** 
     * {@inheritDoc}
     */
    public void initFromXml(Element element) {
    	initFromXml(element, null);
    }
    
    protected void initFromXml(Element element, Integer id) {
        initPropertiesFromXml(element, id);
        initPartContainersFromXml(element);
        initExtPropertiesFromXml(element);
    }

    /**
     * Add the given extension property value identified by the given property id. If the extension
     * property not exists as definitions then the property will be ignored.
     * 
     * @param propertyId id of the extension property
     * @param extPropertyValue extension property value
     */
    protected void addExtensionPropertyValue(String propertyId, String extPropertyValue) {
        initExtPropertiesIfNotDoneSoFar();
        Object value = null;
        if (extPropertyValue != null) {
            IExtensionPropertyDefinition property = findExtensionProperty(propertyId, getExtensionProperties());
            if (property == null) {
                return;
            }
            value = property.getValueFromString(extPropertyValue);
        }
        extPropertyValues.put(propertyId, value);
    }
    
    /**
     * The method is called by the initFromXml() method, so that subclasses can load their properties from
     * the xml element passed as parameter.
     * 
     * @param element 
     * @param id The value for the id-property of the ips object part or null, if the id should be generated 
     *           automatically (preferred).
     */
    protected abstract void initPropertiesFromXml(Element element, Integer id);

    /*
     * The method is called by the initFromXml() method to retrieve the values of the
     * extension properties.
     * 
     * @param element The &ltExtensionProperties&gt element.
     */
    private void initExtPropertiesFromXml(Element containerEl) {
        extPropertyValues = null;
        initExtPropertiesIfNotDoneSoFar(); // to make sure that new extension properties are initialized with their default.
        Element extPropertiesEl = XmlUtil.getFirstElement(containerEl, XML_EXT_PROPERTIES_ELEMENT);
        if (extPropertiesEl==null) {
            extPropertyValues = null;
            return;
        }
        HashMap extPropertyDefinitions = getExtensionProperties();
        NodeList nl = extPropertiesEl.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType()==Node.ELEMENT_NODE && node.getNodeName().equals(IpsObjectPartContainer.XML_VALUE_ELEMENT)) {
                initExtPropertyFromXml((Element)node, extPropertyDefinitions);
            }
        }
    }
    
    /*
     * The method is called by the initFromXml() method to retrieve the values of the
     * extension properties.
     * 
     * @param element The &ltExtensionProperties&gt element.
     */
    private void initExtPropertyFromXml(Element valueElement, HashMap extPropertyDefinitions) {
        String propertyId = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID);
        Object value = null;
        String isNull = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        if (StringUtils.isEmpty(isNull) || !Boolean.valueOf(isNull).booleanValue()) {
            IExtensionPropertyDefinition property = findExtensionProperty(propertyId, extPropertyDefinitions);
            if (property==null) {
                return;
            }
            value = property.getValueFromXml(valueElement);
        }
        extPropertyValues.put(propertyId, value);
    }

    private HashMap getExtensionProperties() {
        HashMap propMap = new HashMap();
        IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
        for (int i = 0; i < properties.length; i++) {
            propMap.put(properties[i].getPropertyId(), properties[i]);
        }
        return propMap;
    }

    /*
     * Searchs an extension property using the given id. Returns null if no such extension property exists.
     */
    private IExtensionPropertyDefinition findExtensionProperty(String propertyId, HashMap extPropertyDefinitions) {
        IExtensionPropertyDefinition property = (IExtensionPropertyDefinition)extPropertyDefinitions.get(propertyId);
        if (property==null) {
            IpsPlugin.log(new IpsStatus(IpsStatus.WARNING, "Extension property " + propertyId + " for " + this + " is unknown")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        return property;
    }

    private void initPartContainersFromXml(Element element) {
        HashMap idPartMap = createIdPartMap();
        reinitPartCollections();
        NodeList nl = element.getChildNodes();
        int nextId = getMaxIdUsedInXml(element) + 1;
        for (int i=0; i<nl.getLength(); i++) {
        	Node item = nl.item(i);
            if (item.getNodeType()!=Node.ELEMENT_NODE || DescriptionHelper.XML_ELEMENT_NAME.equals(item.getNodeName())) {
                continue;
            }
            Element partEl = (Element)item;
            if (partEl.getNodeName().equals(XML_EXT_PROPERTIES_ELEMENT)) {
            	continue;
            }
            String id = partEl.getAttribute("id").trim(); //$NON-NLS-1$
            IIpsObjectPart part = (IIpsObjectPart)idPartMap.get(id);
            if (part==null) {
                part = newPart(partEl, nextId++);
            } else {
                reAddPart(part);
            }
            if (part!=null) {
                // part might may be null if the element does not represent a part!
                part.initFromXml(partEl);
            }
        }
        return;
    }

    private HashMap createIdPartMap() {
        HashMap map = new HashMap();
        IIpsElement[] parts = getChildren();
        for (int i = 0; i < parts.length; i++) {
            IIpsObjectPart part = (IIpsObjectPart)parts[i];
            map.put(""+part.getId(), part); //$NON-NLS-1$
        }
        return map;
    }

    private int getMaxIdUsedInXml(Element element) {
        int maxId = 0;
        NodeList nl = element.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i).getNodeType()==Node.ELEMENT_NODE) {
                Element partEl = (Element)nl.item(i);
                String id = partEl.getAttribute("id"); //$NON-NLS-1$
                if (!StringUtils.isEmpty(id)) {
                    int partId = Integer.parseInt(id);
                    if (partId > maxId) {
                        maxId = partId;
                    }
                }
            }
        }
        return maxId;
    }

    /**
     * This method is called during the initFromXml processing. Subclasses should clear all
     * collections that hold references to parts, e.g. for IPolicyCmptType: Collections for 
     * attributes, methods and so on have to be cleared.
     */
    protected abstract void reinitPartCollections();

    /**
     * This method is called during the initFromXml processing, when the part has been part
     * of the parent before the xml initialization and is still be found in the xml (the part's id
     * is found in the xml). Subclasses must override this method so that the part is added to
     * the correct collection, e.g. for IPolicyCmptType: if the part is an IAttribute, the part must be
     * added to the <code>attributes</code> list.
     * 
     * @throws RuntimeException if the part can't be readded, e.g. because it's type is unknown.
     */
    protected abstract void reAddPart(IIpsObjectPart part);
    
    /**
     * Removes the given part from the container.
     */
    protected abstract void removePart(IIpsObjectPart part);

    /**
     * This method is called during the initFromXml processing to create a new part object for 
     * the given element with the given id. Subclasses must create the right part based on
     * the xml element, e.g. for IPolicyCmptType: if the element name is <code>Attribute</code>
     * an <code>IAttribute</code> is created.
     * <p>
     * Note: It is <strong>NOT</strong> neccessary to fully initialize the part, this is 
     * done later by the caller calling initFromXml().
     * 
     * @return a new part with the given id, or <code>null</code> if the xml tag name is unknown.
     */
    protected abstract IIpsObjectPart newPart(Element xmlTag, int id);
    
	/**
     * {@inheritDoc}
     */
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
        if(isHistoricPartContainer()){
            return new MessageList();
        }
        MessageList result = beforeValidateThis();
        if (result != null){
            return result;
        }
        
        result = new MessageList();
        validateThis(result, ipsProject);

        afterValidateThis(result, ipsProject);
        return result;
    }
    
    /**
     * Before validation method. Perform operations which will be executed before this object part
     * container will be validated. Returns the cached validation result for the given container or
     * <code>null</code> if the cache does not contain a result for the container.
     */
    protected MessageList beforeValidateThis() {
        MessageList result = getValidationCache().getResult(this);
        if (result!=null) {
            if (IpsModel.TRACE_VALIDATION) {
                System.out.println("Validation of " + this + ": Got result from cache."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return result;
        }
        
        if (IpsModel.TRACE_VALIDATION) {
            validationStartTime = System.currentTimeMillis();
            System.out.println("Validation of " + this + ": Started."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return result;
    }

    /**
     * After validation method. Perform operations which will be executed after validation of this
     * object part container.
     */
    protected void afterValidateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        validateExtensionProperties(result);
        validateChildren(result, ipsProject);
        if (IpsModel.TRACE_VALIDATION) {
            System.out.println("Validation of " + this + ": Finsihed, took " + (System.currentTimeMillis() - validationStartTime) + "ms."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            validationStartTime = -1;
        }
        getValidationCache().putResult(this, result);        
    }
    
    private ValidationResultCache getValidationCache() {
        IpsModel model = (IpsModel)getIpsModel();
        ValidationResultCache cache = model.getValidationResultCache();
        return cache;
    }

    /**
     * Validates part container's children.
     */
    protected void validateChildren(MessageList result, IIpsProject ipsProject) throws CoreException {
        IIpsElement[] children = getChildren();
        for (int i=0; i<children.length; i++) {
            MessageList childResult = ((IpsObjectPartContainer)children[i]).validate(ipsProject);
            result.add(childResult);
        }
    }
    
    /**
     * Returns true if this <code>IpsObjectPartContainer</code> is part of an <code>IIpsSrcFile</code>
     * that is marked as historic. If no srcfile can be found, false is returned.
     * @return True only if the parent srcfile is historic, false otherwise.
     */
    private boolean isHistoricPartContainer() {
        IIpsElement container= this;
        while(container != null){
            if(container instanceof IIpsSrcFile &&
               ((IIpsSrcFile) container).isHistoric()){
                return true;
            }
            container= container.getParent();
        }
        return false;
    }    
    
    /**
     * Validates the object and reports invalid states by adding 
     * validation messages to the list. This is an application of the collecting
     * parameter pattern.
     * 
     * @throws NullPointerException if list is null.
     */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        return;
    }
    
    /**
     * Validates the extension property values.
     * 
     * @param ml The message list to which messages generated during the validation are added.
     * 
     * @throws CoreException if an error occurs while validation the extension properties.
     */
    protected void validateExtensionProperties(MessageList ml) throws CoreException {
        IExtensionPropertyDefinition[] properties = getIpsModel().getExtensionPropertyDefinitions(getClass(), true);
        for (int i = 0; i < properties.length; i++) {
        	Object value = getExtPropertyValue(properties[i].getPropertyId());
        	MessageList newList = properties[i].validate(this, value);
        	if (newList!=null) {
        		ml.add(newList);
        	}
        }
    }

    /**
     * {@inheritDoc}
     */
    public Memento newMemento() {
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        return new XmlMemento(this, toXml(doc));
    }
    
    /**
     * {@inheritDoc}
     */
    public void setState(Memento memento) {
        if (!memento.getOriginator().equals(this)) {
            throw new IllegalArgumentException("Memento " + memento + " wasn't created by " + this); //$NON-NLS-1$ //$NON-NLS-2$
        }
        initFromXml(((XmlMemento)memento).getState());
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other) {
    	return this==other;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
    	return System.identityHashCode(this);
    }
}
