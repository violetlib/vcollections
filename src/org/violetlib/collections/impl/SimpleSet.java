/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetlib.annotations.Immutable;
import org.violetlib.collections.*;
import org.violetlib.util.Extensions;

import java.util.*;
import java.util.function.Function;

import static java.util.Spliterator.*;

/**

*/

public final @Immutable class SimpleSet<V>
  implements ISet<V>
{
    @SuppressWarnings("unchecked")
    public static <V> @NotNull SimpleSet<V> empty()
    {
        return (SimpleSet<V>) EMPTY;
    }

    private static final SimpleSet<Object> EMPTY = new SimpleSet<>();

    private final @NotNull HashSet<V> elements;

    private SimpleSet()
    {
        elements = new HashSet<>();
    }

    @SafeVarargs
    public static <V> @NotNull SimpleSet<V> create(@NotNull V... elements)
      throws IllegalArgumentException
    {
        return elements.length == 0 ? empty() : createWithElements(elements);
    }

    @SuppressWarnings("unchecked")
    public static <V> @NotNull SimpleSet<V> fromSet(@NotNull Set<? extends V> values)
    {
        if (values.isEmpty()) {
            return empty();
        } else {
            return createWithElements(values);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> @NotNull SimpleSet<V> fromList(@NotNull IList<? extends V> values)
    {
        if (values.isEmpty()) {
            return empty();
        } else {
            return createWithElements(values);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> @NotNull SimpleSet<V> collect(@NotNull Iterable<? extends V> values)
      throws IllegalArgumentException
    {
        if (values instanceof SimpleSet) {
            return (SimpleSet) values;
        } else if (values instanceof ICollection) {
            return createWithElements((ICollection) values);
        } else {
            HashSet<V> s = new HashSet<>();
            for (V v : values) {
                if (v == null) {
                    throw new IllegalArgumentException("Null elements are not permitted");
                }
                s.add(v);
            }
            return new SimpleSet<>(true, s);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> @NotNull SimpleSet<V> fromJavaCollection(@NotNull Collection<? extends V> values)
      throws IllegalArgumentException
    {
        if (values.isEmpty()) {
            return empty();
        } else {
            return createWithElements(values);
        }
    }

    @SuppressWarnings("unchecked")
    private static <V> @NotNull SimpleSet<V> createWithElements(Object @NotNull [] values)
      throws IllegalArgumentException
    {
        HashSet<V> s = new HashSet<>();
        for (Object value : values) {
            if (value == null) {
                throw new IllegalArgumentException("Null elements are not permitted");
            }
            s.add((V) value);
        }
        return new SimpleSet<>(true, s);
    }

    private static <V> @NotNull SimpleSet<V> createWithElements(@NotNull Collection<? extends V> values)
      throws IllegalArgumentException
    {
        HashSet<V> s = new HashSet<>(values);
        return new SimpleSet<>(true, s);
    }

    private static <V> @NotNull SimpleSet<V> createWithElements(@NotNull ICollection<? extends V> values)
    {
        HashSet<V> s = new HashSet<>();
        values.visit(s::add);
        return new SimpleSet<>(true, s);
    }

    @SuppressWarnings("unchecked")
    private SimpleSet(boolean fake, @NotNull HashSet<V> values)
      throws IllegalArgumentException
    {
        elements = new HashSet<>();
        for (Object value : values) {
            if (value == null) {
                throw new IllegalArgumentException("Null elements are not permitted");
            }
            elements.add((V) value);
        }
    }

    @Override
    public int size()
    {
        return elements.size();
    }

    @Override
    public boolean contains(@NotNull Object target)
    {
        return elements.contains(target);
    }

    @Override
    public @NotNull IIterator<V> iterator()
    {
        return IIterator.from(elements.iterator());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(@NotNull Visitor<V> visitor)
    {
        for (Object o : elements) {
            visitor.visit((V) o);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> @Nullable R find(@NotNull FindVisitor<V,R> visitor)
    {
        for (Object e : elements) {
            R result = visitor.visit((V) e);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public @NotNull ISet<V> extending(@NotNull V value)
    {
        if (!elements.contains(value)) {
            HashSet<V> copy = new HashSet<>(elements);
            copy.add(value);
            return new SimpleSet<>(true, copy);
        } else {
            return this;
        }
    }

    @Override
    public @NotNull ISet<V> extendingAll(@NotNull ICollection<? extends V> values)
    {
        HashSet<V> copy = new HashSet<>(elements);
        for (V v : values) {
            copy.add(v);
        }
        return new SimpleSet<>(true, copy);
    }

    @Override
    public @NotNull ISet<V> removing(@NotNull Object value)
    {
        if (elements.contains(value)) {
            HashSet<V> copy = new HashSet<>(elements);
            copy.remove(value);
            return new SimpleSet<>(true, copy);
        } else {
            return this;
        }
    }

    @Override
    public @NotNull ISet<V> removingAll(@NotNull ISet<?> values)
    {
        if (containsAny(values)) {
            HashSet<V> copy = new HashSet<>(elements);
            for (Object v : values) {
                copy.remove(v);
            }
            return new SimpleSet<>(true, copy);
        } else {
            return this;
        }
    }

    private boolean containsAny(@NotNull ISet<?> values)
    {
        for (Object v : values) {
            if (elements.contains(v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <R> @NotNull ISet<R> map(@NotNull Function<V,R> mapper)
    {
        HashSet<R> result = new HashSet<>();
        visit(v -> {
            R replacement = mapper.apply(v);
            if (replacement != null) {
                result.add(replacement);
            }
        });
        if (result.isEmpty()) {
            return empty();
        } else {
            return new SimpleSet<>(true, result);
        }
    }

    @Override
    public @NotNull Spliterator<V> spliterator()
    {
        return Spliterators.spliterator(iterator(), size(), IMMUTABLE | NONNULL | SIZED | DISTINCT);
    }

    @Override
    public int hashCode()
    {
        return SetEquality.computeHashCode(this);
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        ISet<?> otherSet = Extensions.getExtension(obj, ISet.class);
        if (otherSet == null) {
            return false;
        }

        return SetEquality.isEqual(this, otherSet);
    }

    @Override
    public @NotNull String toString()
    {
        StringBuilder b = new StringBuilder();
        b.append('{');
        for (V e : this) {
            if (b.length() > 1) {
                b.append(' ');
            }
            b.append(e.toString());
        }
        b.append('}');
        return b.toString();
    }
}
