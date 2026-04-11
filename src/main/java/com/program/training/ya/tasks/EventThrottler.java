package com.program.training.ya.tasks;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.stream.Collectors.toList;

/**
 * Implement a throttler to limit the rate of events from IoT devices.
 * Sensors send events, but their rate needs to be controlled to save bandwidth.
 * Requirements:
 * - Use a sliding window algorithm
 * - Thread safety - events come from different threads
 * - Cleanup of stale records
 *
 * @author naletov
 */
public class EventThrottler
{
    private final int maxEventsByDevice;
    private final Duration windowDuration;
    private final HashMap<String, List<Instant>> map = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    public EventThrottler(int maxEventsByDevice, Duration windowDuration) {
        this.maxEventsByDevice = maxEventsByDevice;
        this.windowDuration = windowDuration;
    }

    /**
     * Checks whether eventCount events can be received from a device.
     * Each device has its own independent limit.
     *
     * @param deviceId device identifier
     * @param eventCount number of events to add
     * @return true if events have been received, false if the limit has been exceeded
     */
    public boolean shouldAcceptEvent(String deviceId, int eventCount) {

        if (eventCount > maxEventsByDevice)
            return false;

        try
        {
            lock.lock();
            List<Instant> receivedAt;
            if (!map.containsKey(deviceId))
            {
                receivedAt = new ArrayList<>();
                receivedAt.add(Instant.now());
                map.put(deviceId, receivedAt);
                return true;
            }
            // List cleanup
            cleanupEvents(deviceId);

            receivedAt = map.get(deviceId);
            // check events count
            if (receivedAt.size() >= maxEventsByDevice)
            {
                return false;
            }
            Instant currentTime = Instant.now();
            for (int i = 0; i < eventCount; i++)
            {
                receivedAt.add(currentTime);
            }
        }
        finally
        {
            lock.unlock();
        }
        return true;
    }

    private void cleanupEvents(String deviceId)
    {
        map.computeIfPresent(deviceId,
                (k, cleanupList) ->
                        cleanupList.stream().filter(val -> Instant.now().minus(windowDuration).isBefore(val)).collect(toList()) );
    }
}
