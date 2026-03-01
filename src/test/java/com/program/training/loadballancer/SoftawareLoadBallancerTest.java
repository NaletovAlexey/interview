package com.program.training.loadballancer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author naletov
 */
@ExtendWith(MockitoExtension.class)
class SoftawareLoadBallancerTest
{
    private SoftawareLoadBallancer lb;
    private Server serverOne;
    private Server serverTwo;
    private Server serverThree;
    private Server serverFour;
    private Server serverFive;
    HttpRequest request1;
    HttpRequest request2;
    HttpRequest request3;

    @BeforeEach
    void setUp()
    {
        lb = new SoftawareLoadBallancer(Algorithm.ROUND_ROBIN, 5000);
        serverOne = new Server("s1", "localhot", 8080, 1, new AtomicInteger(5));
        serverTwo = new Server("s2", "localhot", 8081, 2, new AtomicInteger(4));
        serverThree = new Server("s3", "localhot", 8082, 3, new AtomicInteger(3));
        serverFour = new Server("s4", "localhot", 8083, 1, new AtomicInteger(2));
        serverFive = new Server("s5", "localhot", 8085, 2, new AtomicInteger(1));
        request1 = new HttpRequest("127.0.0.1");
        request2 = new HttpRequest("127.0.0.2");
        request3 = new HttpRequest("127.0.0.3");
    }

    @AfterEach
    void stop() throws InterruptedException
    {
        lb.shutdown();
    }

    @Test
    void testRoundRobinSelect()
    {
        lb.registerService(serverOne);
        lb.registerService(serverTwo);
        lb.registerService(serverThree);
        lb.registerService(serverFour);
        lb.registerService(serverFive);

        List<Server> selections = new ArrayList<>();
        for (int i = 0; i < 5; i++)
        {
            selections.add(lb.selectServer(request1));
        }
        assertNotEquals(selections.get(0).id(), selections.get(4).id(), "compare 1st and 5th servers");
        assertNotEquals(selections.get(0).id(), selections.get(3).id(), "compare 1st an 4th server");
        assertNotEquals(selections.get(0).id(), selections.get(2).id(), "compare 1st an 3th server");
        assertNotEquals(selections.get(0).id(), selections.get(1).id(), "compare 1st an 4th server");

        lb.deregisterService(serverFour);
        lb.deregisterService(serverFive);
        selections.clear();
        for (int i = 0; i < 4; i++)
        {
            selections.add(lb.selectServer(request1));
        }
        assertEquals(selections.get(0).id(), selections.get(3).id(), "must be the same");
    }

    @Test
    void testIpHashSelect() throws InterruptedException
    {
        SoftawareLoadBallancer lbIpHash = new SoftawareLoadBallancer(Algorithm.IP_HASH, 5000);
        lbIpHash.registerService(serverOne);
        lbIpHash.registerService(serverTwo);
        lbIpHash.registerService(serverThree);
        lbIpHash.registerService(serverFour);
        lbIpHash.registerService(serverFive);

        List<Server> selections = new ArrayList<>();
        selections.add(lbIpHash.selectServer(request1));
        selections.add(lbIpHash.selectServer(request2));
        selections.add(lbIpHash.selectServer(request3));
        selections.add(lbIpHash.selectServer(request1));
        selections.add(lbIpHash.selectServer(request2));

        assertEquals(selections.get(0).id(), selections.get(3).id(), "must be the same");
        assertEquals(selections.get(1).id(), selections.get(4).id(), "must be the same");
        assertNotEquals(selections.get(0).id(), selections.get(1).id(), "must be different");
        assertNotEquals(selections.get(0).id(), selections.get(2).id(), "must be different");
        assertNotEquals(selections.get(1).id(), selections.get(2).id(), "must be different");

        lb.shutdown();
    }

    @Test
    void testLeastConnectionsSelect() throws InterruptedException
    {
        SoftawareLoadBallancer lbLeastConnections  = new SoftawareLoadBallancer(Algorithm.LEAST_CONNECTIONS, 5000);
        lbLeastConnections.registerService(serverOne);
        lbLeastConnections.registerService(serverTwo);
        lbLeastConnections.registerService(serverThree);
        lbLeastConnections.registerService(serverFour);
        lbLeastConnections.registerService(serverFive);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(10);
        ExecutorService executor = Executors.newFixedThreadPool(5);

        List<Server> selections = new CopyOnWriteArrayList<>();


        for (int i = 0; i < 10; i++)
        {
            executor.submit(() -> {
                try
                {
                    startLatch.await();
                    selections.add(lbLeastConnections .selectServer(request1));
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                finally
                {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        assertTrue(doneLatch.await(60, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(-1, selections.indexOf(serverOne));

        serverOne.connection().getAndDecrement();
        selections.add(lbLeastConnections.selectServer(request1));
        assertEquals(serverOne, selections.get(10));

        lbLeastConnections.shutdown();
    }

    @Test
    void testWeightedRoundRobinSelect() throws InterruptedException
    {
        SoftawareLoadBallancer lbWeightedRoundRobin = new SoftawareLoadBallancer(Algorithm.WEIGHTED_ROUND_ROBIN, 5000);
        lbWeightedRoundRobin.registerService(serverOne);
        lbWeightedRoundRobin.registerService(serverTwo);
        lbWeightedRoundRobin.registerService(serverThree);
        lbWeightedRoundRobin.registerService(serverFour);
        lbWeightedRoundRobin.registerService(serverFive);

        assertEquals(serverThree, lbWeightedRoundRobin.selectServer(request1));

        lbWeightedRoundRobin.incrementServerWeight(serverTwo);
        lbWeightedRoundRobin.incrementServerWeight(serverThree);
        lbWeightedRoundRobin.incrementServerWeight(serverFive);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(6);
        ExecutorService executor = Executors.newFixedThreadPool(3);

        List<Server> selections = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 6; i++)
        {
            executor.submit(() -> {
                try
                {
                    startLatch.await();
                    selections.add(lbWeightedRoundRobin.selectServer(request1));
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                finally
                {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(60, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(-1, selections.indexOf(serverOne));
        assertEquals(-1, selections.indexOf(serverFour));

        lbWeightedRoundRobin.shutdown();
    }

    @Test
    void concurrencySelection() throws InterruptedException
    {
        lb.registerService(serverOne);
        lb.registerService(serverTwo);
        lb.registerService(serverThree);
        lb.registerService(serverFour);
        lb.registerService(serverFive);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(5);
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        ConcurrentMap<String, Server> selections = new ConcurrentHashMap<>();

        for (int i = 0; i < 5; i++)
        {
            executorService.submit(() -> {
                try
                {
                    startLatch.await();
                    Server selected = lb.selectServer(request1);
                    selections.put(selected.id(), selected);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                finally
                {
                    finishLatch.countDown();
                }
            });

        }

        startLatch.countDown();
        executorService.shutdown();
        boolean done = executorService.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(done);

        assertEquals(5, selections.size(), "must be 5");
        assertNotNull(selections.get("s1"));
        assertNotNull(selections.get("s2"));
        assertNotNull(selections.get("s3"));
        assertNotNull(selections.get("s4"));
        assertNotNull(selections.get("s5"));
    }

    @Test
    void testFailCases() throws InterruptedException
    {
        assertThrows(NullPointerException.class, () -> lb.registerService(null));
        assertThrows(NullPointerException.class, () -> lb.deregisterService(null));
        assertThrows(IllegalStateException.class, () -> lb.selectServer(request1));
        lb.shutdown();

        SoftawareLoadBallancer lbWeightedRoundRobin = new SoftawareLoadBallancer(Algorithm.WEIGHTED_ROUND_ROBIN, 5000);
        lbWeightedRoundRobin.registerService(serverOne);
        lbWeightedRoundRobin.decrementServerWeight(serverOne);
        assertThrows(IllegalStateException.class, () -> lbWeightedRoundRobin.selectServer(request1));
        lbWeightedRoundRobin.shutdown();
    }
}