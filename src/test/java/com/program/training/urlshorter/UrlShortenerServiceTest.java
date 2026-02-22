package com.program.training.urlshorter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author naletov
 */
class UrlShortenerServiceTest {

    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        service = new UrlShortenerService();
    }

    @Test
    void shouldShortenAndResolveUrl() {
        String longUrl = "https://revolut.com";
        String code = service.shorten(longUrl);

        assertThat(code).isNotNull().hasSizeGreaterThan(3);
        assertThat(service.getOriginalUrl(code)).isEqualTo(longUrl);
    }

    @Test
    void shouldReturnSameCodeForSameUrl() {
        String url = "https://google.com";
        String code1 = service.shorten(url);
        String code2 = service.shorten(url);

        assertThat(code1).isEqualTo(code2);
    }

    @Test
    void testConcurrency() throws InterruptedException {
        int threads = 50;
        int urlsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        Set<String> codes = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int threadIdx = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < urlsPerThread; j++) {
                        String url = "https://example.com/" + threadIdx + "/" + j;
                        codes.add(service.shorten(url));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // All the codes must be uniq
        assertThat(codes).hasSize(threads * urlsPerThread);
    }

    @Test
    void testRandomGenerator()
    {
        String longUrlOne = "https://revolut.com";
        String longUrlTwo = "https://google.com";
        String codeOne = service.generateRandomString(5, longUrlOne);
        String codeTwo = service.generateRandomString(5, longUrlTwo);
        assertThat(codeOne).isNotNull().hasSize(5);
        assertThat(codeTwo).isNotNull().hasSize(5);
        assertNotEquals(codeOne, codeTwo);
        codeOne = service.generateRandomString(7, longUrlOne);
        codeTwo = service.generateRandomString(7, longUrlTwo);
        assertThat(codeOne).isNotNull().hasSize(7);
        assertThat(codeTwo).isNotNull().hasSize(7);
        assertNotEquals(codeOne, codeTwo);
        codeOne = service.generateRandomString(7, longUrlOne);
        codeTwo = service.generateRandomString(7, longUrlOne);
        assertThat(codeOne).isNotNull().hasSize(7);
        assertThat(codeTwo).isNotNull().hasSize(7);
        assertNotEquals(codeOne, codeTwo);
    }
}