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

package org.faktorips.devtools.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.ui.editors.IIpsProblemChangedListener;

/**
 * Listens to resource deltas and filters for marker changes of type IpsPlugin.PROBLEM_MARKER
 * Viewers or editors showing error ticks should register as listener to
 * this type.
 * 
 * @author Joerg Ortmann
 */
public class IpsProblemMarkerManager implements IResourceChangeListener {
    private List listeners = new ArrayList();
    
    public IpsProblemMarkerManager() {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_BUILD);
    }

    /**
     * Removes the given ips problem change listener.
     */
    public void removeListener(IIpsProblemChangedListener listener) {
        listeners.remove(listener);
    }

    /**
     * Adds the given ips problem change listener.
     */
    public void addListener(IIpsProblemChangedListener listener) {
        listeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void resourceChanged(IResourceChangeEvent event) {
        HashSet changedElements= new HashSet();
        
        try {
            IResourceDelta delta= event.getDelta();
            if (delta != null){
                delta.accept(new ProjectErrorVisitor(changedElements));
            }
        } catch (CoreException e) {
            IpsPlugin.log(e.getStatus());
        }

        if (!changedElements.isEmpty()) {
            fireChanges((IResource[]) changedElements.toArray(new IResource[changedElements.size()]), true);
        }
    }
    
    /*
     * inform all registered ips problem change listener about the ips problem changes
     */
    private void fireChanges(IResource[] changes, boolean b) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            ((IIpsProblemChangedListener)iter.next()).problemsChanged(changes);
        }
    }

    /**
     * Visitors used to look if the element change delta contains a relevant marker change.
     */
    private static class ProjectErrorVisitor implements IResourceDeltaVisitor {
        private HashSet changedElements; 
        
        public ProjectErrorVisitor(HashSet changedElements) {
            this.changedElements= changedElements;
        }
        
        /**
         * {@inheritDoc}
         */
        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource res= delta.getResource();
            if (res instanceof IProject && delta.getKind() == IResourceDelta.CHANGED) {
                IProject project= (IProject) res;
                if (!project.isAccessible()) {
                    // only track open projects
                    return false;
                }
            }
            checkInvalidate(delta, res);
            return true;
        }
        
        /*
         * check the delta for relevant resources
         */
        private void checkInvalidate(IResourceDelta delta, IResource resource) throws CoreException {
            int kind= delta.getKind();
            if (kind == IResourceDelta.REMOVED || kind == IResourceDelta.ADDED
                    || (kind == IResourceDelta.CHANGED && isErrorDelta(delta))) {
                // invalidate the resource and all parents
                while (resource.getType() != IResource.ROOT && changedElements.add(resource)) {
                    resource = resource.getParent();
                }
            }
        }
        
        /*
         * check if there is an ips problem marker change on the given delta
         */
        private boolean isErrorDelta(IResourceDelta delta) {
            if ((delta.getFlags() & IResourceDelta.MARKERS) != 0) {
                IMarkerDelta[] markerDeltas= delta.getMarkerDeltas();
                for (int i= 0; i < markerDeltas.length; i++) {
                    if (markerDeltas[i].isSubtypeOf(IpsPlugin.PROBLEM_MARKER)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
