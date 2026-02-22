package com.program.training.currencyservice.example;

/**
 * @author naletov
 */
public class BaseClass
{
    // Exceptions
    public static class CurrencyConversionException extends RuntimeException {
        public CurrencyConversionException(String message) {
            super(message);
        }
    }

    public static class CurrencyNotFoundException extends RuntimeException {
        public CurrencyNotFoundException(String message) {
            super(message);
        }
    }

    // Currency Enum with scale information
    public enum Currency {
        USD(2), EUR(2), GBP(2), JPY(0), CHF(2), AUD(2), CAD(2), CNY(2),
        INR(2), RUB(2), BRL(2), ZAR(2), SEK(2), NOK(2), DKK(2),
        PLN(2), TRY(2), HKD(2), SGD(2), KRW(0), THB(2), MXN(2);

        private final int scale;

        Currency(int scale) {
            this.scale = scale;
        }

        public int getScale() {
            return scale;
        }

        public static Currency fromCode(String code) {
            try {
                return Currency.valueOf(code.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CurrencyNotFoundException("Unsupported currency: " + code);
            }
        }
    }
}
