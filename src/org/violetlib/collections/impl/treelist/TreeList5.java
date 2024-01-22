/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import java.util.function.Function;

import org.violetlib.collections.IList;
import org.violetlib.collections.impl.ListOperations;

import org.jetbrains.annotations.*;

import static org.violetlib.collections.impl.treelist.ArraySupport.*;
import static org.violetlib.collections.impl.treelist.TreeListConstants.*;

/**
  The tree list representation of a tree of depth 5.
*/

public final class TreeList5<V>
  extends TreeListSlicesImpl<V>
{
    static <V> @NotNull IList<V> internalCreate(int size,
                                                Object @NotNull [] prefix1,
                                                Object @NotNull [][] prefix2,
                                                Object @NotNull [][][] prefix3,
                                                Object @NotNull [][][][] prefix4,
                                                Object @NotNull [][][][][] data,
                                                Object @NotNull [][][][] suffix4,
                                                Object @NotNull [][][] suffix3,
                                                Object @NotNull [][] suffix2,
                                                Object @NotNull [] suffix1)
    {
        return new TreeList5<>(size, prefix1, prefix2, prefix3, prefix4, data, suffix4, suffix3, suffix2, suffix1);
    }

    private final Object @NotNull [] prefix1;
    private final Object @NotNull [][] prefix2;
    private final Object @NotNull [][][] prefix3;
    private final Object @NotNull [][][][] prefix4;
    private final Object @NotNull [][][][][] data;
    private final Object @NotNull [][][][] suffix4;
    private final Object @NotNull [][][] suffix3;
    private final Object @NotNull [][] suffix2;
    private final Object @NotNull [] suffix1;

    private final int len12;
    private final int len123;
    private final int len1234;

    private TreeList5(int size,
                      Object @NotNull [] prefix1,
                      Object @NotNull [][] prefix2,
                      Object @NotNull [][][] prefix3,
                      Object @NotNull [][][][] prefix4,
                      Object @NotNull [][][][][] data,
                      Object @NotNull [][][][] suffix4,
                      Object @NotNull [][][] suffix3,
                      Object @NotNull [][] suffix2,
                      Object @NotNull [] suffix1)
    {
        super(size);

        assert prefix1.length <= WIDTH;
        assert suffix1.length <= WIDTH;

        this.prefix1 = prefix1;
        this.prefix2 = prefix2;
        this.prefix3 = prefix3;
        this.prefix4 = prefix4;
        this.data = data;
        this.suffix4 = suffix4;
        this.suffix3 = suffix3;
        this.suffix2 = suffix2;
        this.suffix1 = suffix1;
        this.len12 = prefix1.length + prefix2.length * WIDTH;
        this.len123 = len12 + prefix3.length * WIDTH2;
        this.len1234 = len123 + prefix4.length * WIDTH3;
    }

    @Override
    public int getSliceCount()
    {
        return 9;
    }

    @Override
    public Object @NotNull [] getSlice(int sliceIndex)
    {
        switch (sliceIndex) {
            case 0: return prefix1;
            case 1: return prefix2;
            case 2: return prefix3;
            case 3: return prefix4;
            case 4: return data;
            case 5: return suffix4;
            case 6: return suffix3;
            case 7: return suffix2;
            case 8: return suffix1;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    public int getSlicePrefixLength(int sliceIndex)
    {
        switch (sliceIndex) {
            case 0: return prefix1.length;
            case 1: return len12;
            case 2: return len123;
            case 3: return len1234;
            case 4: return len1234 + data.length * WIDTH4;
            case 5: return len1234 + data.length * WIDTH4 + suffix4.length * WIDTH3;
            case 6: return len1234 + data.length * WIDTH4 + suffix4.length * WIDTH3 + suffix3.length * WIDTH2;
            case 7: return size - suffix1.length;
            case 8: return size;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    public int getSliceDepth(int sliceIndex)
    {
        switch (sliceIndex) {
            case 0: return 1;
            case 1: return 2;
            case 2: return 3;
            case 3: return 4;
            case 4: return 5;
            case 5: return 4;
            case 6: return 3;
            case 7: return 2;
            case 8: return 1;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    public int getSliceElementCount(int sliceIndex)
    {
        switch (sliceIndex) {
            case 0: return prefix1.length;
            case 1: return prefix2.length * WIDTH;
            case 2: return prefix3.length * WIDTH2;
            case 3: return prefix4.length * WIDTH3;
            case 4: return data.length * WIDTH4;
            case 5: return suffix4.length * WIDTH3;
            case 6: return suffix3.length * WIDTH2;
            case 7: return suffix2.length * WIDTH;
            case 8: return suffix1.length;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    protected @NotNull V internalGet(int index)
    {
        int prefix1Length = prefix1.length;
        if (index < prefix1Length) {
            return (V) prefix1[index];
        }
        if (index < len12) {
            int offset = index - prefix1Length;
            return (V) prefix2[offset >>> BITS][offset & MASK];
        }
        if (index < len123) {
            int offset = index - len12;
            return (V) prefix3[offset >>> BITS2][(offset >>> BITS) & MASK][offset & MASK];
        }
        if (index < len1234) {
            int offset = index - len123;
            return (V) prefix4[offset >>> BITS3][(offset >>> BITS2) & MASK][(offset >>> BITS) & MASK][offset & MASK];
        }

        int offset = index - len1234;
        int i5 = offset >>> BITS4;
        int i4 = (offset >>> BITS3) & MASK;
        int i3 = (offset >>> BITS2) & MASK;
        int i2 = (offset >>> BITS) & MASK;
        int i1 = offset & MASK;
        if (i5 < data.length) {
            return (V) data[i5][i4][i3][i2][i1];
        }
        if (i4 < suffix4.length) {
            return (V) suffix4[i4][i3][i2][i1];
        }
        if (i3 < suffix3.length) {
            return (V) suffix3[i3][i2][i1];
        }
        if (i2 < suffix2.length) {
            return (V) suffix2[i2][i1];
        }
        return (V) suffix1[i1];
    }

    @Override
    public @NotNull IList<V> appending(@NotNull V value)
    {
        if (suffix1.length < WIDTH) {
            Object[] newSuffix1 = copyAppend1(suffix1, value);
            return internalCreate(size + 1, prefix1, prefix2, prefix3, prefix4, data, suffix4, suffix3, suffix2, newSuffix1);
        }
        if (suffix2.length < WIDTH - 1) {
            Object[] newSuffix1 = wrap1(value);
            Object[][] newSuffix2 = copyAppend(suffix2, suffix1);
            return internalCreate(size + 1, prefix1, prefix2, prefix3, prefix4, data, suffix4, suffix3, newSuffix2, newSuffix1);
        }
        if (suffix3.length < WIDTH - 1) {
            Object[] newSuffix1 = wrap1(value);
            Object[][][] newSuffix3 = copyAppend(suffix3, suffix2, suffix1);
            return internalCreate(size + 1, prefix1, prefix2, prefix3, prefix4, data, suffix4, newSuffix3, EMPTY2, newSuffix1);
        }
        if (suffix4.length < WIDTH - 1) {
            Object[] newSuffix1 = wrap1(value);
            Object[][][][] newSuffix4 = copyAppend(suffix4, suffix3, suffix2, suffix1);
            return internalCreate(size + 1, prefix1, prefix2, prefix3, prefix4, data, newSuffix4, EMPTY3, EMPTY2, newSuffix1);
        }
        if (data.length < WIDTH - 2) {
            Object[] newSuffix1 = wrap1(value);
            Object[][][][][] newData = copyAppend(data, suffix4, suffix3, suffix2, suffix1);
            return internalCreate(size + 1, prefix1, prefix2, prefix3, prefix4, newData, EMPTY4, EMPTY3, EMPTY2, newSuffix1);
        }
        Object[] newSuffix1 = wrap1(value);
        Object[][][][][] newSuffix5 = wrap5(copyAppend(suffix4, suffix3, suffix2, suffix1));
        return TreeList6.internalCreate(size + 1, prefix1, prefix2, prefix3, prefix4, data, EMPTY6, newSuffix5, EMPTY4, EMPTY3, EMPTY2, newSuffix1);
    }

    @Override
    public @NotNull IList<V> prepending(@NotNull V value)
    {
        if (prefix1.length < WIDTH) {
            Object[] newPrefix1 = copyPrepend1(value, prefix1);
            return internalCreate(size + 1, newPrefix1, prefix2, prefix3, prefix4, data, suffix4, suffix3, suffix2, suffix1);
        }
        if (prefix2.length < WIDTH - 1) {
            Object[] newPrefix1 = wrap1(value);
            Object[][] newPrefix2 = copyPrepend(prefix1, prefix2);
            return internalCreate(size + 1, newPrefix1, newPrefix2, prefix3, prefix4, data, suffix4, suffix3, suffix2, suffix1);
        }
        if (prefix3.length < WIDTH - 1) {
            Object[] newPrefix1 = wrap1(value);
            Object[][][] newPrefix3 = copyPrepend(prefix1, prefix2, prefix3);
            return internalCreate(size + 1, newPrefix1, EMPTY2, newPrefix3, prefix4, data, suffix4, suffix3, suffix2, suffix1);
        }
        if (prefix4.length < WIDTH - 1) {
            Object[] newPrefix1 = wrap1(value);
            Object[][][][] newPrefix4 = copyPrepend(prefix1, prefix2, prefix3, prefix4);
            return internalCreate(size + 1, newPrefix1, EMPTY2, EMPTY3, newPrefix4, data, suffix4, suffix3, suffix2, suffix1);
        }
        if (data.length < WIDTH - 2) {
            Object[] newPrefix1 = wrap1(value);
            Object[][][][][] newData = copyPrepend(prefix1, prefix2, prefix3, prefix4, data);
            return internalCreate(size + 1, newPrefix1, EMPTY2, EMPTY3, EMPTY4, newData, suffix4, suffix3, suffix2, suffix1);
        }
        Object[] newPrefix1 = wrap1(value);
        Object[][][][][] newPrefix5 = wrap5(copyPrepend(prefix1, prefix2, prefix3, prefix4));
        return TreeList6.internalCreate(size + 1, newPrefix1, EMPTY2, EMPTY3, EMPTY4, newPrefix5, EMPTY6, data, suffix4, suffix3, suffix2, suffix1);
    }

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        Object[] newSuffix1 = append1IfSpace(suffix1, values, WIDTH - suffix1.length);
        if (newSuffix1 != null) {
            int newSize = size - suffix1.length + newSuffix1.length;
            return internalCreate(newSize, prefix1, prefix2, prefix3, prefix4, data, suffix4, suffix3, suffix2, newSuffix1);
        }
        return ListOperations.appendingAll(this, values);
    }

    @Override
    public @NotNull <R> IList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        Object[] mprefix1 = ArraySupport.map1(prefix1, mapper);
        Object[][] mprefix2 = ArraySupport.map2(prefix2, mapper);
        Object[][][] mprefix3 = ArraySupport.map3(prefix3, mapper);
        Object[][][][] mprefix4 = ArraySupport.map4(prefix4, mapper);
        Object[][][][][] mdata = ArraySupport.map5(data, mapper);
        Object[][][][] msuffix4 = ArraySupport.map4(suffix4, mapper);
        Object[][][] msuffix3 = ArraySupport.map3(suffix3, mapper);
        Object[][] msuffix2 = ArraySupport.map2(suffix2, mapper);
        Object[] msuffix1= ArraySupport.map1(suffix1, mapper);
        return new TreeList5<>(size, mprefix1, mprefix2, mprefix3, mprefix4, mdata, msuffix4, msuffix3, msuffix2, msuffix1);
    }
}
