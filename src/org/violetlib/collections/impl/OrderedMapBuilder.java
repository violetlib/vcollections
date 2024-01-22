/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.violetlib.collections.Binding;
import org.violetlib.collections.IList;
import org.violetlib.collections.IMap;
import org.violetlib.collections.MapBuilder;

import org.jetbrains.annotations.*;

/**

*/

public final class OrderedMapBuilder<K,V>
  implements MapBuilder<K,V>
{
    public static <K,V> MapBuilder<K,V> create()
    {
        return new OrderedMapBuilder<>();
    }

    private final @NotNull List<K> keys;
    private final @NotNull Map<K,V> bindings;

    private OrderedMapBuilder()
    {
        keys = new ArrayList<>();
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
        if (!keys.contains(key)) {
            keys.add(key);
        }

        bindings.put(key, value);
    }

    @Override
    public void add(@NotNull Binding<? extends K,? extends V> binding)
    {
        K key = binding.getKey();
        V value = binding.getValue();
        put(key, value);
    }

    @Override
    public void reset()
    {
        keys.clear();
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
        return OrderedMapImpl.create(IList.create(keys), Impl.createMap(bindings));
    }
}
