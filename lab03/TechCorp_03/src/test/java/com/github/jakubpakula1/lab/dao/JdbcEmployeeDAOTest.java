package com.github.jakubpakula1.lab.dao;

import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@Import(JdbcEmployeeDAO.class)
public class JdbcEmployeeDAOTest {

    @Autowired
    private JdbcEmployeeDAO employeeDAO;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee("John", "Doe", "TechCorp", "john@example.com", Position.PROGRAMISTA, 5000);
        testEmployee.setStatus(EmploymentStatus.ACTIVE);
    }

    @Test
    void testSaveAndFindByEmail() {
        employeeDAO.save(testEmployee);
        Optional<Employee> found = employeeDAO.findByEmail("john@example.com");

        assertThat(found).isPresent();
    }

    @Test
    void testEnumMapping() {
        employeeDAO.save(testEmployee);
        Optional<Employee> found = employeeDAO.findByEmail("john@example.com");

        assertThat(found)
            .isPresent()
            .hasValueSatisfying(emp -> {
                assertThat(emp.getPosition()).isEqualTo(Position.PROGRAMISTA);
                assertThat(emp.getStatus()).isEqualTo(EmploymentStatus.ACTIVE);
            });
    }

    @Test
    void testFindByEmailNotFound() {
        Optional<Employee> found = employeeDAO.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void testFindAll() {
        Employee emp1 = new Employee("Alice", "Smith", "CompanyA", "alice@example.com", Position.MANAGER, 6000);
        emp1.setStatus(EmploymentStatus.ACTIVE);
        Employee emp2 = new Employee("Bob", "Johnson", "CompanyB", "bob@example.com", Position.PROGRAMISTA, 5500);
        emp2.setStatus(EmploymentStatus.ACTIVE);

        employeeDAO.save(emp1);
        employeeDAO.save(emp2);

        List<Employee> employees = employeeDAO.findAll();

        assertThat(employees)
            .hasSize(2)
            .extracting(Employee::getEmail)
            .containsExactlyInAnyOrder("alice@example.com", "bob@example.com");
    }

    @Test
    void testDeleteByEmail() {
        employeeDAO.save(testEmployee);
        employeeDAO.delete("john@example.com");

        assertThat(employeeDAO.findByEmail("john@example.com")).isEmpty();
    }

    @Test
    void testDeleteAll() {
        Employee emp1 = new Employee("Alice", "Smith", "CompanyA", "alice@example.com", Position.MANAGER, 6000);
        emp1.setStatus(EmploymentStatus.ACTIVE);
        Employee emp2 = new Employee("Bob", "Johnson", "CompanyB", "bob@example.com", Position.PROGRAMISTA, 5500);
        emp2.setStatus(EmploymentStatus.ACTIVE);

        employeeDAO.save(emp1);
        employeeDAO.save(emp2);
        employeeDAO.deleteAll();

        assertThat(employeeDAO.findAll()).isEmpty();
    }

    @Test
    void testGetCompanyStatistics() {
        Employee emp1 = new Employee("Alice", "Smith", "TechCorp", "alice@example.com", Position.MANAGER, 8000);
        emp1.setStatus(EmploymentStatus.ACTIVE);
        Employee emp2 = new Employee("Bob", "Johnson", "TechCorp", "bob@example.com", Position.PROGRAMISTA, 5000);
        emp2.setStatus(EmploymentStatus.ACTIVE);
        Employee emp3 = new Employee("Charlie", "Brown", "OtherCo", "charlie@example.com", Position.PROGRAMISTA, 4500);
        emp3.setStatus(EmploymentStatus.ACTIVE);

        employeeDAO.save(emp1);
        employeeDAO.save(emp2);
        employeeDAO.save(emp3);

        var stats = employeeDAO.getCompanyStatistics();

        assertThat(stats)
            .hasSize(2)
            .anySatisfy(s -> assertThat(s)
                .extracting("company", "numberOfEmployees", "averageSalary", "bestEarningName")
                .containsExactly("TechCorp", 2, 6500.0, "Alice Smith"));
    }
}