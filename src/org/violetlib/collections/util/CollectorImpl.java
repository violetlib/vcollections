/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.util;

import org.jetbrains.annotations.NotNull;
import org.violetlib.annotations.Immutable;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
  * Simple implementation class for {@code Collector}.
  *
  * @param <T> the type of elements to be collected
 * @param <R> the type of the result
*/

public final @Immutable class CollectorImpl<T,A,R> implements Collector<T,A,R>
{
    public static <T,A,R> Collector<T,A,R> create(@NotNull Supplier<A> supplier,
                                                  @NotNull BiConsumer<A,T> accumulator,
                                                  @NotNull BinaryOperator<A> combiner,
                                                  @NotNull Function<A,R> finisher,
                                                  @NotNull Set<Characteristics> characteristics)
    {
        return new CollectorImpl<>(supplier, accumulator, combiner, finisher, characteristics);
    }

    private final @NotNull Supplier<A> supplier;
    private final @NotNull BiConsumer<A,T> accumulator;
    private final @NotNull BinaryOperator<A> combiner;
    private final @NotNull Function<A,R> finisher;
    private final @NotNull Set<Characteristics> characteristics;

    private CollectorImpl(@NotNull Supplier<A> supplier,
                          @NotNull BiConsumer<A,T> accumulator,
                          @NotNull BinaryOperator<A> combiner,
                          @NotNull Function<A,R> finisher,
                          @NotNull Set<Characteristics> characteristics)
    {
        this.supplier = supplier;
        this.accumulator = accumulator;
        this.combiner = combiner;
        this.finisher = finisher;
        this.characteristics = characteristics;
    }

    @Override
    public @NotNull BiConsumer<A,T> accumulator()
    {
        return accumulator;
    }

    @Override
    public @NotNull Supplier<A> supplier()
    {
        return supplier;
    }

    @Override
    public @NotNull BinaryOperator<A> combiner()
    {
        return combiner;
    }

    @Override
    public @NotNull Function<A,R> finisher()
    {
        return finisher;
    }

    @Override
    public @NotNull Set<Characteristics> characteristics()
    {
        return characteristics;
    }
}
