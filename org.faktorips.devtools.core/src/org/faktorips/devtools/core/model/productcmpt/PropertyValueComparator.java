/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

/**
 * Comparator that compares two {@link IPropertyValue}s by there type and by the position the
 * corresponding {@link IProdDefProperty} has in the model.
 * <p>
 * Example:
 * <p>
 * Given two formulas, the positions of the corresponding formula signatures in the product component
 * type's list of formula signatures defines the order. 
 * 
 * @author Jan Ortmann
 */
public class PropertyValueComparator implements Comparator {

    private IProductCmptType type;
    private Map propIndexMap = null;
    
    public PropertyValueComparator(IProductCmptGeneration gen, IIpsProject ipsProject) {
        this(gen.getProductCmpt(), ipsProject);
    }

    public PropertyValueComparator(IProductCmpt productCmpt, IIpsProject ipsProject) {
        this(productCmpt.getProductCmptType(), ipsProject);
    }

    public PropertyValueComparator(String productCmptType, IIpsProject ipsProject) {
        try {
            type = ipsProject.findProductCmptType(productCmptType); 
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error finding type for property comparator, type name = " + productCmptType, e)); //$NON-NLS-1$
        }
        initPropIndexMap(ipsProject);
    }

    public PropertyValueComparator(IProductCmptType type, IIpsProject ipsProject) {
        this.type = type;
        initPropIndexMap(ipsProject);
    }
    
    private void initPropIndexMap(IIpsProject ipsProject) {
        try {
            if (type!=null) {
                propIndexMap = new HashMap();
                IProdDefProperty[] props = type.findProdDefProperties(ipsProject);
                for (int i = 0; i < props.length; i++) {
                    propIndexMap.put(props[i].getPropertyName(), new Integer(i));
                }
            }
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error initializing property comparator for type " + type, e)); //$NON-NLS-1$
        }
    }
    
    public IProductCmptType getProductCmptType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public int compare(Object o1, Object o2) {
        IPropertyValue prop1 = (IPropertyValue)o1;
        IPropertyValue prop2 = (IPropertyValue)o2;
        int typeCompare = prop1.getPropertyType().compareTo(prop2.getPropertyType());
        if (typeCompare!=0) {
            return typeCompare;
        }
        return getIndex(prop1) - getIndex(prop2);
    }
    
    private int getIndex(IPropertyValue prop) {
        if (type==null) {
            return 0;
        }
        Integer index = (Integer)propIndexMap.get(prop.getPropertyName());
        if (index==null) {
            return 0;
        } 
        return index.intValue();
    }

}
