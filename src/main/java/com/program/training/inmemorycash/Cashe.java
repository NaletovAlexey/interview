package com.program.training.inmemorycash;

import java.util.Optional;

/**
 * Contract for a key-value cache with basic read/write operations.
 *
 * <p>Part of an in-memory cache exercise: callers store and retrieve values by key,
 * remove individual entries, clear the cache, and inspect its size. Implementations
 * may add eviction and expiration policies on top of this API.</p>
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author naletov
 */
public interface Cashe<K, V>
{
    void put(K key, V value);
    Optional<V> get(K key);
    void remove(K key);
    void clear();
    int size();
}
