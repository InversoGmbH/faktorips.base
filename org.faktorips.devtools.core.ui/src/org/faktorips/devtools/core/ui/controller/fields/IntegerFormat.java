/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controller.fields;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Format for integer number input.
 * 
 * @author Stefan Widmaier
 */
public class IntegerFormat extends AbstractNumberFormat {

    private NumberFormat numberFormat;

    /**
     * String that is an example of a valid input string.
     */
    private String exampleString;

    @Override
    protected void initFormat(Locale locale) {
        numberFormat = NumberFormat.getIntegerInstance(locale);
        numberFormat.setParseIntegerOnly(true);
        exampleString = numberFormat.format(-100000000);
    }

    @Override
    protected String formatInternal(Object value) {
        Integer integer = new Integer((String)value);
        // MTB#524: Thousand-separator only if the number has at least 5 digits.
        if (integer > 9999) {
            numberFormat.setGroupingUsed(true);
        } else {
            numberFormat.setGroupingUsed(false);
        }
        return numberFormat.format(integer);
    }

    @Override
    protected String getExampleString() {
        return exampleString;
    }

    @Override
    protected NumberFormat getNumberFormat() {
        return numberFormat;
    }

}
