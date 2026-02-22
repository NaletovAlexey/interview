package com.program.training.loadballancer;

import java.util.List;

/**
 * @author naletov
 */
public interface LoadBalancerContext
{
    Server roundRobinSelect(List<Server> servers);
    Server ipHashSelect(List<Server> servers);
    Server weightSelect(List<Server> servers);
    // ...
}
