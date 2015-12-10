/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.ConfigElement;
import org.faktorips.devtools.core.internal.model.valueset.DelegatingValueSet;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.Messages;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AnyValueSetFormatTest {

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IpsUIPlugin uiPlugin;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private ConfigElement configElement;

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IInputFormat<String> cachedInputFormat;

    @Mock
    private ValueDatatype datatype;

    private EnumValueSet enumValueSet;

    private RangeValueSet rangeValueSet;

    private UnrestrictedValueSet unrestrictedValueSet;

    private AnyValueSetFormat format;

    @Before
    public void setUp() throws Exception {
        enumValueSet = new EnumValueSet(configElement, "ID");
        when(configElement.getValueSet()).thenReturn(enumValueSet);
        when(configElement.getIpsProject()).thenReturn(ipsProject);
        when(configElement.findValueDatatype(ipsProject)).thenReturn(datatype);
        when(configElement.getIpsModel()).thenReturn(ipsModel);
        when(configElement.getIpsObject()).thenReturn(ipsObject);
        when(uiPlugin.getInputFormat(datatype, ipsProject)).thenReturn(cachedInputFormat);
        format = new AnyValueSetFormat(configElement, uiPlugin);
        rangeValueSet = new RangeValueSet(configElement, "ID");
        unrestrictedValueSet = new UnrestrictedValueSet(configElement, "ID");
    }

    @Test
    public void testParseInternalEmptyUnrestrictedValueSet() throws Exception {
        when(configElement.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.ENUM, ValueSetType.UNRESTRICTED));

        IValueSet parseInternal = format.parseInternal("");

        assertNotNull(parseInternal);
        assertTrue(parseInternal instanceof UnrestrictedValueSet);
        assertEquals(configElement, parseInternal.getParent());
    }

    @Test
    public void testParseInternalEmptyUnrestrictedValueSetAlreadyUnrestricted() throws Exception {
        IValueSet unrestrictedValueSet = new UnrestrictedValueSet(configElement, "");
        when(configElement.getValueSet()).thenReturn(unrestrictedValueSet);
        when(configElement.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.ENUM, ValueSetType.UNRESTRICTED));

        IValueSet parseInternal = format.parseInternal("");

        assertSame(unrestrictedValueSet, parseInternal);
    }

    @Test
    public void testParseInternalEmptyReturnUnrestrictedValueSetIfValueEmpty() throws Exception {
        IValueSet parseInternal = format.parseInternal("");

        assertTrue(parseInternal instanceof UnrestrictedValueSet);
    }

    @Test
    public void testParseInternalUnrestrictedValueSet() throws Exception {
        when(configElement.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.ENUM, ValueSetType.UNRESTRICTED));

        IValueSet parseInternal = format.parseInternal(Messages.ValueSetFormat_unrestricted);

        assertNotNull(parseInternal);
        assertTrue(parseInternal instanceof UnrestrictedValueSet);
        assertEquals(configElement, parseInternal.getParent());
    }

    @Test
    public void testParseInternal_ReturnOriginValueSetIfValueSetFormatIsNull() throws Exception {
        when(configElement.getAllowedValueSetTypes(ipsProject)).thenReturn(
                Arrays.asList(ValueSetType.RANGE, ValueSetType.UNRESTRICTED));

        IValueSet parseInternal = format.parseInternal("|aabc|");

        assertEquals(parseInternal, enumValueSet);
    }

    @Test
    public void testFormatInternal_delegateEnum() throws Exception {
        DelegatingValueSet delegatingValueSet = new DelegatingValueSet(enumValueSet, configElement);

        String formattedValue = format.formatInternal(delegatingValueSet);

        assertThat(formattedValue, is("{}"));
    }

    @Test
    public void testFormatInternal_delegateRange() throws Exception {
        DelegatingValueSet delegatingValueSet = new DelegatingValueSet(rangeValueSet, configElement);

        String formattedValue = format.formatInternal(delegatingValueSet);

        assertThat(formattedValue, is("[* ... *]"));
    }

    @Test
    public void testFormatInternal_delegateUnrestricted() throws Exception {
        DelegatingValueSet delegatingValueSet = new DelegatingValueSet(unrestrictedValueSet, configElement);

        String formattedValue = format.formatInternal(delegatingValueSet);

        assertThat(formattedValue, is(unrestrictedValueSet.toShortString()));
    }

}
