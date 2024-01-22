/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.List;
import java.util.function.Function;

import org.violetlib.collections.FindVisitor;
import org.violetlib.collections.IList;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.Visitor;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**
  Operations on lists that can be performed using list methods.
*/

public final @NoInstances class ListOperations
{
    public ListOperations()
    {
        throw new AssertionError("ListOperations may not be instantiated");
    }

    public static <V> @NotNull IList<V> getElements(@NotNull IList<V> list, int index, int count)
      throws IndexOutOfBoundsException
    {
        if (index < 0 || count < 0 || index + count > list.size()) {
            throw new IndexOutOfBoundsException();
        }

        if (count == 0) {
            return IList.empty();
        }

        IList<V> result = list.find(ListImplSupport.createSubrangeVisitor(index, count, RRBList.builder()));
        assert result != null;
        return result;
    }

    public static <V> @NotNull IList<V> prepending(@NotNull IList<V> list,
                                                   @NotNull V value,
                                                   @NotNull ListBuilder<V> builder)
      throws IllegalArgumentException
    {
        builder.add(value);
        builder.addAll(list);
        return builder.values();
    }

    public static <V> @NotNull IList<V> appending(@NotNull IList<V> list,
                                                  @NotNull V value,
                                                  @NotNull ListBuilder<V> builder)
      throws IllegalArgumentException
    {
        builder.addAll(list);
        builder.add(value);
        return builder.values();
    }

    public static <V> @NotNull IList<V> appendingAll(@NotNull IList<V> list,
                                                     @NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        IList<V> result = list;
        for (V value : values) {
            if (value == null) {
                throw new IllegalArgumentException("Null elements are not permitted");
            }
            result = result.appending(value);
        }
        return result;
    }

    public static <V> @NotNull IList<V> appendingAll(@NotNull IList<V> list,
                                                     @NotNull Iterable<? extends V> values,
                                                     @NotNull ListBuilder<V> builder)
      throws IllegalArgumentException
    {
        builder.addAll(list);
        builder.addAll(values);
        return builder.values();
    }

    public static <V> @NotNull IList<V> appendingAll(@NotNull IList<V> list,
                                                     @NotNull List<? extends V> values)
    {
        IList<V> result = list;
        for (V value : values) {
            result = result.appending(value);
        }
        return result;
    }

    public static <V> @NotNull IList<V> appendingAll(@NotNull IList<V> list,
                                                     @NotNull List<? extends V> values,
                                                     @NotNull ListBuilder<V> builder)
    {
        builder.addAll(list);
        builder.addAll(values);
        return builder.values();
    }

    public static <V> @NotNull IList<V> insertingAll(@NotNull IList<V> list,
                                                     int position,
                                                     @NotNull Iterable<? extends V> values,
                                                     @NotNull ListBuilder<V> builder)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        int size = list.size();
        if (position < 0 || position > size) {
            throw new IndexOutOfBoundsException();
        }
        if (list.isEmpty()) {
            builder.addAll(values);
            return builder.values();
        }

        FindVisitor<V,IList<V>> v = ListImplSupport.createInsertElementsVisitor(size-1, position, values, builder);
        IList<V> result = list.find(v);
        assert result != null;
        return result;
    }

    public static <V> @NotNull IList<V> replacing(@NotNull IList<V> list,
                                                  int index,
                                                  @NotNull V value,
                                                  @NotNull ListBuilder<V> builder)
    {
        if (index < 0 || index >= list.size()) {
            throw new IndexOutOfBoundsException();
        }
        FindVisitor<V,IList<V>> v = ListImplSupport.createReplaceElementVisitor(list, index, value, builder);
        IList<V> result = list.find(v);
        assert result != null;
        return result;
    }

    public static <V> @NotNull IList<V> replacingAll(@NotNull IList<V> list,
                                                     int index,
                                                     int count,
                                                     @NotNull Iterable<? extends V> values,
                                                     @NotNull ListBuilder<V> builder)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        int size = list.size();
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        if (count < 0) {
            throw new IndexOutOfBoundsException("Invalid count: " + count);
        }

        if (list.isEmpty()) {
            builder.addAll(values);
            return builder.values();
        }

        FindVisitor<V,IList<V>> v = ListImplSupport.createReplaceElementsVisitor(size-1, index, count, values, builder);
        IList<V> result = list.find(v);
        assert result != null;
        return result;
    }

    public static <V> @NotNull IList<V> removing(@NotNull IList<V> list, @NotNull Object value)
    {
        return list.mapFilter(ListImplSupport.createRemoveElementMapper(value));
    }

    public static <V> @NotNull IList<V> removing(@NotNull IList<V> list,
                                                 int index,
                                                 int deleteCount,
                                                 @NotNull ListBuilder<V> builder)
      throws IndexOutOfBoundsException
    {
        if (deleteCount == 0) {
            return list;
        }

        int size = list.size();
        if (index < 0 || index >= size || deleteCount < 0) {
            throw new IndexOutOfBoundsException();
        }

        int availableToRemove = size - index;
        if (deleteCount > availableToRemove) {
            deleteCount = availableToRemove;
        }

        int newSize = size - deleteCount;
        if (newSize == 0) {
            return IList.empty();
        }

        FindVisitor<V, IList<V>> v = ListImplSupport.createRemoveElementsVisitor(index, deleteCount, size-1, builder);
        IList<V> result = list.find(v);
        return result != null ? result : IList.empty();
    }

    public static <V,R> @NotNull IList<R> map(@NotNull IList<V> list,
                                              @NotNull Function<@NotNull V,@NotNull R> mapper,
                                              @NotNull ListBuilder<R> builder)
    {
        list.visit(e -> builder.add(mapper.apply(e)));
        return builder.values();
    }

    public static <V,R> @NotNull IList<R> mapFilter(@NotNull IList<V> list,
                                                    @NotNull Function<@NotNull V,@Nullable R> mapper,
                                                    @NotNull ListBuilder<R> builder)
    {
        Visitor<V> v = ListImplSupport.createMapFilterVisitor(builder, mapper);
        list.visit(v);
        return builder.values();
    }
}
