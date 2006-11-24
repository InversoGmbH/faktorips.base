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

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ValueSet;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumDatatypeField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.TableCellEditor;

/**
 * A control factory for the datytpes enumeration.
 * 
 * @author Joerg Ortmann
 */
public class EnumDatatypeControlFactory extends ValueDatatypeControlFactory {

	public EnumDatatypeControlFactory() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFactoryFor(ValueDatatype datatype) {
		return datatype instanceof EnumDatatype;
	}

	/**
	 * {@inheritDoc}
	 */
	public EditField createEditField(UIToolkit toolkit, Composite parent,
			ValueDatatype datatype, IValueSet valueSet) {

		Combo combo = toolkit.createCombo(parent);
		if (valueSet instanceof IEnumValueSet) {
			return new EnumValueSetField(combo, (IEnumValueSet)valueSet, datatype);
		} else {
			return new EnumDatatypeField(combo, (EnumDatatype)datatype);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Control createControl(UIToolkit toolkit, Composite parent,
			ValueDatatype datatype, IValueSet valueSet) {

		return createEditField(toolkit, parent, datatype, valueSet).getControl();
	}

    /**
     * Creates a <code>ComboCellEditor</code> for the given valueset and Datatype. The created
     * CellEditor contains a <code>Combo</code> control that is filled with the corresponding
     * values from the given <code>ValueSet</code>. If the given valueset is either not an
     * <code>EnumValueSet</code> or <code>null</code> a <code>ComboCellEditor</code> is
     * created with a <code>Combo</code> control for the given <code>DataType</code>. In this case
     * the Combo contains the value IDs (not the names) of the given <code>EnumDatatype</code> 
     * {@inheritDoc}
     */
    public TableCellEditor createCellEditor(UIToolkit toolkit, ValueDatatype dataType, ValueSet valueSet, TableViewer tableViewer, int columnIndex) {
        Combo comboControl;
        if (valueSet instanceof IEnumValueSet) {
            comboControl= toolkit.createCombo(tableViewer.getTable(), (IEnumValueSet)valueSet, (EnumDatatype)dataType);
        }else{
            comboControl= toolkit.createIDCombo(tableViewer.getTable(), (EnumDatatype)dataType);
        }
        return new ComboCellEditor(tableViewer, columnIndex, comboControl);
    }

}
