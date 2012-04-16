/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public abstract class IpsElement extends PlatformObject implements IIpsElement {

    protected String name;
    protected IIpsElement parent;

    final static IIpsElement[] NO_CHILDREN = new IIpsElement[0];

    /**
     * Resource mapping based on the mapping for the resource model.
     */
    private class IpsElementResourceMapping extends ResourceMapping {

        private IIpsElement ipsElement;

        public IpsElementResourceMapping(IIpsElement ipsElement) {
            this.ipsElement = ipsElement;
        }

        @Override
        public Object getModelObject() {
            return ipsElement.getEnclosingResource();
        }

        @Override
        public String getModelProviderId() {
            return ModelProvider.RESOURCE_MODEL_PROVIDER_ID;
        }

        @Override
        public IProject[] getProjects() {
            IIpsProject ipsProject = ipsElement.getIpsProject();
            return new IProject[] { ipsProject.getProject() };
        }

        @Override
        public ResourceTraversal[] getTraversals(ResourceMappingContext context, IProgressMonitor monitor) {
            Object modelObject = getModelObject();
            if (modelObject instanceof IResource) {
                final IResource resource = (IResource)modelObject;
                if (resource.getType() == IResource.ROOT) {
                    return new ResourceTraversal[] { new ResourceTraversal(((IWorkspaceRoot)resource).getProjects(),
                            IResource.DEPTH_INFINITE, IResource.NONE) };
                }
                return new ResourceTraversal[] { new ResourceTraversal(new IResource[] { resource },
                        IResource.DEPTH_INFINITE, IResource.NONE) };
            }
            return null;
        }

    }

    public IpsElement(IIpsElement parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    /**
     * Constructor for testing purposes.
     */
    public IpsElement() {
        // Constructor for testing purposes.
    }

    @Override
    @SuppressWarnings("rawtypes")
    // IAdaptable uses raw type
    public Object getAdapter(Class adapterType) {
        // TODO this code is getting deprecated. The adapters should be handled in
        // IpsElementAdapterFactory

        if (adapterType == null) {
            return null;
        }
        IResource enclosingResource = getEnclosingResource();
        if (adapterType.isInstance(enclosingResource)) {
            return enclosingResource;
        }
        // TODO the adaptation to ResourceMapping have to be moved to the IpsElementAdapterFactory.
        if (adapterType.equals(ResourceMapping.class)) {
            return new IpsElementResourceMapping(this);
        }
        return super.getAdapter(adapterType);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public final IIpsElement getParent() {
        return parent;
    }

    @Override
    public boolean exists() {
        if (getParent() == null || !getParent().exists()) {
            return false;
        }
        if (getCorrespondingResource() == null) {
            /*
             * If no corresponding resource exists, the EnclosingResource.exists() is handled by
             * calling getParent().exists() above. So if we have arrived here, we have to return
             * true (the parent exists) to avoid a NullPointerException in the rest of the code.
             */
            return true;
        }
        return getCorrespondingResource().exists();
    }

    @Override
    public IResource getEnclosingResource() {
        IResource resource = getCorrespondingResource();
        if (resource != null) {
            return resource;
        }
        return getParent().getEnclosingResource();
    }

    @Override
    public IIpsModel getIpsModel() {
        return IpsPlugin.getDefault().getIpsModel();
    }

    @Override
    public IIpsProject getIpsProject() {
        if (getParent() == null) {
            return null;
        }
        return getParent().getIpsProject();
    }

    @Override
    public IIpsElement[] getChildren() throws CoreException {
        return NO_CHILDREN;
    }

    @Override
    public boolean hasChildren() throws CoreException {
        return getChildren().length > 0;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IIpsElement)) {
            return false;
        }
        IIpsElement other = (IIpsElement)o;
        return other.getName().equals(getName())
                && ((parent == null && other.getParent() == null) || (parent != null && parent
                        .equals(other.getParent())));
    }

    @Override
    public String toString() {
        if (getParent() == null) {
            return getName();
        }
        return getParent().toString() + "/" + getName(); //$NON-NLS-1$
    }

    @Override
    public boolean isContainedInArchive() {
        return getParent().isContainedInArchive();
    }

}
