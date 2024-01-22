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

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  An iterator that provides array elements in reverse order. Null elements are not supported.
*/

/* package private */ final class ArrayReverseIterator<V>
  implements IIterator<V>
{
    public static <V> @NotNull IIterator<V> create(@NotNull Object[] elements)
    {
        return new ArrayReverseIterator<>(elements);
    }

    private final @NotNull Object @NotNull [] elements;
    private int nextIndex;

    private ArrayReverseIterator(@NotNull Object @NotNull [] elements)
    {
        this.elements = elements;
        this.nextIndex = elements.length - 1;
    }

    @Override
    public boolean hasNext()
    {
        return nextIndex >= 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull V next()
    {
        int i = nextIndex;
        if (i < 0) {
            throw new NoSuchElementException();
        }
        nextIndex = i - 1;
        return (V) elements[i];
    }
}
