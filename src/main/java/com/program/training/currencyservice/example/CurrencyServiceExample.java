package com.program.training.currencyservice.example;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * @author naletov
 */
public interface CurrencyServiceExample
{
    void initialize(Map<String, BigDecimal> initialRates);
    void updateRate(String fromCurrency, String toCurrency, BigDecimal rate);
    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) throws BaseClass.CurrencyConversionException;
    BigDecimal getRate(String fromCurrency, String toCurrency) throws BaseClass.CurrencyNotFoundException;
    Set<String> getAvailableCurrencies();
}
