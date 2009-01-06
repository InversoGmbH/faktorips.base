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

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import java.util.Comparator;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;

/**
 * Comparator for <code>ProductCmptCompareItem</code>s. Compares the actual
 * <code>IIpsElement</code>s referenced by each compare item. Sorts
 * <code>IProductCmptGeneration</code>s by their validFrom date. <code>IConfigElement</code>s
 * and <code>ITableContentUsage</code>s are sorted in the following order: product attributes,
 * table usages, formulas, policy attributes. Attributes are placed above relations in each
 * generation. <code>IProductCmptRelations</code> are <em>not</em> sorted, instead their natural
 * order (in the XML file) is maintained.
 * <p>
 * The sorting of <code>ProductCmptCompareItem</code>s is necessary to ensure that differences in
 * product components (their structures) are consistent with differences in the text representation
 * displayed in the <code>ProductCmptCompareViewer</code>. Moreover the representation must be
 * consistent with the ProductCmptEditor.
 * 
 * @see org.faktorips.devtools.core.ui.team.compare.productcmpt.ProductCmptCompareItem
 * 
 * @author Stefan Widmaier
 */
public class ProductCmptCompareItemComparator implements Comparator {
    /**
     * {@inheritDoc}
     */
    public int compare(Object o1, Object o2) {
        if (o1 instanceof ProductCmptCompareItem && o2 instanceof ProductCmptCompareItem) {
            IIpsElement element1 = ((ProductCmptCompareItem)o1).getIpsElement();
            IIpsElement element2 = ((ProductCmptCompareItem)o2).getIpsElement();
            // Sort generations by generation number (and thus chronographically)
            if (element1 instanceof IProductCmptGeneration && element2 instanceof IProductCmptGeneration) {
                return ((IProductCmptGeneration)element1).getGenerationNo()
                        - ((IProductCmptGeneration)element2).getGenerationNo();
            }
            if ((element1 instanceof IPropertyValue) && (element2 instanceof IPropertyValue)) {
                int first= ((IPropertyValue)element1).getPropertyType().getSortOrder();
                int second= ((IPropertyValue)element2).getPropertyType().getSortOrder();
                return first-second;
            }
        }
        return 0;
    }
}
