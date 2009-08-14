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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.DefaultUIController;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;

/**
 * A composite that consits of three textfields for input. if there is a uicontroller supplied it is
 * used to establish a mapping between the modell object and the control which represents the object
 * property.
 */
public class RangeEditControl extends ControlComposite implements IDataChangeableReadWriteAccess {
    private UIToolkit uiToolkit;

    // lower and button controls
    private Text lower;
    private Text upper;
    private Text step;
    private RangeValueSet range;
    private TextField lowerfield;
    private TextField upperfield;
    private TextField stepfield;
    private IpsObjectUIController uiController;
    private Checkbox containsNullCB;
    private CheckboxField containsNullField;

    private boolean dataChangeable;

    /**
     */
    public RangeEditControl(Composite parent, UIToolkit toolkit, RangeValueSet range, DefaultUIController uiController) {
        super(parent, SWT.NONE);
        this.range = range;
        this.uiToolkit = toolkit;

        setLayout();
        Group group = createRangeGroup(uiToolkit);
        Composite workArea = createWorkArea(uiToolkit, group);
        createTextControls(uiToolkit, workArea);

        if (uiController instanceof IpsObjectUIController) {
            this.uiController = (IpsObjectUIController)uiController;
        } else {
            this.uiController = new IpsObjectUIController((IIpsObjectPart)range.getParent());
        }
        connectToModel();
    }

    private void setLayout() {
        setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 10;
        layout.marginWidth = 0;
        setLayout(layout);
    }

    private Group createRangeGroup(UIToolkit toolkit) {
        Group group = toolkit.createGroup(this, Messages.RangeEditControl_titleRange);
        GridLayout grouplayout = new GridLayout(1, false);
        grouplayout.marginHeight = 10;
        group.setLayout(grouplayout);
        return group;
    }

    private Composite createWorkArea(UIToolkit toolkit, Composite parent) {
        Composite workArea;
        GridLayout layoutWorkArea = new GridLayout(2, false);
        if (toolkit.getFormToolkit() == null) {
            workArea = toolkit.createComposite(parent);
            layoutWorkArea.marginHeight = 0;
            layoutWorkArea.marginWidth = 0;
            layoutWorkArea.horizontalSpacing = 12; // this is important for the diplayed icons !!

        } else {
            workArea = toolkit.getFormToolkit().createComposite(parent);
            layoutWorkArea.marginHeight = 3;
            layoutWorkArea.marginWidth = 1;
        }
        workArea.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        workArea.setLayout(layoutWorkArea);
        return workArea;
    }

    private void createTextControls(UIToolkit toolkit, Composite workArea) {
        toolkit.createFormLabel(workArea, Messages.RangeEditControl_labelMinimum);
        lower = toolkit.createText(workArea);
        lower.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));

        toolkit.createLabel(workArea, Messages.RangeEditControl_labelMaximum);
        upper = toolkit.createText(workArea);
        upper.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));

        toolkit.createFormLabel(workArea, Messages.RangeEditControl_labelStep);
        step = toolkit.createText(workArea);
        step.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));

        toolkit.createLabel(workArea, NLS.bind(Messages.RangeEditControl_labelIncludeNull, IpsPlugin.getDefault()
                .getIpsPreferences().getNullPresentation()));
        containsNullCB = toolkit.createCheckbox(workArea);

        if (toolkit.getFormToolkit() != null) {
            toolkit.getFormToolkit().paintBordersFor(workArea);
            toolkit.getFormToolkit().adapt(workArea);
        }
    }

    private void connectToModel() {
        upperfield = new TextField(upper);
        lowerfield = new TextField(lower);
        stepfield = new TextField(step);
        containsNullField = new CheckboxField(containsNullCB);
        uiController.add(upperfield, range, IRangeValueSet.PROPERTY_UPPERBOUND);
        uiController.add(lowerfield, range, IRangeValueSet.PROPERTY_LOWERBOUND);
        uiController.add(stepfield, range, IRangeValueSet.PROPERTY_STEP);
        uiController.add(containsNullField, range, IRangeValueSet.PROPERTY_CONTAINS_NULL);
        uiController.updateUI();
    }

    public RangeValueSet getRange() {
        return range;
    }

    public void setValueSet(IValueSet valueSet) {
        range = (RangeValueSet)valueSet;
        uiController.remove(upperfield);
        uiController.remove(lowerfield);
        uiController.remove(stepfield);
        uiController.remove(containsNullField);
        uiController.add(upperfield, range, IRangeValueSet.PROPERTY_UPPERBOUND);
        uiController.add(lowerfield, range, IRangeValueSet.PROPERTY_LOWERBOUND);
        uiController.add(stepfield, range, IRangeValueSet.PROPERTY_STEP);
        uiController.add(containsNullField, range, IRangeValueSet.PROPERTY_CONTAINS_NULL);
        uiController.updateUI();
    }

    public void setLower(String newText) {
        lower.setText(newText);
    }

    public String getLower() {
        return lower.getText();
    }

    public void setUpper(String newText) {
        upper.setText(newText);
    }

    public String getUpper() {
        return upper.getText();
    }

    public void setStep(String newText) {
        step.setText(newText);
    }

    public String getStep() {
        return step.getText();
    }

    public Text getLowerControl() {
        return lower;
    }

    public Text getStepControl() {
        return step;
    }

    public Text getUpperControl() {
        return upper;
    }

    public boolean setFocus() {
        return lower.setFocus();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        upper.setEnabled(enabled);
        lower.setEnabled(enabled);
        step.setEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    /**
     * {@inheritDoc}
     */
    public void setDataChangeable(boolean changeable) {
        this.dataChangeable = changeable;

        uiToolkit.setDataChangeable(lower, changeable);
        uiToolkit.setDataChangeable(upper, changeable);
        uiToolkit.setDataChangeable(step, changeable);
        uiToolkit.setDataChangeable(containsNullCB, changeable);
    }
}
