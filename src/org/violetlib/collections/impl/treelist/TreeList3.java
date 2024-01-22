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
  The tree list representation of a tree of depth 3.
*/

public final class TreeList3<V>
  extends TreeListSlicesImpl<V>
{
    static <V> @NotNull IList<V> internalCreate(int size,
                                                Object @NotNull [] prefix1,
                                                Object @NotNull [][] prefix2,
                                                Object @NotNull [][][] data,
                                                Object @NotNull [][] suffix2,
                                                Object @NotNull [] suffix1)
    {
        return new TreeList3<>(size, prefix1, prefix2, data, suffix2, suffix1);
    }

    private final Object @NotNull [] prefix1;
    private final Object @NotNull [][] prefix2;
    private final Object @NotNull [][][] data;
    private final Object @NotNull [][] suffix2;
    private final Object @NotNull [] suffix1;

    private final int len12;

    private TreeList3(int size,
                      Object @NotNull [] prefix1,
                      Object @NotNull [][] prefix2,
                      Object @NotNull [][][] data,
                      Object @NotNull [][] suffix2,
                      Object @NotNull [] suffix1)
    {
        super(size);

        assert prefix1.length <= WIDTH;
        assert suffix1.length <= WIDTH;

        this.prefix1 = prefix1;
        this.prefix2 = prefix2;
        this.data = data;
        this.suffix2 = suffix2;
        this.suffix1 = suffix1;
        this.len12 = prefix1.length + prefix2.length * WIDTH;
    }

    @Override
    public int getSliceCount()
    {
        return 5;
    }

    @Override
    public Object @NotNull [] getSlice(int sliceIndex)
    {
        switch (sliceIndex) {
            case 0: return prefix1;
            case 1: return prefix2;
            case 2: return data;
            case 3: return suffix2;
            case 4: return suffix1;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    public int getSlicePrefixLength(int sliceIndex)
    {
        switch (sliceIndex) {
            case 0: return prefix1.length;
            case 1: return len12;
            case 2: return len12 + data.length * WIDTH2;
            case 3: return size - suffix1.length;
            case 4: return size;
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
            case 3: return 2;
            case 4: return 1;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    public int getSliceElementCount(int sliceIndex)
    {
        switch (sliceIndex) {
            case 0: return prefix1.length;
            case 1: return prefix2.length * WIDTH;
            case 2: return data.length * WIDTH2;
            case 3: return suffix2.length * WIDTH;
            case 4: return suffix1.length;
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
        int offset = index - len12;
        int i3 = offset >>> BITS2;
        int i2 = (offset >>> BITS) & MASK;
        int i1 = offset & MASK;
        if (i3 < data.length) {
            return (V) data[i3][i2][i1];
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
            return internalCreate(size + 1, prefix1, prefix2, data, suffix2, newSuffix1);
        }
        if (suffix2.length < WIDTH - 1) {
            Object[] newSuffix1 = wrap1(value);
            Object[][] newSuffix2 = copyAppend(suffix2, suffix1);
            return internalCreate(size + 1, prefix1, prefix2, data, newSuffix2, newSuffix1);
        }
        if (data.length < WIDTH - 2) {
            Object[] newSuffix1 = wrap1(value);
            Object[][][] newData = copyAppend(data, suffix2, suffix1);
            return internalCreate(size + 1, prefix1, prefix2, newData, EMPTY2, newSuffix1);
        }
        Object[] newSuffix1 = wrap1(value);
        Object[][][] newSuffix3 = wrap3(copyAppend(suffix2, suffix1));
        return TreeList4.internalCreate(size + 1, prefix1, prefix2, data, EMPTY4, newSuffix3, EMPTY2, newSuffix1);
    }

    @Override
    public @NotNull IList<V> prepending(@NotNull V value)
    {
        if (prefix1.length < WIDTH) {
            Object[] newPrefix1 = copyPrepend1(value, prefix1);
            return internalCreate(size + 1, newPrefix1, prefix2, data, suffix2, suffix1);
        }
        if (prefix2.length < WIDTH - 1) {
            Object[] newPrefix1 = wrap1(value);
            Object[][] newPrefix2 = copyPrepend(prefix1, prefix2);
            return internalCreate(size + 1, newPrefix1, newPrefix2, data, suffix2, suffix1);
        }
        if (data.length < WIDTH - 2) {
            Object[] newPrefix1 = wrap1(value);
            Object[][][] newData = copyPrepend(prefix1, prefix2, data);
            return internalCreate(size + 1, newPrefix1, EMPTY2, newData, suffix2, suffix1);
        }
        Object[] newPrefix1 = wrap1(value);
        Object[][][] newPrefix3 = wrap3(copyPrepend(prefix1, prefix2));
        return TreeList4.internalCreate(size + 1, newPrefix1, EMPTY2, newPrefix3, EMPTY4, data, suffix2, suffix1);
    }

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        Object[] newSuffix1 = append1IfSpace(suffix1, values, WIDTH - suffix1.length);
        if (newSuffix1 != null) {
            int newSize = size - suffix1.length + newSuffix1.length;
            return internalCreate(newSize, prefix1, prefix2, data, suffix2, newSuffix1);
        }
        return ListOperations.appendingAll(this, values);
    }

    @Override
    public @NotNull <R> IList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        Object[] mprefix1 = map1(prefix1, mapper);
        Object[][] mprefix2 = map2(prefix2, mapper);
        Object[][][] mdata = map3(data, mapper);
        Object[][] msuffix2 = map2(suffix2, mapper);
        Object[] msuffix1= map1(suffix1, mapper);
        return new TreeList3<>(size, mprefix1, mprefix2, mdata, msuffix2, msuffix1);
    }
}
