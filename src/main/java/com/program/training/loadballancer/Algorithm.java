package com.program.training.loadballancer;

import java.util.List;

/**
 * @author naletov
 */
public enum Algorithm
{
    ROUND_ROBIN
            {
                @Override
                public Server select(List<Server> servers, HttpRequest request, LoadBalancerContext context)
                {
                    return context.roundRobinSelect(servers);
                }
            },
    LEAST_CONNECTIONS
            {
                @Override
                public Server select(List<Server> servers, HttpRequest request, LoadBalancerContext context)
                {
                    return context.leastConnectionsSelect(servers);
                }
            },
    WEIGHTED_ROUND_ROBIN
            {
                @Override
                public Server select(List<Server> servers, HttpRequest request, LoadBalancerContext context)
                {
                    return context.weightedRoundRobinSelect(servers);
                }
            },

    IP_HASH
            {
                @Override
                public Server select(List<Server> servers,  HttpRequest request, LoadBalancerContext context)
                {
                    return context.ipHashSelect(servers, request);
                }
            };

    public abstract Server select(List<Server> servers, HttpRequest request, LoadBalancerContext context);
}
