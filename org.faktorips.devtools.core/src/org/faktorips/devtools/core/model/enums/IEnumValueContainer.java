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

package org.faktorips.devtools.core.model.enums;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * This is the published interface for <code>EnumValueContainer</code>.
 * <p>
 * <code>EnumValueContainer</code> is the supertype for <code>EnumType</code> and
 * <code>EnumContent</code>. This is because in Faktor-IPS the values of an enumeration can be
 * defined directly in the enum type or separate from it by the product side.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumValueContainer extends IIpsObject {

    /**
     * Returns a list containing all enum values that belong to this enum value container.
     */
    public List<IEnumValue> getEnumValues();

    /**
     * Creates and returns a new enum value that has as many enum attribute values as the
     * corresponding enum type has attributes.
     * 
     * @throws CoreException If an error occurs while searching for the enum type.
     */
    public IEnumValue newEnumValue() throws CoreException;

    /**
     * Returns a reference to the enum type or <code>null</code> if no enum type can be found.
     * 
     * @throws CoreException If an error occures while searching the ips model for the enum type.
     */
    public IEnumType findEnumType() throws CoreException;

    /**
     * Returns how many enum values this enum value container currently contains.
     */
    public int getEnumValuesCount();

    /**
     * Moves the given enum value one position up / down in the containing list and returns its new
     * index.
     * <p>
     * If the given enum value is already the first / last enum value then nothing will be done.
     * 
     * @param enumValue The enum value to move.
     * @param up Flag indicating whether to move up (<code>true</code>) or down (<code>false</code>
     *            ).
     * 
     * @throws CoreException If an error occurs while moving the enum value.
     * @throws NullPointerException If <code>enumValue</code> is <code>null</code>.
     * @throws NoSuchElementException If the given enum value is not contained in this enum value
     *             container.
     */
    public int moveEnumValue(IEnumValue enumValue, boolean up) throws CoreException;

    /**
     * Returns the index of the given enum value in the containing list.
     * 
     * @param enumValue The enum value to obtain its index for.
     * 
     * @throws NoSuchElementException If there is no such enum value in this enum value container.
     * @throws NullPointerException If <code>enumValue</code> is <code>null</code>.
     */
    public int getIndexOfEnumValue(IEnumValue enumValue);

}
