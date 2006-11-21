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

package org.faktorips.devtools.core.model.pctype;

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

/**
 * Describes the kind of attribute. 
 */
public class AttributeType extends DefaultEnumValue {
    
    /**
     * Defines an attribute as being changeable per policy component instance. 
     */
    public final static AttributeType CHANGEABLE;

    /**
     * Defines an attribute as being computed. In contrast to a derived attribute a computed 
     * attribute is computed by an explicit method call. E.g. a method calculatePremium() might 
     * calculate severall computed attributes like netPremium and grossPremium. The computed 
     * attributes keep their computed value until their are recalculated by another methodcall.
     * <p>
     * If a computed attribute is also product relevant the computation formula can be defined by the
     * product developer. The IT developer defines the paramters that the product developer
     * can use.
     */
    public final static AttributeType DERIVED_BY_EXPLICIT_METHOD_CALL;

    /**
     * Defines an attribute as being derived, that means the attrbutes value can
     * be derived from other attribute values. In contrast to computed attributes
     * the value of derived attributes are always calculated on the fly. E.g. the
     * gross premium could be derived on the fly from the net premium and the tax. 
     * <p>
     * If a derived attribute is product relevant the computation formula can be defined 
     * by the product developer. The IT developer defines the paramters that the product developer
     * can use. 
     */
    public final static AttributeType DERIVED_ON_THE_FLY;
    
    /**
     * Defines an attribute as being constant for all policy components that
     * are based on the same product. 
     */
    public final static AttributeType CONSTANT;
    
    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("AttributeType", AttributeType.class); //$NON-NLS-1$
        CHANGEABLE = new AttributeType(enumType, "changeable", Messages.getString("AttributeType.changeable")); //$NON-NLS-1$ //$NON-NLS-2$
        CONSTANT = new AttributeType(enumType, "constant", Messages.getString("AttributeType.constant")); //$NON-NLS-1$ //$NON-NLS-2$
        DERIVED_BY_EXPLICIT_METHOD_CALL = new AttributeType(enumType, "computed", Messages.getString("AttributeType.derived-by-explicit-method-call")); //$NON-NLS-1$ //$NON-NLS-2$
        DERIVED_ON_THE_FLY = new AttributeType(enumType, "derived", Messages.getString("AttributeType.derived-on-the-fly")); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    public final static AttributeType getAttributeType(String id) {
        if (id.equals("changable")) { //$NON-NLS-1$
            return CHANGEABLE; // migration of old files
        }
        return (AttributeType)enumType.getEnumValue(id);
    }
    
    /**
     * Returns <code>true</code> if this is one of the derived type,
     * otherwise <code>false</code>.
     */
    public boolean isDerived() {
        return this==DERIVED_BY_EXPLICIT_METHOD_CALL || this==DERIVED_ON_THE_FLY;
    }
    
    private AttributeType(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }
    
}
