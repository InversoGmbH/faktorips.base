/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.formulalibrary.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;

/**
 * Wizard for new {@link IFormulaLibrary}
 * 
 * @author frank
 */
public class NewFormulaLibraryWizard extends NewIpsObjectWizard {

	private static final String PLUGIN_ID = "org.faktorips.devtools.formulalibrary.ui"; //$NON-NLS-1$
    private static final String FORMULA_LIBRARY_WIZARD_PNG = "NewFormulaLibraryWizard.png"; //$NON-NLS-1$
    private static final String WIZARD_FOLDER = "icons/wizards/"; //$NON-NLS-1$

    private FormulaLibraryPage page;

    public NewFormulaLibraryWizard() {
        setDefaultPageImageDescriptor(IpsUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, WIZARD_FOLDER
                + FORMULA_LIBRARY_WIZARD_PNG));
    }

    @Override
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws Exception {
        page = new FormulaLibraryPage(selection);
        return page;
    }

}
