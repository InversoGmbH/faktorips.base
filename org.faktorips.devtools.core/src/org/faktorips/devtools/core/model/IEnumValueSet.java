package org.faktorips.devtools.core.model;

import org.faktorips.datatype.EnumDatatype;

/**
 * EnumSet represents a value set of discrete values, each value has to be explicitly defined.
 * 
 * @author Thorsten Guenther
 */
public interface IEnumValueSet extends IValueSet {

	/**
	 * Prefix for all message codes of this class.
	 */
	public final static String MSGCODE_PREFIX = "ENUMVALUESET-"; //$NON-NLS-1$

	/**
	 * Validation message code to indicate that a value in this value set is duplicate.
	 */
	public final static String MSGCODE_DUPLICATE_VALUE = MSGCODE_PREFIX
			+ "DuplicateValue"; //$NON-NLS-1$

	/**
	 * Returns an array of all Elements in the EnunValueSet
	 */
	public String[] getValues();

	/**
	 * Adds the value to the set. Duplicate values are allowed but will lead to a
	 * message if validated.
	 */
	public void addValue(String val);

	/**
	 * Removes the value at the given index from the value set.
	 * @throws IndexOutOfBoundsException if the given index is out of bounds.
	 */
	public void removeValue(int index);

	/**
	 * Retrieves the value at the given index.
	 * @throws IndexOutOfBoundsException if the given index is out of bounds.
	 */
	public String getValue(int index);

	/**
	 * Sets the value at the given index.
	 * @throws IndexOutOfBoundsException if the given index is out of bounds.
	 */
	public void setValue(int index, String value);

	/**
	 * Returns the number of values in the set.
	 */
	public int size();

	/**
	 * Returns all values contained in the given other value set but not in 
	 * this one.
	 * @param otherSet The set to take the values from to find in this one.
	 * @return An string array representing the values not contained.
	 */
	public String[] getValuesNotContained(IEnumValueSet otherSet);

	/**
	 * Adds all values from the given datatype to the value set.
	 */
	public void addValuesFromDatatype(EnumDatatype datatype);

}