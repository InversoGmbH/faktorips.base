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

package org.faktorips.devtools.core.enums;

/**
 * Default implementation of enum value.
 */
public class DefaultEnumValue implements EnumValue {
    
    private DefaultEnumType type;
    private String id;
    private String name;
    
    public DefaultEnumValue(DefaultEnumType type, String id) {
        this(type, id, id);
    }

    public DefaultEnumValue(DefaultEnumType type, String id, String name) {
        if (type==null) {
            throw new NullPointerException();
        }
        this.type = type;
        this.id = id;
        this.name = name;
        type.addValue(this);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.enums.EnumValue#getType()
     */
    public EnumType getType() {
        return type;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.enums.EnumValue#getId()
     */
    public String getId() {
        return id;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.enums.EnumValue#getName()
     */
    public String getName() {
        return name;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof EnumValue )) {
            return false;
        }
        EnumValue other = (EnumValue )o;
        return id.equals(other.getId()) && type.equals(other.getType());
    }
    
    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return type.toString() + "." + id;
    }

    /** 
     * Overridden method.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        EnumValue other = (EnumValue )o;
        return id.compareTo(other.getId());
    }

}
