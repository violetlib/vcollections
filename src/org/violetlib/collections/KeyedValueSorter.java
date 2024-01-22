/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import java.util.Comparator;

import org.jetbrains.annotations.*;

/**

*/

public interface KeyedValueSorter<K,V>
{
    @NotNull Comparator<? super V> getComparatorForKey(@NotNull K key);
}
