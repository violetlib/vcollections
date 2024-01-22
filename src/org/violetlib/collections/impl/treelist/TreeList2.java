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
  The tree list representation of a tree of depth 2.
*/

public final class TreeList2<V>
  extends TreeListSlicesImpl<V>
{
    /**
      Create a tree of depth 2 by concatenating an array of elements and a single element.
      @param prefix A valid prefix array.
      @param suffix The suffix element.
    */

    static <V> @NotNull IList<V> internalCreate(Object @NotNull [] prefix, @NotNull V suffix)
    {
        Object[] suffixArray = new Object[1];
        suffixArray[0] = suffix;
        int size = prefix.length + 1;
        return new TreeList2<>(size, prefix, EMPTY2, suffixArray);
    }

    /**
      Create a tree of depth 2 by concatenating a single element and an array of elements.
      @param prefix The prefix element.
      @param suffix A valid suffix array.
    */

    static <V> @NotNull IList<V> internalCreate(@NotNull V prefix, Object @NotNull [] suffix)
    {
        Object[] prefixArray = new Object[1];
        prefixArray[0] = prefix;
        int size = suffix.length + 1;
        return new TreeList2<>(size, prefixArray, EMPTY2, suffix);
    }

    static <V> @NotNull IList<V> internalCreate(int size,
                                                Object @NotNull [] prefix,
                                                Object @NotNull [][] data,
                                                Object @NotNull [] suffix)
    {
        return new TreeList2<>(size, prefix, data, suffix);
    }

    // The prefix and suffix arrays may not be full?

    private final Object @NotNull [] prefix;
    private final Object @NotNull [][] data;
    private final Object @NotNull [] suffix;

    private TreeList2(int size,
                      Object @NotNull [] prefix,
                      Object @NotNull [][] data,
                      Object @NotNull [] suffix)
    {
        super(size);

        assert prefix.length <= WIDTH;
        assert suffix.length <= WIDTH;

        this.prefix = prefix;
        this.data = data;
        this.suffix = suffix;
    }

    @Override
    public int getSliceCount()
    {
        return 3;
    }

    @Override
    public Object @NotNull [] getSlice(int sliceIndex)
    {
        switch (sliceIndex) {
            case 0: return prefix;
            case 1: return data;
            case 2: return suffix;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    public int getSlicePrefixLength(int sliceIndex)
    {
        switch (sliceIndex) {
            case 0: return prefix.length;
            case 1: return size - suffix.length;
            case 2: return size;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    public int getSliceDepth(int sliceIndex)
    {
        switch (sliceIndex) {
            case 0: return 1;
            case 1: return 2;
            case 2: return 1;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    public int getSliceElementCount(int sliceIndex)
    {
        switch (sliceIndex) {
            case 0: return prefix.length;
            case 1: return data.length * WIDTH;
            case 2: return suffix.length;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    protected @NotNull V internalGet(int index)
    {
        int prefixLength = prefix.length;
        if (index < prefixLength) {
            return (V) prefix[index];
        }
        int offset = index - prefixLength;
        int i2 = offset >>> BITS;
        int i1 = offset & MASK;
        if (i2 < data.length) {
            return (V) data[i2][i1];
        }
        return (V) suffix[i1];
    }

    @Override
    public @NotNull IList<V> appending(@NotNull V value)
    {
        if (suffix.length < WIDTH) {
            Object[] newSuffix = copyAppend1(suffix, value);
            return internalCreate(size+1, prefix, data, newSuffix);
        }
        if (data.length < WIDTH - 2) {
            Object[][] newData = copyAppend(data, suffix);
            Object[] newSuffix = wrap1(value);
            return internalCreate(size+1, prefix, newData, newSuffix);
        }
        return TreeList3.internalCreate(size+1, prefix, data, EMPTY3, wrap2(suffix), wrap1(value));
    }

    @Override
    public @NotNull IList<V> prepending(@NotNull V value)
    {
        if (prefix.length < WIDTH) {
            Object[] newPrefix = copyPrepend1(value, prefix);
            return internalCreate(size+1, newPrefix, data, suffix);
        }
        if (data.length < WIDTH - 2) {
            Object[] newPrefix = wrap1(value);
            Object[][] newData = copyPrepend(prefix, data);
            return internalCreate(size+1, newPrefix, newData, suffix);
        }
        return TreeList3.internalCreate(size+1, wrap1(value), wrap2(prefix), EMPTY3, data, suffix);
    }

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        Object[] newSuffix = append1IfSpace(suffix, values, WIDTH - suffix.length);
        if (newSuffix != null) {
            int newSize = size - suffix.length + newSuffix.length;
            return internalCreate(newSize, prefix, data, newSuffix);
        }
        return ListOperations.appendingAll(this, values);
    }

    @Override
    public @NotNull <R> IList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        Object[] newPrefix = map1(prefix, mapper);
        Object[][] newData = map2(data, mapper);
        Object[] newSuffix = map1(suffix, mapper);
        return new TreeList2<>(size, newPrefix, newData, newSuffix);
    }
}
