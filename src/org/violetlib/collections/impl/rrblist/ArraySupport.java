/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.rrblist;

import java.lang.reflect.Array;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**

*/

public final @NoInstances class ArraySupport
{
    private ArraySupport()
    {
    }

    public static <T> T @NotNull [] copyOf(T @NotNull [] a, int nElements, int newSize)
    {
        Object[] result = (Object[]) Array.newInstance(a.getClass().getComponentType(), newSize);
        System.arraycopy(a, 0, result, 0, nElements);
        return (T[]) result;
    }

    public static <T> T @NotNull [] copyOf(T @NotNull [] a)
    {
        int len = a.length;
        Object[] result = (Object[]) Array.newInstance(a.getClass().getComponentType(), len);
        System.arraycopy(a, 0, result, 0, len);
        return (T[]) result;
    }

    public static Object @NotNull [] copyPrepend1(@NotNull Object element, Object @NotNull [] data)
    {
        int len = data.length;
        Object[] result = (Object[]) Array.newInstance(data.getClass().getComponentType(), len+1);
        System.arraycopy(data, 0, result, 1, len);
        result[0] = element;
        return result;
    }

    public static Object @NotNull [] copyAppend1(Object @NotNull [] data, @NotNull Object element)
    {
        int len = data.length;
        Object[] result = (Object[]) Array.newInstance(data.getClass().getComponentType(), len+1);
        System.arraycopy(data, 0, result, 0, len);
        result[len] = element;
        return result;
    }
}
