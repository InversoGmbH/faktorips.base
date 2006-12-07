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

package org.faktorips.devtools.core.ui.wizards.move;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.refactor.MoveOperation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Page to let the user select the target package for the move. 
 * 
 * @author Thorsten Guenther
 */
public class MovePage extends WizardPage implements ModifyListener {

	/**
	 * The input field for the target package. 
	 */
	private TreeViewer targetInput;

	/**
	 * The page-id to identify this page.
	 */
	private static final String PAGE_ID = "MoveWizard.move"; //$NON-NLS-1$

    private IIpsElement[] sources;
    
	/**
	 * Creates a new page to select the objects to copy.
	 */
	protected MovePage(IIpsElement[] selectedObjects) {
		super(PAGE_ID, Messages.MovePage_title, null);
        this.sources = selectedObjects;
		super.setDescription(Messages.MovePage_description);
		super.setPageComplete(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
		UIToolkit toolkit = new UIToolkit(null);

		Composite root = toolkit.createComposite(parent);
		root.setLayout(new GridLayout(1, false));
		root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(root);

		toolkit.createFormLabel(root, Messages.MovePage_targetLabel);

		Tree tree = new Tree(root, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        targetInput = new TreeViewer(tree);
        targetInput.setLabelProvider(new MoveLabelProvider());
        targetInput.setContentProvider(new MoveContentProvider());
        try {
            targetInput.setInput(IpsPlugin.getDefault().getIpsModel().getIpsProjects());
        }
        catch (CoreException e) {
            // error creating the input, rethrow as runtime exception
            throw new RuntimeException(e);
        }
        
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.minimumHeight = 300;
        tree.setLayoutData(gridData);
        
        targetInput.addSelectionChangedListener(new ISelectionChangedListener(){
            public void selectionChanged(SelectionChangedEvent event) {
                setPageComplete();
            }
        });
        
        targetInput.expandToLevel(2);
	}
    
	/**
	 * Set the current completion state (and, if neccessary, messages for the user
	 * to help him to get the page complete).
	 */
	private void setPageComplete() {
		setMessage(null);
		setErrorMessage(null);
        
        if (targetInput == null) {
            // page not yet created, so do nothing.
            return;
        }
        
        // check for invalid sources
        try {
            IpsStatus status = MoveOperation.checkSourcesForInvalidContent(sources);
            if (status != null){
                if (status.getSeverity() == IpsStatus.ERROR){
                    setErrorMessage(status.getMessage());
                    super.setPageComplete(false);
                    return;                    
                } else {
                    setMessage(status.getMessage());
                }
            }
        }
        catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            super.setPageComplete(false);
            return;
        }
        
        // check for invalid target selection
		Object selected = ((IStructuredSelection)targetInput.getSelection()).getFirstElement();
        if (! (selected instanceof IIpsPackageFragment)){
            super.setPageComplete(false);
            return;
        }
        
        try {
            if (MoveOperation.isTargetIncludedInSources(sources, (IIpsPackageFragment)selected)){
                setErrorMessage(Messages.MovePage_msgErrorSelectedTargetIsIncludedInSource);
                super.setPageComplete(false);
                return;
            } else {
                for (int i = 0; i < sources.length; i++) {
                    if (! (sources[i] instanceof IIpsPackageFragment)){
                        continue;
                    }
                    IIpsPackageFragment[] childs = ((IIpsPackageFragment)selected).getChildIpsPackageFragments();
                    for (int j = 0; j < childs.length; j++) {
                        if (ObjectUtils.equals(childs[j], sources[i])) {
                            setErrorMessage(NLS.bind(Messages.MovePage_msgErrorPackageAlreadyExists,
                                    ((IIpsPackageFragment)sources[i]).getLastSegmentName()));
                            super.setPageComplete(false);
                            return;
                        }
                    }
                }
            }
        }
        catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            super.setPageComplete(false);
            return;
        }
        
        // all ok
        super.setPageComplete(true);
	}

	/**
	 * Returns the package selected as target. The returned package is neither guaranteed to exist nor
	 * that it can be created.
	 */
	public IIpsPackageFragment getTarget() {
		Object selected = ((IStructuredSelection) targetInput.getSelection())
				.getFirstElement();
		return (IIpsPackageFragment) selected;
	}

	/**
	 * {@inheritDoc}
	 */
	public void modifyText(ModifyEvent e) {
		setPageComplete();
	}

	/**
	 * Label provider for the package selection tree used by this move page.
	 * 
	 * @author Thorsten Guenther
	 */
	private class MoveLabelProvider extends DefaultLabelProvider {
		/**
		 * {@inheritDoc}
		 */
		public String getText(Object element) {
 			String text = ""; //$NON-NLS-1$
            if (element instanceof IIpsPackageFragment) {
                if (((IIpsPackageFragment) element).isDefaultPackage()) {
					text = super.getText(element);
				} else {
				    text = ((IIpsPackageFragment) element).getLastSegmentName();
                }
			} else {
                text = super.getText(element);
            }
			return text;
		}
	}

	/**
	 * Content provider for the package selection tree used by this move page.
	 * All packages (including the default package) of one project are examind.
	 * 
	 * @author Thorsten Guenther
	 */
	private class MoveContentProvider implements ITreeContentProvider {
		/**
		 * {@inheritDoc}
		 */
		public Object[] getChildren(Object parentElement) {
            try {
                if (parentElement instanceof IIpsProject) {
                    return ((IIpsProject)parentElement).getSourceIpsPackageFragmentRoots();
                } else if (parentElement instanceof IIpsPackageFragmentRoot) {
                    ArrayList result = new ArrayList();
                    IIpsPackageFragment def = ((IIpsPackageFragmentRoot)parentElement).getDefaultIpsPackageFragment();
                    result.add(def);
                    result.addAll(Arrays.asList(def.getChildIpsPackageFragments()));
                    return result.toArray();
                } else if (parentElement instanceof IIpsPackageFragment) {
                    if (((IIpsPackageFragment)parentElement).isDefaultPackage()) {
                        return new Object[0];
                    }
                    return ((IIpsPackageFragment)parentElement).getChildIpsPackageFragments();
                }
            }
            catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }            
            return new Object[0];
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getParent(Object element) {
			if (element instanceof IIpsPackageFragment) {
				return ((IIpsPackageFragment) element).getParent();
			} else  if (element instanceof IIpsPackageFragmentRoot) {
                return ((IIpsPackageFragmentRoot) element).getIpsProject();
            }
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IIpsProject[]) {
				return (IIpsProject[]) inputElement;
			}
			return new Object[0];
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
