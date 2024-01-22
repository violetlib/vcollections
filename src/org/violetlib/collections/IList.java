/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import javax.annotation.CheckReturnValue;

import org.violetlib.collections.impl.Impl;
import org.violetlib.collections.impl.ListBuilderImpl;
import org.violetlib.collections.impl.ListSliceImpl;
import org.violetlib.collections.impl.RRBList;
import org.violetlib.types.Option;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

import static java.util.Spliterator.*;

/**
  An immutable ordered collection of elements. Null elements are not permitted.

  @param <V> The type of the elements.
*/

public @Immutable @CheckReturnValue interface IList<V>
  extends ICollection<V>, Iterable<V>
{
    @NotNull Option NO_DUPLICATES = Option.named("No duplicates list");

    /**
      Return a list containing no elements.
      If elements are added to the end of this list, the returned list will be optimized for adding more elements at
      the end.
      If elements are added to the beginning of this list, the returned list will be optimized for adding more elements
      at the beginning.
    */

    static <V> @NotNull IList<V> empty()
    {
        return Impl.getEmptyList();
    }

    /**
      Return a list containing no elements.
      @param usage An expected usage.
      If elements are added to list, the returned list will support the specified usage..
    */

    static <V> @NotNull IList<V> empty(@NotNull ListUsage usage)
    {
        return usage == ListUsage.ACCESS ? Impl.getIndexableEmptyList() : Impl.getEmptyList();
    }

    /**
      Return a new list builder that uses a general purpose representation for the created lists.
    */

    static <V> @NotNull ListBuilder<V> builder()
    {
        return ListBuilderImpl.create();
    }

    /**
      Return a new list builder that constructs lists supporting the specified usage.
      @param usage The usage.
      @return the list builder.
    */

    static <V> @NotNull ListBuilder<V> builder(@NotNull ListUsage usage)
    {
        if (usage == ListUsage.APPEND || usage == ListUsage.PREPEND || usage == ListUsage.ACCESS) {
            return ListBuilderImpl.create(usage);
        }
        return RRBList.builder();
    }

    /**
      Return a new list builder.
      @option If the option is {@link #NO_DUPLICATES}, the returned list builder will ignore duplicate elements.
      @return the list builder.
    */

    static <V> @NotNull ListBuilder<V> builder(@Nullable Option option)
    {
        return option == NO_DUPLICATES ? ListBuilderImpl.create(false) : ListBuilderImpl.create(true);
    }

    /**
      Return a new list builder that uses a general purpose representation for the created lists.
    */

    static <V> @NotNull Collector<V,ListBuilder<V>,IList<V>> collector()
    {
        return ListBuilderImpl.collector();
    }

    /**
      Return a new list builder that constructs lists supporting the specified usage.
      @param usage The usage.
      @return the list builder.
    */

    static <V> @NotNull Collector<V,ListBuilder<V>,IList<V>> collector(@NotNull ListUsage usage)
    {
        if (usage == ListUsage.APPEND || usage == ListUsage.PREPEND || usage == ListUsage.ACCESS) {
            return ListBuilderImpl.collector(usage);
        }
        return RRBList.collector();
    }

    /**
      Return a new list builder.
      @option If the option is {@link #NO_DUPLICATES}, the returned list builder will ignore duplicate elements.
      @return the list builder.
    */

    static <V> @NotNull Collector<V,ListBuilder<V>,IList<V>> collector(@Nullable Option option)
    {
        return option == NO_DUPLICATES ? ListBuilderImpl.collector(false) : ListBuilderImpl.collector(true);
    }

    /**
      Return a list containing a single element.
      @param e The element.
      @return a list containing the specified element.
    */

    static <V> @NotNull IList<V> of(@NotNull V e)
    {
        ListBuilder<V> builder = builder();
        builder.add(e);
        return builder.values();
    }

    /**
      Return a list containing specified elements.
      @param elements The elements.
      @return a list containing the specified elements.
    */

    @SafeVarargs
    static <V> @NotNull IList<V> of(@NotNull V... elements)
    {
        ListBuilder<V> builder = builder();
        for (V v : elements) {
            builder.add(v);
        }
        return builder.values();
    }

    /**
      Return a list containing specified elements.
      @param elements The elements. Null values are ignored.
      @return a list containing the specified elements.
    */

    @SafeVarargs
    static <V> @NotNull IList<V> ofOptional(@Nullable V... elements)
    {
        ListBuilder<V> builder = builder();
        for (V v : elements) {
            builder.addOptional(v);
        }
        return builder.values();
    }

    /**
      Return a list containing the specified elements.
      @param it An iterable sequence of elements for the list.
      @return a list containing the specified elements.
      @throws IllegalArgumentException if the iterator returns a null element.
    */

    static <V> @NotNull IList<V> create(@NotNull Iterable<? extends V> it)
      throws IllegalArgumentException
    {
        if (it instanceof IList) {
            return (IList) it;
        }

        ListBuilder<V> builder = builder();
        builder.addAll(it);
        return builder.values();
    }

    /**
      Return a list containing the specified elements.
      @param it An iterable sequence of elements for the list. Null values are ignored.
      @return a list containing the specified elements.
    */

    static <V> @NotNull IList<V> createOptional(@NotNull Iterable<? extends V> it)
      throws IllegalArgumentException
    {
        if (it instanceof IList) {
            return (IList) it;
        }

        ListBuilder<V> builder = builder();
        for (V v : it) {
            builder.addOptional(v);
        }
        return builder.values();
    }

    /**
      Return a list containing the specified elements.
      @param it An array containing the elements for the list.
      @return a list containing the specified elements.
      @throws IllegalArgumentException if the sequence contains a null element.
    */

    static <V> @NotNull IList<V> create(@NotNull V @NotNull [] it)
      throws IllegalArgumentException
    {
        ListBuilder<V> builder = builder();
        for (V v : it) {
            if (v == null) {
                throw new IllegalArgumentException("Null elements are not permitted");
            }
            builder.add(v);
        }
        return builder.values();
    }

    /**
      Return a list containing the specified elements.
      @param it An array containing the elements for the list. Null values are ignored.
      @return a list containing the specified elements.
    */

    static <V> @NotNull IList<V> createOptional(@NotNull V @NotNull [] it)
      throws IllegalArgumentException
    {
        ListBuilder<V> builder = builder();
        for (V v : it) {
            builder.addOptional(v);
        }
        return builder.values();
    }


    /**
      Return a list containing the specified elements.
      @param values The elements for the list.
      @return a list containing the specified elements.
    */

    static @NotNull IList<Integer> create(int @NotNull [] values)
    {
        ListBuilder<Integer> b = IList.builder();
        for (int value : values) {
            b.add(value);
        }
        return b.values();
    }

    /**
      Return a list containing the specified elements.
      @param values The elements for the list.
      @return a list containing the specified elements.
    */

    static @NotNull IList<Long> create(long @NotNull [] values)
    {
        ListBuilder<Long> b = IList.builder();
        for (long value : values) {
            b.add(value);
        }
        return b.values();
    }

    /**
      Return a list containing the specified elements.
      @param values The elements for the list.
      @return a list containing the specified elements.
    */

    static @NotNull IList<Float> create(float @NotNull [] values)
    {
        ListBuilder<Float> b = IList.builder();
        for (float value : values) {
            b.add(value);
        }
        return b.values();
    }

    /**
      Return a list containing the specified elements.
      @param values The elements for the list.
      @return a list containing the specified elements.
    */

    static @NotNull IList<Double> create(double @NotNull [] values)
    {
        ListBuilder<Double> b = IList.builder();
        for (double value : values) {
            b.add(value);
        }
        return b.values();
    }

    /**
      Return a list containing the specified elements, optimized for the specified usage.
      @param usage The usage.
      @param it An iterable sequence of elements for the list.
      @return a list containing the specified elements.
      @throws IllegalArgumentException if the sequence contains a null element.
    */

    static <V> @NotNull IList<V> create(@NotNull ListUsage usage, @NotNull Iterable<? extends V> it)
      throws IllegalArgumentException
    {
        ListBuilder<V> builder = builder(usage);
        builder.addAll(it);
        return builder.values();
    }

    /**
      Cast a list to a specific type. This method is no more reliable than an explicit type cast, but it prevents the
      warning.
    */

    static <V> @NotNull IList<V> cast(@NotNull IList<?> o)
    {
        @SuppressWarnings("unchecked")
        IList<V> result = (IList) o;
        return result;
    }

    /**
      Cast a list to a specific type. This method is no more reliable than an explicit type cast, but it prevents the
      warning.
    */

    static <V> @Nullable IList<V> castNullable(@Nullable IList<?> o)
    {
        @SuppressWarnings("unchecked")
        IList<V> result = (IList) o;
        return result;
    }

    /**
      Return true if and only if there are no elements in the list.
    */

    @Override
    boolean isEmpty();

    /**
      Return the number of elements in the list.
    */

    @Override
    int size();

    /**
      Return true if and only if the specified value is an element of the list.
      @param target The value to find in the list.
      @return true if and only if {@code target} is an element of the list.
    */

    @Override
    default boolean contains(@NotNull Object target)
    {
        return indexOf(target) >= 0;
    }

    /**
      Return the index of the specified element in the list.

      @param element The element to find.
      @return the index of {@code element} in the list, or -1 if {@code element} is not an element of the list.
    */

    default int indexOf(@NotNull Object element)
    {
        int index = 0;
        for (V v : this) {
            if (element.equals(v)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
      Visit each element of the list from the first element to the last element.
      @param visitor The visitor to call on the list elements.
    */

    @Override
    default void visit(@NotNull Visitor<V> visitor)
    {
        for (V v : this) {
            visitor.visit(v);
        }
    }

    /**
      Visit the elements of the list from the first element to the last element until the visitor returns a non-null
      result.

      @param visitor The visitor to call on the list elements.
      @return the first non-null result returned by the visitor, or null if none.
    */

    @Override
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
      Visit each element of the list from the first element to the last element until the visitor returns a non-null
      result.

      @param visitor The visitor to call on the list elements.
      @param defaultValue The value to return if the visitor never returns a non-null result.
      @return the first non-null result returned by the visitor, or {@code defaultValue} if none.
    */

    @Override
    default <R> @NotNull R find(@NotNull FindVisitor<V,R> visitor, @NotNull R defaultValue)
    {
        R result = find(visitor);
        return result != null ? result : defaultValue;
    }

    /**
      Return the first element of the list.
      @return the first element of the list, or null if the list is empty.
    */

    default @Nullable V optionalFirst()
    {
        Iterator<V> it = iterator();
        return it.hasNext() ? it.next() : null;
    }

    /**
      Return the first element of the list.
      @return the first element of the list.
      @throws NoSuchElementException if the list is empty.
    */

    default @NotNull V first()
      throws NoSuchElementException
    {
        V result = optionalFirst();
        if (result != null) {
            return result;
        }
        throw new NoSuchElementException();
    }

    /**
      Return the last element of the list.
      @return the last element of the list, or null if the list is empty.
    */

    default @Nullable V optionalLast()
    {
        V result = null;
        for (V v : this) {
            result = v;
        }
        return result;
    }

    /**
      Return the last element of the list.
      @return the last element of the list.
      @throws NoSuchElementException if the list is empty.
    */

    default @NotNull V last()
      throws NoSuchElementException
    {
        V result = optionalLast();
        if (result != null) {
            return result;
        }
        throw new NoSuchElementException();
    }

    /**
      Return the element with the specified index.

      @param index The index.

      @return the designated element, or null if there is no element with that index.
    */

    default @Nullable V getOptional(int index)
    {
        if (index >= 0) {
            int currentIndex = 0;
            for (V v : this) {
                if (currentIndex == index) {
                    return v;
                }
                currentIndex++;
            }
        }
        return null;
    }

    /**
      Return the element with the specified index.

      @param index The index.

      @return the designated element.

      @throws IndexOutOfBoundsException if there is no element with the specified index.
    */

    default @NotNull V get(int index)
      throws IndexOutOfBoundsException
    {
        V result = getOptional(index);
        if (result == null) {
            throw new IndexOutOfBoundsException();
        }
        return result;
    }

    /**
      Return the elements with consecutive indexes.

      @param index The index of the first element to return.
      @param count The number of elements to return.

      @return the designated elements (empty if {@code count} is zero).

      @throws IndexOutOfBoundsException if {@code count} is negative or the specified index range identifies elements
      that are not present.
    */

    @NotNull IList<V> getElements(int index, int count)
      throws IndexOutOfBoundsException;

    /**
      Return a list containing a designated subsequence of the elements of this list.
      The returned list is called a slice of this list.
      <p>
      The slice may be a <em>view</em> on this list rather than a copy of this list. A view can provide much better
      performance by avoiding the need to copy the list, especially when the slice is used to perform traversals. The
      implication is that the slice may hold a strong reference to this list, preventing this list from being reclaimed
      before the slice. Using this method is most appropriate when it is used only to perform a single operation and is
      not retained.
      <p>

      @param start The index of the first element to be included in the slice.
      @param end One greater than the index of the last element to be included in the slice. If this parameter is
      greater than the size of this list, it is interpreted as if it were specified as the size of this list.
      @throws IndexOutOfBoundsException if {@code start} is negative or greater than the size of this list or if
      {@code end} is less than {@code start}.
    */

    default @NotNull IList<V> onSlice(int start, int end)
      throws IndexOutOfBoundsException
    {
        return ListSliceImpl.create(this, start, end);
    }

    /**
      Return a list containing the same elements as this list, but in the reverse order.
      <p>
      The returned list may be a <em>view</em> on this list rather than a copy of this list. A view can provide much
      better performance by avoiding the need to copy the list when the result is used only to perform traversals.
    */

    @NotNull IList<V> onReverse();

    /**
      Return a list with an additional value added at the end.
      @param value The value to be appended.
      @return A list with the values from this list, plus the specified value.
    */

    @NotNull IList<V> appending(@NotNull V value); // not called "add" to catch errors when converting a program to use immutable lists

    /**
      Return a list with an additional value added at the beginning.
      @param value The value to be added.
      @return A list with the specified value, plus the values from this list.
    */

    @NotNull IList<V> prepending(@NotNull V value);

    /**
      Return a list with additional values added at the end.
      @param values An iterator providing the values to be appended.
      @return A list with the values from this list, plus the specified values.
      @throws IllegalArgumentException if the iterator returns a null element.
    */

    @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException;

    /**
      Return this list with a value removed.

      @param value The value to be removed (if present).
      @return A list with the values from this list, in the same order, but exclusing all instances of the specified
      value.
    */

    @NotNull IList<V> removing(@NotNull Object value);

    /**
      Return a list with consecutive elements removed.

      @param index The index of the first element to remove.
      @param count The number of elements to remove.
      @return A list with the values from this list, in the same order, except the removed elements.
      @throws IndexOutOfBoundsException if {@code index} does not identify an existing element or if {@code count} is
      less than zero. No exception is thrown if {@code count} exceeds the available elements or if {@code count} is
      zero.
    */

    @NotNull IList<V> removing(int index, int count)
      throws IndexOutOfBoundsException;

    /**
      Return this list with the first element removed. If this list is empty, the empty list is returned.

      @return A list with the values from this list, except the first element.
    */

    default @NotNull IList<V> removingFirst()
    {
        if (isEmpty()) {
            return this;
        }
        return removing(0, 1);
    }

    /**
      Return this list with the last element removed. If this list is empty, the empty list is returned.

      @return A list with the values from this list, except the last element.
    */

    default @NotNull IList<V> removingLast()
    {
        if (isEmpty()) {
            return this;
        }
        int lastIndex = size() - 1;
        return removing(lastIndex, 1);
    }

    /**
      Return a list with the values from this list, in the same order, except that all instances of the specified values
      are excluded.

      @param values The values to be excluded in the result.
      @return A list with the values from this list, except the specified values.
    */

    default @NotNull IList<V> removingAll(@NotNull ISet<?> values)
    {
        IList<V> result = this;
        for (Object value : values) {
            result = result.removing(value);
        }
        return result;
    }

    /**
      Return a list containing the result of applying a mapper to each list element.
      @param mapper Maps elements of the list to elements of the result. The mapper must not return null.
      @return a list containing the results of the mapper calls.
    */

    <R> @NotNull IList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper);

    /**
      Return a list containing the result of applying a mapper to each list element.
      @param mapper Maps elements of the list to elements of the result. If the mapper returns null, no element is
      added to the result.
      @return a list containing the non-null results of the mapper calls.
    */

    <R> @NotNull IList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper);

    /**
      Return a list containing the same elements as this list, but in the reverse order.
    */

    default @NotNull IList<V> reverse()
    {
        List<V> elements = toJavaList();
        Collections.reverse(elements);
        return IList.create(elements);
    }

    /**
      Return a list containing the same elements as this list, but sorted using the natural comparator.
      @return the sorted list.
    */

    default @NotNull IList<V> sort()
    {
        return sort(Impl.getUniversalComparator());
    }

    /**
      Return a list containing the same elements as this list, but sorted using the specified comparator.
      @param c The comparator used to determine the order of the elements in the returned list.
      @return the sorted list.
    */

    @NotNull IList<V> sort(@NotNull Comparator<? super V> c);

    /**
      Return a list with a new value for the specified element.

      @returns a list with the same elements as this list, except that the element at the specified index is the
      specified value.

      @throws IndexOutOfBoundsException if this list does not contain an element with the specified index.
    */

    @NotNull IList<V> replacing(int index, @NotNull V value)
      throws IndexOutOfBoundsException;

    /**
      Return a list with new values replacing the specified elements.

      @param index The position of the first element to replace.
      @param count The number of elements to replace. With a count of zero this operation is equivalent to
      {@link #insertingAll}.
      @param values The values to replace the designated elements.
      @returns a list as described.

      @throws IndexOutOfBoundsException if {@code index} does not identify an existing element or if {@code count} is
      less than zero. No exception is thrown if {@code count} exceeds the available elements or if {@code count} is
      zero.
      @throws IllegalArgumentException if the iterator returns a null element.
    */

    @NotNull IList<V> replacingAll(int index, int count, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException;

    /**
      Return a list with a new value inserted at the specified position.

      @param position The position at which the value is inserted.
      @param value The value to insert as an element of the returned list.
      @returns a list as described.

      @throws IndexOutOfBoundsException if {@code position} is not a valid position.
    */

    default @NotNull IList<V> inserting(int position, @NotNull V value)
      throws IndexOutOfBoundsException
    {
        return insertingAll(position, IList.of(value));
    }

    /**
      Return a list with new values inserted at the specified position.

      @param position The position at which the values are inserted.
      @param values The values to insert as elements of the returned list.
      @returns a list as described.

      @throws IndexOutOfBoundsException if {@code position} is not a valid position.
      @throws IllegalArgumentException if the iterator returns a null element.
    */

    default @NotNull IList<V> insertingAll(int position, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        return replacingAll(position, 0, values);
    }

    @Override
    default Spliterator<V> spliterator() {
        return Spliterators.spliterator(iterator(), size(), IMMUTABLE | NONNULL | SIZED | ORDERED);
    }

    /**
      Return a new Java list containing the elements of this list, in the same order.
    */

    @Override
    default @NotNull List<V> toJavaList()
    {
        List<V> result = new ArrayList<>();
        visit((Visitor<V>) result::add);
        return result;
    }

    /**
      Return a new Java set containing the elements of this collection.
    */

    @Override
    default @NotNull Set<V> toJavaSet()
    {
        Set<V> result = new HashSet<>();
        visit((Visitor<V>) result::add);
        return result;
    }

    /**
      Return an equivalent list whose representation efficiently supports the specified usage.
    */

    @NotNull IList<V> optimize(@NotNull ListUsage usage);

    /**
      Return an equivalent list whose representation efficiently supports forward traversal.
    */

    @NotNull IList<V> optimizeForForwardTraversal();

    /**
      Return an equivalent list whose representation efficiently supports indexed access to list elements.
    */

    @NotNull IList<V> optimizeForIndexing();
}
