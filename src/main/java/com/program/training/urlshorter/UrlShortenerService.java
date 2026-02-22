package com.program.training.urlshorter;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author naletov
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

    // generate Random by link
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