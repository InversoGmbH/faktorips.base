/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * The IPS model is the top of the IpsElement hierarchy (like the Java model is the top of the Java
 * element hierarchy). One model instance exists per workspace. The model instance can be retrievedd
 * via the plugin's <code>getIpsModel()</code> method.
 */
public interface IIpsModel extends IIpsElement {

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "IPSMODEL-"; //$NON-NLS-1$

    /**
     * Returns the workspace.
     */
    public IWorkspace getWorkspace();

    /**
     * Runs the given runnable/action as an atomic workspace operation like the <code>run</code>
     * method in IWorkspace. All ips source file change events are queued until the action is
     * finished and then broadcasted. If an ips source file is changed more than once, only one
     * change event is sent.
     * 
     * @throws CoreException
     * 
     * @see IWorkspace#run(org.eclipse.core.resources.IWorkspaceRunnable,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void runAndQueueChangeEvents(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException;

    /**
     * Runs the given runnable/action as an atomic workspace operation like the <code>run</code>
     * method in IWorkspace. All ips source file change events are queued until the action is
     * finished and then broadcastet. If an ips source file is change more than one, only one change
     * event is sent.
     * 
     * @throws CoreException
     * 
     * @see IWorkspace#run(org.eclipse.core.resources.IWorkspaceRunnable,
     *      org.eclipse.core.runtime.jobs.ISchedulingRule, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void runAndQueueChangeEvents(IWorkspaceRunnable action,
            ISchedulingRule rule,
            int flags,
            IProgressMonitor monitor) throws CoreException;

    /**
     * Creates an IpsProject for the given Java project by adding the IPS nature and creating (an
     * empty) .ipsproject file.
     * 
     * @throws NullPointerException if javaProjecct is <code>null</code>.
     * @throws CoreException if an error occurs while creating the ips project.
     */
    public IIpsProject createIpsProject(IJavaProject javaProject) throws CoreException;

    /**
     * Returns all IPS projects opened in the workspace or an empty array if none.
     */
    public IIpsProject[] getIpsProjects() throws CoreException;

    /**
     * Returns the IpsProject with the indicated name.
     */
    public IIpsProject getIpsProject(String name);

    /**
     * Returns the IpsProject that belongs to the indicated platform project.
     * 
     * @throws NullPointerException if project is null.
     */
    public IIpsProject getIpsProject(IProject project);

    /**
     * Returns all <code>IProject</code>s in the workspace, that do not have an IpsProject nature.
     * Ignores closed projects.
     * 
     * @throws CoreException if an exception occurs while accessing project-natures.
     */
    public IResource[] getNonIpsProjects() throws CoreException;

    /**
     * Returns the IpsElement that corresponds to the indicated resource. The IpsElement may not be
     * valid or existing inside a IPS project's source folder.
     */
    public IIpsElement getIpsElement(IResource resource);

    /**
     * Returns the IpsElement that corresponds to the indicated resource, if such a valid element
     * exists for that resource. Returns <code>null</code> if no corresponding element is found or
     * the resource is not inside a valid context.
     */
    public IIpsElement findIpsElement(IResource resource);

    /**
     * <p>
     * Adds a listener that is notified when something in the model was changed. The notifications
     * are made in the ui-thread.
     * </p>
     * <p>
     * <strong>Important</strong>: Do not forget to <strong>remove the listener again</strong> if no
     * longer needed to prevent memory leaks.
     * </p>
     * 
     * @see #removeChangeListener(ContentsChangeListener)
     */
    public void addChangeListener(ContentsChangeListener listener);

    /**
     * Removes the change listener.
     */
    public void removeChangeListener(ContentsChangeListener listener);

    /**
     * <p>
     * Adds the modification status change listener.
     * </p>
     * <p>
     * <strong>Important</strong>: Do not forget to <strong>remove the listener again</strong> if no
     * longer needed to prevent memory leaks.
     * </p>
     * 
     * @see #removeModificationStatusChangeListener(IModificationStatusChangeListener)
     */
    public void addModifcationStatusChangeListener(IModificationStatusChangeListener listener);

    /**
     * Removes the modification status change listener.
     */
    public void removeModificationStatusChangeListener(IModificationStatusChangeListener listener);

    /**
     * Returns all package fragment roots containing source files or an empty array if none is
     * found.
     * 
     * @throws CoreException
     */
    public IIpsPackageFragmentRoot[] getSourcePackageFragmentRoots() throws CoreException;

    /**
     * Returns the extension properties for the given type. Returns an empty array if no extension
     * property is defined.
     * 
     * @param type The published interface of the ips object or part e.g.
     *            <code>org.faktorips.plugin.model.pctype.IAttribute</code>
     * @param <code>true</code> if not only the extension properties defined for for the type itself
     *        should be returned, but also the ones registered for it's supertype(s) and it's
     *        interfaces.
     */
    public IExtensionPropertyDefinition[] getExtensionPropertyDefinitions(Class type,
            boolean includeSupertypesAndInterfaces);

    /**
     * Returns the extension property with the given id that belongs to the given type. Returns
     * <code>null</code> if no such extension property is defined.
     * 
     * @param type The published interface of the ips object or part e.g.
     *            <code>or.faktorips.plugin.model.pctype.Attribute</code>
     * @param propertyId the extension property id
     * @param <code>true</code> if not only the extension properties defined for for the type itself
     *        should be returned, but also the ones registered for it's supertype(s) and it's
     *        interfaces.
     */
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(Class type,
            String propertyId,
            boolean includeSupertypesAndInterfaces);

    /**
     * Returns the predefines value datatypes like String, Integer etc.
     */
    public ValueDatatype[] getPredefinedValueDatatypes();

    /**
     * Returns <code>true</code> if the datatype is predefined (via the datatypeDefinition extension
     * point), otherwise <code>false</code>. Returns <code>false</code> if datatypeId is
     * <code>null</code>.
     */
    public boolean isPredefinedValueDatatype(String datatypeId);

    /**
     * Returns the available changes over time naming conventions.
     */
    public IChangesOverTimeNamingConvention[] getChangesOverTimeNamingConvention();

    /**
     * Returns the changes in time naming convention identified by the given id. If the id does not
     * identify a naming convention, the VAA naming convention is returned per default.
     */
    public IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention(String id);

    /**
     * Removes the given object from the model.
     */
    public void delete(IIpsElement toDelete);

    /**
     * Clears the validation results for all elements in the workspace.
     */
    public void clearValidationCache();

    /**
     * Returns the type of ips objects supported.
     */
    public IpsObjectType[] getIpsObjectTypes();

    /**
     * Returns the type identified by the name or <code>null</code> if no such type exists.
     */
    public IpsObjectType getIpsObjectType(String name);

    /**
     * Returns the type identified by the given file extension or <code>null</code> if no such type
     * exists.
     */
    public IpsObjectType getIpsObjectTypeByFileExtension(String fileExtension);

    /**
     * Returns all registered IpsArtefactBuilderSetInfo objects.
     */
    public IIpsArtefactBuilderSetInfo[] getIpsArtefactBuilderSetInfos();

    /**
     * Returns the IpsArtefactBuilderSetInfo object for the specified id. If none is found
     * <code>null</code> will be returned.
     */
    public IIpsArtefactBuilderSetInfo getIpsArtefactBuilderSetInfo(String id);

}
