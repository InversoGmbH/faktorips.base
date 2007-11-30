/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcasetype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test policy component type parameter class. 
 * Defines a test policy component type parameter for a specific test case type.
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmptTypeParameter extends TestParameter implements
		ITestPolicyCmptTypeParameter {

	/** Tags */
	final static String TAG_NAME = "PolicyCmptTypeParameter"; //$NON-NLS-1$
	
	private String policyCmptType = ""; //$NON-NLS-1$
	
	private String association = ""; //$NON-NLS-1$
	
	private boolean requiresProductCmpt = false;
	
	private List testAttributes = new ArrayList(0);
	
	private List testPolicyCmptTypeChilds = new ArrayList(0);
	
	private int minInstances = 0;
	
	private int maxInstances = 1;
	
	public TestPolicyCmptTypeParameter(IIpsObject parent, int id) {
		super(parent, id);
	}

	public TestPolicyCmptTypeParameter(IIpsObjectPart parent, int id) {
		super(parent, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsElement[] getChildren() {
		int numOfChildren = testAttributes.size() + testPolicyCmptTypeChilds.size();
		IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
		List childrenList = new ArrayList(numOfChildren);
		childrenList.addAll(testAttributes);
		childrenList.addAll(testPolicyCmptTypeChilds);
		childrenList.toArray(childrenArray);
		return childrenArray;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reinitPartCollections() {
		this.testAttributes = new ArrayList();
		this.testPolicyCmptTypeChilds = new ArrayList();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reAddPart(IIpsObjectPart part) {
		if (part instanceof TestAttribute) {
			testAttributes.add(part);
			return;
		}else if(part instanceof TestPolicyCmptTypeParameter){
			testPolicyCmptTypeChilds.add(part);
			return;
		}
		throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
	}

    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof TestAttribute) {
            testAttributes.remove(part);
            return;
        }else if(part instanceof TestPolicyCmptTypeParameter){
            testPolicyCmptTypeChilds.remove(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

	/**
	 * {@inheritDoc}
	 */
	protected IIpsObjectPart newPart(Element xmlTag, int id) {
		String xmlTagName = xmlTag.getNodeName();
		if (xmlTagName.equals(TestAttribute.TAG_NAME)) {
			return newTestAttributeInternal(id);
		} else if (xmlTagName.equals(TAG_NAME)) {
			return newTestPolicyCmptTypeParamChildInternal(id);
		}
		throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
	}	
	
    /**
     * {@inheritDoc}
     */
    public String getDatatype() {
        return getPolicyCmptType();
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(String datatype) {
        setPolicyCmptType(datatype);
    }

    /**
     * {@inheritDoc}
     */
    public void setTestParameterType(TestParameterType testParameterType) {
        ArgumentCheck.isTrue(testParameterType.equals(TestParameterType.INPUT)
                || testParameterType.equals(TestParameterType.EXPECTED_RESULT)
                || testParameterType.equals(TestParameterType.COMBINED));
        TestParameterType oldType = this.type;
        this.type = testParameterType;
        valueChanged(oldType, testParameterType);
    }
    
	/**
	 * {@inheritDoc}
	 */
	public String getPolicyCmptType() {
		return policyCmptType;
	}

    /**
	 * {@inheritDoc}
	 */
	public void setPolicyCmptType(String policyCmptType) {
		String oldPolicyCmptType = this.policyCmptType;
        this.policyCmptType = policyCmptType;
        valueChanged(oldPolicyCmptType, policyCmptType);
	}

	/**
	 * {@inheritDoc}
	 */
	public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(policyCmptType)) {
            return null;
        }
		return ipsProject.findPolicyCmptType(policyCmptType);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAssociation() {
		return association;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAssociation(String association) {
		String oldAssociation = this.association;
		this.association = association;
		valueChanged(oldAssociation, association);
	}

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAssociation findAssociation(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(association)) {
            return null;
        }
        // if this is a root parameter then the association field is not used
        if (isRoot()){
            return null;
        }
        
        // this is a child parameter therfore a association should exists
        ITestPolicyCmptTypeParameter parent = (ITestPolicyCmptTypeParameter) getParent();
        IPolicyCmptType pcType = parent.findPolicyCmptType(ipsProject);
        
        while (pcType != null){
            IPolicyCmptTypeAssociation[] associations = pcType.getPolicyCmptTypeAssociations();
            for (int i = 0; i < associations.length; i++) {
                if (associations[i].getName().equals(association)) {
                    return associations[i];
                }
            }
            pcType = (IPolicyCmptType)pcType.findSupertype(ipsProject);
        }
        
        return null;
    }

	/**
	 * {@inheritDoc}
	 */
	protected Element createElement(Document doc) {
		return doc.createElement(TAG_NAME);
	}
	
    /**
     * {@inheritDoc}
     */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		policyCmptType = element.getAttribute(PROPERTY_POLICYCMPTTYPE);
		association = element.getAttribute(PROPERTY_ASSOCIATION);
		String needsProductCmptAttr = element.getAttribute(PROPERTY_REQUIRES_PRODUCTCMT);
		if (StringUtils.isNotEmpty(needsProductCmptAttr)){
			requiresProductCmpt = needsProductCmptAttr.equalsIgnoreCase("yes")  ? true : //$NON-NLS-1$
							   needsProductCmptAttr.equalsIgnoreCase("true") ? true : //$NON-NLS-1$
							   needsProductCmptAttr.equalsIgnoreCase("1")    ? true : false; //$NON-NLS-1$
		}else{
			requiresProductCmpt = false;
		}
		try {
			minInstances = Integer.parseInt(element.getAttribute(PROPERTY_MIN_INSTANCES));
		} catch (NumberFormatException e) {
			minInstances = 0;
		}
		try {
			maxInstances = Integer.parseInt(element.getAttribute(PROPERTY_MAX_INSTANCES));
		} catch (NumberFormatException e) {
			maxInstances = 0;
		}		
	}

    /**
     * {@inheritDoc}
     */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_POLICYCMPTTYPE, policyCmptType);
		element.setAttribute(PROPERTY_ASSOCIATION, association);
		element.setAttribute(PROPERTY_REQUIRES_PRODUCTCMT, requiresProductCmpt ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		element.setAttribute(PROPERTY_MIN_INSTANCES, "" + minInstances); //$NON-NLS-1$
		element.setAttribute(PROPERTY_MAX_INSTANCES, "" + maxInstances); //$NON-NLS-1$
	}
	
    /**
     * If no association is specified then return the policy cmpt type image or if a product cmpt is required the
     * the product cmpt image. If a association is specified then return the image which is provided by the association
     * or if the association is not found the default "association.gif" image.
     * 
     * {@inheritDoc}
     */
    public Image getImage() {
        if (StringUtils.isEmpty(association))
            if (requiresProductCmpt)
                return IpsObjectType.PRODUCT_CMPT.getEnabledImage();
            else
                return IpsObjectType.POLICY_CMPT_TYPE.getEnabledImage();
        
        if (!isRoot()) {
            try {
                //TODO provide ipsProject as parameter
                IPolicyCmptTypeAssociation association = findAssociation(getIpsProject());
                if (association != null)
                    return association.getImage();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return IpsPlugin.getDefault().getImage("Association.gif"); //$NON-NLS-1$
    }

    /**
	 * {@inheritDoc}
	 */
	public ITestAttribute newInputTestAttribute() {
		TestAttribute a = newTestAttributeInternal(getNextPartId());
		a.setTestAttributeType(TestParameterType.INPUT);
        objectHasChanged();
		return a;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestAttribute newExpectedResultTestAttribute() {
	    TestAttribute a = newTestAttributeInternal(getNextPartId());
	    a.setTestAttributeType(TestParameterType.EXPECTED_RESULT);
	    objectHasChanged();
	    return a;
	}

    /**
	 * Creates a new test attribute without updating the src file.
	 */
	private TestAttribute newTestAttributeInternal(int id) {
		TestAttribute a = new TestAttribute(this, id);
		testAttributes.add(a);
		return a;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter newTestPolicyCmptTypeParamChild() {
		ITestPolicyCmptTypeParameter p = newTestPolicyCmptTypeParamChildInternal(getNextPartId());
		objectHasChanged();
		return p;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestAttribute getTestAttribute(String attributeName) {
		for (Iterator it = testAttributes.iterator(); it.hasNext();) {
			ITestAttribute a = (ITestAttribute) it.next();
			if (a.getName().equals(attributeName)) {
				return a;
			}
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestAttribute[] getTestAttributes() {
		ITestAttribute[] a = new ITestAttribute[testAttributes.size()];
		testAttributes.toArray(a);
		return a;
	}

	/**
	 * Removes the attribute from the type. 
	 */
	void removeTestAttribute(TestAttribute attribute) {
		testAttributes.remove(attribute);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public TestPolicyCmptTypeParameter getTestPolicyCmptTypeChild(String name) {
		for (Iterator it = testPolicyCmptTypeChilds.iterator(); it.hasNext();) {
			TestPolicyCmptTypeParameter p = (TestPolicyCmptTypeParameter) it.next();
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter[] getTestPolicyCmptTypeParamChilds() {
		TestPolicyCmptTypeParameter[] p = new TestPolicyCmptTypeParameter[testPolicyCmptTypeChilds.size()];
		testPolicyCmptTypeChilds.toArray(p);
		return p;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ITestPolicyCmptTypeParameter getTestPolicyCmptTypeParamChild(String name){
		for (Iterator it = testPolicyCmptTypeChilds.iterator(); it.hasNext();) {
			ITestPolicyCmptTypeParameter p = (ITestPolicyCmptTypeParameter) it.next();
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}	

	/**
	 * Creates a new test policy component type param child without updating the src file.
	 */
	private TestPolicyCmptTypeParameter newTestPolicyCmptTypeParamChildInternal(int id) {
		TestPolicyCmptTypeParameter p = new TestPolicyCmptTypeParameter(this, id);
		testPolicyCmptTypeChilds.add(p);
		return p;
	}
    
    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter getParentTestPolicyCmptTypeParam() {
        return (ITestPolicyCmptTypeParameter)getParent();
    }

    /**
     * {@inheritDoc}
     */
	public void removeTestPolicyCmptTypeParamChild(TestPolicyCmptTypeParameter testPolicyCmptTypeParamChildName) {
		testPolicyCmptTypeChilds.remove(testPolicyCmptTypeParamChildName);
	}

    /**
	 * {@inheritDoc}
	 */	
	public ITestParameter getRootParameter() {
        ITestParameter current = this;
		while (!current.isRoot()){
			current = (ITestParameter) current.getParent();
		}
		return current;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequiresProductCmpt() {
		return requiresProductCmpt;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRequiresProductCmpt(boolean newNeedsProductCmpt) {
		boolean oldRequiresProductCmpt = this.requiresProductCmpt;
		this.requiresProductCmpt = newNeedsProductCmpt;
		valueChanged(oldRequiresProductCmpt, newNeedsProductCmpt);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getMinInstances() {
		return minInstances;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMinInstances(int minInstances) {
		int oldMinInstances = this.minInstances;
		this.minInstances = minInstances;
		valueChanged(oldMinInstances, minInstances);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxInstances() {
		return maxInstances;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMaxInstances(int maxInstances) {
		int oldMaxInstances = this.maxInstances;
		this.maxInstances = maxInstances;
		valueChanged(oldMaxInstances, maxInstances);
	}

    /**
     * {@inheritDoc}
     */
    public boolean isRoot() {
        return (! (getParent() instanceof TestPolicyCmptTypeParameter)); 
    }
    
    /**
     * {@inheritDoc}
     */
    public int[] moveTestAttributes(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(testAttributes);
        int[] newIdxs = mover.move(indexes, up);
        valueChanged(indexes, newIdxs);
        return newIdxs;
    }    
    
    /**
     * {@inheritDoc}
     */
    public int[] moveTestPolicyCmptTypeChild(int[] indexes, boolean up){
        ListElementMover mover = new ListElementMover(testPolicyCmptTypeChilds);
        int[] newIdxs = mover.move(indexes, up);
        valueChanged(indexes, newIdxs);
        return newIdxs;
    }
    
    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile[] getAllowedProductCmpt(IIpsProject ipsProjectToSearch, IProductCmpt productCmpt) throws CoreException {
        if (isRoot() || productCmpt == null){
            IPolicyCmptType policyCmptType = findPolicyCmptType(ipsProjectToSearch);
            if (policyCmptType == null){
                return new IIpsSrcFile[0];
            }
            IProductCmptType productCmptType = policyCmptType.findProductCmptType(getIpsProject());
            if (productCmptType == null){
                return new IIpsSrcFile[0];
            }            
            return ipsProjectToSearch.findAllProductCmptSrcFiles(productCmptType, true);
        }
        IPolicyCmptTypeAssociation policyCmptTypeAssociation = findAssociation(ipsProjectToSearch);
        if (policyCmptTypeAssociation == null){
            return new IIpsSrcFile[0];
        }
        IPolicyCmptType policyCmptTypeTarget = policyCmptTypeAssociation.findTargetPolicyCmptType(getIpsProject());
        if (!policyCmptTypeTarget.isConfigurableByProductCmptType()){
            return new IIpsSrcFile[0];
        }
        IProductCmptType productCmptTypeTarget = policyCmptTypeTarget.findProductCmptType(getIpsProject());
        if (productCmptTypeTarget == null){
            return new IIpsSrcFile[0];
        }
        IProductCmptTypeAssociation association = policyCmptTypeAssociation.findMatchingProductCmptTypeAssociation(getIpsProject());
        if (association == null){
            // no matching association found
            return new IIpsSrcFile[0];
        }
        List result = new ArrayList(100);
        IIpsObjectGeneration[] generations = productCmpt.getGenerations();
        for (int i = 0; i < generations.length; i++) {
            IProductCmptLink[] associations = ((IProductCmptGeneration)generations[i]).getLinks(association.getName());
            for (int j = 0; j < associations.length; j++) {
                IIpsSrcFile productCmptFound = ipsProjectToSearch.findIpsSrcFile(IpsObjectType.PRODUCT_CMPT, associations[j].getTarget());
                if (productCmptFound != null && !result.contains(productCmptFound)) {
                    result.add(productCmptFound);
                }
            }
        }
        return (IIpsSrcFile[])result.toArray(new IIpsSrcFile[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        
        // check if the policy component type exists
        IPolicyCmptType policyCmptTypeFound = findPolicyCmptType(ipsProject);
        if (policyCmptTypeFound == null) {
            String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_PolicyCmptTypeNotExists, policyCmptType);
            Message msg = new Message(MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS, text, Message.ERROR, this,
                    PROPERTY_POLICYCMPTTYPE); //$NON-NLS-1$
            list.add(msg);
        }

        // check min and max instances
        if (minInstances > maxInstances) {
            String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_MinGreaterThanMax, "" + minInstances, "" + maxInstances);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            Message msg = new Message(MSGCODE_MIN_INSTANCES_IS_GREATER_THAN_MAX, text, Message.ERROR, this,
                    PROPERTY_MIN_INSTANCES); //$NON-NLS-1$
            list.add(msg);
        }
         if (maxInstances < minInstances) {
            String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_MaxLessThanMin, "" + minInstances, "" + maxInstances);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            Message msg = new Message(MSGCODE_MAX_INSTANCES_IS_LESS_THAN_MIN, text, Message.ERROR, this,
                    PROPERTY_MAX_INSTANCES); //$NON-NLS-1$
            list.add(msg);
        }
        
        // check if the type of the parameter matches the type of the parent
        if (!isRoot()){
            TestParameterType parentType = ((ITestPolicyCmptTypeParameter)getParent()).getTestParameterType();
            if (!TestParameterType.isChildTypeMatching(type, parentType)) {
                String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_TypeNotAllowed, type.getName(), parentType
                        .getName());
                Message msg = new Message(MSGCODE_TYPE_DOES_NOT_MATCH_PARENT_TYPE, text, Message.ERROR, this,
                        PROPERTY_TEST_PARAMETER_TYPE);
                list.add(msg);
            }
        }
        
        // check if the association exists
        //  if the parameter is root, no association is defined
        if (! isRoot()){
            IPolicyCmptTypeAssociation associationFound = findAssociation(ipsProject);
            if (associationFound == null) {
                String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_AssociationNotExists, association);
                Message msg = new Message(MSGCODE_ASSOCIATION_NOT_EXISTS, text, Message.ERROR, this,
                        PROPERTY_ASSOCIATION); //$NON-NLS-1$
                list.add(msg);
            } else if (policyCmptTypeFound != null){
                // check if the association is specified and the policy component type exists
                //   that the policy cmpt type is a possible target of the association  
                IPolicyCmptType targetOfAssociation = associationFound.findTargetPolicyCmptType(getIpsProject());
                if (targetOfAssociation == null){
                    String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_TargetOfAssociationNotExists, associationFound.getTarget(), association);
                    Message msg = new Message(MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS, text, Message.WARNING, this,
                            PROPERTY_ASSOCIATION); //$NON-NLS-1$
                    list.add(msg);
                }else{
                    if (!policyCmptTypeFound.isSubtypeOrSameType(targetOfAssociation)){
                        String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_PolicyCmptNotAllowedForAssociation, policyCmptType, association);
                        Message msg = new Message(MSGCODE_WRONG_POLICY_CMPT_TYPE_OF_ASSOCIATION, text, Message.ERROR, this,
                                PROPERTY_POLICYCMPTTYPE); //$NON-NLS-1$
                        list.add(msg);
                    }
                }
                
                // ckeck if the target of an association exists
                if (associationFound.isAssoziation()){
                    ITestParameter targetOfAssoziationInTestCaseType = null;
                    ITestParameter[] allTestParameter = getTestCaseType().getAllTestParameter();
                    for (int i = 0; i < allTestParameter.length; i++) {
                        if (allTestParameter[i] instanceof ITestPolicyCmptTypeParameter){
                            ITestPolicyCmptTypeParameter tPCTP = (ITestPolicyCmptTypeParameter) allTestParameter[i];
                            boolean isTestObject = false;
                            isTestObject = isRoot();
                            if (!isTestObject){
                                // check if the test parameter implements no accosiation
                                // because we search only for non accosiations
                                IPolicyCmptTypeAssociation association = tPCTP.findAssociation(ipsProject);
                                isTestObject =  (association == null) || ! association.isAssoziation();
                            }
                            if (isTestObject && tPCTP.getPolicyCmptType().equals(associationFound.getTarget())) {
                                // check if the parameter type matches, if the parameter type is unequal then
                                // the object will not be generated in the test case and thus it is not available
                                if (tPCTP.getTestParameterType().equals(this.getTestParameterType())
                                        || tPCTP.isCombinedParameter()) {
                                    targetOfAssoziationInTestCaseType = tPCTP;
                                    break;
                                }
                            }
                        }
                    }
                    if (targetOfAssoziationInTestCaseType == null){
                        String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationWarning_AccosiationTargetNotInTestCaseType, policyCmptType, association);
                        Message msg = new Message(MSGCODE_TARGET_OF_ASSOCIATION_NOT_EXISTS_IN_TESTCASETYPE, text, Message.WARNING, this,
                                PROPERTY_POLICYCMPTTYPE); //$NON-NLS-1$
                        list.add(msg);          
                    }
                }
            }
        } // check association end
        
        // check if this is a root parameter and the related policy cmpt is abstract, that the required product cmpt flag
        // is true, otherwise it is not possible to select a derived class of the abstract policy cmpt.
        // for none root parameters this check is not necessary, because in this case a dialog will be displayed to select
        // the target of a association (childs are always defined by using a association)
        if (isRoot() && policyCmptTypeFound != null){
            if (!isRequiresProductCmpt() && policyCmptTypeFound.isAbstract()){
                String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_MustRequireProdCmptIfRootAndAbstract, policyCmptType);
                Message msg = new Message(MSGCODE_MUST_REQUIRE_PROD_IF_ROOT_AND_ABSTRACT, text, Message.ERROR, this,
                        PROPERTY_REQUIRES_PRODUCTCMT); //$NON-NLS-1$
                list.add(msg);
            }
        }
        
        // check if the requires product flag is only true if the related test policy cmpt is configurable by product cmpt type
        if (policyCmptTypeFound != null && requiresProductCmpt && ! policyCmptTypeFound.isConfigurableByProductCmptType() && policyCmptTypeFound != null){
            String text = NLS.bind(Messages.TestPolicyCmptTypeParameter_ValidationError_FlagRequiresIsTrueButPolicyCmptTypeIsNotConfByProduct, policyCmptType);
            Message msg = new Message(MSGCODE_REQUIRES_PROD_BUT_POLICY_CMPT_TYPE_IS_NOT_CONF_BY_PROD, text, Message.ERROR, this,
                    PROPERTY_REQUIRES_PRODUCTCMT); //$NON-NLS-1$
            list.add(msg);
        }
    }
}