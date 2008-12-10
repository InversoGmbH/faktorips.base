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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.util.ArgumentCheck;



/**
 * Edit field for text controls.
 */
public class TextField extends DefaultEditField {

    private Text text;
    
    private boolean immediatelyNotifyListener = false;
    
    public TextField(Text text) {
        super();
        ArgumentCheck.notNull(text);
        this.text = text;
    }
    
    /** 
     * {@inheritDoc}
     */
    public Control getControl() {
        return text;
    }
    
    /**
     * Returns the text control this is an edit field for. 
     */
    public Text getTextControl() {
        return text;
    }
    
    /** 
     * {@inheritDoc}
     */
    public Object parseContent() {
        return super.prepareObjectForGet(text.getText());
    }

    /** 
     * {@inheritDoc}
     */
    public void setValue(Object newValue) {
        setText((String)super.prepareObjectForSet(newValue));
    }

    /** 
     * {@inheritDoc}
     */
    public String getText() {
        return text.getText();
    }

    /** 
     * {@inheritDoc}
     */
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            text.setText(newText);
        }
        finally {
            immediatelyNotifyListener = false;
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void insertText(String insertText) {
        text.insert(insertText);
    }

    /** 
     * {@inheritDoc}
     */
    public void selectAll() {
        text.selectAll();
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void addListenerToControl() {
        text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(TextField.this), immediatelyNotifyListener);
            }
            
        });
    }

}
