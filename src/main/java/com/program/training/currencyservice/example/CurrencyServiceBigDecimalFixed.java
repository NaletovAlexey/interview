package com.program.training.currencyservice.example;

/**
 * @author naletov
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class CurrencyServiceBigDecimalFixed implements CurrencyServiceExample
{

    public static final String BASE_CURRENCY = "USD";

    // Основная мапа: Key = "FROM/TO", Value = курс
    private final ConcurrentMap<String, BigDecimal> directRates;

    // Опционально: кэш обратных курсов (для производительности)
    // Необязательно, но ускоряет частые обратные конвертации
    private final ConcurrentMap<String, BigDecimal> inverseRatesCache;

    // Флаг: разрешать ли triangular arbitrage (конвертацию через промежуточные валюты)
    private final boolean allowTriangularConversion;

    public CurrencyServiceBigDecimalFixed() {
        this(true);
    }

    public CurrencyServiceBigDecimalFixed(boolean allowTriangularConversion) {
        this.directRates = new ConcurrentHashMap<>();
        this.inverseRatesCache = new ConcurrentHashMap<>();
        this.allowTriangularConversion = allowTriangularConversion;
    }

    @Override
    public void initialize(Map<String, BigDecimal> initialRates) {
        if (initialRates == null) {
            throw new IllegalArgumentException("Initial rates must not be null");
        }

        directRates.clear();
        inverseRatesCache.clear();

        for (Map.Entry<String, BigDecimal> entry : initialRates.entrySet()) {
            String key = entry.getKey();
            BigDecimal rate = entry.getValue();
            validateRateKey(key);
            validateRate(key, rate);

            directRates.put(key, rate);
            // Не кэшируем обратный при инициализации - будем вычислять lazily
        }
    }

    @Override
    public void updateRate(String fromCurrency, String toCurrency, BigDecimal rate) {
        validateCurrencyCode(fromCurrency);
        validateCurrencyCode(toCurrency);

        String key = fromCurrency + "/" + toCurrency;
        validateRate(key, rate);

        // Обновляем прямой курс
        directRates.put(key, rate);

        // Инвалидируем кэш обратных курсов, которые могут зависеть
        String inverseKey = toCurrency + "/" + fromCurrency;
        inverseRatesCache.remove(inverseKey);

        // Также инвалидируем смежные кэши (если есть other->from или to->other)
        // Это упрощенная инвалидация, в реальности нужен более умный подход
        inverseRatesCache.keySet().removeIf(k ->
                k.startsWith(fromCurrency + "/") || k.endsWith("/" + toCurrency)
        );
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        validateAmount(amount);
        validateCurrencyCode(fromCurrency);
        validateCurrencyCode(toCurrency);

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(
                    BaseClass.Currency.fromCode(toCurrency).getScale(),
                    RoundingMode.HALF_UP
            );
        }

        if (fromCurrency.equals(toCurrency)) {
            return roundToCurrencyScale(amount, toCurrency);
        }

        // ПРИОРИТЕТ 1: Прямой курс (from/to)
        String directKey = fromCurrency + "/" + toCurrency;
        BigDecimal directRate = directRates.get(directKey);
        if (directRate != null) {
            return multiplyAndRound(amount, directRate, toCurrency);
        }

        // ПРИОРИТЕТ 2: Обратный курс (to/from) с явным заданием
        String explicitInverseKey = toCurrency + "/" + fromCurrency;
        BigDecimal explicitInverseRate = directRates.get(explicitInverseKey);
        if (explicitInverseRate != null) {
            // Используем явно заданный обратный курс
            return divideAndRound(amount, explicitInverseRate, toCurrency);
        }

        // ПРИОРИТЕТ 3: Обратный курс из кэша (вычисленный как 1/прямой)
        BigDecimal cachedInverseRate = inverseRatesCache.get(directKey);
        if (cachedInverseRate != null) {
            return multiplyAndRound(amount, cachedInverseRate, toCurrency);
        }

        // ПРИОРИТЕТ 4: Triangular conversion через BASE (если разрешено)
        if (allowTriangularConversion) {
            BigDecimal result = convertViaBase(amount, fromCurrency, toCurrency);
            if (result != null) {
                return result;
            }
        }

        throw new BaseClass.CurrencyConversionException(
                String.format("No conversion path from %s to %s. " +
                                "Available currencies: %s",
                        fromCurrency, toCurrency, getAvailableCurrencies())
        );
    }

    private BigDecimal convertViaBase(BigDecimal amount, String from, String to) {
        // from -> BASE -> to
        if (from.equals(BASE_CURRENCY)) {
            // Просто прямой курс BASE->to
            String baseToKey = BASE_CURRENCY + "/" + to;
            BigDecimal baseToRate = directRates.get(baseToKey);
            if (baseToRate != null) {
                return multiplyAndRound(amount, baseToRate, to);
            }
            return null;
        }

        if (to.equals(BASE_CURRENCY)) {
            // from -> BASE: ищем прямой from/BASE или обратный BASE/from
            String fromBaseKey = from + "/" + BASE_CURRENCY;
            BigDecimal fromBaseRate = directRates.get(fromBaseKey);
            if (fromBaseRate != null) {
                return divideAndRound(amount, fromBaseRate, to);
            }

            String baseFromKey = BASE_CURRENCY + "/" + from;
            BigDecimal baseFromRate = directRates.get(baseFromKey);
            if (baseFromRate != null) {
                // from->BASE = 1 / (BASE/from)
                BigDecimal inverse = BigDecimal.ONE.divide(baseFromRate, 20, RoundingMode.HALF_EVEN);
                return multiplyAndRound(amount, inverse, to);
            }
            return null;
        }

        // Обычный случай: from -> BASE -> to
        // from -> BASE
        String fromBaseKey = from + "/" + BASE_CURRENCY;
        BigDecimal fromBaseRate = directRates.get(fromBaseKey);
        if (fromBaseRate == null) {
            String baseFromKey = BASE_CURRENCY + "/" + from;
            BigDecimal baseFromRate = directRates.get(baseFromKey);
            if (baseFromRate != null) {
                fromBaseRate = BigDecimal.ONE.divide(baseFromRate, 20, RoundingMode.HALF_EVEN);
            } else {
                return null; // нет пути from->BASE
            }
        }

        // BASE -> to
        String baseToKey = BASE_CURRENCY + "/" + to;
        BigDecimal baseToRate = directRates.get(baseToKey);
        if (baseToRate == null) {
            String toBaseKey = to + "/" + BASE_CURRENCY;
            BigDecimal toBaseRate = directRates.get(toBaseKey);
            if (toBaseRate != null) {
                baseToRate = BigDecimal.ONE.divide(toBaseRate, 20, RoundingMode.HALF_EVEN);
            } else {
                return null; // нет пути BASE->to
            }
        }

        // Общая комбинация
        BigDecimal combinedRate = fromBaseRate.multiply(baseToRate);
        return multiplyAndRound(amount, combinedRate, to);
    }

    @Override
    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        validateCurrencyCode(fromCurrency);
        validateCurrencyCode(toCurrency);

        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        // Прямой курс
        String directKey = fromCurrency + "/" + toCurrency;
        BigDecimal directRate = directRates.get(directKey);
        if (directRate != null) {
            return directRate;
        }

        // Обратный курс (явно заданный)
        String inverseKey = toCurrency + "/" + fromCurrency;
        BigDecimal inverseRate = directRates.get(inverseKey);
        if (inverseRate != null) {
            return BigDecimal.ONE.divide(inverseRate, 20, RoundingMode.HALF_EVEN);
        }

        // Кэшированный обратный (вычисленный)
        BigDecimal cachedInverse = inverseRatesCache.get(directKey);
        if (cachedInverse != null) {
            return cachedInverse;
        }

        // Triangular через BASE
        if (allowTriangularConversion) {
            BigDecimal viaBase = computeRateViaBase(fromCurrency, toCurrency);
            if (viaBase != null) {
                return viaBase;
            }
        }

        throw new BaseClass.CurrencyNotFoundException(
                String.format("Rate not found for %s/%s. Available: %s",
                        fromCurrency, toCurrency, directRates.keySet())
        );
    }

    private BigDecimal computeRateViaBase(String from, String to) {
        // from -> BASE -> to
        if (from.equals(BASE_CURRENCY)) {
            String baseToKey = BASE_CURRENCY + "/" + to;
            return directRates.get(baseToKey);
        }

        if (to.equals(BASE_CURRENCY)) {
            String fromBaseKey = from + "/" + BASE_CURRENCY;
            BigDecimal fromBaseRate = directRates.get(fromBaseKey);
            if (fromBaseRate != null) {
                return BigDecimal.ONE.divide(fromBaseRate, 20, RoundingMode.HALF_EVEN);
            }

            String baseFromKey = BASE_CURRENCY + "/" + from;
            BigDecimal baseFromRate = directRates.get(baseFromKey);
            if (baseFromRate != null) {
                return BigDecimal.ONE.divide(baseFromRate, 20, RoundingMode.HALF_EVEN);
            }
            return null;
        }

        // from -> BASE
        String fromBaseKey = from + "/" + BASE_CURRENCY;
        BigDecimal fromBaseRate = directRates.get(fromBaseKey);
        if (fromBaseRate == null) {
            String baseFromKey = BASE_CURRENCY + "/" + from;
            BigDecimal baseFromRate = directRates.get(baseFromKey);
            if (baseFromRate != null) {
                fromBaseRate = BigDecimal.ONE.divide(baseFromRate, 20, RoundingMode.HALF_EVEN);
            } else {
                return null;
            }
        }

        // BASE -> to
        String baseToKey = BASE_CURRENCY + "/" + to;
        BigDecimal baseToRate = directRates.get(baseToKey);
        if (baseToRate == null) {
            String toBaseKey = to + "/" + BASE_CURRENCY;
            BigDecimal toBaseRate = directRates.get(toBaseKey);
            if (toBaseRate != null) {
                baseToRate = BigDecimal.ONE.divide(toBaseRate, 20, RoundingMode.HALF_EVEN);
            } else {
                return null;
            }
        }

        return fromBaseRate.multiply(baseToRate);
    }

    @Override
    public Set<String> getAvailableCurrencies() {
        return directRates.keySet().stream()
                .flatMap(key -> Set.of(key.split("/")).stream())
                .collect(Collectors.toSet());
    }

    public int getDirectRateCount() {
        return directRates.size();
    }

    // Вспомогательные методы (остаются без изменений)
    private BigDecimal multiplyAndRound(BigDecimal amount, BigDecimal rate, String toCurrency) {
        BigDecimal result = amount.multiply(rate);
        return roundToCurrencyScale(result, toCurrency);
    }

    private BigDecimal divideAndRound(BigDecimal amount, BigDecimal rate, String toCurrency) {
        BigDecimal result = amount.divide(rate, 20, RoundingMode.HALF_EVEN);
        return roundToCurrencyScale(result, toCurrency);
    }

    private BigDecimal roundToCurrencyScale(BigDecimal value, String currencyCode) {
        BaseClass.Currency currency = BaseClass.Currency.fromCode(currencyCode);
        return value.setScale(currency.getScale(), RoundingMode.HALF_UP);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (amount.scale() > 10) {
            throw new IllegalArgumentException("Amount scale too high (max 10)");
        }
    }

    private void validateCurrencyCode(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency code must not be null or blank");
        }
        if (!currency.matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("Invalid currency code format: " + currency);
        }
        try {
            BaseClass.Currency.fromCode(currency);
        } catch (BaseClass.CurrencyNotFoundException e) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
    }

    private void validateRateKey(String key) {
        if (!key.matches("[A-Za-z]{3}/[A-Za-z]{3}")) {
            throw new IllegalArgumentException("Invalid rate key format: " + key);
        }
        String[] parts = key.split("/");
        if (parts[0].equals(parts[1])) {
            throw new IllegalArgumentException("Cannot set rate for same currency: " + key);
        }
    }

    private void validateRate(String key, BigDecimal rate) {
        if (rate == null) {
            throw new IllegalArgumentException("Rate must not be null for key: " + key);
        }
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Rate must be positive for key: " + key);
        }
        if (rate.scale() > 12) {
            throw new IllegalArgumentException("Rate scale too high for key: " + key + " (max 12)");
        }
        if (rate.compareTo(new BigDecimal("0.000001")) < 0) {
            throw new IllegalArgumentException("Rate too small for key: " + key);
        }
        if (rate.compareTo(new BigDecimal("10000")) > 0) {
            throw new IllegalArgumentException("Rate too large for key: " + key);
        }
    }
}
