package com.program.training.loadballancer;

import java.util.Comparator;
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
        // populate weightedRoundRobinCounters map for corresponding algorithm
        if (this.algorithm.equals(Algorithm.WEIGHTED_ROUND_ROBIN))
        {
            weightedRoundRobinCounters.put(server.id(), server.weight());
        }
    }

    @Override
    public void deregisterService(Server server)
    {
        // Mustn't be active server
        Objects.requireNonNull(server);
        servers.remove(server.id());
        // populate weightedRoundRobinCounters map for corresponding algorithm
        if (this.algorithm.equals(Algorithm.WEIGHTED_ROUND_ROBIN))
        {
            weightedRoundRobinCounters.remove(server.id());
        }
    }

    @Override
    public Server selectServer(HttpRequest request)
    {
        if (servers.isEmpty())
            throw new IllegalStateException();
        return algorithm.select(servers.values().stream().toList(), request, this);
    }

    @Override
    public Server roundRobinSelect(List<Server> servers)
    {
        int index = Math.abs(roundRobinCounter.getAndIncrement()) % servers.size();
        return servers.get(index);
    }

    @Override
    public Server ipHashSelect(List<Server> servers, HttpRequest request)
    {
        // To avoid Integer.MIN_VALUE and overflow
        // int index = Math.abs(request.clientIP().hashCode()) % servers.size(); - dangerous case
        // int index = (hash == Integer.MIN_VALUE) ? 0 : Math.abs(hash) % servers.size(); - can be done like this
        int index = (request.clientIP().hashCode() & Integer.MAX_VALUE) % servers.size();
        return servers.get(index);
    }

    @Override
    public Server weightedRoundRobinSelect(List<Server> servers)
    {
        Server selected = servers.stream().filter(s -> weightedRoundRobinCounters.get(s.id()) > 0).
                max(Comparator.comparingInt( s ->
                    weightedRoundRobinCounters.get(s.id())
        )).orElseThrow(IllegalStateException::new);
        decrementServerWeight(selected);
        return selected;
    }

    public void incrementServerWeight(Server server)
    {
        weightedRoundRobinCounters.compute(server.id(),
                (key, currentWeight)-> currentWeight == null ? server.weight() + 1 : currentWeight + 1);
    }

    public void decrementServerWeight(Server server)
    {
        weightedRoundRobinCounters.compute(server.id(),
                (key, currentWeight) ->
                {
                    if  (currentWeight == null)
                        currentWeight = server.weight();
                    currentWeight--;

                    return currentWeight < 0 ? 0 : currentWeight;
                });
    }

    @Override
    public Server leastConnectionsSelect(List<Server> servers)
    {
        Server selected = servers.stream().min(
                Comparator.comparingInt(s -> s.connection().get())).orElse(servers.getFirst());
        selected.connection().getAndIncrement();

        return selected;
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
