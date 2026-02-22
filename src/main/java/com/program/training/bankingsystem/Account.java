package com.program.training.bankingsystem;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author naletov
 */
public class Account
{
    private final UUID id;
    private BigDecimal balance;
    public final Lock lock = new ReentrantLock();

    public Account(UUID id, BigDecimal balance)
    {
        this.id = id;
        this.balance = balance;
    }

    public UUID getId()
    {
        return id;
    }

    public BigDecimal getBalance()
    {
        return balance;
    }

    public void setBalance(BigDecimal balance)
    {
        this.balance = balance;
    }
}
