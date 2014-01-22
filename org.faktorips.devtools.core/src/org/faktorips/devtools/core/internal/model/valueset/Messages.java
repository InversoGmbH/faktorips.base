/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.valueset;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.valueset.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String EnumValueSet_msgValueNotInEnumeration;
    public static String EnumValueSet_msgNotAnEnumValueset;
    public static String EnumValueSet_msgValueNotParsable;
    public static String EnumValueSet_msgDuplicateValue;
    public static String EnumValueSet_msgValueNotParsableDatatypeUnknown;

    public static String Range_msgValueNotInRange;
    public static String Range_msgTypeOfValuesetNotMatching;
    public static String Range_msgNoStepDefinedInSubset;
    public static String Range_msgLowerBoundViolation;
    public static String Range_msgUpperBoundViolation;
    public static String Range_msgUnknownDatatype;
    public static String Range_msgLowerboundGreaterUpperbound;
    public static String Range_msgPropertyValueNotParsable;

    public static String EnumValueSet__msgDatatypeUnknown;
    public static String EnumValueSet_msgDatatypeMissmatch;
    public static String EnumValueSet_msgNullNotSupported;
    public static String EnumValueSet_msgNotSubset;

    public static String RangeValueSet_msgDatatypeUnknown;
    public static String RangeValueSet_msgNullNotContained;
    public static String RangeValueSet_msgNullNotSupported;

    public static String AllValuesValueSet_msgUnknownDatatype;
    public static String AllValuesValueSet_msgValueNotParsable;
    public static String AllValuesValueSet_msgValueNotContained;
    public static String AllValuesValueSet_msgUnknowndDatatype;
    public static String AllValuesValueSet_msgNoSubset;

    public static String RangeValueSet_msgDatatypeNotComparable;
    public static String RangeValueSet_msgStepViolation;
    public static String RangeValueSet_msgStepMismatch;
    public static String RangeValueSet_msgLowerboundMismatch;
    public static String RangeValueSet_msgUpperboundMismatch;
    public static String RangeValueSet_msgStepRangeMismatch;
    public static String RangeValueSet_msgDatatypeNotNumeric;
    public static String RangeValueSet_msgStepNotParsable;
    public static String RangeValueSet_msgLowerboundViolation;
    public static String RangeValueSet_msgUpperboundViolation;

}
