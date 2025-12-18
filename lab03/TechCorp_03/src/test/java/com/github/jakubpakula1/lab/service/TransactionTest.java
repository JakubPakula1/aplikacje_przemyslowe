package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.exception.InvalidSalaryException;
import com.github.jakubpakula1.lab.model.AuditLog;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import com.github.jakubpakula1.lab.repository.AuditLogRepository;
import com.github.jakubpakula1.lab.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionTest {

    @Autowired
    private SalaryService salaryService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        employeeRepository.deleteAll();

        testEmployee = new Employee();
        testEmployee.setName("Test Employee");
        testEmployee.setSurname("Test");
        testEmployee.setEmail("test@techcorp.com");
        testEmployee.setPosition(Position.PROGRAMISTA);
        testEmployee.setSalary(BigDecimal.valueOf(8000));
        testEmployee.setCompany("TestCorp");
        testEmployee.setStatus(EmploymentStatus.ACTIVE);
        testEmployee = employeeRepository.save(testEmployee);
    }

    @Test
    void updateSalary_withInvalidSalary_auditLogIsStillPersisted() {
        BigDecimal invalidSalary = BigDecimal.valueOf(-1000);

        assertThatThrownBy(() -> salaryService.updateSalary(testEmployee.getId(), invalidSalary))
                .isInstanceOf(InvalidSalaryException.class);

        Employee updatedEmployee = employeeRepository.findById(testEmployee.getId()).orElseThrow();
        assertThat(updatedEmployee.getSalary()).isEqualByComparingTo(BigDecimal.valueOf(8000));

        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertThat(auditLogs)
                .isNotEmpty()
                .hasSize(1);

        AuditLog log = auditLogs.get(0);
        assertThat(log.getMessage())
                .contains("Updating salary for employee")
                .isNotBlank();
        assertThat(log.getEventDate()).isNotNull();
    }

    @Test
    void updateSalary_success_auditLogAndSalaryBothPersisted() throws InvalidSalaryException {
        BigDecimal newSalary = BigDecimal.valueOf(10000);

        salaryService.updateSalary(testEmployee.getId(), newSalary);

        Employee updatedEmployee = employeeRepository.findById(testEmployee.getId()).orElseThrow();
        assertThat(updatedEmployee.getSalary()).isEqualByComparingTo(newSalary);

        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertThat(auditLogs)
                .isNotEmpty()
                .extracting(AuditLog::getMessage)
                .anyMatch(msg -> msg.contains("Updating salary for employee"));
    }
}