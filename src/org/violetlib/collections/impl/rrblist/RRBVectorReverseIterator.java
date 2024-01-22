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
  A reverse-order iterator for a relaxed radix balanced vector.
*/

public class RRBVectorReverseIterator<V>
  extends MutableRRBVector<V>
  implements Iterator<V>
{
    public static <V> @NotNull Iterator<V> create(int startIndex, int endIndex, @NotNull MutableRRBVector<V> that)
    {
        return new RRBVectorReverseIterator<>(startIndex, endIndex, that);
    }

    private final int startIndex;
    private int lastIndexOfBlock;
    private int lo;
    private int endLo;
    private boolean _hasNext;

    protected RRBVectorReverseIterator(int startIndex, int endIndex, @NotNull MutableRRBVector<V> that)
    {
        super(true, that);
        this.startIndex = startIndex;
        this.endIndex = Math.min(endIndex, this.endIndex);
        _hasNext = startIndex < endIndex;
        if (_hasNext) {
            int idx = endIndex - 1;
            focusOn(idx);
            assert focus == idx - focusStart;
            lo = focus & MASK;
            int firstIndexInBlock = focusStart + (focus & ANTI_MASK);
            endLo = Math.max(0, startIndex - firstIndexInBlock);
            lastIndexOfBlock = firstIndexInBlock + display1.length - 1;
        } else {
            lastIndexOfBlock = 0;
            lo = 0;
            endLo = 0;
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
        // TODO push the check of _hasNext and the throwing of the NoSuchElementException into gotoPrevBlock() like in the normal RRBVectorIterator
        if (_hasNext) {
            int _lo = lo;
            V result = (V) display1[_lo];
            _lo -= 1;
            lo = _lo;
            if (_lo < endLo) {
                gotoPrevBlock();
            }
            return result;
        } else
            throw new NoSuchElementException();
    }

    private void gotoPrevBlock()
    {
        int lastIndexInNewBlock = lastIndexOfBlock - display1.length;
        if (focusStart <= lastIndexInNewBlock) {
            int newBlockIndexInFocus = lastIndexInNewBlock - focusStart;
            gotoPrevBlockStart(newBlockIndexInFocus, newBlockIndexInFocus ^ (lastIndexOfBlock - focusStart));
            lastIndexOfBlock = lastIndexInNewBlock;
            if (startIndex > lastIndexOfBlock) {
                _hasNext = false;
                return;
            }
            int firstIndexInBlock = lastIndexInNewBlock - display1.length + 1;
            lo = display1.length - 1;
            endLo = Math.max(0, startIndex - firstIndexInBlock);
        } else if (startIndex < focusStart) {
            focusOn(lastIndexInNewBlock);
            lastIndexOfBlock = lastIndexInNewBlock;
            int firstIndexInBlock = lastIndexInNewBlock - display1.length + 1;
            lo = display1.length - 1;
            endLo = Math.max(0, startIndex - firstIndexInBlock);
        } else {
            _hasNext = false;
        }
    }
}
