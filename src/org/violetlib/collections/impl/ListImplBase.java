/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.List;

import org.violetlib.collections.FindVisitor;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;

/**
  A base class for list implementations that cache the list size.
*/

public abstract class ListImplBase<V>
  implements IList<V>
{
    protected final int size;

    protected ListImplBase(int size)
    {
        this.size = size;
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
    public @Nullable V getOptional(int index)
    {
        if (index < 0 || index >= size) {
            return null;
        }
        return find(ListImplSupport.createGetVisitor(index));
    }

    @Override
    public @NotNull V get(int index)
      throws IndexOutOfBoundsException
    {
        V result = getOptional(index);
        if (result == null) {
            throw new IndexOutOfBoundsException();
        }
        return result;
    }

    @Override
    public @NotNull IList<V> getElements(int index, int count)
      throws IndexOutOfBoundsException
    {
        return ListOperations.getElements(this, index, count);
    }

    @Override
    public int indexOf(@NotNull Object element)
    {
        return find(ListImplSupport.createIndexOfVisitor(element), -1);
    }

    @Override
    public boolean contains(@NotNull Object target)
    {
        FindVisitor<V,Boolean> visitor = e -> e.equals(target) ? true : null;
        return find(visitor, false);
    }

    @Override
    public abstract <R> @Nullable R find(@NotNull FindVisitor<V,R> visitor);

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        return ListOperations.appendingAll(this, values);
    }

    @Override
    public @NotNull IList<V> replacing(int index, @NotNull V value)
      throws IndexOutOfBoundsException
    {
        return ListOperations.replacing(this, index, value, IList.builder());
    }

    @Override
    public @NotNull IList<V> replacingAll(int index, int count, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        return ListOperations.replacingAll(this, index, count, values, IList.builder());
    }

    @Override
    public @NotNull IList<V> removing(@NotNull Object value)
    {
        return ListOperations.removing(this, value);
    }

    @Override
    public @NotNull IList<V> removing(int index, int deleteCount)
      throws IndexOutOfBoundsException
    {
        return ListOperations.removing(this, index, deleteCount, RRBList.builder());
    }

    @Override
    public @NotNull IList<V> onReverse()
    {
        return ListReverseImpl.create(this);
    }

    @Override
    public @NotNull List<V> toJavaList()
    {
        return IList.super.toJavaList();
    }

    @Override
    public @NotNull IIterator<V> iterator()
    {
        List<V> elements = toJavaList();
        return IIterator.from(elements.iterator());
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
    public @NotNull String toString()
    {
        StringBuilder b = new StringBuilder();
        b.append('(');
        for (V e : this) {
            if (b.length() > 1) {
                b.append(' ');
            }
            b.append(e.toString());
        }
        b.append(')');
        return b.toString();
    }
}
