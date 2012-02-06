/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.CategoryPmo;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Edit dialog to edit the table structure usages.
 */
public class TableStructureUsageEditDialog extends IpsPartEditDialog {

    private TextField nameField;
    private CheckboxField mandatoryTableContentField;
    private ComboViewerField<IProductCmptCategory> categoryField;

    private ITableStructureUsage tableStructureUsage;

    private TableViewer viewer;

    private Button btnAdd;
    private Button btnRemove;
    private Button btnUp;
    private Button btnDown;

    private CategoryPmo categoryPmo;

    public TableStructureUsageEditDialog(ITableStructureUsage tblStructureUsage, Shell parentShell) {
        super(tblStructureUsage, parentShell, Messages.TblsStructureUsageEditDialog_title, true);
        this.tableStructureUsage = tblStructureUsage;
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.TblsStructureUsageEditDialog_propertiesPageTtitle);
        page.setControl(createGeneralPage(folder));

        nameField.getControl().setFocus();
        nameField.addChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged(FieldValueChangedEvent e) {
                if (!getShell().isDisposed()) {
                    getShell().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            if (getShell().isDisposed()) {
                                return;
                            }
                            updateTitleInTitleArea();
                        }
                    });
                }
            }
        });

        return folder;
    }

    private Control createTableStructureComposite(Composite cmp) {
        Composite composite = getToolkit().createGridComposite(cmp, 2, false, true);

        createTable(composite);
        createButtons(composite);

        updateButtonsEnabledState();

        return composite;
    }

    private void createTable(Composite composite) {
        Table table = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.TblsStructureUsageEditDialog_tableStructure);

        // Create the viewer and connect it to the view
        viewer = new TableViewer(table);
        viewer.setContentProvider(new TableStructureContentProvider());
        viewer.setLabelProvider(new DefaultLabelProvider());
        viewer.setInput(tableStructureUsage);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateButtonsEnabledState();
            }
        });
        repackTable(viewer);
    }

    /**
     * Repacks the columns in the test attribute table
     */
    private void repackTable(TableViewer tableViewer) {
        for (int i = 0, n = tableViewer.getTable().getColumnCount(); i < n; i++) {
            tableViewer.getTable().getColumn(i).pack();
        }
    }

    private void createButtons(Composite composite) {
        Composite buttonComposite = getToolkit().createGridComposite(composite, 1, true, false);
        buttonComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        btnAdd = getToolkit().createButton(buttonComposite, Messages.TblsStructureUsageEditDialog_addButton);
        btnRemove = getToolkit().createButton(buttonComposite, Messages.TblsStructureUsageEditDialog_removeButton);
        btnUp = getToolkit().createButton(buttonComposite, Messages.TblsStructureUsageEditDialog_upButton);
        btnDown = getToolkit().createButton(buttonComposite, Messages.TblsStructureUsageEditDialog_downButton);

        btnAdd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnRemove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnUp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        btnAdd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    addClicked();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
        });
        btnRemove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    removedClicked();
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
        });
        btnUp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    moveClicked(true);
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
        });
        btnDown.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    moveClicked(false);
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
        });
    }

    private void addClicked() {
        ITableStructure tableStructure;
        try {
            tableStructure = selectTableStructureByDialog();
            if (tableStructure != null) {
                tableStructureUsage.addTableStructure(tableStructure.getQualifiedName());
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
        if (selection != null && selection instanceof IStructuredSelection) {
            IStructuredSelection structSelection = (IStructuredSelection)selection;
            for (Iterator<?> iter = structSelection.iterator(); iter.hasNext();) {
                String element = (String)iter.next();
                tableStructureUsage.removeTableStructure(element);
            }
            refresh();
            updateButtonsEnabledState();
        }
    }

    private void moveClicked(boolean up) {
        int newSelection[] = tableStructureUsage.moveTableStructure(viewer.getTable().getSelectionIndices(), up);
        refresh();
        viewer.getTable().setSelection(newSelection);
        viewer.getTable().setFocus();
        updateButtonsEnabledState();
    }

    private void selectInTable(String qualifiedName) {
        viewer.setSelection(new StructuredSelection(new String[] { qualifiedName }));
    }

    private void updateButtonsEnabledState() {
        if (!isDataChangeable()) {
            getToolkit().setDataChangeable(btnAdd, false);
            getToolkit().setDataChangeable(btnRemove, false);
            getToolkit().setDataChangeable(btnUp, false);
            getToolkit().setDataChangeable(btnDown, false);
            return;
        }
        boolean enabled = false;
        btnAdd.setEnabled(true);
        ISelection selection = viewer.getSelection();
        if (selection != null && selection instanceof IStructuredSelection && !selection.isEmpty()) {
            enabled = true;
        }
        btnRemove.setEnabled(enabled);
        btnUp.setEnabled(enabled);
        btnDown.setEnabled(enabled);
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = getToolkit().createLabelEditColumnComposite(c);

        getToolkit().createFormLabel(workArea, Messages.TblsStructureUsageEditDialog_rolenameLabel);
        Text nameText = getToolkit().createText(workArea);

        getToolkit().createFormLabel(workArea, Messages.TblsStructureUsageEditDialog_contentRequiredLabel);
        Checkbox mandatoryTableContent = getToolkit().createCheckbox(workArea);

        nameField = new TextField(nameText);
        mandatoryTableContentField = new CheckboxField(mandatoryTableContent);

        getToolkit().createFormLabel(workArea, Messages.TableStructureUsageEditDialog_categoryLabel);
        createCategoryCombo(workArea);

        getToolkit().createVerticalSpacer(c, 10);

        Group grp = getToolkit()
                .createGridGroup(c, Messages.TblsStructureUsageEditDialog_tableStructuresGroup, 1, true);
        createTableStructureComposite(grp);

        return c;
    }

    private void createCategoryCombo(Composite workArea) {
        Combo categoryCombo = getToolkit().createCombo(workArea);
        categoryField = new ComboViewerField<IProductCmptCategory>(categoryCombo, IProductCmptCategory.class);

        categoryPmo = new CategoryPmo(tableStructureUsage);
        categoryField.setInput(categoryPmo.getCategories());
        categoryField.setAllowEmptySelection(true);
        categoryField.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                IProductCmptCategory category = (IProductCmptCategory)element;
                return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(category);
            }
        });

    }

    @Override
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(nameField, ITableStructureUsage.PROPERTY_ROLENAME);
        uiController.add(mandatoryTableContentField, ITableStructureUsage.PROPERTY_MANDATORY_TABLE_CONTENT);
        uiController.add(categoryField, categoryPmo, IProductCmptProperty.PROPERTY_CATEGORY);
    }

    /**
     * Displays a dialog to select a table structure. Returns the selected table structure or
     * <code>null</code> if the user select nothing.
     */
    private ITableStructure selectTableStructureByDialog() throws CoreException {
        ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(), new DefaultLabelProvider());
        selectDialog.setTitle(Messages.TblsStructureUsageEditDialog_selectStructurDialogTitle);
        selectDialog.setMessage(Messages.TblsStructureUsageEditDialog_selectStructurDialogMessage);

        IIpsSrcFile[] tableStructures = tableStructureUsage.getIpsProject().findIpsSrcFiles(
                IpsObjectType.TABLE_STRUCTURE);
        selectDialog.setElements(tableStructures);
        if (selectDialog.open() == Window.OK) {
            if (selectDialog.getResult().length > 0) {
                return (ITableStructure)((IIpsSrcFile)selectDialog.getResult()[0]).getIpsObject();
            }
        }
        return null;
    }

    private void refresh() {
        viewer.refresh();
        repackTable(viewer);
        updateButtonsEnabledState();
        updateTitleInTitleArea();
    }

    @Override
    protected void updateTitleInTitleArea() {
        try {
            MessageList ml = tableStructureUsage.validate(tableStructureUsage.getIpsProject());
            if (ml.getFirstMessage(Message.ERROR) != null) {
                setMessage(ml.getFirstMessage(Message.ERROR).getText(), IMessageProvider.ERROR);
            } else {
                setMessage(null);
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Simple array content provider to provide all table structure references inside a table
     * structure usage object as String array
     */
    private static class TableStructureContentProvider extends ArrayContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            ArgumentCheck.isInstanceOf(inputElement, ITableStructureUsage.class);
            return ((ITableStructureUsage)inputElement).getTableStructures();
        }
    }

}