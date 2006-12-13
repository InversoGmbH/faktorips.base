/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.AbstractPrimitiveDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.util.ArgumentCheck;

/**
 * A cell editor wich delegates to different row cell editors. This cell editor could be defined as cell editor in a column,
 * but depending on the value datatype in a row different cell editors will be used to edit the value in a row, e.g. in the first row
 * a drop down and in the second row a text cell editor could be used.<br>
 * Usage: 
 * <ol>
 * <li>Create this cell editor with a corresponding table viewer which uses this cell editor and the column index this cell editor is adapt to.
 * <li>Set the cell editors for all rows.
 * </ol>
 * 
 * @author Joerg Ortmann
 */
public class DelegateCellEditor extends CellEditor {
    // Dummy indicator for the delegate cell editor
    public static final ValueDatatype DELEGATE_VALUE_DATATYPE = new DelegateValueDatatype();
    
    // Dummy value datatype to indicate that the delegate cell editor is used for this datatype
    private static class DelegateValueDatatype extends AbstractPrimitiveDatatype {
        public Object getValue(String value) {
            return null;
        }
        public String getDefaultValue() {
            return null;
        }
        public Datatype getWrapperType() {
            return null;
        }
        public boolean supportsCompare() {
            return false;
        }
        public String getJavaClassName() {
            return null;
        }
        public String getName() {
            return null;
        }
        public String getQualifiedName() {
            return null;
        }
    }
    
    // The table viewer this cell editor is used for
    private TableViewer tableViewer;

    // the column this cell editor is adapted
    private int column;
    
    // The list of cell editors for each row one cell editor
    private List cellEditors;
    
    public DelegateCellEditor(TableViewer tableViewer, int column) {
        super();
        this.tableViewer = tableViewer;
        this.column = column;
    }

    /**
     * Returns the column this cell editor is specified for.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Set the cell editors. Remark: For each row a cell editor must be specified. Otherwise a
     * argument exception will be thrown.
     */
    public void setCellEditors(CellEditor[] cellEditors) {
        ArgumentCheck.isTrue(cellEditors.length == tableViewer.getTable().getItems().length);
        this.cellEditors = Arrays.asList(cellEditors);
    }

    /*
     * Returns the current cell editor. First the current selected row will be determined and then the
     * corresponding cell editor will be returned. If no cell editor is defined for the selected row index
     * a runtime exception will be thrown.
     */
    private TableCellEditor getCurrent(){
        int currentCellEditorRow = tableViewer.getTable().getSelectionIndex();
        if (currentCellEditorRow >= cellEditors.size()){
            throw new RuntimeException("Undefined table cell editor! No table cell editor is specified for the selected row."); //$NON-NLS-1$
        }
        return ((TableCellEditor)cellEditors.get(currentCellEditorRow));
    }
    
    /**
     * {@inheritDoc}
     */
    public Control getControl() {
        return getCurrent().getControl();
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeListener(ICellEditorListener listener) {
        getCurrent().removeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    protected Control createControl(Composite parent) {
        return getCurrent().createControl(parent);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMappedValue() {
        return getCurrent().isMappedValue();
    }

    /**
     * {@inheritDoc}
     */
    protected Object doGetValue() {
        TableCellEditor current = getCurrent();
        if (current instanceof TextCellEditor){
            return ((TextCellEditor) current).doGetValue();
        } else if (current instanceof ComboCellEditor){
            return ((ComboCellEditor) current).doGetValue();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected void doSetFocus() {
        TableCellEditor current = getCurrent();
        if (current instanceof TextCellEditor){
            ((TextCellEditor) current).doSetFocus();
        } else if (current instanceof ComboCellEditor){
            ((ComboCellEditor) current).doSetFocus();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void doSetValue(Object value) {
        TableCellEditor current = getCurrent();
        if (current instanceof TextCellEditor){
            ((TextCellEditor) current).doSetValue(value);
        } else if (current instanceof ComboCellEditor){
            ((ComboCellEditor) current).doSetValue(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deactivate() {
        super.deactivate();
        getControl().setVisible(false);
    }
    
    
}
