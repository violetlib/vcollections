/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.NoSuchElementException;

import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;

import org.jetbrains.annotations.*;

/**
  An iterator that gets its elements from some number of iterators, called in sequence.

  @see CompositeIterator
*/

public final class SimpleCompositeIterator<E>
  implements IIterator<E>
{
    public static <E> @NotNull IIterator<E> create(@NotNull IList<IIterator<E>> its)
    {
        return new SimpleCompositeIterator<>(its);
    }

    private final @NotNull IList<IIterator<E>> iterators;
    private final int count;
    private int current;

    private SimpleCompositeIterator(@NotNull IList<IIterator<E>> its)
    {
        this.iterators = its;
        count = its.size();
        advanceToNextAvailable();
    }

    @Override
    public boolean hasNext()
    {
        return current < count;
    }

    @Override
    public @NotNull E next()
    {
        if (current >= count) {
            throw new NoSuchElementException();
        }

        IIterator<E> it = iterators.get(current);
        E result = it.next();
        advanceToNextAvailable();
        return result;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    private void advanceToNextAvailable()
    {
        while (current < count) {
            IIterator<E> it = iterators.get(current);
            if (it.hasNext()) {
                return;
            }
            ++current;
        }
    }
}
