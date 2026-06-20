package com.program.training.urlshorter;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base-62 URL shortening service backed by two {@link ConcurrentHashMap} indexes.
 *
 * <h2>Encoding</h2>
 * <p>Each new URL is assigned a monotonically increasing sequence number (starting at
 * {@code 1_000_000} to guarantee at least 4 characters in the output) and encoded into a
 * base-62 string using the alphabet
 * {@code abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789}.
 *
 * <h2>Idempotency</h2>
 * <p>{@link #shorten} uses {@link ConcurrentHashMap#computeIfAbsent} to atomically assign
 * codes, so concurrent calls with the same URL always return the same code without
 * producing duplicates.
 *
 * <h2>Thread safety</h2>
 * <p>Both maps are {@link ConcurrentHashMap} instances and the sequence counter is an
 * {@link AtomicLong}, making all operations safe for concurrent use without external locking.
 *
 * @author naletov
 * @see UrlShortener
 */
public class UrlShortenerService implements UrlShortener
{
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALPHABET.length();

    private static final SecureRandom random = new SecureRandom();

    private final Map<String, String> codeToUrl = new ConcurrentHashMap<>();
    private final Map<String, String> urlToCode = new ConcurrentHashMap<>();

    // Counter for generating unique IDs (Starting with a million for the code length)
    private final AtomicLong sequence = new AtomicLong(1000000L);

    /** {@inheritDoc} */
    @Override
    public String shorten(String longUrl)
    {
        if (longUrl == null || longUrl.isBlank())
        {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        return urlToCode.computeIfAbsent(longUrl, url -> {
            String code = encode(sequence.getAndIncrement());
            codeToUrl.put(code, url);
            return code;
        });
    }

    /** {@inheritDoc} */
    @Override
    public String getOriginalUrl(String code)
    {
        String url = codeToUrl.get(code);
        if (url == null) {
            throw new IllegalArgumentException("Invalid short code");
        }
        return url;
    }

    // Convert num to String
    private String encode(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(ALPHABET.charAt((int) (num % BASE)));
            num /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * Generates a random string of the specified length by sampling characters from the
     * given source URL.
     *
     * @param length the number of characters in the result; must be positive
     * @param url    the source string to sample characters from; must not be empty
     * @return a random string of exactly {@code length} characters
     */
    public String generateRandomString(int length, String url)
    {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++)
        {
            result.append(url.charAt(random.nextInt(url.length())));
        }
        return result.toString();
    }
}