package com.github.jakubpakula1.lab.specification;

import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.Position;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {

    public static Specification<Employee> hasFirstName(String name) {
        return (root, query, builder) -> {
            if (name == null || name.isBlank()) return null;
            return builder.like(
                    builder.lower(root.get("firstName")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasLastName(String surname) {
        return (root, query, builder) -> {
            if (surname == null || surname.isBlank()) return null;
            return builder.like(
                    builder.lower(root.get("lastName")),
                    "%" + surname.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasCompany(String company) {
        return (root, query, builder) -> {
            if (company == null || company.isBlank()) return null;
            return builder.equal(
                    builder.lower(root.get("company")),
                    company.toLowerCase()
            );
        };
    }

    public static Specification<Employee> hasPosition(Position position) {
        return (root, query, builder) -> {
            if (position == null) return null;
            return builder.equal(root.get("position"), position);
        };
    }

    public static Specification<Employee> salaryGreaterThanOrEqual(Integer minSalary) {
        return (root, query, builder) -> {
            if (minSalary == null) return null;
            return builder.greaterThanOrEqualTo(root.get("salary"), minSalary);
        };
    }

    public static Specification<Employee> salaryLessThanOrEqual(Integer maxSalary) {
        return (root, query, builder) -> {
            if (maxSalary == null) return null;
            return builder.lessThanOrEqualTo(root.get("salary"), maxSalary);
        };
    }

    public static Specification<Employee> hasDepartment(Long departmentId) {
        return (root, query, builder) -> {
            if (departmentId == null) return null;
            return builder.equal(root.get("department").get("id"), departmentId);
        };
    }
}