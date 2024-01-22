/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.rrblist;

import org.jetbrains.annotations.*;

import static org.violetlib.collections.impl.rrblist.RRBVectorConstants.*;

/**
  A builder for a relaxed radix balanced vector.
*/

public class RRBVectorBuilder<V>
  extends MutableRRBVector<V>
{
    public static <V> @NotNull RRBVectorBuilder<V> create()
    {
        return new RRBVectorBuilder<>();
    }

    private int blockIndex = 0;
    private int lo = 0;

    protected RRBVectorBuilder()
    {
        clear();
    }

    @Override
    public void clear()
    {
        super.clear();
        display1 = new Object[WIDTH];
        blockIndex = 0;
        lo = 0;
        depth = 1;
    }

    public final boolean isEmpty()
    {
        return endIndex == 0;
    }

    public final int size()
    {
        return endIndex;
    }

    public void add(@NotNull V item)
    {
        int _lo = lo;
        if (_lo >= WIDTH) {
            int _blockIndex = blockIndex;
            int newBlockIndex = _blockIndex + WIDTH;
            blockIndex = newBlockIndex;
            gotoNextBlockStartWritable(newBlockIndex ^ _blockIndex);
            _lo = 0;
        }
        display1[_lo] = item;
        lo = _lo + 1;
        ++endIndex;
    }

    /**
      Return the current contents as a new vector.
    */

    public final @NotNull RRBVector<V> asVector()
    {
        int _lo = lo;
        int size = blockIndex + _lo;
        if (size == 0) {
            return createEmptyResult();
        } else {
            Object[] d1 = display1;
            if (_lo != WIDTH) {
                Object[] d0_truncated = new Object[_lo];
                System.arraycopy(d1, 0, d0_truncated, 0, _lo);
                d1 = d0_truncated;
            }
            int lastIndex = size - 1;

            switch (depth) {

                case 1:
                    return createResult(size, d1, 1);

                case 2: {
                    Object[] d2 = copyOfAndStabilize(display2, d1, (lastIndex >> BITS1) & MASK);
                    return createResult(size, d2, 2);
                }

                case 3: {
                    Object[] d2 = copyOfAndStabilize(display2, d1, (lastIndex >> BITS1) & MASK);
                    Object[] d3 = copyOfAndStabilize(display3, d2, (lastIndex >> BITS2) & MASK);
                    return createResult(size, d3, 3);
                }

                case 4: {
                    Object[] d2 = copyOfAndStabilize(display2, d1, (lastIndex >> BITS1) & MASK);
                    Object[] d3 = copyOfAndStabilize(display3, d2, (lastIndex >> BITS2) & MASK);
                    Object[] d4 = copyOfAndStabilize(display4, d3, (lastIndex >> BITS3) & MASK);
                    return createResult(size, d4, 4);
                }

                case 5: {
                    Object[] d2 = copyOfAndStabilize(display2, d1, (lastIndex >> BITS1) & MASK);
                    Object[] d3 = copyOfAndStabilize(display3, d2, (lastIndex >> BITS2) & MASK);
                    Object[] d4 = copyOfAndStabilize(display4, d3, (lastIndex >> BITS3) & MASK);
                    Object[] d5 = copyOfAndStabilize(display5, d4, (lastIndex >> BITS4) & MASK);
                    return createResult(size, d5, 5);
                }

                case 6: {
                    Object[] d2 = copyOfAndStabilize(display2, d1, (lastIndex >> BITS1) & MASK);
                    Object[] d3 = copyOfAndStabilize(display3, d2, (lastIndex >> BITS2) & MASK);
                    Object[] d4 = copyOfAndStabilize(display4, d3, (lastIndex >> BITS3) & MASK);
                    Object[] d5 = copyOfAndStabilize(display5, d4, (lastIndex >> BITS4) & MASK);
                    Object[] createResult = copyOfAndStabilize(display6, d5, (lastIndex >> BITS5) & MASK);
                    return createResult(size, createResult, 6);
                }
            }
            throw new AssertionError("Unexpected depth: " + depth);
        }
    }

    protected @NotNull RRBVector<V> createEmptyResult()
    {
        return RRBVector.empty();
    }

    protected @NotNull RRBVector<V> createResult(int size, Object @NotNull [] root, int depth)
    {
        return new RRBVector<>(size, root, depth);
    }

    private Object @NotNull [] copyOfAndStabilize(Object @NotNull [] array,
                                                  Object lastChild,
                                                  int indexOfLastChild)
    {
        Object[] newArray = new Object[indexOfLastChild + 2];
        System.arraycopy(array, 0, newArray, 0, indexOfLastChild);
        newArray[indexOfLastChild] = lastChild;
        return newArray;
    }

    private void gotoNextBlockStartWritable(int xor)
    {
        if (xor < WIDTH2) {
            Object[] d2 = display2;
            if (depth == 1) {
                depth = 2;
                d2 = new Object[WIDTH+1];
                d2[0] = display1;
                display2 = d2;
            }
            Object[] d1 = new Object[WIDTH];
            display1 = d1;
            d2[(blockIndex >> BITS1) & MASK] = d1;
        } else if (xor < WIDTH3) {
            Object[] d3 = display3;
            if (depth == 2) {
                depth = 3;
                d3 = new Object[WIDTH+1];
                d3[0] = display2;
                display3 = d3;
            }
            Object[] d1 = new Object[WIDTH];
            Object[] d2 = new Object[WIDTH+1];
            display1 = d1;
            display2 = d2;
            int index = blockIndex;
            d2[(index >> BITS1) & MASK] = d1;
            d3[(index >> BITS2) & MASK] = d2;
        } else if (xor < WIDTH4) {
            Object[] d4 = display4;
            if (depth == 3) {
                depth = 4;
                d4 = new Object[WIDTH+1];
                d4[0] = display3;
                display4 = d4;
            }
            Object[] d1 = new Object[WIDTH];
            Object[] d2 = new Object[WIDTH+1];
            Object[] d3 = new Object[WIDTH+1];
            display1 = d1;
            display2 = d2;
            display3 = d3;
            int index = blockIndex;
            d2[(index >> BITS1) & MASK] = d1;
            d3[(index >> BITS2) & MASK] = d2;
            d4[(index >> BITS3) & MASK] = d3;
        } else if (xor < WIDTH5) {
            Object[] d5 = display5;
            if (depth == 4) {
                depth = 5;
                d5 = new Object[WIDTH+1];
                d5[0] = display4;
                display5 = d5;
            }
            Object[] d1 = new Object[WIDTH];
            Object[] d2 = new Object[WIDTH+1];
            Object[] d3 = new Object[WIDTH+1];
            Object[] d4 = new Object[WIDTH+1];
            display1 = d1;
            display2 = d2;
            display3 = d3;
            display4 = d4;
            int index = blockIndex;
            d2[(index >> BITS1) & MASK] = d1;
            d3[(index >> BITS2) & MASK] = d2;
            d4[(index >> BITS3) & MASK] = d3;
            d5[(index >> BITS4) & MASK] = d4;
        } else if (xor < WIDTH6) {
            Object[] createResult = display6;
            if (depth == 5) {
                depth = 6;
                createResult = new Object[WIDTH+1];
                createResult[0] = display5;
                display6 = createResult;
            }
            Object[] d1 = new Object[WIDTH];
            Object[] d2 = new Object[WIDTH+1];
            Object[] d3 = new Object[WIDTH+1];
            Object[] d4 = new Object[WIDTH+1];
            Object[] d5 = new Object[WIDTH+1];
            display1 = d1;
            display2 = d2;
            display3 = d3;
            display4 = d4;
            display5 = d5;
            int index = blockIndex;
            d2[(index >> BITS1) & MASK] = d1;
            d3[(index >> BITS2) & MASK] = d2;
            d4[(index >> BITS3) & MASK] = d3;
            d5[(index >> BITS4) & MASK] = d4;
            createResult[(index >> BITS5) & MASK] = d5;
        } else {
            throw new AssertionError("Unexpected xor");
        }
    }
}
