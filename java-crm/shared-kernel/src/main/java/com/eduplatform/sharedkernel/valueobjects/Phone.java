package com.eduplatform.sharedkernel.valueobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class Phone {
    private static final String PHONE_PATTERN = "^[+]?[0-9]{10,15}$";

    @NotBlank
    @Pattern(regexp = PHONE_PATTERN)
    private final String value;

    public Phone(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone cannot be blank");
        }
        if (!value.matches(PHONE_PATTERN)) {
            throw new IllegalArgumentException("Invalid phone format");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return value.equals(phone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
