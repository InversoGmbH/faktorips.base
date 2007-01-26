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

package org.faktorips.fl;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;

/**
 * Datatype for the test enum.
 * 
 * @author Jan Ortmann
 */
public class TestEnumDatatype extends AbstractDatatype implements EnumDatatype {

    public final static EnumDatatype INSTANCE = new TestEnumDatatype();
    
    private TestEnumDatatype() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAllValueIds(boolean includeNull) {
        return new String[]{"MONTH", "YEAR"};
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSupportingNames() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getValueName(String id) {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isParsable(String value) {
        return value.equals("MONTH") || value.equals(("YEAR"));
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(String value) {
        if ("MONTH".equals(value)) {
            return TestEnum.MONTH;
        }
        if ("YEAR".equals(value)) {
            return TestEnum.YEAR;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String valueToString(Object value) {
        return value.toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNull(Object value) {
        return value==null;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "TestEnumDatatype";
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        return "TestEnumDatatype";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValueDatatype() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return TestEnum.class.getName();
    }

    public boolean isNull(String value) {
        return false;
    }

    public boolean supportsCompare() {
        return false;
    }

    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        return 0;
    }

    public boolean areValuesEqual(String valueA, String valueB) {
        return false;
    }

}
