/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.collections.IList;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.impl.rrblist.RRBVectorBuilder;

import java.util.stream.Collector;

/**
  A list builder that builds a relaxed radix balanced tree.
*/

public final class RRBListBuilder<V>
  extends RRBVectorBuilder<V>
  implements ListBuilder<V>
{
    public static <V> @NotNull RRBListBuilder<V> create()
    {
        return new RRBListBuilder<>();
    }

    public static <V> @NotNull Collector<V,ListBuilder<V>,IList<V>> collector()
    {
        return RRBList.collector();
    }

    private @Nullable V lastItem;

    private RRBListBuilder()
    {
    }

    @Override
    public final void reset()
    {
        clear();
    }

    @Override
    public void clear()
    {
        super.clear();
        lastItem = null;
    }

    public @Nullable V lastItem()
    {
        return lastItem;
    }

    @Override
    public void add(@NotNull V item)
    {
        super.add(item);
        lastItem = item;
    }

    @Override
    public @NotNull IList<V> values()
    {
        return (RRBList<V>) asVector();
    }

    @Override
    protected @NotNull RRBList<V> createEmptyResult()
    {
        return RRBList.empty();
    }

    @Override
    protected @NotNull RRBList<V> createResult(int size, Object @NotNull [] root, int depth)
    {
        return new RRBList<>(size, root, depth);
    }
}
