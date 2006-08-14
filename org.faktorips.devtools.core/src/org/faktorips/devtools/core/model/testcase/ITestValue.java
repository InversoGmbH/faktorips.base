/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;

/**
 *  Specification of a test value.
 *  
 * @author Joerg Ortmann
 */
public interface ITestValue extends ITestObject {
	
	/** Property names */
    public final static String PROPERTY_VALUE_PARAMETER = "testValueParameter"; //$NON-NLS-1$
    public final static String PROPERTY_VALUE = "value"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTVALUE-"; //$NON-NLS-1$

    /**
	 * Validation message code to indicate that the corresponding test value parameter not exists.
	 */
	public final static String MSGCODE_TEST_VALUE_PARAM_NOT_FOUND = MSGCODE_PREFIX
		+ "TestValueParamNotFound"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that the datatype of the attribute not exists.
	 */
	public final static String MSGCODE_DATATYPE_NOT_FOUND = MSGCODE_PREFIX
		+ "DatatypeNotFound"; //$NON-NLS-1$
	
    /**
	 * Validation message code to indicate that the datatype value is not parsable.
	 */
	public final static String MSGCODE_DATATYPEVALUE_NOT_PARSABLE = MSGCODE_PREFIX
		+ "DatatypeValueNotParsable"; //$NON-NLS-1$
	
    /**
     * Returns the test value.
     */
	public String getTestValueParameter();
	
    /**
     * Sets the test value.
     */	
	public void setTestValueParameter(String valueParameter);
	
    /**
     * Returns the test value or <code>null</code> if the object does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the test value.
     */		
	public ITestValueParameter findTestValueParameter() throws CoreException;
	
	/**
	 * Returns the value.
	 */
	public String getValue();
	
	/**
	 * Sets the value
	 */
	public void setValue(String newValue);
}
