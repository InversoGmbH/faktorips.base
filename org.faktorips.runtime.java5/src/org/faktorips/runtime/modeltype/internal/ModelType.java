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

package org.faktorips.runtime.modeltype.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.internal.AbstractRuntimeRepository;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.faktorips.runtime.modeltype.TypeHierarchyVisitor;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelType extends AbstractModelElement implements IModelType {

    private List<IModelTypeAssociation> associations = new ArrayList<IModelTypeAssociation>();
    private Map<String, IModelTypeAssociation> associationsByName = new HashMap<String, IModelTypeAssociation>();

    private List<IModelTypeAttribute> attributes = new ArrayList<IModelTypeAttribute>();
    private Map<String, IModelTypeAttribute> attributesByName = new HashMap<String, IModelTypeAttribute>();

    private String className;
    private String superTypeName;

    public ModelType(AbstractRuntimeRepository repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     */
    public IModelTypeAssociation getDeclaredAssociation(int index) {
        return associations.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public IModelTypeAssociation getDeclaredAssociation(String name) {
        return associationsByName.get(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<IModelTypeAssociation> getDeclaredAssociations() {
        return Collections.unmodifiableList(associations);
    }
    
    /**
     * {@inheritDoc}
     */
    public List<IModelTypeAssociation> getAssociations() {
        AssociationsCollector asscCollector = new AssociationsCollector();
        asscCollector.visitHierarchy(this);
        return asscCollector.result;
    }

    /**
     * {@inheritDoc}
     */
    public IModelTypeAttribute getDeclaredAttribute(int index) throws IndexOutOfBoundsException {
        return attributes.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public IModelTypeAttribute getDeclaredAttribute(String name) throws IllegalArgumentException {
        IModelTypeAttribute attr = attributesByName.get(name);
        if (attr==null) {
            throw new IllegalArgumentException("The type " + this + " hasn't got a declared attribute "  + name);
        }
        return attr;
    }
    
    /**
     * {@inheritDoc}
     */
    public IModelTypeAttribute getAttribute(String name) throws IllegalArgumentException {
        AttributeFinder finder = new AttributeFinder(name);
        finder.visitHierarchy(this);
        if (finder.attribute==null) {
            throw new IllegalArgumentException("The type " + this + "(or one of it's supertypes) hasn't got an attribute "  + name);
        }
        return finder.attribute;
    }

    /**
     * {@inheritDoc}
     */
    public List<IModelTypeAttribute> getDeclaredAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * {@inheritDoc}
     */
    public List<IModelTypeAttribute> getAttributes() {
        AttributeCollector attrCollector = new AttributeCollector();
        attrCollector.visitHierarchy(this);
        return attrCollector.result;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws ClassNotFoundException
     */
    public Class<?> getJavaClass() throws ClassNotFoundException {
        return loadClass(className);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ClassNotFoundException
     */
    public Class<?> getJavaInterface() throws ClassNotFoundException {
        String interfaceName = className.replace(".internal", "");
        interfaceName = interfaceName.substring(0, interfaceName.lastIndexOf('.') + 1) + 'I'
                + interfaceName.substring(interfaceName.lastIndexOf('.') + 1);
        return loadClass(interfaceName);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @SuppressWarnings("unchecked")
    public IModelType getSuperType() {
        if (superTypeName != null && superTypeName.length() > 0) {
            Class superclass = loadClass(superTypeName);
            return getRepository().getModelType(superclass);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        super.initFromXml(parser);
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals("class")) {
                this.className = parser.getAttributeValue(i);
            } else if (parser.getAttributeLocalName(i).equals("supertype")) {
                this.superTypeName = parser.getAttributeValue(i);
            }
        }
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals("ExtensionProperties")) {
                        initExtPropertiesFromXml(parser);
                    } else if (parser.getLocalName().equals("ModelTypeAttributes")) {
                        initModelTypeAttributesFromXML(parser);
                    } else if (parser.getLocalName().equals("ModelTypeAssociations")) {
                        initModelTypeAssociationsFromXML(parser);
                    }
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void initModelTypeAttributesFromXML(XMLStreamReader parser) throws XMLStreamException {
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals("ModelTypeAttribute")) {
                        IModelTypeAttribute attribute = new ModelTypeAttribute(getAbstractRepository());
                        attribute.initFromXml(parser);
                        attributes.add(attribute);
                        attributesByName.put(attribute.getName(), attribute);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("ModelTypeAttributes")) {
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void initModelTypeAssociationsFromXML(XMLStreamReader parser) throws XMLStreamException {
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals("ModelTypeAssociation")) {
                        IModelTypeAssociation association = new ModelTypeAssociation(getAbstractRepository());
                        association.initFromXml(parser);
                        associations.add(association);
                        associationsByName.put(association.getName(), association);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals("ModelTypeAssociations")) {
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        if (superTypeName != null && superTypeName.length() > 0) {
            sb.append(" extends ");
            sb.append(superTypeName);
        }
        return sb.toString();
    }

    static class AttributeCollector extends TypeHierarchyVisitor {

        List<IModelTypeAttribute> result = new ArrayList<IModelTypeAttribute>(30);
        
        @Override
        public boolean visitType(IModelType type) {
            result.addAll(type.getDeclaredAttributes());
            return true;
        }
        
    }
    
    static class AttributeFinder extends TypeHierarchyVisitor {

        private String attrName;
        private IModelTypeAttribute attribute = null;
        
        public AttributeFinder(String attrName) {
            super();
            this.attrName = attrName;
        }


        @Override
        public boolean visitType(IModelType type) {
            attribute = ((ModelType)type).attributesByName.get(attrName);
            return attribute==null;
        }
        
    }
    static class AssociationsCollector extends TypeHierarchyVisitor {

        List<IModelTypeAssociation> result = new ArrayList<IModelTypeAssociation>();
        
        @Override
        public boolean visitType(IModelType type) {
            result.addAll(type.getDeclaredAssociations());
            return true;
        }
        
    }
    
}
