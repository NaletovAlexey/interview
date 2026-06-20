package com.program.training.loadballancer;

import java.util.List;

/**
 * Enumeration of supported load balancing strategies.
 *
 * <p>Each constant encapsulates the routing decision for a specific algorithm and
 * delegates the actual server selection to the corresponding {@link LoadBalancerContext}
 * method, keeping the enum free of implementation details.
 *
 * @author naletov
 * @see LoadBalancerContext
 */
public enum Algorithm
{
    /**
     * Distributes requests across servers in a cyclic order, regardless of current load.
     * Suitable for homogeneous server pools where every server has equal capacity.
     */
    ROUND_ROBIN
            {
                @Override
                public Server select(List<Server> servers, HttpRequest request, LoadBalancerContext context)
                {
                    return context.roundRobinSelect(servers);
                }
            },

    /**
     * Routes each request to the server currently handling the fewest active connections.
     * Preferred for long-lived or variable-duration workloads.
     */
    LEAST_CONNECTIONS
            {
                @Override
                public Server select(List<Server> servers, HttpRequest request, LoadBalancerContext context)
                {
                    return context.leastConnectionsSelect(servers);
                }
            },

    /**
     * Extends round-robin by giving each server proportional traffic according to its
     * {@link Server#weight()}.  A server with weight 3 receives three times as many
     * requests as a server with weight 1 within each distribution cycle.
     */
    WEIGHTED_ROUND_ROBIN
            {
                @Override
                public Server select(List<Server> servers, HttpRequest request, LoadBalancerContext context)
                {
                    return context.weightedRoundRobinSelect(servers);
                }
            },

    /**
     * Pins each client IP address to a specific server by hashing the IP.
     * Guarantees session affinity: the same IP is always routed to the same server
     * as long as the server pool does not change.
     */
    IP_HASH
            {
                @Override
                public Server select(List<Server> servers,  HttpRequest request, LoadBalancerContext context)
                {
                    return context.ipHashSelect(servers, request);
                }
            };

    /**
     * Selects a backend server for the given request using this algorithm's strategy.
     *
     * @param servers  the list of currently registered servers; must not be empty
     * @param request  the incoming HTTP request; used by {@link #IP_HASH} for client IP extraction
     * @param context  the load balancer context providing the concrete selection methods
     * @return         the selected {@link Server}
     */
    public abstract Server select(List<Server> servers, HttpRequest request, LoadBalancerContext context);
}
