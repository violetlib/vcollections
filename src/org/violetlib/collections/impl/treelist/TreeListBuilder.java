/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import java.util.Arrays;

import org.violetlib.collections.IList;
import org.violetlib.collections.ListBuilder;

import org.jetbrains.annotations.*;

import static org.violetlib.collections.impl.treelist.TreeListConstants.*;
import static org.violetlib.collections.impl.treelist.TreeListUtils.*;

/**

*/

public final class TreeListBuilder<V>
  implements ListBuilder<V>
{
    public static <V> @NotNull ListBuilder<V> create()
    {
        return new TreeListBuilder<>();
    }

    private Object @Nullable [][][][][][] tree6;
    private Object @Nullable [][][][][] tree5;
    private Object @Nullable [][][][] tree4;
    private Object @Nullable [][][] tree3;
    private Object @Nullable [][] tree2;
    private Object @NotNull [] currentLeaf;

    private int currentLeafSize;
    private int lenRest;
    private int depth = 1;

    private int currentSize;
    private @Nullable V lastAddedItem;

    private TreeListBuilder()
    {
        currentLeaf = new Object[WIDTH];
    }

    @Override
    public void add(@NotNull V e)
    {
        if (currentLeafSize == WIDTH) {
            advance();
        }
        currentLeaf[currentLeafSize++] = e;
        ++currentSize;
        lastAddedItem = e;
    }

    private void advance()
    {
        int index = lenRest + WIDTH;
        int xor = index ^ lenRest;
        lenRest = index;
        currentLeafSize = 0;
        advance1(index, xor);
    }

    private void advance1(int index, int xor)
    {
        if (xor < WIDTH2) { // level = 1
            if (depth == 1) {
                tree2 = new Object[WIDTH][];
                tree2[0] = currentLeaf;
                depth++;
            }
            currentLeaf = new Object[WIDTH];
            assert tree2 != null;
            tree2[(index >>> BITS) & MASK] = currentLeaf;
        } else if (xor < WIDTH3) { // level = 2
            if (depth == 2) {
                tree3 = new Object[WIDTH][][];
                tree3[0] = tree2;
                ++depth;
            }
            currentLeaf = new Object[WIDTH];
            tree2 = new Object[WIDTH][];
            tree2[(index >>> BITS) & MASK] = currentLeaf;
            assert tree3 != null;
            tree3[(index >>> BITS2) & MASK] = tree2;
        } else if (xor < WIDTH4) { // level = 3
            if (depth == 3) {
                tree4 = new Object[WIDTH][][][];
                tree4[0] = tree3;
                ++depth;
            }
            currentLeaf = new Object[WIDTH];
            tree2 = new Object[WIDTH][];
            tree3 = new Object[WIDTH][][];
            tree2[(index >>> BITS) & MASK] = currentLeaf;
            tree3[(index >>> BITS2) & MASK] = tree2;
            assert tree4 != null;
            tree4[(index >>> BITS3) & MASK] = tree3;
        } else if (xor < WIDTH5) { // level = 4
            if (depth == 4) {
                tree5 = new Object[WIDTH][][][][];
                tree5[0] = tree4;
                ++depth;
            }
            currentLeaf = new Object[WIDTH];
            tree2 = new Object[WIDTH][];
            tree3 = new Object[WIDTH][][];
            tree4 = new Object[WIDTH][][][];
            tree2[(index >>> BITS) & MASK] = currentLeaf;
            tree3[(index >>> BITS2) & MASK] = tree2;
            tree4[(index >>> BITS3) & MASK] = tree3;
            assert tree5 != null;
            tree5[(index >>> BITS4) & MASK] = tree4;
        } else if (xor < WIDTH6) { // level = 5
            if (depth == 5) {
                tree6 = new Object[LASTWIDTH][][][][][];
                tree6[0] = tree5;
                ++depth;
            }
            currentLeaf = new Object[WIDTH];
            tree2 = new Object[WIDTH][];
            tree3 = new Object[WIDTH][][];
            tree4 = new Object[WIDTH][][][];
            tree5 = new Object[WIDTH][][][][];
            tree2[(index >>> BITS) & MASK] = currentLeaf;
            tree3[(index >>> BITS2) & MASK] = tree2;
            tree4[(index >>> BITS3) & MASK] = tree3;
            tree5[(index >>> BITS4) & MASK] = tree4;
            assert tree6 != null;
            tree6[(index >>> BITS5)] = tree5;
        } else {                      // level = 6
            throw new UnsupportedOperationException("List is too large");
        }
    }

    @Override
    public void reset()
    {
        currentLeaf = new Object[WIDTH];
        tree2 = null;
        tree3 = null;
        tree4 = null;
        tree5 = null;
        tree6 = null;
        currentLeafSize = 0;
        lenRest = 0;
        depth = 1;
        currentSize = 0;
        lastAddedItem = null;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public int size()
    {
        return currentSize;
    }

    @Override
    public @Nullable V lastItem()
    {
        return lastAddedItem;
    }

    @Override
    public @NotNull IList<V> values()
    {
        int len = currentLeafSize + lenRest;
        if (len == 0) {
            return TreeList.empty();
        } else if (len <= WIDTH) {
            assert currentLeaf != null;
            if (len == WIDTH) {
                return TreeList1.createWithPrivateArray(currentLeaf);
            } else {
                return TreeList1.createWithPrivateArray(Arrays.copyOf(currentLeaf, len));
            }
        } else if (len <= WIDTH2) {
            assert currentLeaf != null;
            assert tree2 != null;
            int i1 = (len-1) & MASK;
            int i2 = (len-1) >>> BITS;
            Object[] data = Arrays.copyOfRange(tree2, 1, i2);
            Object[] prefix1 = tree2[0];
            Object[] suffix1 = copyIfDifferentSize(tree2[i2], i1+1);
            return TreeList2.internalCreate(len, prefix1, (Object[][]) data, suffix1);
        } else if (len <= WIDTH3) {
            assert currentLeaf != null;
            assert tree2 != null;
            assert tree3 != null;
            int i1 = (len-1) & MASK;
            int i2 = ((len-1) >>> BITS) & MASK;
            int i3 = ((len-1) >>> BITS2);
            Object[][][] data = Arrays.copyOfRange(tree3, 1, i3);
            Object[][] prefix2 = copyTail(tree3[0]);
            Object[] prefix1 = tree3[0][0];
            Object[][] suffix2 = Arrays.copyOf(tree3[i3], i2);
            Object[] suffix1 = copyIfDifferentSize(tree3[i3][i2], i1+1);
            return TreeList3.internalCreate(len, prefix1, prefix2, data, suffix2, suffix1);
        } else if (len <= WIDTH4) {
            assert currentLeaf != null;
            assert tree2 != null;
            assert tree3 != null;
            assert tree4 != null;

            int i1 = (len-1) & MASK;
            int i2 = ((len-1) >>> BITS) & MASK;
            int i3 = ((len-1) >>> BITS2) & MASK;
            int i4 = ((len-1) >>> BITS3);
            Object[][][][] data = Arrays.copyOfRange(tree4, 1, i4);
            Object[][][] prefix3 = copyTail(tree4[0]);
            Object[][] prefix2 = copyTail(tree4[0][0]);
            Object[] prefix1 = tree4[0][0][0];
            Object[][][] suffix3 = Arrays.copyOf(tree4[i4], i3);
            Object[][] suffix2 = Arrays.copyOf(tree4[i4][i3], i2);
            Object[] suffix1 = copyIfDifferentSize(tree4[i4][i3][i2], i1+1);
            return TreeList4.internalCreate(len, prefix1, prefix2, prefix3, data, suffix3, suffix2, suffix1);
        } else if (len <= WIDTH5) {
            assert currentLeaf != null;
            assert tree2 != null;
            assert tree3 != null;
            assert tree4 != null;
            assert tree5 != null;

            int i1 = (len-1) & MASK;
            int i2 = ((len-1) >>> BITS) & MASK;
            int i3 = ((len-1) >>> BITS2) & MASK;
            int i4 = ((len-1) >>> BITS3) & MASK;
            int i5 = ((len-1) >>> BITS4);
            Object[][][][][] data = Arrays.copyOfRange(tree5, 1, i5);
            Object[][][][] prefix4 = copyTail(tree5[0]);
            Object[][][] prefix3 = copyTail(tree5[0][0]);
            Object[][] prefix2 = copyTail(tree5[0][0][0]);
            Object[] prefix1 = tree5[0][0][0][0];
            Object[][][][] suffix4 = Arrays.copyOf(tree5[i5], i4);
            Object[][][] suffix3 = Arrays.copyOf(tree5[i5][i4], i3);
            Object[][] suffix2 = Arrays.copyOf(tree5[i5][i4][i3], i2);
            Object[] suffix1 = copyIfDifferentSize(tree5[i5][i4][i3][i2], i1+1);
            return TreeList5.internalCreate(len, prefix1, prefix2, prefix3, prefix4, data, suffix4, suffix3, suffix2, suffix1);
        } else {
            assert currentLeaf != null;
            assert tree2 != null;
            assert tree3 != null;
            assert tree4 != null;
            assert tree5 != null;
            assert tree6 != null;

            int i1 = (len-1) & MASK;
            int i2 = ((len-1) >>> BITS) & MASK;
            int i3 = ((len-1) >>> BITS2) & MASK;
            int i4 = ((len-1) >>> BITS3) & MASK;
            int i5 = ((len-1) >>> BITS4) & MASK;
            int i6 = ((len-1) >>> BITS5);
            Object[][][][][][] data = Arrays.copyOfRange(tree6, 1, i6);
            Object[][][][][] prefix5 = copyTail(tree6[0]);
            Object[][][][] prefix4 = copyTail(tree6[0][0]);
            Object[][][] prefix3 = copyTail(tree6[0][0][0]);
            Object[][] prefix2 = copyTail(tree6[0][0][0][0]);
            Object[] prefix1 = tree6[0][0][0][0][0];
            Object[][][][][] suffix5 = Arrays.copyOf(tree6[i6], i5);
            Object[][][][] suffix4 = Arrays.copyOf(tree6[i6][i5], i4);
            Object[][][] suffix3 = Arrays.copyOf(tree6[i6][i5][i4], i3);
            Object[][] suffix2 = Arrays.copyOf(tree6[i6][i5][i4][i3], i2);
            Object[] suffix1 = copyIfDifferentSize(tree6[i6][i5][i4][i3][i2], i1+1);
            return TreeList6.internalCreate(len, prefix1, prefix2, prefix3, prefix4, prefix5, data, suffix5, suffix4, suffix3, suffix2, suffix1);
        }
    }
}
