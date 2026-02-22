package com.program.training.loadballancer;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author naletov
 */
public class SoftawareLoadBallancer implements LoadBalancer, LoadBalancerContext
{
    private final ConcurrentHashMap<String, Server> servers = new ConcurrentHashMap<>();
    private final Algorithm algorithm;
    private final long healthCheckIntervalMillis;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);
    private final Map<String, Integer> weightedRoundRobinCounters = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    public SoftawareLoadBallancer(Algorithm algorithm, long healthCheckIntervalMillis)
    {
        this.algorithm = algorithm;
        this.healthCheckIntervalMillis = healthCheckIntervalMillis;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
          r -> {
              Thread t = new Thread(r, "load-balancer-health-check");
              t.setDaemon(true);
              return t;
          });
        startHealthCheckTask();
    }

    @Override
    public void registerService(Server server)
    {
        Objects.requireNonNull(server);
        servers.put(server.id(), server);
    }

    @Override
    public void deregisterService(Server server)
    {
        // Mustn't be active server
        Objects.requireNonNull(server);
        servers.remove(server.id());
    }

    @Override
    public Server selectServer()
    {
        if (servers.isEmpty())
            throw new IllegalStateException();
        return algorithm.select(servers.values().stream().toList(), this);
    }

    @Override
    public Server roundRobinSelect(List<Server> servers)
    {
        int index = Math.abs(roundRobinCounter.getAndIncrement()) % servers.size();
        return servers.get(index);
    }

    @Override
    public Server ipHashSelect(List<Server> servers)
    {
        // not in this version
        throw new UnsupportedOperationException();
    }

    @Override
    public Server weightSelect(List<Server> servers) {
        return null;
    }

    private void startHealthCheckTask()
    {
        scheduler.scheduleAtFixedRate(
                () -> {
                    if (!running) return;
                    for (Server server : servers.values())
                    {
                        if (!performHC(server))
                        {
                            // handle server availability
                            throw new IllegalStateException();
                        }
                    }
                }, 0, healthCheckIntervalMillis, TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void shutdown() throws InterruptedException
    {
        running = false;
        scheduler.shutdown();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);
    }

    private boolean performHC(Server server)
    {
        return true;
    }
}
