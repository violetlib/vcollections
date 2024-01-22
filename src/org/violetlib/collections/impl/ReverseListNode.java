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
import org.violetlib.collections.FindVisitor;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;
import org.violetlib.collections.Visitor;
import org.violetlib.util.Extensions;

import java.util.NoSuchElementException;
import java.util.function.Function;

/**
  A node in a reverse linked list.
  <p>
  This class avoids the usual recursive method implementations on larger lists which can overflow the stack.
*/

/* package private */ final class ReverseListNode<V>
  extends AppendOrientedList<V>
  implements HasReverseIterator<V>
{
    /* package private */ static <V> @NotNull ReverseListNode<V>
    create(@NotNull AppendOrientedList<V> precedingElements, @NotNull V lastElement)
    {
        return new ReverseListNode<>(precedingElements, lastElement);
    }

    private int size;
    private @NotNull AppendOrientedList<V> precedingElements;  // final except for the builder
    private final @NotNull V lastElement;

    private ReverseListNode(@NotNull AppendOrientedList<V> precedingElements, @NotNull V lastElement)
    {
        this.size = precedingElements.size() + 1;
        this.precedingElements = precedingElements;
        this.lastElement = lastElement;
    }

    /**
      For the exclusive use of the builder.
    */

    /* package private */ void installPrevious(@NotNull AppendOrientedList<V> preceding)
    {
        if (!precedingElements.isEmpty()) {
            throw new IllegalStateException("This node already has a preceding element");
        }
        this.precedingElements = preceding;
    }

    /**
      For the exclusive use of the builder.
    */

    /* package private */ @Nullable ReverseListNode<V> installSize(int size)
    {
        this.size = size;
        return precedingElements.isEmpty() ? null : (ReverseListNode<V>) precedingElements;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public @NotNull AppendOrientedList<V> head()
    {
        return precedingElements;
    }

    @Override
    public @NotNull IList<V> getElements(int index, int count)
      throws IndexOutOfBoundsException
    {
        if (index < 0 || count < 0 || index + count > size) {
            throw new IndexOutOfBoundsException();
        }

        if (count == 0) {
            return IList.empty();
        }

        IList<V> result = onReverse().find(ListImplSupport.createReverseSubrangeVisitor(size, index, count,
          AppendOrientedList.builder()));
        assert result != null;
        return result.reverse();
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

        if (size != otherList.size()) {
            return false;
        }

        if (size <= ListImplSupport.RECURSION_LIMIT) {
            ReverseListNode<?> otherNode = Extensions.getExtension(obj, ReverseListNode.class);
            if (otherNode != null) {
                return lastElement.equals(otherNode.lastElement) && precedingElements.equals(otherNode.precedingElements);
            }
        }

        return ListEquality.isEqual(this, otherList);
    }

    @Override
    public @Nullable V optionalFirst()
    {
        if (size == 1) {
            return lastElement;
        }
        AppendOrientedList<V> currentNode = precedingElements;
        V result = null;
        while (!currentNode.isEmpty()) {
            result = currentNode.last();
            currentNode = currentNode.head();
        }
        return result;
    }

    @Override
    public @NotNull V optionalLast()
    {
        return lastElement;
    }

    @Override
    public @NotNull AppendOrientedList<V> appending(@NotNull V value)
    {
        return create(this, value);
    }

    @Override
    public @NotNull AppendOrientedList<V> prepending(@NotNull V value)
    {
        return (AppendOrientedList<V>) ListOperations.prepending(this, value, builder());
    }

    @Override
    public @NotNull AppendOrientedList<V> removing(@NotNull Object value)
    {
        return mapFilter(ListImplSupport.createRemoveElementMapper(value));
    }

    @Override
    public <R> @NotNull AppendOrientedList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        return (AppendOrientedList<R>) ListOperations.map(this, mapper, builder());
    }

    @Override
    public <R> @NotNull AppendOrientedList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper)
    {
        return (AppendOrientedList<R>) ListOperations.mapFilter(this, mapper, builder());
    }

    @Override
    public void visit(@NotNull Visitor<V> visitor)
    {
        if (size <= ListImplSupport.RECURSION_LIMIT) {
            precedingElements.visit(visitor);
            visitor.visit(lastElement);
        } else {
            super.visit(visitor);
        }
    }

    @Override
    public <R> @Nullable R find(@NotNull FindVisitor<V,R> visitor)
    {
        if (size <= ListImplSupport.RECURSION_LIMIT) {
            R result = precedingElements.find(visitor);
            if (result != null) {
                return result;
            }
            return visitor.visit(lastElement);
        } else {
            return super.find(visitor);
        }
    }

    @Override
    public @NotNull IIterator<V> iterator()
    {
        Object[] elements = toJavaArray();
        return ArrayIterator.create(elements);
    }

    @Override
    public @NotNull IIterator<V> reverseIterator()
    {
        return new MyReverseIterator<>(this);
    }

    @Override
    public @NotNull IList<V> onReverse()
    {
        return ListReverseImpl.create(this);
    }

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        return ListOperations.appendingAll(this, values);
    }

    @Override
    public @NotNull IList<V> removing(int index, int count)
      throws IndexOutOfBoundsException
    {
        return ListOperations.removing(this, index, count, RRBList.builder());
    }

    @Override
    public @NotNull IList<V> replacing(int index, @NotNull V value)
      throws IndexOutOfBoundsException
    {
        return ListOperations.replacing(this, index, value, RRBList.builder());
    }

    @Override
    public @NotNull AppendOrientedList<V> replacingAll(int index, int count, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        return (AppendOrientedList) ListOperations.replacingAll(this, index, count, values, builder());
    }

    @Override
    public @NotNull IList<V> insertingAll(int position, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        return ListOperations.insertingAll(this, position, values, builder());
    }

    private @NotNull Object @NotNull [] toJavaArray()
    {
        Object[] array = new Object[size];
        visitReverse(ListImplSupport.createToReverseJavaArrayVisitor(array));
        return array;
    }

    private static class MyReverseIterator<V>
      implements IIterator<V>
    {
        private @Nullable ReverseListNode<V> node;

        public MyReverseIterator(@NotNull ReverseListNode<V> node)
        {
            this.node = node;
        }

        @Override
        public @NotNull V next()
        {
            if (node == null) {
                throw new NoSuchElementException();
            }
            V result = node.lastElement;
            AppendOrientedList<V> remainingElements = node.precedingElements;
            if (remainingElements.isEmpty()) {
                node = null;
            } else {
                node = (ReverseListNode<V>) remainingElements;
            }
            return result;
        }

        @Override
        public boolean hasNext()
        {
            return node != null;
        }
    }
}
