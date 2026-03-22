package com.program.training.streamapi;

/**
 * @author naletov
 */
public enum TransactionType
{
    BUY(1),
    SELL(-1);

    private final int value;
    TransactionType(int value)
    {
        this.value = value;
    }

    public String value()
    {
        return name();
    }

    public int getValue()
    {
        return value;
    }
}
