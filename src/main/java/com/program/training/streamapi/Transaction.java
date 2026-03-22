package com.program.training.streamapi;

import java.util.Date;
import java.util.UUID;

/**
 * @author naletov
 */
public record Transaction(String securityId, double quantity, Date tradeDate, TransactionType transactionType)
{
}
