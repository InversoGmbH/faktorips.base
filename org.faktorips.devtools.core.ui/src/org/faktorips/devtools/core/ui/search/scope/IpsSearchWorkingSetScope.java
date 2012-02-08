/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ui.IWorkingSet;

/**
 * scope for working sets
 * 
 * @author dicker
 */
public class IpsSearchWorkingSetScope extends AbstractIpsSearchScope {

    private final IWorkingSet[] workingSets;

    public IpsSearchWorkingSetScope(IWorkingSet[] workingSets) {
        this.workingSets = workingSets;
    }

    @Override
    protected List<?> getSelectedObjects() {
        List<Object> selectedObjects = new ArrayList<Object>();

        for (IWorkingSet workingSet : workingSets) {
            selectedObjects.addAll(Arrays.asList(workingSet.getElements()));
        }

        return selectedObjects;
    }

    @Override
    protected String getScopeTypeLabel(boolean singular) {
        return singular ? Messages.IpsSearchWorkingSetScope_scopeTypeLabelSingular
                : Messages.IpsSearchWorkingSetScope_scopeTypeLabelPlural;
    }

    @Override
    protected List<String> getNamesOfSelectedObjects() {
        List<String> namesOfSelectedObjects = new ArrayList<String>();

        for (IWorkingSet workingSet : workingSets) {
            namesOfSelectedObjects.add(workingSet.getName());
        }

        return namesOfSelectedObjects;
    }

}
