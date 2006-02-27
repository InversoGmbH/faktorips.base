package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;


/**
 *
 */
public class PdSourceRootSelectionDialog extends ElementTreeSelectionDialog {

    /**
     * @param parent
     * @param renderer
     */
    public PdSourceRootSelectionDialog(Shell parent, boolean onlySourceRoots) {
        super(parent, new DefaultLabelProvider(), new ContentProvider(onlySourceRoots));
        setTitle(Messages.PdSourceRootSelectionDialog_title);
        setMessage(Messages.PdSourceRootSelectionDialog_description);
        setAllowMultiple(false);
        setInput(IpsPlugin.getDefault().getIpsModel());
    }
    
    public IIpsPackageFragmentRoot getSelectedRoot() {
        if (getResult().length>0) {
            return (IIpsPackageFragmentRoot) this.getResult()[0];    
        }
        return null;
    }
    
    private static class ContentProvider implements ITreeContentProvider {
        
        // true if only package fragment roots contains sourcecode are allowed. 
        private boolean onlySourceRoots;

        ContentProvider(boolean onlySourceRoots) {
            this.onlySourceRoots = onlySourceRoots;
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IIpsProject) {
                try {
                    if (onlySourceRoots) {
                        return ((IIpsProject)parentElement).getSourceIpsPackageFragmentRoots();
                    } else {
                        return ((IIpsProject)parentElement).getIpsPackageFragmentRoots();    
                    }
                } catch (CoreException e) {
                    return new Object[0];
                }
            }
            return new Object[0];
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
            return ((IIpsElement)element).getParent();
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object element) {
            return getChildren(element).length>0;
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement) { 
            try {
                return IpsPlugin.getDefault().getIpsModel().getIpsProjects();
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
                return new Object[0];
            }
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }

        /** 
         * Overridden method.
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        
    }
    
}
