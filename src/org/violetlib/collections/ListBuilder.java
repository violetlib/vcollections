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
  An interface for a list builder. A list builder is a mutable object that accepts a sequence of elements and creates a
  list containing those elements.
*/

public interface ListBuilder<V>
  extends Builder<V>
{
    /**
      Identify the element that would be the last element of the list created in the current state.
      @return the last element, or null if the list would be empty.
    */

    @Nullable V lastItem();

    /**
      Return a list containing the previously supplied (and not removed) elements.
      @return the list.
    */

    @Override
    @NotNull IList<V> values();
}
