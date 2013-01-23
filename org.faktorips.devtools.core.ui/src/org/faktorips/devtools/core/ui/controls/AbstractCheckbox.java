/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.faktorips.devtools.core.ui.UIToolkit;

public abstract class AbstractCheckbox extends ControlComposite {

    private Button button;
    private boolean invertValue = false;

    protected AbstractCheckbox(Composite parent, UIToolkit toolkit, int checkboxStyle, boolean invertValue) {
        this(parent, toolkit, checkboxStyle);
        this.invertValue = invertValue;
    }

    protected AbstractCheckbox(Composite parent, UIToolkit toolkit, int checkboxStyle) {
        super(parent, SWT.NONE);
        GridData data = new GridData(SWT.CENTER | GridData.FILL_HORIZONTAL);
        data.heightHint = 20;
        setLayoutData(data);
        GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        /*
         * SW 18.11.2011 Reduced height margin from 4 to 0 as the previous value would cut off three
         * pixels of the check-box' label at the bottom (at least in linux/gnome). Another
         * possibility would have been to increase the height hint from 20 to 23, but that would
         * have made the abstract check box too high.
         */
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        if (toolkit.getFormToolkit() != null) {
            button = toolkit.getFormToolkit().createButton(this, null, checkboxStyle);
            toolkit.getFormToolkit().adapt(this);
        } else {
            button = new Button(this, checkboxStyle);
        }
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    public Button getButton() {
        return button;
    }

    public boolean isChecked() {
        if (invertValue) {
            return !button.getSelection();
        }
        return button.getSelection();
    }

    public void setChecked(boolean checked) {
        if (invertValue) {
            button.setSelection(!checked);
            return;
        }
        button.setSelection(checked);
    }

    public void setText(String s) {
        button.setText(s);
    }

    @Override
    public void setToolTipText(String string) {
        super.setToolTipText(string);
        button.setToolTipText(string);
    }

    public String getText() {
        return button.getText();
    }

    @Override
    public void addListener(int eventType, Listener listener) {
        super.addListener(eventType, listener);
        if (eventType != SWT.Paint && eventType != SWT.Dispose) {
            listenToControl(button, eventType);
        }
    }

    @Override
    public boolean getEnabled() {
        return button.getEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        button.setEnabled(enabled);
    }

}
