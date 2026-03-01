package com.program.training.loadballancer;

/**
 * @author naletov
 */
public interface LoadBalancer
{
    void registerService(Server server);
    void deregisterService(Server server);
    Server selectServer(HttpRequest request);
    void shutdown() throws InterruptedException;
}
