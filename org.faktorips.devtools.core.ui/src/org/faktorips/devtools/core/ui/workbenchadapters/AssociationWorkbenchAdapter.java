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

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;

public class AssociationWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAssociation) {
            IAssociation association = (IAssociation)ipsObjectPart;
            String baseName = ""; //$NON-NLS-1$
            if (association.getAssociationType() == AssociationType.AGGREGATION) {
                baseName = "AssociationType-Aggregation.gif"; //$NON-NLS-1$
            } else if (association.getAssociationType() == AssociationType.ASSOCIATION) {
                baseName = "AssociationType-Association.gif"; //$NON-NLS-1$
            } else if (association.getAssociationType() == AssociationType.COMPOSITION_DETAIL_TO_MASTER) {
                baseName = "AssociationType-CompositionDetailToMaster.gif"; //$NON-NLS-1$
            } else if (association.getAssociationType() == AssociationType.COMPOSITION_MASTER_TO_DETAIL) {
                baseName = "AssociationType-Composition.gif"; //$NON-NLS-1$
            }
            if (association instanceof IPolicyCmptTypeAssociation) {
                try {
                    IPolicyCmptTypeAssociation polAssociation = (IPolicyCmptTypeAssociation)association;
                    if (polAssociation.isConfigured()
                            && polAssociation.isConstrainedByProductStructure(association.getIpsProject())) {
                        return IpsUIPlugin.getImageHandling().getSharedOverlayImage(baseName, OverlayIcons.PRODUCT_OVR,
                                IDecoration.TOP_RIGHT);
                    }
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }
            return IpsUIPlugin.getImageHandling().getSharedImageDescriptor(baseName, true);
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("AssociationType-Association.gif", true); //$NON-NLS-1$
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAssociation) {
            IAssociation association = (IAssociation)ipsObjectPart;
            if (association.is1ToMany()) {
                return association.getTargetRolePlural();
            }
            return association.getTargetRoleSingular();
        } else {
            return super.getLabel(ipsObjectPart);
        }
    }

}
