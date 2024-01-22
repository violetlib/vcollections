/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.Binding;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IMap;
import org.violetlib.collections.ISet;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  A map containing no bindings.
*/

public final @Immutable class EmptyMap<K,V>
  implements IMap<K,V>
{
    public static <K,V> @NotNull IMap<K,V> get()
    {
        return (IMap) INSTANCE;
    }

    private static final @NotNull EmptyMap<Object,Object> INSTANCE = new EmptyMap<>();

    private EmptyMap()
    {
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

    @Override
    public int size()
    {
        return 0;
    }

    @Override
    public @Nullable V get(@NotNull K key)
    {
        return null;
    }

    @Override
    public boolean containsKey(@NotNull Object key)
    {
        return false;
    }

    @Override
    public void visit(@NotNull Visitor<K,V> visitor)
    {
    }

    @Override
    public <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor, @Nullable R defaultResult)
    {
        return null;
    }

    @Override
    public @NotNull ISet<K> keySet()
    {
        return ISet.empty();
    }

    @Override
    public @NotNull ISet<V> values()
    {
        return ISet.empty();
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull K key, @Nullable V value)
    {
        return value != null ? Impl.createSingletonMap(key, value) : this;
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull IMap<K,V> bindings)
    {
        return bindings;
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
        return otherMap.isEmpty();
    }
}
