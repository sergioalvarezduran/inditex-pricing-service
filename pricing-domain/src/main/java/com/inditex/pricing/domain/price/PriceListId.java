package com.inditex.pricing.domain.price;

import com.inditex.pricing.domain.shared.Validator;

public record PriceListId(long value) {

    public static PriceListId of(long value) {
        Validator.create()
                .requirePositive(value, "priceListId")
                .throwIfInvalid();
        return new PriceListId(value);
    }
}