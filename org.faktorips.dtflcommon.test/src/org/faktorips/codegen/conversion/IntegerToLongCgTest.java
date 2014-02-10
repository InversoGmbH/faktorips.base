/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class IntegerToLongCgTest extends AbstractSingleConversionCgTest {

    private IntegerToLongCg converter;

    @Before
    public void setUp() throws Exception {
        converter = new IntegerToLongCg();
    }

    @Test
    public void testGetConversionCode() throws Exception {
        assertEquals("Long.valueOf(integer.intValue())", getConversionCode(converter, "integer"));
    }
}
