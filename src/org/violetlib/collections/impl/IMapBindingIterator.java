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

import org.jetbrains.annotations.*;

/**
  An iterator for the bindings of an immutable map.
*/

public final class IMapBindingIterator<K,V>
  implements IIterator<Binding<K,V>>
{
    public static <K,V> @NotNull IIterator<Binding<K,V>> create(@NotNull IMap<K,V> map)
    {
        return new IMapBindingIterator<>(map);
    }

    private final @NotNull IMap<K,V> map;
    private final @NotNull IIterator<K> keyIterator;

    private IMapBindingIterator(@NotNull IMap<K,V> map)
    {
        this.map = map;
        keyIterator = map.keySet().iterator();
    }

    @Override
    public @NotNull Binding<K,V> next()
    {
        K key = keyIterator.next();
        V value = map.get(key);
        assert value != null;
        return Binding.create(key, value);
    }

    @Override
    public boolean hasNext()
    {
        return keyIterator.hasNext();
    }
}
