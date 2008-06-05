/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.core;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of the {@link IExtension} interface for testing purposes. Not all of the methods are implemented but can be implemented
 * as needed. Those methods that are not implemented throw a RuntimeException.
 * 
 * @author Peter Erzberger
 */
public class TestExtension implements IExtension {

    private IConfigurationElement[] elements;
    private String simpleIdentifier;
    private String namespaceIdentifier = "";

    /**
     * Creates a TestExtension with the specified IConfigurationElement and simpleIdentifier. 
     */
    public TestExtension(IConfigurationElement[] elements, String simpleIdentifier) {
        this(elements, "", simpleIdentifier);
    }

    /**
     * Creates a TestExtension with the specified IConfigurationElement namespaceIdentifier and simpleIdentifier. 
     */
    public TestExtension(IConfigurationElement[] elements, String namespaceIdentifier, String simpleIdentifier) {
        ArgumentCheck.notNull(elements, this);
        ArgumentCheck.notNull(simpleIdentifier, this);
        this.elements = elements;
        this.simpleIdentifier = simpleIdentifier;
        this.namespaceIdentifier = namespaceIdentifier != null ? namespaceIdentifier : "";
    }

    /**
     * {@inheritDoc}
     */
    public IConfigurationElement[] getConfigurationElements() throws InvalidRegistryObjectException {
        return elements;
    }

    /**
     * Throws RuntimeException
     */
    public IContributor getContributor() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented yet.");
    }

    /**
     * Throws RuntimeException
     */
    public IPluginDescriptor getDeclaringPluginDescriptor() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented yet.");
    }

    /**
     * Throws RuntimeException
     */
    public String getExtensionPointUniqueIdentifier() throws InvalidRegistryObjectException {
        throw new RuntimeException("Not implemented yet.");
    }

    /**
     * Returns an empty label.
     */
    public String getLabel() throws InvalidRegistryObjectException {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public String getNamespace() throws InvalidRegistryObjectException {
        return getNamespaceIdentifier();
    }

    /**
     * {@inheritDoc}
     */
    public String getNamespaceIdentifier() throws InvalidRegistryObjectException {
        return namespaceIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    public String getSimpleIdentifier() throws InvalidRegistryObjectException {
        return simpleIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    public String getUniqueIdentifier() throws InvalidRegistryObjectException {
        StringBuffer buf = new StringBuffer();
        buf.append(namespaceIdentifier);
        if(!StringUtils.isEmpty(namespaceIdentifier)){
            buf.append('.');
            
        }
        buf.append(simpleIdentifier);
        return buf.toString();
    }

    /**
     * Throws RuntimeException
     */
    public boolean isValid() {
        throw new RuntimeException("Not implemented yet.");
    }

}
