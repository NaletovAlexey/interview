package com.program.training.loadballancer;

/**
 * Immutable value object representing an inbound HTTP request.
 *
 * <p>Carries the client's IP address, which is used by the {@link Algorithm#IP_HASH}
 * strategy to achieve session affinity without server-side session storage.
 *
 * @param clientIP the IPv4 or IPv6 address of the originating client; must not be {@code null}
 * @author naletov
 */
public record HttpRequest(String clientIP)
{
}
