package com.inditex.pricing.domain.price;

import com.inditex.pricing.domain.shared.Validator;

public record ProductId(long value) {

    public static ProductId of(long value) {
        Validator.create()
                .requirePositive(value, "productId")
                .throwIfInvalid();
        return new ProductId(value);
    }
}