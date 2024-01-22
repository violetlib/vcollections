/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.collections.Builder;
import org.violetlib.collections.IList;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.ListUsage;
import org.violetlib.collections.util.CollectorImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;

/**
  A builder of lists.

  @param <V> The type of the list elements.
*/

public final class ListBuilderImpl<V>
  implements ListBuilder<V>
{
    public static <V> ListBuilder<V> create()
    {
        return new ListBuilderImpl<>(true, null);
    }

    public static <V> ListBuilder<V> create(boolean allowDuplicates)
    {
        return new ListBuilderImpl<>(allowDuplicates, null);
    }

    public static <V> ListBuilder<V> create(@NotNull ListUsage usage)
    {
        return new ListBuilderImpl<>(true, usage);
    }

    public static <V> @NotNull Collector<V,ListBuilder<V>,IList<V>> collector()
    {
        return CollectorImpl.<V,ListBuilder<V>,IList<V>>create(
          () -> ListBuilderImpl.<V>create(),
          Builder::add,
          ListBuilderImpl::append,
          ListBuilder::values,
          CH_NOID);
    }

    public static <V> @NotNull Collector<V,ListBuilder<V>,IList<V>> collector(@NotNull ListUsage usage)
    {
        return CollectorImpl.<V,ListBuilder<V>,IList<V>>create(
          () -> ListBuilderImpl.<V>create(usage),
          Builder::add,
          ListBuilderImpl::append,
          ListBuilder::values,
          CH_NOID);
    }

    public static <V> @NotNull Collector<V,ListBuilder<V>,IList<V>> collector(boolean allowDuplicates)
    {
        return CollectorImpl.<V,ListBuilder<V>,IList<V>>create(
          () -> ListBuilderImpl.<V>create(allowDuplicates),
          Builder::add,
          ListBuilderImpl::append,
          ListBuilder::values,
          CH_NOID);
    }

    private static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

    private static <V> ListBuilder<V> append(@NotNull ListBuilder<V> b1, @NotNull ListBuilder<V> b2)
    {
        b1.addAll(b2.values());
        return b1;
    }

    private final boolean allowDuplicates;
    private final @NotNull List<V> elements;
    private final @Nullable ListUsage usage;

    private ListBuilderImpl(boolean allowDuplicates, @Nullable ListUsage usage)
    {
        this.allowDuplicates = allowDuplicates;
        this.elements = new ArrayList<>();
        this.usage = usage;
    }

    /**
      Remove all elements.
    */

    @Override
    public void reset()
    {
        elements.clear();
    }

    /**
      Add an element to the current list of elements.
      @param element The element to be added.
    */

    @Override
    public void add(@NotNull V element)
    {
        // safety check
        if (element == null) {
            throw new IllegalArgumentException("Element must not be null");
        }

        if (!allowDuplicates && elements.contains(element)) {
            return;
        }

        elements.add(element);
    }

    @Override
    public boolean isEmpty()
    {
        return elements.isEmpty();
    }

    @Override
    public int size()
    {
        return elements.size();
    }

    @Override
    public @Nullable V lastItem()
    {
        int count = elements.size();
        return count > 0 ? elements.get(count-1) : null;
    }

    /**
      Return an immutable list containing the current elements.
      If no usage was specified to create this builder, the list is optimized for access, not for extending.
      @return the list.
    */

    @Override
    public @NotNull IList<V> values()
    {
        return usage != null ? values(usage) : SimpleList.fromList(elements);
    }

    /**
      Return an immutable list containing the current elements.
      The list is optimized for the specified usage.
      @param usage The expected usage of the list.
      @return the list.
    */

    public @NotNull IList<V> values(@NotNull ListUsage usage)
    {
        if (elements.isEmpty()) {
            if (usage == ListUsage.DEFAULT) {
                return IList.empty(ListUsage.ACCESS);
            } else {
                return IList.empty();
            }
        }

        if (usage == ListUsage.APPEND) {
            return AppendOrientedList.fromList(elements);
        }
        if (usage == ListUsage.PREPEND) {
            return PrependOrientedList.fromList(elements);
        }
        return SimpleList.fromList(elements);
    }
}
