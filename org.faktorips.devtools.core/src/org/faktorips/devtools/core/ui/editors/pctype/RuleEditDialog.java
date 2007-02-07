/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public class RuleEditDialog extends IpsPartEditDialog {
    
    private IValidationRule rule;
    
    // edit fields
    private TextField nameField;
    private TextField msgCodeField;
    private EnumValueField msgSeverityField;
    private TextField msgTextField;
    private CheckboxField appliedToAllField;
    private RuleFunctionsControl rfControl;
    private CheckboxField specifiedInSrcField;
    private Text msgText;
    private Label charCount;

    /**
     * @param parentShell
     * @param title
     */
    public RuleEditDialog(IValidationRule rule, Shell parentShell) {
        super(rule, parentShell, Messages.RuleEditDialog_title, true);
        this.rule = rule;
    }

    /** 
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        
        TabFolder folder = (TabFolder)parent;
        
        TabItem msgPage = new TabItem(folder, SWT.NONE);
        msgPage.setText(Messages.RuleEditDialog_messageTitle);
        msgPage.setControl(createMessagePage(folder));
        
        TabItem functionsPage = new TabItem(folder, SWT.NONE);
        functionsPage.setText(Messages.RuleEditDialog_functionTitle);
        functionsPage.setControl(createFunctionsPage(folder));
        
        TabItem attributesPage = new TabItem(folder, SWT.NONE);
        attributesPage.setText(Messages.RuleEditDialog_attrTitle);
        attributesPage.setControl(createAttributesPage(folder));
        
        createDescriptionTabItem(folder);
        //the update cycle for changes to model objects is extended so that the gui will be updated due to
        //model changes. The update cycle gui -> model -> gui is currently not implemented in a super class
        //but should be considered in the future.
        //It is necessary here because changes made to the model within the RuleFunctionsControl need to be 
        //communicated to the gui so that other controls can adjust there current state. 
        final ContentsChangeListenerForWidget listener = new ContentsChangeListenerForWidget (){

            public void contentsChangedAndWidgetIsNotDisposed(ContentChangeEvent event) {
                if(!event.getIpsSrcFile().exists()){
                    return;
                }
                if(event.getIpsSrcFile().equals(rule.getIpsObject().getIpsSrcFile())){
                    RuleEditDialog.this.uiController.updateUI();
                }
            }
        };
        listener.setWidget(parent);
        rule.getIpsModel().addChangeListener(listener);
        return folder;
    }
    
    private Control createMessagePage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder,1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;

        Composite nameComposite = uiToolkit.createLabelEditColumnComposite(workArea);
        uiToolkit.createFormLabel(nameComposite, Messages.RuleEditDialog_labelName);
        Text nameText = uiToolkit.createText(nameComposite);
        nameText.setFocus();

        // message group
        Group msgGroup = uiToolkit.createGroup(workArea, Messages.RuleEditDialog_messageGroupTitle);
        Composite msgComposite = uiToolkit.createLabelEditColumnComposite(msgGroup);
        msgComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        uiToolkit.createFormLabel(msgComposite, Messages.RuleEditDialog_labelCode);
        Text codeText = uiToolkit.createText(msgComposite);
        uiToolkit.createFormLabel(msgComposite, Messages.RuleEditDialog_labelSeverity);
        Combo severityCombo = uiToolkit.createCombo(msgComposite, MessageSeverity.getEnumType());
        Label label = uiToolkit.createFormLabel(msgComposite, Messages.RuleEditDialog_labelText);
        label.getParent().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
        msgText = uiToolkit.createMultilineText(msgComposite);
        msgText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateCharCount();
			}
		});
        uiToolkit.createVerticalSpacer(msgComposite, 1);
        charCount = uiToolkit.createFormLabel(msgComposite, ""); //$NON-NLS-1$
        charCount.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, true));
        updateCharCount();
        
        // create fields
        nameField = new TextField(nameText);
        msgCodeField = new TextField(codeText);
        msgTextField = new TextField(msgText);
        msgSeverityField = new EnumValueField(severityCombo, MessageSeverity.getEnumType());;
        
        
        return workArea;
    }
    
    private void updateCharCount() {
    	String msg = NLS.bind(Messages.RuleEditDialog_contains, new Integer(msgText.getText().length()));
    	charCount.setText(msg);
    	charCount.getParent().layout();
    }
    
    private Control createFunctionsPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder,1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;
        Checkbox appliedToAllCheckbox = uiToolkit.createCheckbox(workArea, Messages.RuleEditDialog_labelApplyInAllBusinessFunctions);
        rfControl = new RuleFunctionsControl((IValidationRule)super.getIpsPart(), workArea);
        appliedToAllField = new CheckboxField(appliedToAllCheckbox);
        
        return workArea;
    }
    
    private Control createAttributesPage(TabFolder folder) {
        Composite workArea = createTabItemComposite(folder,1, false);
        ((GridLayout)workArea.getLayout()).verticalSpacing = 20;
        Checkbox specifiedInSrc = uiToolkit.createCheckbox(workArea, Messages.RuleEditDialog_labelSpecifiedInSrc);
        specifiedInSrcField = new CheckboxField(specifiedInSrc);
        
        new ValidatedAttributesControl((IValidationRule)super.getIpsPart(), workArea);
        return workArea;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#createUIController(org.faktorips.devtools.core.model.IIpsObjectPart)
     */
    protected IpsPartUIController createUIController(IIpsObjectPart part) {
        return new UIController(part);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#connectToModel()
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(nameField, rule, IValidationRule.PROPERTY_NAME);
        uiController.add(msgCodeField, rule, IValidationRule.PROPERTY_MESSAGE_CODE);
        uiController.add(msgSeverityField, rule, IValidationRule.PROPERTY_MESSAGE_SEVERITY);
        uiController.add(msgTextField, rule, IValidationRule.PROPERTY_MESSAGE_TEXT);
        uiController.add(appliedToAllField, rule, IValidationRule.PROPERTY_APPLIED_FOR_ALL_BUSINESS_FUNCTIONS);
        uiController.add(specifiedInSrcField, rule, IValidationRule.PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC);
    }
    
	protected Point getInitialSize() {
	    return new Point(500, 420);
	}
	
    class UIController extends IpsPartUIController {

        public UIController(IIpsObjectPart pdPart) {
            super(pdPart);
        }
        
        protected MessageList validatePartContainerAndUpdateUI() {
            MessageList list = super.validatePartContainerAndUpdateUI();
            rfControl.updateValidationStatus();
            return list;
        }
    }

}
