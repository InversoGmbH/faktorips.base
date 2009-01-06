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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.TimedIpsObject;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;

public class DeepCopyOperation implements IWorkspaceRunnable{

	private IProductCmptStructureReference[] toCopy;
	private IProductCmptStructureReference[] toRefer;
	private Map handleMap;
	private IProductCmpt copiedRoot;
    private boolean createEmptyTableContents = false;
	
	/**
	 * Creates a new operation to copy the given product components.
	 * 
	 * @param toCopy All product components and table contents that should be copied.
	 * @param toRefer All product components and table contents which should be referred from the copied ones.
	 * @param handleMap All <code>IIpsSrcFiles</code> (which are all handles to non-existing resources!). Keys are the
	 * nodes given in <code>toCopy</code>.
	 */
	public DeepCopyOperation(IProductCmptStructureReference[] toCopy, IProductCmptStructureReference[] toRefer, Map handleMap) {
		this.toCopy = toCopy;
		this.toRefer = toRefer;
		this.handleMap = handleMap;
	}

	/**
     * If <code>true</code> table contents will be created as empty files, otherwise the table
     * contents will be copied.
     */
    public void setCreateEmptyTableContents(boolean createEmptyTableContents) {
        this.createEmptyTableContents = createEmptyTableContents;
    }

    /**
	 * {@inheritDoc}
	 * FIXME Joerg Testcases
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(Messages.DeepCopyOperation_taskTitle, 2 + toCopy.length*2 + toRefer.length);
		
		monitor.worked(1);

		// stores all objects which refers old targets instead of changing to the new one
		Set objectsToRefer = collectObjectsToRefer();
		
		// maps used to fix the targets (table usages or links) on the new productCmpt
		HashMap tblContentData2newTableContentQName = new HashMap();
		HashMap linkData2newProductCmptQName = new HashMap();
		
		Hashtable productNew2ProductOld = new Hashtable();
		List newIpsObjects = new ArrayList();
		
		for (int i = 0; i < toCopy.length; i++) {
            IIpsObject newIpsObject = createNewIpsObjectIfNecessary(toCopy[i], productNew2ProductOld, monitor);
            newIpsObjects.add(newIpsObject);
            
			// stores the link or tableUsage which indicates the creation of the new object
			// will be used later to fix the new link target or new table contents if necessary
			if (toCopy[i] instanceof IProductCmptReference){
			    IProductCmptReference productCmptReference = (IProductCmptReference)toCopy[i];
			    storeLinkToNewNewProductCmpt(productCmptReference, newIpsObject.getQualifiedName(), linkData2newProductCmptQName);
			} else if (toCopy[i] instanceof IProductCmptStructureTblUsageReference){
			    IProductCmptStructureTblUsageReference productCmptStructureTblUsageReference = (IProductCmptStructureTblUsageReference)toCopy[i];
			    storeTableUsageToNewTableContents(productCmptStructureTblUsageReference, newIpsObject.getQualifiedName(), tblContentData2newTableContentQName);
			}

			monitor.worked(1);
		}

		// fix links, on of the following options: 
		//   a) change target to new (copied) productCmpt's: if the target should also be copied -> checked on the 1st wizard page and checked on the 2nd page
		//   b) leave old target: if the target shouldn't be changed -> check on 1st wizard page and unchecked on the 2nd page
		//   c) delete link: if target are not copied -> not check on 1st wizard page
		// fix tableContentUsages, on of the following options:
		//   a) change table usage to the new (copied) tableContents -> see a) above
		//   b) leave the table usage to the old tableContents -> see b) above
		//   C) clear table usage (leave empty) -> see c) above
		for (Iterator iterator = productNew2ProductOld.keySet().iterator(); iterator.hasNext();) {
		    IIpsObject ipsObject = (IIpsObject)iterator.next();
		    if (!(ipsObject instanceof IProductCmpt)){
		        continue;
		    }
		    IProductCmpt productCmptNew = (IProductCmpt)ipsObject.getIpsObject();
		    IProductCmpt productCmptTemplate = (IProductCmpt)productNew2ProductOld.get(productCmptNew);
		    
		    fixRelationsToTableContents(productCmptNew, productCmptTemplate, tblContentData2newTableContentQName, objectsToRefer);
		    fixRelationsToProductCmpt(productCmptNew, productCmptTemplate, linkData2newProductCmptQName, objectsToRefer);
        }

		// save all ipsSource files
		for (Iterator iterator = newIpsObjects.iterator(); iterator.hasNext();) {
		    IIpsSrcFile ipsSrcFile = ((IIpsObject)iterator.next()).getIpsSrcFile();
		    ipsSrcFile.save(true, monitor);
		    monitor.worked(1);
        }
		
		if (newIpsObjects.size() == 0){
		    throw new RuntimeException("No copied root found!"); //$NON-NLS-1$
		}
		copiedRoot = (IProductCmpt)newIpsObjects.get(0);
		monitor.done();
	}

    private IIpsObject createNewIpsObjectIfNecessary(final IProductCmptStructureReference toCopyProductCmptStructureReference,
            Hashtable productNew2ProductOld,
            IProgressMonitor monitor) throws CoreException {
        IIpsObject templateObject = toCopyProductCmptStructureReference.getWrappedIpsObject();
        IIpsSrcFile file = (IIpsSrcFile)handleMap.get(toCopyProductCmptStructureReference);
        
        // if the file already exists, we can do nothing because the file was created already
        // caused by another reference to the same product component.
        if (!file.exists()) {
            GregorianCalendar date = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        	IIpsPackageFragment targetPackage = createTargetPackage(file, monitor);
        	String newName = file.getName().substring(0, file.getName().lastIndexOf('.'));
        	
        	boolean createEmptyFile = false;
            
        	if (createEmptyTableContents && IpsObjectType.TABLE_CONTENTS.equals(templateObject.getIpsObjectType())){
        	    createEmptyFile = true;
        	}
        	
        	if (!createEmptyFile){
        		// try to create the file as copy
        	    try {
        			file = targetPackage.createIpsFileFromTemplate(newName, templateObject, date, false, monitor);
        		} catch (CoreException e) {
        		    // exception occurred thus create empty file below
        		    createEmptyFile = true;
        		}
        	}
        	
        	if (createEmptyFile) {
        	    // if table contents should be created empty or
        	    // or if the file could not be created from template then create an empty file
        		file = targetPackage.createIpsFile(templateObject.getIpsObjectType(), newName, false, monitor);
        		TimedIpsObject ipsObject = (TimedIpsObject)file.getIpsObject();
        		IIpsObjectGeneration generation = (IIpsObjectGeneration)ipsObject.newGeneration();
        		generation.setValidFrom(date);
        		setPropertiesFromTemplate(templateObject, ipsObject);
        	}
        }
        
        IIpsObject newIpsObject = (IIpsObject)file.getIpsObject();
        if (newIpsObject instanceof IProductCmpt){
            productNew2ProductOld.put(newIpsObject, templateObject);
        }
        return newIpsObject;
    }

    private void storeTableUsageToNewTableContents(IProductCmptStructureTblUsageReference productCmptStructureReference,
            String newTableContentsQName,
            HashMap tblContentData2newTableContentQName) {
        ITableContentUsage tblContentUsageOld = productCmptStructureReference.getTableContentUsage();
        tblContentData2newTableContentQName.put(new TblContentUsageData(tblContentUsageOld), newTableContentsQName);
    }

    private void storeLinkToNewNewProductCmpt(IProductCmptReference productCmptStructureReference,
            String newProductCmptQName,
            HashMap linkData2newProductCmptQName) {
        IProductCmptStructureReference parent = productCmptStructureReference.getParent();
        IProductCmpt productCmpt = ((IProductCmptReference)productCmptStructureReference).getProductCmpt();
        if (parent instanceof IProductCmptTypeRelationReference){
            IProductCmptTypeAssociation productCmptTypeAssociationOld = ((IProductCmptTypeRelationReference)parent).getRelation();
            IProductCmpt parentProductCmpt = ((IProductCmptReference)((IProductCmptTypeRelationReference)parent).getParent()).getProductCmpt();
            linkData2newProductCmptQName.put(new LinkData(parentProductCmpt, productCmpt, productCmptTypeAssociationOld), newProductCmptQName);
        }
    }
	
	private Set collectObjectsToRefer() {
        Set tblContentUsageAndLinkDataRefer = new HashSet();
        List toReferList = Arrays.asList(toRefer);
        for (int i = 0; i < toRefer.length; i++) {
            if (toRefer[i] instanceof IProductCmptStructureTblUsageReference){
                final IProductCmptStructureTblUsageReference productCmptStructureTblUsageReference = (IProductCmptStructureTblUsageReference)toRefer[i];
                IProductCmptStructureReference parent = productCmptStructureTblUsageReference.getParent();
                if (toReferList.contains(parent)){
                    // the tableContents should be linked, check if the productCmpt for this tableContentUsage is also a link
                    // and if true, don't store this table because the parent productCmpt must not be fixed
                    continue;
                }
                tblContentUsageAndLinkDataRefer.add(new TblContentUsageData((productCmptStructureTblUsageReference).getTableContentUsage()));
            } else if (toRefer[i] instanceof IProductCmptStructureReference){
                // root elements couldn't be linked thus getParent returns the type association
                // and the getParent of the type association returns the parent product cmpt of
                // the to be linked object
                IProductCmptStructureReference productCmptStructureReference = (IProductCmptStructureReference)toRefer[i];
                IProductCmptTypeRelationReference parentTypeRel = (IProductCmptTypeRelationReference)productCmptStructureReference.getParent();
                IProductCmptStructureReference parent = parentTypeRel.getParent();
                tblContentUsageAndLinkDataRefer.add(new LinkData((IProductCmpt)parent.getWrappedIpsObject(), (IProductCmpt)productCmptStructureReference.getWrappedIpsObject(), parentTypeRel.getRelation()));
            }
        }
        return tblContentUsageAndLinkDataRefer;
    }

    /*
     * Fix all tableContentUsages
     * 
     * @param productCmptNew the new productCmpt which will be fixed
     * @param productCmptTemplate the template (old) productCmpt which was used to copy the new
     *            productCmpt
     * @param tblContentData2newTableContentQName A map containing the tableContentUsage-identifier
     *            as key and the new created tableContent which was initiated by the key tableContentUsage
     * @param objectsToRefer A set containing the not copied objects
     */
    private void fixRelationsToTableContents(IProductCmpt productCmptNew,
            IProductCmpt productCmptTemplate,
            HashMap tblContentData2newTableContentQName, Set objectsToRefer) {
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmptNew.getGenerationsOrderedByValidDate()[0];
        ITableContentUsage[] tableContentUsages = generation.getTableContentUsages();
        for (int i = 0; i < tableContentUsages.length; i++) {
            TblContentUsageData tblContentsData = new TblContentUsageData(productCmptTemplate, tableContentUsages[i].getTableContentName());
            if (objectsToRefer.contains(tblContentsData)){
                // keep table usage to old table contents
                continue;
            }
            
            String newTarget = (String)tblContentData2newTableContentQName.get(tblContentsData);
            tableContentUsages[i].setTableContentName(newTarget==null?"":newTarget); //$NON-NLS-1$
        }
    }
    
    /*
     * Fix all links
     * 
     * @param productCmptNew the new productCmpt which will be fixed
     * @param productCmptTemplate the template (old) productCmpt which was used to copy the new
     *            productCmpt
     * @param linkData2newProductCmptQName A map containing the link-identifier
     *            as key and the new created productCmpt which was initiated by the key link
     * @param objectsToRefer A set containing the not copied objects
     */
    private void fixRelationsToProductCmpt(IProductCmpt productCmptNew,
            IProductCmpt productCmptTemplate,
            HashMap linkData2newProductCmptQName,
            Set objectsToRefer) {
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmptNew.getGenerationsOrderedByValidDate()[0];
        IProductCmptLink[] links = generation.getLinks();
        for (int i = 0; i < links.length; i++) {
            LinkData linkData;
            try {
                final IIpsProject ipsProject = productCmptTemplate.getIpsProject();
                IProductCmpt oldTargetProductCmpt = links[i].findTarget(ipsProject);
                linkData = new LinkData(productCmptTemplate, oldTargetProductCmpt, links[i].findAssociation(ipsProject));
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
                return;
            }
            
            String newTarget = (String)linkData2newProductCmptQName.get(linkData);
            if (newTarget != null){
                links[i].setTarget(newTarget);
            } else {
                if (objectsToRefer.contains(linkData)){
                    // keep table usage to old table contents
                    continue;
                }
                links[i].delete();
            }
        }
	}

    private void setPropertiesFromTemplate(IIpsObject template, IIpsObject newObject) {
        if (template instanceof IProductCmpt) {
            ((IProductCmpt)newObject).setProductCmptType(((IProductCmpt)template).getProductCmptType());
        } else if (template instanceof ITableContents) {
            ((ITableContents)newObject).setTableStructure(((ITableContents)template).getTableStructure());
            ((TableContents)newObject).setNumOfColumnsInternal(((ITableContents)template).getNumOfColumns());
        }
    }
	
	/**
	 * Creates a new package, based on the target package. To this base package, the path of the source
	 * is appended, after the given number of segments to ignore is cut off.
	 */
	private IIpsPackageFragment createTargetPackage(IIpsSrcFile file, IProgressMonitor monitor) throws CoreException {
		IIpsPackageFragment result;
		IIpsPackageFragmentRoot root = file.getIpsPackageFragment().getRoot();
		String path = file.getIpsPackageFragment().getRelativePath().toString().replace('/', '.');
		result = root.createPackageFragment(path, false, monitor);
		return result;
	}	
	
	public IProductCmpt getCopiedRoot() {
		return copiedRoot;
	}
	
	/*
     * Represents a productCmptLink, which is defined as a link object between two product cmpts
     * (sourceProductCmpt and targetProductCmpt) and a productCmptTypeAssociation on which the link
     * is based on.
     */
	private class LinkData {
        private IProductCmpt sourceProductCmpt;
        private IProductCmpt targetProductCmpt;
        private IProductCmptTypeAssociation productCmptTypeAssociation;

        public LinkData(IProductCmpt sourceProductCmpt, IProductCmpt targetProductCmpt,
                IProductCmptTypeAssociation productCmptTypeAssociation) {
            this.sourceProductCmpt = sourceProductCmpt;
            this.targetProductCmpt = targetProductCmpt;
            this.productCmptTypeAssociation = productCmptTypeAssociation;
        }

        public IProductCmpt getSourceProductCmpt() {
            return sourceProductCmpt;
        }

        public IProductCmpt getTargetProductCmpt() {
            return targetProductCmpt;
        }

        public IProductCmptTypeAssociation getProductCmptTypeAssociation() {
            return productCmptTypeAssociation;
        }

        public boolean equals(Object other) {
            if ((this == other))
                return true;
            if ((other == null))
                return false;
            if (!(other instanceof LinkData))
                return false;
            LinkData castOther = (LinkData)other;
            return (this.getSourceProductCmpt() != null && this.getSourceProductCmpt().equals(
                    castOther.getSourceProductCmpt()))
                    && (this.getTargetProductCmpt() != null && this.getTargetProductCmpt().equals(
                            castOther.getTargetProductCmpt()))
                    && (this.getProductCmptTypeAssociation() != null && this.getProductCmptTypeAssociation().equals(
                            castOther.getProductCmptTypeAssociation()));

        }

        public int hashCode() {
            int result = 17;
            result = 37 * result + sourceProductCmpt.hashCode();
            result = 37 * result + targetProductCmpt.hashCode();
            result = 37 * result + productCmptTypeAssociation.hashCode();
            return result;
        }
    }

	/*
     * Represents a tblContentUsage object, which is defined by productCmpt and the table usage
     * name (role of the table contents) inside the productCmpt.
     */
    private class TblContentUsageData {
        private IProductCmpt productCmpt;
        private String tableUsageName;

        public TblContentUsageData(ITableContentUsage tblContentUsage) {
            this(tblContentUsage.getProductCmptGeneration().getProductCmpt(), tblContentUsage.getTableContentName());
        }

        public TblContentUsageData(IProductCmpt productCmpt, String tableUsageName) {
            this.productCmpt = productCmpt;
            this.tableUsageName = tableUsageName;
        }

        public IProductCmpt getProductCmpt() {
            return productCmpt;
        }

        public String getTableUsageName() {
            return tableUsageName;
        }

        public boolean equals(Object other) {
            if ((this == other))
                return true;
            if ((other == null))
                return false;
            if (!(other instanceof TblContentUsageData))
                return false;
            TblContentUsageData castOther = (TblContentUsageData)other;
            return (this.getProductCmpt() != null && this.getProductCmpt().equals(castOther.getProductCmpt()))
                    && (this.getTableUsageName() != null && this.getTableUsageName().equals(
                            castOther.getTableUsageName()));
        }

        public int hashCode() {
            int result = 17;
            result = 37 * result + productCmpt.hashCode();
            result = 37 * result + tableUsageName.hashCode();
            return result;
        }
    }

}
