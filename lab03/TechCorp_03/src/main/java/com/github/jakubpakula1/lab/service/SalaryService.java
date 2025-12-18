package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.exception.InvalidSalaryException;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SalaryService {
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    public SalaryService(EmployeeRepository employeeRepository, AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.auditService = auditService;
    }
    @Transactional(rollbackFor = InvalidSalaryException.class)
    public void updateSalary(Long id, BigDecimal newSalary) throws InvalidSalaryException {
        String message = String.format("Updating salary for employee %d to %s", id, newSalary != null ? newSalary.toPlainString() : "null");

        auditService.logEvent(message);

        if (newSalary == null || newSalary.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSalaryException("Pensja musi być wartością dodatnią");
        }

        Employee employee = employeeRepository.findByIdWithLock(id)
                .orElseThrow(() -> new InvalidSalaryException("Pracownik nie znaleziony"));

        employee.setSalary(newSalary);

        employeeRepository.save(employee);

    }
}
