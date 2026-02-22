package com.program.training.bankingsystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author naletov
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private AccountService accountService;
    private Account one;
    private Account two;

    @BeforeEach
    void setUp()
    {
        accountService = new AccountService();
        one = new Account(UUID.randomUUID(), new BigDecimal("100"));
        two = new Account(UUID.randomUUID(), new BigDecimal("200"));
    }

    @Test
    void testTransferMoney() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(
                () -> {
                    try {
                        startLatch.await();
                        accountService.transfer(one, two, new BigDecimal("50"));
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        executorService.submit(
                () -> {
                    try {
                        startLatch.await();
                        accountService.transfer(two, one, new BigDecimal("100"));
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        startLatch.countDown();
        executorService.shutdown();
        boolean done = executorService.awaitTermination(2, TimeUnit.SECONDS);
        assertTrue(done);

        assertEquals(0, one.getBalance().compareTo(two.getBalance()));
    }

    @Test
    void testTransferMoneyRaceCondition() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        for (int i = 0; i < 100; i++)
        {
            executorService.submit(
                    () -> {
                        try {
                            startLatch.await();
                            accountService.transfer(one, two, new BigDecimal("1"));
                        }
                        catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

        startLatch.countDown();
        executorService.shutdown();
        boolean done = executorService.awaitTermination(2, TimeUnit.SECONDS);
        assertTrue(done);

        assertEquals(0, one.getBalance().compareTo(BigDecimal.ZERO));
        assertEquals(0, two.getBalance().compareTo(new BigDecimal("300")));
    }

    @Test
    void testNegativeCases()
    {
        assertThrows(IllegalArgumentException.class, () -> accountService.transfer(one, two, new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> accountService.transfer(one, one, new BigDecimal("100")));
        assertThrows(IllegalStateException.class, () -> accountService.transfer(one, two, new BigDecimal("1000")));
    }
}