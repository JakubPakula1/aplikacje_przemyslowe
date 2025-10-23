package com.github.jakubpakula1.lab.model;

public class CompanyStatistics {
    private int numberOfEmployees;
    private double averageSalary;
    private String bestEarningName;

    public CompanyStatistics(int numberOfEmployees, double averageSalary, String bestEarningName) {
        this.numberOfEmployees = numberOfEmployees;
        this.averageSalary = averageSalary;
        this.bestEarningName = bestEarningName;
    }

    public int getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(int numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    public double getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(int averageSalary) {
        this.averageSalary = averageSalary;
    }

    public String getBestEarningName() {
        return bestEarningName;
    }

    public void setBestEarningName(String bestEarningName) {
        this.bestEarningName = bestEarningName;
    }

    @Override
    public String toString() {
        return "CompanyStatistics{" +
                "numberOfEmployees=" + numberOfEmployees +
                ", averageSalary=" + averageSalary +
                ", bestEarningName='" + bestEarningName + '\'' +
                '}';
    }
}
