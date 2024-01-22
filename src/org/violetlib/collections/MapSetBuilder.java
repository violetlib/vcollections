/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import org.jetbrains.annotations.*;

/**
  Build an immutable map from keys of type K to immutable sets of values of type V. Null values are not supported.
*/

public interface MapSetBuilder<K,V>
{
    /**
      Return the number of keys with associated values.
    */

    int size();

    /**
      Add an element to the set associated with the specified key.

      @param key The key.
      @param value The value to be added.
    */

    void add(@NotNull K key, @NotNull V value);

    /**
      Add elements to the set associated with the specified key.

      @param key The key.
      @param values The values to be added.
    */

    void addAll(@NotNull K key, @NotNull ICollection<V> values);

    /**
      Add elements to the set from the associated map.

      @param values The values to be added.
    */

    void addAll(@NotNull IMap<K,ISet<V>> values);

    /**
      Return the number of distinct values associated with the specified key.

      @param key The key.

      @return the number of values associated with <code>key</code>.
    */

    int getValueCount(@NotNull K key);

    /**
      Return the values currently associated with the specified key.

      @param key The key.

      @return an immutable set that contains the values associated with the specified key.
    */

    @NotNull ISet<V> getValues(@NotNull K key);

    /**
      Return the keys that have associated values.
    */

    @NotNull IList<K> keys();

    /**
      Return a map containing the keys bound to sets of values.
    */

    @NotNull IMap<K,ISet<V>> value();
}
