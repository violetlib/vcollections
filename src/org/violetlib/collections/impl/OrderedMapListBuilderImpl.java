/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.violetlib.collections.IList;
import org.violetlib.collections.IMap;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.MapBuilder;
import org.violetlib.collections.MapListBuilder;

import org.jetbrains.annotations.*;

/**
  Build an immutable map from keys of type K to immutable lists of values of type V. Null values are not supported.
*/

public final class OrderedMapListBuilderImpl<K,V>
  implements MapListBuilder<K,V>
{
    /**
      Create a map builder. The key order in the map will be based on the order in which the keys were first added to
      this builder.
    */

    public static <K,V> MapListBuilder<K,V> create()
    {
        return new OrderedMapListBuilderImpl<>(null);
    }

    /**
      Create a map builder. The key order in the map will be based on the specified key comparator.
    */

    public static <K,V> MapListBuilder<K,V> create(@NotNull Comparator<? super K> keyComparator)
    {
        return new OrderedMapListBuilderImpl<>(keyComparator);
    }

    private final @NotNull List<K> keys;
    private final @NotNull Map<K,ListBuilder<V>> map;
    private final @Nullable Comparator<? super K> keyComparator;

    private OrderedMapListBuilderImpl(@Nullable Comparator<? super K> keyComparator)
    {
        keys = new ArrayList<>();
        map = new HashMap<>();
        this.keyComparator = keyComparator;
    }

    /**
      Return the number of keys with associated values.
    */

    @Override
    public int size()
    {
        return map.size();
    }

    /**
      Add an element at the end of the list associated with the specified key.

      @param key The key.
      @param value The value to be added.
    */

    @Override
    public void add(@NotNull K key, @NotNull V value)
    {
        ListBuilder<V> b = map.get(key);
        if (b == null) {
            b = IList.builder();
            map.put(key, b);
            keys.add(key);
        }
        b.add(value);
    }

    /**
      Add elements at the end of the list associated with the specified key.

      @param key The key.
      @param values The values to be added.
    */

    @Override
    public void addAll(@NotNull K key, @NotNull IList<V> values)
    {
        if (!values.isEmpty()) {
            ListBuilder<V> b = map.get(key);
            if (b == null) {
                b = IList.builder();
                map.put(key, b);
                keys.add(key);
            }
            b.addAll(values);
        }
    }

    /**
      Return the number of values associated with the specified key.

      @param key The key.

      @return the number of values associated with <code>key</code>.
    */

    @Override
    public int getValueCount(@NotNull K key)
    {
        ListBuilder<V> b = map.get(key);
        return b != null ? b.size() : 0;
    }

    /**
      Return the values currently associated with the specified key.

      @param key The key.

      @return an immutable list that contains the values associated with the specified key.
    */

    @Override
    public @NotNull IList<V> getValues(@NotNull K key)
    {
        ListBuilder<V> b = map.get(key);
        return b != null ? b.values() : IList.empty();
    }

    /**
      Return the keys that have associated values.

      @return the keys, in the order defined for the map.
    */

    @Override
    public @NotNull IList<K> keys()
    {
        IList<K> ks = IList.create(keys);
        if (keyComparator != null) {
            return ks.sort(keyComparator);
        } else {
            return ks;
        }
    }

    /**
      Return an ordered map containing the keys bound to lists of values.
    */

    @Override
    public @NotNull IMap<K,IList<V>> value()
    {
        MapBuilder<K,IList<V>> b = IMap.builder(IMap.ORDERED);
        if (keyComparator != null) {
            keys.sort(keyComparator);
        }
        for (K key : keys) {
            IList<V> values = map.get(key).values();
            b.put(key, values);
        }
        return b.value();
    }
}
