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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.DatatypeCompletionProcessor;
import org.faktorips.devtools.core.ui.DatatypeSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * A control that allows to edit a reference to a datatype.
 */
public class DatatypeRefControl extends TextButtonControl {
    
    private IIpsProject ipsProject;

    private DatatypeCompletionProcessor completionProcessor;
    
    public DatatypeRefControl(
            IIpsProject project,
            Composite parent, 
            UIToolkit toolkit) {
        super(parent, toolkit, Messages.DatatypeRefControl_title);
        ipsProject = project;
        completionProcessor = new DatatypeCompletionProcessor();
        completionProcessor.setIpsProject(project);
        CompletionUtil.createContentAssistant(completionProcessor);
        ContentAssistHandler.createHandlerForText(text, CompletionUtil.createContentAssistant(completionProcessor));
    }
    
    public void setVoidAllowed(boolean includeVoid) {
        completionProcessor.setIncludeVoid(includeVoid);        
    }
    
    public void setPrimitivesAllowed(boolean includePrimitives) {
    	completionProcessor.setIncludePrimitives(includePrimitives);
    }
    
    public boolean getPrimitivesAllowed() {
    	return completionProcessor.isIncludePrimitives();
    }
    
    public boolean isVoidAllowed() {
        return completionProcessor.isIncludeVoid();
    }
    
    public void setOnlyValueDatatypesAllowed(boolean valuetypesOnly) {
        completionProcessor.setValueDatatypesOnly(valuetypesOnly);
    }
    
    public boolean isOnlyValueDatatypesAllowed() {
        return completionProcessor.getValueDatatypesOnly();
    }

    public void setIpsProject(IIpsProject project) {
        this.ipsProject = project;
        completionProcessor.setIpsProject(project);
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void buttonClicked() {
        try {
            DatatypeSelectionDialog dialog = new DatatypeSelectionDialog(getShell());
            dialog.setElements(ipsProject.findDatatypes(isOnlyValueDatatypesAllowed(), isVoidAllowed()));
            if (dialog.open()==Window.OK) {
                String textToSet = ""; //$NON-NLS-1$
                if (dialog.getResult().length>0) {
                    Datatype datatype = (Datatype)dialog.getResult()[0];
                    textToSet = datatype.getQualifiedName();
                }
                try{
                    immediatelyNotifyListener = true;
                    text.setText(textToSet);
                } finally {
                    immediatelyNotifyListener = false;
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

}
