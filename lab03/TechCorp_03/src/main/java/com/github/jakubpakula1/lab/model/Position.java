package com.github.jakubpakula1.lab.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum Position {
    PREZES(25000, 1),
    WICEPREZES(18000, 2),
    MANAGER(12000, 3),
    PROGRAMISTA(8000, 4),
    STAZYSTA(3000, 5);

    private final int baseSalary;
    private final int hierarchyLevel;

    Position(int baseSalary, int hierarchyLevel) {
        this.baseSalary = baseSalary;
        this.hierarchyLevel = hierarchyLevel;
    }

    public int getBaseSalary() { return baseSalary; }
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