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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.testcasetype.OpenNewTestCaseTypeWizardAction;

/**
 * Opens the wizard for creating a new TestCaseType.
 * 
 * @author Joerg Ortmann
 */
public class NewTestCaseTypeAction extends Action {
	private IWorkbenchWindow window;
	
	public NewTestCaseTypeAction(IWorkbenchWindow window){
		super();
		this.window = window;
		setText(Messages.NewTestCaseTypeAction_name);
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("NewTestCaseType.gif")); //$NON-NLS-1$
	}
	
	public void run(){
        IWorkbenchWindowActionDelegate openAction = new OpenNewTestCaseTypeWizardAction();
		openAction.init(window);
		openAction.run(this);
	}
}
