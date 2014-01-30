/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.actions.Messages;

/**
 * Action for deleting a single row in a tableviewer.
 * 
 * @author Stefan Widmaier
 */
public class DeleteRowAction extends IpsAction {

    /**
     * The TableViewer this action operates in.
     */
    private TableViewer tableViewer;

    private ContentPage contentPage;

    /**
     * Creates an action that, when run, deletes the (first) selected row in the given
     * <code>TableViewer</code>.
     */
    public DeleteRowAction(TableViewer tableViewer, ContentPage page) {
        super(tableViewer);
        this.tableViewer = tableViewer;
        contentPage = page;
        setControlWithDataChangeableSupport(page);
        setText(Messages.DeleteRowAction_Label);
        setToolTipText(Messages.DeleteRowAction_Tooltip);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("DeleteRow.gif")); //$NON-NLS-1$
    }

    /**
     * Deletes the first selected row in the tableviewer and refreshes thereafter. {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        if (selected instanceof IRow) {
            IRow selRow = ((IRow)selected);
            int rowNumber = selRow.getRowNumber();
            selRow.delete();
            selectPreviousRow(rowNumber);
        }
        tableViewer.refresh(false);
    }

    /**
     * Selects the previous row or if the given row is the first row the new first row.
     */
    private void selectPreviousRow(int rowNumber) {
        int rowIndexToSelect = rowNumber - 1;
        if (rowIndexToSelect < 0) {
            rowIndexToSelect = 0;
        }
        IRow row = contentPage.getRow(rowIndexToSelect);
        if (row != null) {
            tableViewer.setSelection(new StructuredSelection(row));
        }
    }
}
