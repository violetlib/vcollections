/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import org.violetlib.collections.IIterator;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  An iterator that returns no elements.
*/

public final @Immutable class EmptyIIterator<T>
  implements IIterator<T>
{
    public static <T> @NotNull IIterator<T> get()
    {
        return (IIterator) INSTANCE;
    }

    private static final EmptyIIterator<Object> INSTANCE = new EmptyIIterator<>();

    private EmptyIIterator()
    {
    }

    @Override
    public boolean hasNext()
    {
        return false;
    }

    @Override
    public @NotNull T next()
    {
        throw new NoSuchElementException("No elements");
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(@NotNull Consumer<? super T> action)
    {
        Objects.requireNonNull(action);
    }
}
