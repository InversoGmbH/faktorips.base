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

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Format for Date input. Maps a locale-specific string to a gregorian calendar and vice versa.
 * <p>
 * If you need a field that maps a locale specific date string to the ISO date format (also string)
 * use a {@link FormattingTextField} with {@link DateISOStringFormat} instead.
 * 
 * @see DateISOStringFormat
 * 
 * @author Stefan Widmaier
 */
public class GregorianCalendarFormat extends AbstractDateFormat<GregorianCalendar> {

    public static GregorianCalendarFormat newInstance() {
        GregorianCalendarFormat format = new GregorianCalendarFormat();
        format.initFormat();
        return format;
    }

    private GregorianCalendarFormat() {
        // only hide the constructor
    }

    @Override
    protected GregorianCalendar mapDateToObject(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar;
    }

    @Override
    protected Date mapObjectToDate(GregorianCalendar value) {
        return value.getTime();
    }

}
