/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Comparator;
import java.util.Map;

import org.violetlib.collections.*;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**

*/

public final @NoInstances class Impl
{
    private Impl()
    {
        throw new AssertionError("Impl may not be instantiated");
    }

    public static <V> @NotNull IList<V> getEmptyList()
    {
        return EmptyListImpl.empty();
    }

    public static <V> @NotNull IList<V> getIndexableEmptyList()
    {
        return SimpleList.EMPTY;
    }

    public static <V> @NotNull ICollection<V> concatenate(@NotNull IList<? extends ICollection<V>> sources)
    {
        return ICollectionSequence.create(sources);
    }

    public static <V> @NotNull ISet<V> getEmptySet()
    {
        return SimpleSet.empty();
    }

    public static <V> @NotNull ISet<V> createSet(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        return SimpleSet.collect(values);
    }

    public static <V> @NotNull IIterator<V> concatenateIterators(@NotNull IList<? extends IIterator<V>> sources)
    {
        return SimpleCompositeIterator.create(IList.cast(sources));
    }

    @SafeVarargs
    public static <V> @NotNull ISet<V> setOf(@NotNull V... elements)
      throws IllegalArgumentException
    {
        return SimpleSet.create(elements);
    }

    public static <V> @NotNull SetBuilder<V> getSetBuilder()
    {
        return SetBuilderImpl.create();
    }

    public static <V> @NotNull SetBuilder<V> getOrderedSetBuilder()
    {
        return OrderedSetBuilderImpl.create();
    }

    public static @NotNull Comparator<Object> getUniversalComparator()
    {
        return UniversalComparator.get();
    }

    public static <K,V> @NotNull IMap<K,V> createSingletonMap(@NotNull K key, @NotNull V value)
    {
        return SingletonMap.create(key, value);
    }

    public static <K,V> @NotNull IMap<K,V> createMap(@NotNull Map<? extends K,? extends V> map)
    {
        if (map.isEmpty()) {
            return EmptyMap.get();
        }

        int count = map.size();
        if (count < 8) {
            return ArrayMapImpl.create(map);
        }

        return HashMapImpl.create(map);
    }

    public static <K,V> @NotNull IMap<K,V> createWeakMap(@NotNull Map<? extends K,? extends V> map)
    {
        return WeakMap.from(map);
    }

    public static <K,V> @NotNull MapBuilder<K,V> getMapBuilder()
    {
        return MapBuilderImpl.create();
    }

    public static <K,V> @NotNull MapBuilder<K,V> getOrderedMapBuilder()
    {
        return OrderedMapBuilder.create();
    }

    public static <K,V> @NotNull UnorderedMapListBuilder<K,V> getMapListBuilder()
    {
        return MapListBuilderImpl.create();
    }

    public static <K,V> @NotNull MapListBuilder<K,V> getOrderedMapListBuilder()
    {
        return OrderedMapListBuilderImpl.create();
    }

    public static <K,V> @NotNull MapSetBuilder<K,V> getMapSetBuilder()
    {
        return MapSetBuilderImpl.create(false);
    }

    public static <K,V> @NotNull MapSetBuilder<K,V> getMapOrderedSetBuilder()
    {
        return MapSetBuilderImpl.create(true);
    }
}
