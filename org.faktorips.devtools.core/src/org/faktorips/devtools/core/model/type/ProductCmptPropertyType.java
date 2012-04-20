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

package org.faktorips.devtools.core.model.type;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.productcmpt.AttributeValue;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.internal.model.productcmpt.Formula;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.TableContentUsage;
import org.faktorips.devtools.core.internal.model.productcmpt.ValidationRuleConfig;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;

/**
 * Specifies the different types of product component properties.
 * <p>
 * This enum contains methods providing information about the relationship of product component
 * properties to their corresponding property values. It also provides a convenient way to create
 * property values for given product component properties.
 * 
 * @author Jan Ortmann
 * @author Stefan Widmaier
 * @author Alexander Weickmann
 * 
 * @see IProductCmptProperty
 */
public enum ProductCmptPropertyType {

    /**
     * Represents an {@link IProductCmptTypeAttribute} of an {@link IProductCmptType}.
     * <p>
     * An {@link IProductCmptProperty} with this {@link ProductCmptPropertyType} can be safely
     * casted to {@link IProductCmptTypeAttribute}.
     * <p>
     * An {@link IPropertyValue} with this {@link ProductCmptPropertyType} can be safely casted to
     * {@link IAttributeValue}.
     */
    PRODUCT_CMPT_TYPE_ATTRIBUTE("productCmptTypeAttribute", Messages.ProductCmptPropertyType_productAttribute) { //$NON-NLS-1$

        @Override
        public IAttributeValue createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {

            AttributeValue attributeValue = new AttributeValue(container, partId,
                    property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            IProductCmptTypeAttribute attribute = (IProductCmptTypeAttribute)property;
            AttributeValueType attributeValueType = AttributeValueType.getTypeFor(attribute);

            String defaultStringValue;
            if (attribute != null) {
                defaultStringValue = attribute.getDefaultValue();
            } else {
                defaultStringValue = StringUtils.EMPTY;
            }
            // TODO the default value should also be a ValueHolder
            Object defaultObject;
            if (attributeValueType == AttributeValueType.MULTI_VALUE) {
                ArrayList<SingleValueHolder> defaultList = new ArrayList<SingleValueHolder>();
                if (defaultStringValue != null) {
                    SingleValueHolder defaultHolder = new SingleValueHolder(attributeValue, defaultStringValue);
                    defaultList.add(defaultHolder);
                }
                defaultObject = defaultList;
            } else {
                defaultObject = defaultStringValue;
            }

            IValueHolder<?> valueHolder = attributeValueType.newHolderInstance(attributeValue, defaultObject);
            attributeValue.setValueHolderInternal(valueHolder);
            return attributeValue;
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IAttributeValue.class;
        }

        @Override
        public Class<? extends IpsObjectPart> getValueImplementationClass() {
            return AttributeValue.class;
        }

        @Override
        public String getValueXmlTagName() {
            return AttributeValue.TAG_NAME;
        }

    },

    /**
     * The {@link IProductCmptProperty} is an {@link ITableStructureUsage} of an
     * {@link IProductCmptType}.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link ITableStructureUsage}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link ITableContentUsage}.
     */
    TABLE_STRUCTURE_USAGE("tableStructureUsage", Messages.ProductCmptPropertyType_tableUsage) { //$NON-NLS-1$

        @Override
        public ITableContentUsage createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {

            ITableContentUsage tableUsage = new TableContentUsage(container, partId,
                    property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            return tableUsage;
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return ITableContentUsage.class;
        }

        @Override
        public Class<? extends IpsObjectPart> getValueImplementationClass() {
            return TableContentUsage.class;
        }

        @Override
        public String getValueXmlTagName() {
            return TableContentUsage.TAG_NAME;
        }
    },

    /**
     * The {@link IProductCmptProperty} is an {@link IProductCmptTypeMethod} of an
     * {@link IProductCmptType} that is marked as <em>formula signature</em>.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IProductCmptTypeMethod}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IFormula}.
     */
    FORMULA_SIGNATURE_DEFINITION("formulaSignatureDefinition", Messages.ProductCmptPropertyType_fomula) { //$NON-NLS-1$

        @Override
        public IFormula createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {

            IFormula formula = new Formula(container, partId, property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            return formula;
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IFormula.class;
        }

        @Override
        public Class<? extends IpsObjectPart> getValueImplementationClass() {
            return Formula.class;
        }

        @Override
        public String getValueXmlTagName() {
            return Formula.TAG_NAME;
        }
    },

    /**
     * The {@link IProductCmptProperty} is an {@link IPolicyCmptTypeAttribute} of an
     * {@link IPolicyCmptType}.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IPolicyCmptTypeAttribute}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to {@link IConfigElement}.
     */
    POLICY_CMPT_TYPE_ATTRIBUTE("policyCmptTypeAttribute", Messages.ProductCmptPropertyType_defaultValueAndValueSet) { //$NON-NLS-1$

        @Override
        public IConfigElement createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {

            IConfigElement configElement = new ConfigElement(container, partId,
                    property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)property;
            if (attribute != null) {
                configElement.setPolicyCmptTypeAttribute(attribute.getName());
                configElement.setValue(attribute.getDefaultValue());
                configElement.setValueSetCopy(attribute.getValueSet());
            }
            return configElement;
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IConfigElement.class;
        }

        @Override
        public Class<? extends IpsObjectPart> getValueImplementationClass() {
            return ConfigElement.class;
        }

        @Override
        public String getValueXmlTagName() {
            return ConfigElement.TAG_NAME;
        }
    },

    /**
     * The {@link IProductCmptProperty} is an {@link IValidationRule} of an {@link IPolicyCmptType}.
     * <p>
     * An {@link IProductCmptProperty} with this type can be safely casted to
     * {@link IValidationRule}.
     * <p>
     * An {@link IPropertyValue} with this type can be safely casted to
     * {@link IValidationRuleConfig}.
     */
    VALIDATION_RULE("validationRule", Messages.ProductCmptPropertyType_ValidationRule) { //$NON-NLS-1$

        @Override
        public IValidationRuleConfig createPropertyValue(IPropertyValueContainer container,
                IProductCmptProperty property,
                String partId) {

            IValidationRuleConfig ruleConfig = new ValidationRuleConfig(container, partId,
                    property == null ? "" : property.getPropertyName()); //$NON-NLS-1$
            IValidationRule rule = (IValidationRule)property;
            if (rule != null) {
                ruleConfig.setActive(rule.isActivatedByDefault());
            }
            return ruleConfig;
        }

        @Override
        public Class<? extends IPropertyValue> getValueClass() {
            return IValidationRuleConfig.class;
        }

        @Override
        public Class<? extends IpsObjectPart> getValueImplementationClass() {
            return ValidationRuleConfig.class;
        }

        @Override
        public String getValueXmlTagName() {
            return ValidationRuleConfig.TAG_NAME;
        }

    };

    private final String id;

    private final String name;

    private ProductCmptPropertyType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the name of this property type.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the id of this property type.
     */
    public String getId() {
        return id;
    }

    /**
     * Creates an {@link IPropertyValue} for this property type.
     * <p>
     * If you have the concrete class of the {@link IPropertyValue} you want to create, use the
     * typesafe method
     * {@link #createPropertyValue(IPropertyValueContainer, IProductCmptProperty, String, Class)}
     * 
     * @param container The {@link IPropertyValueContainer} the new part is created for
     * @param property The {@link IProductCmptProperty} a new value is created for
     * @param partId The new parts's id
     */
    public abstract IPropertyValue createPropertyValue(IPropertyValueContainer container,
            IProductCmptProperty property,
            String partId);

    /**
     * Returns the class of the {@link IPropertyValue} represented by this
     * {@link ProductCmptPropertyType}.
     */
    public abstract Class<? extends IPropertyValue> getValueClass();

    /**
     * Returns the class of the default implementation corresponding to the {@link IPropertyValue}
     * represented by this {@link ProductCmptPropertyType}.
     */
    public abstract Class<? extends IpsObjectPart> getValueImplementationClass();

    /**
     * Returns the XML tag for {@link IPropertyValue} of this {@link ProductCmptPropertyType}.
     */
    public abstract String getValueXmlTagName();

    /**
     * Searches and returns a {@link ProductCmptPropertyType} that can create IPS object parts for
     * the given class.
     * <p>
     * This method also takes subclasses into account. For example, calling this method with
     * {@code AttributeValue.class} will return {@link #PRODUCT_CMPT_TYPE_ATTRIBUTE} even though
     * it's value class is {@link IAttributeValue}.
     * <p>
     * However, if the given class is not part of the {@link IPropertyValue} hierarchy, this method
     * returns null.
     * 
     * @param partType The class a {@link ProductCmptPropertyType} is searched for
     */
    public static ProductCmptPropertyType getTypeForValueClass(Class<? extends IIpsObjectPart> partType) {
        for (ProductCmptPropertyType type : values()) {
            if (type.getValueClass().isAssignableFrom(partType)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Searches and returns a {@link ProductCmptPropertyType} that can create IPS object parts for
     * the given XML tag name.
     * <p>
     * Returns null if no appropriate property type is found.
     * 
     * @param xmlTagName The XML tag name a {@link ProductCmptPropertyType} is searched for
     */
    public static ProductCmptPropertyType getTypeForXmlTag(String xmlTagName) {
        for (ProductCmptPropertyType type : values()) {
            if (type.getValueXmlTagName().equals(xmlTagName)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Creates and returns a concrete {@link IPropertyValue} of the given property type.
     * <p>
     * This method does <strong>not</strong> add the created property value to the given container
     * but only using this information for setting the parent!
     * <p>
     * If the given {@link IProductCmptProperty} parameter is null, you have to specify the concrete
     * type to be created by setting the correct type parameter. If the parameter is not null, the
     * concrete type is obtained from this parameter and the caller has to ensure that the given
     * type is the same as the type obtained from the {@link IProductCmptProperty}'s type.
     * 
     * @param container The container that is used as parent object, the created element is
     *            <strong>not</strong> added to it
     * @param productCmptProperty The {@link IProductCmptProperty} that may be set in the new value
     *            if it is not null
     * @param partId The part id of the generated {@link IPropertyValue}
     * @param type The class that specifies the type of the created element
     */
    public static <T extends IPropertyValue> T createPropertyValue(IPropertyValueContainer container,
            IProductCmptProperty productCmptProperty,
            String partId,
            Class<T> type) {

        @SuppressWarnings("unchecked")
        // The enum could not be specialized with generics but the implementation is type safe
        T propertyValue = (T)ProductCmptPropertyType.getTypeForValueClass(type).createPropertyValue(container,
                productCmptProperty, partId);
        return propertyValue;
    }

    /**
     * Returns the property type corresponding to the provided id or null if no corresponding
     * property type can be found.
     * 
     * @param id The id whose corresponding property type to search for
     */
    public static ProductCmptPropertyType getValueById(String id) {
        for (ProductCmptPropertyType propertyType : values()) {
            if (propertyType.getId().equals(id)) {
                return propertyType;
            }
        }
        return null;
    }

}
