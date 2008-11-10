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

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.tablestructure.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String ColumnRange_msgMissingColumn;

	public static String TableStructure_descriptionStart;

	public static String TableStructure_descriptionEnd;

	public static String ForeignKey_msgMissingUniqueKey;

	public static String ForeignKey_msgMalformedForeignKey;

	public static String ForeignKey_msgInvalidKeyItem;

	public static String ForeignKey_msgKeyItemMissmatch;

	public static String ForeignKey_msgNotARange;

	public static String ForeignKey_msgInvalidRange;

	public static String ForeignKey_msgReferencedRangeInvalid;

	public static String ForeignKey_msgForeignKeyDatatypeMismatch;

	public static String ForeignKey_msgKeyMissmatch;

	public static String ForeignKey_msgNotAColumn;

	public static String ForeignKey_msgKeyDatatypeMismatch;

    public static String TableStructure_msgMoreThanOneKeyNotAdvisableInFormulas;

    public static String TableStructure_needsTwoKeys;

    public static String UniqueKey_datatypeMustBeString;

    public static String UniqueKey_firstKeyOnlyOneItem;

	public static String UniqueKey_msgTooLessItems;

	public static String UniqueKey_msgKeyItemMismatch;

	public static String ColumnRange_msgParameterEmpty;

	public static String Column_msgPrimitvesArentSupported;

	public static String TableStructureType_labelSingleContent;

	public static String TableStructureType_lableMultipleContents;

	public static String TableStructureType_labelEnumTypeModel;

	public static String ColumnRange_msgDatatypeInvalidForRange;

    public static String Column_msgInvalidName;

    public static String UniqueKey_secondKeyOnlyOneItem;

    public static String UniqueKey_wrong_sequence;
}
