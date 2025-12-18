package com.github.jakubpakula1.lab.dto;

import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import com.github.jakubpakula1.lab.validation.TechCorpEmail;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Objects;

public class EmployeeDTO {
    @NotBlank(message = "Imię nie może być puste")
    @Size(min = 2, message = "Imię musi mieć co najmniej 2 znaki")
    private String firstName;
    
    @NotBlank(message = "Nazwisko nie może być puste")
    @Size(min = 2, message = "Nazwisko musi mieć co najmniej 2 znaki")
    private String lastName;
    
    @NotBlank(message = "Email nie może być pusty")
    @Email(message = "Email musi mieć poprawny format")
    @TechCorpEmail(message = "Email musi posiadać domenę @techcorp.com")
    private String email;
    
    @NotBlank(message = "Firma nie może być pusta")
    private String company;
    
    @NotNull(message = "Stanowisko nie może być puste")
    private Position position;
    
    @Positive(message = "Pensja musi być większa od 0")
    private BigDecimal salary;
    
    @NotNull(message = "Status nie może być pusty")
    private EmploymentStatus status;
    
    private Long departmentId;

    public EmployeeDTO() {}

    public EmployeeDTO(String firstName, String lastName, String email, String company,
                       Position position, BigDecimal salary, EmploymentStatus status, Long departmentId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.company = company;
        this.position = position;
        this.salary = salary;
        this.status = status;
        this.departmentId = departmentId;
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

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public EmploymentStatus getStatus() { return status; }
    public void setStatus(EmploymentStatus status) { this.status = status; }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

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
        return Objects.equals(salary, that.salary) &&
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