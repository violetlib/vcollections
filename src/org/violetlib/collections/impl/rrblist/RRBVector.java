/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.rrblist;

import java.util.function.Supplier;

import org.jetbrains.annotations.*;

import static org.violetlib.collections.impl.rrblist.RRBVectorConstants.*;

/**
  A vector implemented using a relaxed radix balanced tree.
  This class is fully immutable. It does not contain support for a focused subtree.
  @see FocusableRRBVector
  @see MutableRRBVector
*/

public class RRBVector<V>
{
    private static final @NotNull RRBVector<Object> EMPTY = new RRBVector<>();
    private static final Object @NotNull [] EMPTY0 = new Object[0];

    /**
      Return an empty vector.
    */

    public static <V> @NotNull RRBVector<V> empty()
    {
        return (RRBVector) EMPTY;
    }

    /**
      Return a vector containing one element.
    */

    public static <V> @NotNull RRBVector<V> singleton(@NotNull V element)
    {
        return new RRBVector<>(true, element);
    }

    /**
      Return a vector with a specified size filled with the specified items.
      @param size The number of elements.
      @param items The source for the element values. This supplier is invoked once per element in increasing order.
      It must not return null.
    */

    public static <V> @NotNull RRBVector<V> create(int size, @NotNull Supplier<V> items)
    {
        return new RRBVector<>(size, items);
    }

    /**
      The depth of the tree, which is the number of nodes in any tree path.
      A value of 1 indicates that the tree consists of a single leaf node which may be empty.
      The largest supported depth is 6.
    */

    protected final int depth;

    /**
      One greater than the last valid index (in other words, the number of elements).
    */

    protected final int endIndex;

    protected final @NotNull Object[] root;

    /**
      Initialize a vector containing no elements.
    */

    protected RRBVector()
    {
        depth = 1;
        endIndex = 0;
        root = new Object[0];
    }

    /**
      Initialize a vector with one element.
    */

    protected RRBVector(boolean fake, @NotNull V element)
    {
        endIndex = 1;
        root = new Object[1];
        root[0] = element;
        depth = 1;
    }

    /**
      Initialize a vector with a specified size.
      @param size The number of elements.
      @param items The source for the element values. This supplier is invoked once per element in increasing order.
      It must not return null.
    */

    protected RRBVector(int size, @NotNull Supplier<V> items)
    {
        if (size < 0) {
            throw new IllegalArgumentException("Invalid requested size");
        }
        depth = depthForSize(size);
        endIndex = size;
        root = createSubtree(depth, endIndex, items);
    }

    private Object @NotNull [] createSubtree(int level, int remaining, @NotNull Supplier<V> items)
    {
        if (level == 1) {
            if (remaining == 0) {
                return EMPTY0;
            }
            assert remaining > 0 && remaining <= WIDTH;
            Object[] node = new Object[remaining];
            for (int i = 0; i < remaining; i++) {
                V item = items.get();
                if (item == null) {
                    throw new IllegalArgumentException("Item supplier must not return null");
                }
                node[i] = item;
            }
            return node;
        }
        int subtreeSize = getMaximumTreeSize(level - 1);
        int subtreesRequired = (remaining + subtreeSize - 1) / subtreeSize;
        Object[] node = new Object[subtreesRequired + 1];
        for (int i = 0; i < subtreesRequired; i++) {
            int thisSubtreeSize = Math.min(remaining, subtreeSize);
            node[i] = createSubtree(level - 1, thisSubtreeSize, items);
            remaining -= thisSubtreeSize;
        }
        return node;
    }

    /**
      Initialize a vector with data from the specified source.
    */

    protected RRBVector(@NotNull RRBVector<V> source)
    {
        this.depth = source.depth;
        this.endIndex = source.endIndex;
        this.root = source.root;
    }

    /**
      Implementation method: Initialize a vector with the specified tree.
      @param length The number of list elements.
      @param root The root node of the tree.
      @param depth The depth of the tree.
    */

    protected RRBVector(int length, Object @NotNull [] root, int depth)
    {
        assert depth > 0 && depth <= 6;
        this.depth = depth;
        this.endIndex = length;
        this.root = root;
    }

    protected @NotNull V getElementFromRoot(int index)
    {
        int indexInSubTree = index;
        int currentLevel = depth;
        Object[] node = root;
        int[] sizes = getSizes(currentLevel, node);
        while (sizes != null) {
            int sizesIdx = getIndexInSizes(sizes, indexInSubTree);
            if (sizesIdx != 0) {
                indexInSubTree -= sizes[sizesIdx - 1];
            }
            node = (Object[]) node[sizesIdx];
            --currentLevel;
            sizes = getSizes(currentLevel, node);
        }

        switch (currentLevel) {
            case 1: return getElem0(node, indexInSubTree);
            case 2: return getElem1(node, indexInSubTree);
            case 3: return getElem2(node, indexInSubTree);
            case 4: return getElem3(node, indexInSubTree);
            case 5: return getElem4(node, indexInSubTree);
            case 6: return getElem5(node, indexInSubTree);
        }
        throw new AssertionError("Unexpected depth: " + currentLevel);
    }

    private int getIndexInSizes(int @NotNull [] sizes, int indexInSubTree)
    {
        if (indexInSubTree == 0) {
            return 0;
        }
        int is = 0;
        while (sizes[is] <= indexInSubTree) {
            is += 1;
        }
        return is;
    }

    protected @NotNull V getElem0(Object @NotNull [] a0, int index) {
        return (V) a0[index & MASK];
    }

    protected @NotNull V getElem1(Object @NotNull [] a1, int index)
    {
        Object[] a0 = (Object[]) a1[(index >> BITS1) & MASK];
        return (V) a0[index & MASK];
    }

    protected @NotNull V getElem2(Object @NotNull [] a2, int index)
    {
        Object[] a1 = (Object[]) a2[(index >> BITS2) & MASK];
        Object[] a0 = (Object[]) a1[(index >> BITS1) & MASK];
        return (V) a0[index & MASK];
    }

    protected @NotNull V getElem3(Object @NotNull [] a3, int index)
    {
        Object[] a2 = (Object[]) a3[(index >> BITS3) & MASK];
        Object[] a1 = (Object[]) a2[(index >> BITS2) & MASK];
        Object[] a0 = (Object[]) a1[(index >> BITS1) & MASK];
        return (V) a0[index & MASK];
    }

    protected @NotNull V getElem4(Object @NotNull [] a4, int index)
    {
        Object[] a3 = (Object[]) a4[(index >> BITS4) & MASK];
        Object[] a2 = (Object[]) a3[(index >> BITS3) & MASK];
        Object[] a1 = (Object[]) a2[(index >> BITS2) & MASK];
        Object[] a0 = (Object[]) a1[(index >> BITS1) & MASK];
        return (V) a0[index & MASK];
    }

    protected @NotNull V getElem5(Object @NotNull [] a5, int index)
    {
        Object[] a4 = (Object[]) a5[(index >> BITS5) & MASK];
        Object[] a3 = (Object[]) a4[(index >> BITS4) & MASK];
        Object[] a2 = (Object[]) a3[(index >> BITS3) & MASK];
        Object[] a1 = (Object[]) a2[(index >> BITS2) & MASK];
        Object[] a0 = (Object[]) a1[(index >> BITS1) & MASK];
        return (V) a0[index & MASK];
    }
}
