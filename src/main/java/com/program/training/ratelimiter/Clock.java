package com.program.training.ratelimiter;

/**
 * Abstraction over the system clock used by {@link RateLimiter} to obtain the current time.
 *
 * <p>In production code {@link RateLimiter#RateLimiter(int, long)} wires this to
 * {@code System::currentTimeMillis}.  In tests a mock implementation lets you control the
 * perceived passage of time without actually sleeping.
 *
 * @author naletov
 */
@FunctionalInterface
public interface Clock
{
    /**
     * Returns the current time in milliseconds since the Unix epoch.
     *
     * @return current wall-clock time in milliseconds
     */
    long currentTimeMillis();
}