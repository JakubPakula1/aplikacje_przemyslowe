package com.github.jakubpakula1.lab.dto;

import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import java.util.Objects;

public class EmployeeDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String company;
    private Position position;
    private int salary;
    private EmploymentStatus status;

    public EmployeeDTO() {}

    public EmployeeDTO(String firstName, String lastName, String email, String company,
                       Position position, int salary, EmploymentStatus status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.company = company;
        this.position = position;
        this.salary = salary;
        this.status = status;
    }

    public String getName() { return firstName; }
    public void setName(String firstName) { this.firstName = firstName; }

    public String getSurname() { return lastName; }
    public void setSurname(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public int getSalary() { return salary; }
    public void setSalary(int salary) { this.salary = salary; }

    public EmploymentStatus getStatus() { return status; }
    public void setStatus(EmploymentStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "EmployeeDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", company='" + company + '\'' +
                ", position=" + position +
                ", salary=" + salary +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmployeeDTO that = (EmployeeDTO) o;
        return salary == that.salary &&
                status == that.status &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(email, that.email) &&
                Objects.equals(company, that.company) &&
                position == that.position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, company, position, salary, status);
    }
}