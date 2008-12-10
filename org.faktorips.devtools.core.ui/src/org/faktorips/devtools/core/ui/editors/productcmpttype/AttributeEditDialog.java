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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.controls.TableElementValidator;
import org.faktorips.devtools.core.ui.controls.ValueSetEditControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.util.message.MessageList;

/**
 * Dialog to edit an attribute.
 * 
 * @author Jan Ortmann
 */
public class AttributeEditDialog extends IpsPartEditDialog2 implements ContentsChangeListener {

    /*
     * Folder which contains the pages shown by this dialog. Used to modify which page
     * is shown.
     */
    private TabFolder folder;
    
    private IIpsProject ipsProject;
    private IProductCmptTypeAttribute attribute;

    // placeholder for the default edit field, the edit field for the default value depends on
    // the attributes datatype
    private Composite defaultEditFieldPlaceholder;
    private EditField defaultValueField;
    
    private ValueSetEditControl valueSetEditControl;
    
    private ValueDatatype currentDatatype;
    private ValueSetType currentValueSetType;
    
    private ExtensionPropertyControlFactory extFactory;

    /**
     * @param part
     * @param parentShell
     * @param windowTitle
     */
    public AttributeEditDialog(IProductCmptTypeAttribute a, Shell parentShell) {
        super(a, parentShell, Messages.AttributeEditDialog_title, true);
        this.attribute = a;
        this.ipsProject = attribute.getIpsProject();
        try {
            currentDatatype = a.findDatatype(ipsProject);
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
        }
        currentValueSetType = a.getValueSet().getValueSetType();
        extFactory = new ExtensionPropertyControlFactory(attribute.getClass());
    }

    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        folder = (TabFolder)parent;
        
        TabItem generalItem = new TabItem(folder, SWT.NONE);
        generalItem.setText(Messages.AttributeEditDialog_general);
        generalItem.setControl(createGeneralPage(folder));

        createDescriptionTabItem(folder);
        
        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {

        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        extFactory.createControls(workArea, uiToolkit, attribute, IExtensionPropertyDefinition.POSITION_TOP);

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_nameLabel);
        Text nameText = uiToolkit.createText(workArea);
        nameText.setFocus();
        bindingContext.bindContent(nameText, attribute, IProductCmptTypeAttribute.PROPERTY_NAME);
        
        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_datatypeLabel);
        DatatypeRefControl datatypeControl = uiToolkit.createDatatypeRefEdit(attribute.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);
        bindingContext.bindContent(datatypeControl, attribute, IProductCmptTypeAttribute.PROPERTY_DATATYPE);

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_modifierLabel);
        Combo modifierCombo = uiToolkit.createCombo(workArea, Modifier.getEnumType());
        bindingContext.bindContent(modifierCombo, attribute, IProductCmptTypeAttribute.PROPERTY_MODIFIER, Modifier.getEnumType());
        
        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_defaultvalueLabel);
        defaultEditFieldPlaceholder = uiToolkit.createComposite(workArea);
        defaultEditFieldPlaceholder.setLayout(uiToolkit.createNoMarginGridLayout(1, true));
        defaultEditFieldPlaceholder.setLayoutData(new GridData(GridData.FILL_BOTH));
        createDefaultValueEditField();
        
        uiToolkit.createVerticalSpacer(c, 4);
        uiToolkit.createHorizonzalLine(c);
        uiToolkit.createVerticalSpacer(c, 4);
        
        IpsObjectUIController uiController = new IpsObjectUIController(attribute);
        Composite temp = uiToolkit.createGridComposite(c, 1, true, false);
        uiToolkit.createLabel(temp, Messages.AttributeEditDialog_valueSetSection);
        uiToolkit.createVerticalSpacer(temp, 8);
        valueSetEditControl = new ValueSetEditControl(temp, uiToolkit,  uiController, attribute, new Validator());
        updateValueSetTypes();
        
        Object layoutData = valueSetEditControl.getLayoutData();
        if (layoutData instanceof GridData){
            // set the minimum height to show at least the maximum size of the selected ValueSetEditControl
            GridData gd = (GridData)layoutData;
            gd.heightHint = 260;
        }
        
        extFactory.createControls(workArea, uiToolkit, attribute, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(bindingContext);
        
        return c;
        
    }
    
    private void createDefaultValueEditField() {
        ValueDatatypeControlFactory datatypeCtrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(currentDatatype);
        defaultValueField = datatypeCtrlFactory.createEditField(uiToolkit, defaultEditFieldPlaceholder, currentDatatype, null);
        defaultValueField.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        defaultEditFieldPlaceholder.layout();
        defaultEditFieldPlaceholder.getParent().getParent().layout();
        bindingContext.bindContent(defaultValueField, attribute, IProductCmptTypeAttribute.PROPERTY_DEFAULT_VALUE);
    }

    public void contentsChanged(ContentChangeEvent event) {
        super.contentsChanged(event);
        ValueDatatype newDatatype = null;
        try {
            newDatatype  = attribute.findDatatype(ipsProject);
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
        }
        boolean enabled = newDatatype != null;
        defaultValueField.getControl().setEnabled(enabled);
        valueSetEditControl.setDataChangeable(enabled);
        if (newDatatype==null || newDatatype.equals(currentDatatype)) {
            return;
        }
        currentDatatype = newDatatype;
        if (defaultValueField!=null) {
            bindingContext.removeBindings(defaultValueField.getControl());
            defaultValueField.getControl().dispose();
        }
        createDefaultValueEditField();
        updateValueSetTypes();
    }
    
    private void updateValueSetTypes() {
        currentValueSetType = valueSetEditControl.getValueSetType();
        ValueSetType[] types;
        try {
            types = ipsProject.getValueSetTypes(currentDatatype);
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
            types = new ValueSetType[]{ValueSetType.ALL_VALUES};
        }
        valueSetEditControl.setTypes(types, currentDatatype);
        if (currentValueSetType != null){
            // if the previous selction was a valid selection use this one as new selection in drop down,
            // otherwise the default (first one) is selected
            valueSetEditControl.selectValueSetType(currentValueSetType);
        }
    }

    private class Validator implements TableElementValidator {

        public MessageList validate(String element) {
            MessageList list;
            try {
                list = attribute.validate(attribute.getIpsProject());
                // update the ui to set the current messages for all registered controls
                bindingContext.updateUI();
                return list.getMessagesFor(element);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return new MessageList();
            }
        }
    }
    
}
