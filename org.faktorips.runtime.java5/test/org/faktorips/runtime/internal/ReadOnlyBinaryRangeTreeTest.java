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

package org.faktorips.runtime.internal;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.faktorips.runtime.internal.ReadOnlyBinaryRangeTree.TwoColumnKey;

/**
 * 
 * @author Peter Erzberger
 */
@SuppressWarnings("unchecked")
public class ReadOnlyBinaryRangeTreeTest extends TestCase {

    public void testTwoCulmnTreeBasedOnStrings() {
        Map map = new HashMap();
        map.put(new TwoColumnKey("a", "c"), new Integer(42));
        map.put(new TwoColumnKey("d", "z"), new Integer(43));

        ReadOnlyBinaryRangeTree tree = new ReadOnlyBinaryRangeTree(map, ReadOnlyBinaryRangeTree.KEY_IS_TWO_COLUMN_KEY);
        assertEquals(new Integer(43), tree.getValue("m"));
    }

    public void testLowerRangeTreeBasedOnStrings() {
        Map map = new HashMap();
        map.put("a", new Integer(42));
        map.put("m", new Integer(43));
        map.put("x", new Integer(44));

        ReadOnlyBinaryRangeTree tree = new ReadOnlyBinaryRangeTree(map, ReadOnlyBinaryRangeTree.KEY_IS_LOWER_BOUND_EQUAL);
        assertEquals(new Integer(42), tree.getValue("l"));
        assertEquals(new Integer(43), tree.getValue("o"));
    }

    public void testBuildTreeWith1Node(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(1, 1, 1);

        assertNode(1,tree.root);

        assertNull(tree.root.left);
        assertNull(tree.root.left);
    }

    public void testBuildTreeWith2Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(2, 0, 1);

        assertNode(1,tree.root);
        assertNode(0,tree.root.left);
        assertNull(tree.root.right);
    }

    public void testBuildTreeWith3Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(3, 1, 1);

        assertNode(2,tree.root);
        assertNode(1,tree.root.left);
        assertNode(3,tree.root.right);

        assertNull(tree.root.left.left);
        assertNull(tree.root.left.right);

        assertNull(tree.root.right.left);
        assertNull(tree.root.right.right);
    }

    public void testBuildTreeWith4Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(4, 0, 1);

        assertNode(2,tree.root);
        assertNode(1,tree.root.left);
        assertNode(3,tree.root.right);
        assertNode(0,tree.root.left.left);
    }

    public void testBuildTreeWidth8Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(8, 0, 1);

        assertNode(4,tree.root);
        assertNode(2,tree.root.left);
        assertNode(6,tree.root.right);
        assertNode(1,tree.root.left.left);
        assertNode(3,tree.root.left.right);
        assertNode(5,tree.root.right.left);
        assertNode(7,tree.root.right.right);
        assertNode(0,tree.root.left.left.left);

        //0
        assertNull(tree.root.left.left.left.left);
        assertNull(tree.root.left.left.left.right);
        //1
        assertNull(tree.root.left.left.right);
        //3
        assertNull(tree.root.left.right.left);
        assertNull(tree.root.left.right.right);
        //5
        assertNull(tree.root.right.left.left);
        assertNull(tree.root.right.left.right);
        //7
        assertNull(tree.root.right.right.left);
        assertNull(tree.root.right.right.right);
    }

    public void testBuildTreeWidth5Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(5, 1, 1);

        assertNode(3,tree.root);
        assertNode(2,tree.root.left);
        assertNode(4,tree.root.right);
        assertNode(1, tree.root.left.left);
        assertNode(5, tree.root.right.right);

        //1
        assertNull(tree.root.left.left.left);
        assertNull(tree.root.left.left.right);

        //2
        assertNull(tree.root.left.right);
        //4
        assertNull(tree.root.right.left);

        //5
        assertNull(tree.root.right.right.left);
        assertNull(tree.root.right.right.right);
    }

    public void testBuildTreeWidth7Nodes(){
        TestReadOnlyBinaryRangTree tree = TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(7, 1, 1);

        assertNode(4,tree.root);
        assertNode(2,tree.root.left);
        assertNode(6,tree.root.right);
        assertNode(1, tree.root.left.left);
        assertNode(3, tree.root.left.right);
        assertNode(7, tree.root.right.right);
        assertNode(5, tree.root.right.left);

        //1
        assertNull(tree.root.left.left.left);
        assertNull(tree.root.left.left.right);

        //3
        assertNull(tree.root.left.right.left);
        assertNull(tree.root.left.right.right);

        //5
        assertNull(tree.root.right.left.left);
        assertNull(tree.root.right.left.right);

        //7
        assertNull(tree.root.right.right.left);
        assertNull(tree.root.right.right.right);
    }

    public void testLowerBoundEqualGetValue(){
        TestReadOnlyBinaryRangTree tree =
            TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(
                    10, 10, 10, ReadOnlyBinaryRangeTree.KEY_IS_LOWER_BOUND_EQUAL);

        Integer lowerBound = (Integer)tree.getValue(new Integer(5));
        assertNull(lowerBound);

        lowerBound = (Integer)tree.getValue(new Integer(110));
        assertEquals(new Integer(100), lowerBound);

        lowerBound = (Integer)tree.getValue(new Integer(10));
        assertEquals(new Integer(10), lowerBound);

        lowerBound = (Integer)tree.getValue(new Integer(19));
        assertEquals(new Integer(10), lowerBound);

        lowerBound = (Integer)tree.getValue(new Integer(100));
        assertEquals(new Integer(100), lowerBound);

        lowerBound = (Integer)tree.getValue(new Integer(35));
        assertEquals(new Integer(30), lowerBound);

        lowerBound = (Integer)tree.getValue(new Integer(69));
        assertEquals(new Integer(60), lowerBound);
    }

    public void testLowerBoundGetValue(){
        TestReadOnlyBinaryRangTree tree =
            TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(
                    10, 10, 10, ReadOnlyBinaryRangeTree.KEY_IS_LOWER_BOUND);

        Integer lowerBound = (Integer)tree.getValue(new Integer(10));
        assertNull(lowerBound);

        lowerBound = (Integer)tree.getValue(new Integer(11));
        assertEquals(new Integer(10), lowerBound);

    }

    public void testUpperBoundEqualGetValue(){
        TestReadOnlyBinaryRangTree tree =
            TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(
                    10, 10, 10, ReadOnlyBinaryRangeTree.KEY_IS_UPPER_BOUND_EQUAL);

        Integer upperBound = (Integer)tree.getValue(new Integer(5));
        assertEquals(new Integer(10), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(110));
        assertNull(upperBound);

        upperBound = (Integer)tree.getValue(new Integer(100));
        assertEquals(new Integer(100), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(10));
        assertEquals(new Integer(10), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(26));
        assertEquals(new Integer(30), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(76));
        assertEquals(new Integer(80), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(30));
        assertEquals(new Integer(30), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(80));
        assertEquals(new Integer(80), upperBound);
    }

    public void testUpperBoundGetValue(){
        TestReadOnlyBinaryRangTree tree =
            TestReadOnlyBinaryRangTree.createTreeWidthIntegerValues(
                    10, 10, 10, ReadOnlyBinaryRangeTree.KEY_IS_UPPER_BOUND);

        Integer upperBound = (Integer)tree.getValue(new Integer(10));
        assertEquals(new Integer(20), upperBound);

        upperBound = (Integer)tree.getValue(new Integer(9));
        assertEquals(new Integer(10), upperBound);
    }

    public void testTwoColumnRangeGetValue() {
        TestTwoColumnReadOnlyBinaryRangeTree tree =
            TestTwoColumnReadOnlyBinaryRangeTree.createTreeWithIntegerValues(
                    10, 2, 1);

        Integer value = (Integer) tree.getValue(new Integer(-2));
        assertNull(value);

        value = (Integer) tree.getValue(new Integer(-1));
        assertNull(value);

        value = (Integer) tree.getValue(new Integer(0));
        assertEquals(new Integer(1), value);

        value = (Integer) tree.getValue(new Integer(1));
        assertEquals(new Integer(1), value);

        value = (Integer) tree.getValue(new Integer(2));
        assertEquals(new Integer(1), value);

        value = (Integer) tree.getValue(new Integer(3));
        assertNull(value);

        value = (Integer) tree.getValue(new Integer(4));
        assertEquals(new Integer(5), value);

        value = (Integer) tree.getValue(new Integer(5));
        assertEquals(new Integer(5), value);

        value = (Integer) tree.getValue(new Integer(6));
        assertEquals(new Integer(5), value);

        value = (Integer) tree.getValue(new Integer(7));
        assertNull(value);
    }

    public void testTwoColumnRangeWithZipCodes(){
        //There was a test case with the number of 35 nodes that caused the tree to break during initialization
        TestTwoColumnReadOnlyBinaryRangeTree.createTreeWithIntegerValues(35, 0, 0);
    }

    private void assertNode(int expected, ReadOnlyBinaryRangeTree.Node node){
        assertEquals(expected, ((Integer)node.key).intValue());
    }

    private static class TestReadOnlyBinaryRangTree extends ReadOnlyBinaryRangeTree{

        private static final long serialVersionUID = 1L;

        /**
         * @param values
         */
        public TestReadOnlyBinaryRangTree(Map map, KeyType keyType) {
            super(map, keyType);
        }

        private static TestReadOnlyBinaryRangTree createTreeWidthIntegerValues(int nodeCount, int startPos, int rangWidth){
            return createTreeWidthIntegerValues(nodeCount, startPos, rangWidth, ReadOnlyBinaryRangeTree.KEY_IS_LOWER_BOUND_EQUAL);
        }

        private static TestReadOnlyBinaryRangTree createTreeWidthIntegerValues(int nodeCount, int startPos, int rangeWidth, KeyType keyType){
            HashMap map = new HashMap(nodeCount);
            for (int i = 0; i < nodeCount; i++) {
                Integer value = new Integer(startPos + rangeWidth * i);
                map.put(value, value);
            }
            return new TestReadOnlyBinaryRangTree(map, keyType);
        }
    }

    private static class TestTwoColumnReadOnlyBinaryRangeTree extends ReadOnlyBinaryRangeTree {

        private static final long serialVersionUID = 42L;

        /**
         * @param map
         */
        public TestTwoColumnReadOnlyBinaryRangeTree(Map map) {
            super(map, ReadOnlyBinaryRangeTree.KEY_IS_TWO_COLUMN_KEY);
        }

        private static TestTwoColumnReadOnlyBinaryRangeTree createTreeWithIntegerValues(int nodeCount, int nodeWidth, int gap) {
            final Map map = new HashMap(nodeCount);

            for (int i = 0; i < nodeCount; i++) {
                final Integer lowerBound = new Integer((nodeWidth + gap + 1) * (i));
                final Integer upperBound = new Integer(lowerBound.intValue() + nodeWidth);
                final Integer value = new Integer(lowerBound.intValue() + nodeWidth / 2);
                final TwoColumnKey key = new TwoColumnKey(lowerBound, upperBound);
                map.put(key, value);
            }

            return new TestTwoColumnReadOnlyBinaryRangeTree(map);
        }
    }
}
