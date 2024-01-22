/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.rrblist;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.*;

import static org.violetlib.collections.impl.rrblist.RRBVectorConstants.*;

/**
  An iterator for a relaxed radix balanced vector.
*/

public class RRBVectorIterator<V>
  extends MutableRRBVector<V>
  implements Iterator<V>
{
    public static <V> @NotNull Iterator<V> create(int startIndex, int endIndex, @NotNull RRBVector<V> that)
    {
        return new RRBVectorIterator<V>(startIndex, endIndex, that);
    }

    private final int endIndex;
    /* Index in the vector of the first element of current leaf node, i.e. current display0 */
    private int blockIndex;
    /* Index in the current leaf node, i.e. current display0 */
    private int lo;
    /* End index (or length) of current leaf node, i.e. current display0 */
    private int endLo;
    private boolean _hasNext;

    protected RRBVectorIterator(int startIndex, int endIndex, @NotNull RRBVector<V> that)
    {
        super(true, that);
        this.endIndex = endIndex;
        _hasNext = startIndex < endIndex;
        if (_hasNext) {
            focusOn(startIndex);
            blockIndex = focusStart + (focus & ANTI_MASK);
            lo = focus & MASK;
            if (endIndex < focusEnd) {
                focusEnd = endIndex;
            }
            endLo = Math.min(focusEnd - blockIndex, WIDTH);
        } else {
            blockIndex = 0;
            lo = 0;
            endLo = 1;
            display1 = new Object[1];
        }
    }

    @Override
    public boolean hasNext()
    {
        return _hasNext;
    }

    @Override
    public @NotNull V next()
    {
        int _lo = lo;
        V result = (V) display1[_lo];
        _lo += 1;
        lo = _lo;
        if (_lo == endLo) {
            gotoNextBlock();
        }
        return result;
    }

    private void gotoNextBlock()
    {
        int oldBlockIndex = blockIndex;
        int newBlockIndex = oldBlockIndex + endLo;
        blockIndex = newBlockIndex;
        lo = 0;
        int _focusEnd = focusEnd;
        if (newBlockIndex < _focusEnd) {
            int _focusStart = focusStart;
            int newBlockIndexInFocus = newBlockIndex - _focusStart;
            gotoNextBlockStart(newBlockIndexInFocus, newBlockIndexInFocus ^ (oldBlockIndex - _focusStart));
            endLo = Math.min(_focusEnd - newBlockIndex, WIDTH);
        } else {
            if (newBlockIndex < endIndex) {
                focusOn(newBlockIndex);
                if (endIndex < focusEnd) {
                    focusEnd = endIndex;
                }
                endLo = Math.min(focusEnd - newBlockIndex, WIDTH);
            } else {
                /* setup dummy index that will not fail with IndexOutOfBound in subsequent 'next()' invocations */
                lo = 0;
                blockIndex = endIndex;
                endLo = 1;
                if (_hasNext) {
                    _hasNext = false;
                } else {
                    throw new NoSuchElementException("reached iterator end");
                }
            }
        }
    }

    private int remaining()
    {
        return Math.max(endIndex - (blockIndex + lo), 0);
    }
}
