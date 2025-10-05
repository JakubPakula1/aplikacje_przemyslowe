package com.github.jakubpakula1.lab.modules;

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
    public int getBaseSalary() {
        return baseSalary;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }
}
