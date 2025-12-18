package com.github.jakubpakula1.lab.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.util.Locale;

public enum Position {
    PREZES(BigDecimal.valueOf(25000.00), 1),
    WICEPREZES(BigDecimal.valueOf(18000), 2),
    MANAGER(BigDecimal.valueOf(12000), 3),
    PROGRAMISTA(BigDecimal.valueOf(8000), 4),
    STAZYSTA(BigDecimal.valueOf(3000), 5);

    private final BigDecimal baseSalary;
    private final int hierarchyLevel;

    Position(BigDecimal baseSalary, int hierarchyLevel) {
        this.baseSalary = baseSalary;
        this.hierarchyLevel = hierarchyLevel;
    }

    public BigDecimal getBaseSalary() { return baseSalary; }
    public int getHierarchyLevel() { return hierarchyLevel; }

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static Position fromValue(String value) {
        if (value == null) return null;
        try {
            return Position.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}