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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.faktorips.devtools.core.ui.wizards.tablecontents.OpenNewTableContentsWizardAction;

/**
 * Open the new product component wizard.
 * 
 * @author Thorsten Guenther
 */
public class NewTableContentAction extends Action {

	private IWorkbenchWindow window;
	
	public NewTableContentAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setText(Messages.NewTableContentAction_name);
	}

	/** 
	 * {@inheritDoc}
	 */
	public void run() {
		OpenNewTableContentsWizardAction o = new OpenNewTableContentsWizardAction();
		o.init(window);
		o.run(this);
	}
}
