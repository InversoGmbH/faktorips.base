/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;

/**
 * This node represents the first part of a enum reference, the part that identifies the enum type.
 * It uses a special datatype implementation {@link EnumClass} of {@link Datatype} to store the
 * found enum type. The successor of a {@link EnumClassNode} have to be always of type
 * {@link EnumValueNode}
 * 
 * @author dirmeier
 */
public class EnumClassNode extends IdentifierNode {

    EnumClassNode(EnumClass datatype) {
        super(datatype);
    }

    @Override
    public EnumClass getDatatype() {
        return (EnumClass)super.getDatatype();
    }

    @Override
    public void setSuccessor(IdentifierNode successor) {
        if (successor instanceof EnumValueNode) {
            super.setSuccessor(successor);
        } else {
            throw new RuntimeException("Invalid successor type in enum class node: " + successor); //$NON-NLS-1$
        }
    }

    @Override
    public EnumValueNode getSuccessor() {
        return (EnumValueNode)super.getSuccessor();
    }

    public static class EnumClass extends AbstractDatatype {

        private static final String CLASS_NAME = Class.class.getSimpleName();

        private final EnumDatatype enumDatatype;

        public EnumClass(EnumDatatype enumDatatype) {
            this.enumDatatype = enumDatatype;
        }

        @Override
        public String getName() {
            return CLASS_NAME + "<" + getEnumDatatype().getName() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public String getQualifiedName() {
            return getName();
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isAbstract() {
            return false;
        }

        @Override
        public boolean isValueDatatype() {
            return false;
        }

        @Override
        public String getJavaClassName() {
            return Class.class.getName();
        }

        public EnumDatatype getEnumDatatype() {
            return enumDatatype;
        }

    }

}