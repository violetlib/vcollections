/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.violetlib.collections.Binding;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IMap;
import org.violetlib.collections.ISet;
import org.violetlib.collections.SetBuilder;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  A virtual map defined using a base map and function that maps base map values to the values of this virtual map.
*/

public final @Immutable class MappedMap<K,V,E>
  implements IMap<K,V>
{
    public static <K,V,E> @NotNull IMap<K,V> create(@NotNull IMap<K,E> base, @NotNull Function<E,V> mapper)
    {
        return new MappedMap<>(base, mapper);
    }

    private final @NotNull IMap<K,E> base;
    private final @NotNull Function<E,V> mapper;

    private MappedMap(@NotNull IMap<K,E> base, @NotNull Function<E,V> mapper)
    {
        this.base = base;
        this.mapper = mapper;
    }

    @Override
    public @NotNull IIterator<Binding<K,V>> iterator()
    {
        return IMapBindingIterator.create(this);
    }

    @Override
    public boolean isEmpty()
    {
        return base.isEmpty();
    }

    @Override
    public int size()
    {
        return base.size();
    }

    @Override
    public @Nullable V get(@NotNull K key)
    {
        E e = base.get(key);
        return e != null ? mapper.apply(e) : null;
    }

    @Override
    public boolean containsKey(@NotNull Object key)
    {
        try {
            K k = (K) key;
            E e = base.get(k);
            return e != null ? mapper.apply(e) != null : false;
        } catch (ClassCastException ex) {
            // probably of no use in current Java
            return false;
        }
    }

    @Override
    public void visit(@NotNull Visitor<K,V> visitor)
    {
        base.visit((key, e) -> visitor.visit(key, mapper.apply(e)));
    }

    @Override
    public <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor, @Nullable R defaultResult)
    {
        return base.find((key, e) -> visitor.visit(key, mapper.apply(e)), defaultResult);
    }

    @Override
    public @NotNull ISet<K> keySet()
    {
        Set<K> result = new HashSet<>();
        for (K key : base.keySet()) {
            V value = get(key);
            if (value != null) {
                result.add(key);
            }
        }
        return SimpleSet.fromSet(result);
    }

    @Override
    public @NotNull ISet<V> values()
    {
        SetBuilder<V> b = ISet.builder();
        for (K key : base.keySet()) {
            V value = get(key);
            if (value != null) {
                b.add(value);
            }
        }
        return b.values();
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull K key, @Nullable V value)
    {
        Map<K,V> bindings = new HashMap<>();
        visit(bindings::put);
        bindings.put(key, value);
        return Impl.createMap(bindings);
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull IMap<K,V> delta)
    {
        Map<K,V> bindings = new HashMap<>();
        visit(bindings::put);
        delta.visit(bindings::put);
        return Impl.createMap(bindings);
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
