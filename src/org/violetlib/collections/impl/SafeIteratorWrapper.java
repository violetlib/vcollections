/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.violetlib.collections.IIterable;
import org.violetlib.collections.IIterator;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;

/**
  Wraps an iterator to make it an IIterator. Null elements returned by the iterator are skipped.

  @see IIteratorImpl
*/

public final class SafeIteratorWrapper<T>
  implements IIterator<T>
{
    public static <T> @NotNull IIterator<T> wrap(@NotNull Iterator<T> source)
    {
        IIterator<T> it = Extensions.getExtension(source, IIterator.class);
        if (it != null) {
            return it;
        }
        return new SafeIteratorWrapper<>(source);
    }

    private final @NotNull Iterator<T> source;
    private @Nullable T nextValue;

    private SafeIteratorWrapper(@NotNull Iterator<T> source)
    {
        this.source = source;

        while (source.hasNext()) {
            nextValue = source.next();
            if (nextValue != null) {
                break;
            }
        }
    }

    @Override
    public @NotNull T next()
    {
        if (nextValue == null) {
            throw new NoSuchElementException();
        }

        T result = nextValue;

        while (source.hasNext()) {
            nextValue = source.next();
            if (nextValue != null) {
                break;
            }
        }

        return result;
    }

    @Override
    public boolean hasNext()
    {
        return nextValue != null;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
