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
import org.violetlib.collections.IMap;
import org.violetlib.collections.ISet;
import org.violetlib.collections.SetBuilder;
import org.violetlib.util.Extensions;
import org.violetlib.util.VObjects;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  An implementation of an immutable map using an array. Intended for small maps.

  @param <K> The type of the keys.
  @param <V> The type of the values.
*/

public final @Immutable class ArrayMapImpl<K,V>
  implements IMap<K,V>
{
    public static <K,V> @NotNull IMap<K,V> create(@NotNull Map<? extends K, ? extends V> bindings)
    {
        return new ArrayMapImpl<>(bindings);
    }

    private final @NotNull Object @NotNull [] data;
    private volatile ISet<K> keySet;
    private volatile ISet<V> valueSet;

    private ArrayMapImpl(@NotNull Map<? extends K, ? extends V> bindings)
    {
        int count = bindings.size();
        Object[] data = new Object[count * 2];
        int index = 0;
        for (Map.Entry<? extends K, ? extends V> entry : bindings.entrySet()) {
            K key = entry.getKey();
            if (key != null) {
                V value = entry.getValue();
                if (value != null) {
                    data[index++] = key;
                    data[index++] = value;
                }
            }
        }
        if (index != count * 2) {
            // should not happen
            Object[] newData = new Object[index];
            System.arraycopy(data, 0, newData, 0, index);
            data = newData;
        }
        this.data = data;
    }

    @Override
    public boolean isEmpty()
    {
        return data.length == 0;
    }

    @Override
    public int size()
    {
        return data.length / 2;
    }

    @Override
    public @Nullable V get(@NotNull K key)
    {
        int length = data.length;
        for (int index = 0; index < length; index += 2) {
            Object testKey = data[index];
            if (key == testKey || key.equals(testKey)) {
                return (V) data[index+1];
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(@NotNull Object key)
    {
        int length = data.length;
        for (int index = 0; index < length; index += 2) {
            Object testKey = data[index];
            if (key == testKey || key.equals(testKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(@NotNull Visitor<K,V> visitor)
    {
        int length = data.length;
        for (int index = 0; index < length; index += 2) {
            K key = (K) data[index];
            V value = (V) data[index + 1];
            visitor.visit(key, value);
        }
    }

    @Override
    public <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor, @Nullable R defaultResult)
    {
        int length = data.length;
        for (int index = 0; index < length; index += 2) {
            K key = (K) data[index];
            V value = (V) data[index + 1];
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
        ISet<K> ks = keySet;
        if (ks != null) {
            return ks;
        }
        return keySet = createKeySet();
    }

    @Override
    public @NotNull ISet<V> values()
    {
        ISet<V> vs = valueSet;
        if (vs != null) {
            return vs;
        }
        return valueSet = createValueSet();
    }

    private @NotNull ISet<K> createKeySet()
    {
        SetBuilder<K> sb = ISet.builder();
        int length = data.length;
        for (int index = 0; index < length; index += 2) {
            K key = (K) data[index];
            sb.add(key);
        }
        return sb.values();
    }

    private @NotNull ISet<V> createValueSet()
    {
        SetBuilder<V> sb = ISet.builder();
        int length = data.length;
        for (int index = 0; index < length; index += 2) {
            V value = (V) data[index + 1];
            sb.add(value);
        }
        return sb.values();
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull K key, @Nullable V value)
    {
        if (VObjects.equals(get(key), value)) {
            return this;
        }

        Map<K,V> map = asJavaMap();
        map.put(key, value);
        return Impl.createMap(map);
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull IMap<K,V> bindings)
    {
        Map<K,V> map = asJavaMap();
        bindings.visit(map::put);
        return Impl.createMap(map);
    }

    @Override
    public @NotNull IIterator<Binding<K,V>> iterator()
    {
        return IMapBindingIterator.create(this);
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
