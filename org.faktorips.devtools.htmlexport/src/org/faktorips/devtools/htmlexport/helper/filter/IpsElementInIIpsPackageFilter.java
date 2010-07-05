package org.faktorips.devtools.htmlexport.helper.filter;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * Filter, which checks, whether an {@link IpsObject} is within the given {@link IpsPackageFragment}
 * or the is within the {@link IpsPackageFragment} with the same name of the given
 * {@link IpsPackageFragment} (necessary for {@link IpsObject}s in referenced {@link IpsProject}s
 * 
 * @author dicker
 * 
 */
public class IpsElementInIIpsPackageFilter implements IpsElementFilter {
    private IIpsPackageFragment ipsPackageFragment;

    public IpsElementInIIpsPackageFilter(IIpsPackageFragment ipsPackageFragment) {
        this.ipsPackageFragment = ipsPackageFragment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.helper.filter.IpsElementFilter#accept
     * (org.faktorips.devtools.core.model.IIpsElement)
     */
    @Override
    public boolean accept(IIpsElement ipsElement) {

        if (ipsElement instanceof IIpsSrcFile) {

            try {
                IIpsObject ipsObject = ((IIpsSrcFile)ipsElement).getIpsObject();
                return acceptIpsObject(ipsObject);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
        if (ipsElement instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)ipsElement;
            return acceptIpsObject(ipsObject);
        }
        return false;
    }

    private boolean acceptIpsObject(IIpsObject ipsObject) {
        return ipsObject.getIpsPackageFragment().equals(ipsPackageFragment)
                || ipsObject.getIpsPackageFragment().getName().equals(ipsPackageFragment.getName());
    }
}
