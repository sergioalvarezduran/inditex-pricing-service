package com.inditex.pricing.domain.price;

import com.inditex.pricing.domain.shared.Validator;

public record BrandId(long value) {

    public static BrandId of(long value) {
        Validator.create()
                .requirePositive(value, "brandId")
                .throwIfInvalid();
        return new BrandId(value);
    }
}