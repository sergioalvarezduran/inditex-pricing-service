package com.inditex.pricing.domain.price;

import com.inditex.pricing.domain.shared.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public static Money of(BigDecimal amount, Currency currency) {
        Validator.create()
                .requireNonNull(amount, "amount")
                .requireNonNull(currency, "currency")
                .throwIfInvalid();

        Validator.create()
                .requireTrue(amount.signum() >= 0, "amount", "must be >= 0")
                .throwIfInvalid();

        BigDecimal normalized = normalize(amount);
        return new Money(normalized, currency);
    }

    public static Money eur(BigDecimal amount) {
        return of(amount, Currency.getInstance("EUR"));
    }

    private static BigDecimal normalize(BigDecimal amount) {
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);

        if (Objects.equals(scaled, BigDecimal.valueOf(-0.00).setScale(2, RoundingMode.HALF_UP))) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return scaled;
    }
}