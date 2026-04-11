package com.program.training.metricsaggregator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author naletov
 */
class MetricsAggregatorImplTest
{
    private MetricsAggregator metrics;
    private MutableClock clock;

    @BeforeEach
    void setUp()
    {
        clock = new MutableClock(Instant.parse("2026-04-10T12:00:00Z"));
        metrics = new MetricsAggregatorImpl(Duration.ofMinutes(5), clock);
    }

    @Test
    void calculateAverage()
    {
        metrics.addMetric(100);
        metrics.addMetric(200);
        metrics.addMetric(300);

        assertEquals(200.0, metrics.getAverage(), 0.001);
    }

    @Test
    void calculateAverageWithExpiration()
    {
        metrics.addMetric(100); // 12:00
        metrics.addMetric(200); // 12:00

        clock.advanceBy(Duration.ofMinutes(6));

        metrics.addMetric(300); // 12:06
        assertEquals(300.0, metrics.getAverage(), 0.001, "Just a single metric");
    }

    @Test
    void checkConcurrency() throws InterruptedException
    {
        int threadsCount = 10;
        int operationsPerThread = 1000;

        Executor executor = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadsCount);

        for (int i = 0; i < threadsCount; i++)
        {
            executor.execute(() -> {
                try
                {
                    for (int j = 0; j < operationsPerThread; j++)
                    {
                        metrics.addMetric(100L);
                    }
                }
                finally
                {
                    countDownLatch.countDown();
                }

            });
        }

        countDownLatch.await();
        countDownLatch.countDown();

        assertEquals(100.0, metrics.getAverage(), 0.001, "Must be certain 100");
    }

    /**
     * Clock class
     */
    private static class MutableClock extends Clock
    {
        private Instant now;

        MutableClock(Instant start)
        {
            this.now = start;
        }

        public void advanceBy(Duration duration)
        {
            this.now = this.now.plus(duration);
        }

        @Override
        public ZoneId getZone()
        {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone)
        {
            throw new UnsupportedOperationException("Unsupported operation for now");
        }

        @Override
        public Instant instant()
        {
            return now;
        }
    }
}
