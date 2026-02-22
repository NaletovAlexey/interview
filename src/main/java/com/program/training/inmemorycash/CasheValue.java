package com.program.training.inmemorycash;

/**
 * @author naletov
 */
public record CasheValue<V>(V value, long createdAt)
{
}
