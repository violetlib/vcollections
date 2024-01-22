/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.util;

import java.util.NoSuchElementException;

import org.violetlib.collections.IIterator;

import org.jetbrains.annotations.*;

/**
  Iterators are easy to use but hard to implement. One key issue is that it may not be possible to determine if there is
  a next element until the next element has been identified. An iterator, however, needs to be able to do exactly that.
  This helper class simplifies the problem by buffering the next element. It can be used for iterators that do not
  return null as an element.

  <p>
  This class avoids asking for the next element unless the client requires it.

  @see SimpleIteratorWrapper

  @param <E> The type of the iteration elements. Null elements are not supported.
*/

public abstract class IteratorHelper<E>
  implements IIterator<E>
{
    private boolean atEnd = false;
    private @Nullable E nextElement = null;

    protected IteratorHelper()
    {
    }

    @Override
    public boolean hasNext()
    {
        if (atEnd) {
            return false;
        }

        if (nextElement == null) {
            nextElement = provide();
            if (nextElement == null) {
                atEnd = true;
                return false;
            }
        }

        return true;
    }

    @Override
    public @NotNull E next()
    {
        /*
          The next() method must function whether or not hasNext() is called. Hence, there is a fair amount of code
          duplication.
        */

        if (atEnd) {
            throw new NoSuchElementException();
        }

        if (nextElement == null) {
            nextElement = provide();
            if (nextElement == null) {
                atEnd = true;
                throw new NoSuchElementException();
            }
        }

        E e = nextElement;
        nextElement = null;
        return e;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("This iterator does not support remove.");
    }

    /**
      This subclass hook method returns the next available element. This class ensures that this method is not called
      again after it returns null.

      @return the next available element, or null if there are no more.
    */

    protected abstract @Nullable E provide();
}
