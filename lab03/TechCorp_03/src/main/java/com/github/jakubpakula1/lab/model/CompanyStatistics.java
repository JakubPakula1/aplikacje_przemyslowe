package com.github.jakubpakula1.lab.model;

public class CompanyStatistics {
    private String company;
    private int numberOfEmployees;
    private double averageSalary;
    private String bestEarningName;

    public CompanyStatistics(String company, int numberOfEmployees, double averageSalary, String bestEarningName) {
        this.company = company;
        this.numberOfEmployees = numberOfEmployees;
        this.averageSalary = averageSalary;
        this.bestEarningName = bestEarningName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
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
                "company='" + company + '\'' +
                ", numberOfEmployees=" + numberOfEmployees +
                ", averageSalary=" + averageSalary +
                ", bestEarningName='" + bestEarningName + '\'' +
                '}';
    }

}
