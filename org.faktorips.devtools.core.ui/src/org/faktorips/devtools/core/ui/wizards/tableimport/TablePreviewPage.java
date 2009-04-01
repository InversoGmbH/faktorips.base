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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.CompositeFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Wizard Page to show a preview of the table contents to be imported.
 * 
 * @author Roman Grutza
 */
public class TablePreviewPage extends WizardPage implements ValueChangeListener {

    // max number of rows to consider for preview
    public static final int MAX_NUMBER_PREVIEW_ROWS = 8;

    // a leading and trailing column with no data, prevents artifacts when resizing the dialog
    private final static int TABLE_PADDING_LEFT_RIGHT = 20;

    // TODO: cleanup, not needed
    // the resource that was selected in the workbench or null if none.
    private IResource selectedResource;

    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;

    // display preview for this filename using the table format and structure
    private String filename;
    private ITableFormat tableFormat;
    private ITableStructure tableStructure;

    private UIToolkit toolkit = new UIToolkit(null);
    private TableColumn[] columns;

    // page control as defined by the wizard page class
    private Composite pageControl;
    private Composite dynamicPropertiesAnchorComposite;
    private Composite dynamicPropertiesComposite;
    private Group previewGroup;
    private Table previewTable;

    // creates configuration controls specific to a table format
    private CompositeFactory configCompositeFactory;

    
    /**
     * Initializes the preview dialog using the given selection.
     * <p>
     * Note that in order to correctly display a preview one must also set a filename, a table
     * format and a table structure.
     * 
     * @param selection A selection
     * @throws JavaModelException if the resource corresponding to the selection does not exist or
     *             an Exception happens during the access of the resource
     * @see #setFilename(String)
     * @see #setTableFormat(ITableFormat)
     * @see #setTableStructure(ITableStructure)
     */
    public TablePreviewPage(IStructuredSelection selection) throws JavaModelException {
        super("Table preview");

        // TODO: cleanup, not needed
        if (selection.getFirstElement() instanceof IResource) {
            selectedResource = (IResource)selection.getFirstElement();
        } else if (selection.getFirstElement() instanceof IJavaElement) {
            selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();
        } else if (selection.getFirstElement() instanceof IIpsElement) {
            selectedResource = ((IIpsElement)selection.getFirstElement()).getEnclosingResource();
        } else {
            selectedResource = null;
        }
        setPageComplete(false);
    }

    /**
     * Set the filename to display the preview for. Aside from setting the filename one must also
     * set the corresponding table format/structure.
     * 
     * @see #setTableFormat(ITableFormat)
     * @see #setTableStructure(ITableStructure)
     */
    public void setFilename(String filename) {
        this.filename = filename;
        refreshPage();
    }

    /**
     * Set the table format corresponding with the filename for which to display the preview.
     * 
     * @see #setFilename(String)
     * @see #setTableStructure(ITableStructure)
     */
    public void setTableFormat(ITableFormat tableFormat) {
        this.tableFormat = tableFormat;
        resetDynamicPropertiesControl(dynamicPropertiesAnchorComposite, toolkit, tableFormat);
        refreshPage();
    }

    /**
     * Set the table structure to use when showing the preview.
     * 
     * @see #setFilename(String)
     * @see #setTableFormat(ITableFormat)
     */
    public void setTableStructure(ITableStructure tableStructure) {
        this.tableStructure = tableStructure;
        refreshPage();
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        validateInput = false;
        setTitle("Table preview");

        pageControl = toolkit.createGridComposite(parent, 1, false, false);

        // create anchor for properties composite, filled later when TableFormat is known
        dynamicPropertiesAnchorComposite = toolkit.createComposite(pageControl);

        previewGroup = toolkit.createGroup(pageControl, "Preview of data being imported");
        createTablePreviewControls(previewGroup, toolkit);

        setControl(pageControl);
        validateInput = true;
    }

    private void createTablePreviewControls(Composite parent, UIToolkit toolkit) {
        if (previewTable != null && (!previewTable.isDisposed())) {
            Listener[] resizeListeners = previewTable.getListeners(SWT.Resize);
            for (Listener listener : resizeListeners) {
                previewTable.removeListener(SWT.Resize, listener);
            }
            previewTable.dispose();
        }

        // TODO:
        // if preview is not available this label should be shown
//        Label previewNotAvailableLabel = toolkit.createLabel(parent, "A preview is not available.");
//        previewNotAvailableLabel.getLayoutData();
        
        previewTable = toolkit.createTable(parent, SWT.BORDER | SWT.NO_SCROLL);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        previewTable.setLayoutData(data);
        previewTable.setHeaderVisible(true);
        previewTable.setLinesVisible(true);
        previewTable.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                refreshColumnWidths();
            }
        });
    }

    private void resetDynamicPropertiesControl(Composite parent, UIToolkit toolkit, ITableFormat tableFormat) {
        if (!IpsUIPlugin.getDefault().hasTableFormatCustomProperties(tableFormat)) {
            return;
        }

        if (dynamicPropertiesComposite != null && (!dynamicPropertiesComposite.isDisposed())) {
            dynamicPropertiesComposite.dispose();
        }

        dynamicPropertiesComposite = toolkit.createGroup(pageControl, "Configuration");
        Object layoutData = dynamicPropertiesComposite.getLayoutData();
        if (layoutData instanceof GridData) {
            ((GridData)layoutData).grabExcessVerticalSpace = false;
        }

        configCompositeFactory = getCompositeFactory();
        if (configCompositeFactory != null) {
            configCompositeFactory.createPropertyComposite(dynamicPropertiesComposite, toolkit);
            configCompositeFactory.addValueChangedListener(this);
        }

        // make sure the configuration composite is displayed above the preview
        dynamicPropertiesComposite.moveAbove(null);
    }

    private CompositeFactory getCompositeFactory() {
        if (configCompositeFactory == null) {
            configCompositeFactory = IpsUIPlugin.getDefault()
                .getTableFormatPropertiesControlFactory(tableFormat);
        }
        return configCompositeFactory;
    }

    private void fillPreviewTableContents() {
        if (this.filename == null || this.tableFormat == null) {
            return;
        }

        createTablePreviewControls(previewGroup, toolkit);
        List preview = tableFormat.getImportTablePreview(tableStructure, new Path(filename), MAX_NUMBER_PREVIEW_ROWS);

        int numberOfColumns = preview.isEmpty() ? 0 : ((String[])preview.get(0)).length;

        // take empty leading and trailing column into account
        columns = new TableColumn[numberOfColumns + 2];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = new TableColumn(previewTable, SWT.LEFT, i);
        }

        for (int i = 1; i < columns.length - 1; i++) {
            columns[i].setText(tableStructure.getColumn(i - 1).getName());
        }

        for (Iterator iterator = preview.iterator(); iterator.hasNext();) {
            String[] row = (String[])iterator.next();
            TableItem item = new TableItem(previewTable, SWT.LEAD);
            for (int col = 1; col < previewTable.getColumnCount() - 1; col++) {
                item.setText(col, row[col - 1]);
            }
        }

        previewTable.getParent().layout(true);
    }

    /**
     * {@inheritDoc}
     */
    public void valueChanged(FieldValueChangedEvent e) {
        fillPreviewTableContents();

        if (validateInput) { // don't validate during control creating!
            validatePage();
        }
        updatePageComplete();
    }

    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        setPageComplete(true);
    }

    private void refreshPage() {
        refreshColumnWidths();
    }

    private void refreshColumnWidths() {
        if (previewTable.getColumnCount() > 2) {

            int tableWidth = previewTable.getSize().x;

            // columns equal in size
            int newColumnWidth = (tableWidth - 2 * TABLE_PADDING_LEFT_RIGHT) / (previewTable.getColumnCount() - 2);

            columns[0].setWidth(TABLE_PADDING_LEFT_RIGHT);
            for (int i = 1; i < previewTable.getColumnCount() - 2; i++) {
                columns[i].setWidth(newColumnWidth);
            }

            // the last column containing data will grab the whole remaining space
            columns[previewTable.getColumnCount() - 2]
                    .setWidth((previewTable.getSize().x - 2 * TABLE_PADDING_LEFT_RIGHT)
                            - (previewTable.getColumnCount() - 3) * newColumnWidth);
            columns[previewTable.getColumnCount() - 1].setWidth(TABLE_PADDING_LEFT_RIGHT);
        }
        pageControl.layout(true);
    }

    protected void validatePage() {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        if (! tableFormat.isValidImportSource(filename)) {
            setMessage("The file to be imported seems not to be valid.", IMessageProvider.WARNING);
        }
        setErrorMessage(null);

        if (configCompositeFactory != null) {
            MessageList ml = configCompositeFactory.validate();
            if (ml.containsErrorMsg()) {
                setErrorMessage(ml.getFirstMessage(Message.ERROR).getText());
            }
        }

        updatePageComplete();
    }
}
