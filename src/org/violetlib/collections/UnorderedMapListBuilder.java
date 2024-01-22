/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import java.util.Comparator;

import org.jetbrains.annotations.*;

/**
  Build an immutable map from keys of type K to immutable lists of values of type V. Null values are not supported.
*/

public interface UnorderedMapListBuilder<K,V>
    extends MapListBuilder<K,V>
{
    /**
      Return a map containing the keys bound to lists of values, sorted in the natural order.
    */

    @NotNull IMap<K,IList<V>> sort();

    /**
      Return a map containing the keys bound to lists of values, sorted using the specified comparator.
    */

    @NotNull IMap<K,IList<V>> sort(@NotNull Comparator<? super V> comparator);

    /**
      Return a map containing the keys bound to lists of values, sorted using the specified set of key dependent
      comparators.
    */

    @NotNull IMap<K,IList<V>> sort(@NotNull KeyedValueSorter<K,V> sorter);
}
