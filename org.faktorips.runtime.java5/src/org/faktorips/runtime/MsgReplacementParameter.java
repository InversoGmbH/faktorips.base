/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime;

import java.io.Serializable;

/**
 * When creating a message the text might be created by replacing parameters (or placeholders) with
 * concrete values, e.g. "The sum insured must be at least {minSumInsured}." where {minSumInsured}
 * is replaced with the current minimum e.g. 200 Euro. If you need to represent the user a different
 * text, you need the actual value for the parameter. To archieve this the message holds the
 * parameters along with their actual value.
 * <p>
 * The following are scenarios where you might need to present a different text for a message:
 * <ul>
 * <li>You have limited space available for the text, for example if your display is a terminal.</li>
 * <li>You present the text to a different user group, e.g. internet users instead of your
 * backoffice employees.</li>
 * </ul>
 * 
 * @author Jan Ortmann
 */
public class MsgReplacementParameter implements Serializable {

    private static final long serialVersionUID = -4588558762246019241L;

    private String name;
    private Object value;

    /**
     * Creates a new parameter value with name and value.
     * 
     * @throws NullPointerException if paramName is null.
     */
    public MsgReplacementParameter(String paramName, Object paramValue) {
        if (paramName == null) {
            throw new NullPointerException();
        }
        name = paramName;
        value = paramValue;
    }

    /**
     * Returns the parameter's name. This method never returns <code>null</code>.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parameter's value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MsgReplacementParameter)) {
            return false;
        }
        MsgReplacementParameter other = (MsgReplacementParameter)o;
        return name.equals(other.name)
                && ((value == null && other.value == null) || (value != null && value.equals(other.value)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name + "=" + value;
    }

}