package com.program.training.currencyservice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author naletov
 */
public class CurrencyServiceImpl implements CurrencyService
{
    private final ConcurrentMap<String, BigDecimal> directRates;
    private final boolean allowTriangularConversion;

    public CurrencyServiceImpl()
    {
        this( true);
    }

    public CurrencyServiceImpl(boolean allowTriangularConversion)
    {
        this.directRates = new ConcurrentHashMap<>();
        this.allowTriangularConversion = allowTriangularConversion;
    }

    @Override
    public void initialization(Map<String, BigDecimal> initRates)
    {
        if (initRates == null || initRates.isEmpty())
        {
            throw new IllegalArgumentException("initial rates is null or empty");
        }

        directRates.clear();

        for (Map.Entry<String, BigDecimal> entry : initRates.entrySet())
        {
            String key = entry.getKey();
            BigDecimal rate = entry.getValue();
            validateRateKey(key);
            validateRate(key, rate);

            directRates.put(entry.getKey(), entry.getValue());
        }

    }

    @Override
    public void updateRate(String from, String to, BigDecimal rate)
    {
        validateCurrencyCode(from);
        validateCurrencyCode(to);
        String key = from + SEPARATOR + to;
        validateRate(key, rate);

        directRates.put(key, rate);
    }



    private void validateCurrencyCode(String currencyCode)
    {
        if (currencyCode == null || currencyCode.isBlank())
        {
            throw new IllegalArgumentException("from is null or empty");
        }
        if (!currencyCode.matches("[A-Za-z]{3}"))
        {
            throw new IllegalArgumentException("currency code is invalid");
        }
        CurrencyCodes.fromString(currencyCode);
    }

    private void validateRateKey(String key)
    {
        if (key == null || key.isBlank())
            throw new IllegalArgumentException("initial rate key is null or empty");
        if (!key.matches("[A-Za-z]{3}/[A-Za-z]{3}"))
            throw new IllegalArgumentException("initial rate key contains illegal characters");
        String[] currencies = key.split(SEPARATOR);
        if (currencies.length != 2 || currencies[0].equals(currencies[1]))
            throw new IllegalArgumentException("initial rate key contains illegal characters or this is the same currency");
    }

    private void validateRate(String key, BigDecimal rate)
    {
        if (rate == null)
            throw new IllegalArgumentException("initial rate is null: " + key);
        if (rate.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("initial rate is negative or zero: " + key);
    }

    @Override
    public BigDecimal getRate(String from, String to) throws IllegalArgumentException
    {
        validateCurrencyCode(from);
        validateCurrencyCode(to);

        // direct rate
        if (directRates.containsKey(from + SEPARATOR + to))
            return directRates.get(from + SEPARATOR + to);

        // Reverse rate
        if (directRates.containsKey(to + SEPARATOR + from))
        {
            return BigDecimal.ONE.
                    divide(directRates.get(to + SEPARATOR + from), 2, RoundingMode.HALF_UP);
        }

        // triangular via BASE
        if (allowTriangularConversion)
        {
            throw new UnsupportedOperationException("Triangular conversion is not yet supported");
        }

        // there is no such rate
        throw new IllegalArgumentException("initial rate is absent: " + from + SEPARATOR + to);
    }

    @Override
    public BigDecimal convertRate(String from, String to, BigDecimal amount) throws IllegalArgumentException
    {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0 || amount.compareTo(BigDecimal.ZERO) < 0)
            throw  new IllegalArgumentException("initial amount is negative or zero: " + from + SEPARATOR + to);

        return getRate(from, to).multiply(amount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Set<String> getAvailableCurrencies()
    {
        return directRates.keySet().stream()
                .flatMap(k -> Set.of(k.split(SEPARATOR)).stream())
                .collect(Collectors.toUnmodifiableSet());
    }
}