/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.Binding;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**

*/

public final @Immutable class BindingImpl<K,V>
  implements Binding<K,V>
{
    public static <K,V> @NotNull Binding<K,V> create(@NotNull K key, @NotNull V value)
    {
        return new BindingImpl<>(key, value);
    }

    private final @NotNull K key;
    private final @NotNull V value;

    private BindingImpl(@NotNull K key, @NotNull V value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public @NotNull K getKey()
    {
        return key;
    }

    @Override
    public @NotNull V getValue()
    {
        return value;
    }
}
