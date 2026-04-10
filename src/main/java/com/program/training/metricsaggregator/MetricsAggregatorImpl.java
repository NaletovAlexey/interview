package com.program.training.metricsaggregator;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;

/**
 * We need to collect and aggregate metrics (e.g., server response time).
 * The service should be able to return the average metric value over a sliding window (the last 5 minutes).
 *
 * @author naletov
 */
public class MetricsAggregatorImpl implements MetricsAggregator
{
    private final Duration windowSize;
    private final Clock clock;

    private final LongAdder sum = new LongAdder();
    private final LongAdder count = new LongAdder();

    private final Queue<MetricEvent> events = new ConcurrentLinkedQueue<>();

    public MetricsAggregatorImpl(Duration windowSize, Clock clock)
    {
        this.windowSize = windowSize;
        this.clock = clock;
    }

    @Override
    public void addMetric(long value)
    {
        events.offer(new MetricEvent(value, Instant.now(clock)));
        sum.add(value);
        count.increment();
        cleanupOldEvents();
    }

    @Override
    public double getAverage()
    {
        cleanupOldEvents();

        long currentCount = count.sum();
        if (currentCount == 0) {
            return 0.0;
        }

        return (double) sum.sum() / currentCount;
    }

    private void cleanupOldEvents()
    {
        Instant cutoff = Instant.now(clock).minus(windowSize);
        // drop the items from Queue
        while (!events.isEmpty() && events.peek().timestamp().isBefore(cutoff))
        {
           MetricEvent expiredEvent = events.poll();
            sum.add(-expiredEvent.value());
            count.decrement();
        }
    }

    private record MetricEvent(long value, Instant timestamp){}
}
