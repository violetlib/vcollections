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
  A node in a linked list.
  <p>
  This class avoids the usual recursive method implementations on larger lists which can overflow the stack.
*/

/* package private */ final class ForwardListNode<V>
  extends PrependOrientedList<V>
  implements HasReverseIterator<V>
{
    /* package private */ static <V> @NotNull ForwardListNode<V>
    create(@NotNull V firstElement, @NotNull PrependOrientedList<V> remainingElements)
    {
        return new ForwardListNode<>(firstElement, remainingElements);
    }

    private int size;  // final except for the builder
    private final @NotNull V firstElement;
    private @NotNull PrependOrientedList<V> remainingElements;  // final except for the builder

    private ForwardListNode(@NotNull V firstElement, @NotNull PrependOrientedList<V> remainingElements)
    {
        this.size = remainingElements.size() + 1;
        this.firstElement = firstElement;
        this.remainingElements = remainingElements;
    }

    /**
      For the exclusive use of the builder.
    */

    /* package private */ void installNext(@NotNull PrependOrientedList<V> next)
    {
        if (!remainingElements.isEmpty()) {
            throw new IllegalStateException("This node already has a next element");
        }
        this.remainingElements = next;
    }

    /**
      For the exclusive use of the builder.
    */

    /* package private */ @Nullable ForwardListNode<V> installSize(int size)
    {
        this.size = size;
        return remainingElements.isEmpty() ? null : (ForwardListNode<V>) remainingElements;
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
    public @NotNull PrependOrientedList<V> tail()
    {
        return remainingElements;
    }

    @Override
    public @NotNull IList<V> getElements(int index, int count)
      throws IndexOutOfBoundsException
    {
        return ListOperations.getElements(this, index, count);
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
            ForwardListNode<?> otherNode = Extensions.getExtension(obj, ForwardListNode.class);
            if (otherNode != null) {
                return firstElement.equals(otherNode.firstElement) && remainingElements.equals(otherNode.remainingElements);
            }
        }

        return ListEquality.isEqual(this, otherList);
    }

    @Override
    public void visit(@NotNull Visitor<V> visitor)
    {
        visitor.visit(firstElement);
        PrependOrientedList<V> currentNode = remainingElements;
        while (!currentNode.isEmpty()) {
            visitor.visit(currentNode.first());
            currentNode = currentNode.tail();
        }
    }

    @Override
    public <R> @Nullable R find(@NotNull FindVisitor<V,R> visitor)
    {
        R result = visitor.visit(firstElement);
        if (result != null) {
            return result;
        }
        PrependOrientedList<V> currentNode = remainingElements;
        while (!currentNode.isEmpty()) {
            result = visitor.visit(currentNode.first());
            if (result != null) {
                return result;
            }
            currentNode = currentNode.tail();
        }
        return null;
    }

    @Override
    public @NotNull V optionalFirst()
    {
        return firstElement;
    }

    @Override
    public @Nullable V optionalLast()
    {
        if (size == 1) {
            return firstElement;
        }
        PrependOrientedList<V> currentNode = remainingElements;
        V result = null;
        while (!currentNode.isEmpty()) {
            result = currentNode.first();
            currentNode = currentNode.tail();
        }
        return result;
    }

    @Override
    public @NotNull PrependOrientedList<V> appending(@NotNull V value)
    {
        return (PrependOrientedList<V>) ListOperations.appending(this, value, builder());
    }

    @Override
    public @NotNull PrependOrientedList<V> prepending(@NotNull V value)
    {
        return new ForwardListNode<>(value, this);
    }

    @Override
    public @NotNull PrependOrientedList<V> removing(@NotNull Object value)
    {
        return mapFilter(ListImplSupport.createRemoveElementMapper(value));
    }

    @Override
    public <R> @NotNull PrependOrientedList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        return (PrependOrientedList<R>) ListOperations.map(this, mapper, builder());
    }

    @Override
    public <R> @NotNull PrependOrientedList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper)
    {
        return (PrependOrientedList<R>) ListOperations.mapFilter(this, mapper, builder());
    }

    @Override
    public @NotNull IIterator<V> iterator()
    {
        return new MyIterator<>(this);
    }

    @Override
    public @NotNull IIterator<V> reverseIterator()
    {
        Object[] elements = toJavaArray();
        return ArrayReverseIterator.create(elements);
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
        return ListOperations.removing(this, index, count, builder());
    }

    @Override
    public @NotNull IList<V> replacing(int index, @NotNull V value)
      throws IndexOutOfBoundsException
    {
        return ListOperations.replacing(this, index, value, builder());
    }

    @Override
    public @NotNull IList<V> replacingAll(int index, int count, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        return ListOperations.replacingAll(this, index, count, values, builder());
    }

    private Object @NotNull [] toJavaArray()
    {
        Object[] array = new Object[size];
        visit(ListImplSupport.createToJavaArrayVisitor(array));
        return array;
    }

    private static class MyIterator<V>
      implements IIterator<V>
    {
        private @Nullable ForwardListNode<V> node;

        public MyIterator(@NotNull ForwardListNode<V> node)
        {
            this.node = node;
        }

        @Override
        public @NotNull V next()
        {
            if (node == null) {
                throw new NoSuchElementException();
            }
            V result = node.firstElement;
            PrependOrientedList<V> remainingElements = node.remainingElements;
            if (remainingElements.isEmpty()) {
                node = null;
            } else {
                node = (ForwardListNode<V>) remainingElements;
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
