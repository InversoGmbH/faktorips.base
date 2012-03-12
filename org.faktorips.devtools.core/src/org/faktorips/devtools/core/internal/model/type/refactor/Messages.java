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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.type.refactor.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String RenameAttributeProcessor_processorName;

    public static String RenameAssociationProcessor_processorName;
    public static String RenameAssociationProcessor_msgNewNameMustNotBeEmpty;
    public static String RenameAssociationProcessor_msgEitherNameOrPluralNameMustBeChanged;
    public static String RenameAssociationProcessor_msgNewPluralNameMustNotBeEmptyForToManyAssociations;

    public static String PullUpAttributeProcessor_processorName;
    public static String PullUpAttributeProcessor_msgTypeHasNoSupertype;
    public static String PullUpAttributeProcessor_msgSupertypeCouldNotBeFound;
    public static String PullUpAttributeProcessor_msgTargetTypeMustBeSupertype;
    public static String PullUpAttributeProcessor_msgAttributeAlreadyExistingInTargetType;
    public static String PullUpAttributeProcessor_msgBaseOfOverwrittenAttributeNotFound;

}
