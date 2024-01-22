/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import org.violetlib.collections.FindVisitor;
import org.violetlib.collections.ICollection;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;
import org.violetlib.collections.ISet;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.ListUsage;
import org.violetlib.collections.Visitor;
import org.violetlib.util.Extensions;
import org.violetlib.types.UndefinedValueError;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  An implementation of immutable lists that stores the elements in an array.
*/

/* package private */ final @Immutable class SimpleList<V>
  extends ListImplBase<V>
  implements HasReverseIterator<V>
{
    @SafeVarargs
    public static <V> @NotNull IList<V> create(@NotNull V... elements)
    {
        return elements.length == 0 ? EMPTY : createWithElements(elements);
    }

    public static <V> @NotNull IList<V> fromList(@NotNull List<? extends V> values)
    {
        if (values.isEmpty()) {
            return EMPTY;
        } else {
            return new SimpleList<>(values);
        }
    }

    public static <V> @NotNull IList<V> fromList(@NotNull IList<? extends V> values)
    {
        if (values instanceof SimpleList) {
            return (SimpleList) values;
        } else if (values.isEmpty()) {
            return EMPTY;
        } else {
            return new SimpleList<>(values);
        }
    }

    public static <V> @NotNull IList<V> fromSet(@NotNull Set<? extends V> values)
    {
        if (values.isEmpty()) {
            return EMPTY;
        } else {
            return new SimpleList<>(new ArrayList<>());
        }
    }

    private static <V> @NotNull IList<V> createWithElements(@NotNull Object @NotNull [] elements)
    {
        if (elements.length == 0) {
            return EMPTY;
        }

        return new SimpleList<>(true, elements);
    }

    private static <V> @NotNull IList<V> createWithElements(@NotNull List<V> elements)
    {
        return elements.isEmpty() ? EMPTY : new SimpleList<>(elements);
    }

    /* package private */ static final @NotNull MyEmptyList EMPTY = new MyEmptyList();

    private final @NotNull Object @NotNull [] elements;

    private SimpleList(@NotNull V value)
    {
        super(1);
        elements = new Object[1];
        elements[0] = value;
    }

    private SimpleList(@NotNull List<? extends V> values)
    {
        super(values.size());
        elements = values.toArray();
    }

    private SimpleList(@NotNull IList<? extends V> values)
    {
        super(values.size());
        int count = values.size();
        this.elements = new Object[count];
        int[] indexCell = new int[1];
        values.visit(value -> elements[indexCell[0]++] = value);
    }

    private SimpleList(boolean fake, @NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        super(count(values));
        this.elements = new Object[size];
        int index = 0;
        for (V value : values) {
            if (value == null) {
                throw new IllegalArgumentException("Null elements are not permitted");
            }
            elements[index++] = value;
        }
    }

    private SimpleList(@NotNull SimpleList list, @NotNull V e)
    {
        super(list.size() + 1);
        this.elements = new Object[size];
        System.arraycopy(list.elements, 0, this.elements, 0, size-1);
        this.elements[size-1] = e;
    }

    private SimpleList(boolean fake, @NotNull V e, @NotNull SimpleList list)
    {
        super(list.size() + 1);
        this.elements = new Object[size];
        System.arraycopy(list.elements, 0, this.elements, 1, size-1);
        this.elements[0] = e;
    }

    private SimpleList(boolean fake, @NotNull Object[] elements)
    {
        super(elements.length);
        this.elements = elements;
    }

    /* package private */ @NotNull Object @NotNull [] asArray()
    {
        return elements;
    }

    @Override
    public @Nullable V getOptional(int index)
    {
        if (index >= 0 && index < size) {
            return (V) elements[index];
        } else {
            return null;
        }
    }

    @Override
    public @NotNull V get(int index)
      throws IndexOutOfBoundsException
    {
        if (index >= 0 && index < size) {
            return (V) elements[index];
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public @Nullable V optionalFirst()
    {
        return size == 0 ? null : (V) elements[0];
    }

    @Override
    public @Nullable V optionalLast()
    {
        return size == 0 ? null : (V) elements[size-1];
    }

    @Override
    public @NotNull IList<V> getElements(int index, int count)
      throws IndexOutOfBoundsException
    {
        if (index < 0 || count < 0 || index + count > size) {
            throw new IndexOutOfBoundsException();
        }

        Object[] result = new Object[count];
        System.arraycopy(elements, index, result, 0, count);
        return createWithElements(result);
    }

    @Override
    public int indexOf(@NotNull Object element)
    {
        for (int i = 0; i < size; i++) {
            if (elements[i].equals(element)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean contains(@NotNull Object target)
    {
        for (int i = 0; i < size; i++) {
            if (elements[i].equals(target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull IIterator<V> iterator()
    {
        return new MyIterator<>(elements);
    }

    @Override
    public @NotNull IIterator<V> reverseIterator()
    {
        return new MyReverseIterator<>(elements);
    }

    @Override
    public void visit(@NotNull Visitor<V> visitor)
    {
        for (int i = 0 ; i < size; i++) {
            V e = (V) elements[i];
            visitor.visit(e);
        }
    }

    @Override
    public <R> @Nullable R find(@NotNull FindVisitor<V,R> visitor)
    {
        for (int i = 0; i < size; i++) {
            V e = (V) elements[i];
            R result = visitor.visit(e);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public @NotNull IList<V> replacing(int index, @NotNull V value)
      throws IndexOutOfBoundsException
    {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        if (elements[index] == value) {
            return this;
        }
        Object[] newElements = new Object[size];
        System.arraycopy(elements, 0, newElements, 0, size);
        newElements[index] = value;
        return createWithElements(newElements);
    }

    @Override
    public @NotNull IList<V> appending(@NotNull V value)
    {
        return new SimpleList<>(this, value);
    }

    @Override
    public @NotNull IList<V> prepending(@NotNull V value)
    {
        return new SimpleList<>(true, value, this);
    }

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        ICollection c = Extensions.getExtension(values, ICollection.class);
        if (c != null) {
            return addElements(c);
        }
        ListBuilder<V> b = IList.builder();
        b.addAll(values);
        return addElements(b.values());
    }

    private @NotNull IList<V> addElements(@NotNull ICollection<? extends V> values)
    {
        int addedCount = values.size();
        if (addedCount == 0) {
            return this;
        }

        Object[] newElements = new Object[size+addedCount];
        System.arraycopy(elements, 0, newElements, 0, size);
        int[] indexCell = new int[1];
        indexCell[0] = size;
        values.visit(v -> {
            int index = indexCell[0]++;
            newElements[index] = v;
        });
        return createWithElements(newElements);
    }

    @Override
    public @NotNull IList<V> replacingAll(int index, int count, @NotNull Iterable<? extends V> values)
      throws IndexOutOfBoundsException, IllegalArgumentException
    {
        if (index < 0 || index > size || count < 0) {
            throw new IndexOutOfBoundsException();
        }
        int availableToRemove = size - index;
        if (count > availableToRemove) {
            count = availableToRemove;
        }
        int tailSize = size - (index + count);
        int addedCount = count(values);

        if (count == 0 && addedCount == 0) {
            return this;
        }

        Object[] newElements = new Object[index + addedCount + tailSize];
        System.arraycopy(elements, 0, newElements, 0, index);

         int destIndex = index;
         for (V value : values) {
             if (value == null) {
                 throw new IllegalArgumentException("Null elements are not permitted");
             }
             newElements[destIndex++] = value;
         }

         if (tailSize > 0) {
             System.arraycopy(elements, index + count, newElements, destIndex, tailSize);
         }

         return createWithElements(newElements);
    }

    private static int count(@NotNull Iterable<?> source)
    {
        int knownSize = CollectionsUtils.getKnownSize(source);
        if (knownSize >= 0) {
            return knownSize;
        }

        int count = 0;
        for (Object ignore : source) {
            ++count;
        }
        return count;
    }

    @Override
    public @NotNull IList<V> removingAll(@NotNull ISet<?> values)
    {
        List<V> remaining = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            V e = (V) elements[i];
            if (!values.contains(e)) {
                remaining.add(e);
            }
        }
        return remaining.size() == size ? this : createWithElements(remaining);
    }

    @Override
    public @NotNull IList<V> removing(int index, int deleteCount)
      throws ArrayIndexOutOfBoundsException
    {
        if (deleteCount == 0) {
            return this;
        }

        if (index < 0 || index >= size || deleteCount < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        int availableToRemove = size - index;
        if (deleteCount > availableToRemove) {
            deleteCount = availableToRemove;
        }

        if (deleteCount == 0) {
            return this;
        }

        int newSize = size - deleteCount;
        if (newSize == 0) {
            return IList.empty();
        }

        Object[] newElements = new Object[newSize];
        System.arraycopy(elements, 0, newElements, 0, index);
        System.arraycopy(elements, index + deleteCount, newElements, index, newSize - index);
        return createWithElements(newElements);
    }

    @Override
    public <R> @NotNull IList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        Object[] newElements = new Object[size];
        for (int i = 0; i < size; i++) {
            R replacement = mapper.apply((V) elements[i]);
            if (replacement == null) {
                throw UndefinedValueError.create("Mapper must not return null");
            }
            newElements[i] = replacement;
        }
        return createWithElements(newElements);
    }

    @Override
    public <R> @NotNull IList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper)
    {
        List<R> newElements = new ArrayList<>();
        visit(v -> {
            R replacement = mapper.apply(v);
            if (replacement != null) {
                newElements.add(replacement);
            }
        });
        return createWithElements(newElements);
    }

    @Override
    public @NotNull IList<V> reverse()
    {
        if (size < 2) {
            return this;
        }
        Object[] newElements = new Object[size];
        int midpoint = size >> 1;
        int i = 0;
        int j = size-1;
        while (i < midpoint) {
            newElements[i] = elements[j];
            newElements[j] = elements[i];
            i++;
            j--;
        }
        if (i == j) {
            newElements[i] = elements[i];
        }
        return createWithElements(newElements);
    }

    @Override
    public @NotNull IList<V> onReverse()
    {
        return ListReverseImpl.create(this);
    }

    @Override
    public @NotNull IList<V> sort(@NotNull Comparator<? super V> c)
    {
        if (size < 2) {
            return this;
        }
        V[] newElements = (V[]) new Object[size];
        System.arraycopy(elements, 0, newElements, 0, size);
        Arrays.sort(newElements, c);
        return createWithElements(newElements);
    }

    @Override
    public <E> @NotNull E[] toJavaArray(@NotNull E[] template)
    {
        E[] result = (E[]) java.lang.reflect.Array.newInstance(template.getClass().getComponentType(), size);
        System.arraycopy(elements, 0, result, 0, size);
        return result;
    }

    private static class MyEmptyList<V>
      extends EmptyListBase<V>
      implements IList<V>
    {
        @Override
        public @NotNull IList<V> appending(@NotNull V value)
        {
            return new SimpleList<>(value);
        }

        @Override
        public @NotNull IList<V> prepending(@NotNull V value)
        {
            return new SimpleList<>(value);
        }

        @Override
        public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
          throws IllegalArgumentException
        {
            return IList.create(values);
        }

        @Override
        public @Nullable V getOptional(int index)
        {
            return null;
        }

        @Override
        public @NotNull V get(int index)
          throws IndexOutOfBoundsException
        {
            throw new IndexOutOfBoundsException("Empty array");
        }

        @Override
        public @NotNull IList<V> getElements(int index, int count)
          throws IndexOutOfBoundsException
        {
            if (count == 0) {
                return this;
            } else {
                throw new IndexOutOfBoundsException("Empty array");
            }
        }

        @Override
        public int indexOf(@NotNull Object element)
        {
            return -1;
        }

        @Override
        public @NotNull IList<V> replacing(int index, @NotNull V value)
          throws IndexOutOfBoundsException
        {
            throw new IndexOutOfBoundsException("Empty array");
        }

        @Override
        public @NotNull IList<V> replacingAll(int index, int count, @NotNull Iterable<? extends V> values)
          throws IndexOutOfBoundsException, IllegalArgumentException
        {
            if (index != 0 || count < 0) {
                throw new IndexOutOfBoundsException();
            }
            return new SimpleList<>(true, values);
        }
    }

    @Override
    public @NotNull IList<V> optimizeForForwardTraversal()
    {
        return this;
    }

    @Override
    public @NotNull IList<V> optimizeForIndexing()
    {
        return this;
    }

    @Override
    public @NotNull IList<V> optimize(@NotNull ListUsage usage)
    {
        if (usage == ListUsage.PREPEND) {
            return PrependOrientedList.fromList(this);
        }
        if (usage == ListUsage.APPEND) {
            return AppendOrientedList.fromList(this);
        }
        // TBD: indexed vs default
        return this;
    }

    private static class MyIterator<V>
      implements IIterator<V>
    {
        private final @NotNull Object[] elements;
        private int nextIndex;

        public MyIterator(@NotNull Object[] elements)
        {
            this.elements = elements;
        }

        @Override
        public boolean hasNext()
        {
            return nextIndex != elements.length;
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull V next()
        {
            int i = nextIndex;
            if (i >= elements.length) {
                throw new NoSuchElementException();
            }
            nextIndex = i + 1;
            return (V) elements[i];
        }
    }

    private static class MyReverseIterator<V>
      implements IIterator<V>
    {
        private final @NotNull Object[] elements;
        private int nextIndex;

        public MyReverseIterator(@NotNull Object[] elements)
        {
            this.elements = elements;
            this.nextIndex = elements.length - 1;
        }

        @Override
        public boolean hasNext()
        {
            return nextIndex >= 0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull V next()
        {
            int i = nextIndex;
            if (i < 0) {
                throw new NoSuchElementException();
            }
            nextIndex = i - 1;
            return (V) elements[i];
        }
    }
}
