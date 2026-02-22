package com.program.training.currencyservice;

/**
 * @author naletov
 */
public enum CurrencyCodes
{
    USD,
    EUR,
    GBP,
    RUB,
    CHF,
    CAD,
    JPY,
    CNY,
    AUD,
    CZK,
    DKK,
    DOP,
    NZD,
    NOK,
    PLN;

    public static CurrencyCodes fromString(String s)
    {
        try
        {
            return CurrencyCodes.valueOf(s.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
}
