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

package org.faktorips.devtools.core.ui.wizards.ipspackage;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Wizard zum Anlegen eines neuen Packages.
 */
public class NewIpsPackageWizard extends Wizard implements INewWizard {

    private IStructuredSelection selection;

    private IpsPackagePage packagePage;

    public NewIpsPackageWizard() {
        setWindowTitle(Messages.IpsPackagePage_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewIpsPackageWizard.png")); //$NON-NLS-1$
    }

    @Override
    public final void addPages() {
        try {
            packagePage = createFirstPage(selection);
            addPage(packagePage);
            createAdditionalPages();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    protected IpsPackagePage createFirstPage(IStructuredSelection selection) {
        return new IpsPackagePage(selection);
    }

    protected void createAdditionalPages() {
        // Empty default implementation
    }

    @Override
    public final boolean performFinish() {
        try {
            // IIpsPackageFragment pack = packagePage.getParentPackageFragment();
            IIpsPackageFragment pack = packagePage.getIpsPackageFragmentRoot().getDefaultIpsPackageFragment();
            String path = packagePage.getIpsPackagePath();
            if (!path.isEmpty()) {
                // do not add the when adding a new package in the root directory (default package)
                path += "."; //$NON-NLS-1$
            }
            path += packagePage.getIpsPackageName();
            pack.createSubPackage(path, true, null);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

}
