/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.table.TextCellEditor;

/**
 * A default factory that creates a combo box for none-abstract enum value sets and a simple text
 * control in all other cases.
 * 
 * @author Joerg Ortmann
 */
public class DefaultControlFactory extends ValueDatatypeControlFactory {

    public DefaultControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return true;
    }

    @Override
    public EditField<String> createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {

        if (datatype != null && valueSet != null && valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()) {
            Combo combo = toolkit.createCombo(parent);
            return new EnumValueSetField(combo, (IEnumValueSet)valueSet, datatype);
        }
        return new TextField(toolkit.createText(parent));
    }

    @Override
    public Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {

        return createEditField(toolkit, parent, datatype, valueSet, ipsProject).getControl();
    }

    /**
     * Creates a <code>TextCellEditor</code> with the given <code>TableViewer</code>, the given
     * columnIndex and a <code>Text</code> control. {@inheritDoc}
     * 
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    @Override
    public IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        return createTableCellEditor(toolkit, dataType, valueSet, tableViewer, columnIndex, ipsProject);
    }

    /**
     * Creates a <code>TextCellEditor</code> with the given <code>TableViewer</code>, the given
     * columnIndex and a <code>Text</code> control. {@inheritDoc}
     */
    @Override
    public IpsCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        Text textControl = toolkit.createText(tableViewer.getTable(), SWT.SINGLE | getDefaultAlignment());
        TextCellEditor cellEditor = new TextCellEditor(textControl);
        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, tableViewer, columnIndex);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);
        return cellEditor;
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.LEFT;
    }

}
