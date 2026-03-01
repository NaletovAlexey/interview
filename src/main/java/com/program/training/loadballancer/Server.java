package com.program.training.loadballancer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author naletov
 */
public record Server(String id, String host, int port, int weight, AtomicInteger connection)
{
}
