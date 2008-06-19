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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.values.EnumType;
import org.faktorips.values.EnumValue;


/**
 *
 */
public class EnumValueField extends ComboField {
    
    // array that contains all values used in the the combo box in the same order
    private EnumValue[] usedEnumValues;
    
    /**
     * @param combo
     */
    public EnumValueField(Combo combo, EnumType enumType) {
        super(combo);
        String[] items = combo.getItems();
        EnumValue[] allEnumValues = enumType.getValues();
        usedEnumValues = new EnumValue[items.length];
        for (int i = 0; i < items.length; i++) {
            usedEnumValues[i] = getEnumValue(allEnumValues, items[i]);
            if (usedEnumValues[i]==null) {
                throw new RuntimeException("Not enum value for combo box item " + items[i]);
            }
        }
    }
    
    private EnumValue getEnumValue(EnumValue[] allValues, String name) {
        for (int i = 0; i < allValues.length; i++) {
            if (allValues[i].getName().equals(name)) {
                return allValues[i];
            }
        }
        return null;
    }
    
    public EnumValue getEnumValue() {
        int index = getCombo().getSelectionIndex();
        if (index==-1) {
            return null;
        }
        return usedEnumValues[index];
    }

    /**
     * {@inheritDoc}
     */
    public Object parseContent() {
        return getEnumValue();
    }
    
    public void setEnumValue(EnumValue newValue) {
        getCombo().setText(newValue.getName());
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(Object newValue) {
        setEnumValue((EnumValue)newValue);
    }

}
