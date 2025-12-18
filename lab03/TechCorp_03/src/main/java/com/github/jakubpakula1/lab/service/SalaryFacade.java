package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.exception.InvalidSalaryException;
import com.github.jakubpakula1.lab.model.Employee;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class SalaryFacade {

    private final SalaryService salaryService;
    private final EmployeeService employeeService;

    public SalaryFacade(SalaryService salaryService, EmployeeService employeeService) {
        this.salaryService = salaryService;
        this.employeeService = employeeService;
    }

    public void updateSalariesBatch(List<Long> employeeIds, BigDecimal newSalary) throws InvalidSalaryException {
        for (Long employeeId : employeeIds) {
            salaryService.updateSalary(employeeId, newSalary);
        }
    }

    public void updateSalariesByCompany(String company, BigDecimal salaryIncrease) throws InvalidSalaryException {
        List<Employee> employees = employeeService.getCompanyEmployees(company);
        for (Employee employee : employees) {
            if (employee.getSalary() != null) {
                BigDecimal newSalary = employee.getSalary().add(salaryIncrease);
                salaryService.updateSalary(employee.getId(), newSalary);
            }
        }
    }
}