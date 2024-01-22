/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import org.violetlib.collections.impl.ListImplSupport;

import org.jetbrains.annotations.*;

/**
  A visitor of elements in a collection that can short-circuit the iteration by returning a (non-null) result.

  @param <E> The type of the elements.

  @param <R> The type of the value returned by the visitor.
*/

public interface FindVisitor<E,R>
{
    /**
      Return a find visitor that calls an indexed find visitor.
      The indexed find visitor receives indexes in increasing order.
    */

    static <V,R> @NotNull FindVisitor<V,R> fromIndexedFindVisitor(@NotNull IndexedFindVisitor<V,R> v)
    {
        return ListImplSupport.fromIndexedFindVisitor(v);
    }

    @Nullable R visit(@NotNull E element);
}
