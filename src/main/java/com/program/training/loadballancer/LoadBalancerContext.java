package com.program.training.loadballancer;

import java.util.List;

/**
 * Strategy interface that encapsulates the concrete server-selection algorithms.
 *
 * <p>Implementations hold the mutable state required by each algorithm (e.g., the
 * round-robin counter, per-server weight quotas, connection counts) and expose one
 * method per supported strategy.  This interface is typically implemented together with
 * {@link LoadBalancer} by the same class.
 *
 * @author naletov
 * @see Algorithm
 */
public interface LoadBalancerContext
{
    /**
     * Selects a server using round-robin rotation.
     *
     * @param servers the current server pool; must not be empty
     * @return the next server in the cyclic rotation
     */
    Server roundRobinSelect(List<Server> servers);

    /**
     * Selects a server by hashing the client IP from the given request.
     *
     * <p>The same IP always maps to the same server as long as the pool size is unchanged,
     * providing session affinity without server-side session storage.
     *
     * @param servers the current server pool; must not be empty
     * @param request the HTTP request whose {@link HttpRequest#clientIP()} is used for hashing
     * @return the server to which the client IP is pinned
     */
    Server ipHashSelect(List<Server> servers, HttpRequest request);

    /**
     * Selects a server using weighted round-robin.
     *
     * <p>Servers with a higher {@link Server#weight()} receive proportionally more requests
     * within each distribution cycle.  When all per-server weight counters reach zero the
     * cycle is considered exhausted and subsequent calls will throw.
     *
     * @param servers the current server pool; must not be empty
     * @return the server with the highest remaining weight quota
     * @throws IllegalStateException if no server has a positive remaining weight
     */
    Server weightedRoundRobinSelect(List<Server> servers);

    /**
     * Selects the server with the fewest active connections.
     *
     * <p>The selected server's connection counter is incremented atomically before the method
     * returns.  The caller is responsible for decrementing the counter when the request completes.
     *
     * @param servers the current server pool; must not be empty
     * @return the server with the lowest active-connection count
     */
    Server leastConnectionsSelect(List<Server> servers);
}
