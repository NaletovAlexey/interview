package com.program.training.loadballancer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Immutable descriptor of a backend server instance.
 *
 * <p>The {@code connection} field is the only mutable element: it is an
 * {@link AtomicInteger} so that multiple threads can track active-connection counts
 * without external synchronization.
 *
 * @param id         unique identifier used as the server's key in the registry (e.g. {@code "s1"})
 * @param host       hostname or IP address the server listens on
 * @param port       TCP port the server listens on
 * @param weight     relative capacity hint used by {@link Algorithm#WEIGHTED_ROUND_ROBIN};
 *                   a higher value attracts proportionally more traffic within each cycle
 * @param connection atomic counter tracking the number of active connections currently
 *                   handled by this server; used by {@link Algorithm#LEAST_CONNECTIONS}
 * @author naletov
 */
public record Server(String id, String host, int port, int weight, AtomicInteger connection)
{
}
