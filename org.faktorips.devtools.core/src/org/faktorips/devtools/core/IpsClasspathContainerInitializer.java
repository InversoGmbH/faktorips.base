/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

public class IpsClasspathContainerInitializer extends ClasspathContainerInitializer {

    public static final String CONTAINER_ID = "org.faktorips.devtools.core.ipsClasspathContainer"; //$NON-NLS-1$

    public static final IPath ENTRY_PATH = new Path(CONTAINER_ID);

    private static final String NAME_VERSION_SEP = "_"; //$NON-NLS-1$

    public static final String RUNTIME_BUNDLE = "org.faktorips.runtime.java5"; //$NON-NLS-1$

    public static final String VALUETYPES_BUNDLE = "org.faktorips.valuetypes.java5"; //$NON-NLS-1$

    public IpsClasspathContainerInitializer() {
        // empty constructor
    }

    @Override
    public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
        IClasspathContainer[] respectiveContainers = new IClasspathContainer[] { new IpsClasspathContainer(
                containerPath) };
        JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, respectiveContainers, null);

    }

    class IpsClasspathContainer implements IClasspathContainer {

        private final IPath containerPath;
        private IClasspathEntry[] entries;

        public IpsClasspathContainer(IPath containerPath) {
            this.containerPath = containerPath;
            ArrayList<IClasspathEntry> entryList = new ArrayList<IClasspathEntry>();

            IClasspathEntry runtime = JavaCore.newLibraryEntry(getBundlePath(RUNTIME_BUNDLE, false),
                    getBundlePath(RUNTIME_BUNDLE, true), null);
            IClasspathEntry valuetypes = JavaCore.newLibraryEntry(getBundlePath(VALUETYPES_BUNDLE, false),
                    getBundlePath(VALUETYPES_BUNDLE, true), null);

            entryList.add(runtime);
            entryList.add(valuetypes);

            if (containerPath.segmentCount() == 2 && !containerPath.lastSegment().isEmpty()) {
                String lastSegment = containerPath.lastSegment();
                String[] addEntries = lastSegment.split(","); //$NON-NLS-1$
                for (String additionalEntry : addEntries) {
                    IClasspathEntry addEntry = JavaCore.newLibraryEntry(getBundlePath(additionalEntry, false),
                            getBundlePath(additionalEntry, true), null);
                    entryList.add(addEntry);
                }
            }
            entries = entryList.toArray(new IClasspathEntry[entryList.size()]);
        }

        @Override
        public IClasspathEntry[] getClasspathEntries() {
            return entries;
        }

        @Override
        public String getDescription() {
            return Messages.IpsClasspathContainerInitializer_containerDescription;
        }

        @Override
        public int getKind() {
            return K_APPLICATION;
        }

        @Override
        public IPath getPath() {
            return containerPath;
        }

        private IPath getBundlePath(String pluginId, boolean sources) {
            Bundle bundle = Platform.getBundle(pluginId);
            if (bundle == null) {
                IpsPlugin
                        .log(new IpsStatus(
                                "Error initializing " + (sources ? "source for " : "") + "classpath container. Bundle " + pluginId + " not found.")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                return null;
            }

            URL installLocation;
            if (sources) {
                try {
                    installLocation = bundle.getEntry("src"); //$NON-NLS-1$ 
                    String fullPath;
                    if (installLocation != null) {
                        URL local = FileLocator.toFileURL(installLocation);
                        fullPath = new File(local.getPath()).getAbsolutePath();
                    } else {
                        fullPath = FileLocator.getBundleFile(bundle).getAbsolutePath();
                        String[] split = fullPath.split(NAME_VERSION_SEP);
                        if (split.length < 2) {
                            return null;
                        }
                        split[split.length - 2] = split[split.length - 2] + ".source"; //$NON-NLS-1$
                        fullPath = StringUtils.EMPTY;
                        for (String string : split) {
                            if (string != split[split.length - 1]) {
                                fullPath += string + NAME_VERSION_SEP;
                            } else {
                                fullPath += string;
                            }
                        }
                    }
                    return Path.fromOSString(fullPath);
                } catch (Exception e) {
                    IpsPlugin.log(new IpsStatus(
                            "Error initializing classpath container for source bundle " + pluginId, e)); //$NON-NLS-1$ 
                    return null;
                }
            } else {
                try {
                    installLocation = bundle.getEntry("bin"); //$NON-NLS-1$ 
                    String fullPath;
                    if (installLocation != null) {
                        URL local = FileLocator.toFileURL(installLocation);
                        fullPath = new File(local.getPath()).getAbsolutePath();
                    } else {
                        fullPath = FileLocator.getBundleFile(bundle).getAbsolutePath();
                    }
                    return Path.fromOSString(fullPath);
                } catch (Exception e) {
                    IpsPlugin.log(new IpsStatus("Error initializing classpath container for bundle " + pluginId, e)); //$NON-NLS-1$ 
                    return null;
                }
            }
        }

    }

}
