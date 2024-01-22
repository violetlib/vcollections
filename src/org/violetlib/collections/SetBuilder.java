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
  An interface for a set builder. A set builder is a mutable object that accepts a sequence of elements and creates a
  set containing those elements.
*/

public interface SetBuilder<V>
  extends Builder<V>
{
    /**
      Return a set containing the previously supplied (and not removed) elements.
      @return the set.
    */

    @Override
    @NotNull ISet<V> values();
}
