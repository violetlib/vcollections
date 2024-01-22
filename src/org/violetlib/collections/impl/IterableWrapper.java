/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.jetbrains.annotations.NotNull;
import org.violetlib.collections.IIterable;
import org.violetlib.collections.IIterator;

/**

*/

public final class IterableWrapper<T>
  implements IIterable<T>
{
    public static <T> @NotNull IIterable<T> create(@NotNull Iterable<T> it)
    {
        return new IterableWrapper<>(it);
    }

    private final @NotNull Iterable<T> it;

    private IterableWrapper(@NotNull Iterable<T> it)
    {
        this.it = it;
    }

    @Override
    public @NotNull IIterator<T> iterator()
    {
        return IIteratorImpl.create(it.iterator());
    }
}
