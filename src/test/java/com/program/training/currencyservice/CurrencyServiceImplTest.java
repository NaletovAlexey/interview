package com.program.training.currencyservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author naletov
 */
@ExtendWith(MockitoExtension.class)
class CurrencyServiceImplTest
{

    private CurrencyService currencyService;

    @BeforeEach
    void setUp()
    {
        currencyService = new CurrencyServiceImpl();
        currencyService.initialization(Map.of(
                "USD/EUR", new BigDecimal("0.85"),
                "EUR/USD", new BigDecimal("1.176"),
                "USD/RUB", new BigDecimal("90.0"),
                "EUR/RUB", new BigDecimal("100.0")
        ));
    }

    @Test
    void testDirectConvert()
    {
        BigDecimal expectedEUR = new BigDecimal("85.00");
        BigDecimal expectedEURwoAcc = new BigDecimal("85");
        BigDecimal amountUS = new BigDecimal("100");
        assertEquals(expectedEUR, currencyService.convertRate("USD", "EUR", amountUS));
        assertEquals(0, expectedEURwoAcc.compareTo(currencyService.convertRate("USD", "EUR", amountUS)));
    }

    @Test
    void testReverseConvert()
    {
        BigDecimal expectedEUR = new BigDecimal("100");
        BigDecimal amountRUB = new BigDecimal("10000");
        assertEquals(0, expectedEUR.compareTo(currencyService.convertRate("RUB", "EUR", amountRUB)));
    }

    @Test
    void testInvalidRateCurrency()
    {
        BigDecimal amountRUB = new BigDecimal("10000");
        BigDecimal negativeAmount = new BigDecimal("-1");

        assertThrows(IllegalArgumentException.class, () -> currencyService.convertRate("RRR", "EUR", amountRUB));
        assertThrows(IllegalArgumentException.class, () -> currencyService.convertRate("RUBS", "EUR", amountRUB));
        assertThrows(IllegalArgumentException.class, () -> currencyService.convertRate("", "EUR", amountRUB));
        assertThrows(IllegalArgumentException.class, () -> currencyService.convertRate("   ", "EUR", amountRUB));
        assertThrows(IllegalArgumentException.class, () -> currencyService.convertRate("RUB", "EUR", null));
        assertThrows(IllegalArgumentException.class, () -> currencyService.convertRate("RUB", "EUR", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> currencyService.convertRate("RUB", "EUR", negativeAmount));
    }

    @Test
    void testUpdateRateCurrency()
    {
        BigDecimal expectedEUR = new BigDecimal("90.00");
        BigDecimal amountRUB = new BigDecimal("10000");
        BigDecimal newRate = new BigDecimal("0.009");
        currencyService.updateRate("RUB", "EUR", newRate);

        assertEquals(expectedEUR, currencyService.convertRate("RUB", "EUR", amountRUB));
    }

    @Test
    void testConcurrentConvertAndUpdate() throws InterruptedException
    {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch compilationLatch = new CountDownLatch(25);

        ExecutorService executorConvert = Executors.newFixedThreadPool(25);

        final BigDecimal amountRUB = new BigDecimal("10000");
        final BigDecimal expectedEUR = new BigDecimal("100.00");
        for (int i = 0; i < 25; i++)
        {
            executorConvert.submit(
                    () -> {
                        try
                        {
                            startLatch.await();
                            assertEquals(expectedEUR, currencyService.convertRate("RUB", "EUR", amountRUB));
                        } catch (InterruptedException e)
                        {
                            throw new RuntimeException(e);
                        }
                        finally
                        {
                            compilationLatch.countDown();
                        }
                    }
            );
        }
        startLatch.countDown();
        executorConvert.shutdown();
        boolean terminated = executorConvert.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(terminated);
    }
}