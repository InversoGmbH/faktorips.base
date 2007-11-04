/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.model.valueset;

/**
 * Valueset representing a range out of a discrete or continuous set of values.
 * 
 * @author Thorsten Guenther
 */
public interface IRangeValueSet extends IValueSet {

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "RANGE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the lower bound of the subset is less than the lower
     * bound of this value set.
     */
    public final static String MSGCODE_LBOUND_GREATER_UBOUND = MSGCODE_PREFIX + "LBoundGreaterUBound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a step was only defined in this valueset, but not in
     * the subset.
     */
    public final static String MSGCODE_NO_STEP_DEFINED_IN_SUBSET = MSGCODE_PREFIX + "NoStepDefinedInSubset"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the upper bound of the subset is greater than the
     * upper bound of this value set.
     */
    public final static String MSGCODE_UPPER_BOUND_VIOLATION = MSGCODE_PREFIX + "UpperBoundViolation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the lower bound of the subset is less than the lower
     * bound of this value set.
     */
    public final static String MSGCODE_LOWER_BOUND_VIOLATION = MSGCODE_PREFIX + "LowerBoundViolation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the datatype of the attribute this range is based on
     * is not a numeric datatype (ranges are only possible for numeric datatypes).
     */
    public final static String MSGCODE_NOT_NUMERIC_DATATYPE = MSGCODE_PREFIX + "notNumericDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the range definde by the lower and upper bound is
     * not devisible wihtout remainder using the step.
     */
    public final static String MSGCODE_STEP_RANGE_MISMATCH = MSGCODE_PREFIX + "stepRangeMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the step of this range is not divisible without
     * remainder by the step of another range.
     */
    public final static String MSGCODE_STEP_MISMATCH = MSGCODE_PREFIX + "stepMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the lower bound of an other range is not divisible
     * without remainder by the step of this range.
     */
    public final static String MSGCODE_LOWERBOUND_MISMATCH = MSGCODE_PREFIX + "lowerBoundMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the upper bound of an other range is not divisible
     * without remainder by the step of this range.
     */
    public final static String MSGCODE_UPPERBOUND_MISMATCH = MSGCODE_PREFIX + "upperBoundMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a value is not divisible without remainder by the
     * step of this range.
     */
    public final static String MSGCODE_STEP_VIOLATION = MSGCODE_PREFIX + "stepViolation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the step is not parsable.
     */
    public final static String MSGCODE_STEP_NOT_PARSABLE = MSGCODE_PREFIX + "StepNotParsable"; //$NON-NLS-1$

    public final static String PROPERTY_UPPERBOUND = "upperBound"; //$NON-NLS-1$

    public final static String PROPERTY_LOWERBOUND = "lowerBound"; //$NON-NLS-1$

    public final static String PROPERTY_STEP = "step"; //$NON-NLS-1$

    /**
     * Sets the lower bound. An empty string means that the range is unbouned.
     * 
     * @throws NullPointerException if lowerBound is <code>null</code>.
     */
    public void setLowerBound(String lowerBound);

    /**
     * Sets the step. An empty string means that no step exists and all possible values in the range
     * are valid.
     * 
     * @throws NullPointerException if step is <code>null</code>.
     */
    public void setStep(String step);

    /**
     * Sets the upper bound. An empty string means that the range is unbounded.
     * 
     * @throws NullPointerException if upperBound is <code>null</code>.
     */
    public void setUpperBound(String upperBound);

    /**
     * Returns the lower bound of the range
     */
    public String getLowerBound();

    /**
     * Returns the upper bound of the range
     */
    public String getUpperBound();

    /**
     * Returns the step of the range
     */
    public String getStep();

}