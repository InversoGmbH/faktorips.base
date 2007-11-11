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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.valueset.AllValuesValueSet;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.ConfigElementType;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class ConfigElement extends IpsObjectPart implements IConfigElement {

	final static String TAG_NAME = "ConfigElement"; //$NON-NLS-1$

	private ConfigElementType type = ConfigElementType.POLICY_ATTRIBUTE;

	private String pcTypeAttribute = ""; //$NON-NLS-1$

	private IValueSet valueSet;

	private String value = ""; //$NON-NLS-1$

    private List formulaTestCases = new ArrayList(0);
    
	public ConfigElement(ProductCmptGeneration parent, int id) {
		super(parent, id);
		valueSet = new AllValuesValueSet(this, getNextPartId());
	}

    public ConfigElement(ProductCmptGeneration parent, int id, String pcTypeAttribute) {
        super(parent, id);
        this.pcTypeAttribute = pcTypeAttribute;
        valueSet = new AllValuesValueSet(this, getNextPartId());
    }

    /**
	 * {@inheritDoc}
	 */
	public IProductCmpt getProductCmpt() {
		return (IProductCmpt) getParent().getParent();
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptGeneration getProductCmptGeneration() {
		return (IProductCmptGeneration) getParent();
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigElementType getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setType(ConfigElementType newType) {
		ConfigElementType oldType = type;
		type = newType;
		valueChanged(oldType, newType);
	}

    /**
     * {@inheritDoc}
     */
	public String getPropertyName() {
        return pcTypeAttribute;
    }
    
    /**
     * {@inheritDoc}
     */
    public IProdDefProperty findProperty(IIpsProject ipsProject) throws CoreException {
        return findPcTypeAttribute();
    }

    /**
     * {@inheritDoc}
     */
    public ProdDefPropertyType getPropertyType() {
        return ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET;
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyValue() {
        return value;
    }

    /**
     * {@inheritDoc}
	 */
	public String getPolicyCmptTypeAttribute() {
		return pcTypeAttribute;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPolicyCmptTypeAttribute(String newName) {
		String oldName = pcTypeAttribute;
		pcTypeAttribute = newName;
		name = pcTypeAttribute;
		valueChanged(oldName, pcTypeAttribute);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String newValue) {
		String oldValue = value;
		value = newValue;
		valueChanged(oldValue, value);
	}

	/**
     * {@inheritDoc}
	 */
	public Image getImage() {
		return IpsPlugin.getDefault().getImage("AttributePublic.gif"); //$NON-NLS-1$
	}

	/**
     * {@inheritDoc}
	 */
	public IPolicyCmptTypeAttribute findPcTypeAttribute() throws CoreException {
		IPolicyCmptType pcType = ((IProductCmpt) getIpsObject())
				.findPolicyCmptType();
		if (pcType == null) {
			return null;
		}
        return pcType.findAttributeInSupertypeHierarchy(pcTypeAttribute);
	}
    
    /**
     * {@inheritDoc}
     */
	public ValueDatatype findValueDatatype() throws CoreException {
        IPolicyCmptTypeAttribute a = findPcTypeAttribute();
        if (a!=null) {
            return a.findDatatype();
        }
        return null;
    }

	/**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
		super.validateThis(list, ipsProject);
		IPolicyCmptTypeAttribute attribute = findPcTypeAttribute();
		if (attribute == null) {
            IPolicyCmptType policyCmptType = getProductCmpt().findPolicyCmptType();
            if (policyCmptType==null) {
                String text = NLS.bind(Messages.ConfigElement_policyCmptTypeNotFound, pcTypeAttribute);
                list.add(new Message(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE, text, Message.ERROR, this, PROPERTY_VALUE));
            } else {
                String text = NLS.bind(Messages.ConfigElement_msgAttrNotDefined, pcTypeAttribute, policyCmptType.getName());
                list.add(new Message(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE, text, Message.ERROR, this, PROPERTY_VALUE));
            }
		} else {
    		if (attribute.getAttributeType() == AttributeType.CHANGEABLE
    				|| attribute.getAttributeType() == AttributeType.CONSTANT) {
    			validateValue(attribute, ipsProject, list);
    		}
        }
	}

	private void validateValue(IPolicyCmptTypeAttribute attribute, IIpsProject ipsProject, MessageList list)
			throws CoreException {
		
		ValueDatatype valueDatatype = attribute.findDatatype();
		if (valueDatatype == null) {
			if (!StringUtils.isEmpty(value)) {
				String text = Messages.ConfigElement_msgUndknownDatatype;
				list.add(new Message(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_VALUE, text, Message.WARNING, this,
						PROPERTY_VALUE));
			}
			return;
		}
		try {
			if (valueDatatype.checkReadyToUse().containsErrorMsg()) {
				String text = Messages.ConfigElement_msgInvalidDatatype;
				list.add(new Message(IConfigElement.MSGCODE_INVALID_DATATYPE, text, Message.ERROR, this,
						PROPERTY_VALUE));
				return;
			}
		} catch (Exception e) {
			throw new CoreException(new IpsStatus(e));
		}

		if (!valueDatatype.isParsable(value)) {
        	String valueInMsg = value;
        	if (value==null) {
        		valueInMsg = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        	} else if (value.equals("")){ //$NON-NLS-1$
        		valueInMsg = Messages.ConfigElement_msgValueIsEmptyString;
        	}
			String text = NLS.bind(Messages.ConfigElement_msgValueNotParsable, valueInMsg, valueDatatype.getName());
			list.add(new Message(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE, text, Message.ERROR, this,
					PROPERTY_VALUE));
		}
		
        IValueSet modelValueSet = attribute.getValueSet();
        if (modelValueSet.validate(ipsProject).containsErrorMsg()) {
            String text = Messages.ConfigElement_msgInvalidAttributeValueset;
            list.add(new Message(IConfigElement.MSGCODE_UNKNWON_VALUESET, text, Message.WARNING, this, PROPERTY_VALUE));
            return;
        }

        if (this.type == ConfigElementType.POLICY_ATTRIBUTE && 
                ( ! modelValueSet.containsValueSet(valueSet) || 
                  ! modelValueSet.getValueSetType().equals(valueSet.getValueSetType()))) {
            // model value set contains not the value set defined in the config element
            // or different value set types 
            String text; 
            if (!modelValueSet.getValueSetType().equals(valueSet.getValueSetType())) {
                text = NLS.bind(Messages.ConfigElement_msgTypeMismatch,
                        new String[] { valueSet.toShortString(), modelValueSet.toShortString() });
            } else {
                text = NLS.bind(Messages.ConfigElement_valueSetIsNotASubset, valueSet.toShortString(), modelValueSet
                        .toShortString());
            }
            // determine invalid property (usage e.g. to display problem marker on correct ui control)
            String[] invalidProperties = null; 
            Object obj = valueSet;
            if (valueSet instanceof IEnumValueSet){
                invalidProperties = new String[]{IEnumValueSet.PROPERTY_VALUES};
            } else if (valueSet instanceof IRangeValueSet) {
                invalidProperties = new String[]{IRangeValueSet.PROPERTY_LOWERBOUND, IRangeValueSet.PROPERTY_UPPERBOUND};
            } else {
                // AllValueSet or unkown
                obj = this;
                invalidProperties = new String[]{PROPERTY_VALUE};
            }
            list.add(new Message(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET, text, Message.ERROR, obj, invalidProperties));
        }

		if (StringUtils.isNotEmpty(value)) {
			// validate valuset containment. If the type of this element
			// is PRODUCT_ATTRIBUTE, we do not validate against the
			// valueset of this element but against the valueset of
			// the attribute this element is based on. This is because an
			// element
			// of type PRODUCT_ATTRIBUTE becomes an ALL_VALUES-valueset,
			// but the valueset can not be changed for this type of config
			// element.
			if (this.type == ConfigElementType.POLICY_ATTRIBUTE) {
				if (!valueSet.containsValue(value)) {
					list.add(new Message(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET, NLS.bind(
                            Messages.ConfigElement_msgValueNotInValueset, value), Message.ERROR, this, PROPERTY_VALUE));
				}
			} else if (!modelValueSet.containsValue(value)) { // PRODUCT_ATTRIBUTE
				list.add(new Message(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET, NLS.bind(
                        Messages.ConfigElement_valueIsNotInTheValueSetDefinedInTheModel, value), Message.ERROR, this, PROPERTY_VALUE));
			}
		}
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
		IValueSet oldset = valueSet;
		valueSet = type.newValueSet(this, getNextPartId());
		valueChanged(oldset, valueSet);
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
	protected Element createElement(Document doc) {
		return doc.createElement(TAG_NAME);
	}

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
    }
    
    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = (IValueSet)part;
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            this.valueSet = null;
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		type = ConfigElementType.getConfigElementType(element
				.getAttribute(PROPERTY_TYPE));
		
		value = ValueToXmlHelper.getValueFromElement(element, "Value"); //$NON-NLS-1$
		
		pcTypeAttribute = element.getAttribute("attribute"); //$NON-NLS-1$
		name = pcTypeAttribute;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_TYPE, type.getId());
		element.setAttribute("attribute", pcTypeAttribute); //$NON-NLS-1$
		ValueToXmlHelper.addValueToElement(value, element, "Value"); //$NON-NLS-1$
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
	public IIpsElement[] getChildren() {
        List childrenList = new ArrayList((valueSet!=null?1:0) + formulaTestCases.size());
        if (valueSet != null) {
            childrenList.add(valueSet);
		}
        childrenList.addAll(formulaTestCases);
        return (IIpsElement[]) childrenList.toArray(new IIpsElement[0]);
    }
	
	/**
	 * {@inheritDoc}
	 */
    protected IIpsObjectPart newPart(Element partEl, int id) {
        String xmlTagName = partEl.getNodeName();
    	if (ValueSet.XML_TAG.equals(xmlTagName)) {
    		valueSet = ValueSetType.newValueSet(partEl, this, id);
    		return valueSet;
        } else if (PROPERTY_VALUE.equalsIgnoreCase(xmlTagName)){
            // ignore value nodes, will be parsed in the this#initPropertiesFromXml method
            return null;
        }
        throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
    }

	/**
	 * {@inheritDoc}
	 */
	public ValueDatatype getValueDatatype() {
		try {
			IPolicyCmptTypeAttribute attr = findPcTypeAttribute();
			if (attr == null){
				return null;
			}
			return attr.getValueDatatype();
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}

		return null;
	}
}
