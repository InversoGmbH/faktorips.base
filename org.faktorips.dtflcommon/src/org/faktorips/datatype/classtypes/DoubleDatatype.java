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

package org.faktorips.datatype.classtypes;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassDatatype;

/**
 * 
 * @author Jan Ortmann
 */
public class DoubleDatatype extends ValueClassDatatype implements NumericDatatype {

    /**
     * @param clazz
     */
    public DoubleDatatype() {
        super(Double.class);
    }

    /**
     * @param clazz
     * @param name
     */
    public DoubleDatatype(String name) {
        super(Double.class, name);
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(String s) {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
		return Double.valueOf(s);
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String subtract(String minuend, String subtrahend) {
        if (minuend == null || subtrahend == null) {
            throw new NullPointerException("Minuend and subtrahend both can not be null.");
        }
        double result = ((Double)getValue(minuend)).doubleValue() - ((Double)getValue(subtrahend)).doubleValue();
        return Double.toString(result);
    }

    /**
     * {@inheritDoc}
     */
    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null.");
        }
        
        BigDecimal a = new BigDecimal(dividend);
        BigDecimal b = new BigDecimal(divisor);

        try {
            a.divide(b, 0, BigDecimal.ROUND_UNNECESSARY);
            return true;
        } catch (ArithmeticException e) {
            return false;
        }
    }
}
