/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.ArgumentCheck;


/**
 * A completion processor that searchs for associations for a given policy cmpt type.
 */
public class AssociationCompletionProcessor extends AbstractCompletionProcessor {
    
    private IPolicyCmptType pcType;
    
    // indicates that only assoziations and composition should be searched
    private boolean onlyAssoziationOrComposition;
    
    public AssociationCompletionProcessor() {
    }
    
    /**
     * @param pcType The policy cmpt type the associations will be searched for
     * @param onlyAssoziationOrComposition <code>true</code> indicates that only assoziations and
     *            composition should be searched, <code>false</code> all association will be searched
     */
    public AssociationCompletionProcessor(IPolicyCmptType pcType, boolean onlyAssoziationOrComposition) {
        ArgumentCheck.notNull(pcType);
        this.pcType = pcType;
        this.onlyAssoziationOrComposition = onlyAssoziationOrComposition;
        setIpsProject(pcType.getIpsProject());
    }

    /**
     * {@inheritDoc}
     */
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {
        prefix = prefix.toLowerCase();

        IPolicyCmptType currentPcType = pcType;
        while (currentPcType != null){
            IPolicyCmptTypeAssociation[] associations = currentPcType.getPolicyCmptTypeAssociations();
            for (int i = 0; i < associations.length; i++) {
                if (onlyAssoziationOrComposition &&
                    !(associations[i].isAssoziation() || associations[i].isCompositionMasterToDetail())){
                    continue;
                }
                
                if (associations[i].getName().toLowerCase().startsWith(prefix)) {
                    addToResult(result, associations[i], documentOffset);
                }
            }
            currentPcType = (IPolicyCmptType)currentPcType.findSupertype(currentPcType.getIpsProject());
        }
    }
    
    private void addToResult(List result, IPolicyCmptTypeAssociation association, int documentOffset) {
        String name = association.getName();
        String displayText = name + " - " + association.getParent().getName(); //$NON-NLS-1$
        CompletionProposal proposal = new CompletionProposal(
                name, 0, documentOffset, name.length(),  
                association.getImage(), displayText, null, association.getDescription());
        result.add(proposal);
    }
}
