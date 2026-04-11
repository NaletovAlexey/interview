package com.program.training.priorityeventbus;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author naletov
 */
class PriorityEventBusImplTest
{
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