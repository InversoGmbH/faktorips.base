/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * 
 * @author Jan Ortmann
 */
public class PropertyTypeMismatchEntry extends AbstractDeltaEntryForProperty {

    private final IProductCmptProperty property;
    private final IPropertyValue value;
    private final IPropertyValueContainer propertyValueContainer;

    public PropertyTypeMismatchEntry(IPropertyValueContainer poIPropertyValueContainer, IProductCmptProperty property,
            IPropertyValue value) {
        super(value);
        this.propertyValueContainer = poIPropertyValueContainer;
        this.property = property;
        this.value = value;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.PROPERTY_TYPE_MISMATCH;
    }

    @Override
    public String getDescription() {
        String desc = Messages.PropertyTypeMismatchEntry_desc;
        String label = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(property);
        return NLS.bind(desc, new Object[] { label, property.getProductCmptPropertyType().getName(),
                value.getPropertyType().getName() });
    }

    @Override
    public void fix() {
        value.delete();
        propertyValueContainer.newPropertyValue(property);
    }

}
