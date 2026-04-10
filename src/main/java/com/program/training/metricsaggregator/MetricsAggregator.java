package com.program.training.metricsaggregator;

/**
 * @author naletov
 */
public interface MetricsAggregator
{
    /**
     * server response time
     * @param value time
     */
    void addMetric(long value);

    /**
     * Average value of the metric over a sliding window
     * @return average response time
     */
    double getAverage();
}
