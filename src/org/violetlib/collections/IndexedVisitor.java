/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import org.jetbrains.annotations.*;

/**
  A visitor of elements in a list that receives elements along with their indexes.
*/

public interface IndexedVisitor<V>
{
    void visit(int index, @NotNull V element);
}
