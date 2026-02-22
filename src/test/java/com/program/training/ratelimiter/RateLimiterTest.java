package com.program.training.ratelimiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author naletov
 */
@ExtendWith(MockitoExtension.class)
class RateLimiterTest
{
    private final Clock mockClock = Mockito.mock(Clock.class);
    private final RateLimiter limiter = new RateLimiter(2, 1000, mockClock);

    @Test
    void allowsRequestsWithinLimit() {
        when(mockClock.currentTimeMillis()).thenReturn(1000L);
        assertTrue(limiter.allowRequest("user1"));

        when(mockClock.currentTimeMillis()).thenReturn(1500L);
        assertTrue(limiter.allowRequest("user1"));
    }

    @Test
    void blocksRequestsOverLimit() {
        when(mockClock.currentTimeMillis()).thenReturn(1000L);
        limiter.allowRequest("user2");
        limiter.allowRequest("user2");

        when(mockClock.currentTimeMillis()).thenReturn(1500L);
        assertFalse(limiter.allowRequest("user2"));
    }

    @Test
    void resetsAfterTimeWindow() {
        when(mockClock.currentTimeMillis()).thenReturn(1000L);
        limiter.allowRequest("user3");
        limiter.allowRequest("user3");

        when(mockClock.currentTimeMillis()).thenReturn(2500L);
        assertTrue(limiter.allowRequest("user3"));
    }

    @Test
    void emptyUser() {
        assertThrows(IllegalArgumentException.class, () -> limiter.allowRequest(""));
        assertThrows(IllegalArgumentException.class, () -> limiter.allowRequest(null));
    }

    @Test
    void severalUsersRequests() throws InterruptedException
    {
        when(mockClock.currentTimeMillis()).thenReturn(1000L);
        RateLimiter rl = new RateLimiter(19, 1000, mockClock);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(30); // 20 + 10 запросов

        // AtomicInteger for thread safe counter
        AtomicInteger successCountUser4 = new AtomicInteger(0);
        AtomicInteger successCountUser5 = new AtomicInteger(0);

        // 20 requests for user4
        for (int i = 0; i < 20; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    boolean allowed = rl.allowRequest("user4");
                    if (allowed) {
                        successCountUser4.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // 10 requests for user5
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    boolean allowed = rl.allowRequest("user5");
                    if (allowed) {
                        successCountUser5.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all the threads simultaneously
        completionLatch.await(5, TimeUnit.SECONDS); // Ждем завершения всех запросов

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Проверяем результаты
        assertEquals(19, successCountUser4.get(), "User4 должен иметь ровно 19 успешных запросов");
        assertNotEquals(20, successCountUser4.get(), "User4 должен иметь ровно 19 успешных запросов");
        assertEquals(10, successCountUser5.get(), "User5 должен иметь 10 успешных запросов");
    }
}