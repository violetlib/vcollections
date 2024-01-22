/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.util;

import org.violetlib.collections.IIterable;
import org.violetlib.collections.IIterator;

import org.jetbrains.annotations.*;

/**
  An iterator interface that is easier to implement, but does not support null elements.

  @see SimpleIteratorWrapper

  @param <E> The type of the iteration elements. Null values are not supported.
*/

public interface SimpleIterator<E>
{
    default @NotNull IIterator<E> asIterator()
    {
        return SimpleIteratorWrapper.create(this);
    }

    default @NotNull IIterable<E> asIterable()
    {
        IIterator<E> w = asIterator();
        return () -> w;
    }

    /**
      Return the next element.

      @return the next element, or null if all elements have been scanned.
    */

    @Nullable E next();
}
