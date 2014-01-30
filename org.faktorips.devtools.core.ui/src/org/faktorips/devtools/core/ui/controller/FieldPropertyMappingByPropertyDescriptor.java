/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.ui.controller.fields.AbstractEnumDatatypeBasedField;
import org.faktorips.devtools.core.ui.controller.fields.RadioButtonGroupField;
import org.faktorips.devtools.core.ui.controller.fields.StringValueComboField;

public class FieldPropertyMappingByPropertyDescriptor<T> implements FieldPropertyMapping<T> {

    protected EditField<T> field;
    protected Object object;
    protected PropertyDescriptor property;

    public FieldPropertyMappingByPropertyDescriptor(EditField<T> edit, Object object, PropertyDescriptor property) {
        this.field = edit;
        this.object = object;
        this.property = property;
    }

    @Override
    public EditField<T> getField() {
        return field;
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public String getPropertyName() {
        return property.getName();
    }

    @Override
    public void setPropertyValue() {
        if (field.getControl().isDisposed()) {
            return;
        }
        if (!field.isTextContentParsable()) {
            return;
        }
        if (ObjectUtils.equals(getPropertyValue(), field.getValue())) {
            return; // value hasn't changed
        }
        try {
            Method setter = property.getWriteMethod();
            if (setter == null) {
                throw new RuntimeException(
                        "Error setting property value " + property.getName() + ": Found no setter method"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            setter.invoke(object, new Object[] { field.getValue() });
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error setting property value " + property.getName() + ": Illegal Access", e); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error setting property value " + property.getName() + ": Illegal Argument", e); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    "Error setting property value " + property.getName() + ": Setter throws an exception", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public Object getPropertyValue() {
        try {
            Method getter = property.getReadMethod();
            return getter.invoke(object, new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error getting property value " + property.getName(), e); //$NON-NLS-1$
        }
    }

    @Override
    public void setControlValue() {
        try {
            if (field.getControl().isDisposed()) {
                return;
            }
            @SuppressWarnings("unchecked")
            // the property is get by reflection - cannot cast safely
            T propertyValue = (T)getPropertyValue();

            // if we have a field which maintans a list - update it.
            if (field instanceof AbstractEnumDatatypeBasedField) {
                ((AbstractEnumDatatypeBasedField)field).reInit();
            }

            if (field.isTextContentParsable() && ObjectUtils.equals(propertyValue, field.getValue())) {
                if (field instanceof StringValueComboField) {
                    /*
                     * special case: if the field is a combo field the getValue method returns null
                     * if there is no selection and if the null value is selected, therefore we must
                     * check here if the getValue is a valid selection or nothing is selected. If
                     * there is no valid selection set the new value (e.g. the null value)
                     */
                    if (((StringValueComboField)field).getCombo().getSelectionIndex() != -1) {
                        // the selection in the combo is valid and equal to the property value,
                        // don't set the new value
                        return;
                    }
                } else if (field instanceof RadioButtonGroupField) {
                    /*
                     * Unfortunately, the same special case applies to radio button groups as well.
                     */
                    if (((RadioButtonGroupField<?>)field).getRadioButtonGroup().getSelectedButton() != null) {
                        return;
                    }
                } else {
                    return;
                }
            }
            field.setValue(propertyValue, false);

        } catch (Exception e) {
            throw new RuntimeException("Error setting value in control for property " + property.getName(), e); //$NON-NLS-1$
        }
    }

    @Override
    public String toString() {
        return object.getClass().getName() + '.' + property.getName() + '-' + field;
    }
}
