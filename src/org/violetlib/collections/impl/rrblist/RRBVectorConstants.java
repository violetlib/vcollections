/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.rrblist;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**
  Constants used in the implementation of a relaxed radix balanced vector.
*/

public final @NoInstances class RRBVectorConstants
{
    public static final int WIDTH = 32;
    public static final int WIDTH1 = WIDTH;
    public static final int WIDTH2 = WIDTH * WIDTH;
    public static final int WIDTH3 = WIDTH * WIDTH * WIDTH;
    public static final int WIDTH4 = WIDTH * WIDTH * WIDTH * WIDTH;
    public static final int WIDTH5 = WIDTH * WIDTH * WIDTH * WIDTH * WIDTH;
    public static final int WIDTH6 = WIDTH * WIDTH * WIDTH * WIDTH * WIDTH * WIDTH;

    public static final int BITS1 = 5;
    public static final int BITS2 = BITS1 * 2;
    public static final int BITS3 = BITS1 * 3;
    public static final int BITS4 = BITS1 * 4;
    public static final int BITS5 = BITS1 * 5;

    public static final int MASK = WIDTH-1;
    public static final int ANTI_MASK = -WIDTH;

    /**
      Return the tree depth required for a newly create vector of the specified size.
      @param size The vector size.
      @return the required depth.
    */

    public static int depthForSize(int size)
      throws IllegalArgumentException, UnsupportedOperationException
    {
        if (size < 0) {
            throw new IllegalArgumentException("Invalid vector size: " + size);
        }
        if (size <= WIDTH) {
            return 1;
        }
        if (size <= WIDTH2) {
            return 2;
        }
        if (size <= WIDTH3) {
            return 3;
        }
        if (size <= WIDTH4) {
            return 4;
        }
        if (size <= WIDTH5) {
            return 5;
        }
        if (size <= WIDTH6) {
            return 6;
        }
        throw new UnsupportedOperationException("Unsupported vector size: " + size);
    }

    public static int @Nullable [] getSizes(int level, Object @NotNull [] a)
    {
        if (level < 2) {
            return null;
        }
        return getSizes(a);
    }

    public static int @Nullable [] getSizes(Object @NotNull [] a)
    {
        int length = a.length;
        if (length == 0) {
            return null;
        }
        Object o = a[length-1];
        if (o == null) {
            return null;
        }
        if (o instanceof int[]) {
            return (int[]) o;
        }
        return null;
    }

    public static void installSizes(Object @NotNull [] a, int @Nullable [] sizes)
    {
        int length = a.length;
        assert length > 0;
        a[length-1] = sizes;
    }

    public static int getSubtreeSize(int @NotNull [] sizes, int offset)
    {
        int n = sizes[offset];
        return offset > 0 ? n - sizes[offset-1] : n;
    }

    public static int getMaximumTreeSize(int level)
    {
        switch (level) {
            case 1: return WIDTH;
            case 2: return WIDTH2;
            case 3: return WIDTH3;
            case 4: return WIDTH4;
            case 5: return WIDTH5;
            case 6: return WIDTH6;
            default: throw new AssertionError("Unexpected level: " + level);
        }
    }
}
