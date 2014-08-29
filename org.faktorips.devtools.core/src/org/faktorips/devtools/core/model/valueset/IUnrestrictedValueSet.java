/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.valueset;

import org.faktorips.datatype.ValueDatatype;

/**
 * Represents a value set that does not restrict the values defined by a value data type.
 * 
 * @see ValueDatatype
 * 
 * @author Thorsten Guenther
 */
public interface IUnrestrictedValueSet extends IValueSet {

    /**
     * Returns <tt>true</tt> if this {@link IUnrestrictedValueSet} contains null
     */
    public boolean isContainsNull();

    /**
     * Sets whether this {@link IUnrestrictedValueSet} contains null
     */
    public void setContainsNull(boolean containsNull);

}
