package com.program.training.currencyservice;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * @author naletov
 */
public interface CurrencyService
{
    String SEPARATOR = "/";
    String BASE_CURRENCY = "USD";

    /**
     * initialization
     * @param rates from/to -> rate
     */
    void initialization(Map<String, BigDecimal> rates);

    /**
     * update sate
     * @param from
     * @param to
     * @param rate from/to
     */
    void updateRate(String from, String to, BigDecimal rate);

    /**
     *
     * @param from
     * @param to
     * @return rate
     * @throws IllegalArgumentException - in case of unknown currency
     */
    BigDecimal getRate(String from, String to) throws IllegalArgumentException;

    /**
     *
     * @param from
     * @param to
     * @param amount
     * @return
     * @throws IllegalArgumentException - in case of unknown currency
     */
    BigDecimal convertRate(String from, String to, BigDecimal amount) throws IllegalArgumentException;

    /**
     * returns available currencies
     * @return
     */
    Set<String> getAvailableCurrencies();
}
