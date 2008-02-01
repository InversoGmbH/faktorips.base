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

package org.faktorips.devtools.core.model.valueset;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.util.message.MessageList;

/**
 * A ValueSet is the specification of a set of values. It is asumed that all values in a ValueSet are of the same
 * datatype.
 * <p>
 * Values in the set are represented by strings so that we don't have to deal with type conversion
 * when the datatype changes. E.g. If an attributes datatype is changed by the user from Decimal to Money,
 * lower bound and upper bound from a range value set become invalid (if they were valid before) but the 
 * string values remain. The user can switch back the datatype to Decimal and the range is valid again. 
 * This works also when the attribute's datatype is unknown.
 * 
 * @author Thorsten Guenther
 * @author Jan Ortmann
 */
public interface IValueSet extends IIpsObjectPart {
    
	public static final String PROPERTY_CONTAINS_NULL = "containsNull"; //$NON-NLS-1$
	
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "VALUESET-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value is not contained in the value set.
     */
    public final static String MSGCODE_VALUE_NOT_CONTAINED = MSGCODE_PREFIX + "ValueNotContained"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value-subset is not of the correct type.
     */
    public final static String MSGCODE_TYPE_OF_VALUESET_NOT_MATCHING = MSGCODE_PREFIX + "TypeOfValueSetNotMatching"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value-subset is of a different datatype.
     */
    public final static String MSGCODE_DATATYPES_NOT_MATCHING = MSGCODE_PREFIX + "DatatypesNotMatching"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that one value-set is not a subset of another one.
     */
    public final static String MSGCODE_NOT_SUBSET = MSGCODE_PREFIX + "NotSubset"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the value could not be parsed.
     */
    public final static String MSGCODE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX + "ValueNotParsable"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the lower bound of the subset is less than the lower
     * bound of this value set. 
     */
    public final static String MSGCODE_UNKNOWN_DATATYPE = MSGCODE_PREFIX + "UnknownDatatype"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the datatype does not support comparision for its values.
     */
    public final static String MSGCODE_DATATYPE_NOT_COMPARABLE = MSGCODE_PREFIX + "DatatypeNotComparable"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the datatype this value set is based on does not support
     * <code>null</code>-values, but this valueset is marked to contain a <code>null</code>-value. 
     */
    public final static String MSGCODE_NULL_NOT_SUPPORTED = MSGCODE_PREFIX + "NullNotSupported"; //$NON-NLS-1$
    
    /**
     * Returns the type of the value set.
     */
    public ValueSetType getValueSetType();
    
    /**
     * Returns <code>true</code> if the value set contains the indicated value, otherwise <code>false</code>.
     * 
     * @param value The value to check.
     * 
     * @throws NullPointerException if datatype is <code>null</code>. 
     */
    public boolean containsValue(String value);

    /**
     * Returns <code>true</code> it the value set contains the indicated value, otherwise <code>false</code>.
     * A message is stored in the message list, if the value set doesn't contain the indicated value.
     * 
     * @param value The value to check.
     * @param list The list to add messages, if any (might not be <code>null</code>).
     * @param invalidObject The object the message refers to. Can be <code>null</code>. 
     * @param invalidProperty The property of the object the message refers to. Ignored if 
     *                        <code>invalidObject</code> is <code>null</code>.
     *                        Can be <code>null</code> itself.
     * @throws NullPointerException if list is <code>null</code>. 
     */
    public boolean containsValue(String value, 
			MessageList list, Object invalidObject, String invalidProperty);
    
    /**
     * Returns <code>true</code> if this valueset contains the other valueset, otherwise <code>false</code>.
     * 
     * @param subset The valueset to check.
     * 
     * @throws NullPointerException if subset or datatype is <code>null</code>. 
     */
    public boolean containsValueSet(IValueSet subset);
    
    /**
     * Returns <code>true</code> if this valueset contains the other valueset, otherwise <code>false</code>.
     * 
     * @param subset The valueset to check.
     * @param list The list to which a message is added in case the given valueset is not a subset of this valueset.
     * @param invalidObject The object the message refers to. Can be <code>null</code> . 
     * @param invalidProperty The property of the object the message refers to. Ignored if 
     *                        <code>invalidObject</code> is <code>null</code>.
     *                        Can be <code>null</code> itself.
     * 
     * @throws NullPointerException if subset or list is <code>null</code>. 
     */
    public boolean containsValueSet(IValueSet subset,  
    		MessageList list, Object invalidObject, String invalidProperty);

    /**
     * Creates a copy of this value set (type and values, parent and id are set
     * to the given values).
     */
    public IValueSet copy(IIpsObjectPart newParent, int id);

    /**
     * Copy all values (not the parent or the id) of the given source to this value set.
     * @throws IllegalArgumentException if the given target is not the same type
     * this method is invoked.
     */
    public void setValuesOf(IValueSet source);
    
    /**
     * Returns the unqualified, human readable representation of this value set.
     * If the datatype provided by the parent supports named values, the names provided
     * from the datatype are used to build the value-representations.
     */
    public String toShortString();
    
    /**
    * Returns true if this value set contains the null-value, false if not.
    */
    public boolean getContainsNull();

    /**
    * Adds or removes the null-value from this value set.
    * @param containsNull <code>true</code> to add the null-value to this value set
    * or <code>false</code> to remove it.
    * @throws UnsupportedOperationException if the underlying datatype does not support null values.
    */
    public void setContainsNull(boolean containsNull);
}
