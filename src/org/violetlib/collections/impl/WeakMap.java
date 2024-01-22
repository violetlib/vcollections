/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Map;
import java.util.WeakHashMap;

import org.violetlib.collections.Binding;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IMap;
import org.violetlib.collections.ISet;
import org.violetlib.util.Extensions;
import org.violetlib.util.VObjects;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  A simple implementation of an immutable map using weak references for the keys. Null keys and values are not
  permitted.
*/

public @Immutable class WeakMap<K,V>
  implements IMap<K,V>
{
    public static <K,V> @NotNull WeakMap<K,V> empty()
    {
        return (WeakMap<K,V>) EMPTY;
    }

    public static <K,V> @NotNull WeakMap<K,V> from(@NotNull Map<? extends K,? extends V> map)
    {
        return new WeakMap<>(map);
    }

    private static final @NotNull WeakMap<Object,Object> EMPTY = new WeakMap<>();

    private final @NotNull Map<K,V> data;

    private WeakMap()
    {
        this.data = new WeakHashMap<>();
    }

    private WeakMap(@NotNull Map<? extends K,? extends V> initialBindings)
    {
        data = new WeakHashMap<>(initialBindings);
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
        for (Map.Entry<K,V> entry : data.entrySet()) {
            visitor.visit(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor, @Nullable R defaultResult)
    {
        for (Map.Entry<K,V> entry : data.entrySet()) {
            R result = visitor.visit(entry.getKey(), entry.getValue());
            if (result != null) {
                return result;
            }
        }
        return defaultResult;
    }

    @Override
    public @NotNull ISet<K> keySet()
    {
        return ISet.create(data.keySet());
    }

    @Override
    public @NotNull ISet<V> values()
    {
        return ISet.create(data.values());
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull K key, @Nullable V value)
    {
        V existingValue = data.get(key);
        if (VObjects.equals(existingValue, value)) {
            return this;
        } else {
            WeakMap<K,V> result = new WeakMap<>(data);
            if (value != null) {
                result.data.put(key, value);
            } else {
                result.data.remove(key);
            }
            return result;
        }
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull IMap<K,V> delta)
    {
        Map<K,V> result = new WeakHashMap<>(data);
        delta.visit(result::put);
        return new WeakMap<>(result);
    }

    @Override
    public int hashCode()
    {
        return MapEquality.computeHashCode(this);
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

        return MapEquality.isEqual(this, otherMap);
    }
}
