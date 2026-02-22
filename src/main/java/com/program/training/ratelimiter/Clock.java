package com.program.training.ratelimiter;

/**
 * @author naletov
 */
@FunctionalInterface
public interface Clock
{
    long currentTimeMillis();
}