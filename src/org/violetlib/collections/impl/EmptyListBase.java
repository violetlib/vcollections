/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import org.violetlib.collections.FindVisitor;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;
import org.violetlib.collections.ISet;
import org.violetlib.collections.ListUsage;
import org.violetlib.collections.Visitor;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  A base class for empty lists. Empty lists are equivalent except for their choice of representation when used to create
  new lists.
*/

public abstract @Immutable class EmptyListBase<V>
  implements IList<V>
{
    @Override
    public boolean isEmpty()
    {
        return true;
    }

    @Override
    public int size()
    {
        return 0;
    }

    @Override
    public boolean contains(@NotNull Object target)
    {
        return false;
    }

    @Override
    public void visit(@NotNull Visitor<V> visitor)
    {
    }

    @Override
    public <R> @Nullable R find(@NotNull FindVisitor<V,R> visitor)
    {
        return null;
    }

    @Override
    public <R> @NotNull R find(@NotNull FindVisitor<V,R> visitor, @NotNull R defaultValue)
    {
        return defaultValue;
    }

    @Override
    public @Nullable V optionalFirst()
    {
        return null;
    }

    @Override
    public @Nullable V optionalLast()
    {
        return null;
    }

    @Override
    public @NotNull IList<V> removing(@NotNull Object value)
    {
        return this;
    }

    @Override
    public @NotNull IList<V> removingAll(@NotNull ISet<?> values)
    {
        return this;
    }

    @Override
    public int indexOf(@NotNull Object element)
    {
        return -1;
    }

    @Override
    public @Nullable V getOptional(int index)
    {
        return null;
    }

    @Override
    public @NotNull V get(int index)
      throws IndexOutOfBoundsException
    {
        throw new IndexOutOfBoundsException("Empty list");
    }

    @Override
    public @NotNull IList<V> getElements(int index, int count)
      throws IndexOutOfBoundsException
    {
        if (count == 0) {
            return this;
        }
        throw new IndexOutOfBoundsException("Empty list");
    }

    @Override
    public @NotNull IList<V> removing(int index, int count)
      throws IndexOutOfBoundsException
    {
        if (count == 0) {
            return this;
        }
        throw new IndexOutOfBoundsException("Empty list");
    }

    @Override
    public @NotNull V first() throws NoSuchElementException
    {
        throw new NoSuchElementException();
    }

    @Override
    public @NotNull V last() throws NoSuchElementException
    {
        throw new NoSuchElementException();
    }

    @Override
    public @NotNull IList<V> removingFirst()
    {
        return this;
    }

    @Override
    public @NotNull IList<V> removingLast()
    {
        return this;
    }

    @Override
    public @NotNull IList<V> reverse()
    {
        return this;
    }

    @Override
    public @NotNull IList<V> onReverse()
    {
        return this;
    }

    @Override
    public @NotNull IList<V> sort()
    {
        return this;
    }

    @Override
    public <E> @NotNull E[] toJavaArray(@NotNull E[] template)
    {
        return Arrays.copyOf(template, 0);
    }

    @Override
    public @NotNull IList<V> replacing(int index, @NotNull V value)
      throws IndexOutOfBoundsException
    {
        throw new IndexOutOfBoundsException("Empty list");
    }

    @Override
    public @NotNull IList<V> replacingAll(int index, int count, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        if (index != 0 || count < 0) {
            throw new IndexOutOfBoundsException();
        }
        return IList.create(values);
    }

    @Override
    public <R> @NotNull IList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        return (IList) this;
    }

    @Override
    public @NotNull <R> IList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper)
    {
        return (IList) this;
    }

    @Override
    public @NotNull IList<V> sort(@NotNull Comparator<? super V> c)
    {
        return this;
    }

    @Override
    public @NotNull IIterator<V> iterator()
    {
        return EmptyIIterator.get();
    }

    @Override
    public @NotNull List<V> toJavaList()
    {
        return new ArrayList<>();
    }

    @Override
    public @NotNull Set<V> toJavaSet()
    {
        return new HashSet<>();
    }

    @Override
    public @NotNull IList<V> optimizeForForwardTraversal()
    {
        return this;
    }

    @Override
    public @NotNull IList<V> optimizeForIndexing()
    {
        return this;
    }

    @Override
    public @NotNull IList<V> optimize(@NotNull ListUsage usage)
    {
        return this;
    }

    @Override
    public int hashCode()
    {
        return 1;
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        IList<?> otherList = Extensions.getExtension(obj, IList.class);
        if (otherList == null) {
            return false;
        }

        return otherList.isEmpty();
    }
}
