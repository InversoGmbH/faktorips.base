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


package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.core.internal.model.ipsobject.TimedIpsObject;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IGenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 * Implementation of product component.
 * 
 * @author Jan Ortmann
 */
public class ProductCmpt extends TimedIpsObject implements IProductCmpt {
    
    private String productCmptType = ""; //$NON-NLS-1$
    private String runtimeId = ""; //$NON-NLS-1$

    public ProductCmpt(IIpsSrcFile file) {
        super(file);
    }

    public ProductCmpt() {
        super();
    }

    /** 
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT;
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptGeneration getProductCmptGeneration(int index) {
        return (IProductCmptGeneration)getGeneration(index);
    }

    /**
	 * {@inheritDoc}
	 */
	public IProductCmptKind findProductCmptKind() throws CoreException {
        IProductCmptNamingStrategy stratgey = getIpsProject().getProductCmptNamingStrategy();
        try {
            String kindName = stratgey.getKindId(getName());
            return new ProductCmptKind(kindName, getIpsProject().getRuntimeIdPrefix() + kindName);
        } catch (Exception e) {
            return null; // error in parsing the name results in a "not found" for the client
        }
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersionId() throws CoreException {
		try {
			return getIpsProject().getProductCmptNamingStrategy().getVersionId(getName());
		} catch (IllegalArgumentException e) {
			throw new CoreException(new IpsStatus("Can't get version id for " + this, e)); //$NON-NLS-1$
		}
	}

    /** 
     * {@inheritDoc}
     */
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException {
        IProductCmptType productCmptType = findProductCmptType(ipsProject);
        if (productCmptType==null) {
            return null;
        }
        return productCmptType.findPolicyCmptType(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public String getProductCmptType() {
        return productCmptType;
    }

    /**
     * {@inheritDoc}
     */
    public void setProductCmptType(String newType) {
        String oldType = productCmptType;
        productCmptType = newType;
        valueChanged(oldType, newType);
    }
    
    /**
     * {@inheritDoc}
     */
	public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findProductCmptType(productCmptType);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void sortPropertiesAccordingToModel(IIpsProject ipsProject) throws CoreException {
        int max = getNumOfGenerations();
        for (int i=0; i<max; i++) {
            IProductCmptGeneration gen = getProductCmptGeneration(i);
            gen.sortPropertiesAccordingToModel(ipsProject);
        }
    }

    /** 
     * {@inheritDoc}
     */
    protected IpsObjectGeneration createNewGeneration(int id) {
        return new ProductCmptGeneration(this, id);
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType type = findProductCmptType(ipsProject);
        if (type == null) {
            String text = NLS.bind(Messages.ProductCmpt_msgUnknownTemplate, this.productCmptType);
            list.add(new Message(MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE, text, Message.ERROR, this, PROPERTY_PRODUCT_CMPT_TYPE)); //$NON-NLS-1$
        } else {
        	try {
				MessageList list3 = type.validate(ipsProject);
				if (list3.getMessageByCode(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY) != null || 
                    list3.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND) != null ||
				    list3.getMessageByCode(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY) != null) {
					String msg = NLS.bind(Messages.ProductCmpt_msgInvalidTypeHierarchy, this.getProductCmptType());
					list.add(new Message(MSGCODE_INCONSISTENT_TYPE_HIERARCHY, msg, Message.ERROR, type, IProductCmptType.PROPERTY_NAME));
				}
			} catch (Exception e) {
				throw new CoreException(new IpsStatus("Error during validate of product component type", e)); //$NON-NLS-1$
			}
        }
        IProductCmptNamingStrategy strategy = ipsProject.getProductCmptNamingStrategy();
        MessageList list2 = strategy.validate(getName());
        for (Iterator iter = list2.iterator(); iter.hasNext();) {
            Message msg = (Message)iter.next();
            Message msgNew = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_NAME);
            list.add(msgNew);
        }
        list2 = strategy.validateRuntimeId(getRuntimeId());
        for (Iterator iter = list2.iterator(); iter.hasNext();) {
            Message msg = (Message)iter.next();
            Message msgNew = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_RUNTIME_ID);
            list.add(msgNew);
        }

        list2 = getIpsProject().checkForDuplicateRuntimeIds(new IIpsSrcFile[] {this.getIpsSrcFile()});
        list.add(list2);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsFormula() {
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (int i=0; i<generations.length; i++) {
            if (((ProductCmptGeneration)generations[i]).containsFormula()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsFormulaTest() {
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (int i=0; i<generations.length; i++) {
            IProductCmptGeneration gen = getProductCmptGeneration(0);
            if (gen.getNumOfFormulas()>0) {
                IFormula[] formulas = gen.getFormulas();
                for (int j = 0; j < formulas.length; j++) {
                    if (formulas[j].getFormulaTestCases().length > 0){
                        return true;
                    }
                }
            }
        }
        return false;
    }    

    /**
     * {@inheritDoc}
     */
    public IDependency[] dependsOn() throws CoreException {

        Set dependencySet = new HashSet();

        if (!StringUtils.isEmpty(productCmptType)) {
            dependencySet.add(IpsObjectDependency.createInstanceOfDependency(this.getQualifiedNameType(), new QualifiedNameType(
                    productCmptType, IpsObjectType.PRODUCT_CMPT_TYPE_V2)));
        }

        // add dependency to related product cmpt's and add dependency to table contents
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (int i = 0; i < generations.length; i++) {
            ((ProductCmptGeneration)generations[i]).dependsOn(dependencySet);
        }

        return (IDependency[])dependencySet.toArray(new IDependency[dependencySet.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_PRODUCT_CMPT_TYPE, productCmptType);
        element.setAttribute(PROPERTY_RUNTIME_ID, runtimeId);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        productCmptType = element.getAttribute(PROPERTY_PRODUCT_CMPT_TYPE);
        runtimeId = element.getAttribute(PROPERTY_RUNTIME_ID);
    }

	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTreeStructure getStructure(IIpsProject ipsProject) throws CycleInProductStructureException {
		return new ProductCmptTreeStructure(this, ipsProject);
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTreeStructure getStructure(GregorianCalendar date, IIpsProject ipsProject) throws CycleInProductStructureException {
		return new ProductCmptTreeStructure(this, date, ipsProject);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRuntimeId() {
		return runtimeId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRuntimeId(String runtimeId) {
        String oldId = this.runtimeId;
        this.runtimeId = runtimeId;
        valueChanged(oldId, runtimeId);
	}

    /**
     * {@inheritDoc}
     */
    public boolean containsDifferenceToModel(IIpsProject ipsProject) throws CoreException {
        IIpsObjectGeneration[] generations = this.getGenerationsOrderedByValidDate();
        for (int i = 0; i < generations.length; i++) {
            IIpsObjectGeneration generation = generations[i];
            if(generation instanceof IProductCmptGeneration){
                IGenerationToTypeDelta delta = ((IProductCmptGeneration)generation).computeDeltaToModel(ipsProject);
                if(!delta.isEmpty()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void fixAllDifferencesToModel(IIpsProject ipsProject) throws CoreException {
        int max = getNumOfGenerations();
        for (int i = 0; i < max; i++) {
            IProductCmptGeneration generation = getProductCmptGeneration(i);
            IGenerationToTypeDelta delta = ((IProductCmptGeneration)generation).computeDeltaToModel(ipsProject);
            delta.fix();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUsedAsTargetProductCmpt(IIpsProject ipsProjectToSearch, String association, IProductCmpt productCmptCandidate) {
        int numOfGenerations = getNumOfGenerations();
        for (int i = 0; i < numOfGenerations; i++) {
            IProductCmptGeneration generation = (IProductCmptGeneration)getGeneration(i);
            IProductCmptLink[] links = generation.getLinks(association);
            for (int j = 0; j < links.length; j++) {
                if (productCmptCandidate.getQualifiedName().equals(links[j].getTarget())){
                    return true;
                }
            }
        }
        return false;
    }
}
