/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.internal.StringUtils;
import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelTypeAssociation extends AbstractModelElement implements IModelTypeAssociation {

    private final Map<Locale, String> pluralLabelsByLocale = new HashMap<Locale, String>();

    private ModelType modelType;
    private AssociationType associationType = AssociationType.Association;
    private int minCardinality = 0;
    private int maxCardinality = Integer.MAX_VALUE;
    private String namePlural = null;
    private String targetJavaClassName = null;
    private boolean isProductRelevant = false;
    private boolean isDerivedUnion = false;
    private boolean isSubsetOfADerivedUnion = false;
    private Boolean isTargetRolePluralRequired = false;
    private String inverseAssociation;
    private String matchingAssociationName;
    private String matchingAssociationSource;

    private String getterName;

    public ModelTypeAssociation(ModelType modelType) {
        super(modelType.getRepository());
        this.modelType = modelType;
    }

    public String getLabelForPlural(Locale locale) {
        String label = pluralLabelsByLocale.get(locale);
        return StringUtils.isEmpty(label) ? getNamePlural() : label;
    }

    public IModelType getModelType() {
        return modelType;
    }

    public AssociationType getAssociationType() {
        return associationType;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public String getNamePlural() {
        return namePlural;
    }

    public IModelType getTarget() throws ClassNotFoundException {
        if (targetJavaClassName != null && targetJavaClassName.length() > 0) {
            Class<?> targetClass = loadClass(targetJavaClassName);
            return getRepository().getModelType(targetClass);
        }
        return null;
    }

    public List<IModelObject> getTargetObjects(IModelObject source) {
        List<IModelObject> targets = new ArrayList<IModelObject>();
        try {
            Object object = getGetter(source).invoke(source);
            if (object instanceof Iterable<?>) {
                for (Object target : (Iterable<?>)object) {
                    targets.add((IModelObject)target);
                }
            } else if (object instanceof IModelObject) {
                targets.add((IModelObject)object);
            }
        } catch (IntrospectionException e) {
            handleGetterError(source, e);
        } catch (IllegalArgumentException e) {
            handleGetterError(source, e);
        } catch (IllegalAccessException e) {
            handleGetterError(source, e);
        } catch (InvocationTargetException e) {
            handleGetterError(source, e);
        }
        return targets;
    }

    private Method getGetter(IModelObject source) throws IntrospectionException {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(getUsedName(), source.getClass(),
                getGetterName(), null);
        return propertyDescriptor.getReadMethod();
    }

    private String getGetterName() {
        if (getterName == null) {
            getterName = "get" + getUsedName().substring(0, 1).toUpperCase() + getUsedName().substring(1);
        }
        return getterName;
    }

    private void handleGetterError(IModelObject source, Exception e) {
        throw new IllegalArgumentException(String.format("Could not get target %s on source object %s.", getUsedName(),
                source), e);
    }

    public boolean isProductRelevant() {
        return isProductRelevant;
    }

    @Override
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        super.initFromXml(parser);

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            initAttributeValuesInternal(parser, i);
        }
        for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (parser.getLocalName().equals(EXTENSION_PROPERTIES_XML_WRAPPER_TAG)) {
                        initExtPropertiesFromXml(parser);
                    } else if (parser.getLocalName().equals(IModelElement.DESCRIPTIONS_XML_WRAPPER_TAG)) {
                        initDescriptionsFromXml(parser);
                    } else if (parser.getLocalName().equals(IModelElement.LABELS_XML_WRAPPER_TAG)) {
                        initLabelsFromXml(parser);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (parser.getLocalName().equals(IModelTypeAssociation.XML_TAG)) {
                        return;
                    }
                    break;
            }
        }
    }

    private void initAttributeValuesInternal(XMLStreamReader parser, int index) {
        initNamePluralInternal(parser, index);
        initTargetJavaClassNameInternal(parser, index);
        initMinCardinalityInternal(parser, index);
        initMaxCardinalityInternal(parser, index);
        initAssociationTypeInternal(parser, index);
        initProduktRelevantInternal(parser, index);
        initDerivedUnionInternal(parser, index);
        initSubsetOfDerivedUnionInternal(parser, index);
        initTargetRolePluralRequiredInternal(parser, index);
        initInverseAssociationInternal(parser, index);
        initMatchingAssociationNameInternal(parser, index);
        initMatchingAssociationSourceInternal(parser, index);
    }

    private void initNamePluralInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(IModelTypeAssociation.PROPERTY_NAME_PLURAL)) {
            namePlural = parser.getAttributeValue(index);
            if (namePlural.length() == 0) {
                namePlural = null;
            }
        }
    }

    private void initTargetJavaClassNameInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(PROPERTY_TARGET)) {
            targetJavaClassName = parser.getAttributeValue(index);
        }
    }

    private void initMinCardinalityInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(PROPERTY_MIN_CARDINALITY)) {
            minCardinality = Integer.parseInt(parser.getAttributeValue(index));
        }
    }

    private void initMaxCardinalityInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(PROPERTY_MAX_CARDINALITY)) {
            maxCardinality = Integer.parseInt(parser.getAttributeValue(index));
        }
    }

    private void initAssociationTypeInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(PROPERTY_ASSOCIATION_TYPE)) {
            associationType = AssociationType.valueOf(parser.getAttributeValue(index));
        }
    }

    private void initProduktRelevantInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(PROPERTY_PRODUCT_RELEVANT)) {
            isProductRelevant = Boolean.valueOf(parser.getAttributeValue(index));
        }
    }

    private void initDerivedUnionInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(PROPERTY_DERIVED_UNION)) {
            isDerivedUnion = Boolean.valueOf(parser.getAttributeValue(index));
        }
    }

    private void initSubsetOfDerivedUnionInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(PROPERTY_SUBSET_OF_A_DERIVED_UNION)) {
            isSubsetOfADerivedUnion = Boolean.valueOf(parser.getAttributeValue(index));
        }
    }

    private void initTargetRolePluralRequiredInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(PROPERTY_TARGET_ROLE_PLURAL_REQUIRED)) {
            isTargetRolePluralRequired = Boolean.valueOf(parser.getAttributeValue(index));
        }
    }

    private void initInverseAssociationInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(PROPERTY_INVERSE_ASSOCIATION)) {
            inverseAssociation = parser.getAttributeValue(index);
        }
    }

    private void initMatchingAssociationNameInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(PROPERTY_MATCHING_ASSOCIATION_NAME)) {
            matchingAssociationName = parser.getAttributeValue(index);
        }
    }

    private void initMatchingAssociationSourceInternal(XMLStreamReader parser, int index) {
        if (parser.getAttributeLocalName(index).equals(PROPERTY_MATCHING_ASSOCIATION_SOURCE)) {
            matchingAssociationSource = parser.getAttributeValue(index);
        }
    }

    @Override
    protected void initLabelFromXml(XMLStreamReader parser) {
        super.initLabelFromXml(parser);
        String localeCode = parser.getAttributeValue(null, IModelElement.LABELS_PROPERTY_LOCALE);
        Locale locale = StringUtils.isEmpty(localeCode) ? null : new Locale(localeCode);
        String value = parser.getAttributeValue(null, IModelElement.LABELS_PROPERTY_PLURAL_VALUE);
        pluralLabelsByLocale.put(locale, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getUsedName());
        sb.append(": ");
        sb.append(targetJavaClassName);
        sb.append('(');
        sb.append(associationType);
        sb.append(' ');
        if (isDerivedUnion) {
            sb.append(", Derived Union ");
        }
        if (isSubsetOfADerivedUnion) {
            sb.append(", Subset of a Derived Union ");
        }
        sb.append(minCardinality);
        sb.append("..");
        sb.append(maxCardinality == Integer.MAX_VALUE ? "*" : maxCardinality);
        if (isProductRelevant) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

    public String getUsedName() {
        return isTargetRolePluralRequired ? getNamePlural() : getName();
    }

    public boolean isDerivedUnion() {
        return isDerivedUnion;
    }

    public boolean isSubsetOfADerivedUnion() {
        return isSubsetOfADerivedUnion;
    }

    public String getInverseAssociation() {
        return inverseAssociation;
    }

    public String getMatchingAssociationName() {
        return matchingAssociationName;
    }

    public String getMatchingAssociationSource() {
        return matchingAssociationSource;
    }
}
