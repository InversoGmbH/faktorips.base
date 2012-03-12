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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.ipsobject.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsObjectPartContainer_msgInvalidDescriptionCount;
    public static String IpsObjectPartContainer_msgInvalidLabelCount;

    public static String IpsObject_msg_OtherIpsObjectAlreadyInPathAhead;

    public static String TimedIpsObject_msgIvalidValidToDate;

    public static String IpsObjectGeneration_msgInvalidFromDate;

    public static String Label_msgLocaleMissing;
    public static String Label_msgLocaleNotSupportedByProject;

    public static String Description_msgLocaleMissing;
    public static String Description_msgLocaleNotSupportedByProject;

}
