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

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.faktorips.runtime.IClRepositoryObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.valueset.IntegerRange;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ProductComponentGeneration extends RuntimeObject implements IProductComponentGeneration,
        IClRepositoryObject {

    // the product component this generation belongs to.
    private ProductComponent productCmpt;

    private DateTime validFrom;

    public ProductComponentGeneration(ProductComponent productCmpt) {
        this.productCmpt = productCmpt;
    }

    /**
     * {@inheritDoc}
     */
    public final IProductComponent getProductComponent() {
        return productCmpt;
    }

    public final IProductComponentGeneration getPreviousGeneration() {
        return getRepository().getPreviousProductComponentGeneration(this);
    }

    public final IProductComponentGeneration getNextGeneration() {
        return getRepository().getNextProductComponentGeneration(this);
    }

    /**
     * {@inheritDoc}
     */
    public IRuntimeRepository getRepository() {
        return productCmpt.getRepository();
    }

    public final long getValidFromInMillisec(TimeZone zone) {
        return validFrom.toDate(zone).getTime();
    }

    /**
     * {@inheritDoc}
     */
    public final Date getValidFrom(TimeZone zone) {
        return validFrom.toDate(zone);
    }

    /**
     * Sets the new valid from date.
     * 
     * @throws org.faktorips.runtime.IllegalRepositoryModificationException if the repository this
     *             generation belongs to does not allow to modify its contents. The method is
     *             provided to ease the development of test cases.
     */
    public void setValidFrom(DateTime newValidFrom) {
        if (getRepository() != null && !getRepository().isModifiable()) {
            throw new IllegalRepositoryModificationException();
        }
        if (newValidFrom == null) {
            throw new NullPointerException();
        }
        validFrom = newValidFrom;
    }

    /**
     * Initializes the generation with the data from the xml element.
     * 
     * @throws IllegalRepositoryModificationException if the component has already been initialized
     *             and the repository prohibit changing its contents.
     * 
     * @throws NullPointerException if genElement is <code>null</code>.
     */
    public final void initFromXml(Element genElement) {
        if (validFrom != null && getRepository() != null && !getRepository().isModifiable()) {
            throw new IllegalRepositoryModificationException();
        }
        validFrom = DateTime.parseIso(genElement.getAttribute("validFrom"));
        Map<String, Element> propertyElements = getPropertyElements(genElement);
        doInitPropertiesFromXml(propertyElements);
        doInitTableUsagesFromXml(propertyElements);
        doInitReferencesFromXml(getLinkElements(genElement));
    }

    /**
     * Initializes the properties with the data in the map.
     */
    protected void doInitPropertiesFromXml(Map<String, Element> map) {
        // nothing to do in the base class
        //
        // Note that the method is deliberately not declared as abstract to
        // allow in subclasses calls to super.doInitPropertiesFromXml().
    }

    /**
     * Initializes the links with the data in the map.
     */
    protected void doInitReferencesFromXml(Map<String, List<Element>> map) {
        // nothing to do in the base class
        //
        // Note that the method is deliberately not declared as abstract to
        // allow in subclasses calls to doInitReferencesFromXml().
    }

    /**
     * Initializes the table content usages with the data in the map. The map contains the table
     * structure usage roles as key and the qualified table content name as value.
     */
    protected void doInitTableUsagesFromXml(Map<String, Element> map) {
        // nothing to do in the base class
        //
        // Note that the method is deliberately not declared as abstract to
        // allow in subclasses calls to doInitTableUsagesFromXml().
    }

    /**
     * Returns a map containing the xml elements representing config elements found in the indicated
     * generation's xml element. For each config element the map contains an entry with the
     * pcTypeAttribute's name as key and the xml element containing the config element data as
     * value.
     * 
     * @param genElement An xml element containing a product component generation's data.
     * @throws NullPointerException if genElement is <code>null</code>.
     */
    // note: not private to allow access by test case
    final Map<String, Element> getPropertyElements(Element genElement) {
        Map<String, Element> elementMap = new HashMap<String, Element>();
        NodeList nl = genElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if ("ConfigElement".equals(node.getNodeName())) {
                    Element childElement = (Element)nl.item(i);
                    elementMap.put(childElement.getAttribute("attribute"), childElement);
                } else if ("AttributeValue".equals(node.getNodeName())) {
                    Element childElement = (Element)nl.item(i);
                    elementMap.put(childElement.getAttribute("attribute"), childElement);
                } else if ("TableContentUsage".equals(node.getNodeName())) {
                    Element childElement = (Element)nl.item(i);
                    String structureUsage = childElement.getAttribute("structureUsage");
                    elementMap.put(structureUsage, childElement);
                }

            }
        }
        return elementMap;
    }

    /**
     * Returns a map containing the xml elements representing relations found in the indicated
     * generation's xml element. For each policy component type relation (pcTypeRelation) the map
     * contains an entry with the pcTypeRelation as key. The value is an array list containing all
     * relation elements for the pcTypeRelation.
     * 
     * @param genElement An xml element containing a product component generation's data.
     * @throws NullPointerException if genElement is <code>null</code>.
     */
    // note: not private to allow access by test case
    final Map<String, List<Element>> getLinkElements(Element genElement) {
        Map<String, List<Element>> elementMap = new HashMap<String, List<Element>>();
        NodeList nl = genElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && "Link".equals(node.getNodeName())) {
                Element childElement = (Element)nl.item(i);
                String association = childElement.getAttribute("association");
                List<Element> associationElements = elementMap.get(association);
                if (associationElements == null) {
                    associationElements = new ArrayList<Element>(1);
                    elementMap.put(association, associationElements);
                }
                associationElements.add(childElement);
            }

        }
        return elementMap;
    }

    protected Element getRangeElement(Element configElement) {
        Element valueSetElement = getValueSetElement(configElement);
        return XmlUtil.getFirstElement(valueSetElement, "Range");
    }

    protected NodeList getEnumNodeList(Element configElement) {
        Element enumElement = getEnumElement(configElement);
        NodeList nl = enumElement.getElementsByTagName("Value");
        return nl;
    }

    private Element getEnumElement(Element configElement) {
        Element valueSetElement = getValueSetElement(configElement);
        return XmlUtil.getFirstElement(valueSetElement, "Enum");
    }

    private Element getValueSetElement(Element configElement) {
        if (configElement == null) {
            throw new NullPointerException();
        }
        Element valueSetElement = XmlUtil.getFirstElement(configElement, "ValueSet");
        if (valueSetElement == null) {
            throw new NullPointerException();
        }
        return valueSetElement;
    }

    /**
     * This method for implementations of the <code>doInitReferencesFromXml</code> method to read
     * the cardinality bounds from an xml dom element. An IntegerRange object is created and added
     * to the provided cardinalityMap.
     */
    public static void addToCardinalityMap(Map<String, IntegerRange> cardinalityMap,
            String targetId,
            Element relationElement) {
        String maxStr = relationElement.getAttribute("maxCardinality");
        Integer maxCardinality = null;
        if ("*".equals(maxStr) || "n".equals(maxStr.toLowerCase())) {
            maxCardinality = new Integer(Integer.MAX_VALUE);
        } else {
            maxCardinality = Integer.valueOf(maxStr);
        }

        Integer minCardinality = Integer.valueOf(relationElement.getAttribute("minCardinality"));
        cardinalityMap.put(targetId, new IntegerRange(minCardinality, maxCardinality));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getProductComponent().getId() + "-" + validFrom;
    }

    /**
     * Sets the product component this generation belongs to.
     */
    protected void setProductCmpt(ProductComponent productCmpt) {
        this.productCmpt = productCmpt;
    }

    /**
     * {@inheritDoc}
     */
    public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target) {
        throw new RuntimeException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    public List<IProductComponentLink<? extends IProductComponent>> getLinks() {
        throw new RuntimeException("Not implemented yet.");
    }
}
