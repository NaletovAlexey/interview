package com.program.training.bankingsystem;

import java.math.BigDecimal;

/**
 * Thread-safe service for transferring money between bank accounts.
 *
 * <p>This class solves a classic concurrent banking problem: multiple threads may
 * transfer funds between accounts at the same time, and the implementation must
 * keep balances correct (no lost updates), reject invalid operations, and avoid
 * deadlocks when two transfers involve the same pair of accounts in opposite
 * directions.</p>
 *
 * <p>Deadlock is prevented by ordered locking — both accounts involved in a transfer
 * are locked in a consistent order (by {@linkplain Account#getId() account ID}), so
 * concurrent transfers always acquire locks in the same sequence.</p>
 *
 * @author naletov
 */
public class AccountService
{

    public void transfer(Account from, Account to, BigDecimal amount)
    {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("negative amount");

        if (from.getId().equals(to.getId()))
            throw new IllegalArgumentException("operation with the same Account");

        Account first = from.getId().compareTo(to.getId()) < 0 ? from : to;
        Account second = from.getId().compareTo(to.getId()) < 0 ? to : from;

        first.lock.lock();
        try
        {
            second.lock.lock();
            try
            {
                if (from.getBalance().compareTo(amount) < 0)
                    throw new IllegalStateException("insufficient balance");
                from.setBalance(from.getBalance().subtract(amount));
                to.setBalance(to.getBalance().add(amount));
            }
            finally
            {
                second.lock.unlock();
            }
        }
        finally
        {
            first.lock.unlock();
        }
    }
}
