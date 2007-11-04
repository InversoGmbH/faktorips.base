/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.model.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * 
 */
public interface IColumn extends IIpsObjectPart, IKeyItem {

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "ATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the column's datatype is a primitive, (but
     * primitives aren't supported.)
     */
    public final static String MSGCODE_DATATYPE_IS_A_PRIMITTVE = MSGCODE_PREFIX + "DatatypeIsAPrimitive"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the column's datatype is a primitive, (but
     * primitives aren't supported.)
     */
    public final static String MSGCODE_INVALID_NAME = MSGCODE_PREFIX + "InvalidName"; //$NON-NLS-1$

    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /**
     * Sets the column name.
     * 
     * @throws IllegalArgumentException if newName is <code>null</code>.
     */
    public void setName(String newName);

    /**
     * Sets the column's datatype.
     * 
     * @throws IllegalArgumentException if newDatatype is <code>null</code>.
     */
    public void setDatatype(String newDatatype);

    /**
     * Returns the valuedatatype of this column. 
     * @return
     * @throws CoreException 
     */
    public ValueDatatype findValueDatatype() throws CoreException;

}
