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

package org.faktorips.devtools.core.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * 
 * @author Stefan Widmaier
 */
public class ReferencesToPolicySearchQuery extends ReferenceSearchQuery {

	public ReferencesToPolicySearchQuery(IPolicyCmptType referenced) {
		super(referenced);
	}

    /**
     * @inheritDoc
     */
	protected IIpsElement[] findReferences() throws CoreException {
		return referenced.getIpsProject().findReferencingPolicyCmptTypes((PolicyCmptType)referenced);
	}

    /**
     * @inheritDoc
     */
	protected Object[] getDataForResult(IIpsElement object) throws CoreException {
		return new Object[]{object};
	}
}
