package com.program.training.threadpool;

/**
 * Contract for a simplified fixed-size thread pool.
 *
 * <p>Worker threads pull tasks from a shared queue and execute them concurrently.
 * After {@link #shutdown()} is called the pool stops accepting new tasks; tasks that are
 * already in the queue continue to run until the queue is fully drained (drain phase).
 *
 * @author naletov
 * @see CustomThreadPoolImpl
 */
public interface CustomThreadPool
{
    /**
     * Enqueues a task for asynchronous execution by one of the pool's worker threads.
     *
     * @param task the task to execute; must not be {@code null}
     * @throws IllegalArgumentException if {@code task} is {@code null}
     * @throws IllegalStateException    if the pool has already been shut down
     */
    void submit(Runnable task);

    /**
     * Initiates an orderly shutdown: no new tasks are accepted, and worker threads finish
     * executing all tasks already in the queue before terminating (drain phase).
     *
     * <p>Calling this method more than once has no additional effect.
     */
    void shutdown();
}
