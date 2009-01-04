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

package org.faktorips.valueset;

import java.util.Set;

import junit.framework.TestCase;

import org.faktorips.values.Money;

/**
 * Test class for <code>com.fja.pm.domain.MoneyRange</code>
 * 
 * @author Peter Kuntz
 */
public class MoneyRangeTest extends TestCase {

    public void testValueOf() {
        MoneyRange range = MoneyRange.valueOf("1.25 EUR", "5.67 EUR");
        Money lower = range.getLowerBound();
        Money upper = range.getUpperBound();
        assertEquals(Money.euro(1, 25), lower);
        assertEquals(Money.euro(5, 67), upper);

        try{
            MoneyRange.valueOf(Money.euro(10, 0), Money.euro(10, 10), Money.euro(10, 0), false);
            fail();
        }
        catch(IllegalArgumentException e){}

        try{
            MoneyRange.valueOf(Money.euro(10, 0), Money.euro(10, 10), Money.euro(0, 0), false);
            fail("Expected to fail since zero step size is not allowed.");
        }
        catch(IllegalArgumentException e){}

        range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);
        assertEquals(Money.euro(10, 0), range.getLowerBound());
        assertEquals(Money.euro(100, 0), range.getUpperBound());
        assertEquals(Money.euro(10, 0), range.getStep());
        assertTrue(range.containsNull());

    }

    public void testConstructor() {
        MoneyRange range = new MoneyRange(Money.euro(1, 25), Money.euro(5, 67));
        Money lower = range.getLowerBound();
        Money upper = range.getUpperBound();
        assertEquals(Money.euro(1, 25), lower);
        assertEquals(Money.euro(5, 67), upper);
    }

    public void testContains(){
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);
        assertEquals(11, range.size());
        assertTrue(range.contains(Money.euro(10, 0)));
        assertTrue(range.contains(Money.euro(20, 0)));
        assertTrue(range.contains(Money.euro(30, 0)));
        assertTrue(range.contains(Money.euro(100, 0)));
        assertTrue(range.contains(Money.NULL));
        assertFalse(range.contains(Money.euro(110, 0)));

        range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), false);
        assertEquals(10, range.size());
        assertTrue(range.contains(Money.euro(10, 0)));
        assertTrue(range.contains(Money.euro(20, 0)));
        assertTrue(range.contains(Money.euro(30, 0)));
        assertTrue(range.contains(Money.euro(100, 0)));
        assertFalse(range.contains(Money.euro(110, 0)));

    }

    public void testSize(){
        MoneyRange range = MoneyRange.valueOf(Money.euro(0, 0), Money.euro(100, 0), Money.euro(10, 0), true);
        assertEquals(12, range.size());

        range = MoneyRange.valueOf(Money.euro(0, 0), Money.euro(100, 0), Money.euro(10, 0), false);
        assertEquals(11, range.size());

    }

    public void testGetValues(){
        MoneyRange range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true);
        Set<Money> values = range.getValues(false);
        assertEquals(11, range.size());
        assertTrue(values.contains(Money.euro(10, 0)));
        assertTrue(values.contains(Money.euro(20, 0)));
        assertTrue(values.contains(Money.euro(30, 0)));
        assertTrue(values.contains(Money.euro(100, 0)));
        assertTrue(values.contains(Money.NULL));
        assertFalse(values.contains(Money.euro(110, 0)));

        range = MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), false);
        values = range.getValues(false);
        assertEquals(10, range.size());
        assertTrue(values.contains(Money.euro(10, 0)));
        assertTrue(values.contains(Money.euro(20, 0)));
        assertTrue(values.contains(Money.euro(30, 0)));
        assertTrue(values.contains(Money.euro(100, 0)));
        assertFalse(values.contains(Money.euro(110, 0)));
    }

    public void testSerializable() throws Exception{
        TestUtil.testSerializable(MoneyRange.valueOf(Money.euro(10, 0), Money.euro(100, 0), Money.euro(10, 0), true));
    }
}
