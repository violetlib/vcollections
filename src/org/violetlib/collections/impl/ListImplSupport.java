/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.violetlib.collections.FindVisitor;
import org.violetlib.collections.IList;
import org.violetlib.collections.IndexedFindVisitor;
import org.violetlib.collections.IndexedVisitor;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.Visitor;
import org.violetlib.types.Option;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**
  Support for implementing IList operations using traversal.
*/

public final @NoInstances class ListImplSupport
{
    private ListImplSupport()
    {
        throw new AssertionError("ListImplSupport may not be instantiated");
    }

    /**
      Wrap an indexed visitor to use as a plain visitor. The indexed visitor receives indexes in increasing order.
    */

    public static <V> @NotNull Visitor<V> fromIndexedVisitor(@NotNull IndexedVisitor<V> v)
    {
        return new CountingVisitor<>(v);
    }

    /**
      Wrap an indexed visitor to use as a plain visitor. The indexed visitor receives indexes in increasing order.
    */

    public static <V> @NotNull Visitor<V> fromIndexedVisitor(@NotNull IndexedVisitor<V> v, int firstIndex)
    {
        return new CountingVisitor<>(v, firstIndex);
    }

    /**
      Wrap an indexed visitor to use as a plain visitor. The indexed visitor receives indexes in decreasing order.
    */

    public static <V> @NotNull Visitor<V> fromIndexedVisitorReversed(@NotNull IndexedVisitor<V> v, int firstIndex)
    {
        return new ReverseCountingVisitor<>(v, firstIndex);
    }

    /**
      Wrap an indexed find visitor to use as a plain find visitor. The indexed visitor receives indexes in increasing
      order.
    */

    public static <V,R> @NotNull FindVisitor<V,R> fromIndexedFindVisitor(@NotNull IndexedFindVisitor<V,R> v)
    {
        return new CountingFindVisitor<>(v);
    }

    /**
      Wrap an indexed find visitor to use as a plain find visitor. The indexed visitor receives indexes in increasing
      order.
    */

    public static <V,R> @NotNull FindVisitor<V,R>
    fromIndexedFindVisitor(@NotNull IndexedFindVisitor<V,R> v, int firstIndex)
    {
        return new CountingFindVisitor<>(v, firstIndex);
    }

    /**
      Wrap an indexed find visitor to use as a plain find visitor. The indexed visitor receives indexes in decreasing
      order.
    */

    public static <V,R> @NotNull FindVisitor<V,R>
    fromIndexedFindVisitorReverse(@NotNull IndexedFindVisitor<V,R> v, int firstIndex)
    {
        return new ReverseCountingFindVisitor<>(v, firstIndex);
    }

    /**
      Wrap a simple visitor to use as a find visitor that never returns a non-null value (no early termination).
    */

    public static <V> @NotNull FindVisitor<V,Boolean> toFindVisitor(@NotNull Visitor <V> v)
    {
        return new TrivialFindVisitor<>(v);
    }

    /**
      Create a visitor that implements getting a list element for a specified index.
    */

    public static <V> @NotNull FindVisitor<V,V> createGetVisitor(int index)
    {
        return new GetVisitor<>(index);
    }

    /**
      Create a visitor that implements getting a subrange of list elements.

      @param startIndex The index of the first element of the source element to include in the subrange.
      @param count The maximum number of elements to include in the subrange.
      @param builder The builder that creates the subrange list.
    */

    public static <V> @NotNull FindVisitor<V,IList<V>>
    createSubrangeVisitor(int startIndex, int count, @NotNull ListBuilder<V> builder)
    {
        return new SubrangeVisitor<>(startIndex, count, builder);
    }

    /**
      Create a visitor that implements getting a subrange of list elements in reverse order.

      @param listSize The number of elements in the source list.
      @param startIndex The index of the first element of the source element to include in the subrange.
      @param count The maximum number of elements to include in the subrange.
      @param builder The builder that creates the subrange list.
    */

    public static <V> @NotNull FindVisitor<V,IList<V>>
    createReverseSubrangeVisitor(int listSize, int startIndex, int count, @NotNull ListBuilder<V> builder)
    {
        return new ReverseSubrangeVisitor<>(listSize, startIndex, count, builder);
    }

    /**
      Create a visitor that implements visiting a range of elements supplied in increasing order of index.

      @param start The index of the first element in the subrange.
      @param end The index of the last elmeent in the subrange.
      @param visitor The visitor to be invoked on the designated elements of the source list.
    */

    public static <V> @NotNull FindVisitor<V,Boolean> createRangeVisitor(int start, int end,
                                                                         @NotNull Visitor<V> visitor)
    {
        return new RangeVisitor<>(0, start, end, visitor);
    }

    /**
      Create a visitor that implements visiting a range of elements supplied in decreasing order of index.

      @param lastIndex The index of the last element of the source list (the source list size less one).
      @param start The index of the first element of the subrange.
      @param end The index of the last elmeent of the subrange.
      @param visitor The visitor to be invoked on the designated elements of the source list.
    */

    public static <V> @NotNull FindVisitor<V,Boolean> createReverseRangeVisitor(int lastIndex, int start, int end,
                                                                                @NotNull Visitor<V> visitor)
    {
        return new ReverseRangeVisitor<>(lastIndex, start, end, visitor);
    }

    /**
      Create a visitor that invokes a find visitor on a range of elements supplied in increasing order of index.

      @param start The index of the first element of the subrange.
      @param end The index of the last elmeent of the subrange.
      @param visitor The visitor to be invoked on the designated elements of the source list. After the designated
      elements have been supplied, the visitor is invoked with the special value {@link #TERMINATION}.
    */

    public static <V,R> @NotNull FindVisitor<V,Object> createRangeFindVisitor(int start, int end,
                                                                              @NotNull FindVisitor<V,R> visitor)

    {
        return new RangeFindVisitor<>(0, start, end, visitor);
    }

    /**
      Create a visitor that invokes a find visitor on a range of elements supplied in decreasing order of index.

      @param lastIndex The index of the last element of the source list (the source list size less one).
      @param start The index of the first element of the subrange.
      @param end The index of the last elmeent of the subrange.
      @param visitor The visitor to be invoked on the designated elements of the source list. After the designated
      elements have been supplied, the visitor is invoked with the special value {@link #TERMINATION}.
    */

    public static <V,R> @NotNull FindVisitor<V,Object> createReverseRangeFindVisitor(int lastIndex, int start, int end,
                                                                                     @NotNull FindVisitor<V,R> visitor)
    {
        return new ReverseRangeFindVisitor<>(lastIndex, start, end, visitor);
    }

    /**
      Create a visitor that implements finding an element and returning its index.

      @param target The element value to find.
    */

    public static <V> @NotNull FindVisitor<V,Integer> createIndexOfVisitor(@NotNull Object target)
    {
        return new IndexOfVisitor<>(target);
    }

    /**
      Create a visitor that inserts element into a list.

      @param lastIndex The index of the last element of the source list (the list size less one).
      @param targetIndex The index where the first inserted element will appear in the new list.
      @param values The values to insert.
      @param builder The builder used to construct the new list.
    */

    public static <V> @NotNull FindVisitor<V,IList<V>> createInsertElementsVisitor(int lastIndex,
                                                                                   int targetIndex,
                                                                                   @NotNull Iterable<? extends V> values,
                                                                                   @NotNull ListBuilder<V> builder)
    {
        return new InsertElementsVisitor<>(lastIndex, targetIndex, values, builder);
    }

    /**
      Create a visitor that implements replacing a list element.

      @param source The source list.
      @param targetIndex The index of the element in the source list to replace.
      @param replacementValue The value that replaces the target element.
      @param builder The builder used to construct the new list.
    */

    public static <V> @NotNull FindVisitor<V,IList<V>> createReplaceElementVisitor(@NotNull IList<V> source,
                                                                                   int targetIndex,
                                                                                   @NotNull V replacementValue,
                                                                                   @NotNull ListBuilder<V> builder)
    {
        return new ReplaceElementVisitor<>(source, targetIndex, replacementValue, builder);
    }

    /**
      Create a visitor that implements replacing elements in a list.

      @param lastIndex The index of the last element of the source list (the list size less one).
      @param targetIndex The index of the first element of the source list to replace.
      @param removeCount The number of elements of the source list to replace.
      @param values The values to replace the designated elements of the source list in the new list.
      @param builder The builder used to create the new list.
    */

    public static <V> @NotNull FindVisitor<V,IList<V>> createReplaceElementsVisitor(int lastIndex,
                                                                                    int targetIndex,
                                                                                    int removeCount,
                                                                                    @NotNull Iterable<? extends V> values,
                                                                                    @NotNull ListBuilder<V> builder)
    {
        return new ReplaceElementsVisitor<>(lastIndex, targetIndex, removeCount, values, builder);
    }

    /**
      Return a function that maps the designated value to null; any other value is mapped to itself.
      @param target The value to be mapped to null.
    */

    public static <V> @NotNull Function<@NotNull V,@Nullable V> createRemoveElementMapper(@NotNull Object target)
    {
        return new RemoveElementMapper<>(target);
    }

    /**
      Create a visitor that implements removing elements from a list.

      @param startIndex The index of the first element of the source list to remove.
      @param removeCount The number of elements of the source list to remove.
      @param lastIndex The index of the last element of the source list (the list size less one).
      @param builder The builder used to create the new list.
    */

    public static <V> @NotNull FindVisitor<V,IList<V>> createRemoveElementsVisitor(int startIndex,
                                                                                   int removeCount,
                                                                                   int lastIndex,
                                                                                   @NotNull ListBuilder<V> builder)
    {
        return new RemoveElementsVisitor<>(startIndex, removeCount, lastIndex, builder);
    }

    /**
      Create a visitor that maps and filters list elements.
      @param builder The builder used to create the new list.
      @param mapper A function that maps elements from the source list to the corresponding element in the new list, or
      to null to exclude that source element.
    */

    public static <V,R> @NotNull Visitor<V> createMapFilterVisitor(@NotNull ListBuilder<R> builder,
                                                                   @NotNull Function<@NotNull V,@Nullable R> mapper)
    {
        return new MapFilterVisitor<>(builder, mapper);
    }

    public static <V> @NotNull Visitor<V> createToJavaListVisitor(@NotNull List<V> result)
    {
        return new ToJavaListVisitor<>(result);
    }

    public static <V> @NotNull Visitor<V> createToJavaSetVisitor(@NotNull Set<V> result)
    {
        return new ToJavaSetVisitor<>(result);
    }

    public static <V> @NotNull Visitor<V> createToJavaArrayVisitor(Object @NotNull [] array)
    {
        return new ToJavaArrayVisitor<>(array);
    }

    public static <V> @NotNull Visitor<V> createToReverseJavaArrayVisitor(Object @NotNull [] array)
    {
        return new ToReverseJavaArrayVisitor<>(array);
    }

    public static final int RECURSION_LIMIT = 300;

    public static final @NotNull Option TERMINATION = Option.named("Termination");

    /**
      Wrap an indexed visitor to use as a plain visitor. The indexed visitor receives indexes in increasing order.
    */

    private static class CountingVisitor<V>
      implements Visitor<V>
    {
        private final @NotNull IndexedVisitor<V> base;
        private int index;

        public CountingVisitor(@NotNull IndexedVisitor<V> base)
        {
            this.base = base;
        }

        public CountingVisitor(@NotNull IndexedVisitor<V> base, int firstIndex)
        {
            this.base = base;
            this.index = firstIndex;
        }

        @Override
        public void visit(@NotNull V element)
        {
            base.visit(index++, element);
        }
    }

    /**
      Wrap an indexed visitor to use as a plain visitor. The indexed visitor receives indexes in decreasing order.
    */

    private static class ReverseCountingVisitor<V>
      implements Visitor<V>
    {
        private final @NotNull IndexedVisitor<V> base;
        private int index;

        public ReverseCountingVisitor(@NotNull IndexedVisitor<V> base, int firstIndex)
        {
            this.base = base;
            this.index = firstIndex;
        }

        @Override
        public void visit(@NotNull V element)
        {
            base.visit(index--, element);
        }
    }

    /**
      Wrap an indexed find visitor to use as a plain find visitor. The indexed visitor receives indexes in increasing
      order.
    */

    private static class CountingFindVisitor<V,R>
      implements FindVisitor<V,R>
    {
        private final @NotNull IndexedFindVisitor<V,R> base;
        private int index;

        public CountingFindVisitor(@NotNull IndexedFindVisitor<V,R> base)
        {
            this.base = base;
        }

        public CountingFindVisitor(@NotNull IndexedFindVisitor<V,R> base, int firstIndex)
        {
            this.base = base;
            this.index = firstIndex;
        }

        @Override
        public @Nullable R visit(@NotNull V element)
        {
            return base.visit(index++, element);
        }
    }

    /**
      Wrap an indexed find visitor to use as a plain find visitor. The indexed visitor receives indexes in decreasing
      order.
    */

    private static class ReverseCountingFindVisitor<V,R>
      implements FindVisitor<V,R>
    {
        private final @NotNull IndexedFindVisitor<V,R> base;
        private int index;

        public ReverseCountingFindVisitor(@NotNull IndexedFindVisitor<V,R> base, int firstIndex)
        {
            this.base = base;
            this.index = firstIndex;
        }

        @Override
        public @Nullable R visit(@NotNull V element)
        {
            return base.visit(index--, element);
        }
    }

    /**
      Wrap a simple visitor to use as a find visitor that never returns a non-null value (no early termination).
    */

    private static class TrivialFindVisitor<V>
      implements FindVisitor<V,Boolean>
    {
        private final @NotNull Visitor<V> base;

        public TrivialFindVisitor(@NotNull Visitor<V> base)
        {
            this.base = base;
        }

        @Override
        public @Nullable Boolean visit(@NotNull V element)
        {
            base.visit(element);
            return null;
        }
    }

    /**
      A visitor that implements getting a list element for a specified index.
    */

    private static class GetVisitor<V>
      implements FindVisitor<V,V>
    {
        private final int targetIndex;
        private int index;

        public GetVisitor(int targetIndex)
        {
            this.targetIndex = targetIndex;
        }

        @Override
        public @Nullable V visit(@NotNull V element)
        {
            return index++ == targetIndex ? element : null;
        }
    }

    /**
      A visitor that implements getting a subrange of list elements.
    */

    private static class SubrangeVisitor<V>
      implements FindVisitor<V,IList<V>>
    {
        private final int startIndex;
        private int remainingCount;
        private final @NotNull ListBuilder<V> builder;
        private int index;

        public SubrangeVisitor(int startIndex, int count, @NotNull ListBuilder<V> builder)
        {
            this.startIndex = startIndex;
            this.remainingCount = count;
            this.builder = builder;
        }

        @Override
        public @Nullable IList<V> visit(@NotNull V element)
        {
            if (index >= startIndex) {
                builder.add(element);
                if (--remainingCount <= 0) {
                    return builder.values();
                }
            }

            ++index;
            return null;
        }
    }

    /**
      A visitor that implements getting a subrange of list elements assuming reverse iteration. The builder receives
      elements in reverse order.
    */

    private static class ReverseSubrangeVisitor<V>
      implements FindVisitor<V,IList<V>>
    {
        private final int endIndex;
        private int remainingCount;
        private final @NotNull ListBuilder<V> builder;
        private int index;

        public ReverseSubrangeVisitor(int listSize, int startIndex, int count, @NotNull ListBuilder<V> builder)
        {
            this.endIndex = startIndex + count;
            this.remainingCount = count;
            this.builder = builder;
            index = listSize - 1;
        }

        @Override
        public @Nullable IList<V> visit(@NotNull V element)
        {
            if (index < endIndex) {
                builder.add(element);
                if (--remainingCount <= 0) {
                    return builder.values();
                }
            }

            --index;
            return null;
        }
    }

    /**
      A visitor that implements finding an element and returning its index.
    */

    private static class IndexOfVisitor<V>
      implements FindVisitor<V,Integer>
    {
        private final @NotNull Object target;
        private int index;

        public IndexOfVisitor(@NotNull Object target)
        {
            this.target = target;
        }

        @Override
        public @Nullable Integer visit(@NotNull V element)
        {
            Integer result = target.equals(element) ? index : null;
            index++;
            return result;
        }
    }

    /**
      A visitor that implements replacing a list element.
    */

    public static class ReplaceElementVisitor<V>
      implements FindVisitor<V,IList<V>>
    {
        private final @NotNull IList<V> source;
        private int index;
        private final int lastIndex;
        private final int targetIndex;
        private final @NotNull V replacementValue;
        private final @NotNull ListBuilder<V> builder;

        public ReplaceElementVisitor(@NotNull IList<V> source,
                                     int targetIndex,
                                     @NotNull V replacementValue,
                                     @NotNull ListBuilder<V> builder)
        {
            this.source = source;
            this.lastIndex = source.size() - 1;
            this.targetIndex = targetIndex;
            this.replacementValue = replacementValue;
            this.builder = builder;
        }

        @Override
        public @Nullable IList<V> visit(@NotNull V element)
        {
            if (index == targetIndex) {
                if (element.equals(replacementValue)) {
                    // no change
                    return source;
                }
                element = replacementValue;
            }
            builder.add(element);
            if (index == lastIndex) {
                return builder.values();
            }
            index++;
            return null;
        }
    }

    /**
      A visitor that implements inserting elements in a list.
    */

    private static class InsertElementsVisitor<V>
      implements FindVisitor<V,IList<V>>
    {
        private int index;
        private final int lastIndex;
        private final int targetIndex;
        private final @NotNull Iterable<? extends V> values;
        private @NotNull ListBuilder<V> builder;

        public InsertElementsVisitor(int lastIndex,
                                     int targetIndex,
                                     @NotNull Iterable<? extends V> values,
                                     @NotNull ListBuilder<V> builder)
        {
            this.lastIndex = lastIndex;
            this.targetIndex = targetIndex;
            this.values = values;
            this.builder = builder;
        }

        @Override
        public @Nullable IList<V> visit(@NotNull V element)
          throws IllegalArgumentException
        {
            if (index == targetIndex) {
                for (V value : values) {
                    if (value == null) {
                        throw new IllegalArgumentException("Null elements are not permitted");
                    }
                    builder.add(value);
                }
            }
            builder.add(element);
            if (index == lastIndex) {
                return builder.values();
            }
            index++;
            return null;
        }
    }

    /**
      A visitor that implements replacing elements in a list.
    */

    private static class ReplaceElementsVisitor<V>
      implements FindVisitor<V,IList<V>>
    {
        private int index;
        private final int lastIndex;
        private int targetIndex;
        private int removeCount;
        private final @NotNull Iterable<? extends V> values;
        private @NotNull ListBuilder<V> builder;
        private boolean hasInserted;

        public ReplaceElementsVisitor(int lastIndex,
                                      int targetIndex,
                                      int removeCount,
                                      @NotNull Iterable<? extends V> values,
                                      @NotNull ListBuilder<V> builder)
        {
            this.lastIndex = lastIndex;
            this.targetIndex = targetIndex;
            this.removeCount = Math.min(removeCount, lastIndex + 1 - targetIndex);
            this.values = values;
            this.builder = builder;
        }

        @Override
        public @Nullable IList<V> visit(@NotNull V element)
          throws IllegalArgumentException
        {
            if (index >= targetIndex) {
                if (removeCount == 0) {
                    for (V value : values) {
                        if (value == null) {
                            throw new IllegalArgumentException("Null elements are not permitted");
                        }
                        builder.add(value);
                    }
                    hasInserted = true;
                    targetIndex = Integer.MAX_VALUE;
                    builder.add(element);
                } else {
                    removeCount--;
                }
            } else {
                builder.add(element);
            }
            if (index == lastIndex) {
                if (!hasInserted) {
                    builder.addAll(values);
                }
                return builder.values();
            }
            index++;
            return null;
        }
    }

    /**
      Map a designated value to null.
    */

    private static class RemoveElementMapper<V>
      implements Function<@NotNull V,@Nullable V>
    {
        private final @NotNull Object target;

        public RemoveElementMapper(@NotNull Object target)
        {
            this.target = target;
        }

        @Override
        public @Nullable V apply(@NotNull V v)
        {
            return target.equals(v) ? null : v;
        }
    }

    /**
      A visitor that implements removing elements from a list.
    */

    private static class RemoveElementsVisitor<V>
      implements FindVisitor<V,IList<V>>
    {
        private final int startIndex;
        private int index;
        private int remainingCount;
        private final int lastIndex;
        private @NotNull ListBuilder<V> builder;

        public RemoveElementsVisitor(int startIndex, int removeCount, int lastIndex, @NotNull ListBuilder<V> builder)
        {
            this.startIndex = startIndex;
            this.remainingCount = removeCount;
            this.lastIndex = lastIndex;
            this.builder = builder;
        }

        @Override
        public @Nullable IList<V> visit(@NotNull V element)
        {
            if (index >= startIndex && remainingCount > 0) {
                remainingCount--;
            } else {
                builder.add(element);
            }
            if (index == lastIndex) {
                return builder.values();
            }
            ++index;
            return null;
        }
    }

    private static class MapFilterVisitor<V,R>
      implements Visitor<V>
    {
        private final @NotNull Function<@NotNull V,@Nullable R> mapper;
        private final @NotNull ListBuilder<R> builder;

        public MapFilterVisitor(@NotNull ListBuilder<R> builder, @NotNull Function<@NotNull V,@Nullable R> mapper)
        {
            this.mapper = mapper;
            this.builder = builder;
        }

        @Override
        public void visit(@NotNull V element)
        {
            R replacement = mapper.apply(element);
            if (replacement != null) {
                builder.add(replacement);
            }
        }
    }

    private static class ToJavaListVisitor<V>
      implements Visitor<V>
    {
        private final @NotNull List<V> result;

        public ToJavaListVisitor(@NotNull List<V> result)
        {
            this.result = result;
        }

        @Override
        public void visit(@NotNull V element)
        {
            result.add(element);
        }
    }

    private static class ToJavaSetVisitor<V>
      implements Visitor<V>
    {
        private final @NotNull Set<V> result;

        public ToJavaSetVisitor(@NotNull Set<V> result)
        {
            this.result = result;
        }

        @Override
        public void visit(@NotNull V element)
        {
            result.add(element);
        }
    }

    private static class ToJavaArrayVisitor<V>
      implements Visitor<V>
    {
        private final Object @NotNull [] array;
        private int index;

        public ToJavaArrayVisitor(Object @NotNull [] array)
        {
            this.array = array;
        }

        @Override
        public void visit(@NotNull V element)
        {
            array[index++] = element;
        }
    }

    private static class ToReverseJavaArrayVisitor<V>
      implements Visitor<V>
    {
        private final Object @NotNull [] array;
        private int index;

        public ToReverseJavaArrayVisitor(Object @NotNull [] array)
        {
            this.array = array;
            this.index = array.length - 1;
        }

        @Override
        public void visit(@NotNull V element)
        {
            array[index--] = element;
        }
    }

    /**
      A visitor that implements visiting a range of elements supplied in increasing order of index.
    */

    private static class RangeVisitor<V>
      implements FindVisitor<V,Boolean>
    {
        private final int start;
        private final int end;
        private final @NotNull Visitor<V> visitor;
        private int index;

        public RangeVisitor(int firstIndex, int start, int end, @NotNull Visitor<V> visitor)
        {
            this.start = start;
            this.end = end;
            this.visitor = visitor;
            this.index = firstIndex;
        }

        @Override
        public @Nullable Boolean visit(@NotNull V element)
        {
            if (index >= end) {
                return Boolean.TRUE;
            }
            if (index >= start) {
                visitor.visit(element);
            }
            ++index;
            return index >= end;
        }
    }

    /**
      A visitor that implements visiting a range of elements supplied in decreasing order of index.
    */

    private static class ReverseRangeVisitor<V>
      implements FindVisitor<V,Boolean>
    {
        private final int start;
        private final int end;
        private final @NotNull Visitor<V> visitor;
        private int index;

        public ReverseRangeVisitor(int firstIndex, int start, int end, @NotNull Visitor<V> visitor)
        {
            this.start = start;
            this.end = end;
            this.visitor = visitor;
            this.index = firstIndex;
        }

        @Override
        public @Nullable Boolean visit(@NotNull V element)
        {
            if (index < start) {
                return Boolean.TRUE;
            }
            if (index < end) {
                visitor.visit(element);
            }
            --index;
            return index < start;
        }
    }

    /**
      A visitor that invokes a find visitor on a range of elements supplied in increasing order of index.
    */

    private static class RangeFindVisitor<V,R>
      implements FindVisitor<V,Object>
    {
        private final int start;
        private final int end;
        private final @NotNull FindVisitor<V,R> base;
        private int index;

        public RangeFindVisitor(int firstIndex, int start, int end, @NotNull FindVisitor<V,R> base)
        {
            this.start = start;
            this.end = end;
            this.base = base;
            this.index = firstIndex;
        }

        @Override
        public @Nullable Object visit(@NotNull V element)
        {
            if (index >= end) {
                return TERMINATION;
            }

            if (index >= start) {
                R result = base.visit(element);
                if (result != null) {
                    return result;
                }
            }

            index++;

            return index >= end;
        }
    }

    /**
      A visitor that invokes a find visitor on a range of elements supplied in decreasing order of index.
    */

    private static class ReverseRangeFindVisitor<V,R>
      implements FindVisitor<V,Object>
    {
        private final int start;
        private final int end;
        private final @NotNull FindVisitor<V,R> base;
        private int index;

        public ReverseRangeFindVisitor(int firstIndex, int start, int end, @NotNull FindVisitor<V,R> base)
        {
            this.start = start;
            this.end = end;
            this.base = base;
            this.index = firstIndex;
        }

        @Override
        public @Nullable Object visit(@NotNull V element)
        {
            if (index < start) {
                return TERMINATION;
            }

            if (index < end) {
                R result = base.visit(element);
                if (result != null) {
                    return result;
                }
            }

            --index;
            return index < start;
        }
    }
}
