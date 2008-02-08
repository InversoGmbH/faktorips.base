/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsproject;

import java.io.InputStream;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;

/**
 * An ips archive is an archive for ips objects. It is physically stored in a file. The file's format is jar.
 * 
 * @author Jan Ortmann
 */
public interface IIpsArchive {

    /**
     * Constant for the top-level folder in the archive file that contains the entries for the ips 
     * objects. 
     */
    public final static String IPSOBJECTS_FOLDER = "ipsobjects"; //$NON-NLS-1$
    
    /**
     * Constant for the jar entry name" that contains additional ipsobjects properties like the mapping to Java base packages.
     */
    public final static String JAVA_MAPPING_ENTRY_NAME = IPSOBJECTS_FOLDER + IPath.SEPARATOR + "ipsobjects.properties"; //$NON-NLS-1$

    public final static String QNT_PROPERTY_POSTFIX_SEPARATOR = "#"; //$NON-NLS-1$
    
    public final static String PROPERTY_POSTFIX_BASE_PACKAGE = "basePackage"; //$NON-NLS-1$
    
    public final static String PROPERTY_POSTFIX_EXTENSION_PACKAGE = "extensionPackage"; //$NON-NLS-1$

    /**
     * Returns the underlying file. 
     */
    public IFile getArchiveFile();

    /**
     * Returns <code>true</code> if the archive exists, otherwise <code>false</code>.
     */
    public boolean exists();
    
    /**
     * Returns the name of the non-empty packages contained in the archive. A package is not empty,
     * if it contains at least one ips object.
     */
    public String[] getNoneEmptyPackages() throws CoreException;

    /**
     * Returns <code>true</code> if the archive contains the package (empty or not),
     * otherwise <code>false</code>.
     */
    public boolean containsPackage(String name) throws CoreException;

    /**
     * Returns the names of the non-empty subpackages for the given parent package as list.
     */
    public Set getNoneEmptySubpackages(String pack) throws CoreException;
    // TODO Stefan auf String[] aendern, subpackges sind alphabetisch sortiert 

    /**
     * Returns the set of qualified name types for the ips objects stored in the archive
     */
    public Set getQNameTypes() throws CoreException;
    
    /**
     * Returns the set of qualified name types for the ips objects stored in the given package.
     * Returns an empty set if the archive does not contain an object for the given package or
     * packName is <code>null</code>.
     */
    public Set getQNameTypes(String packName) throws CoreException;
    
    /**
     * Returns <code>true</code> if the archive contains the ips object identified by the given
     * qualified name type, otherwise <code>false</code>.
     */
    public boolean contains(QualifiedNameType qnt) throws CoreException;
    
    /**
     * Returns the content for the qualified name type or <code>null</code> if the archive
     * does not contain the given qualified name type. Returns <code>null</code> if qnt is <code>null</code>. 
     */
    public InputStream getContent(QualifiedNameType qnt) throws CoreException;    

    public InputStream getSortDefinitionContent(String packName) throws CoreException;
    
    /**
     * Returns the name of the base package for the generated Java source files.
     * All generated Java types are contained in this package or one of the child packages.
     */
    public String getBasePackageNameForGeneratedJavaClass(QualifiedNameType qnt) throws CoreException;

    /**
     * Returns the name of the base package for the extension Java source files. All generated Java types
     * are contained in this package or one of the child packages.
     * Extension Java files are the files where the developer adds his own code.
     */
    public String getBasePackageNameForExtensionJavaClass(QualifiedNameType qnt) throws CoreException;
    
}