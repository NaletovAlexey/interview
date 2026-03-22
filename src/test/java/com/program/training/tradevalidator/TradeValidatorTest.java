package com.program.training.tradevalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TradeValidator test
 *
 * @author naletov
 */
class TradeValidatorTest
{
    private TradeValidator validator;

    @BeforeEach
    void setUp()
    {
        validator = new TradeValidator();
    }

    @Test
    void testSequentialTrade()
    {
        validator.updatePosition("MSFT", new BigDecimal("100"));

        // Sale 40 - ok
        assertTrue(validator.validateAndExecute("MSFT", new BigDecimal("-40")));
        assertEquals(new BigDecimal("60"), validator.getBalance("MSFT"));

        // Sale 70 - limit exceeded
        assertFalse(validator.validateAndExecute("MSFT", new BigDecimal("-70")));
        assertEquals(new BigDecimal("60"), validator.getBalance("MSFT"));
    }

    @Test
    void testConcurrentSell() throws InterruptedException
    {
        validator.updatePosition("TSLA", new BigDecimal("10"));

        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        // Add a second latch to wait for all tasks to complete.
        CountDownLatch finishLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++)
        {
            executor.submit(() -> {
                try
                {
                    startLatch.await(); // waiting for the general start
                    if (validator.validateAndExecute("TSLA", new BigDecimal("-1")))
                    {
                        successCount.incrementAndGet();
                    }
                }
                catch (InterruptedException ignored)
                {
                }
                finally
                {
                    finishLatch.countDown(); // inform that the stream has ended
                }
            });
        }

        startLatch.countDown(); // give a start to all streams

        // Wait until ALL threads call finishLatch.countDown()
        boolean finished = finishLatch.await(5, TimeUnit.SECONDS);
        assertTrue(finished, "The test took too long.");

        executor.shutdown();

        assertEquals(10, successCount.get(), "There must be exactly 10 successful sales");
        assertEquals(0, validator.getBalance("TSLA").intValue());
    }
}