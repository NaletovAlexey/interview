package com.program.training.loadballancer;

/**
 * Core contract for a software load balancer.
 *
 * <p>A load balancer maintains a pool of backend {@link Server} instances and routes
 * incoming {@link HttpRequest} objects to one of them according to a chosen
 * {@link Algorithm}.  Implementations are expected to be thread-safe.
 *
 * @author naletov
 * @see Algorithm
 * @see Server
 */
public interface LoadBalancer
{
    /**
     * Adds a server to the active pool so that future requests may be routed to it.
     *
     * @param server the server to register; must not be {@code null}
     * @throws NullPointerException if {@code server} is {@code null}
     */
    void registerService(Server server);

    /**
     * Removes a server from the active pool.
     *
     * <p>Requests already in flight are not affected; only future selections will skip
     * the removed server.
     *
     * @param server the server to deregister; must not be {@code null}
     * @throws NullPointerException if {@code server} is {@code null}
     */
    void deregisterService(Server server);

    /**
     * Selects a backend server for the given request according to the configured algorithm.
     *
     * @param request the incoming HTTP request; certain algorithms use it for routing
     * @return the selected {@link Server}
     * @throws IllegalStateException if no servers are currently registered
     */
    Server selectServer(HttpRequest request);

    /**
     * Initiates an orderly shutdown of background tasks (e.g., health checks).
     *
     * <p>Blocks until all background threads have terminated or an internal timeout elapses.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    void shutdown() throws InterruptedException;
}
