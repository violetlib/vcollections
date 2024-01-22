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
  A collection builder. A collection builder is a mutable object that accepts a sequence of elements and creates a
  collection containing those elements.

  @param <V> The type of the elements.
*/

public interface Builder<V>
{
    /**
      Add an element to be included in the collection.
      The order in which elements are added may determine the order of elements in an ordered collection.

      @param e The element to be added.
    */

    void add(@NotNull V e);

    default void addOptional(@Nullable V e)
    {
        if (e != null) {
            add(e);
        }
    }

    /**
      A convenience method to add the elements obtained from the supplied iterable collection.
      @param it The iterable collection that provides the elements.
      @throws IllegalArgumentException if the sequence contains a null element.
    */

    default void addAll(@NotNull Iterable<? extends V> it)
      throws IllegalArgumentException
    {
        for (V v : it) {
            if (v == null) {
                throw new IllegalArgumentException("Null elements are not permitted");
            }
            add(v);
        }
    }

    /**
      Remove all previously supplied elements.
    */

    void reset();

    /**
      Indicate whether the collection created in the current state would be empty.
      @return true if and only if the collection would be empty.
    */

    boolean isEmpty();

    /**
      Return the number of elements that would be contained in the collection created in the current state.
      @return the number of elements.
    */

    int size();

    /**
      Return a collection containing the previously supplied (and not removed) elements.
      @return the collection.
    */

    @NotNull ICollection<V> values();
}
