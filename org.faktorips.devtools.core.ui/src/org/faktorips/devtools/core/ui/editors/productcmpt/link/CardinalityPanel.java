/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.editors.productcmpt.Messages;

/**
 * Panel to display cardinality. Note that this is <strong>NOT</strong> a control.
 * <p>
 * To edit a {@link IProductCmptLink} call {@link #setProductCmptLinkToEdit(IProductCmptLink)} and
 * this panel will be updated with the links values. If <code>null</code> is passed to the method,
 * this panel resets and disables itself.
 * 
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public class CardinalityPanel implements IDataChangeableReadWriteAccess {

    private Text minCard;
    private Text maxCard;
    private Text defaultCard;
    private Label minMaxCardLabel;
    private Label defaultCardLabel;
    private Button optional;
    private Button mandatory;
    private Button other;

    private BindingContext bindingContext;
    private CardinalityField minCardField;
    private CardinalityField maxCardField;
    private CardinalityField defaultCardField;

    private IProductCmptLink currentLink;

    private boolean dataChangeable;

    /**
     * Creates a new Cardinality panel
     */
    public CardinalityPanel(Composite parent, UIToolkit toolkit) {
        Composite kardinalityPane = toolkit.createLabelEditColumnComposite(parent);
        ((GridLayout)kardinalityPane.getLayout()).numColumns = 4;
        ((GridLayout)kardinalityPane.getLayout()).horizontalSpacing = 9;
        ((GridLayout)kardinalityPane.getLayout()).marginWidth = 5;
        ((GridLayout)kardinalityPane.getLayout()).marginHeight = 8;

        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, false, false);
        kardinalityPane.setLayoutData(layoutData);

        // create header
        Label headerLabel = toolkit.createLabel(kardinalityPane, Messages.RelationsSection_cardinality);
        toolkit.setHorizontalSpan(headerLabel, 4);
        ((GridData)headerLabel.getLayoutData()).horizontalAlignment = SWT.CENTER;

        // create radio buttons
        KardinalitySelectionListener listener = new KardinalitySelectionListener();
        optional = toolkit.createRadioButton(kardinalityPane, Messages.CardinalityPanel_labelOptional);
        optional.addSelectionListener(listener);
        optional.setLayoutData(new GridData());
        toolkit.setHorizontalSpan(optional, 4);
        mandatory = toolkit.createRadioButton(kardinalityPane, Messages.CardinalityPanel_labelMandatory);
        mandatory.addSelectionListener(listener);
        mandatory.setLayoutData(new GridData());
        toolkit.setHorizontalSpan(mandatory, 4);
        other = toolkit.createRadioButton(kardinalityPane, Messages.CardinalityPanel_labelOther);
        other.addSelectionListener(listener);

        // Min/Max bei Other, Default darunter
        minCard = toolkit.createText(kardinalityPane);
        GridData gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
        gd.widthHint = 15;
        minCard.setLayoutData(gd);
        minMaxCardLabel = toolkit.createLabel(kardinalityPane, ".."); //$NON-NLS-1$
        maxCard = toolkit.createText(kardinalityPane);
        gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
        gd.widthHint = 15;
        maxCard.setLayoutData(gd);

        /*
         * Default Input Field
         */

        Control verticalSpacer = toolkit.createVerticalSpacer(kardinalityPane, 3);
        toolkit.setHorizontalSpan(verticalSpacer, 4);

        defaultCardLabel = toolkit.createLabel(kardinalityPane, Messages.CardinalityPanel_LabelDefaultCardinality);

        defaultCard = toolkit.createText(kardinalityPane);
        ((GridData)defaultCard.getLayoutData()).widthHint = 20;
        toolkit.setHorizontalSpan(defaultCard, 3);

        kardinalityPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        toolkit.getFormToolkit().paintBordersFor(parent);
    }

    /**
     * Configures this {@link CardinalityPanel} to change the cardinality values of the given link
     * when the user modifies the text controls. If the given {@link IProductCmptLink} is
     * <code>null</code> this panel will be deactivated.
     * 
     * @param link the {@link IProductCmptLink} currently edited by this panel or <code>null</code>
     *            if none.
     */
    public void setProductCmptLinkToEdit(IProductCmptLink link) {
        currentLink = link;
        if (currentLink == null) {
            deactivate();
        } else {

            boolean cardinalityPanelEnabled;
            try {
                cardinalityPanelEnabled = currentLink.constrainsPolicyCmptTypeAssociation(currentLink.getIpsProject());
            } catch (CoreException e) {
                IpsPlugin.log(e);
                cardinalityPanelEnabled = false;
            }

            if (!cardinalityPanelEnabled) {
                deactivate();
                return;
            } else {
                if (bindingContext != null) {
                    bindingContext.clear();
                } else {
                    bindingContext = new BindingContext();
                }
                addFields(currentLink);
                bindingContext.updateUI();
                setEnabled(true);
            }
        }
    }

    /**
     * Disables this panel (as in setEnabled(false)) and removes all values and selection from its
     * controls. The panel then displays no data.
     */
    public void deactivate() {
        setEnabled(false);
        removeFields();
        mandatory.setSelection(false);
        optional.setSelection(false);
        other.setSelection(false);
        minCard.setText(""); //$NON-NLS-1$
        maxCard.setText(""); //$NON-NLS-1$
        defaultCard.setText(""); //$NON-NLS-1$
    }

    private void removeFields() {
        if (bindingContext != null) {
            bindingContext.clear();
        }
    }

    private void addFields(IProductCmptLink link) {
        minCardField = new CardinalityField(getMinCardinalityTextControl());
        maxCardField = new CardinalityField(getMaxCardinalityTextControl());
        defaultCardField = new CardinalityField(getDefaultCardinalityTextControl());
        bindingContext.bindContent(minCardField, link, IProductCmptLink.PROPERTY_MIN_CARDINALITY);
        bindingContext.bindContent(maxCardField, link, IProductCmptLink.PROPERTY_MAX_CARDINALITY);
        bindingContext.bindContent(defaultCardField, link, IProductCmptLink.PROPERTY_DEFAULT_CARDINALITY);
    }

    public void refresh() {
        if (bindingContext != null) {
            bindingContext.updateUI();
        }
    }

    /**
     * Method to enable or disable this panel. If no checkbox (optional, mandatory, other) is
     * selected all controls will be disabled, regardless of the given value. This is the case if no
     * ProdCmptLink is selected in the {@link LinksSection}s tree, thus this panel is disabled
     * completely.
     */
    public void setEnabled(boolean enabled) {

        boolean doEnable = enabled & isDataChangeable();

        mandatory.setEnabled(doEnable);
        optional.setEnabled(doEnable);
        other.setEnabled(doEnable);

        minCard.setEnabled(false);
        maxCard.setEnabled(false);
        defaultCard.setEnabled(false);
        minMaxCardLabel.setEnabled(false);
        defaultCardLabel.setEnabled(false);

        String min = minCard.getText();
        String max = maxCard.getText();
        String def = defaultCard.getText();

        if (min.equals("1") && max.equals("1") && def.equals("1")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            mandatory.setSelection(true);
            optional.setSelection(false);
            other.setSelection(false);
        } else if (min.equals("0") && max.equals("1")) { //$NON-NLS-1$ //$NON-NLS-2$ 
            optional.setSelection(true);
            mandatory.setSelection(false);
            other.setSelection(false);
            defaultCard.setEnabled(doEnable);
            defaultCardLabel.setEnabled(doEnable);
        } else {
            other.setSelection(true);
            mandatory.setSelection(false);
            optional.setSelection(false);

            minCard.setEnabled(doEnable);
            maxCard.setEnabled(doEnable);
            minMaxCardLabel.setEnabled(doEnable);
            defaultCard.setEnabled(doEnable);
            defaultCardLabel.setEnabled(doEnable);
        }
    }

    protected void setFieldValue(CardinalityField field, Integer integer, boolean triggerValueChanged) {
        if (field != null) {
            field.setValue(integer, triggerValueChanged);
        }
    }

    /**
     * Returns the Text control which displays the min cardinality
     */
    private Text getMinCardinalityTextControl() {
        return minCard;
    }

    /**
     * Returns the Text control which displays the max cardinality
     */
    private Text getMaxCardinalityTextControl() {
        return maxCard;
    }

    /**
     * Returns the control which displays the default cardinality
     */
    private Text getDefaultCardinalityTextControl() {
        return defaultCard;
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;
        setEnabled(changeable);
    }

    /**
     * Listener to update on cardinality modifications.
     * 
     * @author Thorsten Guenther
     * @author Stefan Widmaier
     */
    private class KardinalitySelectionListener implements SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent e) {

            boolean otherSelected = e.getSource() == other;
            boolean optionalSelected = e.getSource() == optional;
            minCard.setEnabled(otherSelected);
            maxCard.setEnabled(otherSelected);
            minMaxCardLabel.setEnabled(otherSelected);
            defaultCard.setEnabled(otherSelected | optionalSelected);
            defaultCardLabel.setEnabled(otherSelected | optionalSelected);

            /*
             * Setting the cardinality values using their fields causes an inconsistent GUI state
             * and incorrect model data. This is due to the fact that edit fields update the model
             * asynchronously (see EventBroadcaster). Following calls of setValue on other fields
             * might be "too early" as the Text controls will also be updated by triggered
             * modelChangeEvents that then write possibly incorrect or outdated data to the GUI.
             * 
             * Calling the model-setters directly forces synchronous processing and works around
             * those problems.
             */
            if (e.getSource() == optional) {
                currentLink.setMinCardinality(0);
                currentLink.setMaxCardinality(1);
            } else if (e.getSource() == mandatory) {
                currentLink.setMinCardinality(1);
                currentLink.setMaxCardinality(1);
                currentLink.setDefaultCardinality(1);
            }

        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }

    }

}
