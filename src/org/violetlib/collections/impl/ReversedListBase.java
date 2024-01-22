/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.violetlib.collections.IList;

import org.jetbrains.annotations.*;

/**
  A base class for a reversed list.
*/

public abstract class ReversedListBase<V,DELEGATE extends IList<V>>
  implements IList<V>
{
    protected final @NotNull DELEGATE delegate;

    protected ReversedListBase(@NotNull DELEGATE delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }

    @Override
    public int size()
    {
        return delegate.size();
    }

    @Override
    public boolean contains(@NotNull Object target)
    {
        return delegate.contains(target);
    }

    @Override
    public @Nullable V optionalFirst()
    {
        return delegate.optionalLast();
    }

    @Override
    public @Nullable V optionalLast()
    {
        return delegate.optionalFirst();
    }
}
