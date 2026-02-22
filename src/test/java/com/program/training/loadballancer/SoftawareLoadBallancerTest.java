package com.program.training.loadballancer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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

    @BeforeEach
    void setUp()
    {
        lb = new SoftawareLoadBallancer(Algorithm.ROUND_ROBIN, 5000);
        serverOne = new Server("s1", "localhot", 8080);
        serverTwo = new Server("s2", "localhot", 8081);
        serverThree = new Server("s3", "localhot", 8082);
        serverFour = new Server("s4", "localhot", 8083);
        serverFive = new Server("s5", "localhot", 8085);

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
            selections.add(lb.selectServer());
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
            selections.add(lb.selectServer());
        }
        assertEquals(selections.get(0).id(), selections.get(3).id(), "must be the same");
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
                executorService.submit(
                        () -> {
                            try
                            {
                                startLatch.await();
                                Server selected = lb.selectServer();
                                selections.put(selected.id(), selected);
                            } catch (InterruptedException e)
                            {
                                throw new RuntimeException(e);
                            }
                            finally
                            {
                                finishLatch.countDown();
                            }
                        }
                );

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
    void testFailCases()
    {
        assertThrows(NullPointerException.class, () -> lb.registerService(null));
        assertThrows(NullPointerException.class, () -> lb.deregisterService(null));
        assertThrows(IllegalStateException.class, () -> lb.selectServer());

        SoftawareLoadBallancer lbIPHash = new SoftawareLoadBallancer(Algorithm.IP_HASH, 5000);
        lbIPHash.registerService(serverOne);
        assertThrows(UnsupportedOperationException.class, lbIPHash::selectServer);
    }
}