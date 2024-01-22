/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.ISet;
import org.violetlib.collections.Visitor;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**

*/

public final @NoInstances class SetEquality
{
    private SetEquality()
    {
        throw new AssertionError("SetEquality may not be instantiated");
    }

    public static boolean isEqual(@NotNull ISet<?> s1, @NotNull ISet<?> s2)
    {
        if (s1 == s2) {
            return true;
        }

        if (s1.size() != s2.size()) {
            return false;
        }

        Boolean result = (Boolean) s1.find(e -> s2.contains(e) ? null : false);
        return result == null;
    }

    public static <V> int computeHashCode(@NotNull ISet<V> set)
    {
        if (set.isEmpty()) {
            return 0;
        }

        MyHashCodeVisitor<V> visitor = new MyHashCodeVisitor<>();
        set.visit(visitor);
        return visitor.hashCode;
    }

    private static class MyHashCodeVisitor<V>
      implements Visitor<V>
    {
        private int hashCode = 0;

        @Override
        public void visit(@NotNull V element)
        {
            hashCode += element.hashCode();
        }
    }
}
