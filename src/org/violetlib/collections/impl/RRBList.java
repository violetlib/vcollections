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
import org.violetlib.annotations.Immutable;
import org.violetlib.collections.*;
import org.violetlib.collections.impl.rrblist.FocusableRRBVector;
import org.violetlib.collections.impl.rrblist.MutableRRBVector;
import org.violetlib.collections.impl.rrblist.RRBVector;
import org.violetlib.collections.util.CollectorImpl;
import org.violetlib.util.Extensions;
import org.violetlib.types.UndefinedValueError;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static org.violetlib.collections.impl.rrblist.RRBVectorConstants.*;

/**
  An immutable list implemented using a relaxed radix balanced tree.
*/

public final @Immutable class RRBList<V>
  extends RRBVector<V>
  implements IList<V>, HasReverseIterator<V>
{
    public static <V> @NotNull ListBuilder<V> builder()
    {
        return RRBListBuilder.create();
    }

    public static <V> @NotNull Collector<V,ListBuilder<V>,IList<V>> collector()
    {
        return CollectorImpl.<V,ListBuilder<V>,IList<V>>create(
          RRBListBuilder::<V>create,
          ListBuilder::add,
          RRBList::append,
          ListBuilder::values,
          CH_NOID);
    }

    private static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

    private static <V> ListBuilder<V> append(@NotNull ListBuilder<V> b1, @NotNull ListBuilder<V> b2)
    {
        b1.addAll(b2.values());
        return b1;
    }

    /**
      Return an empty immutable list. Operations that extend this list will favor creating instances of this class.
    */

    public static <V> @NotNull RRBList<V> empty()
    {
        return (RRBList<V>) EMPTY;
    }

    /**
      Create an immutable list containing one element.
      @param element The element.
    */

    public static <V> @NotNull RRBList<V> singleton(@NotNull V element)
    {
        return new RRBList<>(true, element);
    }

    /**
      Create an immutable list with a specified size.
      @param size The number of elements.
      @param items The source for the element values. This supplier is invoked once per element in increasing order.
      It must not return null.
    */

    public static <V> @NotNull RRBList<V> create(int size, @NotNull Supplier<V> items)
    {
        return new RRBList<>(size, items);
    }

    /**
      Return a list containing the specified elements.
    */

    @SafeVarargs
    public static <V> @NotNull IList<V> of(@NotNull V... elements)
    {
        return createWithArray(elements);
    }

    /**
      Return a list containing the specified elements.
      @param elements An iterator providing the elements.
      @throws IllegalArgumentException if the iterator returns a null element.
    */

    public static <V> @NotNull IList<V> createWithElements(@NotNull Iterable<? extends V> elements)
      throws IllegalArgumentException
    {
        int size = CollectionsUtils.getKnownSize(elements);
        if (size >= 0) {
            if (size == 0) {
                return empty();
            }

            if (size == 1) {
                V value = elements.iterator().next();
                if (value == null) {
                    throw new IllegalArgumentException("Null values are not permitted");
                }
                return singleton(value);
            }

            return internalCreateWithElements(size, elements);
        }

        ListBuilder<V> b = builder();
        for (V element : elements) {
            b.add(element);
        }
        return b.values();
    }

    /**
      Create an RRB list from a source with a known number of elements.

      @param size The number of elements that the iterator will return.
      @param elements The iterator that provides the elements.
      @throws IllegalArgumentException if the iterator returns a null element.
    */

    private static <V> @NotNull RRBList<V> internalCreateWithElements(int size, @NotNull Iterable<? extends V> elements)
      throws IllegalArgumentException
    {
        if (size == 0) {
            return RRBList.empty();
        }

        RRBList<V> result = Extensions.getExtension(elements, RRBList.class);
        if (result != null) {
            return result;
        }

        SimpleList<V> simple = Extensions.getExtension(elements, SimpleList.class);
        if (simple != null) {
            return create(size, ArraySupplier.create(simple.asArray()));
        }

        Supplier<V> arraySupplier = ArraySupplier.create(elements);
        if (arraySupplier != null) {
            return create(size, ArraySupplier.create(elements));
        }

        return create(size, (Supplier<V>) IteratorSupplier.create(elements.iterator()));
    }

    public static <V> @NotNull IList<V> createWithArray(@NotNull Object @NotNull [] elements)
    {
        if (elements.length == 0) {
            return empty();
        }
        if (elements.length == 1) {
            V value = (V) elements[0];
            return singleton(value);
        }
        return create(elements.length, ArraySupplier.create(elements));
    }

    private static final @NotNull IList<Object> EMPTY = new RRBList<>();

    private RRBList()
    {
        super();
    }

    private RRBList(int size, @NotNull Supplier<V> items)
    {
        super(size, items);
    }

    private RRBList(@NotNull RRBVector<V> vector)
    {
        super(vector);
    }

    private RRBList(boolean fake, @NotNull V element)
    {
        super(fake, element);
    }

    /* package private */ RRBList(int length, Object @NotNull [] root, int depth)
    {
        super(length, root, depth);
    }

    @Override
    public boolean isEmpty()
    {
        return endIndex == 0;
    }

    @Override
    public int size()
    {
        return endIndex;
    }

    @Override
    public @NotNull V get(int index)
      throws IndexOutOfBoundsException
    {
        if (index >= 0 && index < endIndex) {
            return getElementFromRoot(index);
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public @Nullable V getOptional(int index)
    {
        if (index >= 0 && index < endIndex) {
            return getElementFromRoot(index);
        }
        return null;
    }

    @Override
    public @NotNull V first()
      throws NoSuchElementException
    {
        if (endIndex > 0) {
            return getElementFromRoot(0);
        }
        throw new NoSuchElementException();
    }

    @Override
    public @Nullable V optionalFirst()
    {
        return endIndex == 0 ? null : getElementFromRoot(0);
    }

    @Override
    public @NotNull V last()
      throws NoSuchElementException
    {
        if (endIndex > 0) {
            return getElementFromRoot(endIndex - 1);
        }
        throw new NoSuchElementException();
    }

    @Override
    public @Nullable V optionalLast()
    {
        return endIndex == 0 ? null : getElementFromRoot(endIndex - 1);
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
    public @NotNull IIterator<V> iterator()
    {
        if (endIndex == 0) {
            return EmptyIIterator.get();
        }

        return RRBListIterator.create(0, endIndex, this);
    }

    @Override
    public @NotNull IIterator<V> reverseIterator()
    {
        if (endIndex == 0) {
            return EmptyIIterator.get();
        }

        return RRBListReverseIterator.create(0, endIndex, MutableRRBVector.create(this));
    }

    @Override
    public int indexOf(@NotNull Object element)
    {
        Integer result = find(ListImplSupport.createIndexOfVisitor(element));
        return result != null ? result : -1;
    }

    @Override
    public boolean contains(@NotNull Object target)
    {
        Integer result = find(ListImplSupport.createIndexOfVisitor(target));
        return result != null ;
    }

    @Override
    public void visit(@NotNull Visitor<V> visitor)
    {
        //noinspection ResultOfMethodCallIgnored
        find(ListImplSupport.toFindVisitor(visitor));
    }

    @Override
    public <R> @Nullable R find(@NotNull FindVisitor<V,R> visitor)
    {
        if (endIndex == 0) {
            return null;
        }
        int firstIndexInBlock = 0;
        return internalFind(0, endIndex, visitor, root, depth, firstIndexInBlock);
    }

    @Override
    public @NotNull IList<V> getElements(int index, int count)
      throws IndexOutOfBoundsException
    {
        if (count < 0) {
            throw new IndexOutOfBoundsException("Invalid count: " + count);
        }
        if (index < 0 || index + count > endIndex && count > 0) {
            throw new IndexOutOfBoundsException("Invalid range: " + index + " " + count);
        }

        if (count == 0) {
            return empty();
        }
        if (count == endIndex) {
            return this;
        }
        if (count == 1) {
            V element = get(index);
            return singleton(element);
        }

        if (index == 0) {
            MutableRRBVector<V> result = MutableRRBVector.create(this);
            result.retainPrefix(count);
            return toIList(result);
        }

        if (index + count == endIndex) {
            MutableRRBVector<V> result = MutableRRBVector.create(this);
            result.removePrefix(index);
            return toIList(result);
        }

        MutableRRBVector<V> result = MutableRRBVector.create(this);
        // TBD: does the order matter?
        result.retainPrefix(index + count);
        result.removePrefix(index);
        return toIList(result);
    }

    @Override
    public @NotNull IList<V> onSlice(int start, int end)
      throws IndexOutOfBoundsException
    {
        return new MyListSliceImpl(start, end);
    }

    @Override
    public @NotNull IList<V> replacing(int index, @NotNull V value)
      throws IndexOutOfBoundsException
    {
        MutableRRBVector<V> result = MutableRRBVector.create(this);
        result.replace(index, value);
        return toIList(result);
    }

    @Override
    public @NotNull IList<V> replacingAll(int index, int count, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        int size = size();
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        if (count < 0) {
            throw new IndexOutOfBoundsException("Invalid count: " + count);
        }

        if (size == 0) {
            return RRBList.createWithElements(values);
        }

        int availableToRemove = size - index;
        if (count > availableToRemove) {
            count = availableToRemove;
        }

        int tailSize = size - (index + count);
        int valueCount = CollectionsUtils.getKnownSize(values);

        if (count == 0 && valueCount == 0) {
            return this;
        }

        MutableRRBVector<V> result;
        if (index == 0) {
            result = MutableRRBVector.create();
        } else {
            result = MutableRRBVector.create(this);
            result.retainPrefix(index);
        }
        appendValues(result, values, valueCount);
        if (tailSize > 0) {
            result.appendAll(getTail(tailSize));
        }
        return toIList(result);
    }

    @Override
    public @NotNull IList<V> appending(@NotNull V value)
    {
        if (isEmpty()) {
            return singleton(value);
        }
        MutableRRBVector<V> result = MutableRRBVector.create(this);
        result.append(value);
        return toIList(result);
    }

    @Override
    public @NotNull IList<V> prepending(@NotNull V value)
    {
        if (isEmpty()) {
            return singleton(value);
        }
        MutableRRBVector<V> result = MutableRRBVector.create(this);
        result.prepend(value);
        return toIList(result);
    }

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        if (isEmpty()) {
            return createWithElements(values);
        }

        int valueCount = CollectionsUtils.getKnownSize(values);
        if (valueCount == 0) {
            return this;
        }

        MutableRRBVector<V> result = MutableRRBVector.create(this);
        appendValues(result, values, valueCount);
        return toIList(result);
    }

    @Override
    public @NotNull IList<V> removing(@NotNull Object value)
    {
        return ListOperations.removing(this, value);
    }

    @Override
    public @NotNull IList<V> removing(int index, int count)
      throws IndexOutOfBoundsException
    {
        int size = size();
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Invalid removal index: " + index);
        }
        if (count < 0) {
            throw new IndexOutOfBoundsException("Invalid removal count: " + count);
        }
        int availableToRemove = size - index;
        if (count > availableToRemove) {
            count = availableToRemove;
        }
        if (count == 0) {
            return this;
        }
        int tailSize = size - (index + count);
        RRBVector<V> tail = tailSize > 0 ? getTail(tailSize) : null;
        if (index == 0) {
            return tail != null ? new RRBList<>(tail) : RRBList.empty();
        }
        MutableRRBVector<V> result = MutableRRBVector.create(this);
        result.retainPrefix(index);
        if (tail != null) {
            result.appendAll(tail);
        }
        return toIList(result);
    }

    @Override
    public @NotNull <R> IList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        if (endIndex == 0) {
            return RRBList.empty();
        }
        Object[] resultRoot = mapBlock(mapper, root, depth);
        return new RRBList<>(endIndex, resultRoot, depth);
    }

    @Override
    public @NotNull <R> IList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper)
    {
        return ListOperations.mapFilter(this, mapper, builder());
    }

    private void appendValues(@NotNull MutableRRBVector<V> m,
                              @NotNull Iterable<? extends V> values,
                              int count)
    {
        if (count == 0) {
            return;
        }

        if (count > 0) {
            RRBList<? extends V> other = Extensions.getExtension(values, RRBList.class);
            if (other != null) {
                m.appendAll(other);
                return;
            }

            SimpleList<? extends V> simpleList = Extensions.getExtension(values, SimpleList.class);
            if (simpleList != null) {
                m.appendArray(simpleList.asArray());
                return;
            }

            AppendOrientedList<? extends V> aol = Extensions.getExtension(values, AppendOrientedList.class);
            if (aol != null) {
                m.appendArray(aol.toJavaArray(new Object[0]));
                return;
            }
        }

        for (V value : values) {
            if (value == null) {
                throw new IllegalArgumentException("Null values are not permitted");
            }
            m.append(value);
        }
    }

    private @NotNull RRBVector<V> getTail(int count)
    {
        assert count > 0;
        MutableRRBVector<V> result = MutableRRBVector.create(this);
        int index = endIndex - count;
        result.removePrefix(index);
        return result.asBasic();
    }

    @Override
    public <E> @NotNull E[] toJavaArray(@NotNull E[] template)
    {
        E[] a = (E[]) java.lang.reflect.Array.newInstance(template.getClass().getComponentType(), endIndex);
        visit(ListImplSupport.fromIndexedVisitor((i, v) -> a[i] = (E) v));
        return a;
    }

    @Override
    public @NotNull IList<V> reverse()
    {
        IIterator<V> it = RRBListReverseIterator.create(0, endIndex, MutableRRBVector.create(this));
        return create(endIndex, IteratorSupplier.create(it));
    }

    @Override
    public @NotNull IList<V> onReverse()
    {
        return ListReverseImpl.create(this);
    }

    @Override
    public @NotNull IList<V> sort(@NotNull Comparator<? super V> c)
    {
        V[] elements = (V[]) toJavaArray(new Object[0]);
        Arrays.sort(elements, c);
        return createWithArray(elements);
    }

    @Override
    public @NotNull IList<V> optimize(@NotNull ListUsage usage)
    {
        return this;  // TBD
    }

    @Override
    public @NotNull IList<V> optimizeForForwardTraversal()
    {
        return this;  // TBD
    }

    @Override
    public @NotNull IList<V> optimizeForIndexing()
    {
        return this;  // TBD
    }

    private <R> @Nullable R internalFind(int startIndex,
                                         int endIndex,
                                         @NotNull FindVisitor<V,R> visitor,
                                         Object @NotNull [] block,
                                         int level,
                                         int firstIndexInBlock)
    {
        int blockLength = block.length;
        if (level == 1) {
            // A leaf node
            for (int offset = 0; offset < blockLength; offset++) {
                int index = firstIndexInBlock + offset;
                if (index >= endIndex) {
                    return null;
                }
                if (index >= startIndex) {
                    V item = (V) block[offset];
                    R result = visitor.visit(item);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        }
        // A branch node
        int[] sizes = getSizes(level, block);
        if (sizes == null) {
            // Each subtree (except possibly the last) has a fixed size based on level
            int fixedSubtreeSize = getMaximumTreeSize(level - 1);
            int firstIndexInSubtree = firstIndexInBlock;
            for (int offset = 0; offset < blockLength-1; offset++) {
                if (firstIndexInSubtree >= endIndex) {
                    return null;
                }
                int lastIndexInSubtree = firstIndexInSubtree + fixedSubtreeSize - 1;
                if (lastIndexInSubtree >= startIndex) {
                    Object[] subtree = (Object[]) block[offset];
                    R result = internalFind(startIndex, endIndex, visitor, subtree, level - 1, firstIndexInSubtree);
                    if (result != null) {
                        return result;
                    }
                }
                firstIndexInSubtree = lastIndexInSubtree + 1;
            }
            return null;
        } else {
            // The subtrees may have different sizes
            int firstIndexInSubtree = firstIndexInBlock;
            for (int offset = 0; offset < blockLength-1; offset++) {
                if (firstIndexInSubtree >= endIndex) {
                    return null;
                }
                int lastIndexInSubtree = firstIndexInSubtree + getSubtreeSize(sizes, offset) - 1;
                if (lastIndexInSubtree >= startIndex) {
                    Object[] subtree = (Object[]) block[offset];
                    R result = internalFind(startIndex, endIndex, visitor, subtree, level - 1, firstIndexInSubtree);
                    if (result != null) {
                        return result;
                    }
                }
                firstIndexInSubtree = lastIndexInSubtree + 1;
            }
            return null;
        }
    }

    private <R> @Nullable R internalFindReverse(int startIndex,
                                                int endIndex,
                                                @NotNull FindVisitor<V,R> visitor,
                                                Object @NotNull [] block,
                                                int level,
                                                int firstIndexInBlock)
    {
        int blockLength = block.length;
        if (level == 1) {
            // A leaf node
            for (int offset = blockLength - 1; offset >= 0; offset--) {
                int index = firstIndexInBlock + offset;
                if (index < startIndex) {
                    return null;
                }
                if (index < endIndex) {
                    V item = (V) block[offset];
                    R result = visitor.visit(item);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        }
        // A branch node
        int nodeCount = blockLength - 1;
        int[] sizes = getSizes(level, block);
        if (sizes == null) {
            // Each subtree (except possibly the last) has a fixed size based on level
            int fixedSubtreeSize = getMaximumTreeSize(level - 1);
            int lastIndexInSubtree = firstIndexInBlock + nodeCount * fixedSubtreeSize - 1;
            for (int offset = nodeCount - 1; offset >= 0; offset--) {
                if (lastIndexInSubtree < startIndex) {
                    return null;
                }
                int firstIndexInSubtree = lastIndexInSubtree - fixedSubtreeSize + 1;
                if (firstIndexInSubtree < endIndex) {
                    Object[] subtree = (Object[]) block[offset];
                    R result = internalFindReverse(startIndex, endIndex, visitor, subtree, level - 1, firstIndexInSubtree);
                    if (result != null) {
                        return result;
                    }
                }
                lastIndexInSubtree = firstIndexInSubtree - 1;
            }
            return null;
        } else {
            // The subtrees may have different sizes
            int lastIndexInSubtree = firstIndexInBlock + sizes[nodeCount-1] - 1;
            for (int offset = nodeCount - 1; offset >= 0; offset--) {
                if (lastIndexInSubtree < startIndex) {
                    return null;
                }
                int firstIndexInSubtree = lastIndexInSubtree - getSubtreeSize(sizes, offset) + 1;
                if (firstIndexInSubtree < endIndex) {
                    Object[] subtree = (Object[]) block[offset];
                    R result = internalFindReverse(startIndex, endIndex, visitor, subtree, level - 1, firstIndexInSubtree);
                    if (result != null) {
                        return result;
                    }
                }
                lastIndexInSubtree = firstIndexInSubtree - 1;
            }
            return null;
        }
    }

    protected <R> Object @NotNull [] mapBlock(@NotNull Function<@NotNull V,@NotNull R> mapper,
                                              Object @NotNull [] block,
                                              int level)
    {
        int blockLength = block.length;
        Object[] resultBlock = new Object[blockLength];
        if (level == 1) {
            // A leaf node
            for (int offset = 0; offset < blockLength; offset++) {
                V item = (V) block[offset];
                R result = mapper.apply(item);
                if (result == null) {
                    throw UndefinedValueError.create("Mapper must not return null");
                }
                resultBlock[offset] = result;
            }
        } else {
            // A branch node
            int[] sizes = getSizes(level, block);
            for (int offset = 0; offset < blockLength - 1; offset++) {
                Object[] subtree = (Object[]) block[offset];
                resultBlock[offset] = mapBlock(mapper, subtree, level - 1);
            }
            resultBlock[blockLength - 1] = sizes;
        }
        return resultBlock;
    }

    private @NotNull IList<V> toIList(@NotNull FocusableRRBVector<V> vector)
    {
        return new RRBList<>(vector.asBasic());
    }

    private class MyListSliceImpl
      extends ListSliceImpl<V>
    {
        public MyListSliceImpl(int start, int end)
          throws IndexOutOfBoundsException
        {
            super(RRBList.this, start, end);
        }

        @Override
        public @NotNull IIterator<V> reverseIterator()
        {
            return RRBListReverseIterator.create(start, end, MutableRRBVector.create(RRBList.this));
        }
    }
}
