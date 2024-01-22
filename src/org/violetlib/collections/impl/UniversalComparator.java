/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Comparator;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  A comparator that uses the natural sort order defined by list elements.
*/

public final @Immutable class UniversalComparator
  implements Comparator<Object>
{
    public static @NotNull Comparator<Object> get()
    {
        return INSTANCE;
    }

    private static final @NotNull UniversalComparator INSTANCE = new UniversalComparator();

    private UniversalComparator()
    {
    }

    @Override
    public int compare(@NotNull Object a, @NotNull Object b)
    {
        if (a instanceof Comparable) {
            Comparable<Object> c = (Comparable) a;
            return c.compareTo(b);
        } else {
            return -1;
        }
    }
}
