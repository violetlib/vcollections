/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.IIterator;
import org.violetlib.collections.impl.rrblist.RRBVector;
import org.violetlib.collections.impl.rrblist.RRBVectorIterator;

import org.jetbrains.annotations.*;

/**

*/

public final class RRBListIterator<V>
  extends RRBVectorIterator<V>
  implements IIterator<V>
{
    public static <V> @NotNull IIterator<V> create(int startIndex, int endIndex, @NotNull RRBVector<V> that)
    {
        return new RRBListIterator<>(startIndex, endIndex, that);
    }

    private RRBListIterator(int startIndex, int endIndex, @NotNull RRBVector<V> that)
    {
        super(startIndex, endIndex, that);
    }
}
