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

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of AbstractEnumDatatypeBasedField that displays the values of an EnumValueSet.
 * If the EnumDatatype the EnumValueSet is based on, supports value names these are displayed
 * instead of the value ids.
 * 
 * @author Peter Erzberger
 */
public class EnumValueSetField extends AbstractEnumDatatypeBasedField {

    private IEnumValueSet valueSet;

    /**
     * Creates a new EnumValueSetField.
     * 
     * @param combo the control of this EditField
     * @param valueSet the value set which is displayed by this edit field
     * @param datatype the datatype the value set bases on
     */
    public EnumValueSetField(Combo combo, IEnumValueSet valueSet, ValueDatatype datatype) {
        super(combo, datatype);
        ArgumentCheck.notNull(valueSet, this);
        this.valueSet = valueSet;
        reInitInternal();
    }

    protected final void reInitInternal() {
        List<String> ids = new ArrayList<String>();
        ids.addAll(Arrays.asList(valueSet.getValues()));
        ArrayList<String> names = new ArrayList<String>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            names.add(getDisplayTextForValue(valueSet.getValue(i)));
        }

        // add the null representation entry if not exists in list
        if (!valueSet.containsValue(null)) {
            names.add(0, getDisplayTextForValue(null));
            ids.add(0, getDisplayTextForValue(null));
        }

        initialize((String[])ids.toArray(new String[ids.size()]), (String[])names.toArray(new String[names.size()]));
    }
}
