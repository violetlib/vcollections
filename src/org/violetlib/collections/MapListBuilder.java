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
  Build an immutable map from keys of type K to immutable lists of values of type V. Null values are not supported.
*/

public interface MapListBuilder<K,V>
{
    /**
      Return the number of keys with associated values.
    */

    int size();

    /**
      Add an element at the end of the list associated with the specified key.

      @param key The key.
      @param value The value to be added.
    */

    void add(@NotNull K key, @NotNull V value);

    /**
      Add elements at the end of the list associated with the specified key.

      @param key The key.
      @param values The values to be added.
    */

    void addAll(@NotNull K key, @NotNull IList<V> values);

    /**
      Return the number of values associated with the specified key.

      @param key The key.

      @return the number of values associated with <code>key</code>.
    */

    int getValueCount(@NotNull K key);

    /**
      Return the values currently associated with the specified key.

      @param key The key.

      @return an immutable list that contains the values associated with the specified key.
    */

    @NotNull IList<V> getValues(@NotNull K key);

    /**
      Return the keys that have associated values.
    */

    @NotNull IList<K> keys();

    /**
      Return a map containing the keys bound to lists of values.
    */

    @NotNull IMap<K,IList<V>> value();
}
