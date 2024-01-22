/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Map;

import org.violetlib.collections.Binding;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;
import org.violetlib.collections.IMap;
import org.violetlib.collections.ISet;
import org.violetlib.util.Extensions;
import org.violetlib.util.VObjects;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  A simple implementation of a map with a defined iteration order of keys. The iteration order does not affect the
  hash code or the definition of equals.

  @param <K> The type of the keys.
  @param <V> The type of the values.
*/

public final @Immutable class OrderedMapImpl<K,V>
  implements IMap<K,V>
{
    public static <K,V> @NotNull IMap<K,V> create(@NotNull IList<K> keys, @NotNull IMap<K,V> data)
    {
        return new OrderedMapImpl<>(keys, data);
    }

    private final @NotNull IList<K> keys;
    private final @NotNull IMap<K,V> data;

    private OrderedMapImpl(@NotNull IList<K> keys, @NotNull IMap<K,V> data)
    {
        this.keys = keys;
        this.data = data;
    }

    @Override
    public @NotNull IIterator<Binding<K,V>> iterator()
    {
        return IMapBindingIterator.create(this);
    }

    @Override
    public boolean isEmpty()
    {
        return data.isEmpty();
    }

    @Override
    public int size()
    {
        return data.size();
    }

    @Override
    public @Nullable V get(@NotNull K key)
    {
        return data.get(key);
    }

    @Override
    public boolean containsKey(@NotNull Object key)
    {
        return data.containsKey(key);
    }

    @Override
    public void visit(@NotNull Visitor<K,V> visitor)
    {
        for (K key : keys) {
            V value = data.get(key);
            assert value != null;
            visitor.visit(key, value);
        }
    }

    @Override
    public <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor, @Nullable R defaultResult)
    {
        for (K key : keys) {
            V value = data.get(key);
            assert value != null;
            R result = visitor.visit(key, value);
            if (result != null) {
                return result;
            }
        }
        return defaultResult;
    }

    @Override
    public @NotNull ISet<K> keySet()
    {
        return SimpleOrderedSet.create(keys);
    }

    @Override
    public @NotNull ISet<V> values()
    {
        return data.values();
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull K key, @Nullable V value)
    {
        V existingValue = data.get(key);
        if (VObjects.equals(existingValue, value)) {
            return this;
        } else {
            return new OrderedMapImpl<>(keys.appending(key), data.extending(key, value));
        }
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull IMap<K,V> delta)
    {
        IMap<K,V> result = this;
        for (K key : delta.keySet()) {
            V value = delta.get(key);
            assert value != null;
            result = result.extending(key, value);
        }
        return result;
    }

    @Override
    public @NotNull Map<K,V> asJavaMap()
    {
        return data.asJavaMap();
    }

    @Override
    public int hashCode()
    {
        return data.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        IMap<?,?> otherMap = Extensions.getExtension(obj, IMap.class);
        if (otherMap == null) {
            return false;
        }

        return MapEquality.isEqual(data, otherMap);
    }
}
