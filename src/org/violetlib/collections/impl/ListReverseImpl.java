/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Comparator;
import java.util.function.Function;

import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.ListUsage;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;

/**
  Experimental: implementing a reverse view.
  <p>
  If the base list has a reverse iterator, that iterator is used to implement many of the operations
  on this view. Otherwise, the reversed list is obtained and used directly.
*/

public final class ListReverseImpl<V>
  implements IList<V>, HasReverseIterator<V>
{
    /**
      Create a reversed view of a list.
    */

    public static <V> @NotNull IList<V> create(@NotNull IList<V> base)
      throws IndexOutOfBoundsException
    {
        return new ListReverseImpl<>(base);
    }

    private final @NotNull IList<V> base;
    private final int size;
    private final @Nullable HasReverseIterator<V> reverseIteratorSource;
    private final @Nullable IList<V> contents;

    private ListReverseImpl(@NotNull IList<V> base)
    {
        this.base = base;
        this.size = base.size();

        HasReverseIterator<V> h = Extensions.getExtension(base, HasReverseIterator.class);
        if (h != null) {
            this.reverseIteratorSource = h;
            this.contents = null;
        } else {
            this.reverseIteratorSource = null;
            this.contents = base.reverse();
        }
    }

    @Override
    public @NotNull IIterator<V> iterator()
    {
        if (reverseIteratorSource != null) {
            return reverseIteratorSource.reverseIterator();
        } else {
            assert contents != null;
            return contents.iterator();
        }
    }

    @Override
    public @NotNull IIterator<V> reverseIterator()
    {
        return base.iterator();
    }

    @Override
    public boolean isEmpty()
    {
        return size == 0;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public @Nullable V optionalFirst()
    {
        return size > 0 ? base.get(size - 1) : null;
    }

    @Override
    public @Nullable V optionalLast()
    {
        return size > 0 ? base.get(0) : null;
    }

    @Override
    public @Nullable V getOptional(int index)
    {
        return size > 0 ? base.get(size - index - 1) : null;
    }

    @Override
    public int hashCode()
    {
        return ListEquality.computeHashCode(this);
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

        return ListEquality.isEqual(this, otherList);
    }

    @Override
    public @NotNull IList<V> getElements(int index, int count)
      throws IndexOutOfBoundsException
    {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        if (count > size) {
            throw new IndexOutOfBoundsException("Invalid count: " + count);
        }
        if (count == 0) {
            return IList.empty();
        }

        if (contents != null) {
            return contents.getElements(index, count);
        }

        ListBuilder<V> builder = builder();
        int i = size - 1 - index;
        for (int o = 0; o < count; o++) {
            builder.add(base.get(i--));
        }
        return builder.values();
    }

    private @NotNull IList<V> getElements()
    {
        return getElements(0, size);
    }

    @Override
    public @NotNull IList<V> removingFirst()
    {
        if (size == 0) {
            return this;
        }
        if (size == 1) {
            return empty();
        }
        return base.removingLast().onReverse();
    }

    @Override
    public @NotNull IList<V> removingLast()
    {
        if (size == 0) {
            return this;
        }
        if (size == 1) {
            return empty();
        }
        return base.removingFirst().onReverse();
    }

    @Override
    public @NotNull IList<V> prepending(@NotNull V value)
    {
        return base.appending(value).onReverse();
    }

    @Override
    public @NotNull IList<V> appending(@NotNull V value)
    {
        return base.prepending(value).onReverse();
    }

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        return ListOperations.appendingAll(this, values, builder());
    }

    @Override
    public @NotNull IList<V> removing(@NotNull Object value)
    {
        if (contents != null) {
            return contents.removing(value);
        }

        ListBuilder<V> builder = builder();
        for (V e : this) {
            if (!e.equals(value)) {
                builder.add(e);
            }
        }
        return builder.values();
    }

    @Override
    public @NotNull IList<V> removing(int index, int count)
      throws IndexOutOfBoundsException
    {
        if (contents != null) {
            return contents.removing(index, count);
        }

        return ListOperations.removing(this, index, count, builder());
    }

    @Override
    public @NotNull <R> IList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        if (contents != null) {
            return contents.map(mapper);
        }

        return ListOperations.map(this, mapper, IList.builder());
    }

    @Override
    public @NotNull <R> IList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper)
    {
        if (contents != null) {
            return contents.mapFilter(mapper);
        }

        return ListOperations.mapFilter(this, mapper, IList.builder());
    }

    @Override
    public @NotNull IList<V> replacing(int index, @NotNull V value)
      throws IndexOutOfBoundsException
    {
        return ListOperations.replacing(this, index, value, builder());
    }

    @Override
    public @NotNull IList<V> replacingAll(int index, int count, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        return ListOperations.replacingAll(this, index, count, values, builder());
    }

    @Override
    public @NotNull IList<V> reverse()
    {
        return base;
    }

    @Override
    public @NotNull IList<V> onReverse()
    {
        return base;
    }

    @Override
    public @NotNull IList<V> onSlice(int start, int end)
      throws IndexOutOfBoundsException
    {
        if (contents != null) {
            return contents.onSlice(start, end);
        }

        int baseStart = Math.max(0, size - end);
        int baseEnd = size - start;
        return base.onSlice(baseStart, baseEnd).onReverse();
    }

    @Override
    public @NotNull IList<V> sort()
    {
        return base.sort();
    }

    @Override
    public @NotNull IList<V> sort(@NotNull Comparator<? super V> c)
    {
        return base.sort(c);
    }

    @Override
    public @NotNull IList<V> optimize(@NotNull ListUsage usage)
    {
        return this;
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

    private @NotNull ListBuilder<V> builder()
    {
        return IList.builder();
    }

    private @NotNull IList<V> empty()
    {
        return IList.empty();
    }
}
