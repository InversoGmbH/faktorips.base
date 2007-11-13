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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.DefaultUIController;
import org.faktorips.devtools.core.ui.controller.fields.DefaultEditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumDatatypeField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.EnumValueSetChooser;
import org.faktorips.devtools.core.ui.controls.RangeEditControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 *
 */
public class DefaultsAndRangesEditDialog extends IpsPartEditDialog {

    private IConfigElement configElement;
    
    private IPolicyCmptTypeAttribute attribute = null;
    
    // edit fields
    private DefaultEditField defaultValueField;

    private boolean viewOnly;
    
    /**
     * @param parentShell
     * @param title
     */
    public DefaultsAndRangesEditDialog(IConfigElement configElement, Shell parentShell) {
        this(configElement, parentShell, false);
    }
    
    public DefaultsAndRangesEditDialog(IConfigElement configElement, Shell parentShell, boolean viewOnly) {
        super(configElement, parentShell, Messages.PolicyAttributeEditDialog_editLabel, true);
        this.configElement = configElement;
        this.viewOnly = viewOnly;
    }

    /**
     * {@inheritDoc}
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        attribute = configElement.findPcTypeAttribute(configElement.getIpsProject());
        TabFolder folder = (TabFolder)parent;
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.PolicyAttributeEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));

        createDescriptionTabItem(folder);
        super.setEnabledDescription(!viewOnly);
        
        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        uiToolkit.createFormLabel(workArea, Messages.PolicyAttributeEditDialog_defaultValue);
        
        try {
			IValueSet valueSet = getValueSet();
			ValueDatatype datatype = attribute.findDatatype(configElement.getIpsProject());
			if (valueSet.getValueSetType() == ValueSetType.ENUM && datatype != null) {
			    Combo combo = uiToolkit.createCombo(workArea);
				defaultValueField = new EnumValueSetField(combo, (IEnumValueSet)valueSet, datatype);
			} else if (datatype instanceof EnumDatatype) {
			    Combo combo = uiToolkit.createCombo(workArea);
				defaultValueField = new EnumDatatypeField(combo, (EnumDatatype)datatype);
			} else {
				Text defaultValueText = uiToolkit.createText(workArea);
				defaultValueText.setEnabled(!viewOnly);
				defaultValueField = new TextField(defaultValueText);
			}
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}

        Control valueSetControl = createValueSetControl(workArea);
        if (valueSetControl != null) {
            GridData valueSetGridData = new GridData(GridData.FILL_BOTH);
            valueSetGridData.horizontalSpan = 2;
            valueSetControl.setLayoutData(valueSetGridData);
            valueSetControl.setEnabled(!viewOnly);
        }
        return c;
    }

    private Composite createValueSetControl(Composite workArea) {
        try {
            IValueSet valueSet = configElement.getValueSet();
            IValueSet attrValueSet = null;
            IPolicyCmptTypeAttribute attribute = configElement.findPcTypeAttribute(configElement.getIpsProject());
            if (attribute != null) {
                attrValueSet = attribute.getValueSet();
                if (attrValueSet.getValueSetType() == ValueSetType.ALL_VALUES) {
                    valueSet = attrValueSet;
                }
            }
            if (valueSet.getValueSetType() == ValueSetType.RANGE) {
                RangeEditControl rangeEditControl = new RangeEditControl(workArea, uiToolkit, (RangeValueSet)valueSet,
                        uiController);
                return rangeEditControl;
            }
            if (valueSet.getValueSetType() == ValueSetType.ENUM) {
                Datatype type = attribute.findDatatype(configElement.getIpsProject());
                EnumDatatype enumType = null;
                if (type instanceof EnumDatatype) {
                    enumType = (EnumDatatype)type;
                }
                EnumValueSetChooser chooser = new Chooser(workArea, uiToolkit, (IEnumValueSet)attrValueSet,
                        (IEnumValueSet)valueSet, enumType, uiController);
                chooser.setSourceLabel(Messages.DefaultsAndRangesEditDialog_additionalValuesDefinedInModel);
                chooser.setTargetLabel(Messages.DefaultsAndRangesEditDialog_valueDefinedInProductCmpt);
                return chooser;
            }
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(defaultValueField, IConfigElement.PROPERTY_VALUE);
    }

    /**
     * Returns the most restrictive valueset defined for this config element and the
     * underlying attribute.
     * 
     * @throws CoreException If an error occured during the search for the attribute.
     */
    private IValueSet getValueSet() throws CoreException {
        IValueSet valueSet = configElement.getValueSet();
        if (valueSet.getValueSetType() != ValueSetType.ALL_VALUES) {
        	return valueSet;
        }
        if (attribute!=null) {
            return attribute.getValueSet();
        }
        return null;
    }
    
    class Chooser extends EnumValueSetChooser {

        public Chooser(Composite parent, UIToolkit toolkit, IEnumValueSet source, IEnumValueSet target, EnumDatatype type, DefaultUIController uiController) {
            super(parent, toolkit, source, target, type, uiController);
        }

        public MessageList getMessagesForValue(String valueId) {
            MessageList list = new MessageList();
            if (getSourceValueSet().containsValue(valueId)) {
                return list;
            }
            String text = NLS.bind(Messages.DefaultsAndRangesEditDialog_valueNotContainedInValueSet, valueId, getSourceValueSet().toShortString());
            list.add(new Message("", text, Message.ERROR)); //$NON-NLS-1$
            return list;
        }

        
    }
}
