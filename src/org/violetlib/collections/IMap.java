/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.CheckReturnValue;

import org.violetlib.collections.impl.EmptyMap;
import org.violetlib.collections.impl.Impl;
import org.violetlib.types.Option;
import org.violetlib.types.UndefinedValueError;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  An immutable map. Null keys and values are not permitted.

  @param <K> The type of the keys.
  @param <V> The type of the values.
*/

public @Immutable @CheckReturnValue interface IMap<K,V>
  extends IIterable<Binding<K,V>>
{
    @NotNull Option ORDERED = Option.named("Ordered Map");

    interface Visitor<K,V>
    {
        void visit(@NotNull K key, @NotNull V value);
    }

    interface FVisitor<K,V,R>
    {
        @Nullable R visit(@NotNull K key, @NotNull V element);
    }

    /**
      Return a map containing no bindings.
    */

    static <K,V> @NotNull IMap<K,V> empty()
    {
        return EmptyMap.get();
    }

    /**
      Return a map containing one binding.
    */

    static <K,V> @NotNull IMap<K,V> singleton(@NotNull K key, @NotNull V value)
    {
        return Impl.createSingletonMap(key, value);
    }

    /**
      Return a map containing one binding.
    */

    static <K,V> @NotNull IMap<K,V> of(@NotNull Binding<? extends K, ? extends V> binding)
    {
        return Impl.createSingletonMap(binding.getKey(), binding.getValue());
    }

    /**
      Return a new map builder.
    */

    static <K,V> @NotNull MapBuilder<K,V> builder()
    {
        return Impl.getMapBuilder();
    }

    /**
      Return a new map builder.
      @param option If option is {@link #ORDERED}, the iteration order of the map will be based on the order in which
      the keys were first added.
    */

    static <K,V> @NotNull MapBuilder<K,V> builder(@NotNull Option option)
    {
        return option == ORDERED ? Impl.getOrderedMapBuilder() : Impl.getMapBuilder();
    }

    /**
      Return a new map list builder.
    */

    static <K,V> @NotNull UnorderedMapListBuilder<K,V> mapListBuilder()
    {
        return Impl.getMapListBuilder();
    }

    /**
      Return a new map list builder.
      @param option If option is {@link #ORDERED}, the iteration order of the map list will be based on the order in
      which keys and elements were first added.
    */

    static <K,V> @NotNull MapListBuilder<K,V> mapListBuilder(@NotNull Option option)
    {
        return option == ORDERED ? Impl.getOrderedMapListBuilder() : Impl.getMapListBuilder();
    }

    /**
      Return a new map set builder.
    */

    static <K,V> @NotNull MapSetBuilder<K,V> mapSetBuilder()
    {
        return Impl.getMapSetBuilder();
    }

    /**
      Return a new map set builder.
      @param option If option is {@link #ORDERED}, the iteration order of the map set will be based on the order in
      which keys and elements were first added.
    */

    static <K,V> @NotNull MapSetBuilder<K,V> mapSetBuilder(@NotNull Option option)
    {
        return option == ORDERED ? Impl.getMapOrderedSetBuilder() : Impl.getMapSetBuilder();
    }

    /**
      Return a map containing the specified bindings.
    */

    static <K,V> @NotNull IMap<K,V> create(@NotNull Map<? extends K,? extends V> map)
    {
        return Impl.createMap(map);
    }

    /**
      Return a map containing the specified bindings using weak references to the keys.
    */

    static <K,V> @NotNull IMap<K,V> createWeak(@NotNull Map<? extends K,? extends V> map)
    {
        return Impl.createWeakMap(map);
    }

    /**
      Cast a map to a specific type. This method is no more reliable than an explicit type cast, but it prevents the
      warning.
    */

    static <K,V> @NotNull IMap<K,V> cast(@NotNull IMap<?,?> o)
    {
        @SuppressWarnings("unchecked")
        IMap<K,V> result = (IMap) o;
        return result;
    }

    /**
      Return true if and only if the map contains no keys.
    */

    boolean isEmpty();

    /**
      Return the number of keys in the map.
    */

    int size();

    /**
      Return the value associated with the specified key.
      @param key The key.
      @return the value associated with {@code key}, or null if none.
    */

    @Nullable V get(@NotNull K key);

    default @NotNull V getRequired(@NotNull K key)
    {
        V value = get(key);
        if (value == null) {
            throw UndefinedValueError.create("No value for key");
        }
        return value;
    }

    boolean containsKey(@NotNull Object key);

    void visit(@NotNull IMap.Visitor<K,V> visitor);

    <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor, @Nullable R defaultResult);

    @NotNull ISet<K> keySet();

    @NotNull ISet<V> values();

    /**
      Return a map with a possibly updated binding for the specified key. The returned map will have the same bindings
      for other keys as this map.

      @param key The key whose value is specified.
      @param value The value to be associated with {@code key}. If null, the returned map will have no binding for
      {@code key}.
      @return A map with the existing bindings from this map, plus the specified binding (or lack thereof) for the key
      {@code key}.
    */

    @NotNull IMap<K,V> extending(@NotNull K key, @Nullable V value);

    /**
      Return a map with possibly additional or updated bindings.

      @param bindings The bindings to be added to or to replace bindings in this map.
      @return A map with the existing bindings from this map, updated by the specified bindings.
    */

    @NotNull IMap<K,V> extending(@NotNull IMap<K,V> bindings);

    /**
      Return a map containing the bindings for the specified keys (if defined).
      @param keys The keys whose bindings are to be returned.
      @return A map containing the existing bindings for {@code keys} in this map.
    */

    default IMap<K,V> subset(@NotNull ISet<K> keys)
    {
        MapBuilder<K,V> b = IMap.builder();
        for (K key : keys) {
            b.putOptional(key, get(key));
        }
        return b.value();
    }

    /**
      Return a new Java map containing the bindings of this map.
    */

    default @NotNull Map<K,V> asJavaMap()
    {
        Map<K,V> result = new HashMap<>();
        visit(result::put);
        return result;
    }
}
