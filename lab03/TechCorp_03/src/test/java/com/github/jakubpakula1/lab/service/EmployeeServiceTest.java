package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class EmployeeServiceTest {

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService();
    }

    // AddEmployee tests
    @Test
    void addEmployee_null_throws() {
        assertThatThrownBy(() -> service.AddEmployee(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addEmployee_duplicateEmail_throws() {
        Employee e1 = new Employee("Jan","Kowalski","Acme","jan@acme.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("Janusz","Nowak","Acme","jan@acme.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        assertThatThrownBy(() -> service.AddEmployee(e2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addEmployee_valid_succeeds() {
        Employee e1 = new Employee("Jan","Kowalski","Acme","jan@acme.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        List<Employee> employees = service.getCompanyEmployees("Acme");
        assertThat(employees)
                .hasSize(1)
                .extracting(Employee::getEmail)
                .containsExactly("jan@acme.com");
    }

    @Test
    void addEmployee_multipleValid_succeeds() {
        Employee e1 = new Employee("Jan","Kowalski","Acme","jan@acme.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("Anna","Nowak","Acme","anna@acme.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        List<Employee> employees = service.getCompanyEmployees("Acme");
        assertThat(employees).hasSize(2);
    }

    // DisplayWorkers tests
    @Test
    void displayWorkers_empty_doesNotThrow() {
        assertThatCode(() -> service.DisplayWorkers())
                .doesNotThrowAnyException();
    }

    @Test
    void displayWorkers_withEmployees_doesNotThrow() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        assertThatCode(() -> service.DisplayWorkers())
                .doesNotThrowAnyException();
    }

    @Test
    void displayWorkers_withMultipleEmployees_doesNotThrow() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("C","D","Y","c@y.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        assertThatCode(() -> service.DisplayWorkers())
                .doesNotThrowAnyException();
    }

    // getCompanyEmployees tests
    @Test
    void getCompanyEmployees_emptyDatabase_returnsEmpty() {
        List<Employee> result = service.getCompanyEmployees("Acme");
        assertThat(result).isEmpty();
    }

    @Test
    void getCompanyEmployees_nonExistingCompany_returnsEmpty() {
        Employee e1 = new Employee("A","B","CompanyA","a@a.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        List<Employee> result = service.getCompanyEmployees("NoSuchCompany");
        assertThat(result).isEmpty();
    }

    @Test
    void getCompanyEmployees_nullCompanyName_returnsEmpty() {
        Employee e1 = new Employee("A","B","CompanyA","a@a.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        List<Employee> result = service.getCompanyEmployees(null);
        assertThat(result).isEmpty();
    }

    @Test
    void getCompanyEmployees_blankCompanyName_returnsEmpty() {
        Employee e1 = new Employee("A","B","CompanyA","a@a.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        List<Employee> result = service.getCompanyEmployees("   ");
        assertThat(result).isEmpty();
    }

    @Test
    void getCompanyEmployees_caseInsensitive_returnsEmployees() {
        Employee e1 = new Employee("A","B","Acme","a@a.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        List<Employee> result = service.getCompanyEmployees("ACME");
        assertThat(result).hasSize(1);
    }

    @Test
    void getCompanyEmployees_multipleFromCompany_returnsAll() {
        Employee e1 = new Employee("A","B","Acme","a@a.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("C","D","Acme","c@d.com", Position.MANAGER, 12000);
        Employee e3 = new Employee("E","F","Other","e@f.com", Position.PROGRAMISTA, 9000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        service.AddEmployee(e3);
        List<Employee> result = service.getCompanyEmployees("Acme");
        assertThat(result)
                .hasSize(2)
                .extracting(Employee::getEmail)
                .containsExactlyInAnyOrder("a@a.com", "c@d.com");
    }

    // getEmployeesSortedByLastName tests
    @Test
    void getEmployeesSortedByLastName_emptyDatabase_returnsEmpty() {
        List<Employee> result = service.getEmployeesSortedByLastName();
        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeesSortedByLastName_singleEmployee_returnsSame() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        List<Employee> result = service.getEmployeesSortedByLastName();
        assertThat(result)
                .hasSize(1)
                .extracting(Employee::getSurname)
                .containsExactly("B");
    }

    @Test
    void getEmployeesSortedByLastName_sortsByLastNameIgnoreCase() {
        Employee e1 = new Employee("Z","Zebra","X","z@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("A","Apple","X","a@x.com", Position.PROGRAMISTA, 9000);
        Employee e3 = new Employee("M","Mango","X","m@x.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        service.AddEmployee(e3);
        List<Employee> sorted = service.getEmployeesSortedByLastName();
        assertThat(sorted)
                .extracting(Employee::getSurname)
                .containsExactly("Apple", "Mango", "Zebra");
    }

    @Test
    void getEmployeesSortedByLastName_mixedCase_sortsCaseInsensitive() {
        Employee e1 = new Employee("X","xray","X","x@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("Y","Yankee","X","y@x.com", Position.PROGRAMISTA, 9000);
        Employee e3 = new Employee("A","apple","X","a@x.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        service.AddEmployee(e3);
        List<Employee> sorted = service.getEmployeesSortedByLastName();
        assertThat(sorted)
                .extracting(Employee::getSurname)
                .containsExactly("apple", "xray", "Yankee");
    }

    // getEmployeesByPosition tests
    @Test
    void getEmployeesByPosition_emptyDatabase_returnsEmpty() {
        Map<String, ?> result = service.getEmployeesByPosition();
        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeesByPosition_singlePosition_groupsCorrectly() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","X","b@x.com", Position.PROGRAMISTA, 9000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        Map<String, ?> result = service.getEmployeesByPosition();
        assertThat(result)
                .hasSize(1)
                .containsKey("PROGRAMISTA");
    }

    @Test
    void getEmployeesByPosition_multiplePositions_groupsCorrectly() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","X","b@x.com", Position.MANAGER, 12000);
        Employee e3 = new Employee("C","C","Y","c@y.com", Position.PROGRAMISTA, 9000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        service.AddEmployee(e3);
        Map<String, ?> result = service.getEmployeesByPosition();
        assertThat(result)
                .hasSize(2)
                .containsKeys("PROGRAMISTA", "MANAGER");
    }

    // getPositionStatistics tests
    @Test
    void getPositionStatistics_emptyDatabase_returnsEmpty() {
        Map<String, Integer> result = service.getPositionStatistics();
        assertThat(result).isEmpty();
    }

    @Test
    void getPositionStatistics_singlePosition_countsCorrectly() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","Y","b@y.com", Position.PROGRAMISTA, 9000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        Map<String, Integer> stats = service.getPositionStatistics();
        assertThat(stats)
                .hasSize(1)
                .containsEntry("PROGRAMISTA", 2);
    }

    @Test
    void getPositionStatistics_multiplePositions_countsCorrectly() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","Y","b@y.com", Position.PROGRAMISTA, 9000);
        Employee e3 = new Employee("C","C","Z","c@z.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        service.AddEmployee(e3);
        Map<String, Integer> stats = service.getPositionStatistics();
        assertThat(stats)
                .containsEntry("PROGRAMISTA", 2)
                .containsEntry("MANAGER", 1);
    }

    // getAverageSalary tests
    @Test
    void getAverageSalary_emptyEmployees_returnsZero() {
        double avg = service.getAverageSalary();
        assertThat(avg).isEqualTo(0.0);
    }

    @Test
    void getAverageSalary_singleEmployee_returnsBaseSalary() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        double avg = service.getAverageSalary();
        assertThat(avg).isEqualTo(Position.PROGRAMISTA.getBaseSalary());
    }

    @Test
    void getAverageSalary_multipleEmployees_calculatesCorrectly() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","X","b@x.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        double avg = service.getAverageSalary();
        double expected = (Position.PROGRAMISTA.getBaseSalary() + Position.MANAGER.getBaseSalary()) / 2.0;
        assertThat(avg).isEqualTo(expected);
    }

    // getHighestPaidEmployee tests
    @Test
    void getHighestPaidEmployee_empty_returnsEmptyOptional() {
        Optional<Employee> highest = service.getHighestPaidEmployee();
        assertThat(highest).isEmpty();
    }

    @Test
    void getHighestPaidEmployee_singleEmployee_returnsThatEmployee() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        Optional<Employee> highest = service.getHighestPaidEmployee();
        assertThat(highest)
                .isPresent()
                .get()
                .extracting(Employee::getEmail)
                .isEqualTo("a@x.com");
    }

    @Test
    void getHighestPaidEmployee_multipleEmployees_returnsHighest() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","Y","b@y.com", Position.MANAGER, 12000);
        Employee e3 = new Employee("C","C","Z","c@z.com", Position.PROGRAMISTA, 9000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        service.AddEmployee(e3);
        Optional<Employee> highest = service.getHighestPaidEmployee();
        assertThat(highest)
                .isPresent()
                .get()
                .extracting(Employee::getEmail)
                .isEqualTo("b@y.com");
    }

    // validateSalaryConsistency tests
    @Test
    void validateSalaryConsistency_emptyDatabase_returnsEmpty() {
        List<Employee> result = service.validateSalaryConsistency();
        assertThat(result).isEmpty();
    }

    @Test
    void validateSalaryConsistency_allConsistent_returnsEmpty() {
        Employee e1 = new Employee("G","Good","C","good@c.com", Position.MANAGER, Position.MANAGER.getBaseSalary());
        Employee e2 = new Employee("G2","Good2","C","good2@c.com", Position.MANAGER, Position.MANAGER.getBaseSalary() + 1000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        List<Employee> inconsistent = service.validateSalaryConsistency();
        assertThat(inconsistent).isEmpty();
    }

    @Test
    void validateSalaryConsistency_singleInconsistent_detectsIt() {
        Employee good = new Employee("G","Good","C","good@c.com", Position.MANAGER, Position.MANAGER.getBaseSalary());
        Employee bad = new Employee("B","Bad","C","bad@c.com", Position.MANAGER, Position.MANAGER.getBaseSalary() - 1000);
        service.AddEmployee(good);
        service.AddEmployee(bad);
        List<Employee> inconsistent = service.validateSalaryConsistency();
        assertThat(inconsistent)
                .hasSize(1)
                .extracting(Employee::getEmail)
                .containsExactly("bad@c.com");
    }

    @Test
    void validateSalaryConsistency_multipleInconsistent_detectsAll() {
        Employee good = new Employee("G","Good","C","good@c.com", Position.MANAGER, Position.MANAGER.getBaseSalary());
        Employee bad1 = new Employee("B1","Bad1","C","bad1@c.com", Position.MANAGER, Position.MANAGER.getBaseSalary() - 500);
        Employee bad2 = new Employee("B2","Bad2","C","bad2@c.com", Position.PROGRAMISTA, Position.PROGRAMISTA.getBaseSalary() - 1000);
        service.AddEmployee(good);
        service.AddEmployee(bad1);
        service.AddEmployee(bad2);
        List<Employee> inconsistent = service.validateSalaryConsistency();
        assertThat(inconsistent).hasSize(2);
    }

    // getCompanyStatistics tests
    @Test
    void getCompanyStatistics_emptyDatabase_returnsEmpty() {
        Map<String, ?> result = service.getCompanyStatistics();
        assertThat(result).isEmpty();
    }

    @Test
    void getCompanyStatistics_singleCompany_calculatesCorrectly() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","X","b@x.com", Position.PROGRAMISTA, 10000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        Map<String, ?> stats = service.getCompanyStatistics();
        assertThat(stats)
                .hasSize(1)
                .containsKey("X");
    }

    @Test
    void getCompanyStatistics_multipleCompanies_groupsCorrectly() {
        Employee e1 = new Employee("Anna","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("Bob","B","X","b@x.com", Position.PROGRAMISTA, 9000);
        Employee e3 = new Employee("C","C","Y","c@y.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        service.AddEmployee(e3);
        Map<String, ?> stats = service.getCompanyStatistics();
        assertThat(stats)
                .hasSize(2)
                .containsKeys("X", "Y");
    }

    @Test
    void getCompanyStatistics_blankCompanyName_isFiltered() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        Map<String, ?> stats = service.getCompanyStatistics();
        assertThat(stats)
                .doesNotContainKey("")
                .doesNotContainKey("   ");
    }
}