/*
 * Copyright (c) 2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.collections.impl;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.violetlib.collections.Binding;
import org.violetlib.collections.IIterator;
import org.violetlib.collections.IMap;
import org.violetlib.collections.ISet;
import org.violetlib.collections.SetBuilder;
import org.violetlib.util.Extensions;
import org.violetlib.util.VObjects;

import org.jetbrains.annotations.*;
import org.violetlib.annotations.Immutable;

/**
  An implementation of an immutable map based on hash codes. Supports substructure sharing.

  @param <K> The type of the keys.
  @param <V> The type of the values.
*/

public final @Immutable class HashMapImpl<K,V>
  implements IMap<K,V>
{
    public static <K,V> @NotNull IMap<K,V> create(@NotNull Map<? extends K, ? extends V> bindings)
    {
        return new HashMapImpl<>(bindings);
    }

    private final @NotNull Buckets<K,V> buckets;
    private final int size;
    private volatile ISet<K> keySet;
    private volatile ISet<V> valueSet;

    private HashMapImpl(@NotNull Map<? extends K, ? extends V> bindings)
    {
        Set<? extends Map.Entry<? extends K,? extends V>> entries = bindings.entrySet();
        int entryCount = entries.size();
        int bucketCount = Math.max(1, entryCount / 2);
        int bindingCount = 0;
        buckets = new BucketsImpl<>(bucketCount);
        for (Map.Entry<? extends K, ? extends V> entry : bindings.entrySet()) {
            K key = entry.getKey();
            if (key != null) {
                V value = entry.getValue();
                if (value != null) {
                    int hash = key.hashCode();
                    int bucketHash = spread(hash);
                    Bucket<K,V> b = buckets.get(key, bucketHash);
                    if (b == null) {
                        b = new LinkedListBucket<>(key, hash, value);
                    } else {
                        b = b.add(key, hash, value);
                    }
                    buckets.set(key, bucketHash, b);
                    bindingCount++;
                }
            }
        }
        size = bindingCount;
    }

    private HashMapImpl(int size, @NotNull Buckets<K,V> buckets)
    {
        this.size = size;
        this.buckets = buckets;
    }

    @Override
    public @NotNull IIterator<Binding<K,V>> iterator()
    {
        return IMapBindingIterator.create(this);
    }

    @Override
    public boolean isEmpty()
    {
        return size == 0;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public @Nullable V get(@NotNull K key)
    {
        if (size > 0) {
            int hash = key.hashCode();
            int bucketHash = spread(hash);
            Bucket<K,V> b = buckets.get(key, bucketHash);
            if (b != null) {
                return b.get(key, hash);
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(@NotNull Object key)
    {
        if (size > 0) {
            @SuppressWarnings("unchecked")
            K k = (K) key;
            int hash = k.hashCode();
            int bucketHash = spread(hash);
            Bucket<K,V> b = buckets.get(k, bucketHash);
            if (b != null) {
                return b.get(k, hash) != null;
            }
        }
        return false;
    }

    @Override
    public void visit(@NotNull Visitor<K,V> visitor)
    {
        buckets.visit(b -> b.visit(visitor));
    }

    @Override
    public <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor, @Nullable R defaultResult)
    {
        R result = buckets.find(visitor);
        return result != null ? result : defaultResult;
    }

    @Override
    public @NotNull ISet<K> keySet()
    {
        ISet<K> ks = keySet;
        if (ks != null) {
            return ks;
        }
        return keySet = buckets.createKeySet();
    }

    @Override
    public @NotNull ISet<V> values()
    {
        ISet<V> vs = valueSet;
        if (vs != null) {
            return vs;
        }
        return valueSet = buckets.createValueSet();
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull K key, @Nullable V value)
    {
        int hash = key.hashCode();
        int bucketHash = spread(hash);
        Bucket<K,V> b = buckets.get(key, bucketHash);
        Bucket<K,V> newBucket;

        if (b != null) {
            newBucket = value != null ? b.add(key, hash, value) : b.remove(key, hash);
            if (newBucket == b) {
                return this;
            }
        } else if (value != null) {
            newBucket = new LinkedListBucket<>(key, hash, value);
        } else {
            return this;
        }

        Buckets<K,V> newBuckets = buckets.withBucket(key, bucketHash, newBucket);
        if (newBuckets != null) {
            int delta = value != null ? 1 : -1;
            return new HashMapImpl<>(size + delta, newBuckets);
        } else {
            return IMap.empty();
        }
    }

    @Override
    public @NotNull IMap<K,V> extending(@NotNull IMap<K,V> delta)
    {
        Map<K,V> result = asJavaMap();
        delta.visit(result::put);
        return new HashMapImpl<>(result);
    }

    @Override
    public int hashCode()
    {
        return MapEquality.computeHashCode(this);
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

        IMap<?,?> otherMap = Extensions.getExtension(obj, IMap.class);
        if (otherMap == null) {
            return false;
        }

        return MapEquality.isEqual(this, otherMap);
    }

    /**
      Maps a key and bucket hash code to an associated bucket. Once initialized, a Buckets object is immutable.
    */

    private interface Buckets<K,V>
    {
        /**
          Return the bucket containing the binding for the specified key.
          @param key The key.
          @param hash The bucket hash code for the key.
          @return the bucket containing the binding for the specified key, or null if no bucket contains a binding
          for the specified key.
        */

        @Nullable Bucket<K,V> get(@NotNull K key, int hash);

        @Nullable Buckets<K,V> withBucket(@NotNull K key, int hash, @Nullable Bucket<K,V> bucket);

        void set(@NotNull K key, int hash, @Nullable Bucket<K,V> b);

        void visit(@NotNull Consumer<Bucket<K,V>> c);

        <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor);

        @NotNull ISet<K> createKeySet();

        @NotNull ISet<V> createValueSet();
    }

    /**
      A bucket maps a set of keys to their associated values.
    */

    private @Immutable interface Bucket<K,V>
    {
        /**
          Return the value for the specified key.
          @param key The key.
          @param hash The hash code for the key.
          @return the value, or null if there is no value for the specified key.
        */

        @Nullable V get(@NotNull K key, int hash);

        /**
          Return a bucket that contains all of the bindings of this bucket except that the specified key is bound to
          the specified value.
          @param key The key.
          @param hash The hash code for the key.
          @param value The value to be associated with the specified key.
          @return a bucket as described.
        */

        @NotNull Bucket<K,V> add(@NotNull K key, int hash, @NotNull V value);

        /**
          Return a bucket that contains all of the bindings of this bucket except that the specified key has no
          associated value.
          @param key The key.
          @param hash The hash code for the key.
          @return a bucket as described, or null if the result would be an empty bucket.
        */

        @Nullable Bucket<K,V> remove(@NotNull K key, int hash);

        void visit(@NotNull Visitor<K,V> visitor);

        <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor);
    }

    /**
      An implementation that supports a fixed number of buckets.
    */

    private static class BucketsImpl<K,V>
      implements Buckets<K,V>
    {
        private final int size;
        private final int mask;
        private final @Nullable Bucket<K,V> @NotNull [] buckets;

        /**
          Create a container with the specified number of buckets.

          @param size The number of buckets.
        */

        public BucketsImpl(int size)
        {
            this.size = size;
            this.mask = 0;
            buckets = new Bucket[size];
        }

        /**
          Create a container with a number of buckets that is a power of two.
        */

        public BucketsImpl(int size, int mask)
        {
            this.size = size;
            this.mask = mask;
            this.buckets = new Bucket[size];
        }

        private BucketsImpl(int size, int mask, @Nullable Bucket<K,V> @NotNull [] buckets)
        {
            this.size = size;
            this.mask = mask;
            this.buckets = buckets;
        }

        private int toBucketIndex(int n)
        {
            int h = spread(n);
            return mask > 0 ? h & mask : h % size;
        }

        @Override
        public @Nullable Bucket<K,V> get(@NotNull K key, int hash)
        {
            int index = toBucketIndex(hash);
            return buckets[index];
        }

        @Override
        public @Nullable Buckets<K,V> withBucket(@NotNull K key, int hash, @Nullable Bucket<K,V> bucket)
        {
            int index = toBucketIndex(hash);
            Bucket<K,V> existingBucket = buckets[index];
            if (VObjects.equals(existingBucket, bucket)) {
                return this;
            }

            // If the new bucket is empty and all the other existing buckets are empty, the result is an empty map,
            // so return null.
            if (bucket == null) {
                boolean isEmpty = true;
                for (int i = 0; i < size; i++) {
                    if (i != index) {
                        if (buckets[i] != null) {
                            isEmpty = false;
                            break;
                        }
                    }
                }
                if (isEmpty) {
                    return null;
                }
            }

            Bucket<K,V>[] newBuckets = new Bucket[size];
            System.arraycopy(buckets, 0, newBuckets, 0, size);
            newBuckets[index] = bucket;
            return new BucketsImpl<>(size, mask, newBuckets);
        }

        @Override
        public void set(@NotNull K key, int hash, @Nullable Bucket<K,V> b)
        {
            int index = toBucketIndex(hash);
            buckets[index] = b;
        }

        @Override
        public void visit(@NotNull Consumer<Bucket<K,V>> c)
        {
            for (Bucket<K,V> b : buckets) {
                if (b != null) {
                    c.accept(b);
                }
            }
        }

        @Override
        public <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor)
        {
            for (Bucket<K,V> b : buckets) {
                if (b != null) {
                    R result = b.find(visitor);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        }

        @Override
        public @NotNull ISet<K> createKeySet()
        {
            SetBuilder<K> sb = ISet.builder();
            for (Bucket<K,V> b : buckets) {
                if (b != null) {
                    b.visit((k, v) -> sb.add(k));
                }
            }
            return sb.values();
        }

        @Override
        public @NotNull ISet<V> createValueSet()
        {
            SetBuilder<V> sb = ISet.builder();
            for (Bucket<K,V> b : buckets) {
                if (b != null) {
                    b.visit((k, v) -> sb.add(v));
                }
            }
            return sb.values();
        }
    }

    /**
      An implementation of a bucket using a linked list.
    */

    private static @Immutable class LinkedListBucket<K,V>
      implements Bucket<K,V>
    {
        public final @NotNull K key;
        public final int hash;
        public final @NotNull V value;
        public final @Nullable LinkedListBucket<K,V> next;

        /**
          Create a bucket containing a single binding.
        */

        public LinkedListBucket(@NotNull K key, int hash, @NotNull V value)
        {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = null;
        }

        /**
          Create a bucket containing a specified binding plus the bindings of the specified bucket. The specified
          bucket must not contain a binding for the specified key.
        */

        private LinkedListBucket(@NotNull K key, int hash, @NotNull V value, @Nullable LinkedListBucket<K,V> next)
        {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public @Nullable V get(@NotNull K key, int hash)
        {
            if (hash == this.hash && (key == this.key || key.equals(this.key))) {
                return value;
            }

            return next != null ? next.get(key, hash) : null;
        }

        @Override
        public @NotNull LinkedListBucket<K,V> add(@NotNull K key, int hash, @NotNull V value)
        {
            V existingValue = get(key, hash);
            if (existingValue == null) {
                return new LinkedListBucket<>(key, hash, value, this);
            }

            if (existingValue == value || value.equals(existingValue)) {
                return this;
            }

            LinkedListBucket<K,V> removed = remove(key, hash);
            return new LinkedListBucket<>(key, hash, value, removed);
        }

        @Override
        public @Nullable LinkedListBucket<K,V> remove(@NotNull K key, int hash)
        {
            if (hash == this.hash && (key == this.key || key.equals(this.key))) {
                return next;
            }

            if (next == null) {
                return this;
            }

            LinkedListBucket<K,V> removedNext = next.remove(key, hash);
            if (removedNext == next) {
                return this;
            }

            return new LinkedListBucket<>(this.key, this.hash, this.value, removedNext);
        }

        @Override
        public void visit(@NotNull Visitor<K,V> visitor)
        {
            visitor.visit(key, value);
            if (next != null) {
                next.visit(visitor);
            }
        }

        @Override
        public <R> @Nullable R find(@NotNull FVisitor<K,V,R> visitor)
        {
            R result = visitor.visit(key, value);
            if (result != null) {
                return result;
            }
            return next != null ? next.find(visitor) : null;
        }
    }

    private static int spread(int hash)
    {
        return (hash ^ (hash >>> 16)) & 0xffff;
    }
}
