package com.program.training.threadpool;

/**
 * We need to implement our own simplified version of a thread pool.
 * We have a task queue. Several workers (threads) should retrieve tasks from the queue in parallel and execute them.
 * @author naletov
 */
public interface CustomThreadPool
{
    /**
     * Submits input task
     * @param task Runnable
     */
    void submit(Runnable task);

    /**
     * New tasks can't be started
     */
    void shutdown();
}
