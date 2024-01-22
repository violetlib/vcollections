/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import java.util.List;

import org.violetlib.collections.FindVisitor;
import org.violetlib.collections.IList;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.ListUsage;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;

/**
  A tree implementation of IList.
*/

public abstract class TreeList<V>
  implements IList<V>
{
    public static <V> @NotNull ListBuilder<V> builder()
    {
        return TreeListBuilder.create();
    }

    public static <V> @NotNull IList<V> empty()
    {
        return TreeList0.create();
    }

    /**
      Return a list containing the specified elements.
    */

    @SafeVarargs
    public static <V> @NotNull IList<V> create(@NotNull V... elements)
    {
        if (elements.length == 0) {
            return empty();
        }
        ListBuilder<V> b = TreeListBuilder.create();
        for (V element : elements) {
            b.add(element);
        }
        return b.values();
    }

    /**
      Return a list containing the specified elements.
    */

    public static <V> @NotNull IList<V> fromList(@NotNull List<? extends V> elements)
    {
        return createWithElements(elements);
    }

    /**
      Return a list containing the specified elements. If not empty, the returned list will be optimized for adding more
      elements at the end.
      @param elements An iterator providing the elements.
      @throws IllegalArgumentException if the iterator returns a null element.
    */

    public static <V> @NotNull IList<V> createWithElements(@NotNull Iterable<? extends V> elements)
      throws IllegalArgumentException
    {
        ListBuilder<V> b = TreeListBuilder.create();
        for (V element : elements) {
            b.add(element);
        }
        return b.values();
    }

    public static <V> @NotNull IList<V> createWithArray(@NotNull Object @NotNull [] elements)
    {
        ListBuilder<V> b = TreeListBuilder.create();
        for (Object element : elements) {
            b.add((V) element);
        }
        return b.values();
    }

    /**
      Return a list containing the specified elements.
    */

    public static <V> @NotNull IList<V> fromList(@NotNull IList<? extends V> elements)
    {
        if (elements.isEmpty()) {
            return empty();
        }

        TreeList<V> result = Extensions.getExtension(elements, TreeList.class);
        if (result != null) {
            return result;
        } else {
            return createWithElements(elements);
        }
    }

    protected final int size;

    protected TreeList(int size)
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
    public final @NotNull V get(int index)
      throws IndexOutOfBoundsException
    {
        if (index >= 0 && index < size) {
            return internalGet(index);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public final @Nullable V getOptional(int index)
    {
        if (index >= 0 && index < size) {
            return internalGet(index);
        } else {
            return null;
        }
    }

    /**
      Return the value at a valid index.
    */

    protected abstract @NotNull V internalGet(int index);

    protected abstract <R> @Nullable R internalFind(@NotNull FindVisitor<V,R> visitor);

    protected abstract <R> @Nullable R internalFindReverse(@NotNull FindVisitor<V,R> visitor);

    @Override
    public final <R> @Nullable R find(@NotNull FindVisitor<V,R> visitor)
    {
        return internalFind(visitor);
    }

    @Override
    public final <R> @NotNull R find(@NotNull FindVisitor<V,R> visitor, @NotNull R defaultValue)
    {
        R result = internalFind(visitor);
        return result != null ? result : defaultValue;
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
}
