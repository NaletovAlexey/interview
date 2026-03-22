package com.program.training.tradevalidator;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Trade Validation
 * Write a component that checks if there are enough assets in an account to sell
 * There are 100 requests per second. We need to ensure that the balance doesn't go into negative territory (thread-safe)
 *
 * @author naletov
 */
public class TradeValidator
{
    // store balances: Key - Ticker (e.g. "AAPL"), Value - quantity
    private final Map<String, BigDecimal> accountPositions = new ConcurrentHashMap<>();

    // Initialization of balance (for example, from the database at startup)
    public void updatePosition(String ticker, BigDecimal amount) {
        accountPositions.put(ticker, amount);
    }

    /**
     * Validates and executes the trade.
     * If it's a sale, it checks for sufficient volume.
     * @return true If the trade is valid and executed, false - if there are insufficient funds.
     */
    public boolean validateAndExecute(String ticker, BigDecimal quantity) {
         AtomicBoolean isExecuted = new AtomicBoolean(false);

        accountPositions.compute(ticker, (k, currentBalance) -> {
            BigDecimal balance = (currentBalance == null) ? BigDecimal.ZERO : currentBalance;
            BigDecimal result = balance.add(quantity);

            // Check: if sale (quantity < 0) and total < 0 - reject
            if (result.compareTo(BigDecimal.ZERO) < 0) {
                isExecuted.set(false);
                return currentBalance; // return the old balance without changes.
            }

            isExecuted.set(true);
            return result; // record the new balance
        });

        return isExecuted.get();
    }

    public BigDecimal getBalance(String ticker) {
        return accountPositions.getOrDefault(ticker, BigDecimal.ZERO);
    }
}
