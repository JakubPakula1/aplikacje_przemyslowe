package com.github.jakubpakula1.lab.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.github.jakubpakula1.lab.validation.TechCorpEmail;

import java.math.BigDecimal;
import java.util.Objects;
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FIRST_NAME", nullable = false)
    @NotBlank(message = "Imię nie może być puste")
    @Size(min = 2, message = "Imię musi mieć co najmniej 2 znaki")
    private String name;

    @Column(name = "LAST_NAME", nullable = false)
    @NotBlank(message = "Nazwisko nie może być puste")
    @Size(min = 2, message = "Nazwisko musi mieć co najmniej 2 znaki")
    private String surname;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email nie może być pusty")
    @Email(message = "Email musi mieć poprawny format")
    @TechCorpEmail(message = "Email musi posiadać domenę @techcorp.com")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Firma nie może być pusta")
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Stanowisko nie może być puste")
    private Position position;

    @Column(nullable = false,precision = 19, scale = 2)
    @Positive(message = "Pensja musi być większa od 0")
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status nie może być pusty")
    private EmploymentStatus status;

    @Column
    private String photoFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departament_id")
    private Department department;

    public Employee() {

    }

    public Employee(String name, String surname, String company, String email, Position position, BigDecimal salary) {
        this.name = name;
        this.surname = surname;
        this.company = company;
        this.email = email;
        this.position = position;
        this.salary = salary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public EmploymentStatus getStatus() {
        return this.status;
    }

    public void setStatus(EmploymentStatus status) {
        this.status = status;
    }

    public String getPhotoFileName() {
        return photoFileName;
    }

    public void setPhotoFileName(String photoFileName) {
        this.photoFileName = photoFileName;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getFullName(){
        return this.name + " " + this.getName();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        if(email == null || employee.email == null) return false;
        return email.equalsIgnoreCase(employee.email);
    }
    public Long getDepartmentId() {
        return department != null ? department.getId() : null;
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
