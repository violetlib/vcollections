/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.violetlib.collections.IList;
import org.violetlib.collections.IMap;
import org.violetlib.collections.KeyedValueSorter;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.MapBuilder;
import org.violetlib.collections.UnorderedMapListBuilder;

import org.jetbrains.annotations.*;

/**
  Build an immutable map from keys of type K to immutable lists of values of type V. Null values are not supported.
*/

public final class MapListBuilderImpl<K,V>
  implements UnorderedMapListBuilder<K,V>
{
    /**
      Create an empty map builder.
    */

    public static <K,V> @NotNull UnorderedMapListBuilder<K,V> create()
    {
        return new MapListBuilderImpl<>();
    }

    private final @NotNull Map<K,ListBuilder<V>> map;

    private MapListBuilderImpl()
    {
        map = new HashMap<>();
    }

    @Override
    public int size()
    {
        return map.size();
    }

    @Override
    public void add(@NotNull K key, @NotNull V value)
    {
        ListBuilder<V> b = map.get(key);
        if (b == null) {
            b = IList.builder();
            map.put(key, b);
        }
        b.add(value);
    }

    @Override
    public void addAll(@NotNull K key, @NotNull IList<V> values)
    {
        if (!values.isEmpty()) {
            ListBuilder<V> b = map.get(key);
            if (b == null) {
                b = IList.builder();
                map.put(key, b);
            }
            b.addAll(values);
        }
    }

    @Override
    public int getValueCount(@NotNull K key)
    {
        ListBuilder<V> b = map.get(key);
        return b != null ? b.size() : 0;
    }

    @Override
    public @NotNull IList<V> getValues(@NotNull K key)
    {
        ListBuilder<V> b = map.get(key);
        return b != null ? b.values() : IList.empty();
    }

    @Override
    public @NotNull IList<K> keys()
    {
        return IList.create(map.keySet());
    }

    @Override
    public @NotNull IMap<K,IList<V>> value()
    {
        MapBuilder<K,IList<V>> b = IMap.builder();
        for (Map.Entry<K,ListBuilder<V>> e : map.entrySet()) {
            b.put(e.getKey(), e.getValue().values());
        }
        return b.value();
    }

    @Override
    public @NotNull IMap<K,IList<V>> sort()
    {
        MapBuilder<K,IList<V>> b = IMap.builder();
        for (Map.Entry<K,ListBuilder<V>> e : map.entrySet()) {
            b.put(e.getKey(), e.getValue().values().sort());
        }
        return b.value();
    }

    @Override
    public @NotNull IMap<K,IList<V>> sort(@NotNull Comparator<? super V> comparator)
    {
        MapBuilder<K,IList<V>> b = IMap.builder();
        for (Map.Entry<K,ListBuilder<V>> e : map.entrySet()) {
            b.put(e.getKey(), e.getValue().values().sort(comparator));
        }
        return b.value();
    }

    @Override
    public @NotNull IMap<K,IList<V>> sort(@NotNull KeyedValueSorter<K,V> sorter)
    {
        MapBuilder<K,IList<V>> b = IMap.builder();
        for (Map.Entry<K,ListBuilder<V>> e : map.entrySet()) {
            K key = e.getKey();
            b.put(key, e.getValue().values().sort(sorter.getComparatorForKey(key)));
        }
        return b.value();
    }
}
