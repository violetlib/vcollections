/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.violetlib.collections.IIterator;

import org.jetbrains.annotations.*;

import static org.violetlib.collections.impl.treelist.TreeListConstants.*;

/**
  An iterator for tree lists that operates directly on the representation.
  It takes advantage of the radix indexing, which means it does not work on arbitrary slices.
  The size of a slice with depth greater than 1 must be the appropriate power of the node width.
*/

public final class TreeSliceIterator<V>
  implements IIterator<V>
{
    // This implementation does not currently support iteration of a specified prefix of the list, which can be used to
    // support Spliterators.

    public static <V> @NotNull Iterator<V> create(@NotNull TreeSlices slices, int size)
    {
        return new TreeSliceIterator<>(slices, size);
    }

    private static final @NotNull Object @NotNull [] fake = new Object[0];

    private final @NotNull TreeSlices slices;
    private final int size;
    private final int sliceCount;

    private int currentSliceIndex;
    private Object @NotNull [] currentSlice;
    private int currentSliceDepth;
    private int currentSliceFirstElementIndex;
    private int nextSliceFirstElementIndex;

    // These variables are effectively a stack whose implementation takes advantage of radix indexing.
    private Object @Nullable [] tree6;
    private Object @Nullable [] tree5;
    private Object @Nullable [] tree4;
    private Object @Nullable [] tree3;
    private Object @Nullable [] tree2;
    private Object @NotNull [] currentLeaf;

    private int remainingSizeFromCurrentLeaf;
    private int currentLeafSize;
    private int currentIndexInLeaf;

    private int oldPos;

    private TreeSliceIterator(@NotNull TreeSlices slices, int size)
    {
        this.slices = slices;
        this.size = size;
        this.sliceCount = slices.getSliceCount();
        this.currentSliceIndex = -1;
        this.currentSlice = fake;

        this.currentLeaf = fake;
        this.currentLeafSize = 0;
        this.currentIndexInLeaf = 0;
        this.remainingSizeFromCurrentLeaf = size;
        this.currentSliceFirstElementIndex = 0;
        this.nextSliceFirstElementIndex = 0;
        this.currentSliceDepth = 0;
    }

    @Override
    public boolean hasNext()
    {
        return remainingSizeFromCurrentLeaf > currentIndexInLeaf;
    }

    @Override
    public @NotNull V next()
    {
        if (currentIndexInLeaf == currentLeafSize) {
            advance();
        }

        return (V) currentLeaf[currentIndexInLeaf++];
    }

    private void advance()
    {
        int elementsBeforeCurrentLeaf = size - remainingSizeFromCurrentLeaf;
        int pos = elementsBeforeCurrentLeaf + currentIndexInLeaf;
        if (pos == nextSliceFirstElementIndex) {
            advanceSlice();
        }
        if (currentSliceDepth > 1) {
            int offset = pos - currentSliceFirstElementIndex;
            int xor = offset ^ oldPos;
            advanceA(offset, xor);
            oldPos = offset;
        }
        assert remainingSizeFromCurrentLeaf > 0;
        assert currentIndexInLeaf == currentLeafSize;
        remainingSizeFromCurrentLeaf -= currentIndexInLeaf;
        currentLeafSize = currentLeaf.length;
        assert currentLeafSize <= remainingSizeFromCurrentLeaf;
        currentIndexInLeaf = 0;
    }

    private void advanceSlice()
    {
        while (++currentSliceIndex < sliceCount) {
            Object[] slice = slices.getSlice(currentSliceIndex);
            if (slice.length > 0) {
                currentSlice = slice;
                currentSliceFirstElementIndex = nextSliceFirstElementIndex;
                currentSliceDepth = slices.getSliceDepth(currentSliceIndex);
                switch (currentSliceDepth) {
                    case 1: currentLeaf = currentSlice; break;
                    case 2: tree2 = currentSlice; break;
                    case 3: tree3 = currentSlice; break;
                    case 4: tree4 = currentSlice; break;
                    case 5: tree5 = currentSlice; break;
                    case 6: tree6 = currentSlice; break;
                    default: throw new UnsupportedOperationException("Unsupported slice depth");
                }
                nextSliceFirstElementIndex = currentSliceFirstElementIndex + slices.getSliceElementCount(currentSliceIndex);
                assert nextSliceFirstElementIndex <= size;
                if (currentSliceDepth > 1) {
                    oldPos = (1 << (BITS * currentSliceDepth)) - 1;
                }
                return;
            }
        }
        throw new NoSuchElementException();
    }

    private void advanceA(int io, int xor)
    {
        if(xor < WIDTH2) {
            currentLeaf = (Object[]) tree2[(io >>> BITS) & MASK];
        } else if(xor < WIDTH3) {
            tree2 = (Object[]) tree3[(io >>> BITS2) & MASK];
            currentLeaf = (Object[]) tree2[0];
        } else if(xor < WIDTH4) {
            tree3 = (Object[]) tree4[(io >>> BITS3) & MASK];
            tree2 = (Object[]) tree3[0];
            currentLeaf = (Object[]) tree2[0];
        } else if(xor < WIDTH5) {
            tree4 = (Object[]) tree5[(io >>> BITS4) & MASK];
            tree3 = (Object[]) tree4[0];
            tree2 = (Object[]) tree3[0];
            currentLeaf = (Object[]) tree2[0];
        } else {
            tree5 = (Object[]) tree6[io >>> BITS5];
            tree4 = (Object[]) tree5[0];
            tree3 = (Object[]) tree4[0];
            tree2 = (Object[]) tree3[0];
            currentLeaf = (Object[]) tree2[0];
        }
    }
}
