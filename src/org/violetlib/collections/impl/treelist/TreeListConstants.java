/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import org.violetlib.annotations.NoInstances;

/**

*/

final @NoInstances class TreeListConstants
{
    static final int BITS = 5;
    static final int WIDTH = 1 << BITS;
    static final int MASK = WIDTH - 1;
    static final int BITS2 = BITS * 2;
    static final int WIDTH2 = 1 << BITS2;
    static final int BITS3 = BITS * 3;
    static final int WIDTH3 = 1 << BITS3;
    static final int BITS4 = BITS * 4;
    static final int WIDTH4 = 1 << BITS4;
    static final int BITS5 = BITS * 5;
    static final int WIDTH5 = 1 << BITS5;
    static final int BITS6 = BITS * 6;
    static final int WIDTH6 = (1 << BITS6) + ((1 << BITS6) - 1);
    static final int LASTWIDTH = WIDTH << 1; // 1 extra bit in the last level to go up to Int.MaxValue (2^31-1) instead of 2^30:
}
