package com.program.training.inmemorycash;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author naletov
 */
public class InMemoryCashe<K, V> implements Cashe<K, V>, AutoCloseable
{
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Map<K, CasheValue<V>> map;
    private final long ttlMillis;
    private final ScheduledExecutorService cleanUpExecutor;

    public InMemoryCashe(int maxSize, long ttlMillis)
    {
        this.ttlMillis = ttlMillis;

        this.map = new LinkedHashMap<>(maxSize, 0.75f, true)
        {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CasheValue<V>> eldest)
            {
                return maxSize < size();
            }
        };
        if (ttlMillis > 0)
        {
            this.cleanUpExecutor = Executors.newSingleThreadScheduledExecutor(
                    r -> {
                        Thread t = new Thread(r, "cache-cleaner");
                        t.setDaemon(true);
                        return t;
                    });
            cleanUpExecutor.scheduleAtFixedRate(this::cleanUpExpiredEntries,
                    ttlMillis, ttlMillis, TimeUnit.MICROSECONDS);
        }
        else
        {
            cleanUpExecutor = null;
        }
    }

    private void cleanUpExpiredEntries()
    {
        writeLock.lock();
        try
        {
            map.entrySet().removeIf(entry -> isExpired(entry.getValue()));
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private boolean isExpired(CasheValue<V> value)
    {
        return ttlMillis > 0 && (System.currentTimeMillis() - value.createdAt() > ttlMillis);
    }

    @Override
    public void put(K key, V value)
    {
        writeLock.lock();
        try
        {
            map.put(key, new CasheValue<>(value, System.currentTimeMillis()));
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public Optional<V> get(K key)
    {
        writeLock.lock();
        try
        {
            CasheValue<V> entry = map.get(key);
            if (entry == null)
                return Optional.empty();

            if (isExpired(entry))
            {
                map.remove(key);
                return Optional.empty();
            }

            return Optional.of(entry.value());
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(K key)
    {
        writeLock.lock();
        try
        {
            map.remove(key);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void clear()
    {
        writeLock.lock();
        try
        {
            map.clear();
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public int size()
    {
        return map.size();
    }

    @Override
    public void close() throws Exception
    {
        // All the resources must be close
        if (cleanUpExecutor != null)
            cleanUpExecutor.shutdownNow();
    }
}
