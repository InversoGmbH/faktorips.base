package org.faktorips.devtools.core.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * The ips builder generates Java sourcecode and xml files based on the ips objects contained in the
 * ips project. It runs before the Java builder, so that first the Java sourcecode is generated by
 * the ips builder and then the Java builder compiles the Java sourcecode into classfiles.
 */
public class IpsBuilder extends IncrementalProjectBuilder {

    /**
     * The builders extension id.
     */
    public final static String BUILDER_ID = IpsPlugin.PLUGIN_ID + ".ipsbuilder";

    private static boolean lastBuildWasCancelled = false;

    // a map contains the modification stamp for each ips package fragment root's product component
    // registry's
    // table of contents.
    private Map packFrgmtRootTocModStamps = new HashMap();

    private DependencyGraph dependencyGraph;

    public IpsBuilder() {
        super();
    }

    private MultiStatus applyBuildCommand(MultiStatus buildStatus, BuildCommand command)
            throws CoreException {

        IIpsArtefactBuilderSet currentBuilderSet = getIpsProject().getCurrentArtefactBuilderSet();

        if (currentBuilderSet == null) {
            return buildStatus;
        }
        IIpsArtefactBuilder[] artefactBuilders = currentBuilderSet.getArtefactBuilders();
        for (int i = 0; i < artefactBuilders.length; i++) {
            try {
                command.build(artefactBuilders[i], buildStatus);
            } catch (Exception e) {
                return addIpsStatus(artefactBuilders[i], command, buildStatus, e);
            }
        }
        return buildStatus;
    }

    private MultiStatus addIpsStatus(IIpsArtefactBuilder builder, BuildCommand command, MultiStatus buildStatus, Exception e) {
        MultiStatus returnStatus = null;
        if (buildStatus == null) {
            returnStatus = new MultiStatus(IpsPlugin.PLUGIN_ID, 0, "Build Results", null);
        }
        String text = builder.getName() + ": Error during: " + command + ".";
        buildStatus.add(new IpsStatus(text, e));
        return returnStatus;
    }

    private DependencyGraph getDependencyGraph() throws CoreException {
        if (dependencyGraph == null) {
            dependencyGraph = new DependencyGraph(getIpsProject());
        }
        return dependencyGraph;
    }

    /**
     * Overridden method.
     */
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
        MultiStatus buildStatus = null;
        if (lastBuildWasCancelled) {
            lastBuildWasCancelled = false;
            return null;
        }
        rememberTocModificationStamps();
        buildStatus = applyBuildCommand(buildStatus, new BeforeBuildProcessCommand(kind));
        if (kind == IncrementalProjectBuilder.FULL_BUILD
                || kind == IncrementalProjectBuilder.CLEAN_BUILD || getDelta(getProject()) == null) {
            // delta not available
            buildStatus = fullBuild(monitor);
        } else {
            buildStatus = incrementalBuild(monitor);
        }
        buildStatus = applyBuildCommand(buildStatus, new AfterBuildProcessCommand(kind));

        saveTocs();
        if (buildStatus.getSeverity() == IStatus.OK) {
            return null;
        }

        try {
            IIpsArtefactBuilderSet builderSet = getIpsProject().getCurrentArtefactBuilderSet();
            builderSet.initialize();
        } catch (Exception e) {
            buildStatus
                    .add(new IpsStatus(
                            "An exception occured while trying to reinitialize the current " +
                            "artefact builder set. The reason for reinitializing the current " +
                            "builder set was an exception that was thrown during the build cycle."));
        }
        throw new CoreException(buildStatus);
    }

    /*
     * Returns the ips project the build is currently building.
     */
    private IIpsProject getIpsProject() {
        return IpsPlugin.getDefault().getIpsModel().getIpsProject(getProject());
    }

    private void rememberTocModificationStamps() throws CoreException {
        IIpsPackageFragmentRoot[] srcRoots = getIpsProject().getSourceIpsPackageFragmentRoots();
        for (int i = 0; i < srcRoots.length; i++) {
            IpsPackageFragmentRoot root = (IpsPackageFragmentRoot)srcRoots[i];
            long modStamp = root.getRuntimeRepositoryToc().getModificationStamp();
            packFrgmtRootTocModStamps.put(root, new Long(modStamp));
        }
    }

    /**
     * Saves the table of contents for all source package fragment roots (if the toc has changed.).
     * 
     * @throws CoreException
     */
    private void saveTocs() throws CoreException {
        IIpsPackageFragmentRoot[] srcRoots = getIpsProject().getSourceIpsPackageFragmentRoots();
        for (int i = 0; i < srcRoots.length; i++) {
            IpsPackageFragmentRoot root = (IpsPackageFragmentRoot)srcRoots[i];
            Long oldModStamp = (Long)packFrgmtRootTocModStamps.get(root);
            if (oldModStamp.longValue() != root.getRuntimeRepositoryToc().getModificationStamp()) {
                root.saveProductCmptRegistryToc();
            }
        }
    }

    /**
     * Full build generates Java source files for all IPS objects.
     */
    private MultiStatus fullBuild(IProgressMonitor monitor) {
        System.out.println("Full build started.");
        long begin = System.currentTimeMillis();
        MultiStatus buildStatus = new MultiStatus(IpsPlugin.PLUGIN_ID, 0, "Full Build Results",
                null);

        try {
            dependencyGraph = new DependencyGraph(getIpsProject());
            IIpsPackageFragmentRoot[] roots = getIpsProject().getIpsPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++) {
                ((IpsPackageFragmentRoot)roots[i]).getRuntimeRepositoryToc().clear();
                IIpsPackageFragment[] packs = roots[i].getIpsPackageFragments();
                for (int j = 0; j < packs.length; j++) {
                    IIpsElement[] elements = packs[j].getChildren();
                    for (int k = 0; k < elements.length; k++) {
                        if (elements[k] instanceof IIpsSrcFile) {
                            try {
                                buildIpsSrcFile((IIpsSrcFile)elements[k], buildStatus, monitor);
                            } catch (Exception e) {
                                buildStatus.add(new IpsStatus(e));
                            }
                        }
                    }
                }
                removeEmptyFolders(roots[i].getArtefactDestination(), false, monitor);
            }

        } catch (CoreException e) {
            buildStatus.add(new IpsStatus(e));
        }
        long end = System.currentTimeMillis();
        System.out.println("Full build finished. Duration: " + (end - begin));
        return buildStatus;
    }

    /**
     * 
     * Overridden IMethod.
     */
    protected void clean(IProgressMonitor monitor) throws CoreException {
        // since the introduction of JMerge the generated java source files don't
        // have to be deleted anymore
    }

    private void removeEmptyFolders(IFolder parent,
            boolean removeThisParent,
            IProgressMonitor monitor) throws CoreException {
        if (!parent.exists()) {
            return;
        }
        IResource[] members = parent.members();
        if (removeThisParent && members.length == 0) {
            parent.delete(true, monitor);
            return;
        }
        for (int i = 0; i < members.length; i++) {
            if (members[i].getType() == IResource.FOLDER) {
                removeEmptyFolders((IFolder)members[i], true, monitor);
            }
        }
    }

    /**
     * Incremental build generates Java source files for all PdObjects that have been changed.
     */
    private MultiStatus incrementalBuild(IProgressMonitor monitor) {
        System.out.println("Incremental build started.");
        MultiStatus buildStatus = new MultiStatus(IpsPlugin.PLUGIN_ID, 0,
                "Incremental Build Results", null);
        IResourceDelta delta = getDelta(getProject());
        try {

            IncBuildVisitor visitor = new IncBuildVisitor(buildStatus, monitor);
            delta.accept(visitor);
            buildStatus = visitor.buildStatus;
        } catch (Exception e) {
            buildStatus.add(new IpsStatus(e));
        } finally {
            System.out.println("Incremental build finished.");
            monitor.done();
        }
        return buildStatus;
    }

    private void updateMarkers(IIpsObject object) throws CoreException {
        if (object==null) {
            return;
        }
        IResource resource = object.getEnclosingResource();
        if (!resource.exists()) {
            return;
        }
        resource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        MessageList list = object.validate();
        for (int i = 0; i < list.getNoOfMessages(); i++) {
            Message msg = list.getMessage(i);
            IMarker marker = resource.createMarker(IMarker.PROBLEM);
            marker.setAttribute(IMarker.MESSAGE, msg.getText());
            marker.setAttribute(IMarker.SEVERITY, getMarkerSeverity(msg));
        }
    }

    private int getMarkerSeverity(Message msg) {
        int msgSeverity = msg.getSeverity();
        if (msgSeverity == Message.ERROR) {
            return IMarker.SEVERITY_ERROR;
        } else if (msgSeverity == Message.WARNING) {
            return IMarker.SEVERITY_WARNING;
        } else if (msgSeverity == IMarker.SEVERITY_INFO) {
            return IMarker.SEVERITY_INFO;
        }
        throw new RuntimeException("Unknown severity " + msgSeverity);
    }

    /**
     * Builds the indicated file.
     * 
     * @throws CoreException
     */
    private void buildIpsSrcFile(IIpsSrcFile file, MultiStatus buildStatus, IProgressMonitor monitor)
            throws CoreException {
        if (!file.isContentParsable()) {
            return;
        }
        IIpsObject ipsObject = file.getIpsObject();
        applyBuildCommand(buildStatus, new BuildArtefactBuildCommand(file));
        updateMarkers(ipsObject);
        HashSet toUpdate = new HashSet();
        toUpdate.add(ipsObject.getQualifiedNameType());
        buildDependants(buildStatus, ipsObject.getQualifiedNameType(), toUpdate, monitor);
        updateDependencyGraph(toUpdate);
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    private void updateDependencyGraph(Set toUpdate) throws CoreException {
        for (Iterator it = toUpdate.iterator(); it.hasNext();) {
            QualifiedNameType qualifiedNameType = (QualifiedNameType)it.next();
            getDependencyGraph().update(qualifiedNameType);
        }
    }

    private void buildDependants(MultiStatus buildStatus,
            QualifiedNameType nameType,
            Set alreadyBuild,
            IProgressMonitor monitor) throws CoreException {
        QualifiedNameType[] dependants = getDependencyGraph().getDependants(nameType);
        for (int i = 0; i < dependants.length; i++) {
            if (!alreadyBuild.contains(dependants[i])) {
                IIpsObject dependant = getIpsProject().findIpsObject(dependants[i]);
                updateMarkers(dependant);
                if(dependant != null){
                    applyBuildCommand(buildStatus, new BuildArtefactBuildCommand(dependant
                        .getIpsSrcFile()));
                }
                alreadyBuild.add(dependants[i]);
                buildDependants(buildStatus, dependants[i], alreadyBuild, monitor);
            }
        }
    }

    private void deleteIpsSrcFile(IIpsSrcFile file, MultiStatus buildStatus, IProgressMonitor monitor)
            throws CoreException {
        applyBuildCommand(buildStatus, new DeleteArtefactBuildCommand(file));
        HashSet toUpdate = new HashSet();
        toUpdate.add(file.getQualifiedNameType());
        buildDependants(buildStatus, file.getQualifiedNameType(), toUpdate, monitor);
        updateDependencyGraph(toUpdate);
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    /**
     * Returns the number of entries in the delta
     * 
     * @throws CoreException
     */
    protected int getWorkLoad(IResourceDelta delta) throws CoreException {
        WorkLoadVisitor visitor = new WorkLoadVisitor();
        delta.accept(visitor);
        return visitor.work;
    }

    /**
     * ResourceDeltaVisitor for the incremental build.
     */
    private class IncBuildVisitor implements IResourceDeltaVisitor {

        private IProgressMonitor monitor;
        private MultiStatus buildStatus;

        private IncBuildVisitor(MultiStatus buildStatus, IProgressMonitor monitor) {
            this.monitor = monitor;
            this.buildStatus = buildStatus;
        }

        /**
         * Checks if the provided resource is the java output folder resource or the IpsProject
         * output folder resource.
         * 
         * @throws CoreException
         */
        private boolean ignoredResource(IResource resource) throws CoreException {
            IPath outPutLocation = getIpsProject().getJavaProject().getOutputLocation();
            IPath resourceLocation = resource.getFullPath();
            if (outPutLocation.equals(resourceLocation)) {
                return true;
            }
            IFolder[] outPutFolders = getIpsProject().getIpsObjectPath().getOutputFolders();
            for (int i = 0; i < outPutFolders.length; i++) {
                if (outPutFolders[i].getFullPath().equals(resourceLocation)) {
                    return true;
                }
            }
            return false;
        }

        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();
            if (resource == null || resource.getType() == IResource.PROJECT) {
                return true;
            }
            // resources in the output folders of the ipsProject and the assigned java project are
            // ignored
            if (ignoredResource(resource)) {
                return false;
            }
            // TODO check if on classpath and is a source file
            switch (delta.getKind()) {
                case IResourceDelta.ADDED:
                    return handleChangedOrAddedResource(resource);
                case IResourceDelta.REMOVED:
                    return handleRemovedResource(resource);
                case IResourceDelta.CHANGED: {
                    // skip changes, not caused by content changes,
                    if (delta.getFlags() != 0) {
                        return handleChangedOrAddedResource(resource);
                    }
                }
                    break;
            }
            return true;
        }

        private boolean handleChangedOrAddedResource(IResource resource) throws CoreException {
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(resource);
            if (!element.exists()) { // not on classpath?
                return true;
            }
            if (!(element instanceof IIpsSrcFile)) {
                return true;
            }
            IIpsSrcFile file = (IIpsSrcFile)element;
            buildIpsSrcFile(file, buildStatus, monitor);
            return true;
        }

        private boolean handleRemovedResource(IResource resource) throws CoreException {
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(resource);

            if (!(element instanceof IIpsSrcFile)) {
                return true;
            }
            IIpsSrcFile file = (IIpsSrcFile)element;
            IIpsPackageFragmentRoot[] roots = file.getIpsProject()
                    .getSourceIpsPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++) {
                if (file.getIpsPackageFragment().getRoot().equals(roots[i])) {
                    deleteIpsSrcFile(file, buildStatus, monitor);
                    return true;
                }
            }
            return true;
        }
    }

    /**
     * ResourceDeltaVisitor that counts the resources that have been added, deleted or changed.
     */
    private static class WorkLoadVisitor implements IResourceDeltaVisitor {

        int work = 0;

        public boolean visit(IResourceDelta delta) throws CoreException {
            switch (delta.getKind()) {
                case IResourceDelta.ADDED:
                    work++;
                    break;
                case IResourceDelta.REMOVED:
                    work++;
                    break;
                case IResourceDelta.CHANGED:
                    work++;
                    break;
                default:
                    throw new RuntimeException("Unkown delta kind " + delta.getKind());
            }
            return true;
        }
    }

    /*
     * The applyBuildCommand method of this class uses this interface.
     */
    private interface BuildCommand {
        public void build(IIpsArtefactBuilder builder, MultiStatus status) throws CoreException;
    }

    private static class BeforeBuildProcessCommand implements BuildCommand {

    	private int buildKind;
    	
    	public BeforeBuildProcessCommand(int buildKind){
    		this.buildKind = buildKind;
    	}
    	
        public void build(IIpsArtefactBuilder builder, MultiStatus status) throws CoreException {
            builder.beforeBuildProcess(buildKind);
        }
        
        public String toString() {
        	return "BeforeBuildProcessCmd[kind=" + buildKind + "]";
        }
        
    }

    private static class AfterBuildProcessCommand implements BuildCommand {

    	private int buildKind;
    	
    	public AfterBuildProcessCommand(int buildKind){
    		this.buildKind = buildKind;
    	}
    	
        public void build(IIpsArtefactBuilder builder, MultiStatus status) throws CoreException {
            builder.afterBuildProcess(buildKind);
        }
        
        public String toString() {
        	return "AfterBuildProcessCmd[kind=" + buildKind + "]";
        }
    }

    private static class BuildArtefactBuildCommand implements BuildCommand {

        private IIpsSrcFile ipsSrcFile;

        public BuildArtefactBuildCommand(IIpsSrcFile ipsSrcFile) {
            this.ipsSrcFile = ipsSrcFile;
        }

        public void build(IIpsArtefactBuilder builder, MultiStatus status) throws CoreException {
            if (builder.isBuilderFor(ipsSrcFile)) {
                try {
                    builder.beforeBuild(ipsSrcFile, status);
                    builder.build(ipsSrcFile);
                } finally {
                    builder.afterBuild(ipsSrcFile);
                }
            }
        }
        
        public String toString() {
        	return "Build file " + ipsSrcFile; 
        }
    }

    private static class DeleteArtefactBuildCommand implements BuildCommand {

        private IIpsSrcFile toDelete;

        public DeleteArtefactBuildCommand(IIpsSrcFile toDelete) {
            this.toDelete = toDelete;
        }

        public void build(IIpsArtefactBuilder builder, MultiStatus status) throws CoreException {
            if (builder.isBuilderFor(toDelete)) {
                builder.delete(toDelete);
            }
        }
        
        public String toString() {
        	return "Delete file " + toDelete; 
        }
        
    }
}
