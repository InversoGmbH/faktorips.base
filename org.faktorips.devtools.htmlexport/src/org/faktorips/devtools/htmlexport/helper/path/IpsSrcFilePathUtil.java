package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * {@link IpsElementPathUtil} for an {@link IIpsObject}
 * 
 * @author dicker
 * 
 */
public class IpsSrcFilePathUtil extends AbstractIpsElementPathUtil<IIpsSrcFile> {

    public IpsSrcFilePathUtil(IIpsSrcFile ipsElement) {
        super(ipsElement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.helper.path.AbstractIpsElementPathUtil#getFileName()
     */
    @Override
    protected String getFileName() {
        StringBuilder builder = new StringBuilder();
        /*
         * builder.append(getIpsElement().getIpsObjectType().getId()); builder.append('_');
         */
        builder.append(getIpsElement().getName());
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.helper.path.IpsElementPathUtil#getPathToRoot()
     */
    @Override
    public String getPathToRoot() {
        return getPackageFragmentPathToRoot(getIpsPackageFragment());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.helper.path.AbstractIpsElementPathUtil#getIpsPackageFragment
     * ()
     */
    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        return getIpsElement().getIpsPackageFragment();
    }
}
