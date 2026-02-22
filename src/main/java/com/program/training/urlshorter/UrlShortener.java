package com.program.training.urlshorter;

/**
 * @author naletov
 */
public interface UrlShortener
{
    String shorten(String longUrl);
    String getOriginalUrl(String code);
}
