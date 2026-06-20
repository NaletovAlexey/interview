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
 * Software load balancer that distributes incoming requests across a pool of backend
 * servers using one of the strategies defined in {@link Algorithm}.
 *
 * <h2>Thread safety</h2>
 * <p>The server registry uses a {@link ConcurrentHashMap} and the round-robin counter is
 * an {@link AtomicInteger}, so concurrent calls to {@link #selectServer}, {@link #registerService},
 * and {@link #deregisterService} are safe without external locking.
 *
 * <h2>Health checks</h2>
 * <p>A background daemon thread invokes {@link #performHC} for every registered server at the
 * configured interval.  In this skeleton implementation {@code performHC} always returns
 * {@code true}; replace it with real HTTP/TCP probes as needed.
 *
 * @author naletov
 * @see Algorithm
 * @see LoadBalancer
 * @see LoadBalancerContext
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

    /**
     * Creates a new load balancer with the specified routing algorithm and health-check schedule.
     *
     * @param algorithm                 the routing strategy used for server selection
     * @param healthCheckIntervalMillis interval in milliseconds between consecutive health checks
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>For {@link Algorithm#WEIGHTED_ROUND_ROBIN} the server's initial weight counter is
     * also populated in the internal weight map.
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>For {@link Algorithm#WEIGHTED_ROUND_ROBIN} the server's weight counter is also
     * removed from the internal weight map.
     */
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

    /** {@inheritDoc} */
    @Override
    public Server selectServer(HttpRequest request)
    {
        if (servers.isEmpty())
            throw new IllegalStateException();
        return algorithm.select(servers.values().stream().toList(), request, this);
    }

    /** {@inheritDoc} */
    @Override
    public Server roundRobinSelect(List<Server> servers)
    {
        int index = Math.abs(roundRobinCounter.getAndIncrement()) % servers.size();
        return servers.get(index);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@code hashCode() & Integer.MAX_VALUE} instead of {@code Math.abs()} to avoid
     * the overflow edge case where {@code Math.abs(Integer.MIN_VALUE)} returns a negative value.
     */
    @Override
    public Server ipHashSelect(List<Server> servers, HttpRequest request)
    {
        // To avoid Integer.MIN_VALUE and overflow
        // int index = Math.abs(request.clientIP().hashCode()) % servers.size(); - dangerous case
        // int index = (hash == Integer.MIN_VALUE) ? 0 : Math.abs(hash) % servers.size(); - can be done like this
        int index = (request.clientIP().hashCode() & Integer.MAX_VALUE) % servers.size();
        return servers.get(index);
    }

    /** {@inheritDoc} */
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

    /**
     * Increments the remaining weight quota of the given server by one.
     *
     * <p>If the server is not yet present in the weight map, the counter is initialised to
     * {@code server.weight() + 1}.
     *
     * @param server the server whose weight counter should be incremented
     */
    public void incrementServerWeight(Server server)
    {
        weightedRoundRobinCounters.compute(server.id(),
                (key, currentWeight)-> currentWeight == null ? server.weight() + 1 : currentWeight + 1);
    }

    /**
     * Decrements the remaining weight quota of the given server by one, clamping to zero.
     *
     * <p>Called automatically after each weighted-round-robin selection to reduce the
     * server's remaining quota for the current cycle.
     *
     * @param server the server whose weight counter should be decremented
     */
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

    /** {@inheritDoc} */
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

    /**
     * {@inheritDoc}
     *
     * <p>Stops the health-check loop and waits up to 5 seconds for the scheduler thread
     * to terminate cleanly.
     */
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
