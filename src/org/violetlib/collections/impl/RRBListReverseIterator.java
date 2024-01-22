/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.IIterator;
import org.violetlib.collections.impl.rrblist.MutableRRBVector;
import org.violetlib.collections.impl.rrblist.RRBVectorReverseIterator;

import org.jetbrains.annotations.*;

/**

*/

public final class RRBListReverseIterator<V>
  extends RRBVectorReverseIterator<V>
  implements IIterator<V>
{
    public static <V> @NotNull IIterator<V> create(int startIndex, int endIndex, @NotNull MutableRRBVector<V> that)
    {
        return new RRBListReverseIterator<>(startIndex, endIndex, that);
    }

    private RRBListReverseIterator(int startIndex, int endIndex, @NotNull MutableRRBVector<V> that)
    {
        super(startIndex, endIndex, that);
    }
}
