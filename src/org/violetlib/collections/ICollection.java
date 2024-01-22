/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.CheckReturnValue;

import org.violetlib.collections.impl.EmptyIIterator;
import org.violetlib.collections.impl.Impl;

import org.jetbrains.annotations.*;

import static java.util.Spliterator.*;

/**
  Common methods for immutable collections.

  @param <V> The type of the elements.
*/

public @CheckReturnValue interface ICollection<V>
    extends IIterable<V>
{
    /**
      Cast a collection to a specific type. This method is no more reliable than an explicit type cast, but it prevents
      the warning.
    */

    static <V> @NotNull ICollection<V> cast(@NotNull ICollection<?> o)
    {
        @SuppressWarnings("unchecked")
        ICollection<V> result = (ICollection) o;
        return result;
    }

    static <V> @NotNull ICollection<V> concatenate(@NotNull IList<? extends ICollection<V>> sources)
    {
        return Impl.concatenate(sources);
    }

    static <V> @NotNull IIterator<V> concatenateIterators(@NotNull IList<? extends IIterator<V>> sources)
    {
        return Impl.concatenateIterators(sources);
    }

    @SafeVarargs
    static <V> @NotNull ICollection<V> concatenate(ICollection<? extends V>... sources)
    {
        return Impl.concatenate(IList.cast(IList.create(sources)));
    }

    static <V> @NotNull IIterator<V> emptyIterator()
    {
        return EmptyIIterator.get();
    }

    /**
      Return true if and only if there are no elements in the collection.
    */

    default boolean isEmpty()
    {
        Iterator<V> it = iterator();
        return !it.hasNext();
    }

    /**
      Return the number of elements in the collection.
    */

    default int size()
    {
        int count = 0;
        for (V v : this) {
            ++count;
        }
        return count;
    }

    /**
      Return true if and only if the specified element is an element of the collection.

      @param target The element to find in the list.
      @return true if and only if {@code target} is an element of the collection.
    */

    default boolean contains(@NotNull Object target)
    {
        return find(e -> e.equals(target) ? true : null, false);
    }

    /**
      Visit each element of the collection. The elements are visited in the order defined by the collection (if there is
      one); otherwise, the visitation order is unspecified.

      @param visitor The visitor to call on the elements.
    */

    default void visit(@NotNull Visitor<V> visitor)
    {
        for (V v : this) {
            visitor.visit(v);
        }
    }

    /**
      Visit the elements of the collection until the visitor returns a non-null result. The elements are visited in the
      order defined by the collection (if there is one); otherwise, the visitation order is unspecified.

      @param visitor The visitor to call on the elements.
      @return the first non-null result returned by the visitor, or null if none.
    */

    default <R> @Nullable R find(@NotNull FindVisitor<V,R> visitor)
    {
        for (V v : this) {
            R result = visitor.visit(v);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
      Visit each element of the collection until the visitor returns a non-null result. The elements are visited in the
      order defined by the collection (if there is one); otherwise, the visitation order is unspecified.

      @param visitor The visitor to call on the elements.
      @param defaultValue The value to return if no visitor call returns a non-null result.
      @return the first non-null result returned by the visitor, or {@code defaultValue} if none.
    */

    default <R> @NotNull R find(@NotNull FindVisitor<V,R> visitor, @NotNull R defaultValue)
    {
        R result = find(visitor);
        return result != null ? result : defaultValue;
    }

    /**
      Return a list containing the members of this collection, sorted in the natural sort order.
      @return the list.
    */

    default @NotNull IList<V> sort()
    {
        return IList.create(this).sort();
    }

    /**
      Return a list containing the same elements as this collection, but sorted using the specified comparator.
      @param c The comparator used to determine the order of the elements in the returned list.
      @return the sorted list.
    */

    default @NotNull IList<V> sort(@NotNull Comparator<? super V> c)
    {
        return IList.create(this).sort(c);
    }

    /**
      Return a Spliterator that yields the elements of the collection.
    */

    @Override
    default Spliterator<V> spliterator() {
        return Spliterators.spliterator(iterator(), size(), IMMUTABLE | NONNULL | SIZED);
    }

    /**
      Return a stream of the elements of the collection.
    */

    default Stream<V> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
      Return a new Java list containing the elements of this collection, in the same order (if the collection defines an
      order).
    */

    default @NotNull List<V> toJavaList()
    {
        List<V> result = new ArrayList<>();
        for (V v: this) {
            result.add(v);
        }
        return result;
    }

    /**
      Return a new unmodifiable Java list containing the elements of this collection, in the same order (if the
      collection defines an order).
    */

    default @NotNull List<V> toJavaUnmodifiableList()
    {
        return Collections.unmodifiableList(toJavaList());
    }

    /**
      Return a new Java set containing the elements of this collection.
    */

    default @NotNull Set<V> toJavaSet()
    {
        Set<V> result = new HashSet<>();
        for (V v: this) {
            result.add(v);
        }
        return result;
    }

    /**
      Return a new Java set containing the elements of this collection.
    */

    default @NotNull Set<V> toJavaUnmodifiableSet()
    {
        return Collections.unmodifiableSet(toJavaSet());
    }

    /**
      Return a new Java array containing the elements of this collection.
    */

    default <E> @NotNull E[] toJavaArray(@NotNull E[] template)
    {
        return toJavaList().toArray(template);
    }
}
