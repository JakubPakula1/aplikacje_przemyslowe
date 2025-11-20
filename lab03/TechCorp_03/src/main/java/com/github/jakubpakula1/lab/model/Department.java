package com.github.jakubpakula1.lab.model;

public class Department {

    private Long id;
    private  String name;
    private String location;
    private double budget;
    private String managerEmail;

    public Department() {
    }

    public Department(Long id, String name, String location, double budget, String managerEmail) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.budget = budget;
        this.managerEmail = managerEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }
}
