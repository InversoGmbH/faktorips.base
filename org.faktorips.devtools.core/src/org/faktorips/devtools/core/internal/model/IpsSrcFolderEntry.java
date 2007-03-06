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

package org.faktorips.devtools.core.internal.model;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsSrcFolderEntry.
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFolderEntry extends IpsObjectPathEntry implements IIpsSrcFolderEntry {

    private final static String DEFAULT_TOC_PATH = ClassloaderRuntimeRepository.TABLE_OF_CONTENTS_FILE;
    
    /**
     * Returns a description of the xml format.
     */
    public final static String getXmlFormatDescription() {
        return "Sourcefolder:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "  <" + XML_ELEMENT + SystemUtils.LINE_SEPARATOR  //$NON-NLS-1$
             + "    type=\"src\"" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "    sourceFolder=\"model\"            Folder in the project that contains the FaktorIPS model and product definition files." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "    outputFolderMergable=\"src\"      Folder in the project where the generator puts the java source files which content will be merged with " + //$NON-NLS-1$
                                                        "the newly generated content during each build cycle." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "    basePackageMergable=\"org.foo\"   The package prefix for all generated but mergable java files." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "    tocPath=\"motor/motor-reposiory-toc.xml\" " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "                                      The partial path of the resource containing the runtime repository table of content (toc)." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "                                      The full path is derived from the basePackageMergeable by adding this partial path." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "                                      The file is not part of the published interface so it is places in the internal package." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "    outputFolderDerived=\"\"          Folder within the project where the generator puts java source files that will be overridden during each build cycle and delete and " + //$NON-NLS-1$
                                                        "regenerated during a clean build cycle." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "                                      Other builders can choose to maintain user code in a separate folder which is defined here." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + "                                      If you use the standard builder, leave the atribute empty." + SystemUtils.LINE_SEPARATOR  //$NON-NLS-1$
             + "    basePackageDerived=\"\">          Package prefix for all generated derived Java classes in the output folder for derived sources. See above." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
             + " </" + XML_ELEMENT + ">" + SystemUtils.LINE_SEPARATOR;  //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    // the folder containg the ips objects
    private IFolder sourceFolder;

    // the output folder containing the generated but mergable Java files.
    private IFolder outputFolderMergable;

    // the name of the base package containing the generated but mergable Java files.
    private String basePackageMergable = ""; //$NON-NLS-1$

    private String tocPath = DEFAULT_TOC_PATH;
    
    // the output folder containing the Java files that are generated and derived.
    private IFolder outputFolderDerived;

    // the name of the base package containing the Java files where the developer adds it's own code.
    private String basePackageDerived = ""; //$NON-NLS-1$

    public IpsSrcFolderEntry(IpsObjectPath path) {
        super(path);
    }

    public IpsSrcFolderEntry(IpsObjectPath path, IFolder sourceFolder) {
        super(path);
        ArgumentCheck.notNull(sourceFolder);
        this.sourceFolder = sourceFolder;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return TYPE_SRC_FOLDER;
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getSourceFolder() {
        return sourceFolder;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(IIpsProject ipsProject) {
        return ipsProject.getIpsPackageFragmentRoot(sourceFolder.getName());
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getOutputFolderForMergableJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderMergable;
        }
        return getIpsObjectPath().getOutputFolderForGeneratedJavaFiles();
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getSpecificOutputFolderForMergableJavaFiles() {
        return outputFolderMergable;
    }

    /**
     * {@inheritDoc}
     */
    public void setSpecificOutputFolderForMergableJavaFiles(IFolder outputFolder) {
        this.outputFolderMergable = outputFolder;
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForMergableJavaClasses() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return basePackageMergable;
        }
        return getIpsObjectPath().getBasePackageNameForMergableJavaClasses();
    }

    /**
     * {@inheritDoc}
     */
    public String getSpecificBasePackageNameForMergableJavaClasses() {
        return basePackageMergable;
    }

    /**
     * {@inheritDoc}
     */
    public void setSpecificBasePackageNameForMergableJavaClasses(String name) {
        this.basePackageMergable = name;
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getOutputFolderForDerivedJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderDerived;
        }
        return getIpsObjectPath().getOutputFolderForDerivedSources();
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getSpecificOutputFolderForDerivedJavaFiles() {
        return outputFolderDerived;
    }

    /**
     * {@inheritDoc}
     */
    public void setSpecificOutputFolderForDerivedJavaFiles(IFolder outputFolder) {
        outputFolderDerived = outputFolder;
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageNameForDerivedJavaClasses() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return basePackageDerived;
        }
        return getIpsObjectPath().getBasePackageNameForDerivedJavaClasses();
    }

    /**
     * {@inheritDoc}
     */
    public String getSpecificBasePackageNameForDerivedJavaClasses() {
        return basePackageDerived;
    }

    /**
     * {@inheritDoc}
     */
    public void setSpecificBasePackageNameForDerivedJavaClasses(String name) {
        basePackageDerived = name;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObject findIpsObjectInternal(IIpsProject ipsProject, QualifiedNameType nameType, Set visitedEntries) throws CoreException {
        return getIpsPackageFragmentRoot(ipsProject).findIpsObject(nameType);
    }

    /**
     * {@inheritDoc}
     */
    public void findIpsObjectsInternal(IIpsProject ipsProject, IpsObjectType type, List result, Set visitedEntries) throws CoreException {
        ((IpsPackageFragmentRoot)getIpsPackageFragmentRoot(ipsProject)).findIpsObjects(type, result);
    }

    /**
     * {@inheritDoc}
     */
    protected void findIpsObjectsStartingWithInternal(IIpsProject ipsProject, IpsObjectType type, String prefix, boolean ignoreCase, List result, Set visitedEntries)
            throws CoreException {
        ((IpsPackageFragmentRoot)getIpsPackageFragmentRoot(ipsProject)).findIpsObjectsStartingWith(type, prefix, ignoreCase,
                result);
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(Element element, IProject project) {
        String sourceFolderPath = element.getAttribute("sourceFolder"); //$NON-NLS-1$
        sourceFolder = project.getFolder(new Path(sourceFolderPath));
        String outputFolderPathMergable = element.getAttribute("outputFolderMergable"); //$NON-NLS-1$
        outputFolderMergable = outputFolderPathMergable.equals("") ? null : project.getFolder(new Path( //$NON-NLS-1$
                outputFolderPathMergable));
        basePackageMergable = element.getAttribute("basePackageMergable"); //$NON-NLS-1$
        tocPath = element.getAttribute("tocPath"); //$NON-NLS-1$
        if (StringUtils.isEmpty(tocPath)) {
            tocPath = DEFAULT_TOC_PATH;
        }
        String outputFolderPathDerived = element.getAttribute("outputFolderDerived"); //$NON-NLS-1$
        outputFolderDerived = outputFolderPathDerived.equals("") ? null : project.getFolder(new Path( //$NON-NLS-1$
                outputFolderPathDerived));
        basePackageDerived = element.getAttribute("basePackageDerived"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(IpsObjectPathEntry.XML_ELEMENT);
        element.setAttribute("type", TYPE_SRC_FOLDER); //$NON-NLS-1$
        element.setAttribute("sourceFolder", sourceFolder.getProjectRelativePath().toString()); //$NON-NLS-1$
        element.setAttribute("outputFolderMergable", outputFolderMergable == null ? "" : outputFolderMergable //$NON-NLS-1$ //$NON-NLS-2$
                .getProjectRelativePath().toString());
        element.setAttribute("basePackageMergable", basePackageMergable==null ? "" : basePackageMergable); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("tocPath", tocPath==null ? "" : tocPath); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("outputFolderDerived", outputFolderDerived == null ? "" : outputFolderDerived //$NON-NLS-1$ //$NON-NLS-2$
                .getProjectRelativePath().toString());
        element.setAttribute("basePackageDerived", basePackageDerived==null ? "" : basePackageDerived); //$NON-NLS-1$ //$NON-NLS-2$
        return element;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validate() throws CoreException {
        MessageList result = new MessageList();
        // the sourceFolder will never be null (see this#initFromXml)
        result.add(validateIfFolderExists(sourceFolder));
        if (sourceFolder.getProjectRelativePath().segmentCount()>1){
            String text = NLS.bind(Messages.IpsSrcFolderEntry_srcFolderMustBeADirectChildOfTheProject, sourceFolder.getProjectRelativePath().toString());
            Message msg = new Message(MSGCODE_SRCFOLDER_MUST_BE_A_DIRECT_CHILD_OF_THE_PROHECT, text, Message.ERROR, this);
            result.add(msg);
        }

        if(outputFolderMergable == null && getIpsObjectPath().getOutputFolderForGeneratedJavaFiles() == null){
            result.add(new Message(MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING, Messages.IpsSrcFolderEntry_outputfoldermergablesrcmissing, Message.ERROR)); //$NON-NLS-1$
        }
        if(outputFolderDerived == null && getIpsObjectPath().getOutputFolderForDerivedSources() == null){
            result.add(new Message(MSGCODE_OUTPUT_FOLDER_DERIVED_MISSING, Messages.IpsSrcFolderEntry_outputfoldersrcderivedmissing, Message.ERROR)); //$NON-NLS-1$
        }
        
        if (outputFolderMergable != null) {
            result.add(validateIfFolderExists(outputFolderMergable));
        }
        if (outputFolderDerived != null) {
            result.add(validateIfFolderExists(outputFolderDerived));
        }
        return result;
    }
    
    /*
     * Validate that the given folder exists.
     */
    private MessageList validateIfFolderExists(IFolder folder) {
        MessageList result = new MessageList();
        if (!folder.exists()){
            String text = NLS.bind(Messages.IpsSrcFolderEntry_msgMissingFolder, folder.getName());
            Message msg = new Message(MSGCODE_MISSING_FOLDER, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "SrcFolderEntry[" + this.sourceFolder.getProjectRelativePath().toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    public void setBasePackageRelativeTocPath(String newPath) {
        tocPath = newPath;
    }

    /**
     * {@inheritDoc}
     */
    public String getBasePackageRelativeTocPath() {
        return tocPath;
    }
 
    /**
     * {@inheritDoc}
     */
    public String getFullTocPath() {
        String path = QNameUtil.toPath(getBasePackageNameForMergableJavaClasses()).toString();
        if (StringUtils.isEmpty(path)) {
            return tocPath.toString();
        }
        return path + IPath.SEPARATOR + tocPath;
    }
    
}