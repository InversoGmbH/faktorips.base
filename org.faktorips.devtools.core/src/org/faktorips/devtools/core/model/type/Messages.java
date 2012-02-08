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

package org.faktorips.devtools.core.model.type;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.type.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AssociationType_label_aggregation;
    public static String AssociationType_label_association;
    public static String AssociationType_label_composition_detail_to_master;
    public static String AssociationType_label_composition_master_to_detail;
    public static String ProductCmptPropertyType_defaultValueAndValueSet;
    public static String ProductCmptPropertyType_fomula;
    public static String ProductCmptPropertyType_productAttribute;
    public static String ProductCmptPropertyType_tableUsage;
    public static String ProductCmptPropertyType_ValidationRule;

}
