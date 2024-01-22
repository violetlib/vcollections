/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.violetlib.collections.IIterator;
import org.violetlib.collections.IList;
import org.violetlib.collections.ListBuilder;
import org.violetlib.collections.impl.SafeIteratorWrapper;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.NoInstances;

/**
  A source of iterators constructed from multiple sources. Sources are iterables or specific elements.
*/

public final @NoInstances class CompositeIterator
{
    public static <E> @NotNull Builder<E> builder()
    {
        return new MyBuilder<>();
    }

    public interface Builder<E>
    {
        void addIterator(@NotNull Iterator<E> iterator);
        void addElement(@NotNull E element);
        @NotNull IIterator<E> value();
    }

    private static class MyBuilder<E>
      implements Builder<E>
    {
        private ListBuilder<Source<E>> b = IList.builder();
        private boolean isSafe = true;

        @Override
        public void addIterator(@NotNull Iterator<E> source)
        {
            b.add(new IteratorSource<E>(source));

            if (isSafe && !(source instanceof IIterator)) {
                isSafe = false;
            }
        }

        @Override
        public void addElement(@NotNull E element)
        {
            b.add(new ElementSource<>(element));
        }

        @Override
        public @NotNull IIterator<E> value()
        {
            IList<Source<E>> sources = b.values();
            return isSafe ? (IIterator) new MyIterator<E>(sources) : SafeIteratorWrapper.wrap(new MyIterator<E>(sources));
        }
    }

    private abstract static class Source<E>
    {
        public abstract @Nullable Iterator<E> getIterator();
        public abstract @Nullable E getElement();
    }

    private static class IteratorSource<E>
      extends Source<E>
    {
        private final @NotNull Iterator<E> source;

        public IteratorSource(@NotNull Iterator<E> source)
        {
            this.source = source;
        }

        @Override
        public @NotNull Iterator<E> getIterator()
        {
            return source;
        }

        @Override
        public @Nullable E getElement()
        {
            return null;
        }
    }

    private static class ElementSource<E>
      extends Source<E>
    {
        private final @NotNull E element;

        public ElementSource(@NotNull E element)
        {
            this.element = element;
        }

        @Override
        public @Nullable Iterator<E> getIterator()
        {
            return null;
        }

        @Override
        public @NotNull E getElement()
        {
            return element;
        }
    }

    private CompositeIterator()
    {
    }

    private static class MyIterator<E>
      implements Iterator<E>
    {
        private final @NotNull IList<Source<E>> sources;
        private int currentSourceIndex = -1;
        private int count;
        private @Nullable Iterator<E> currentIterator;
        private @Nullable E currentElement;

        public MyIterator(@NotNull IList<Source<E>> sources)
        {
            this.sources = sources;
            this.count = sources.size();
            advanceToNextAvailable();
        }

        public boolean hasNext()
        {
            return currentIterator != null || currentElement != null;
        }

        public @NotNull E next()
        {
            E result;
            if (currentElement != null) {
                result = currentElement;
            } else if (currentIterator != null) {
                result = currentIterator.next();
            } else {
                throw new NoSuchElementException();
            }

            advanceToNextAvailable();
            return result;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        // Exactly one of the following conditions hold after this method returns:
        // 1. currentIterator and currentElement are null and iteration is complete
        // 2. currentElement is not null, currentIterator is null, and currentElement is the next iteration result
        // 3. currentElement is null, currentIterator is not null, currentIterator has a next element, and that
        //    element is the next iteration result

        private void advanceToNextAvailable()
        {
            while (true) {
                // Determine if the current source has more elements.
                // If it does, then do not advance.
                // Only an iterator can have another element.
                if (currentIterator != null && currentIterator.hasNext()) {
                    return;
                }

                // advance to the next source, if there is one
                currentElement = null;
                currentIterator = null;
                ++currentSourceIndex;
                if (currentSourceIndex >= count) {
                    return;
                }

                Source<E> s = sources.get(currentSourceIndex);
                currentIterator = s.getIterator();
                if (currentIterator == null) {
                    currentElement = s.getElement();
                    return;
                }
            }
        }
    }
}
