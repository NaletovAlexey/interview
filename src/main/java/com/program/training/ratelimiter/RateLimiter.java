package com.program.training.ratelimiter;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author naletov
 */
public class RateLimiter
{
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Queue<Long>> requests = new ConcurrentHashMap<>();
    private final int limit;
    private final long timeWindow;
    private final Clock clock;

    public RateLimiter(int limit, long timeWindowMs) {
        this(limit, timeWindowMs, System::currentTimeMillis);
    }

    RateLimiter(int limit, long timeWindowMs, Clock clock) {
        this.limit = limit;
        this.timeWindow = timeWindowMs;
        this.clock = clock;
    }

    public boolean allowRequest(String userId)
    {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("userId is null or empty");

        ReentrantLock lock = locks.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();
        try
        {
            Queue<Long> userRequests = requests.computeIfAbsent(
                    userId, k -> new ConcurrentLinkedQueue<>()
            );

            long now = clock.currentTimeMillis();
            dropOldNotes(userRequests, now);

            if (userRequests.size() < limit) {
                userRequests.add(now);
                return true;
            }
            return false;
        }
        finally {
            lock.unlock();
        }
    }

    private void dropOldNotes(Queue<Long> userRequests, long now)
    {
        while (!userRequests.isEmpty() &&
                now - userRequests.peek() > timeWindow) {
            userRequests.poll();
        }
    }
}
