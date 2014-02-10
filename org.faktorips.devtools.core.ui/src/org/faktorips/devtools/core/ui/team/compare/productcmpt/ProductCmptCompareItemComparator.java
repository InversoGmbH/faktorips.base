/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import java.util.Comparator;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItem;

/**
 * Comparator for <code>ProductCmptCompareItem</code>s. Compares the actual <code>IIpsElement</code>
 * s referenced by each compare item. Sorts <code>IProductCmptGeneration</code>s by their validFrom
 * date. <code>IConfigElement</code>s and <code>ITableContentUsage</code>s are sorted in the
 * following order: product attributes, table usages, formulas, policy attributes. Attributes are
 * placed above relations in each generation. <code>IProductCmptRelations</code> are <em>not</em>
 * sorted, instead their natural order (in the XML file) is maintained.
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
public class ProductCmptCompareItemComparator implements Comparator<AbstractCompareItem> {
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(AbstractCompareItem pci1, AbstractCompareItem pci2) {
        IIpsElement element1 = pci1.getIpsElement();
        IIpsElement element2 = pci2.getIpsElement();
        // Sort generations by generation number (and thus chronologically)
        if (element1 instanceof IProductCmptGeneration && element2 instanceof IProductCmptGeneration) {
            return ((IProductCmptGeneration)element1).getGenerationNo()
                    - ((IProductCmptGeneration)element2).getGenerationNo();
        }
        if (element1 instanceof IProductCmptGeneration && !(element2 instanceof IProductCmptGeneration)) {
            return 1;
        }
        if (!(element1 instanceof IProductCmptGeneration) && element2 instanceof IProductCmptGeneration) {
            return -1;
        }
        if ((element1 instanceof IPropertyValue) && (element2 instanceof IPropertyValue)) {
            return ((IPropertyValue)element1).getPropertyType().compareTo(((IPropertyValue)element2).getPropertyType());
        }
        if (element1 instanceof IProductCmptLink && element2 instanceof IProductCmptLink) {
            return 0;
        }
        if (element1 instanceof IProductCmptLink) {
            return 1;
        }
        if (element2 instanceof IProductCmptLink) {
            return -1;
        }
        return 0;
    }
}
