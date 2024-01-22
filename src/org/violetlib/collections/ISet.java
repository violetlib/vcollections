/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.CheckReturnValue;

import org.violetlib.collections.impl.Impl;
import org.violetlib.types.Option;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  An immutable set of elements. Null elements are not permitted. The iteration order may be unspecified.

  @param <V> The type of the values.
*/

public @Immutable @CheckReturnValue interface ISet<V>
  extends ICollection<V>, IIterable<V>
{
    @NotNull Option ORDERED = Option.named("Ordered Set");

    /**
      Return an empty set.
    */

    static <V> @NotNull ISet<V> empty()
    {
        return Impl.getEmptySet();
    }

    /**
      Create a set containing the specified elements.
      @param elements An iterator that provides the elements.
      @return the set.
      @throws IllegalArgumentException if the iterator returns a null value.
    */

    static <V> @NotNull ISet<V> create(@NotNull Iterable<? extends V> elements)
      throws IllegalArgumentException
    {
        return Impl.createSet(elements);
    }

    /**
      Create a set containing the specified elements.
      @param elements An array that provides the elements.
      @return the set.
      @throws IllegalArgumentException if the array contains a null value.
    */

    static <V> @NotNull ISet<V> create(@NotNull V[] elements)
      throws IllegalArgumentException
    {
        return Impl.setOf(elements);
    }

    static <V> @NotNull ISet<V> of(@NotNull V e)
    {
        return Impl.setOf(e);
    }

    @SafeVarargs
    static <V> @NotNull ISet<V> of(V... es)
    {
        return Impl.setOf(es);
    }

    /**
      Create a builder for a set. The iteration order of the set is unspecified.

      @param <V> The type of set elements.
      @return the builder.
    */

    static <V> @NotNull SetBuilder<V> builder()
    {
        return Impl.getSetBuilder();
    }

    /**
      Create a builder for a set whose iteration order is defined by the order in which elements are added to the
      builder.

      @return the builder.
      @param <V> The type of set elements.
      @param option
    */

    static <V> @NotNull SetBuilder<V> builder(Option option)
    {
        return option == ORDERED ? Impl.getOrderedSetBuilder() : Impl.getSetBuilder();
    }

    /**
      Cast a set to a specific type. This method is no more reliable than an explicit type cast, but it prevents the
      warning.
    */

    static <V> @NotNull ISet<V> cast(@NotNull ISet<?> o)
    {
        @SuppressWarnings("unchecked")
        ISet<V> result = (ISet) o;
        return result;
    }

    @Override
    default boolean isEmpty()
    {
        return size() == 0;
    }

    @Override
    int size();

    @Override
    default boolean contains(@NotNull Object target)
    {
        return find(e -> e.equals(target) ? true : null, false);
    }

    /**
      Return one element of this set.

      @throws NoSuchElementException if the set is empty.
    */

    default @NotNull V choose()
      throws NoSuchElementException
    {
        Iterator<V> it = iterator();
        return it.next();
    }

    /**
      Return one element of this set, or null if the set is empty.
    */

    default @Nullable V chooseOptional()
    {
        if (isEmpty()) {
            return null;
        }
        Iterator<V> it = iterator();
        return it.next();
    }

    /**
      Return the sole element of this set.

      @throws NoSuchElementException if the set does not contain exactly one element.
    */

    default @NotNull V element()
      throws NoSuchElementException
    {
        Iterator<V> it = iterator();
        V element = it.next();
        if (it.hasNext()) {
            throw new NoSuchElementException();
        }
        return element;
    }

    /**
      Return a set containing the members of this set and the specified value.

      @param value The value to be included in the result.
      @return A set containing the values from this set and the specified value.
    */

    @NotNull ISet<V> extending(@NotNull V value);

    /**
      Return a set containing the members of this set and the specified values.

      @param values The values to be included in the result.
      @return A set containing the values from this set and the specified values.
    */

    @NotNull ISet<V> extendingAll(@NotNull ICollection<? extends V> values);

    /**
      Return a set containing the members of this set excluding the specified value.

      @param value The value to be excluded from the result.
      @return A set containing the values from this set, but not the specified value.
    */

    @NotNull ISet<V> removing(@NotNull Object value);

    default @NotNull ISet<V> removingAll(@NotNull ISet<?> values)
    {
        ISet<V> result = this;
        for (Object value : values) {
            result = result.removing(value);
        }
        return result;
    }

    default @NotNull ISet<V> filter(@NotNull Predicate<V> predicate)
    {
        SetBuilder<V> b = ISet.builder();
        for (V v : this) {
            if (predicate.test(v)) {
                b.add(v);
            }
        }
        return b.values();
    }

    <R> @NotNull ISet<R> map(@NotNull Function<V,R> mapper);

    /**
      Return a set containing the members of this collection whose iteration order is defined by the natural sort order.
    */

    default @NotNull ISet<V> ordered()
    {
        IList<V> elements = sort();
        SetBuilder<V> b = ISet.builder(ISet.ORDERED);
        b.addAll(elements);
        return b.values();
    }

    /**
      Return a set containing the members of this collection whose iteration order is defined by the specified comparator.
    */

    default @NotNull ISet<V> ordered(@NotNull Comparator<? super V> comp)
    {
        IList<V> elements = sort(comp);
        SetBuilder<V> b = ISet.builder(ISet.ORDERED);
        b.addAll(elements);
        return b.values();
    }

    default @NotNull ISet<V> intersecting(@NotNull ISet<?> other)
    {
        return intersection(this, other);
    }

    static <V> @NotNull ISet<V> intersection(@NotNull ISet<V> s, @NotNull ISet<?> p)
    {
        boolean isChanged = false;
        SetBuilder<V> b = ISet.builder();
        for (V v : s) {
            if (p.contains(v)) {
                b.add(v);
            } else {
                isChanged = true;
            }
        }
        return isChanged ? b.values() : s;
    }
}
