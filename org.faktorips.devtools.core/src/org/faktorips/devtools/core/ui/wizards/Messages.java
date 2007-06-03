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

package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String NewIpsObjectWizard_title;
    public static String IpsObjectPage_msgNew; 
    public static String IpsObjectPage_labelSrcFolder;
    public static String IpsObjectPage_labelPackage;
    public static String IpsObjectPage_labelName;
    public static String IpsObjectPage_msgRootMissing;
    public static String IpsObjectPage_msgRootNoIPSSrcFolder;
    public static String IpsObjectPage_msgPackageMissing;
    public static String IpsObjectPage_msgObjectAllreadyExists;
    public static String IpsObjectPage_msgRootRequired;
    public static String ResultDisplayer_Errors;
    public static String ResultDisplayer_Informations;
    public static String ResultDisplayer_msgErrors;
    public static String ResultDisplayer_msgInformations;
    public static String ResultDisplayer_msgWarnings;
    public static String ResultDisplayer_reasonText;
    public static String ResultDisplayer_titleResults;
    public static String ResultDisplayer_buttonOK;
    public static String ResultDisplayer_Warnings;
}
