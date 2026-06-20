package com.program.training.ratelimiter;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Per-user sliding-window rate limiter.
 *
 * <p>Each user has an independent timestamp queue that records the times of their recent
 * requests.  Before deciding whether to allow a new request, entries older than
 * {@code timeWindow} milliseconds are evicted from the queue.  A per-user
 * {@link ReentrantLock} serialises the read–evict–write sequence for the same user while
 * allowing requests from different users to proceed in parallel.
 *
 * <h2>Thread safety</h2>
 * <p>Concurrent calls for different users proceed without contention.  Concurrent calls for
 * the same user are serialised by the per-user lock.
 *
 * @author naletov
 * @see Clock
 */
public class RateLimiter
{
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Queue<Long>> requests = new ConcurrentHashMap<>();
    private final int limit;
    private final long timeWindow;
    private final Clock clock;

    /**
     * Creates a rate limiter backed by the real system clock.
     *
     * @param limit        maximum number of requests allowed per user within {@code timeWindowMs}
     * @param timeWindowMs length of the sliding window in milliseconds
     */
    public RateLimiter(int limit, long timeWindowMs) {
        this(limit, timeWindowMs, System::currentTimeMillis);
    }

    RateLimiter(int limit, long timeWindowMs, Clock clock) {
        this.limit = limit;
        this.timeWindow = timeWindowMs;
        this.clock = clock;
    }

    /**
     * Decides whether a new request from the given user should be allowed.
     *
     * <p>Timestamps outside the current window are evicted before the decision is made.
     * If the remaining timestamp count is below {@code limit} the request is recorded and
     * {@code true} is returned; otherwise the request is rejected and {@code false} is returned.
     *
     * @param userId the identifier of the requesting user; must not be {@code null} or blank
     * @return {@code true} if the request is within the rate limit, {@code false} if throttled
     * @throws IllegalArgumentException if {@code userId} is {@code null} or blank
     */
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
