/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.violetlib.collections.FindVisitor;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.Visitor;
import org.violetlib.collections.impl.ListEquality;
import org.violetlib.collections.impl.ListImplSupport;
import org.violetlib.collections.impl.ListOperations;
import org.violetlib.collections.impl.ListReverseImpl;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;

/**
  A base class for a tree list implementation based on slices.
*/

public abstract class TreeListSlicesImpl<V>
  extends TreeList<V>
  implements TreeSlices
{
    protected TreeListSlicesImpl(int size)
    {
        super(size);
    }

    @Override
    public @NotNull IIterator<V> iterator()
    {
        return (IIterator<V>) TreeSliceIterator.create(this, size);
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
    public int indexOf(@NotNull Object element)
    {
        Integer index = internalFind(ListImplSupport.createIndexOfVisitor(element));
        return index != null ? index : -1;
    }

    @Override
    public boolean contains(@NotNull Object element)
    {
        Integer index = internalFind(ListImplSupport.createIndexOfVisitor(element));
        return index != null;
    }

    @Override
    public @NotNull IList<V> onReverse()
    {
        return ListReverseImpl.create(this);
    }

    @Override
    public void visit(@NotNull Visitor<V> visitor)
    {
        TreeSliceFinder finder = TreeSliceFinder.get();
        finder.visit(this, size, visitor);
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

        IList<V> result = internalFind(ListImplSupport.createSubrangeVisitor(index, count, TreeList.builder()));
        assert result != null;
        return result;
    }

    @Override
    public @NotNull IList<V> removing(@NotNull Object value)
    {
        return ListOperations.removing(this, value);
    }

    @Override
    public @NotNull IList<V> removing(int index, int deleteCount)
      throws IndexOutOfBoundsException
    {
        return ListOperations.removing(this, index, deleteCount, TreeList.builder());
    }

    @Override
    public @NotNull <R> IList<R> mapFilter(@NotNull Function<@NotNull V,@Nullable R> mapper)
    {
        ListBuilder<R> builder = TreeList.builder();
        Visitor<V> visitor = ListImplSupport.createMapFilterVisitor(builder, mapper);
        TreeSliceFinder finder = TreeSliceFinder.get();
        finder.visit(this, size, visitor);
        return builder.values();
    }

    @Override
    public @NotNull List<V> toJavaList()
    {
        List<V> result = new ArrayList<>();
        Visitor<V> visitor = ListImplSupport.createToJavaListVisitor(result);
        TreeSliceFinder finder = TreeSliceFinder.get();
        finder.visit(this, size, visitor);
        return result;
    }

    @Override
    public @NotNull Set<V> toJavaSet()
    {
        Set<V> result = new HashSet<>();
        Visitor<V> visitor = ListImplSupport.createToJavaSetVisitor(result);
        TreeSliceFinder finder = TreeSliceFinder.get();
        finder.visit(this, size, visitor);
        return result;
    }

    @Override
    public @NotNull IList<V> sort(@NotNull Comparator<? super V> c)
    {
        List<V> list = toJavaList();
        list.sort(c);
        return TreeList.fromList(list);
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

    @Override
    protected <R> @Nullable R internalFind(@NotNull FindVisitor<V,R> visitor)
    {
        TreeSliceFinder finder = TreeSliceFinder.get();
        return finder.find(this, size, visitor);
    }

    @Override
    protected <R> @Nullable R internalFindReverse(@NotNull FindVisitor<V,R> visitor)
    {
        TreeSliceFinder finder = TreeSliceFinder.get();
        return finder.findReverse(this, size, visitor);
    }
}
