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
                public Server select(List<Server> servers, LoadBalancerContext context)
                {
                    return context.roundRobinSelect(servers);
                }
            },
    // ...
    IP_HASH
            {
                @Override
                public Server select(List<Server> servers, LoadBalancerContext context)
                {
                    return context.ipHashSelect(servers);
                }
            };

    public abstract Server select(List<Server> servers, LoadBalancerContext context);
}
