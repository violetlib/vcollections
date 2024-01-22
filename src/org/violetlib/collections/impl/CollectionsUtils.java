/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.List;

import org.violetlib.collections.ICollection;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**

*/

public final @NoInstances class CollectionsUtils
{
    private CollectionsUtils()
    {
        throw new AssertionError("CollectionsUtils may not be instantiated");
    }

    /**
      Return the size of the specified iterable collection, if it can be determined efficiently.
      @param source The collection.
      @return the size of the collection, or -1 if the size cannot be determined efficiently.
    */

    public static int getKnownSize(@NotNull Iterable<?> source)
    {
        ICollection<?> collection = Extensions.getExtension(source, ICollection.class);
        if (collection != null) {
            return collection.size();
        }

        List<?> list = Extensions.getExtension(source, List.class);
        if (list != null) {
            return list.size();
        }

        if (source.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(source);
        }

        return -1;
    }
}
