/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.IMap;
import org.violetlib.collections.ISet;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**

*/

public final @NoInstances class MapEquality
{
    private MapEquality()
    {
        throw new AssertionError("MapEquality may not be instantiated");
    }

    public static boolean isEqual(@NotNull IMap<?,?> m1, @NotNull IMap<?,?> m2)
    {
        if (m1 == m2) {
            return true;
        }

        if (m1.size() != m2.size()) {
            return false;
        }

        ISet<?> keys1 = m1.keySet();
        ISet<?> keys2 = m2.keySet();
        if (!keys1.equals(keys2)) {
            return false;
        }

        IMap<Object,?> other = (IMap) m2;
        return (Boolean) m1.find((key, value) -> value.equals(other.get(key)) ? null : false, true);
    }

    public static <K,V> int computeHashCode(@NotNull IMap<K,V> map)
    {
        if (map.isEmpty()) {
            return 0;
        }

        MyHashCodeVisitor<K,V> visitor = new MyHashCodeVisitor<>();
        map.visit(visitor);
        return visitor.hashCode;
    }

    private static class MyHashCodeVisitor<K,V>
      implements IMap.Visitor<K,V>
    {
        private int hashCode = 0;

        @Override
        public void visit(@NotNull K key, @NotNull V value)
        {
            int entryCode = key.hashCode() ^ value.hashCode();
            hashCode += entryCode;
        }
    }
}
