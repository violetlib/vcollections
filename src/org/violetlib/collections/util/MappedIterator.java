/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.util;

import java.util.Iterator;
import java.util.function.Function;

import org.jetbrains.annotations.*;

/**
  Create an iterator by applying a mapping function to the elements of a source iterable.

  @param <B> The type of elements returned by the source iterable.
  @param <E> The type of elements returned by this iterator.
*/

public final class MappedIterator<B,E>
  extends IteratorHelper<E>
{
    /**
      Create an iterator that maps elements from a source iterable.
      @param source The source iterable. Null elements are ignored.
      @param mapper The mapper. Null values are ignored.
    */

    public static <B,E> @NotNull Iterator<E> create(@NotNull Iterable<B> source, @NotNull Function<B,E> mapper)
    {
        return new MappedIterator<>(source, mapper);
    }

    private final @NotNull Iterator<B> base;
    private final @NotNull Function<B,E> mapper;

    private MappedIterator(@NotNull Iterable<B> source, @NotNull Function<B,E> mapper)
    {
        this.base = source.iterator();
        this.mapper = mapper;
    }

    @Override
    protected @Nullable E provide()
    {
        while (true) {
            if (base.hasNext()) {
                B baseElement = base.next();
                if (baseElement == null) {
                    continue;
                }
                E e = mapper.apply(baseElement);
                if (e == null) {
                    continue;
                }
                return e;
            } else {
                return null;
            }
        }
    }
}
