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
  A visitor of elements in a collection.

  @param <E> The type of the elements.
*/

public interface Visitor<E>
{
    /**
      Return a visitor that calls an indexed visitor.
      The indexed visitor receives indexes in increasing order.
    */

    static <V> @NotNull Visitor<V> fromIndexedVisitor(@NotNull IndexedVisitor<V> v)
    {
        return ListImplSupport.fromIndexedVisitor(v);
    }

    void visit(@NotNull E element);
}
