package com.program.training.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fixed-size thread pool backed by an unbounded {@link LinkedBlockingQueue}.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li><b>Running</b> — workers block on {@link BlockingQueue#take()} and execute tasks.</li>
 *   <li><b>Shutdown</b> — {@link #shutdown()} sets the {@code isShutdown} flag and enqueues
 *       one no-op sentinel per worker to unblock threads waiting on {@code take()}.
 *       Workers detect the flag, exit their main loop, and enter the drain phase.</li>
 *   <li><b>Drain</b> — workers flush remaining tasks via non-blocking {@link BlockingQueue#poll()}
 *       so that already-enqueued work is not discarded.</li>
 * </ol>
 *
 * <h2>Thread safety</h2>
 * <p>The {@code isShutdown} flag is an {@link AtomicBoolean} to guarantee a single atomic
 * state transition.  The task queue is a thread-safe {@link LinkedBlockingQueue}.
 *
 * @author naletov
 * @see CustomThreadPool
 */
public class CustomThreadPoolImpl implements CustomThreadPool
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomThreadPoolImpl.class);
    private final BlockingQueue<Runnable> taskQueue;
    private final List<Worker> workers;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    /**
     * Creates a new thread pool and immediately starts the specified number of worker threads.
     *
     * @param threadCount number of worker threads to create; must be greater than zero
     * @throws IllegalArgumentException if {@code threadCount} is less than or equal to zero
     */
    public CustomThreadPoolImpl(int threadCount) {
        if (threadCount <= 0) {
            throw new IllegalArgumentException("The number of threads must be greater than 0");
        }
        this.taskQueue = new LinkedBlockingQueue<>();
        this.workers = new ArrayList<>(threadCount);

        // Initialize and launch workers
        for (int i = 0; i < threadCount; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            worker.start();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void submit(Runnable task)
    {
        if (task == null) {
            throw new IllegalArgumentException("The task cannot be null");
        }
        if (isShutdown.get()) {
            throw new IllegalStateException("The thread pool has stopped and is not accepting new tasks.");
        }
        taskQueue.add(task);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Injects one no-op sentinel task per worker thread so that threads blocked on
     * {@link BlockingQueue#take()} are unblocked and can observe the shutdown flag on
     * the next loop iteration.
     */
    @Override
    public void shutdown()
    {
        // Atomically switch the state to true
        if (isShutdown.compareAndSet(false, true))
        {
            // Interrupting threads blocked on reading the queue
            for (Worker worker : workers)
            {
                // worker.interrupt();
                taskQueue.add(() -> {});
            }
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            // 1. Main loop: while the flag is false OR the thread is not interrupted
            while (!isShutdown.get() && !Thread.currentThread().isInterrupted())
            {
                try {
                    // take() method blocks the thread if the queue is empty.
                    Runnable task = taskQueue.take();   // blocking
                    task.run();
                } catch (InterruptedException _) {
                    // restores the interrupt flag and check the loop condition.
                    Thread.currentThread().interrupt();
                }
            }
            // 2. Drain phase: finishing tasks via poll()
            Runnable task;
            while ((task = taskQueue.poll()) != null) // not blocking
            {
                try {
                    task.run();
                } catch (Exception e) {
                    LOGGER.error("Error while executing final task: ", e);
                }
            }
        }
    }
}
