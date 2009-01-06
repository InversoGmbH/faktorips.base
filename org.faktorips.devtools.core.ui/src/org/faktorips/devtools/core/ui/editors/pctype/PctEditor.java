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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.editors.type.TypeEditor;


/**
 * The editor to edit policy component types.
 */
public class PctEditor extends TypeEditor {
    
    public PctEditor() {
        super();
    }
    
    IPolicyCmptType getPolicyCmptType() {
        try {
            return (IPolicyCmptType)getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    /** 
     * {@inheritDoc}
     */
    protected String getUniformPageTitle() {
        return Messages.PctEditor_title + getPolicyCmptType().getName();
    }

    /**
     * {@inheritDoc}
     */
    protected void addAllInOneSinglePage() throws PartInitException {
        addPage(new StructurePage(this, false));
    }

    /**
     * {@inheritDoc}
     */
    protected void addSplittedInMorePages() throws PartInitException {
        addPage(new StructurePage(this, true));
        addPage(new BehaviourPage(this));
    }
}
