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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.model.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;


/**
 * A dialog to edit a relation.
 */
public class RelationEditDialog extends IpsPartEditDialog {
    
    public IRelation relation;
    
    // edit fields
    private EnumValueField typeField;
    private CheckboxField abstractContainerField;
    private TextButtonField targetField;
    private TextField targetRoleSingularField;
    private TextField targetRolePluralField;
    private CardinalityField minCardinalityField;
    private CardinalityField maxCardinalityField;
    private CheckboxField productRelevantField;
    private TextField containerRelationField;
    private TextField reverseRelationField;

    private TextField targetRoleSingularProductSideField;
    private TextField targetRolePluralProductSideField;
    private CardinalityField minCardinalityProductSideField;
    private CardinalityField maxCardinalityProductSideField;
    
    private ExtensionPropertyControlFactory extFactory;
    
    /**
     * @param parentShell
     * @param title
     */
    public RelationEditDialog(IRelation relation, Shell parentShell) {
        super(relation, parentShell, Messages.RelationEditDialog_title, true );
        this.relation = relation;
        
        extFactory = new ExtensionPropertyControlFactory(relation.getClass());
    }

    /** 
     * Overridden.
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.RelationEditDialog_propertiesTitle);
        firstPage.setControl(createFirstPage(folder));
        
        TabItem productSidePage = new TabItem(folder, SWT.NONE);
        productSidePage.setText(Messages.RelationEditDialog_productSideTitle);
        productSidePage.setControl(createProductSidePage(folder));
        
        createDescriptionTabItem(folder);
        return folder;
    }
    
    private Control createFirstPage(TabFolder folder) {
        
    	Composite c = createTabItemComposite(folder, 1, false);
    	Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        extFactory.createControls(workArea, uiToolkit, (IpsObjectPartContainer)relation, "top"); //$NON-NLS-1$
                
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelType);
        final Combo typeCombo = uiToolkit.createCombo(workArea, RelationType.getEnumType());
        typeCombo.setFocus();
        typeCombo.addFocusListener(new FocusAdapter() {
            
            public void focusLost(FocusEvent e) {
                int i = typeCombo.getSelectionIndex();
                if (i==-1) {
                    return;
                }
                RelationType type = RelationType.getRelationType(i);
                if (type!=null) {
                	setDefaults(type);
                }
            }
        });
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelReadOnlyContainer);
        Checkbox abstractContainerCheckbox = uiToolkit.createCheckbox(workArea);

        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTarget);
        PcTypeRefControl targetControl = uiToolkit.createPcTypeRefControl(relation.getIpsProject(), workArea);

        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTargetRoleSingular);
        final Text targetRoleSingularText = uiToolkit.createText(workArea);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRoleSingularField.getText())) {
                    String targetName = targetField.getText();
                    int pos = targetName.lastIndexOf('.');
                    if (pos!=-1) {
                        targetName = targetName.substring(pos+1);
                    }
                    targetRoleSingularField.setText(targetName);
                }
            }
        });
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTargetRolePlural);
        Text targetRolePluralText = uiToolkit.createText(workArea);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralField.getText())) {
                    targetRolePluralField.setText(targetRoleSingularText.getText());
                }
            }
        });
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelMinCardinality);
        Text minCardinalityText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelMaxCardinality);
        Text maxCardinalityText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelProdRelevant);
        Checkbox productRelevantCheckbox = uiToolkit.createCheckbox(workArea);
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelReverseRel);
        Text reverseRelationText = uiToolkit.createText(workArea);
        ReverseRelationCompletionProcessor reverseRelationCompletionProcessor = new ReverseRelationCompletionProcessor(relation);
        reverseRelationCompletionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(reverseRelationText, CompletionUtil.createContentAssistant(reverseRelationCompletionProcessor));

        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelContainerRel);
        Text containerRelationText = uiToolkit.createText(workArea);
        ContainerRelationCompletionProcessor completionProcessor = new ContainerRelationCompletionProcessor(relation);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(containerRelationText, CompletionUtil.createContentAssistant(completionProcessor));
        
        // create fields
        typeField = new EnumValueField(typeCombo, RelationType.getEnumType());
        abstractContainerField = new CheckboxField(abstractContainerCheckbox);
        targetField = new TextButtonField(targetControl);
        targetRoleSingularField = new TextField(targetRoleSingularText);
        targetRolePluralField = new TextField(targetRolePluralText);
        minCardinalityField = new CardinalityField(minCardinalityText);
        maxCardinalityField = new CardinalityField(maxCardinalityText);
        productRelevantField = new CheckboxField(productRelevantCheckbox);
        containerRelationField = new TextField(containerRelationText);
        reverseRelationField = new TextField(reverseRelationText);
        
        extFactory.createControls(workArea, uiToolkit, (IpsObjectPartContainer)relation);
        return c;
    }
    
    /**
	 * @param type
	 */
	protected void setDefaults(RelationType type) {
		if (type.isComposition()) {
			relation.setMaxCardinality(Integer.MAX_VALUE);
			relation.setProductRelevant(relation.getPolicyCmptType().isConfigurableByProductCmptType());
		} else if (type.isReverseComposition()) {
			relation.setMinCardinality(1);
			relation.setMaxCardinality(1);
			relation.setProductRelevant(false);
			relation.setTargetRolePluralProductSide(""); //$NON-NLS-1$
			relation.setTargetRoleSingularProductSide(""); //$NON-NLS-1$
		} else if (type.isAssoziation()) {
			relation.setContainerRelation(""); //$NON-NLS-1$
			relation.setReadOnlyContainer(false);
		}
		this.uiController.updateUI();
	}

	private Control createProductSidePage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTargetRoleSingular);
        Text targetRoleSingularText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelTargetRolePlural);
        Text targetRolePluralText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelMinCardinality);
        Text minCardinalityText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_labelMaxCardinality);
        Text maxCardinalityText = uiToolkit.createText(workArea);
        
        // create fields
        targetRoleSingularProductSideField = new TextField(targetRoleSingularText);
        targetRolePluralProductSideField = new TextField(targetRolePluralText);
        minCardinalityProductSideField = new CardinalityField(minCardinalityText);
        maxCardinalityProductSideField = new CardinalityField(maxCardinalityText);
        
        return c;
    }
    
    
    /** 
     * Overridden.
     */
    protected void connectToModel() {
        super.connectToModel();
        
        // first page
        uiController.add(targetField, IRelation.PROPERTY_TARGET);
        uiController.add(abstractContainerField, IRelation.PROPERTY_READONLY_CONTAINER);
        uiController.add(targetRoleSingularField, IRelation.PROPERTY_TARGET_ROLE_SINGULAR);
        uiController.add(targetRolePluralField, IRelation.PROPERTY_TARGET_ROLE_PLURAL);
        uiController.add(typeField, IRelation.PROPERTY_RELATIONTYPE);
        uiController.add(minCardinalityField, IRelation.PROPERTY_MIN_CARDINALITY);
        uiController.add(maxCardinalityField, IRelation.PROPERTY_MAX_CARDINALITY);
        uiController.add(containerRelationField, IRelation.PROPERTY_CONTAINER_RELATION);
        uiController.add(reverseRelationField, IRelation.PROPERTY_REVERSE_RELATION);
        uiController.add(productRelevantField, IRelation.PROPERTY_PRODUCT_RELEVANT);
        
        extFactory.connectToModel(uiController);
        
        // product side page
        uiController.add(targetRoleSingularProductSideField, IRelation.PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE);
        uiController.add(targetRolePluralProductSideField, IRelation.PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE);
        uiController.add(minCardinalityProductSideField, IRelation.PROPERTY_MIN_CARDINALITY_PRODUCTSIDE);
        uiController.add(maxCardinalityProductSideField, IRelation.PROPERTY_MAX_CARDINALITY_PRODUCTSIDE);
    }
    
}
