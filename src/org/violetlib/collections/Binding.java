/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import javax.annotation.CheckReturnValue;

import org.violetlib.collections.impl.BindingImpl;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  An immutable binding of a key to a value.
  Null keys and values are not permitted.
*/

public @Immutable @CheckReturnValue interface Binding<K,V>
{
    static <K,V> @NotNull Binding<K,V> create(@NotNull K key, @NotNull V value)
    {
        return BindingImpl.create(key, value);
    }

    @NotNull K getKey();

    @NotNull V getValue();
}
