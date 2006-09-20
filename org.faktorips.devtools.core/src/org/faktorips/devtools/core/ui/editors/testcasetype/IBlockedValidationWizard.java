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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.jface.wizard.IWizard;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;

/**
 * Interface for a wizard wich disables the next button if the editing object is not valid.
 * If a page is displayed the next button from the previous page is always enabled.
 * 
 * @author Joerg Ortmann
 */
public interface IBlockedValidationWizard extends IWizard{

    /**
     * Returns <code>true</code> if the page with the given number is valid or <code>false</code>
     * if the page isn't valid. The page is valid if the editing object is valid or a page after
     * the given page number was displayed.
     */
    public boolean isPageValid(int pageNumber);

    /**
     * Sets the maximum page number which was displayed.
     */
    public void setMaxPageShown(int pageNumber);
    
    /**
     * Starts given runnable in an asynchronous manner.
     */
    public void postAsyncRunnable(Runnable runnable);
    
    /**
     * Returns the ui controller.
     */
    public IpsPartUIController getController();
}
