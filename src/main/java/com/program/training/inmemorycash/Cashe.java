package com.program.training.inmemorycash;

import java.util.Optional;

/**
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
