package com.program.training.bankingsystem;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A bank account used in the concurrent money-transfer problem.
 *
 * <p>Each account has a unique identifier and a mutable balance. Accounts are
 * shared between threads when {@link AccountService} performs transfers, so each
 * instance exposes a {@link #lock} that the service acquires in a fixed order
 * (by account ID) to update balances safely and avoid deadlocks.</p>
 *
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
