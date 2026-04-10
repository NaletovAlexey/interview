package com.program.training.threadpool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author naletov
 */
class CustomThreadPoolImplTest
{
    private CustomThreadPoolImpl pool;

    @AfterEach
    void tearDown() {
        if (pool != null) {
            pool.shutdown();
        }
    }

    @Test
    @DisplayName("Must successfully complete all assigned tasks")
    void shouldExecuteAllSubmittedTasks() throws InterruptedException {
        // Arrange
        int threadCount = 3;
        int taskCount = 10;
        pool = new CustomThreadPoolImpl(threadCount);

        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger successCounter = new AtomicInteger(0);

        // Act
        for (int i = 0; i < taskCount; i++) {
            pool.submit(() -> {
                successCounter.incrementAndGet();
                latch.countDown();
            });
        }

        // Wait for execution (maximum 2 seconds)
        boolean allTasksCompleted = latch.await(2, TimeUnit.SECONDS);

        // Assert
        assertTrue(allTasksCompleted, "The threads did not complete the tasks within the allotted time.");
        assertEquals(taskCount, successCounter.get(), "The number of completed tasks must be exactly 10");
    }

    @Test
    @DisplayName("Tasks must be distributed across different threads of the pool")
    void shouldDistributeTasksAmongMultipleThreads() throws InterruptedException {
        // Arrange
        int threadCount = 3;
        int taskCount = 15;
        pool = new CustomThreadPoolImpl(threadCount);

        CountDownLatch latch = new CountDownLatch(taskCount);
        Set<String> threadNames = Collections.newSetFromMap(new ConcurrentHashMap<>());

        // Act
        for (int i = 0; i < taskCount; i++) {
            pool.submit(() -> {
                threadNames.add(Thread.currentThread().getName());
                latch.countDown();
            });
        }
        latch.await(2, TimeUnit.SECONDS);

        assertTrue(threadNames.size() > 1, "Tasks were executed by only one thread!");
        assertTrue(threadNames.size() <= threadCount, "More threads were found than specified in the pool.");
    }

    @Test
    @DisplayName("Should throw an error when trying to add a task to a stopped pool.")
    void shouldThrowExceptionWhenSubmittingToShutdownPool() {
        // Arrange
        pool = new CustomThreadPoolImpl(1);
        pool.shutdown();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            pool.submit(() -> System.out.println("This task should not be accepted"));
        }, "The submit() method should throw IllegalStateException after shutdown()");
    }

    @Test
    @DisplayName("Must complete the remaining tasks in the queue after calling shutdown")
    void shouldCompleteQueueTasksAfterShutdown() throws InterruptedException {
        // Arrange
        pool = new CustomThreadPoolImpl(1); // 1 thread so that tasks are executed strictly in sequence
        CountDownLatch firstTaskLatch = new CountDownLatch(1);
        CountDownLatch allTasksLatch = new CountDownLatch(3);
        AtomicInteger completedTasks = new AtomicInteger(0);

        // 1. The first task blocks the only thread
        pool.submit(() -> {
            try {
                firstTaskLatch.await(); // waiting for a signal from the main stream
                completedTasks.incrementAndGet();
                allTasksLatch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 2. We're calling shutdown. The isShutdown flag is now true.
        pool.submit(() -> { completedTasks.incrementAndGet(); allTasksLatch.countDown(); });
        pool.submit(() -> { completedTasks.incrementAndGet(); allTasksLatch.countDown(); });

        // Act
        pool.shutdown(); // calling shutdown. The isShutdown flag is now true.

        // We'll wake up the first task. The thread will be freed and should clean up the remaining two tasks using poll().
        firstTaskLatch.countDown();

        boolean allDone = allTasksLatch.await(2, TimeUnit.SECONDS);

        // Assert
        assertTrue(allDone, "Tasks remaining in the queue were not processed during the Drain phase.");
        assertEquals(3, completedTasks.get(), "All 3 tasks must have been completed successfully.");
    }
}