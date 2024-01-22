/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import java.util.Arrays;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**

*/

public final @NoInstances class TreeListUtils
{
    public static <T> T @NotNull [] copyIfDifferentSize(T @NotNull [] source, int size)
    {
        return source.length == size ? source : Arrays.copyOf(source, size);
    }

    public static <T> T @NotNull [] copyTail(T @NotNull [] source)
    {
        return Arrays.copyOfRange(source, 1, source.length);
    }


}
