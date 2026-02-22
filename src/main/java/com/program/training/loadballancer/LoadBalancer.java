package com.program.training.loadballancer;

/**
 * @author naletov
 */
public interface LoadBalancer
{
    void registerService(Server server);
    void deregisterService(Server server);
    Server selectServer();
    void shutdown() throws InterruptedException;
}
