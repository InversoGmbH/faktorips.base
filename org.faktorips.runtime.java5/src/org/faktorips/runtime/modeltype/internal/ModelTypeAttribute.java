/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelTypeAttribute extends AbstractModelElement implements IModelTypeAttribute {

    private ModelType modelType;

    private Class<?> datatype;

    private String datatypeName;

    private ValueSetType valueSetType = ValueSetType.AllValues;

    private AttributeType attributeType = AttributeType.CHANGEABLE;

    private boolean isProductRelevant = false;

    private String getterName;

    public ModelTypeAttribute(ModelType modelType) {
        super(modelType.getRepository());
        this.modelType = modelType;
    }

    public IModelType getModelType() {
        return modelType;
    }

    public Class<?> getDatatype() throws ClassNotFoundException {
        if (datatype == null) {
            datatype = findDatatype();
        }
        return datatype;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public ValueSetType getValueSetType() {
        return valueSetType;
    }

    public boolean isProductRelevant() {
        return isProductRelevant;
    }

    @Override
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        super.initFromXml(parser);

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals(PROPERTY_DATATYPE)) {
                datatypeName = parser.getAttributeValue(i);
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_VALUE_SET_TYPE)) {
                valueSetType = ValueSetType.valueOf(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_ATTRIBUTE_TYPE)) {
                attributeType = AttributeType.forName(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_PRODUCT_RELEVANT)) {
                isProductRelevant = Boolean.valueOf(parser.getAttributeValue(i));
            }
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
                    if (parser.getLocalName().equals(IModelTypeAttribute.XML_TAG)) {
                        return;
                    }
                    break;
            }
        }
    }

    protected Class<?> findDatatype() {
        String actualName = datatypeName;
        int arraydepth = 0;
        while (actualName.lastIndexOf('[') > 0) {
            actualName = actualName.substring(0, actualName.lastIndexOf('['));
            arraydepth++;
        }
        if (arraydepth > 0) {
            if ("boolean".equals(actualName)) {
                actualName = "Z";
            } else if ("byte".equals(actualName)) {
                actualName = "B";
            } else if ("char".equals(actualName)) {
                actualName = "C";
            } else if ("double".equals(actualName)) {
                actualName = "D";
            } else if ("float".equals(actualName)) {
                actualName = "F";
            } else if ("int".equals(actualName)) {
                actualName = "I";
            } else if ("long".equals(actualName)) {
                actualName = "J";
            } else if ("short".equals(actualName)) {
                actualName = "S";
            } else {
                actualName = "L" + actualName + ";";
            }
            char[] da = new char[arraydepth];
            java.util.Arrays.fill(da, '[');
            actualName = new String(da) + actualName;
        }
        if (actualName.equals(boolean.class.getName())) {
            return boolean.class;
        } else if (actualName.equals(byte.class.getName())) {
            return byte.class;
        } else if (actualName.equals(char.class.getName())) {
            return char.class;
        } else if (actualName.equals(double.class.getName())) {
            return double.class;
        } else if (actualName.equals(float.class.getName())) {
            return float.class;
        } else if (actualName.equals(int.class.getName())) {
            return int.class;
        } else if (actualName.equals(long.class.getName())) {
            return long.class;
        } else if (actualName.equals(short.class.getName())) {
            return short.class;
        }
        return loadClass(actualName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(datatypeName);
        sb.append('(');
        sb.append(attributeType);
        sb.append(", ");
        sb.append(valueSetType);
        if (isProductRelevant) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

    public Object getValue(IModelObject source) {
        try {
            if (AttributeType.CONSTANT == attributeType) {
                Field field = source.getClass().getField(getName().toUpperCase());
                return field.get(source);
            }
            return getGetter(source).invoke(source);
        } catch (IntrospectionException e) {
            handleGetterError(source, e);
        } catch (IllegalArgumentException e) {
            handleGetterError(source, e);
        } catch (IllegalAccessException e) {
            handleGetterError(source, e);
        } catch (InvocationTargetException e) {
            handleGetterError(source, e);
        } catch (SecurityException e) {
            handleGetterError(source, e);
        } catch (NoSuchFieldException e) {
            handleGetterError(source, e);
        }
        return null;
    }

    private Method getGetter(IModelObject source) throws IntrospectionException {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(getName(), source.getClass(), getGetterName(),
                null);
        return propertyDescriptor.getReadMethod();
    }

    private String getGetterName() {
        if (getterName == null) {
            getterName = "get" + getName().substring(0, 1).toUpperCase() + getName().substring(1);
        }
        return getterName;
    }

    public void setValue(IModelObject source, Object value) {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(getName(), source.getClass());
            propertyDescriptor.getWriteMethod().invoke(source, value);
        } catch (IntrospectionException e) {
            handleSetterError(source, value, e);
        } catch (IllegalArgumentException e) {
            handleSetterError(source, value, e);
        } catch (IllegalAccessException e) {
            handleSetterError(source, value, e);
        } catch (InvocationTargetException e) {
            handleSetterError(source, value, e);
        }
    }

    private void handleGetterError(IModelObject source, Exception e) {
        throw new IllegalArgumentException(String.format("Could not get attribute %s on source object %s.", getName(),
                source), e);
    }

    private void handleSetterError(IModelObject source, Object value, Exception e) {
        throw new IllegalArgumentException(String.format(
                "Could not write attribute %s on source object %s to value %s.", getName(), source, value), e);
    }

}
