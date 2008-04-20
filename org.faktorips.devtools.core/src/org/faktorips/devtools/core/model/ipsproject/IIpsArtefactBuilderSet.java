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

package org.faktorips.devtools.core.model.ipsproject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.internal.model.TableContentsEnumDatatypeAdapter;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.IdentifierResolver;

/**
 * Interface for the extension point org.faktorips.plugin.artefactbuilderset of
 * this plugin. Only one implementation of this interface can be registered for
 * this extension point. If more than one extensions are declared only the first
 * one will be registed and the others will be ignored. An IpsArtefactBuilderSet
 * collects a list of IpsArtefactBuilders and makes them available to the build
 * system.
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilderSet extends IJavaPackageStructure {

	/**
	 * The xml element name.
	 */
	public final static String XML_ELEMENT = "IpsArtefactBuilderSet"; //$NON-NLS-1$

	/**
	 * Returns all IpsArtefactBuilders of this set.
	 * 
	 * @return if the set is empty an empty array has to be returned
	 */
	public IIpsArtefactBuilder[] getArtefactBuilders();

	/**
	 * Returns <code>true</code> if the builder set supports table access
	 * functions, otherwise false.
	 */
	public boolean isSupportTableAccess();

	/**
	 * Returns <code>true</code> if the builder set supports an formula
	 * language identifierResolver.
	 */
	public boolean isSupportFlIdentifierResolver();

    /**
     * Returns <code>true</code> if this artefact builder set requires role names in plural form
     * even for relations with a max cardinality of 1.
     */
    public boolean isRoleNamePluralRequiredForTo1Relations();
    
    /**
     * Returns <code>true</code> if this artefact builder set requires that master-to-detail compositions 
     * contain a reference to an inverse detail-to-master relation. 
     * <p>
     * The standard Faktor-IPS generator doesn't need this link. See the artikel on modeling relations for 
     * further details.
     */
    public boolean isInverseRelationLinkRequiredFor2WayCompositions();

    /**
	 * Returns a compilation result that gives access to a table via the
	 * indicated function. Returns <code>null</code> if this builder set does
	 * not support table access.
	 * 
	 * @param tableContents for table structures that allow multiple contents the table contents
	 * 			is needed to identify for which table contents of a table structure a table
	 * 			access function is called. Can be null for single content table structures 
	 * 
	 * @param fct
	 *            The table access function code should be generated for.
	 * @param argResults
	 *            Compilation Results for the function's arguments.
	 * 
	 * @throws CoreException
	 *             if an error occurs while generating the code.
	 */
	public CompilationResult getTableAccessCode(ITableContents tableContents, ITableAccessFunction fct,
			CompilationResult[] argResults) throws CoreException;

	/**
	 * Creates an<code>IdentifierResolver</code> used to resolve identifiers in the given formula. 
     * Returns <code>null</code> if this builder set doesn't support an formula language identifier resolver.
	 */
	public IdentifierResolver createFlIdentifierResolver(IFormula formula) throws CoreException;
	
	/**
     * Creates an<code>IdentifierResolver</code> used to resolve identifiers in the given
     * formula. The returned identifier resolver has an special handling of type attribute (e.g. an
     * policy cmpt type attribute), instead of using the getter method of the attribute a parameter
     * will be used.
     * 
     * Returns <code>null</code> if this builder set doesn't support an formula language
     * identifier resolver.
     */
	public IdentifierResolver createFlIdentifierResolverForFormulaTest(IFormula formula) throws CoreException;
	
    /**
     * Returns the datatype helper used for the enum type that is defined by a 
     * table contents. 
     * 
     * @throws NullPointerException if structure is <code>null</code>.
     * @throws IllegalArgumentException if the structure does not define an enum type. 
     */
    public DatatypeHelper getDatatypeHelperForTableBasedEnum(TableContentsEnumDatatypeAdapter datatype);

	/**
	 * Returns the file that contain the runtime repository toc file.
	 * Note that the file might not exists.
	 */
	public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) throws CoreException;

    /**
     * Returns the name of the resource containing the root's table of contents at runtime.
     * E.g. "org.faktorips.sample.internal.sample-toc.xml". This returned path can be used
     * to create a ClassloaderRuntimeRepository. Returns <code>null</code> if this builder does
     * not generate tocs or this root is not a root based on a source folder. Returns <code>null</code>
     * if the root is <code>null</code>.
     *
     * @see org.faktorips.runtime.ClassloaderRuntimeRepository#create(String)
     */
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root) throws CoreException;
    
    /**
     * Returns the package name of the generated toc file.<br>
     * Returns <code>null</code> if the builder doesn't create a toc file.
     * 
     * @deprecated use getRuntimeRepositoryTocResourceName(root)
     */
    public String getTocFilePackageName(IIpsPackageFragmentRoot root) throws CoreException;

    /**
	 * When the builder set is loaded by the faktor ips plugin the extension id
	 * is set by means of this method. This method is called before
	 * initialization.
	 * 
	 * @param id
	 *            the extension id
	 */
	public void setId(String id);

	/**
	 * When the builder set is loaded by the faktor ips plugin the extension
	 * describtion label is set by means of this method. This method is called
	 * before initialization.
	 * 
	 * @param id
	 *            the extension description label
	 */
	public void setLabel(String label);

	/**
	 * Returns the extension id declared in the plugin descriptor.
	 */
	public String getId();

	/**
	 * Returns the extension description label declared in the plugin
	 * descriptor.
	 */
	public String getLabel();

    /**
     * This version indicates the version of the generated code. That means changes to the generator that do not
     * change the generated code a not reflected in this version identifier.
     */
    public String getVersion();
    
    /**
     * The framework sets the <code>IIpsLoggingFrameworkConnector</code> that is registered with the IIpsProject to
     * this <code>IIpsArtefactBuilderSet</code> at initialization time. This method is not supposed to be called by
     * clients of this class.
     */
    public void setIpsLoggingFrameworkConnector(IIpsLoggingFrameworkConnector logStmtBuilder);

    /**
     * Returns the <code>IIpsLoggingFrameworkConnector</code> of this <code>IIpsArtefactBuilderSet</code>.
     */
    public IIpsLoggingFrameworkConnector getIpsLoggingFrameworkConnector();
    
	/**
	 * Initializes this set. Creation of IpsArtefactBuilders has to go here
	 * instead of the constructor of the set implementation.
     * 
     * @param config the configuration for this builder set instance. The configuration
     *          for a builder set instance is defined in the .ipsproject file of an
     *          ips project. 
     * @see IIpsArtefactBuilderSetConfig class description
	 */
	public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreException;
    
    /**
     * Subclasses should reimplement this method if an aggregate root builder is contained
     * @return <code>true</code> if this builder set contains an builder which requires to be called for the aggregate root 
     * object if any child (regardless of the relations depth) has been modified. Only composite-relations are allowed for this
     * dependency scan. <code>false</code> if only builder are contained which need only a dependency scan of depth one. 
     */
    public boolean containsAggregateRootBuilder();
    
    /**
     * The IpsProject for which this builder set is registered.
     */
    public void setIpsProject(IIpsProject ipsProject);
    
    /**
     * Returns the IpsProject for which this builder set is registered.
     */
    public IIpsProject getIpsProject();
    
    /**
     * This method is called when the build process starts for this builder set. This method is called before the 
     * <code>beforeBuildProcess(IIpsProject, int)</code> method of the registered IpsArtefactBuilders will be called.
     * 
     * @param buildKind One of the build kinds defined in <code>org.eclipse.core.resources.IncrementalProjectBuilder</code>
     * 
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. Throwing a
     *             CoreException or RuntimeException will interrupt the build cycle
     */
    public void beforeBuildProcess(int buildKind) throws CoreException;

    /**
     * This method is called when the build process is finished for this builder set. This method is called after the 
     * <code>afterBuildProcess(IIpsProject, int)</code> methods on the registered IpsArtefactBuilders were called.
     * 
     * @param buildKind One of the build kinds defined in <code>org.eclipse.core.resources.IncrementalProjectBuilder</code>
     * 
     * @throws CoreException implementations can throw or delegate rising CoreExceptions.
     */
    public void afterBuildProcess(int buildKind) throws CoreException;

}
