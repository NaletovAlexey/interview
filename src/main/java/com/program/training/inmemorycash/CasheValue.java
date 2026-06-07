package com.program.training.inmemorycash;

/**
 * A cache entry that pairs a stored value with its creation time.
 *
 * <p>Used by {@link InMemoryCashe} to support time-to-live (TTL) expiration:
 * each {@code put} records when the entry was written so the cache can detect
 * and remove stale values on read or during background cleanup.</p>
 *
 * @param <V> the value type
 * @author naletov
 */
public record CasheValue<V>(V value, long createdAt)
{
}
