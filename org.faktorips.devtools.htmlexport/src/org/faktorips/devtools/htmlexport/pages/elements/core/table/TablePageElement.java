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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;

/**
 * A {@link IPageElement} representing a table
 * 
 * @author dicker
 * 
 */
public class TablePageElement extends AbstractCompositePageElement {

    /**
     * a {@link Set} of {@link ITablePageElementLayout}s
     */
    private Set<ITablePageElementLayout> tableLayouts = new HashSet<ITablePageElementLayout>();

    @Override
    public void acceptLayouter(ILayouter layoutVisitor) {
        layoutVisitor.layoutTablePageElement(this);
    }

    /**
     * adds a {@link TableRowPageElement} to the table
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement#addSubElement(org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement)
     * @throws ClassCastException if given {@link IPageElement} is not a {@link TableRowPageElement}
     */
    @Override
    protected void addSubElement(IPageElement pageElement) {
        TableRowPageElement rowPageElement = (TableRowPageElement)pageElement;

        super.addSubElement(rowPageElement);

        rowPageElement.setParentTablePageElement(this);
    }

    /**
     * creates an empty {@link TablePageElement}
     */
    public TablePageElement() {
        this(true);
    }

    /**
     * creates an empty {@link TablePageElement}
     * 
     * @param border shows the border of the table if is set to true
     */
    public TablePageElement(boolean border) {
        super();
        setBorder(border);
    }

    @Override
    public void build() {
        // could be overridden
    }

    /**
     * @return true, if the table has a border
     */
    public boolean hasBorder() {
        return styles.contains(Style.BORDER);
    }

    /**
     * sets, whether the table has a border or not
     * 
     */
    public void setBorder(boolean border) {
        if (border) {
            styles.add(Style.BORDER);
            return;
        }
        styles.remove(Style.BORDER);
    }

    @Override
    public void visitSubElements(ILayouter layouter) {
        List<IPageElement> subElements = getSubElements();
        for (int i = 0; i < subElements.size(); i++) {
            TableRowPageElement rowPageElement = (TableRowPageElement)subElements.get(i);
            layoutTableRow(i, rowPageElement);
            rowPageElement.build();
            rowPageElement.acceptLayouter(layouter);
        }
    }

    /**
     * layouts the given {@link TableRowPageElement} using all added {@link ITablePageElementLayout}s
     * 
     */
    protected void layoutTableRow(int i, TableRowPageElement rowPageElement) {
        for (ITablePageElementLayout tableLayout : tableLayouts) {
            tableLayout.layoutRow(i, rowPageElement);
        }
    }

    /**
     * @return the {@link ITablePageElementLayout}s
     */
    public Set<ITablePageElementLayout> getLayouts() {
        return tableLayouts;
    }

    /**
     * adds {@link ITablePageElementLayout}s
     * 
     */
    public void addLayouts(ITablePageElementLayout... layouts) {
        tableLayouts.addAll(Arrays.asList(layouts));
    }

    /**
     * remove {@link ITablePageElementLayout}s
     * 
     */
    public void removeLayouts(ITablePageElementLayout... layouts) {
        tableLayouts.removeAll(Arrays.asList(layouts));
    }
}
