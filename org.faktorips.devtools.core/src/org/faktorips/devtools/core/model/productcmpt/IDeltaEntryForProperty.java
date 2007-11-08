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

package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;

/**
 * Delta entry for a product definition property.
 * 
 * @author Jan Ortmann
 */
public interface IDeltaEntryForProperty extends IDeltaEntry {

    /**
     * Returns the type of the property this entry refers.
     */
    public ProdDefPropertyType getPropertyType();
    
    /**
     * Returns the name of the product definition property this entry relates.
     */
    public String getPropertyName();
    
}
