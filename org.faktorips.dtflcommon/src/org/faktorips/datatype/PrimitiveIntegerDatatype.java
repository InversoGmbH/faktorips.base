/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.datatype;

/**
 * Datatype for the primitive <code>int</code>.
 */
public class PrimitiveIntegerDatatype extends AbstractPrimitiveDatatype implements NumericDatatype {

    public String getName() {
        return "int"; //$NON-NLS-1$
    }

    public String getQualifiedName() {
        return "int"; //$NON-NLS-1$
    }

    public ValueDatatype getWrapperType() {
        return Datatype.INTEGER;
    }

    public String getJavaClassName() {
        return "int"; //$NON-NLS-1$
    }

    public String getDefaultValue() {
        return "0"; //$NON-NLS-1$
    }

    @Override
    public Object getValue(String value) {
        return Integer.valueOf(value);
    }

    public boolean supportsCompare() {
        return true;
    }

    public String subtract(String minuend, String subtrahend) {
        if (minuend == null || subtrahend == null) {
            throw new NullPointerException("Minuend and subtrahend both can not be null."); //$NON-NLS-1$
        }
        int result = ((Integer)getValue(minuend)).intValue() - ((Integer)getValue(subtrahend)).intValue();
        return Integer.toString(result);
    }

    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null."); //$NON-NLS-1$
        }
        int a = ((Integer)getValue(dividend)).intValue();
        int b = ((Integer)getValue(divisor)).intValue();
        return b == 0 ? false : a % b == 0;
    }

    public boolean hasDecimalPlaces() {
        return false;
    }

}
