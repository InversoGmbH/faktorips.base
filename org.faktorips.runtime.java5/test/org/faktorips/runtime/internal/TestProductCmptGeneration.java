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

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * ProductComponentGeneration for testing purposes.
 * 
 * @author Jan Ortmann
 */
public class TestProductCmptGeneration extends ProductComponentGeneration {

    public TestProductCmptGeneration(ProductComponent productCmpt) {
        super(productCmpt);
    }

    @Override
    protected void doInitPropertiesFromXml(Map<String, Element> map) {
    }

    @Override
    protected void doInitReferencesFromXml(Map<String, List<Element>> map) {
    }


}
