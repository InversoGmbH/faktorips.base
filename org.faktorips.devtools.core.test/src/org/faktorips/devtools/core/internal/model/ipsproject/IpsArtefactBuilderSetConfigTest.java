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

package org.faktorips.devtools.core.internal.model.ipsproject;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class IpsArtefactBuilderSetConfigTest extends AbstractIpsPluginTest {
    
    public void testInitFromXml() throws Exception {
        IpsArtefactBuilderSetConfigModel config = new IpsArtefactBuilderSetConfigModel();
        Document doc = getTestDocument();        
        config.initFromXml(doc.getDocumentElement());
        
        assertEquals("one", config.getPropertyValue("prop1"));
        assertEquals("two", config.getPropertyValue("prop2"));
    }
    
    public void testToXml() throws Exception {
        IpsArtefactBuilderSetConfigModel config = new IpsArtefactBuilderSetConfigModel();
        Document doc = getTestDocument();        
        config.initFromXml(doc.getDocumentElement());
        
        Element el = config.toXml(doc);
        IpsArtefactBuilderSetConfigModel newConfig = new IpsArtefactBuilderSetConfigModel();
        newConfig.initFromXml(el);
        
        assertEquals("one", newConfig.getPropertyValue("prop1"));
        assertEquals("two", newConfig.getPropertyValue("prop2"));
    }
}
