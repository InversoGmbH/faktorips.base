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

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * A reference to a {@link IProductCmptTypeAssociation}. Used by <code>ProductCmptStructure</code> .
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptTypeAssociationReference extends ProductCmptStructureReference implements
        IProductCmptTypeAssociationReference {

    private final IProductCmptTypeAssociation association;

    public ProductCmptTypeAssociationReference(IProductCmptTreeStructure structure,
            ProductCmptStructureReference parent, IProductCmptTypeAssociation association)
            throws CycleInProductStructureException {
        super(structure, parent);
        this.association = association;
    }

    @Override
    public IProductCmptTypeAssociation getAssociation() {
        return association;
    }

    @Override
    public IIpsObjectPart getWrapped() {
        return association;
    }

    @Override
    public IIpsObject getWrappedIpsObject() {
        return null;
    }

    @Override
    public IIpsProject getIpsProject() {
        return getParent().getIpsProject();
    }

}
