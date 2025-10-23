package com.github.jakubpakula1.lab.model;

import java.util.Objects;

public class Employee {
    private String name;
    private String surname;
    private String email;
    private String company;
    private Position position;
    private int salary;

    public Employee(String name, String surname, String company, String email, Position position, int salary) {
        this.name = name;
        this.surname = surname;
        this.company = company;
        this.email = email;
        this.position = position;
        this.salary = salary;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public String getFullName(){
        return this.name + " " + this.getSurname();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        if(email == null || employee.email == null) return false;
        return email.equalsIgnoreCase(employee.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email == null ? null : email.toLowerCase());
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", company='" + company + '\'' +
                ", position=" + position + '\'' +
                ", salary = " + salary +
                '}';
    }
}
