/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IFolder;

/**
 * An object path entry that defines a folder containing FaktorIPS source files.
 * 
 * @author Jan Ortmann
 */
public interface IIpsSrcFolderEntry extends IIpsObjectPathEntry {

    /**
     * Message code constant to indicate the source folder must be a direct child of the project
     * and it isn't. 
     */
    public final static String MSGCODE_SRCFOLDER_MUST_BE_A_DIRECT_CHILD_OF_THE_PROHECT = "SourceFolder must be a direct child of the project."; //$NON-NLS-1$

    /**
     * Message code constant identifying the message of a validation rule. 
     */
    public final static String MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING  = "OutputFolderMergableMissing"; //$NON-NLS-1$

    /**
     * Message code constant identifying the message of a validation rule. 
     */
    public final static String MSGCODE_OUTPUT_FOLDER_DERIVED_MISSING  = "OutputFolderDerivedMissing"; //$NON-NLS-1$

    /**
     * Returns the folder containing the ips source files.
     */
    public IFolder getSourceFolder();
    
    /**
     * Returns the output folder for generated but mergable Java source files. The content of the
     * java files in this folder are supposed to be merged with the newly generated content during
     * every build cycle . If a specific output folder is set for this entry, the specific output
     * folder is returned, otherwise the ouput folder defined in the object path is returned.
     */
    public IFolder getOutputFolderForMergableJavaFiles();
    
    /**
     * Returns the entry's own output folder for generated but mergable Java source files. This
     * ouptput folder is used only for this entry.
     */
    public IFolder getSpecificOutputFolderForMergableJavaFiles();
    
    /**
     * Sets the entry's output folder for generated but mergable Java source files.
     */
    public void setSpecificOutputFolderForMergableJavaFiles(IFolder outputFolder);
    
    /**
     * Returns the name of the base package for generated but mergable Java source files. If a
     * specific base package name is set for this entry, the specific base package name is returned,
     * otherwise the base package name defined in the object path is returned.
     */
    public String getBasePackageNameForMergableJavaClasses();

    /**
     * Returns partial toc resource name. The fully qualified toc resource name is obtained
     * by adding this partial name to the base package name for generated Java classes.
     * 
     * @see #getTocPath()
     * @see #getBasePackageNameForMergableJavaClasses()
     */
    public String getBasePackageRelativeTocPath();
    
    /**
     * Sets the partial toc resource name. 
     *
     * @see #getBasePackageRelativeTocPath()
     */
    public void setBasePackageRelativeTocPath(String newName);

    /**
     * Returns the name of the entry's own base package for generated but mergable Java source
     * files.
     */
    public String getSpecificBasePackageNameForMergableJavaClasses();

    /**
     * Sets the name of entry's own base package for generated but mergable Java source files.
     */
    public void setSpecificBasePackageNameForMergableJavaClasses(String name);
    
    /**
     * Returns the output folder containting generated derived Java source files. If a specific
     * output folder is set for this entry, the specific output folder is returned, otherwise the
     * ouput folder defined in the object path is returned.
     */
    public IFolder getOutputFolderForDerivedJavaFiles();
    
    /**
     * Returns the entry's own output folder containing generated derived Java source files.
     * This ouptput folder is used only for this entry.
     */
    public IFolder getSpecificOutputFolderForDerivedJavaFiles();
    
    /**
     * Sets the entry's output folder containg generated derived Java source files.
     * This ouptput folder is used only for this entry.
     */
    public void setSpecificOutputFolderForDerivedJavaFiles(IFolder outputFolder);
    
    /**
     * Returns the name of the base package for the generated derived Java source files. If a
     * specific base package name is set for this entry, the specific base package name is returned,
     * otherwise the base package name defined in the object path is returned.
     */
    public String getBasePackageNameForDerivedJavaClasses();
    
    /**
     * Returns the name of the entry's own base package for generated derived Java source files. All
     * generated Java types are contained in this package or one of the child packages.
     */
    public String getSpecificBasePackageNameForDerivedJavaClasses();

    /**
     * Sets the name of the entry's own base package for the generated derived Java source files. 
     * All generated Java types are contained in this package or one of the child packages.
     */
    public void setSpecificBasePackageNameForDerivedJavaClasses(String name);
    
}
