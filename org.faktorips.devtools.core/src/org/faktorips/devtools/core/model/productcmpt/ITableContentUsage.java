/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;

/**
 * 
 * @author Thorsten Guenther
 */
public interface ITableContentUsage extends IPropertyValue {
    public static final String PROPERTY_STRUCTURE_USAGE = "structureUsage"; //$NON-NLS-1$

    public static final String PROPERTY_TABLE_CONTENT = "TableContentName"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TABLECONTENT-USAGE"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the given structure usage is not known.
     */
    public final static String MSGCODE_UNKNOWN_STRUCTURE_USAGE = MSGCODE_PREFIX + "UnknownStructureUsage"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the given table content was not found.
     */
    public final static String MSGCODE_UNKNOWN_TABLE_CONTENT = MSGCODE_PREFIX + "UnknownTableContent"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the given table content does not match the needs of
     * the structure
     */
    public final static String MSGCODE_INVALID_TABLE_CONTENT = MSGCODE_PREFIX + "InvalidTableContent"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product component type was not found.
     */
    public final static String MSGCODE_NO_TYPE = MSGCODE_PREFIX + "NoProductCmptType"; //$NON-NLS-1$

    /**
     * Returns the generation this formula belongs to.
     */
    public IProductCmptGeneration getProductCmptGeneration();

    /**
     * Set the role name of the table structure usage implemented by this content usage.
     * 
     * @param structureUsageRolename The role name of the table structure usage referred to by this
     *            content usage.
     */
    public void setStructureUsage(String structureUsageRolename);

    /**
     * Returns the role name of the table structure usage implemented by this content usage.
     */
    public String getStructureUsage();

    /**
     * Set the name of the table content used by this table content usage.
     * 
     * @param tableContentName The fully qualified name of the used table content.
     */
    public void setTableContentName(String tableContentName);

    /**
     * @return The fully qualified name of the used table content.
     */
    public String getTableContentName();

    /**
     * Returns the table contents which is related or <code>null</code> if the table contents can't
     * be found.
     * 
     * @throws CoreException if an error occurs while searching for the table contents.
     */
    public ITableContents findTableContents(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the related table structure usage or <code>null</code> if the table contents can't be
     * found.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the table structure usage.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public ITableStructureUsage findTableStructureUsage(IIpsProject ipsProject) throws CoreException;

}
