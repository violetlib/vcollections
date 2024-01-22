/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.violetlib.collections.IList;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.ListUsage;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  An implementation of {@link IList} that supports efficient prepending and forward traversal.
  Access to the last element of the list requires time of order N, where N is the length of the list.

  @param <V> The type of the values.
*/

public abstract @Immutable class PrependOrientedList<V>
  implements IList<V>
{
    /**
      Return a list builder that creates a prepend oriented list.
    */

    public static <V> @NotNull ListBuilder<V> builder()
    {
        return PrependOrientedListBuilder.create();
    }

    /**
      Return a list containing the specified elements. If not empty, the returned list will be optimized for adding
      more elements at the beginning.
    */

    @SafeVarargs
    public static <V> @NotNull IList<V> create(@NotNull V... elements)
    {
        if (elements.length == 0) {
            return EmptyForwardList.empty();
        }

        return createWithElements(Arrays.asList(elements));
    }

    /**
      Return a list containing the specified elements. If not empty, the returned list will be optimized for adding
      more elements at the beginning.
    */

    public static <V> @NotNull IList<V> fromList(@NotNull List<? extends V> elements)
    {
        return createWithElements(elements);
    }

    /**
      Return a list containing the specified elements. If not empty, the returned list will be optimized for adding
      more elements at the beginning.
    */

    public static <V> @NotNull IList<V> fromList(@NotNull IList<? extends V> elements)
    {
        if (elements.isEmpty()) {
            return EmptyForwardList.empty();
        }

        PrependOrientedList<V> result = Extensions.getExtension(elements, PrependOrientedList.class);
        if (result != null) {
            return result;
        } else {
            return createWithElements(elements);
        }
    }

    /**
      Return a list containing the specified elements. If not empty, the returned list will be optimized for adding
      more elements at the beginning.
      @param elements An iterator providing the elements.
      @throws IllegalArgumentException if the iterator returns a null element.
    */

    public static <V> @NotNull PrependOrientedList<V> createWithElements(@NotNull Iterable<? extends V> elements)
      throws IllegalArgumentException
    {
        ListBuilder<V> builder = builder();
        for (V e : elements) {
            if (e == null) {
                throw new IllegalArgumentException("Null elements are not permitted");
            }
            builder.add(e);
        }
        return (PrependOrientedList) builder.values();
    }

    protected PrependOrientedList()
    {
    }

    public abstract @NotNull PrependOrientedList<V> tail()
      throws NoSuchElementException;

    @Override
    public abstract @NotNull PrependOrientedList<V> appending(@NotNull V value);

    @Override
    public abstract @NotNull PrependOrientedList<V> removing(@NotNull Object value);

    @Override
    public abstract <R> @NotNull PrependOrientedList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper);

    @Override
    public abstract <R> @NotNull PrependOrientedList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper);

    @Override
    public @NotNull IList<V> sort(@NotNull Comparator<? super V> c)
    {
        List<V> elements = toJavaList();
        elements.sort(c);
        return createWithElements(elements);
    }

    @Override
    public @NotNull IList<V> optimizeForForwardTraversal()
    {
        return this;
    }

    @Override
    public @NotNull IList<V> optimizeForIndexing()
    {
        return SimpleList.fromList(this);
    }

    @Override
    public @NotNull IList<V> optimize(@NotNull ListUsage usage)
    {
        if (usage == ListUsage.APPEND) {
            return AppendOrientedList.fromList(this);
        }
        if (usage == ListUsage.PREPEND) {
            return this;
        }
        // TBD: indexed vs default
        return SimpleList.fromList(this);
    }
}
