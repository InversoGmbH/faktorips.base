/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype;

import java.util.Locale;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.IRuntimeRepository;

/**
 * Base Interface for all model elements.
 * 
 * @author Daniel Hohenberger
 */
public interface IModelElement {

    public static final String PROPERTY_NAME = "name";

    public static final String DESCRIPTIONS_XML_TAG = "Description";

    public static final String DESCRIPTIONS_XML_WRAPPER_TAG = "Descriptions";

    public static final String DESCRIPTIONS_PROPERTY_LOCALE = "locale";

    public static final String LABELS_XML_TAG = "Label";

    public static final String LABELS_XML_WRAPPER_TAG = "Labels";

    public static final String LABELS_PROPERTY_LOCALE = "locale";

    public static final String LABELS_PROPERTY_VALUE = "value";

    public static final String LABELS_PROPERTY_PLURAL_VALUE = "pluralValue";

    public static final String EXTENSION_PROPERTIES_XML_TAG = "Value";

    public static final String EXTENSION_PROPERTIES_XML_WRAPPER_TAG = "ExtensionProperties";

    public static final String EXTENSION_PROPERTIES_PROPERTY_ID = "id";

    public static final String EXTENSION_PROPERTIES_PROPERTY_NULL = "isNull";

    /**
     * @param propertyId the id of the desired extension property. Returns the value of the
     *            extension property defined by the given <code>propertyId</code> or
     *            <code>null</code> if the extension property's <code>isNull</code> attribute is
     *            <code>true</code>.
     * @throws IllegalArgumentException if no such property exists.
     */
    public Object getExtensionPropertyValue(String propertyId) throws IllegalArgumentException;

    /**
     * Returns a set of the extension property ids defined for this element.
     */
    public Set<String> getExtensionPropertyIds();

    /**
     * Returns the name of this model type.
     */
    public String getName();

    /**
     * Initializes the model element's state with the data stored in the xml element at the parser's
     * current position.
     */
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException;

    /**
     * Initializes the model element's extension properties with the data stored in the xml element
     * at the parser's current position. This method assumes that the element is
     * <code>&lt;ExtensionProperties&gt;</code>.
     */
    public void initExtPropertiesFromXml(XMLStreamReader parser) throws XMLStreamException;

    /**
     * Returns the repository this model element belongs to. This method never returns
     * <code>null</code>.
     */
    public IRuntimeRepository getRepository();

    /**
     * Returns the label for the given locale.
     * <p>
     * Returns the element's name if no label exists for the given locale.
     */
    public String getLabel(Locale locale);

    /**
     * Returns the description for the given locale.
     * <p>
     * Returns an empty string if no description exists for the given locale.
     */
    public String getDescription(Locale locale);

}
