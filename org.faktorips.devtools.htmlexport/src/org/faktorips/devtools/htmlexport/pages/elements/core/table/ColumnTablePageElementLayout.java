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

package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;

/**
 * The {@link RowTablePageElementLayout} layouts table columns and adds specified {@link Style}s to
 * the {@link TableCellPageElement}s of a column.
 * 
 * @author dicker
 * 
 */
public class ColumnTablePageElementLayout extends DefaultTablePageElementLayout {
    private int[] columns;
    private Style[] styles;

    /**
     * adds the given {@link Style}s to all cells of the given columns
     * 
     */
    public ColumnTablePageElementLayout(int[] columns, Style... styles) {
        this.columns = columns;
        this.styles = styles;
    }

    /**
     * adds the given {@link Style}s to all cells of the given column
     * 
     */
    public ColumnTablePageElementLayout(int column, Style... styles) {
        this(new int[] { column }, styles);
    }

    protected boolean isRelatedColumn(int column) {
        for (int layoutedColumn : columns) {
            if (layoutedColumn == column) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void layoutCell(int row, int column, TableCellPageElement cellPageElement) {
        if (isRelatedColumn(column)) {
            cellPageElement.addStyles(styles);
        }
    }
}
