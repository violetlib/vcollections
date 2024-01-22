/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Iterator;

import org.violetlib.collections.IList;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**

*/

public final @NoInstances class ListEquality
{
    private ListEquality()
    {
        throw new AssertionError("ListEquality may not be instantiated");
    }

    public static boolean isEqual(@NotNull IList<?> l1, @NotNull IList<?> l2)
    {
        if (l1 == l2) {
            return true;
        }

        if (l1.size() != l2.size()) {
            return false;
        }

        Iterator<?> it1 = l1.iterator();
        Iterator<?> it2 = l2.iterator();
        for (;;) {
            if (it1.hasNext()) {
                if (!it2.hasNext()) {
                    return false;
                }
            } else {
                return !it2.hasNext();
            }

            Object e1 = it1.next();
            Object e2 = it2.next();
            if (!e1.equals(e2)) {
                return false;
            }
        }
    }

    public static int computeHashCode(@NotNull IList<?> list)
    {
        int hashCode = 1;
        for (Object e : list) {
            hashCode = hashCode * 31 + e.hashCode();
        }

        return hashCode;
    }
}
