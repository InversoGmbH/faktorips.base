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

package org.faktorips.devtools.core.ui.wizards.migration;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * @author Thorsten Guenther
 */
public class ProjectSelectionPage extends WizardPage {
    private CheckboxTreeViewer treeViewer;
    private ArrayList preSelected;
    /**
     * @param pageName
     */
    protected ProjectSelectionPage(ArrayList preSelected) {
        super(Messages.ProjectSelectionPage_titleSelectProjects);
        this.preSelected = preSelected;
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new GridLayout(1, true));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeViewer = new CheckboxTreeViewer(root);
        treeViewer.setContentProvider(new ContentProvider());
        treeViewer.setLabelProvider(new TreeLabelProvider());
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        treeViewer.setInput(""); //$NON-NLS-1$
        
        treeViewer.setCheckedElements(preSelected.toArray());
        
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        
            public void selectionChanged(SelectionChangedEvent event) {
                setPageComplete(getProjects().length > 0);
            }
        });
        setPageComplete(false);
        super.setControl(root);
    }

    /**
     * @return All IIpsProjects selected.
     */
    protected IIpsProject[] getProjects() {
        Object[] checked = treeViewer.getCheckedElements();
        IIpsProject[] projects = new IIpsProject[checked.length];
        for (int i = 0; i < projects.length; i++) {
            projects[i] = (IIpsProject)checked[i];
        }
        return projects;
    }
    
    private class TreeLabelProvider extends LabelProvider {

        public String getText(Object element) {
            return ((IIpsProject)element).getName();
        }

        public Image getImage(Object element) {
            return ((IIpsProject)element).getImage();
        }
    }

    private class ContentProvider implements ITreeContentProvider {

        /**
         * {@inheritDoc}
         */
        public Object[] getChildren(Object parentElement) {
            return new Object[0];
        }

        /**
         * {@inheritDoc}
         */
        public Object getParent(Object element) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasChildren(Object element) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public Object[] getElements(Object inputElement) {
            ArrayList result = new ArrayList();
            IIpsProject[] projects;
            try {
                projects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
                setMessage("An internal error occurred while reading the projects", DialogPage.ERROR);
                return new Object[0];
            }
            for (int i = 0; i < projects.length; i++) {
                try {
                    if (!IpsPlugin.getDefault().getMigrationOperation(projects[i]).isEmpty()) {
                        result.add(projects[i]);
                    }
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            if (result.size() == 0) {
                setMessage(Messages.ProjectSelectionPage_msgNoProjects, DialogPage.INFORMATION);
            }
            else {
                setMessage(Messages.ProjectSelectionPage_msgSelectProjects);
            }
            return (IIpsProject[])result.toArray(new IIpsProject[result.size()]);
        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {
            // nothing to do
        }

        /**
         * {@inheritDoc}
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do
        }
    }
}
