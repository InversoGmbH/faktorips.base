/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;

/**
 * A link between two product components. A link is an instance of an association between product
 * component types.
 * 
 * @see IProductCmptTypeAssociation
 */
public interface IProductCmptLink extends IIpsObjectPart, IDescribedElement {

    public final static String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    public final static String PROPERTY_ASSOCIATION = "association"; //$NON-NLS-1$
    public final static String PROPERTY_MIN_CARDINALITY = "minCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_DEFAULT_CARDINALITY = "defaultCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_MAX_CARDINALITY = "maxCardinality"; //$NON-NLS-1$

    public final static int CARDINALITY_MANY = IAssociation.CARDINALITY_MANY;

    /**
     * The name of the XML-tag used if this object is saved to XML.
     */
    public final static String TAG_NAME = "Link"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "PRODUCTCMPT_RELATION-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the association in the model can't be found.
     */
    public final static String MSGCODE_UNKNWON_ASSOCIATION = MSGCODE_PREFIX + "UnknownAssociation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target product component does not exist.
     */
    public final static String MSGCODE_UNKNWON_TARGET = MSGCODE_PREFIX + "UnknownTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is missing.
     */
    public final static String MSGCODE_MISSING_MAX_CARDINALITY = MSGCODE_PREFIX + "MissingMaxCardinality"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than 1.
     */
    public final static String MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1 = MSGCODE_PREFIX + "MaxCardinalityIsLessThan1"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality exceeds the maximum
     * cardinality defined in the model.
     */
    public final static String MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX = MSGCODE_PREFIX
            + "MaxCardinalityExceedsModelMax"; //$NON-NLS-1$

    /**
     * Validation message code to indicate minimum cardinality falls below the minimum cardinality
     * defined in the model.
     */
    public final static String MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN = MSGCODE_PREFIX
            + "MinCardinalityFallsBelowModelMin"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the default cardinality is less than the min- or
     * greater than the max cardinality.
     */
    public final static String MSGCODE_DEFAULT_CARDINALITY_OUT_OF_RANGE = MSGCODE_PREFIX
            + "DefaultCardinalityOutOfRange"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than the minimum
     * cardinality.
     */
    public final static String MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN = MSGCODE_PREFIX
            + "MaxCardinalityIsLessThanMin"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than the minimum
     * cardinality.
     */
    public final static String MSGCODE_INVALID_TARGET = MSGCODE_PREFIX + "InvalidTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate a mismatch with the specification in the product
     * component type association. A message with this message code is added if this link is part of
     * a container that changes over time but the product component type association is defined as
     * static (not changing over time), et vice versa.
     * 
     * @since 3.8
     */
    public final static String MSGCODE_CHANGING_OVER_TIME_MISMATCH = MSGCODE_PREFIX + "ChangingOverTimeMismatch"; //$NON-NLS-1$

    /**
     * Returns the product component this configuration element belongs to.
     */
    public IProductCmpt getProductCmpt();

    /**
     * Returns the product component generation this configuration element belongs to. Returns
     * <code>null</code> if this link is part of the product component (it does not change over
     * time).
     * 
     * @deprecated As of 3.8 {@link IProductCmptLink product component links} can be part of both
     *             {@link IProductCmpt product components} and {@link ProductCmptGeneration product
     *             component generations}. Use {@link #getProductCmptLinkContainer()} and the common
     *             interface {@link IProductCmptLinkContainer} instead.
     */
    @Deprecated
    public IProductCmptGeneration getProductCmptGeneration();

    /**
     * Returns the {@link IProductCmptLinkContainer link container} this link is a part of.
     * 
     * @since 3.8
     * @see IProductCmptLinkContainer
     */
    public IProductCmptLinkContainer getProductCmptLinkContainer();

    /**
     * Returns the name of the product component type association this link is an instance of.
     */
    public String getAssociation();

    /**
     * Setting the association this link is an instance of
     */
    public void setAssociation(String association);

    /**
     * Finds the product component type association this link is an instance of. Note that the
     * method searches not only the direct product component type this product component is based
     * on, but also it's super type hierarchy.
     * 
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @return the association or <code>null</code> if no such association exists.
     * 
     * @throws CoreException if an exception occurs while searching the relation.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public IProductCmptTypeAssociation findAssociation(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the target product component.
     */
    public String getTarget();

    /**
     * Sets the target product component.
     */
    public void setTarget(String newTarget);

    /**
     * Returns the product component which is the target of this association or <code>null</code>,
     * if no (valid) target name is set.
     * 
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this component is part of.
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public IProductCmpt findTarget(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the minimum number of target instances required in this relation.
     */
    public int getMinCardinality();

    /**
     * Sets the minimum number of target instances required in this relation.
     */
    public void setMinCardinality(int newValue);

    /**
     * returns the default number of target instances in this relation.
     */
    public int getDefaultCardinality();

    /**
     * Sets the default number of target instances in this relation.
     */
    public void setDefaultCardinality(int newValue);

    /**
     * Returns the maximum number of target instances allowed in this relation. If the number is not
     * limited CARDINALITY_MANY is returned.
     */
    public int getMaxCardinality();

    /**
     * Sets the maximum number of target instances allowed in this relation. An unlimited number is
     * represented by CARDINALITY_MANY.
     */
    public void setMaxCardinality(int newValue);

    /**
     * Returns true if the association this link is an instance of does constrains a policy
     * component type association
     * 
     * @see IProductCmptTypeAssociation#constrainsPolicyCmptTypeAssociation(IIpsProject)
     */
    public boolean constrainsPolicyCmptTypeAssociation(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns whether this relation is mandatory. A Relation is mandatory if both minimum and
     * maximum-cardinality are equal to 1.
     */
    public boolean isMandatory();

    /**
     * Returns whether this Relation is optional. A Relation is optional if the minimum cardinality
     * equals 0 and the maximum cardinality equals 1.
     * 
     * @return <code>true</code> if this Relation is optional, else <code>false</code>.
     */
    public boolean isOptional();

    /**
     * Returns <code>true</code> if this is a to-many association. This is the case if the max
     * cardinality is greater than 1.
     */
    public boolean is1ToMany();

    /**
     * Checks if this link is a link of the given association. This includes the search for derived
     * union associations if the link's is a subset of a derived union.
     * 
     * @param association The association that should be checked
     * @param ipsProject The project used to find the associations
     * @return true if this link is a link instance of the given association including search for
     *         derived unions.
     * 
     * @throws CoreException Throws a core exception if there occurs exception during finding other
     *             objects
     */
    boolean isLinkOfAssociation(IAssociation association, IIpsProject ipsProject) throws CoreException;

}
