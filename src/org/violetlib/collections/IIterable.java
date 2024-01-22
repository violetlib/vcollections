/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import org.violetlib.collections.impl.IterableWrapper;

import org.jetbrains.annotations.*;

/**
  A specialization of the Iterable interface to return iterators declared to return only non-null values.
*/

@FunctionalInterface
public interface IIterable<T>
  extends Iterable<T>
{
    /**
      Create an {@code IIterable} from an {@code Iterable}. The iterators produced by the returned {@code IIterable}
      will throw a runtime exception if an iterator produced by the {@code Iterable} returns a null value.
      @param it The source iterable.
    */

    static <T> @NotNull IIterable<T> from(@NotNull Iterable<T> it)
    {
        return IterableWrapper.create(it);
    }

    /**
      Returns an iterator over non-null elements of type {@code T}.

      @return an Iterator.
    */

    @Override
    @NotNull IIterator<T> iterator();
}
