/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import java.util.Map;

import org.jetbrains.annotations.*;

/**
  An interface for a map builder. A map builder is a mutable object that accepts a sequence of bindings and creates a
  map containing those bindings.

  @param <K> The type of the keys.
  @param <V> The type of the values.
*/

public interface MapBuilder<K,V>
{
    /**
      Indicate whether or not a binding is specified for a key.
      @param key The key.
      @return true if and only if a binding is specified for the key.
    */

    boolean containsKey(@NotNull K key);

    /**
      Return the value currently bound to a key
      @param key The key.
      @return the associated value, or null if none.
    */

    @Nullable V get(@NotNull K key);

    /**
      Add a binding to be included in the map, replacing any previously added binding with the same key.

      @param key The key.
      @param value The value.
    */

    void put(@NotNull K key, @NotNull V value);

    /**
      Optionally add a binding to be included in the map, replacing any previously added binding with the same key.

      @param key The key.
      @param value The value. If null, this method has no effect.
    */

    default void putOptional(@NotNull K key, @Nullable V value)
    {
        if (value != null) {
            put(key, value);
        }
    }

    /**
      Add a binding to be included in the map, replacing any previously added binding with the same key.

      @param binding The binding to be added.
    */

    void add(@NotNull Binding<? extends K, ? extends V> binding);

    /**
      A convenience method to add a collection of bindings.
      @param it The iterable collection that provides the bindings.
      @throws IllegalArgumentException if the sequence contains a null element or an entry with a null key or value.
    */

    default void addEntries(@NotNull Iterable<Map.Entry<? extends K, ? extends V>> it)
      throws IllegalArgumentException
    {
        for (Map.Entry<? extends K, ? extends V> e : it) {
            if (e == null) {
                throw new IllegalArgumentException("Null bindings are not permitted");
            }
            K key = e.getKey();
            V value = e.getValue();
            if (key == null) {
                throw new IllegalArgumentException("Null keys are not permitted");
            }
            if (value == null) {
                throw new IllegalArgumentException("Null values are not permitted");
            }

            put(key, value);
        }
    }

    /**
      A convenience method to add a collection of bindings.
      @param it The iterable collection that provides the bindings.
      @throws IllegalArgumentException if the sequence contains a null element or an entry with a null key or value.
    */

    default void addAll(@NotNull Iterable<Binding<K,V>> it)
      throws IllegalArgumentException
    {
        for (Binding<? extends K, ? extends V> e : it) {
            if (e == null) {
                throw new IllegalArgumentException("Null bindings not permitted");
            }
            add(e);
        }
    }

    /**
      Remove all previously supplied bindings.
    */

    void reset();

    /**
      Indicate whether the map created in the current state would be empty.
      @return true if and only if the map would be empty.
    */

    boolean isEmpty();

    /**
      Return the number of bindings that would be contained in the map created in the current state.
      @return the number of elements.
    */

    int size();

    /**
      Return a map containing the previously supplied (and not removed) bindings.
      @return the map.
    */

    @NotNull IMap<K,V> value();
}
