package com.program.training.priorityeventbus;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PriorityEventBusImpl}.
 *
 * @author naletov
 */
class PriorityEventBusImplTest
{
    /**
     * Verifies that subscribers are invoked in strictly descending priority order
     * (10 → 5 → 1) regardless of registration sequence.
     */
    @Test
    void shouldInvokeSubscribersInPriorityOrder() {
        PriorityEventBus bus = new PriorityEventBusImpl();
        List<String> executionOrder = new ArrayList<>();

        // subscribe with different priorities
        bus.subscribe(String.class, 1, event -> executionOrder.add("low"));
        bus.subscribe(String.class, 10, event -> executionOrder.add("high"));
        bus.subscribe(String.class, 5, event -> executionOrder.add("medium"));

        bus.publish("Test Event");

        // Checking for strict consistency
        assertEquals(List.of("high", "medium", "low"), executionOrder);
    }

    /**
     * Verifies that an exception thrown by one subscriber does not prevent remaining
     * subscribers from being invoked.
     */
    @Test
    void shouldContinuePublishingIfOneSubscriberFails() {
        PriorityEventBus bus = new PriorityEventBusImpl();
        List<String> executionOrder = new ArrayList<>();

        bus.subscribe(String.class, 10, event -> {
            throw new RuntimeException();
        });
        bus.subscribe(String.class, 5, event -> executionOrder.add("success"));

        // The publication should not end with an exception
        assertDoesNotThrow(() -> bus.publish("Test Event"));

        // The second subscriber should have completed the task successfully.
        assertEquals(List.of("success"), executionOrder);
    }
}