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

package org.faktorips.devtools.core.model.enums;

import org.eclipse.osgi.util.NLS;

/*
 * 
 * @author Alexander Weickmann
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.enums.messages"; //$NON-NLS-1$

    public static String EnumType_SupertypeDoesNotExist;
    public static String EnumType_SupertypeIsNotAbstract;
    public static String EnumType_NotInheritedAttributesInSupertypeHierarchySingular;
    public static String EnumType_NotInheritedAttributesInSupertypeHierarchyPlural;
    public static String EnumType_NoIdentifierAttribute;

    public static String EnumContent_EnumTypeMissing;
    public static String EnumContent_EnumTypeDoesNotExist;
    public static String EnumContent_EnumTypeIsAbstract;
    public static String EnumContent_ValuesArePartOfModel;
    public static String EnumContent_ReferencedEnumAttributesCountInvalid;
    public static String EnumContent_PackageFragmentRootNotCorrect;
    public static String EnumContent_PackageFragmentNotCorrect;

    public static String EnumAttribute_Identifier;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

}
