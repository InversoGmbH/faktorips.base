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

package org.faktorips.devtools.core.ui.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.search.ui.text.Match;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.testcase.ITestCase;

/**
 * Find references to a given product cmpt. This query searches for product cmpt's and test cases
 * which contains an reference to the given product cmpt.
 * 
 * @author Stefan Widmaier
 */
public class ReferencesToProductSearchQuery extends ReferenceSearchQuery {
    
    public ReferencesToProductSearchQuery(IProductCmpt referenced) {
        super(referenced);
    }
    
    /**
     * @inheritDoc
     */
    protected IIpsElement[] findReferences() throws CoreException {
        IIpsElement[] refProductCmptGenerations = referenced.getIpsProject().findReferencingProductCmptGenerations(
                referenced.getQualifiedNameType());
        IIpsElement[] refTestCases = referenced.getIpsProject().findReferencingTestCases(referenced.getQualifiedName());
        
        List generations = Arrays.asList(refProductCmptGenerations);
        List testCases = Arrays.asList(refTestCases);

        List result = new ArrayList(refProductCmptGenerations.length + refTestCases.length);
        result.addAll(generations);
        result.addAll(testCases);
        return (IIpsElement[]) result.toArray(new IIpsElement[result.size()]);
    }
    
    /**
     * @inheritDoc
     */
    protected Object[] getDataForResult(IIpsElement object) {
        if (object instanceof IProductCmptGeneration){
            return new Object[]{((IProductCmptGeneration)object).getProductCmpt(), object};
        } else if (object instanceof ITestCase) {
            return new Object[]{object};
        }
        return null;
	}
    
    /**
     * {@inheritDoc}
     * 
     *  Combines all generations of the same product cmpt to one match.
     */
    protected void addFoundMatches(IIpsElement[] found) throws CoreException {
        List combinedResult = combineResult(found);
        Match[] resultMatches = new Match[combinedResult.size()];
        int idx = 0;
        for (Iterator iter = combinedResult.iterator(); iter.hasNext();) {
            Object foundElem = iter.next();
            Object[] combined = null;
            if (foundElem instanceof IIpsElement){
                combined = getDataForResult((IIpsElement)foundElem);
            } else if (foundElem instanceof List){
                List foundElemList = (List) foundElem;
                if (foundElemList.size() == 0){
                    throw new CoreException(new IpsStatus("Expected at least one product cmpt generation in the combined references list!"));
                }
                sortGenerationsInList(foundElemList);
                Object currentGeneration = foundElemList.get(0);
                if (!(currentGeneration instanceof IProductCmptGeneration)){
                    throw new CoreException(new IpsStatus("Expected only product cmpt generation in the combined references list!"));
                }
                
                IProductCmpt productCmpt = ((IProductCmptGeneration)currentGeneration).getProductCmpt();
                combined = new IIpsElement[foundElemList.size() + 1];
                combined[0] = productCmpt;
                for (int i = 0; i < foundElemList.size(); i++) {
                    combined[i+1] = foundElemList.get(i);
                }
            } else {
                throw new CoreException(new IpsStatus("Unknown reference type: " + foundElem.getClass().getName()));
            }
            resultMatches[idx++] = new Match(combined, 0, 0);
        }
        result.addMatches(resultMatches);        
    }

    private void sortGenerationsInList(List foundElemList) {
        Collections.sort(foundElemList, new Comparator() {
            public int compare(Object o1, Object o2) {
                if (o1 instanceof IIpsObjectGeneration && o2 instanceof IIpsObjectGeneration) {
                    IIpsObjectGeneration gen1 = (IIpsObjectGeneration)o1;
                    IIpsObjectGeneration gen2 = (IIpsObjectGeneration)o2;
                    if (gen1.getValidFrom() == null) {
                        return gen2.getValidFrom() == null ? 0 : -1;
                    }
                    return gen1.getValidFrom().after(gen2.getValidFrom()) ? 1 : -1;
                }
                return 0;
            }
        });
    }

    /*
     * Combines the given elements: If more than one generation of the same product cmpt are given
     * in the input array then the generation will be combined inside a list and added to the result
     * list which will be returned. All other found elements will be added unchanged to the returned
     * list.
     */
    private List combineResult(IIpsElement[] found) {
        List combinedResult = new ArrayList();
        List foundResult = Arrays.asList(found);
        Collections.sort(foundResult, new ProductCmptGenerationComparator());
        IProductCmpt prevProductCmpt = null;
        List combinedGenerations = new ArrayList();
        for (Iterator iter = foundResult.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (element instanceof IProductCmptGeneration) {
                IProductCmptGeneration currGeneration = (IProductCmptGeneration)element;
                IProductCmpt currProductCmpt = currGeneration.getProductCmpt();
                if (currProductCmpt == prevProductCmpt){
                    combinedGenerations.add(currGeneration);
                } else {
                    if (combinedGenerations.size() > 0){
                        combinedResult.add(combinedGenerations);
                        combinedGenerations = new ArrayList();
                    }
                    combinedGenerations.add(element);
                    prevProductCmpt = currProductCmpt;
                }
            } else {
                prevProductCmpt = null;
                combinedResult.add(element);
            }
        }

        if (combinedGenerations.size() > 0){
            combinedResult.add(combinedGenerations);
        }
        
        return combinedResult;
    }

    private class ProductCmptGenerationComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if (o1 instanceof IProductCmptGeneration && o2 instanceof IProductCmptGeneration) {
                return ((IProductCmptGeneration)o1).getProductCmpt().getQualifiedName().compareTo(
                        ((IProductCmptGeneration)o2).getProductCmpt().getQualifiedName());
            }
            return 0;
        }
    }
    
}
