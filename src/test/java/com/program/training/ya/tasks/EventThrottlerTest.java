package com.program.training.ya.tasks;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author naletov
 */
class EventThrottlerTest
{
    @Test
    void testBasicEventThrottling() throws InterruptedException
    {
        EventThrottler throttler = new EventThrottler(3, Duration.ofSeconds(1));

        String deviceId = "sensor-temp-01";

        assertTrue(throttler.shouldAcceptEvent(deviceId, 1));
        assertTrue(throttler.shouldAcceptEvent(deviceId, 2));

        // 4-е событие должно быть отклонено
        assertFalse(throttler.shouldAcceptEvent(deviceId, 1));

        Thread.sleep(1100);
        assertTrue(throttler.shouldAcceptEvent(deviceId, 1));
    }
}