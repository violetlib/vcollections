/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.violetlib.collections.FindVisitor;
import org.violetlib.collections.ICollection;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.Visitor;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;

import static org.violetlib.collections.impl.treelist.TreeListConstants.*;

/**
  The tree list representation of a list as a single array with no unused elements.
  The list size must be between 1 and WIDTH (inclusive).
*/

public final class TreeList1<V>
  extends TreeListSlicesImpl<V>
{
    private static final int MAXIMUM_SIZE = WIDTH;

    static <V> @NotNull TreeList<V> createSingleton(@NotNull V element)
    {
        Object[] data = new Object[1];
        data[0] = element;
        return new TreeList1<>(data);
    }

    static <V> @NotNull IList<V> createWithPrivateArray(@NotNull Object @NotNull [] elements)
    {
        int size = elements.length;
        if (size == 0) {
            return TreeList.empty();
        }
        if (size <= MAXIMUM_SIZE) {
            return new TreeList1<>(elements);
        }
        return TreeList.createWithArray(elements);
    }

    private final @NotNull Object @NotNull [] elements;

    private TreeList1(@NotNull Object @NotNull [] elements)
    {
        super(elements.length);

        assert elements.length <= MAXIMUM_SIZE;

        this.elements = elements;
    }

    @Override
    public int getSliceCount()
    {
        return 1;
    }

    @Override
    public Object @NotNull [] getSlice(int sliceIndex)
    {
        if (sliceIndex == 0) {
            return elements;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    public int getSlicePrefixLength(int sliceIndex)
    {
        if (sliceIndex == 0) {
            return elements.length;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    public int getSliceDepth(int sliceIndex)
    {
        if (sliceIndex == 0) {
            return 1;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    public int getSliceElementCount(int sliceIndex)
    {
        if (sliceIndex == 0) {
            return elements.length;
        }
        throw new IllegalArgumentException("Invalid slice index");
    }

    @Override
    protected @NotNull V internalGet(int index)
    {
        return (V) elements[index];
    }

    @Override
    public @NotNull V optionalFirst()
    {
        return (V) elements[0];
    }

    @Override
    public @NotNull V optionalLast()
    {
        return (V) elements[size-1];
    }

    @Override
    public @NotNull IList<V> getElements(int index, int count)
      throws IndexOutOfBoundsException
    {
        if (index < 0 || count < 0 || index + count > size) {
            throw new IndexOutOfBoundsException();
        }

        if (count == 0) {
            return TreeList.empty();
        }

        Object[] result = new Object[count];
        System.arraycopy(elements, index, result, 0, count);
        return new TreeList1<>(result);
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
    public void visit(@NotNull Visitor<V> visitor)
    {
        for (Object o : elements) {
            visitor.visit((V) o);
        }
    }

    @Override
    protected <R> @Nullable R internalFind(@NotNull FindVisitor<V,R> visitor)
    {
        for (Object e : elements) {
            R result = visitor.visit((V) e);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    protected <R> @Nullable R internalFindReverse(@NotNull FindVisitor<V,R> visitor)
    {
        for (int i = size-1; i >= 0; i--) {
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
        // An equals() replacement might be intentional.
        if (elements[index] == value) {
            return this;
        }
        Object[] newElements = new Object[size];
        System.arraycopy(elements, 0, newElements, 0, size);
        newElements[index] = value;
        return new TreeList1<>(newElements);
    }

    @Override
    public @NotNull IList<V> appending(@NotNull V value)
    {
        if (size < MAXIMUM_SIZE) {
            return new TreeList1<>(ArraySupport.copyAppend1(elements, value));
        } else {
            return TreeList2.internalCreate(elements, value);
        }
    }

    @Override
    public @NotNull IList<V> prepending(@NotNull V value)
    {
        if (size < MAXIMUM_SIZE) {
            return new TreeList1<>(ArraySupport.copyPrepend1(value, elements));
        } else {
            return TreeList2.internalCreate(value, elements);
        }
    }

    @Override
    public @NotNull IList<V> appendingAll(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        ICollection<? extends V> c = Extensions.getExtension(values, ICollection.class);
        if (c != null) {
            return addElements(c);
        }
        ListBuilder<V> b = TreeListBuilder.create();
        b.addAll(values);
        return addElements(b.values());
    }

    private @NotNull IList<V> addElements(@NotNull ICollection<? extends V> values)
    {
        int addedCount = values.size();
        if (addedCount == 0) {
            return this;
        }

        int newSize = size + addedCount;
        if (newSize <= MAXIMUM_SIZE) {
            Object[] newElements = new Object[size + addedCount];
            System.arraycopy(elements, 0, newElements, 0, size);
            int[] indexCell = new int[1];
            indexCell[0] = size;
            values.visit(v -> {
                int index = indexCell[0]++;
                newElements[index] = v;
            });
            return createWithPrivateArray(newElements);
        } else {
            ListBuilder<V> b = TreeListBuilder.create();
            b.addAll(this);
            b.addAll(values);
            return b.values();
        }
    }

    @Override
    public @NotNull IList<V> insertingAll(int position, @NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        if (position < 0 || position > size) {
            throw new ArrayIndexOutOfBoundsException();
        }

        int addedCount = IterableSupport.count(values);
        if (addedCount == 0) {
            return this;
        }

        int newSize = size + addedCount;
        if (newSize <= MAXIMUM_SIZE) {
            Object[] newElements = new Object[size+addedCount];
            System.arraycopy(elements, 0, newElements, 0, position);

            int destIndex = position;
            for (V value : values) {
                if (value == null) {
                    throw new IllegalArgumentException("Null elements are not permitted");
                }
                newElements[destIndex++] = value;
            }

            int remaining = size - position;
            if (remaining > 0) {
                System.arraycopy(elements, position, newElements, destIndex, remaining);
            }

            return createWithPrivateArray(newElements);
        } else {
            return super.insertingAll(position, values);
        }
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

        int newSize = size - deleteCount;
        if (newSize == 0) {
            return IList.empty();
        }

        Object[] newElements = new Object[newSize];
        System.arraycopy(elements, 0, newElements, 0, index);
        System.arraycopy(elements, index + deleteCount, newElements, index, newSize - index);
        return new TreeList1<>(newElements);
    }

    @Override
    public <R> @NotNull IList<R> map(@NotNull Function<@NotNull V,@NotNull R> mapper)
    {
        Object[] resultElements = ArraySupport.map1(elements, mapper);
        return new TreeList1<>(resultElements);
    }

    @Override
    public @NotNull <R> IList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper)
    {
        Object[] resultElements = ArraySupport.map1(elements, mapper);
        if (resultElements == null) {
            return TreeList.empty();
        }
        return new TreeList1<>(resultElements);
    }

    @Override
    public @NotNull IList<V> sort(@NotNull Comparator<? super V> c)
    {
        Object[] resultElements = toJavaArray(new Object[size]);
        Arrays.sort(resultElements);
        return new TreeList1<>(resultElements);
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
}
