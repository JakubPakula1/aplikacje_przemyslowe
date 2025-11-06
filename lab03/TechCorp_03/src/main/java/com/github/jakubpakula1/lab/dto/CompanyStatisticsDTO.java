package com.github.jakubpakula1.lab.dto;

import java.util.Objects;

public class CompanyStatisticsDTO {
    private String companyName;
    private int employeeCount;
    private double averageSalary;
    private double highestSalary;
    private String topEarnerName;

    public CompanyStatisticsDTO() {}

    public CompanyStatisticsDTO(String companyName, int employeeCount, double averageSalary,
                                double highestSalary, String topEarnerName) {
        this.companyName = companyName;
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.highestSalary = highestSalary;
        this.topEarnerName = topEarnerName;
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public int getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(int employeeCount) { this.employeeCount = employeeCount; }

    public double getAverageSalary() { return averageSalary; }
    public void setAverageSalary(double averageSalary) { this.averageSalary = averageSalary; }

    public double getHighestSalary() { return highestSalary; }
    public void setHighestSalary(double highestSalary) { this.highestSalary = highestSalary; }

    public String getTopEarnerName() { return topEarnerName; }
    public void setTopEarnerName(String topEarnerName) { this.topEarnerName = topEarnerName; }

    @Override
    public String toString() {
        return "CompanyStatisticsDTO{" +
                "companyName='" + companyName + '\'' +
                ", employeeCount=" + employeeCount +
                ", averageSalary=" + averageSalary +
                ", highestSalary=" + highestSalary +
                ", topEarnerName='" + topEarnerName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompanyStatisticsDTO that = (CompanyStatisticsDTO) o;
        return employeeCount == that.employeeCount &&
                Double.compare(that.averageSalary, averageSalary) == 0 &&
                Double.compare(that.highestSalary, highestSalary) == 0 &&
                Objects.equals(companyName, that.companyName) &&
                Objects.equals(topEarnerName, that.topEarnerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName, employeeCount, averageSalary, highestSalary, topEarnerName);
    }
}