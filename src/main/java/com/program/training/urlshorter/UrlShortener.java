package com.program.training.urlshorter;

/**
 * Contract for a URL shortening service.
 *
 * <p>Given a long URL, {@link #shorten} returns a compact code that can later be resolved
 * back to the original URL via {@link #getOriginalUrl}.
 *
 * @author naletov
 * @see UrlShortenerService
 */
public interface UrlShortener
{
    /**
     * Returns a short code that uniquely identifies the given URL.
     *
     * <p>Repeated calls with the same URL must return the same code (idempotent).
     *
     * @param longUrl the original URL to shorten; must not be {@code null} or blank
     * @return a compact, unique short code
     * @throws IllegalArgumentException if {@code longUrl} is {@code null} or blank
     */
    String shorten(String longUrl);

    /**
     * Resolves a short code back to the original URL.
     *
     * @param code the short code previously returned by {@link #shorten}
     * @return the original long URL
     * @throws IllegalArgumentException if {@code code} is not registered
     */
    String getOriginalUrl(String code);
}
