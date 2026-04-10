package com.program.training.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author naletov
 */
public class CustomThreadPoolImpl implements CustomThreadPool
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomThreadPoolImpl.class);
    private final BlockingQueue<Runnable> taskQueue;
    private final List<Worker> workers;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

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
