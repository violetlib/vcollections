/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;

import org.violetlib.collections.ICollection;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;
import org.violetlib.collections.ISet;
import org.violetlib.collections.SetBuilder;

import org.jetbrains.annotations.*;

import static java.util.Spliterator.*;

/**

*/

public final class SimpleOrderedSet<V>
  implements ISet<V>
{
    public static <V> @NotNull ISet<V> create(@NotNull ISet<V> members, @NotNull IList<V> order)
    {
        return new SimpleOrderedSet<>(members, order);
    }

    public static <V> @NotNull ISet<V> create(@NotNull IList<V> members)
    {
        return new SimpleOrderedSet<>(ISet.create(members), members);
    }

    private final @NotNull ISet<V> members;
    private final @NotNull IList<V> order;

    private SimpleOrderedSet(@NotNull ISet<V> members, @NotNull IList<V> order)
    {
        this.members = members;
        this.order = order;
    }

    @Override
    public int size()
    {
        return members.size();
    }

    @Override
    public @NotNull ISet<V> extending(@NotNull V value)
    {
        if (members.contains(value)) {
            return this;
        }

        return new SimpleOrderedSet<>(members.extending(value), order.appending(value));
    }

    @Override
    public @NotNull ISet<V> extendingAll(@NotNull ICollection<? extends V> values)
    {
        ISet<V> result = this;
        for (V value : values) {
            result = result.extending(value);
        }
        return result;
    }

    @Override
    public @NotNull ISet<V> removing(@NotNull Object value)
    {
        if (members.contains(value)) {
            return new SimpleOrderedSet<>(members.removing(value), order.removing(value));
        } else {
            return this;
        }
    }

    @Override
    public <R> @NotNull ISet<R> map(@NotNull Function<V,R> mapper)
    {
        SetBuilder<R> b = ISet.builder();
        for (V v : order) {
            R mapped = mapper.apply(v);
            b.addOptional(mapped);
        }
        return b.values();
    }

    @Override
    public @NotNull IIterator<V> iterator()
    {
        return order.iterator();
    }

    @Override
    public @NotNull Spliterator<V> spliterator()
    {
        return Spliterators.spliterator(iterator(), size(), IMMUTABLE | NONNULL | SIZED | DISTINCT);
    }
}
