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
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IMap;
import org.violetlib.collections.ISet;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;

/**

*/

public final class SingletonMap<K,V>
  implements IMap<K,V>
{
    public static <K,V> @NotNull IMap<K,V> create(@NotNull K key, @NotNull V value)
    {
        return new SingletonMap<>(key, value);
    }

    private final @NotNull K key;
    private final @NotNull V value;

    private SingletonMap(@NotNull K key, @NotNull V value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public int size()
    {
        return 1;
    }

    @Override
    public @Nullable V get(@NotNull K key)
    {
        return this.key.equals(key) ? value : null;
    }

    @Override
    public boolean containsKey(@NotNull Object key)
    {
        return this.key.equals(key);
    }

    @Override
    public void visit(@NotNull Visitor<K,V> visitor)
    {
        visitor.visit(key, value);
    }

    @Override
    public <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor, @Nullable R defaultResult)
    {
        R result = visitor.visit(key, value);
        return result != null ? result : defaultResult;
    }

    @Override
    public @NotNull ISet<K> keySet()
    {
        return ISet.of(key);
    }

    @Override
    public @NotNull ISet<V> values()
    {
        return ISet.of(value);
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull K key, @Nullable V value)
    {
        Map<K,V> m = new HashMap<>();
        m.put(this.key, this.value);
        m.put(key, value);
        return Impl.createMap(m);
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull IMap<K,V> bindings)
    {
        return bindings.extending(key, value);
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
