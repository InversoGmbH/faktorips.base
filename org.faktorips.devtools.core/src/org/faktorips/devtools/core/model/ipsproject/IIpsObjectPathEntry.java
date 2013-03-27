/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsproject;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.util.message.MessageList;

/**
 * An entry in an IPS object path.
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectPathEntry {

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "IIPSOBJECTPATHENTRY-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a related folder is missing.
     */
    public static final String MSGCODE_MISSING_FOLDER = MSGCODE_PREFIX + "MissingFolder"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the related project is missing.
     */
    public static final String MSGCODE_PROJECT_NOT_SPECIFIED = MSGCODE_PREFIX + "ProjectNotSpecified"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the related project is missing.
     */
    public static final String MSGCODE_MISSING_PROJECT = MSGCODE_PREFIX + "MissingProject"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a related archive is missing.
     */
    public static final String MSGCODE_MISSING_ARCHVE = MSGCODE_PREFIX + "MissingArchive"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a related jar bundle is missing.
     */
    public static final String MSGCODE_MISSING_JARBUNDLE = MSGCODE_PREFIX + "MissingJarBundle"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a related archive is invalid.
     */
    public static final String MSGCODE_INVALID_ARCHVE = MSGCODE_PREFIX + "InvalidArchive"; //$NON-NLS-1$

    /**
     * Type constant indicating a source folder entry.
     */
    public static final String TYPE_SRC_FOLDER = "src"; //$NON-NLS-1$

    /**
     * Type constant indicating a project reference entry.
     */
    public static final String TYPE_PROJECT_REFERENCE = "project"; //$NON-NLS-1$

    /**
     * Type constant indicating a archive (library) containing the model files.
     */
    public static final String TYPE_ARCHIVE = "archive"; //$NON-NLS-1$

    /**
     * Type constant indicating a container entry.
     */
    public static final String TYPE_CONTAINER = "container"; //$NON-NLS-1$

    /**
     * Type constant indicating a jar bundle entry.
     * 
     */
    public static final String TYPE_JARBUNDLE = "jarbundle"; //$NON-NLS-1$

    /**
     * Returns the object path this is an entry of.
     */
    public IIpsObjectPath getIpsObjectPath();

    /**
     * Returns the ips project this project belongs to.
     */
    public IIpsProject getIpsProject();

    /**
     * Returns the type of this entry as one of the type constant defined in this interface.
     * 
     * @see #TYPE_SRC_FOLDER
     * @see #TYPE_ARCHIVE
     * @see #TYPE_PROJECT_REFERENCE
     * @see #TYPE_CONTAINER
     */
    public String getType();

    /**
     * Returns <code>true</code> if this is a container entry that can be resolved to "real"
     * entries.
     */
    public boolean isContainer();

    /**
     * Returns the (zero based) index of this entry in the path.
     */
    public int getIndex();

    /**
     * Returns the name of the ips package fragment root this entry defines or <code>null</code> if
     * this is a project reference entry.
     */
    public abstract String getIpsPackageFragmentRootName();

    /**
     * Returns the package fragment root this entry defines or <code>null</code> if this is a
     * project reference entry. Note that is not guaranteed that the returned package fragment root
     * exists.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot();

    /**
     * Validates the object path entry and returns the result as list of messages.
     */
    public MessageList validate() throws CoreException;

    /**
     * Returns the IPS object with the indicated type and qualified name.
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException;

    /**
     * Returns the IPS source file with the indicated qualified name type.
     */
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType) throws CoreException;

    /**
     * Returns an {@link InputStream} that provides a resource's/file's contents. The given path is
     * interpreted as a relative path in respect to the path-entry's resource.
     * <p>
     * Callers of this method are responsible for closing the stream after use.
     */
    public InputStream getRessourceAsStream(String path) throws CoreException;

}
