package com.github.jakubpakula1.lab.dto;

import com.github.jakubpakula1.lab.model.Department;
import com.github.jakubpakula1.lab.model.Position;

public class EmployeeListProjection {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private Position position;
    private Department department;

    public EmployeeListProjection(Long id, String email, String name, String surname, Position position, Department department) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.position = position;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getFullName() {
        return name + " " + surname;
    }

    public Position getPosition() {
        return position;
    }

    public Department getDepartment() {
        return department;
    }
}