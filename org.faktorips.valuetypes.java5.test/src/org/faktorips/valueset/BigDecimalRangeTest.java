/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.Test;

public class BigDecimalRangeTest {
    @Test
    public void testValueOf() {
        BigDecimalRange range = BigDecimalRange.valueOf("1.25", "5.67");
        BigDecimal lower = range.getLowerBound();
        BigDecimal upper = range.getUpperBound();
        assertEquals(BigDecimal.valueOf(125, 2), lower);
        assertEquals(BigDecimal.valueOf(567, 2), upper);
    }

    @Test
    public void testConstructor() {
        BigDecimalRange range = new BigDecimalRange(BigDecimal.valueOf(125, 2), BigDecimal.valueOf(567, 2));
        BigDecimal lower = range.getLowerBound();
        BigDecimal upper = range.getUpperBound();
        assertEquals(BigDecimal.valueOf(125, 2), lower);
        assertEquals(BigDecimal.valueOf(567, 2), upper);
    }

    @Test
    public void testConstructorWithStep() {
        BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)),
                BigDecimal.valueOf(10, 0));
        BigDecimalRange.valueOf(BigDecimal.valueOf(135, 2), BigDecimal.valueOf(108, 1), BigDecimal.valueOf(135, 2));

        try {
            // step doesn't fit to range
            BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)),
                    BigDecimal.valueOf(new Integer(12)));
            fail();
        } catch (IllegalArgumentException e) {
            // ok exception expected
        }

        try {
            BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)),
                    BigDecimal.valueOf(new Integer(0)));
            fail("Expect to fail since a step size of zero is not allowed.");
        } catch (IllegalArgumentException e) {
            // ok exception expected
        }
    }

    @Test
    public void testContains() {
        BigDecimalRange range = new BigDecimalRange(BigDecimal.valueOf(new Integer(10)),
                BigDecimal.valueOf(new Integer(100)));
        assertTrue(range.contains(BigDecimal.valueOf(new Integer(30))));
        assertFalse(range.contains(BigDecimal.valueOf(new Integer(120))));
        assertFalse(range.contains(BigDecimal.valueOf(new Integer(5))));

        range = BigDecimalRange
                .valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)), null); // ?
        assertTrue(range.contains(BigDecimal.valueOf(new Integer(30))));
        assertFalse(range.contains(BigDecimal.valueOf(new Integer(120))));
        assertFalse(range.contains(BigDecimal.valueOf(new Integer(5))));

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)),
                BigDecimal.valueOf(new Integer(10)));

        assertTrue(range.contains(BigDecimal.valueOf(30, 0)));
        assertFalse(range.contains(BigDecimal.valueOf(35, 0)));
    }

    @Test
    public void testGetValues() {

        BigDecimalRange range = new BigDecimalRange(BigDecimal.valueOf(new Integer(10)),
                BigDecimal.valueOf(new Integer(100)));
        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // ok exception expected
        }

        range = BigDecimalRange
                .valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)), null);
        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // ok exception expected
        }

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), null, BigDecimal.valueOf(new Integer(10)));

        try {
            range.getValues(false);
            fail();
        } catch (IllegalStateException e) {
            // ok exception expected
        }

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)),
                BigDecimal.valueOf(new Integer(10)));

        Set<BigDecimal> values = range.getValues(false);
        assertEquals(10, values.size());

        assertTrue(values.contains(BigDecimal.valueOf(100, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(70, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(10, 0)));

        range = BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)), BigDecimal.valueOf(new Integer(100)),
                BigDecimal.valueOf(new Integer(10)), true);
        values = range.getValues(false);
        assertEquals(11, values.size());

        assertTrue(values.contains(BigDecimal.valueOf(100, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(70, 0)));
        assertTrue(values.contains(BigDecimal.valueOf(10, 0)));
        assertTrue(values.contains(null));

    }

    @Test
    public void testSerializable() throws Exception {
        BigDecimalRange range = BigDecimalRange.valueOf(BigDecimal.valueOf(new Integer(10)),
                BigDecimal.valueOf(new Integer(100)), BigDecimal.valueOf(new Integer(10)), true);
        TestUtil.testSerializable(range);
    }

}
