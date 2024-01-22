/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.HashMap;
import java.util.Map;

import org.violetlib.collections.Binding;
import org.violetlib.collections.IMap;
import org.violetlib.collections.MapBuilder;

import org.jetbrains.annotations.*;

/**
  A simple implementation of a map builder.
*/

public final class MapBuilderImpl<K,V>
  implements MapBuilder<K,V>
{
    public static <K,V> @NotNull MapBuilder<K,V> create()
    {
        return new MapBuilderImpl<>();
    }

    private final @NotNull Map<K,V> bindings;

    private MapBuilderImpl()
    {
        bindings = new HashMap<>();
    }

    @Override
    public boolean containsKey(@NotNull K key)
    {
        return bindings.containsKey(key);
    }

    @Override
    public @Nullable V get(@NotNull K key)
    {
        return bindings.get(key);
    }

    @Override
    public void put(@NotNull K key, @NotNull V value)
    {
        bindings.put(key, value);
    }

    @Override
    public void add(@NotNull Binding<? extends K,? extends V> binding)
    {
        bindings.put(binding.getKey(), binding.getValue());
    }

    @Override
    public void reset()
    {
        bindings.clear();
    }

    @Override
    public boolean isEmpty()
    {
        return bindings.isEmpty();
    }

    @Override
    public int size()
    {
        return bindings.size();
    }

    @Override
    public @NotNull IMap<K,V> value()
    {
        return Impl.createMap(bindings);
    }
}
