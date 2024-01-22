/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.violetlib.collections.IList;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.ListUsage;
import org.violetlib.collections.Visitor;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  An implementation of {@link IList} that supports efficient appending and reverse traversal.
  Access to the first element of the list requires time of order N, where N is the length of the list.

  @param <V> The type of the values.
*/

public abstract @Immutable class AppendOrientedList<V>
  implements IList<V>, HasReverseIterator<V>
{
    /**
      Return a list builder that creates an append oriented list.
    */

    public static <V> @NotNull ListBuilder<V> builder()
    {
        return AppendOrientedListBuilder.create();
    }

    /**
      Return a list containing the specified elements. The returned list will be optimized for adding more elements at
      the end.
    */

    @SafeVarargs
    public static <V> @NotNull IList<V> create(@NotNull V... elements)
    {
        if (elements.length == 0) {
            return EmptyReverseList.empty();
        }

        AppendOrientedList<V> result = EmptyReverseList.empty();
        for (V element : elements) {
            result = ReverseListNode.create(result, element);
        }
        return result;
    }

    /**
      Return a list containing the specified elements. The returned list will be optimized for adding more elements at
      the end.
    */

    public static <V> @NotNull IList<V> fromList(@NotNull List<? extends V> elements)
    {
        return createWithElements(elements);
    }

    /**
      Return a list containing the specified elements. The returned list will be optimized for adding more elements at
      the end.
    */

    public static <V> @NotNull IList<V> fromList(@NotNull IList<? extends V> elements)
    {
        if (elements.isEmpty()) {
            return EmptyReverseList.empty();
        }

        AppendOrientedList<V> result = Extensions.getExtension(elements, AppendOrientedList.class);
        if (result != null) {
            return result;
        } else {
            return createWithElements(elements);
        }
    }

    /**
      Return a list containing the specified elements. The returned list will be optimized for adding more elements at
      the end.
      @param elements An iterator providing the elements.
      @throws IllegalArgumentException if the iterator returns a null element.
    */

    public static <V> @NotNull AppendOrientedList<V> createWithElements(@NotNull Iterable<? extends V> elements)
      throws IllegalArgumentException
    {
        AppendOrientedList<V> result = EmptyReverseList.empty();
        for (V element : elements) {
            if (element == null) {
                throw new IllegalArgumentException("Null elements are not permitted");
            }
            result = ReverseListNode.create(result, element);
        }
        return result;
    }

    protected AppendOrientedList()
    {
    }

    public abstract @NotNull AppendOrientedList<V> head()
      throws NoSuchElementException;

    protected void visitReverse(@NotNull Visitor<V> visitor)
    {
        AppendOrientedList<V> current = this;
        while (!current.isEmpty()) {
            visitor.visit(current.last());
            current = current.head();
        }
    }

    @Override
    public abstract @NotNull AppendOrientedList<V> appending(@NotNull V value);

    @Override
    public abstract @NotNull AppendOrientedList<V> prepending(@NotNull V value);

    @Override
    public abstract @NotNull AppendOrientedList<V> replacingAll(int index, int count, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException;

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        AppendOrientedList<V> result = this;
        for (V value : values) {
            if (value == null) {
                throw new IllegalArgumentException("Null elements are not permitted");
            }
            result = ReverseListNode.create(result, value);
        }
        return result;
    }

    @Override
    public abstract @NotNull AppendOrientedList<V> removing(@NotNull Object value);

    @Override
    public abstract <R> @NotNull AppendOrientedList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper);

    @Override
    public abstract <R> @NotNull AppendOrientedList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper);

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
        return PrependOrientedList.fromList(this);
    }

    @Override
    public @NotNull IList<V> optimizeForIndexing()
    {
        return SimpleList.fromList(this);
    }

    @Override
    public @NotNull IList<V> optimize(@NotNull ListUsage usage)
    {
        if (usage == ListUsage.PREPEND) {
            return PrependOrientedList.fromList(this);
        }
        if (usage == ListUsage.APPEND) {
            return this;
        }
        // TBD: indexed vs default
        return SimpleList.fromList(this);
    }
}
