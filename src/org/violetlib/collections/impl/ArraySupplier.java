/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.function.Supplier;

import org.jetbrains.annotations.*;

/**

*/

public final class ArraySupplier<V>
  implements Supplier<V>
{
    public static <V> @Nullable Supplier<V> create(@NotNull Object array)
    {
        Class<?> aclass = array.getClass();
        if (!aclass.isArray()) {
            return null;
        }
        if (aclass.getComponentType().isPrimitive()) {
            return null;
        }
        return new ArraySupplier<>(array);
    }

    private final Object @NotNull [] array;
    private int index;

    private ArraySupplier(@NotNull Object array)
    {
        this.array = (Object[]) array;
    }

    @Override
    public V get()
    {
        return (V) array[index++];
    }
}
