package com.github.jakubpakula1.lab.dao;

import com.github.jakubpakula1.lab.model.CompanyStatistics;
import com.github.jakubpakula1.lab.model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeDAO {
    List<Employee> findAll();
    Optional<Employee> findByEmail(String email);
    void save(Employee employee);
    void delete(String email);
    void deleteAll();
    List<CompanyStatistics> getCompanyStatistics();
}
