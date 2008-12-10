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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

/**
 *
 */
public class ComboField extends DefaultEditField {

    private boolean immediatelyNotifyListener = false;
    
    private Combo combo;
    
    public ComboField(Combo combo) {
        this.combo = combo;
    }
    
    /**
     * {@inheritDoc}
     */
    public Control getControl() {
        return combo;
    }

    public Combo getCombo() {
        return combo;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public Object parseContent() {
        return super.prepareObjectForGet(getText());
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(Object newValue) {
        setText((String)prepareObjectForSet(newValue));
    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        int i = combo.getSelectionIndex();
        if (i == -1) {
            // if no item is selected then return the text given in the text control of the combo
            return combo.getText();
        }
        return combo.getItem(i);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            combo.setText(newText);
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void insertText(String text) {
        combo.setText(text);
    }

    /**
     * {@inheritDoc}
     */
    public void selectAll() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void addListenerToControl() {
        // add selection listener to get notifications if the user changes the selection
        combo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                // changes in combo fields will always be notified immediately,
                // it is not necessary to delay the notification, when the user selects a new item
                // the time for the change is long enough
                notifyChangeListeners(new FieldValueChangedEvent(ComboField.this), immediatelyNotifyListener);
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });
        // add modify listener to get changes when using combo#setText method
        combo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(ComboField.this), immediatelyNotifyListener);
            }
        });
    }

}
