/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.IList;

import org.jetbrains.annotations.*;

/**
  An empty list that creates a prepend oriented list when prepended and an append oriented list when appended.
*/

/* package private */ final class EmptyListImpl<V>
  extends EmptyListBase<V>
{
    /* package private */ static <V> @NotNull IList<V> empty()
    {
        return (IList<V>) EMPTY;
    }

    private static final @NotNull IList<Object> EMPTY = new EmptyListImpl<>();

    @Override
    public @NotNull IList<V> appending(@NotNull V value)
    {
        return AppendOrientedList.create(value);
    }

    @Override
    public @NotNull IList<V> prepending(@NotNull V value)
    {
        return PrependOrientedList.create(value);
    }

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        return AppendOrientedList.createWithElements(values);
    }

    @Override
    public @NotNull IList<V> insertingAll(int position, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        if (position != 0) {
            throw new IndexOutOfBoundsException("Empty list");
        }
        return AppendOrientedList.createWithElements(values);
    }
}
