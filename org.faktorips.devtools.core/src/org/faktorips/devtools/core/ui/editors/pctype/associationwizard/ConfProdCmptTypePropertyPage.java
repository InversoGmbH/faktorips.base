/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controls.AssociationDerivedUnionGroup;
import org.faktorips.devtools.core.ui.controls.Checkbox;

/**
 * Page to specify the product new product component type association.
 */
public class ConfProdCmptTypePropertyPage extends WizardPage implements IBlockedValidationWizardPage, IHiddenWizardPage {

    private NewPcTypeAssociationWizard wizard;
    private IProductCmptTypeAssociation association;
    private UIToolkit toolkit;
    private BindingContext bindingContext;

    private ArrayList visibleProperties = new ArrayList(10);
    
    private Text targetRoleSingularTextProdCmptType;
    private Text targetRolePluralTextProdCmptType;
    private CardinalityField cardinalityFieldMinProdCmptType;
    private CardinalityField cardinalityFieldMaxProdCmptType;
    private Text descriptionText;
    private Text targetText;
    private Combo typeCombo;
    private Text unionText;
    private Checkbox derivedUnion;
    private Checkbox subsetCheckbox;

    // Composites to dispose an recreate the page content if the inverse association wil be recreated
    // e.g. the target or the option from the previous page are changed
    private Composite pageComposite;
    private Composite groupGeneral;
    private Composite dynamicComposite;
    
    private AssociationDerivedUnionGroup derivedUnionGroup;

    protected ConfProdCmptTypePropertyPage(NewPcTypeAssociationWizard wizard, UIToolkit toolkit, BindingContext bindingContext) {
        super("Product component type association page", "Product component type association properties", null);
        super.setDescription("Define product component type association properties");
        this.wizard = wizard;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;
        
        setPageComplete(true);
    }
    
    public void createControl(Composite parent) {
        pageComposite = wizard.createPageComposite(parent);
        
        createSourceAndTargetControls(toolkit.createLabelEditColumnComposite(pageComposite));
        
        toolkit.createVerticalSpacer(pageComposite, 10);
        
        dynamicComposite = createGeneralControls(pageComposite);
        
        derivedUnionGroup = new AssociationDerivedUnionGroup(toolkit, bindingContext, pageComposite, association);
        
        // description
        descriptionText = wizard.createDescriptionText(pageComposite, 2);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_DESCRIPTION);
        
        setControl(pageComposite);
    }

    private void createSourceAndTargetControls(Composite top) {
        // source 
        toolkit.createFormLabel(top, "Source");
        Text sourceText = toolkit.createText(top);
        sourceText.setEnabled(false);
        IProductCmptType productCmptType = wizard.findProductCmptType();
        if (productCmptType != null){
            sourceText.setText(productCmptType.getQualifiedName());
        }
        
        // target
        toolkit.createFormLabel(top, "Target");
        targetText = toolkit.createText(top);
        targetText.setEnabled(false);
    }
    
    private Composite createGeneralControls(Composite parent) {
        groupGeneral = toolkit.createGroup(parent, "Properties");
        ((GridData)groupGeneral.getLayoutData()).grabExcessVerticalSpace = false;
        
        Composite workArea = toolkit.createLabelEditColumnComposite(groupGeneral);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // top extensions
        wizard.getExtFactoryProductCmptTypeAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_TOP);
        
        // aggregation kind
        toolkit.createFormLabel(workArea, "Type:");
        typeCombo = toolkit.createCombo(workArea, IProductCmptTypeAssociation.APPLICABLE_ASSOCIATION_TYPES);
        
        // role singular
        toolkit.createFormLabel(workArea, "Target role (singular):");
        targetRoleSingularTextProdCmptType = toolkit.createText(workArea);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularTextProdCmptType.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                updateDefaultTargetRoleSingular();
            }
        });

        // role plural
        toolkit.createFormLabel(workArea, "Target role (plural):");
        targetRolePluralTextProdCmptType = toolkit.createText(workArea);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralTextProdCmptType.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                updateDefaultTargetRolePlural();
            }
        });
        
        // min cardinality
        toolkit.createFormLabel(workArea, "Minimum cardinality:");
        Text minCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMinProdCmptType = new CardinalityField(minCardinalityText);
        cardinalityFieldMinProdCmptType.setSupportsNull(false);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        
        // max cardinality
        toolkit.createFormLabel(workArea, "Maximum cardinality:");
        Text maxCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMaxProdCmptType = new CardinalityField(maxCardinalityText);
        cardinalityFieldMaxProdCmptType.setSupportsNull(false);
        visibleProperties.add(IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        
        // bottom extensions
        wizard.getExtFactoryProductCmptTypeAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_BOTTOM);
        
        return workArea;
    }
    
    /**
     * Sets or resets the product component type association. 
     * 
     * @param productCmptAssociation The product component type association which will be edit in this page
     */
    public void setProductCmptTypeAssociationAndUpdatePage(IProductCmptTypeAssociation productCmptAssociation) {
        association = productCmptAssociation;
        
        resetControlsAndBinding();
        
        if (productCmptAssociation != null){
            dynamicComposite.dispose();
            dynamicComposite = createGeneralControls(groupGeneral);
            
            bindAllControls(productCmptAssociation);
        }
        
        refreshPageConrolLayouts();
    }

    private void refreshPageConrolLayouts() {
        groupGeneral.pack(true);
        pageComposite.pack(true);
        pageComposite.getParent().pack(true);
        pageComposite.getParent().layout();
    }
    
    private void resetControlsAndBinding() {
        bindingContext.removeBindings(targetText);
        bindingContext.removeBindings(typeCombo);
        bindingContext.removeBindings(targetRoleSingularTextProdCmptType);
        bindingContext.removeBindings(targetRolePluralTextProdCmptType);
        bindingContext.removeBindings(cardinalityFieldMinProdCmptType.getControl());
        bindingContext.removeBindings(cardinalityFieldMaxProdCmptType.getControl());
        bindingContext.removeBindings(descriptionText);

        bindingContext.removeBindings(derivedUnion);
        bindingContext.removeBindings(subsetCheckbox);
        bindingContext.removeBindings(unionText);

        wizard.getExtFactoryAssociation().removeBinding(bindingContext);

        targetRoleSingularTextProdCmptType.setText("");
        targetRolePluralTextProdCmptType.setText("");
        cardinalityFieldMinProdCmptType.setText("");
        cardinalityFieldMaxProdCmptType.setText(""); 
        descriptionText.setText("");
    }
    
    private void bindAllControls(IProductCmptTypeAssociation productCmptAssociation) {
        bindingContext.bindContent(targetText, association, IProductCmptTypeAssociation.PROPERTY_TARGET);
        bindingContext.bindContent(typeCombo, association, IAssociation.PROPERTY_ASSOCIATION_TYPE, AssociationType.getEnumType());
        bindingContext.bindContent(targetRoleSingularTextProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        bindingContext.bindContent(targetRolePluralTextProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        bindingContext.bindContent(cardinalityFieldMinProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        bindingContext.bindContent(cardinalityFieldMaxProdCmptType, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        bindingContext.bindContent(descriptionText, productCmptAssociation, IProductCmptTypeAssociation.PROPERTY_DESCRIPTION);

        derivedUnionGroup.bindContent(bindingContext, association);
        
        wizard.getExtFactoryProductCmptTypeAssociation().bind(bindingContext);
        
        bindingContext.updateUI();
    }
    
    private void updateDefaultTargetRolePlural() {
        if (StringUtils.isEmpty(association.getTargetRolePlural()) && association.isTargetRolePluralRequired()) {
            association.setTargetRolePlural(association.getDefaultTargetRolePlural());
        }
    }
    
    private void updateDefaultTargetRoleSingular() {
        if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
            association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @return <code>true</code> if the product component type is available.
     */
    public boolean isPageVisible(){
        return wizard.isProductCmptTypeAvailable() && wizard.isConfigureProductCmptType();
    }
    
    /**
     * {@inheritDoc}
     */
    public List getProperties() {
        return visibleProperties;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setVisible(boolean visible) {
        if (visible){
            wizard.handleConfProdCmptTypeSelectionState();
        }
        super.setVisible(visible);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean canFlipToNextPage() {
        return wizard.canPageFlipToNextPage(this);
    }    
}
