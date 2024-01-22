/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.violetlib.collections.IList;
import org.violetlib.collections.ISet;
import org.violetlib.collections.SetBuilder;

import org.jetbrains.annotations.*;

/**
  A builder of sets.

  @param <V> The type of the set elements.
*/

public final class OrderedSetBuilderImpl<V>
  implements SetBuilder<V>
{
    public static <V> SetBuilder<V> create()
    {
        return new OrderedSetBuilderImpl<>();
    }

    private final @NotNull Set<V> elements;
    private final @NotNull List<V> order;

    private OrderedSetBuilderImpl()
    {
        elements = new HashSet<>();
        order = new ArrayList<>();
    }

    /**
      Remove all elements.
    */

    @Override
    public void reset()
    {
        elements.clear();
        order.clear();
    }

    /**
      Add an element to the current set of elements.
      @param element The element to be added.
    */

    @Override
    public void add(@NotNull V element)
    {
        // safety check
        if (element == null) {
            throw new IllegalArgumentException("Element must not be null");
        }

        if (!elements.contains(element)) {
            elements.add(element);
            order.add(element);
        }
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

    /**
      Return an immutable set containing the current elements.
      @return the set.
    */

    @Override
    public @NotNull ISet<V> values()
    {
        return SimpleOrderedSet.create(ISet.create(elements), IList.create(order));
    }
}
