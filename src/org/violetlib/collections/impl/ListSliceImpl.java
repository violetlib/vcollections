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

import org.violetlib.collections.FindVisitor;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;
import org.violetlib.collections.ListUsage;
import org.violetlib.collections.Visitor;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;

/**
  Experimental: implementing a slice view.
*/

public class ListSliceImpl<V>
  implements IList<V>, HasReverseIterator<V>
{
    protected final @NotNull IList<V> base;
    protected final int start;
    protected final int end;

    public static <V> @NotNull IList<V> create(@NotNull IList<V> base, int start, int end)
      throws IndexOutOfBoundsException
    {
        return new ListSliceImpl<>(base, start, end);
    }

    /**
      Create a view of a list slice.
    */

    protected ListSliceImpl(@NotNull IList<V> base, int start, int end)
      throws IndexOutOfBoundsException
    {
        int size = base.size();
        if (start < 0 || start > size) {
            throw new IndexOutOfBoundsException("Invalid slice start: " + start);
        }
        if (end < start) {
            throw new IndexOutOfBoundsException("Invalid slice end: " + end);
        }
        if (end > size) {
            end = size;
        }

        this.base = base;
        this.start = start;
        this.end = end;
    }

    @Override
    public @NotNull IIterator<V> iterator()
    {
        return new MyIterator();
    }

    @Override
    public @NotNull IIterator<V> reverseIterator()
    {
        return new MyReverseIterator();
    }

    @Override
    public boolean isEmpty()
    {
        return start == end;
    }

    @Override
    public int size()
    {
        return end - start;
    }

    @Override
    public @Nullable V optionalFirst()
    {
        return end > start ? base.get(start) : null;
    }

    @Override
    public @Nullable V optionalLast()
    {
        return end > start ? base.get(end - 1) : null;
    }

    @Override
    public @Nullable V getOptional(int index)
    {
        if (index >= start && index < end) {
            return base.get(start + index);
        }
        return null;
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
        int available = end - (start + index);
        if (count > available) {
            throw new IndexOutOfBoundsException("Invalid count: " + count);
        }
        return base.getElements(start + index, count);
    }

    protected @NotNull IList<V> getElements()
    {
        return base.getElements(start, end - start);
    }

    @Override
    public @NotNull IList<V> onReverse()
    {
        return ListReverseImpl.create(this);
    }

    @Override
    public @NotNull IList<V> removingFirst()
    {
        return end > start ? cloneMe(start + 1, end) : this;
    }

    @Override
    public @NotNull IList<V> removingLast()
    {
        return end > start ? cloneMe(start, end - 1) : this;
    }

    @Override
    public @NotNull IList<V> appending(@NotNull V value)
    {
        return getElements().appending(value);
    }

    @Override
    public @NotNull IList<V> prepending(@NotNull V value)
    {
        return getElements().prepending(value);
    }

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        return getElements().appendingAll(values);
    }

    @Override
    public @NotNull IList<V> removing(@NotNull Object value)
    {
        return getElements().removing(value);
    }

    @Override
    public @NotNull IList<V> removing(int index, int count)
      throws IndexOutOfBoundsException
    {
        return getElements().removing(index, count);
    }

    @Override
    public @NotNull <R> IList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        return getElements().map(mapper);
    }

    @Override
    public @NotNull <R> IList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper)
    {
        return getElements().mapFilter(mapper);
    }

    @Override
    public @NotNull IList<V> replacing(int index, @NotNull V value)
      throws IndexOutOfBoundsException
    {
        return getElements().replacing(index, value);
    }

    @Override
    public @NotNull IList<V> replacingAll(int index, int count, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        return getElements().replacingAll(index, count, values);
    }

    @Override
    public @NotNull IList<V> insertingAll(int position, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        return getElements().insertingAll(position, values);
    }

    @Override
    public @NotNull IList<V> reverse()
    {
        return getElements().reverse();
    }

    @Override
    public @NotNull IList<V> sort()
    {
        return getElements().sort();
    }

    @Override
    public @NotNull IList<V> sort(@NotNull Comparator<? super V> c)
    {
        return getElements().sort(c);
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

    protected @NotNull ListSliceImpl<V> cloneMe(int newStart, int newEnd)
    {
        return new ListSliceImpl<>(base, newStart, newEnd);
    }

    private class MyIterator
      implements IIterator<V>
    {
        private int nextIndex;

        public MyIterator()
        {
            nextIndex = start;
        }

        @Override
        public boolean hasNext()
        {
            return nextIndex < end;
        }

        @Override
        public @NotNull V next()
        {
            if (nextIndex >= end) {
                throw new NoSuchElementException();
            }
            return base.get(nextIndex++);
        }
    }

    private class MyReverseIterator
      implements IIterator<V>
    {
        private int nextIndex;

        public MyReverseIterator()
        {
            nextIndex = end - 1;
        }

        @Override
        public boolean hasNext()
        {
            return nextIndex >= start;
        }

        @Override
        public @NotNull V next()
        {
            if (nextIndex < start) {
                throw new NoSuchElementException();
            }
            return base.get(nextIndex--);
        }
    }
}
