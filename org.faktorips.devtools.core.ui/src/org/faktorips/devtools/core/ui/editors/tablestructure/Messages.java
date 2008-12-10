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

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.tablestructure.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String RangesSection_title;
	public static String ColumnEditDialog_title;
	public static String ColumnEditDialog_pageTitle;
	public static String ColumnEditDialog_labelName;
	public static String ColumnEditDialog_labelDatatype;
	public static String StructurePage_title;
	public static String UniqueKeysSection_title;
	public static String ForeignKeysSection_title;
	public static String ColumnsSection_title;
	public static String KeyEditDialog_title;
	public static String KeyEditDialog_generalTitle;
	public static String KeyEditDialog_labelReferenceStructure;
	public static String KeyEditDialog_labelReferenceUniqueKey;
	public static String KeyEditDialog_labelKeyItems;
	public static String KeyEditDialog_groupTitle;
	public static String RangeEditDialog_title;
	public static String RangeEditDialog_generalTitle;
	public static String RangeEditDialog_labelType;
	public static String RangeEditDialog_groupTitle;
	public static String RangeEditDialog_labelFrom;
	public static String RangeEditDialog_labelTo;
	public static String RangeEditDialog_groupAvailableColsTitle;
	public static String TableStructureEditor_title;
	public static String RangeEditDialog_RangeEditDialog_parameterName;
	public static String GeneralInfoSection_labelGeneralInfoSection;
	public static String GeneralInfoSection_labelTableType;
}
