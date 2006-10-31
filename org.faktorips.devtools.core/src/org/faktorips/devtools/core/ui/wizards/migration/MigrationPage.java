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

package org.faktorips.devtools.core.ui.wizards.migration;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;


/**
 * @author Joerg Ortmann
 */
public class MigrationPage extends WizardPage {
    
    private ProjectSelectionPage projectSelectionPage;
    private Composite overview;
    private Text description;
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public MigrationPage(ProjectSelectionPage projectSelectionPage) {
        super(Messages.MigrationPage_titleMigrationOperations);
        this.projectSelectionPage = projectSelectionPage;
        setMessage(Messages.MigrationPage_msgShortDescription);
        setPageComplete(false);
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        IIpsProject[] projects = projectSelectionPage.getProjects();
        StringBuffer desc = new StringBuffer();
        setPageComplete(true);
        for (int i = 0; i < projects.length; i++) {
            desc.append(Messages.MigrationPage_titleProject).append(projects[i].getName()).append(":").append(SystemUtils.LINE_SEPARATOR).append(SystemUtils.LINE_SEPARATOR); //$NON-NLS-2$
            try {
                desc.append(IpsPlugin.getDefault().getMigrationOperation(projects[i]).getDescription());
                desc.append(SystemUtils.LINE_SEPARATOR);
                desc.append(SystemUtils.LINE_SEPARATOR);
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
                desc.append(Messages.MigrationPage_labelError + e.getMessage());
                desc.append(SystemUtils.LINE_SEPARATOR);
                desc.append(SystemUtils.LINE_SEPARATOR);
                setPageComplete(false);
            }
        }
        description.setText(desc.toString());
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        overview = new Composite(parent, SWT.NONE);
        overview.setLayout(new GridLayout(1, true));
        Label title = new Label(overview, SWT.NONE);
        title.setText(Messages.MigrationPage_labelHeader);
        description = new Text(overview, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        description.setEditable(false);
        
        super.setControl(overview);
    }
    
}
