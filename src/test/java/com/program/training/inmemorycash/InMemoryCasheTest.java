package com.program.training.inmemorycash;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author naletov
 */
class InMemoryCasheTest
{

    @Test
    void testRemoveOldestEntry()
    {
        InMemoryCashe<Integer, String> inMemoryCashe = new InMemoryCashe<>(2, 0);
        inMemoryCashe.put(1, "A");
        inMemoryCashe.put(2, "B");
        inMemoryCashe.get(1);           // freshest
        inMemoryCashe.put(3, "C");

        assertEquals(2, inMemoryCashe.size());
        assertTrue(inMemoryCashe.get(1).isPresent());
        assertTrue(inMemoryCashe.get(3).isPresent());
        assertTrue(inMemoryCashe.get(2).isEmpty());

    }

    @Test
    void testConcurrencyAccess() throws InterruptedException
    {
        int threads = 10;
        int operationsPerThread = 1000;
        InMemoryCashe<Integer, String> inMemoryCashe = new InMemoryCashe<>(5000, 0);
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(10);

        for (int i = 0; i < threads; i++)
        {
            final int threadId = i;
            executorService.submit(
                    () -> {
                        try
                        {
                            for (int j = 0; j < operationsPerThread; j++)
                            {
                                inMemoryCashe.put(threadId * operationsPerThread + j, "value");
                            }
                        }
                        finally
                        {
                            startLatch.countDown();
                        }
                    });
        }
        startLatch.await();
        executorService.shutdown();
        boolean done = executorService.awaitTermination(5, TimeUnit.MICROSECONDS);
        assertTrue(done);

        inMemoryCashe.put(-1, "test");
        assertEquals(5000, inMemoryCashe.size());
        assertTrue(inMemoryCashe.get(-1).isPresent());
    }

    @Test
    void testRemoveClean()
    {
        InMemoryCashe<Integer, String> inMemoryCashe = new InMemoryCashe<>(3, 0);
        inMemoryCashe.put(1, "A");
        inMemoryCashe.put(2, "B");
        inMemoryCashe.put(3, "C");
        assertEquals(3, inMemoryCashe.size());

        inMemoryCashe.remove(2);
        assertEquals(2, inMemoryCashe.size());
        assertTrue(inMemoryCashe.get(1).isPresent());
        assertTrue(inMemoryCashe.get(3).isPresent());
        assertTrue(inMemoryCashe.get(2).isEmpty());

        inMemoryCashe.clear();
        assertEquals(0, inMemoryCashe.size());
    }
}