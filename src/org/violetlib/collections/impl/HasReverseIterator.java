/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.IIterator;

import org.jetbrains.annotations.*;

/**

*/

public interface HasReverseIterator<V>
{
    @NotNull IIterator<V> reverseIterator();
}
