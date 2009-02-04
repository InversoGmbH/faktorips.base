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

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;

/**
 * Marks an object as being validatable.
 */
public interface Validatable {

    /**
     * Returns <code>true</code> if this object does not contain any errors, otherwise
     * <code>false</code>. If the method returns <code>false</code> the method
     * <code>validate()</code> returns at least one error message.
     * 
     * @see #validate(IIpsProject)
     * 
     * @return Flag indicating whether this object is valid.
     * 
     * @throws CoreException If an exception occurs while validating the object.
     */
    public boolean isValid() throws CoreException;

    /**
     * Returns the resulting severity of the validation. The returned severity is equal to the
     * severity of the message list returned by the validate() method.
     * 
     * @see #validate(IIpsProject)
     * 
     * @return Identification number of the resulting validation severity.
     * 
     * @throws CoreException If an exception occurs while obtaining the resulting validation
     *             severity.
     */
    public int getValidationResultSeverity() throws CoreException;

    /**
     * <p>
     * Validates the object and all of it's parts.
     * </p>
     * <p>
     * Note that validations will be cached. The validation cache can be cleared trough the IpsModel.
     * </p>
     * 
     * @see org.faktorips.devtools.core.model.IIpsModel#clearValidationCache()
     * 
     * @param ipsProject The context ips project. The validation might be called from a different
     *            ips project than the actual instance of this validatable belongs to. In this case
     *            it is necessary to use the ips project of the caller for finder-methods that are
     *            used within the implementation of this method.
     * 
     * @return A ValidationMessageList containing a list of messages describing errors, warnings and
     *         information. If no messages are created, an empty list is returned.
     * 
     * @throws CoreException If an exception occurs while validating the object.
     */
    public MessageList validate(IIpsProject ipsProject) throws CoreException;
}
