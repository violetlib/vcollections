/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Iterator;
import java.util.function.Consumer;

import org.violetlib.collections.IIterator;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;

/**
  Wraps an iterator to make it an IIterator. The iterator must return only non-null elements.

  @see SafeIteratorWrapper
*/

public final class IIteratorImpl<T>
    implements IIterator<T>
{
    public static <T> @NotNull IIterator<T> create(@NotNull Iterator<T> source)
    {
        IIterator<T> it = Extensions.getExtension(source, IIterator.class);
        if (it != null) {
            return it;
        }
        return new IIteratorImpl<>(source);
    }

    private final @NotNull Iterator<T> source;

    private IIteratorImpl(@NotNull Iterator<T> source)
    {
        this.source = source;
    }

    @Override
    public @NotNull T next()
    {
        T value = source.next();
        if (value == null) {
            throw new NullPointerException("Iterator returned null value");
        }
        return value;
    }

    @Override
    public boolean hasNext()
    {
        return source.hasNext();
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(@NotNull Consumer<? super T> action)
    {
        source.forEachRemaining(action);
    }
}
