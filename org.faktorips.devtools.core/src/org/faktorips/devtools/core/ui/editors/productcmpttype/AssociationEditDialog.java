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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

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
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.editors.type.DerivedUnionCompletionProcessor;
import org.faktorips.values.EnumValue;


/**
 * A dialog to edit an association.
 */
public class AssociationEditDialog extends IpsPartEditDialog2 {
    
    private IProductCmptTypeAssociation association;
    private PmoAssociation pmoAssociation;
    private ExtensionPropertyControlFactory extFactory;
    
    /**
     * @param parentShell
     * @param title
     */
    public AssociationEditDialog(IProductCmptTypeAssociation association, Shell parentShell) {
        super(association, parentShell, "Edit Association", true );
        this.association = association;
        this.pmoAssociation = new PmoAssociation(association);
        extFactory = new ExtensionPropertyControlFactory(association.getClass());
    }
    
    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText("Properties");
        firstPage.setControl(createFirstPage(folder));
        
        createDescriptionTabItem(folder);
        return folder;
    }
    
    private Control createFirstPage(TabFolder folder) {
    	Composite panel = createTabItemComposite(folder, 1, false);

        createExtensionArea(panel, IExtensionPropertyDefinition.POSITION_TOP);
        createGenerellGroup(uiToolkit.createGroup(panel, "Generel"));
        createDerivedUnionGroup(uiToolkit.createGroup(panel, "Derived union"));
        createExtensionArea(panel, IExtensionPropertyDefinition.POSITION_BOTTOM);
        
        extFactory.bind(bindingContext);
        
        return panel;
    }
    
    private void createExtensionArea(Composite parent, String position) {
        if (!extFactory.needsToCreateControlsFor(association, position)) {
            return;
        }
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        extFactory.createControls(workArea, uiToolkit, association, position);
    }

    private void createGenerellGroup(Composite parent) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);

        // target
        uiToolkit.createFormLabel(workArea, "Target");
        ProductCmptType2RefControl targetControl = new ProductCmptType2RefControl(association.getIpsProject(), workArea, uiToolkit, true);
        bindingContext.bindContent(targetControl, association, IProductCmptTypeAssociation.PROPERTY_TARGET);
        targetControl.setFocus();
        
        // aggregation kind
        uiToolkit.createFormLabel(workArea, "Type:");
        Combo typeCombo = uiToolkit.createCombo(workArea, IProductCmptTypeAssociation.APPLICABLE_ASSOCIATION_TYPES);
        bindingContext.bindContent(typeCombo, association, IAssociation.PROPERTY_ASSOCIATION_TYPE, AssociationType.getEnumType());
        
        // role singular
        uiToolkit.createFormLabel(workArea, "Target role singular:");
        final Text targetRoleSingularText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRoleSingularText, association, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
                    association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
                }
            }
        });
        
        // role plural
        uiToolkit.createFormLabel(workArea, "Target role plural:");
        final Text targetRolePluralText = uiToolkit.createText(workArea);
        bindingContext.bindContent(targetRolePluralText, association, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralText.getText()) && association.isTargetRolePluralRequired()) {
                    association.setTargetRolePlural(association.getDefaultTargetRolePlural());
                }
            }
        });
        
        // min cardinality
        uiToolkit.createFormLabel(workArea, "Min Cardinality");
        Text minCardinalityText = uiToolkit.createText(workArea);
        CardinalityField cardinalityField = new CardinalityField(minCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        
        // max cardinality
        uiToolkit.createFormLabel(workArea, "Max Cardinality");
        Text maxCardinalityText = uiToolkit.createText(workArea);
        cardinalityField = new CardinalityField(maxCardinalityText);
        cardinalityField.setSupportsNull(false);
        bindingContext.bindContent(cardinalityField, association, IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
    }

    private void createDerivedUnionGroup(Composite workArea) {
        Checkbox derivedUnion = uiToolkit.createCheckbox(workArea, "This association is a derived union");
        bindingContext.bindContent(derivedUnion, association, IProductCmptTypeAssociation.PROPERTY_DERIVED_UNION);
        
        Checkbox subsetCheckbox = uiToolkit.createCheckbox(workArea, "This association defines a subset of a derived union");
        bindingContext.bindContent(subsetCheckbox, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);

        Composite temp = uiToolkit.createLabelEditColumnComposite(workArea);
        temp.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        uiToolkit.createFormLabel(temp, "Derived union:");
        Text unionText = uiToolkit.createText(temp);
        bindingContext.bindContent(unionText, association, IProductCmptTypeAssociation.PROPERTY_SUBSETTED_DERIVED_UNION);
        bindingContext.bindEnabled(unionText, pmoAssociation, PmoAssociation.PROPERTY_SUBSET);
        DerivedUnionCompletionProcessor completionProcessor = new DerivedUnionCompletionProcessor(association);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(unionText, CompletionUtil.createContentAssistant(completionProcessor));
    }
    
    
    public class PmoAssociation extends IpsObjectPartPmo {

        public final static String PROPERTY_SUBSET = "subset";

        private IProductCmptTypeAssociation association;
        private boolean subset;
        
        public PmoAssociation(IProductCmptTypeAssociation association) {
            super(association);
            this.association = association;
            subset = association.isSubsetOfADerivedUnion();
        }
        
        public boolean isSubset() {
            return subset;
        }
        
        public void setSubset(boolean newValue) {
            subset = newValue;
            if (!subset) {
                association.setSubsettedDerivedUnion("");
            }
            notifyListeners();
        }
    }        
}
