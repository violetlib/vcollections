/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.ICollection;
import org.violetlib.collections.IIterable;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;

import org.jetbrains.annotations.*;

/**

*/

public final class ICollectionSequence<E>
  implements ICollection<E>
{
    public static <E> @NotNull ICollection<E> create(@NotNull IList<? extends ICollection<E>> sources)
    {
        return new ICollectionSequence<>(sources);
    }

    private final @NotNull IList<ICollection<E>> sources;

    private ICollectionSequence(@NotNull IList<? extends ICollection<E>> sources)
    {
        this.sources = IList.cast(sources);
    }

    @Override
    public @NotNull IIterator<E> iterator()
    {
        return SimpleCompositeIterator.create(sources.map(IIterable::iterator));
    }
}
