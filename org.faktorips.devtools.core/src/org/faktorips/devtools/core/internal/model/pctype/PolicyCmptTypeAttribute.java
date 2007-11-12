/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.type.Attribute;
import org.faktorips.devtools.core.internal.model.valueset.AllValuesValueSet;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.ConfigElementType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAttribute.
 */
public class PolicyCmptTypeAttribute extends Attribute implements IPolicyCmptTypeAttribute {

    final static String TAG_NAME = "Attribute"; //$NON-NLS-1$

    // member variables.
    private boolean productRelevant = false;
    private AttributeType attributeType = AttributeType.CHANGEABLE;
    private IValueSet valueSet;
    private boolean overwrites = false;
    private String computationMethodSignature = ""; //$NON-NLS-1$
    
    /**
     * Creates a new attribute.
     * 
     * @param pcType The type the attribute belongs to.
     * @param id The attribute's unique id within the type.
     */
    public PolicyCmptTypeAttribute(PolicyCmptType pcType, int id) {
        super(pcType, id);
        valueSet = new AllValuesValueSet(this, getNextPartId());
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getIpsObject();
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute findOverwrittenAttribute(IIpsProject ipsProject) throws CoreException {
        IType supertype = getPolicyCmptType().findSupertype(ipsProject);
        if (supertype==null) {
            return null;
        }
        IAttribute candidate = supertype.findAttribute(name, ipsProject);
        if (candidate==this) {
            return null; // can happen if we have a cycle in the type hierarchy!
        }
        return (IPolicyCmptTypeAttribute)candidate;
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype findDatatype() throws CoreException {
        return getIpsProject().findValueDatatype(getDatatype());
    }

    /**
     * {@inheritDoc}
     */
    public void setAttributeType(AttributeType newType) {
        AttributeType oldType = attributeType;
        attributeType = newType;
        valueChanged(oldType, newType);
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule findValueSetRule() {
        IValidationRule[] rules = getPolicyCmptType().getRules();
        
        for (int i = 0; i < rules.length; i++) {
            String[] attributes = rules[i].getValidatedAttributes();
            for (int j = 0; j < attributes.length; j++) {
                if (attributes[j].equals(getName()) && 
                    rules[i].isCheckValueAgainstValueSetRule()) {
                    return rules[i];
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IValidationRule createValueSetRule(){
        IValidationRule rule = findValueSetRule();
        if(rule != null){
            return rule;
        }
        rule = getPolicyCmptType().newRule();
        rule.setName(getProposalValueSetRuleName());
        rule.addValidatedAttribute(getName());
        rule.setMessageCode(getProposalMsgCodeForValueSetRule());
        rule.setCheckValueAgainstValueSetRule(true);
        rule.setAppliedForAllBusinessFunctions(true);
        rule.setValidatedAttrSpecifiedInSrc(false);
        return rule;
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteValueSetRule(){
        IValidationRule rule = findValueSetRule();
        if(rule != null){
            rule.delete();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public AttributeType getAttributeType() {
        return attributeType;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isChangeable() {
        return getAttributeType() == AttributeType.CHANGEABLE;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDerived() {
        return getAttributeType().isDerived();
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod findComputationMethod(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(computationMethodSignature)) {
            return null;
        }
        IProductCmptType productCmptType = getPolicyCmptType().findProductCmptType(ipsProject);
        if (productCmptType==null) {
            return null;
        }
        return (IProductCmptTypeMethod)productCmptType.findMethod(computationMethodSignature, ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isProductRelevant() {
        return productRelevant;
    }

    /**
     * {@inheritDoc}
     */
    public void setProductRelevant(boolean newValue) {
        boolean oldValue = productRelevant;
        productRelevant = newValue;
        if (oldValue!=newValue && !newValue) {
            computationMethodSignature = ""; //$NON-NLS-1$
        }
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public IValueSet getValueSet() {
        return valueSet;
    }

    /**
     * {@inheritDoc}
     */
    public void setValueSetType(ValueSetType type) {
        if (valueSet != null && type == valueSet.getValueSetType()) {
            return;
        }
        valueSet = type.newValueSet(this, getNextPartId());
        objectHasChanged();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isValueSetUpdateable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public ConfigElementType getConfigElementType() {
        if (!isProductRelevant()) {
            return null;
        }
        if (getAttributeType() == AttributeType.CHANGEABLE) {
            return ConfigElementType.POLICY_ATTRIBUTE;
        }
        if (getAttributeType() == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL || getAttributeType() == AttributeType.DERIVED_ON_THE_FLY) {
            return null;
        }
        throw new RuntimeException("Unkown AttributeType!"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        String baseImageName = "AttributePublic.gif"; //$NON-NLS-1$
        if (isProductRelevant()) {
            return IpsPlugin.getDefault().getProductRelevantImage(baseImageName);
        } else {
        	return IpsPlugin.getDefault().getImage(baseImageName);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String getComputationMethodSignature() {
        return computationMethodSignature;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setComputationMethodSignature(String newMethodName) {
        String oldName = computationMethodSignature;
        computationMethodSignature = newMethodName;
        valueChanged(oldName, newMethodName);
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        super.validateThis(result, ipsProject);
        if (isProductRelevant() && !getPolicyCmptType().isConfigurableByProductCmptType()) {
            String text = Messages.Attribute_msgAttributeCantBeProductRelevantIfTypeIsNot;
            result.add(new Message(MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT, text, Message.ERROR,
                    this, PROPERTY_PRODUCT_RELEVANT));
        }

        if (isDerived() && isProductRelevant()) {
            if (StringUtils.isEmpty(computationMethodSignature)) {
                String text = NLS.bind(Messages.PolicyCmptTypeAttribute_msg_ComputationMethodSignatureIsMissing, getName());
                result.add(new Message(MSGCODE_COMPUTATION_METHOD_NOT_SPECIFIED, text, Message.ERROR, this, PROPERTY_COMPUTATION_METHOD_SIGNATURE));
            } else {
                IMethod computationMethod = findComputationMethod(ipsProject);
                if (computationMethod==null) {
                    String text = Messages.PolicyCmptTypeAttribute_msg_ComputationMethodSignatureDoesNotExists;
                    result.add(new Message(MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST, text, Message.ERROR, this, PROPERTY_COMPUTATION_METHOD_SIGNATURE));
                } else {
                    ValueDatatype attributeDataype = findDatatype(ipsProject);
                    if (attributeDataype!=null && !attributeDataype.equals(computationMethod.findDatatype(ipsProject))) {
                        String text = Messages.PolicyCmptTypeAttribute_msg_ComputationMethodSignatureHasADifferentDatatype;
                        result.add(new Message(MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE, text, Message.ERROR, this, 
                                new String[]{PROPERTY_DATATYPE, PROPERTY_COMPUTATION_METHOD_SIGNATURE}));
                    }
                }
            }
        }
        
        IPolicyCmptTypeAttribute superAttr = findOverwrittenAttribute(ipsProject);
        if (overwrites) {
            if (superAttr == null) {
                String text = NLS.bind(Messages.Attribute_msgNothingToOverwrite, getName());
                result.add(new Message(MSGCODE_NOTHING_TO_OVERWRITE, text, Message.ERROR, this, new String[] {
                        PROPERTY_OVERWRITES, PROPERTY_NAME }));
            } else {
                superAttr.getValueSet().containsValueSet(valueSet, result, valueSet, null);
            }
        }
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
        overwrites = Boolean.valueOf(element.getAttribute(PROPERTY_OVERWRITES)).booleanValue();
        productRelevant = Boolean.valueOf(element.getAttribute(PROPERTY_PRODUCT_RELEVANT)).booleanValue();
        attributeType = AttributeType.getAttributeType(element.getAttribute(PROPERTY_ATTRIBUTE_TYPE));
        computationMethodSignature = element.getAttribute(PROPERTY_COMPUTATION_METHOD_SIGNATURE);
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_OVERWRITES, "" + overwrites); //$NON-NLS-1$
        element.setAttribute(PROPERTY_PRODUCT_RELEVANT, "" + productRelevant); //$NON-NLS-1$
        element.setAttribute(PROPERTY_ATTRIBUTE_TYPE, attributeType.getId());
        element.setAttribute(PROPERTY_COMPUTATION_METHOD_SIGNATURE, "" + computationMethodSignature); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        valueSet = (IValueSet)part;
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        if (xmlTag.getNodeName().equals(ValueSet.XML_TAG)) {
            valueSet = ValueSetType.newValueSet(xmlTag, this, id);
            return valueSet;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        if (valueSet != null) {
            return new IIpsElement[] { valueSet };
        } else {
            return new IIpsElement[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype getValueDatatype() {
        try {
            Datatype type = findDatatype();
            if (type instanceof ValueDatatype) {
                return (ValueDatatype)type;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOverwrite() {
        return overwrites;
    }

    /**
     * {@inheritDoc}
     */
    public void setOverwrite(boolean overwrites) {
        boolean old = this.overwrites;
        this.overwrites = overwrites;
        valueChanged(old, overwrites);
    }

    /**
     * {@inheritDoc}
     */
    public void setValueSetCopy(IValueSet source) {
        IValueSet oldset = valueSet;
        valueSet = source.copy(this, getNextPartId());
        valueChanged(oldset, valueSet);
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        valueSet = new AllValuesValueSet(this, getNextPartId());
    }

    /**
     * {@inheritDoc}
     */
    public String getProposalValueSetRuleName() {
        return NLS.bind(Messages.Attribute_proposalForRuleName, StringUtils.capitalise(getName()));
    }

    /**
     * {@inheritDoc}
     */
    public String getProposalMsgCodeForValueSetRule() {
        return NLS.bind(Messages.Attribute_proposalForMsgCode, getName().toUpperCase());
    }
    
    
    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyName() {
        if (productRelevant) {
            return name;
        }
        return "";  //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public ProdDefPropertyType getProdDefPropertyType() {
        return ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET;
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyDatatype() {
        return getDatatype();
    }
}