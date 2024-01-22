/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl.treelist;

import java.util.Collection;
import java.util.List;

import org.violetlib.collections.IList;
import org.violetlib.util.Extensions;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**

*/

public final @NoInstances class IterableSupport
{
    private IterableSupport()
    {
    }

    /**
      Return the size of the iterable source. Determining the size may require performing the iteration.
      @param source The source.
      @return the size of the source.
    */

    public static int count(@NotNull Iterable<?> source)
    {
        int count = quickCount(source);
        if (count >= 0) {
            return count;
        }
        int result = 0;
        for (Object value : source) {
            ++result;
        }
        return result;
    }

    /**
      Return the size of the iterable source, if readily available.
      @param source The source.
      @return the size of the source, or -1 if the size cannot be determined.
    */

    public static int quickCount(@NotNull Iterable<?> source)
    {
        IList<?> ilist = Extensions.getExtension(source, IList.class);
        if (ilist != null) {
            return ilist.size();
        }

        List<?> list = Extensions.getExtension(source, List.class);
        if (list != null) {
            return list.size();
        }

        Collection<?> collection = Extensions.getExtension(source, Collection.class);
        if (collection != null) {
            return collection.size();
        }

        return -1;
    }

    /**
      Return the first value from an iterable source. Determining the value may require starting the iteration.
      @param source The source.
      @return the first value from the source, or null if the source is empty.
    */

    public static @Nullable Object first(@NotNull Iterable<?> source)
    {
        IList<?> ilist = Extensions.getExtension(source, IList.class);
        if (ilist != null) {
            return ilist.first();
        }

        List<?> list = Extensions.getExtension(source, List.class);
        if (list != null) {
            return list.get(0);
        }

        for (Object o : source) {
            return o;
        }
        return null;
    }
}
