/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.ListBuilder;

import org.jetbrains.annotations.*;

/**

*/

public final class AppendOrientedListBuilder<V>
  implements ListBuilder<V>
{
    public static <V> @NotNull ListBuilder<V> create()
    {
        return new AppendOrientedListBuilder<>();
    }

    private @NotNull AppendOrientedList<V> value = EmptyReverseList.empty();
    private @Nullable V lastItem;

    private AppendOrientedListBuilder()
    {
    }

    @Override
    public void add(@NotNull V e)
    {
        value = value.appending(e);
        lastItem = e;
    }

    @Override
    public void reset()
    {
        value = EmptyReverseList.empty();
        lastItem = null;
    }

    @Override
    public boolean isEmpty()
    {
        return lastItem == null;
    }

    @Override
    public int size()
    {
        return value.size();
    }

    @Override
    public @Nullable V lastItem()
    {
        return lastItem;
    }

    @Override
    public @NotNull AppendOrientedList<V> values()
    {
        return value;
    }
}
