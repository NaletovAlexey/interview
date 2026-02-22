package com.program.training.transaction;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author naletov
 */
class TransactionTest
{
    @Test
    void testCalculationFeeForWithdraw()
    {
        Transaction testTransaction = new Transaction(new BigDecimal("100"), TransactionType.WITHDRAW);
        assertEquals(new BigDecimal("5.00"), testTransaction.calculateFee(), "Calculated fee for WITHDRAW transaction: 5.0");
    }

    @Test
    void testCalculationFeeForDeposit()
    {
        Transaction testTransaction = new Transaction(new BigDecimal("100"), TransactionType.DEPOSIT);
        assertEquals(new BigDecimal("0"), testTransaction.calculateFee(), "Calculated fee for DEPOSIT transaction: 0.0");
    }

    @Test
    void testCalculationFeeForUnknown()
    {
        Transaction testTransaction = new Transaction(new BigDecimal("100"), TransactionType.UNKNOWN);
        Exception exception = assertThrows(IllegalArgumentException.class, testTransaction::calculateFee);
        assertEquals("Unknown transaction type: " +  TransactionType.UNKNOWN, exception.getMessage());
    }
}