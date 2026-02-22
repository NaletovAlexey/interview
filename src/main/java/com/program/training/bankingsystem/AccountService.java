package com.program.training.bankingsystem;

import java.math.BigDecimal;

/**
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

        if (from.getBalance().compareTo(amount) < 0)
            throw new IllegalStateException("insufficient balance");

        Account first = from.getId().compareTo(to.getId()) < 0 ? from : to;
        Account second = from.getId().compareTo(to.getId()) < 0 ? to : from;

        first.lock.lock();
        try
        {
            from.setBalance(from.getBalance().subtract(amount));
            second.lock.lock();
            try
            {
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
