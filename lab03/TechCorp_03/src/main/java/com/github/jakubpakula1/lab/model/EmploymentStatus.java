// java
package com.github.jakubpakula1.lab.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum EmploymentStatus {
    ACTIVE,
    ON_LEAVE,
    TERMINATED;

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static EmploymentStatus fromValue(String value) {
        if (value == null) return null;
        try {
            return EmploymentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}