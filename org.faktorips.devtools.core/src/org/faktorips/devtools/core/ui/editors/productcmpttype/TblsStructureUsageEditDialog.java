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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype2.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Edit dialog to edit the table structure usages.
 */
public class TblsStructureUsageEditDialog extends IpsPartEditDialog{
    
    private TextField nameField;
    private CheckboxField mandatoryTableContentField;
    
    private ITableStructureUsage tblStructureUsage;
    
    private TableViewer viewer;
    
    private Button btnAdd;
    private Button btnRemove;
    private Button btnUp;
    private Button btnDown;
    
    public TblsStructureUsageEditDialog(ITableStructureUsage tblStructureUsage, Shell parentShell) {
        super(tblStructureUsage, parentShell, "Edit Table Structure Usage", true );
        this.tblStructureUsage = tblStructureUsage;
    }   
    
    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText("Properties");
        page.setControl(createGeneralPage(folder));

        createDescriptionTabItem(folder);
        
        nameField.getControl().setFocus();
        nameField.addChangeListener(new ValueChangeListener() {
            public void valueChanged(FieldValueChangedEvent e) {
                if (!getShell().isDisposed()) {
                    getShell().getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            if (getShell().isDisposed())
                                return;
                            updateTitleInTitleArea();
                        }
                    });
                }
            }
        });

        return folder;
    }

    private Control createTableStructureComposite(Composite cmp) {
        Composite composite = uiToolkit.createGridComposite(cmp, 2, false, true);

        createTable(composite);
        createButtons(composite);
        
        updateButtonsEnabledState();
        
        return composite;
    }

    private void createTable(Composite composite) {
        Table table = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        table.setHeaderVisible (true);
        table.setLinesVisible (true);

        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText("Table Structure");
        
        // Create the viewer and connect it to the view
        viewer = new TableViewer(table);
        viewer.setContentProvider (new TableStructureContentProvider());
        viewer.setLabelProvider (new DefaultLabelProvider());
        viewer.setInput(tblStructureUsage);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtonsEnabledState();
            }
        });
        repackTable(viewer);
    }

    /*
     * Repacks the columns in the test attribute table
     */
    private void repackTable(TableViewer tableViewer) {
        for (int i = 0, n = tableViewer.getTable().getColumnCount(); i < n; i++) {
            tableViewer.getTable().getColumn(i).pack();
        }
    }
    
    private void createButtons(Composite composite) {
        Composite buttonComposite = uiToolkit.createGridComposite(composite, 1, true, false);
        buttonComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        btnAdd = uiToolkit.createButton(buttonComposite, "Add");
        btnRemove = uiToolkit.createButton(buttonComposite, "Remove");
        btnUp = uiToolkit.createButton(buttonComposite, "Up");
        btnDown = uiToolkit.createButton(buttonComposite, "Down");

        btnAdd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnRemove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnUp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        btnAdd.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    addClicked();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        btnRemove.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    removedClicked();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        btnUp.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    moveClicked(true);
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        btnDown.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    moveClicked(false);
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });                
    }

    private void addClicked() {
        ITableStructure tableStructure;
        try {
            tableStructure = selectTableStructureByDialog();
            if (tableStructure != null){
                tblStructureUsage.addTableStructure(tableStructure.getQualifiedName());
                refresh();
                selectInTable(tableStructure.getQualifiedName());
                viewer.getTable().setFocus();
                updateButtonsEnabledState();
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    private void removedClicked() {
        ISelection selection = viewer.getSelection();
        if (selection != null && selection instanceof IStructuredSelection){
            IStructuredSelection structSelection = (IStructuredSelection) selection;
            for (Iterator iter = structSelection.iterator(); iter.hasNext();) {
                String element = (String)iter.next();
                tblStructureUsage.removeTableStructure(element);
            }
            refresh();
            updateButtonsEnabledState();
        }
    }
    
    private void moveClicked(boolean up) {
        int newSelection[] = tblStructureUsage.moveTableStructure(viewer.getTable().getSelectionIndices(), up);
        refresh();
        viewer.getTable().setSelection(newSelection);
        viewer.getTable().setFocus();
        updateButtonsEnabledState();
    }
    
    private void selectInTable(String qualifiedName) {
        viewer.setSelection(new StructuredSelection(new String[]{qualifiedName}));
    }

    private void updateButtonsEnabledState() {
        if(!isDataChangeable()){
            uiToolkit.setDataChangeable(btnAdd, false);
            uiToolkit.setDataChangeable(btnRemove, false);
            uiToolkit.setDataChangeable(btnUp, false);
            uiToolkit.setDataChangeable(btnDown, false);
            return;
        }
        boolean enabled = false;
        btnAdd.setEnabled(true);
        ISelection selection = viewer.getSelection();
        if (selection != null && selection instanceof IStructuredSelection && ! selection.isEmpty()){
            enabled = true;
        }
        btnRemove.setEnabled(enabled);
        btnUp.setEnabled(enabled);
        btnDown.setEnabled(enabled);
    }
    
    private Control createGeneralPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        
        uiToolkit.createFormLabel(workArea, "Rolename:");
        Text nameText = uiToolkit.createText(workArea);            
        
        uiToolkit.createFormLabel(workArea, "Table content required:");
        Checkbox mandatoryTableContent = uiToolkit.createCheckbox(workArea);     
        
        nameField = new TextField(nameText);
        mandatoryTableContentField = new CheckboxField(mandatoryTableContent);

        uiToolkit.createVerticalSpacer(c, 10);
        
        Group grp = uiToolkit.createGridGroup(c, "Table Structures:", 1, true);
        createTableStructureComposite(grp);
        
        return c;
    }

    protected void connectToModel() {
        super.connectToModel();
        uiController.add(nameField, ITableStructureUsage.PROPERTY_ROLENAME);
        uiController.add(mandatoryTableContentField, ITableStructureUsage.PROPERTY_MANDATORY_TABLE_CONTENT);
    }
    
    /*
     * Displays a dialog to select a table structure. Returns the selected table structure or
     * <code>null</code> if the user select nothing.
     */
    private ITableStructure selectTableStructureByDialog() throws CoreException {
        ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(),
                new DefaultLabelProvider());
        selectDialog.setTitle("Select Table Structure");
        selectDialog.setMessage("Select a table structure for the usage");

        IIpsObject[] tableStructures = tblStructureUsage.getIpsProject().findIpsObjects(IpsObjectType.TABLE_STRUCTURE);
        selectDialog.setElements(tableStructures);
        if (selectDialog.open() == Window.OK) {
            if (selectDialog.getResult().length > 0) {
                return (ITableStructure)selectDialog.getResult()[0];
            }
        }
        return null;
    }
    
    private void refresh(){
        viewer.refresh();
        repackTable(viewer);
        updateButtonsEnabledState();
        updateTitleInTitleArea();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void updateTitleInTitleArea(){
        try {
            MessageList ml = tblStructureUsage.validate();
            if (ml.getFirstMessage(Message.ERROR) != null){
                setMessage(ml.getFirstMessage(Message.ERROR).getText(), IMessageProvider.ERROR);
            } else {
                setMessage(null);
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    /*
     * Simple array content provider to provide all table stucture references inside a table structure
     * usage object as String array
     */
    private class TableStructureContentProvider extends ArrayContentProvider{
        /**
         * {@inheritDoc}
         */
        public Object[] getElements(Object inputElement) {
            ArgumentCheck.isInstanceOf(inputElement, ITableStructureUsage.class);
            return ((ITableStructureUsage)inputElement).getTableStructures();
        }
    }
    
    
}
