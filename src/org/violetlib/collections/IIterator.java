/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import java.util.Iterator;

import org.violetlib.collections.impl.IIteratorImpl;
import org.violetlib.collections.impl.SafeIteratorWrapper;

import org.jetbrains.annotations.*;

/**
  A specialization of the Iterator interface that is declared to return only non-null values.
*/

public interface IIterator<E>
  extends Iterator<E>
{
    /**
      Create an IIterator from an Iterator that does not return null.
      @param it The source iterator, which must not return null.
    */

    static <V> @NotNull IIterator<V> from(@NotNull Iterator<V> it)
    {
        return IIteratorImpl.create(it);
    }

    /**
      Create an IIterator from an Iterator by filtering any nulls it returns.
      @param it The source iterator.
    */

    static <V> @NotNull IIterator<V> filter(@NotNull Iterator<V> it)
    {
        return SafeIteratorWrapper.wrap(it);
    }

    default @NotNull IIterable<E> asIterable()
    {
        return () -> this;
    }

    /**
      Returns the next element in the iteration.
      @return the next element in the iteration
      @throws {@link java.util.NoSuchElementException} if the iteration has no more elements
    */

    @Override
    @NotNull E next();
}
