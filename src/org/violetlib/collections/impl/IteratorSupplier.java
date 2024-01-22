/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Iterator;
import java.util.function.Supplier;

import org.jetbrains.annotations.*;

/**

*/

public final class IteratorSupplier<V>
  implements Supplier<V>
{
    public static <V> @NotNull Supplier<V> create(@NotNull Iterator<V> iterator)
    {
        return new IteratorSupplier<>(iterator);
    }

    private final @NotNull Iterator<V> iterator;

    private IteratorSupplier(@NotNull Iterator<V> iterator)
    {
        this.iterator = iterator;
    }

    @Override
    public V get()
    {
        return iterator.next();
    }
}
