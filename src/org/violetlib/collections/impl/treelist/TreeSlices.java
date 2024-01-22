/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import org.jetbrains.annotations.*;

/**

*/

public interface TreeSlices
{
    int getSliceCount();

    Object @NotNull [] getSlice(int sliceIndex);

    int getSlicePrefixLength(int sliceIndex);

    int getSliceDepth(int sliceIndex);

    int getSliceElementCount(int sliceIndex);
}
