package com.inditex.pricing.domain.price;

import com.inditex.pricing.domain.shared.Validator;

public record Priority(int value) {

    public static Priority of(int value) {
        Validator.create()
                .requireNonNegative(value, "priority")
                .throwIfInvalid();
        return new Priority(value);
    }
}