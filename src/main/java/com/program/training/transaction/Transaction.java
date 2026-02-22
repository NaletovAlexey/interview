package com.program.training.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author naletov
 */
public class Transaction
{
    private final BigDecimal amount;
    private final TransactionType type;

    public Transaction(BigDecimal amount, TransactionType type)
    {
        this.amount = amount;
        this.type = type;
    }

    public BigDecimal calculateFee()
    {
        if (TransactionType.WITHDRAW.equals(type))
        {
            return amount.multiply(new BigDecimal("0.05").setScale(2, RoundingMode.HALF_UP)); // 5% fee
        }
        else if (TransactionType.DEPOSIT.equals(type))
        {
            return BigDecimal.ZERO;     // no fee
        }
        throw new IllegalArgumentException("Unknown transaction type: " + type);
    }
}
