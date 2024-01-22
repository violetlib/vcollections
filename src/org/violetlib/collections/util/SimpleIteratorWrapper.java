/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.util;

import java.util.NoSuchElementException;

import org.violetlib.collections.IIterator;

import org.jetbrains.annotations.*;

/**
  A wrapper class that allows a {@link SimpleIterator} to be used where a standard iterator is required.

  @param <E> The type of the iteration elements. Null values are not supported.
*/

public class SimpleIteratorWrapper<E>
  implements IIterator<E>
{
    /**
      Create an iterator that uses a {@link SimpleIterator} to obtain its elements.
    */

    /* package private */ static <E> @NotNull IIterator<E> create(@NotNull SimpleIterator<E> it)
    {
        return new SimpleIteratorWrapper<>(it);
    }

    private final @NotNull SimpleIterator<E> basic;

    private @Nullable E currentElement;

    private SimpleIteratorWrapper(@NotNull SimpleIterator<E> it)
    {
        this.basic = it;

        currentElement = it.next();
    }

    @Override
    public boolean hasNext()
    {
        return currentElement != null;
    }

    @Override
    public @NotNull E next()
      throws NoSuchElementException
    {
        if (currentElement == null) {
            throw new NoSuchElementException();
        }

        E result = currentElement;
        currentElement = basic.next();
        return result;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("This iterator does not support remove.");
    }
}
