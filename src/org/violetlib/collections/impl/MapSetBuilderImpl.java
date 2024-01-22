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
import org.violetlib.collections.ICollection;
import org.violetlib.collections.IList;
import org.violetlib.collections.IMap;
import org.violetlib.collections.ISet;
import org.violetlib.collections.MapBuilder;
import org.violetlib.collections.MapSetBuilder;
import org.violetlib.collections.SetBuilder;

import org.jetbrains.annotations.*;

/**
  Build an immutable map from keys of type K to immutable sets of values of type V. Null values are not supported.
*/

public final class MapSetBuilderImpl<K,V>
  implements MapSetBuilder<K,V>
{
    /**
      Create an empty map builder.
    */

    public static <K,V> @NotNull MapSetBuilder<K,V> create(boolean createOrderedSets)
    {
        return new MapSetBuilderImpl<>(createOrderedSets);
    }

    private final @NotNull Map<K,SetBuilder<V>> map;
    private final boolean createOrderedSets;

    private MapSetBuilderImpl(boolean createOrderedSets)
    {
        map = new HashMap<>();
        this.createOrderedSets = createOrderedSets;
    }

    @Override
    public int size()
    {
        return map.size();
    }

    @Override
    public void add(@NotNull K key, @NotNull V value)
    {
        SetBuilder<V> b = map.get(key);
        if (b == null) {
            b = createOrderedSets ? ISet.builder(ISet.ORDERED) : ISet.builder();
            map.put(key, b);
        }
        b.add(value);
    }

    @Override
    public void addAll(@NotNull K key, @NotNull ICollection<V> values)
    {
        if (!values.isEmpty()) {
            SetBuilder<V> b = map.get(key);
            if (b == null) {
                b = ISet.builder();
                map.put(key, b);
            }
            b.addAll(values);
        }
    }

    @Override
    public void addAll(@NotNull IMap<K,ISet<V>> values)
    {
        for (Binding<K,ISet<V>> b : values) {
            addAll(b.getKey(), b.getValue());
        }
    }

    @Override
    public int getValueCount(@NotNull K key)
    {
        SetBuilder<V> b = map.get(key);
        return b != null ? b.size() : 0;
    }

    @Override
    public @NotNull ISet<V> getValues(@NotNull K key)
    {
        SetBuilder<V> b = map.get(key);
        return b != null ? b.values() : ISet.empty();
    }

    @Override
    public @NotNull IList<K> keys()
    {
        return IList.create(map.keySet());
    }

    @Override
    public @NotNull IMap<K,ISet<V>> value()
    {
        MapBuilder<K,ISet<V>> b = IMap.builder();
        for (Map.Entry<K,SetBuilder<V>> e : map.entrySet()) {
            b.put(e.getKey(), e.getValue().values());
        }
        return b.value();
    }
}
