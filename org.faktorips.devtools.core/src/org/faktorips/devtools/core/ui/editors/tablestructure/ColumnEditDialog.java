package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;


/**
 *
 */
public class ColumnEditDialog extends IpsPartEditDialog {
    
    private IColumn column;
    
    // edit fields
    private TextField nameField;
    private TextButtonField datatypeField;

    /**
     * @param parentShell
     * @param title
     */
    public ColumnEditDialog(IColumn column, Shell parentShell) {
        super(column, parentShell, Messages.ColumnEditDialog_title, true);
        this.column = column;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.EditDialog#createWorkArea(org.eclipse.swt.widgets.Composite)
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.ColumnEditDialog_pageTitle);
        page.setControl(createGeneralPage(folder));
        
        createDescriptionTabItem(folder);
        return folder;
    }
    
    private Control createGeneralPage(TabFolder folder) {
        
        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        
        uiToolkit.createFormLabel(workArea, Messages.ColumnEditDialog_labelName);
        Text nameText = uiToolkit.createText(workArea);
        nameText.setFocus();
        
        uiToolkit.createFormLabel(workArea, Messages.ColumnEditDialog_labelDatatype);
        DatatypeRefControl datatypeControl = uiToolkit.createDatatypeRefEdit(column.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(false);
        
        // create fields
        nameField = new TextField(nameText);
        datatypeField = new TextButtonField(datatypeControl);
        
        return c;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#connectToModel()
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(nameField, IAttribute.PROPERTY_NAME);
        uiController.add(datatypeField, IAttribute.PROPERTY_DATATYPE);
    }
    
}
