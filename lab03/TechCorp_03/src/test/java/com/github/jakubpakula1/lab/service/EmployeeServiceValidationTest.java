package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import com.github.jakubpakula1.lab.repository.EmployeeRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test walidacji w warstwie serwisu
 */
@SpringBootTest
class EmployeeServiceValidationTest {

    @Autowired
    private EmployeeService employeeService;

    @MockBean
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("addEmployee - walidacja: pusty email zwraca ConstraintViolationException")
    void testAddEmployeeEmptyEmail() {
        Employee employee = new Employee("John", "Doe", "Acme", "", Position.PROGRAMISTA, 5000);
        employee.setStatus(EmploymentStatus.ACTIVE);

        when(employeeRepository.existsByEmail("")).thenReturn(false);
        when(employeeRepository.save(any())).thenReturn(employee);

        assertThrows(ConstraintViolationException.class, () -> {
            employeeService.addEmployee(employee);
        });
    }

    @Test
    @DisplayName("addEmployee - walidacja: ujemna pensja zwraca ConstraintViolationException")
    void testAddEmployeeNegativeSalary() {
        Employee employee = new Employee("John", "Doe", "Acme", "john@techcorp.com", Position.PROGRAMISTA, -5000);
        employee.setStatus(EmploymentStatus.ACTIVE);

        when(employeeRepository.existsByEmail("john@techcorp.com")).thenReturn(false);
        when(employeeRepository.save(any())).thenReturn(employee);

        assertThrows(ConstraintViolationException.class, () -> {
            employeeService.addEmployee(employee);
        });
    }

    @Test
    @DisplayName("addEmployee - walidacja: email bez domeny @techcorp.com zwraca ConstraintViolationException")
    void testAddEmployeeWrongDomain() {
        Employee employee = new Employee("John", "Doe", "Acme", "john@example.com", Position.PROGRAMISTA, 5000);
        employee.setStatus(EmploymentStatus.ACTIVE);

        when(employeeRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(employeeRepository.save(any())).thenReturn(employee);

        assertThrows(ConstraintViolationException.class, () -> {
            employeeService.addEmployee(employee);
        });
    }

    @Test
    @DisplayName("addEmployee - walidacja: imię poniżej 2 znaków zwraca ConstraintViolationException")
    void testAddEmployeeShortFirstName() {
        Employee employee = new Employee("J", "Doe", "Acme", "john@techcorp.com", Position.PROGRAMISTA, 5000);
        employee.setStatus(EmploymentStatus.ACTIVE);

        when(employeeRepository.existsByEmail("john@techcorp.com")).thenReturn(false);
        when(employeeRepository.save(any())).thenReturn(employee);

        assertThrows(ConstraintViolationException.class, () -> {
            employeeService.addEmployee(employee);
        });
    }

    @Test
    @DisplayName("updateEmployee - walidacja: ujemna pensja zwraca ConstraintViolationException")
    void testUpdateEmployeeNegativeSalary() {
        Employee employee = new Employee("John", "Doe", "Acme", "john@techcorp.com", Position.PROGRAMISTA, -5000);
        employee.setStatus(EmploymentStatus.ACTIVE);

        when(employeeRepository.findByEmail("john@techcorp.com")).thenReturn(java.util.Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        Employee updated = new Employee("John", "Doe", "Acme", "john@techcorp.com", Position.PROGRAMISTA, -5000);
        updated.setStatus(EmploymentStatus.ACTIVE);

        assertThrows(ConstraintViolationException.class, () -> {
            employeeService.updateEmployee("john@techcorp.com", updated);
        });
    }
}

