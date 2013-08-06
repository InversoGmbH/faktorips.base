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

package org.faktorips.devtools.core.internal.model.ipsproject.bundle;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * The {@link IpsJarBundleContentIndex} reads the list of entries of a {@link JarFile} and caches
 * some information about the qualified names and folders.
 * 
 * @author dicker
 */
public class IpsJarBundleContentIndex extends AbstractIpsBundleContentIndex {

    /**
     * Create an {@link IpsJarBundleContentIndex} reading from the specified {@link JarFile}. The
     * JarFile should be ready to read and it will be closed after the object was constructed.
     * <p>
     * Every file located in any of the given model folders is registered in the index. The files
     * are indexed relative to the model folder.
     * 
     * @param jarFile The {@link JarFile} that should be read and indexed
     * @param modelFolders The list of model folders, the paths are relativ to the root of the jar
     *            file
     */
    public IpsJarBundleContentIndex(JarFile jarFile, List<IPath> modelFolders) {
        try {
            Assert.isNotNull(jarFile, "jarFile must not be null"); //$NON-NLS-1$
            Assert.isNotNull(modelFolders, "modelFolders must not be null"); //$NON-NLS-1$

            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();

                registerJarEntry(jarEntry, modelFolders);
            }
        } finally {
            try {
                jarFile.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing jar file " + jarFile.getName(), e); //$NON-NLS-1$
            }
        }
    }

    private final void registerJarEntry(JarEntry jarEntry, List<IPath> modelFolders) {
        String pathToFile = jarEntry.getName();

        IPath path = new Path(pathToFile);

        registerPath(path, modelFolders);
    }

    protected final void registerPath(IPath path, List<IPath> modelFolders) {
        for (IPath modelPath : modelFolders) {
            if (modelPath.isPrefixOf(path)) {

                IPath relativePath = path.makeRelativeTo(modelPath);

                registerPath(modelPath, relativePath);
                return;
            }
        }
    }
}