package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
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
    // getEmployeeByEmail tests
    @Test
    void getEmployeeByEmail_nullEmail_returnsNull() {
        assertThat(service.getEmployeeByEmail(null)).isNull();
    }

    @Test
    void getEmployeeByEmail_blankEmail_returnsNull() {
        assertThat(service.getEmployeeByEmail("   ")).isNull();
    }

    @Test
    void getEmployeeByEmail_notFound_returnsNull() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        assertThat(service.getEmployeeByEmail("notfound@x.com")).isNull();
    }

    @Test
    void getEmployeeByEmail_found_returnsEmployee() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        Employee result = service.getEmployeeByEmail("a@x.com");
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("a@x.com");
    }

    @Test
    void getEmployeeByEmail_caseInsensitive_returnsEmployee() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        assertThat(service.getEmployeeByEmail("A@X.COM")).isNotNull();
    }

    // updateEmployee tests
    @Test
    void updateEmployee_nullEmail_returnsNull() {
        Employee updated = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        assertThat(service.updateEmployee(null, updated)).isNull();
    }

    @Test
    void updateEmployee_blankEmail_returnsNull() {
        Employee updated = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        assertThat(service.updateEmployee("   ", updated)).isNull();
    }

    @Test
    void updateEmployee_nullUpdated_returnsNull() {
        assertThat(service.updateEmployee("a@x.com", null)).isNull();
    }

    @Test
    void updateEmployee_notFound_returnsNull() {
        Employee updated = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        assertThat(service.updateEmployee("notfound@x.com", updated)).isNull();
    }

    @Test
    void updateEmployee_validUpdate_updatesEmployee() {
        Employee e1 = new Employee("A","B","X","old@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        Employee updated = new Employee("NewName","NewSurname","Y","old@x.com", Position.MANAGER, 12000);
        Employee result = service.updateEmployee("old@x.com", updated);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getCompany()).isEqualTo("Y");
    }

    @Test
    void updateEmployee_duplicateEmail_throws() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("C","D","X","c@x.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        Employee updated = new Employee("A","B","X","c@x.com", Position.PROGRAMISTA, 8000);
        assertThatThrownBy(() -> service.updateEmployee("a@x.com", updated))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateEmployee_sameEmail_succeeds() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        Employee updated = new Employee("NewName","B","X","a@x.com", Position.PROGRAMISTA, 9000);
        Employee result = service.updateEmployee("a@x.com", updated);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("NewName");
    }

    // deleteEmployee tests
    @Test
    void deleteEmployee_nullEmail_returnsFalse() {
        assertThat(service.deleteEmployee(null)).isFalse();
    }

    @Test
    void deleteEmployee_blankEmail_returnsFalse() {
        assertThat(service.deleteEmployee("   ")).isFalse();
    }

    @Test
    void deleteEmployee_notFound_returnsFalse() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        assertThat(service.deleteEmployee("notfound@x.com")).isFalse();
    }

    @Test
    void deleteEmployee_found_deletesAndReturnsTrue() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        assertThat(service.deleteEmployee("a@x.com")).isTrue();
        assertThat(service.getEmployees()).isEmpty();
    }

    @Test
    void deleteEmployee_caseInsensitive_deletesAndReturnsTrue() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        assertThat(service.deleteEmployee("A@X.COM")).isTrue();
    }

    // updateEmployeeStatus tests
    @Test
    void updateEmployeeStatus_nullEmail_throws() {
        assertThatThrownBy(() -> service.updateEmployeeStatus(null, EmploymentStatus.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateEmployeeStatus_blankEmail_throws() {
        assertThatThrownBy(() -> service.updateEmployeeStatus("   ", EmploymentStatus.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateEmployeeStatus_nullStatus_throws() {
        assertThatThrownBy(() -> service.updateEmployeeStatus("a@x.com", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateEmployeeStatus_notFound_returnsNull() {
        assertThat(service.updateEmployeeStatus("notfound@x.com", EmploymentStatus.ACTIVE)).isNull();
    }

    @Test
    void updateEmployeeStatus_found_updatesStatus() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        Employee result = service.updateEmployeeStatus("a@x.com", EmploymentStatus.ON_LEAVE);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(EmploymentStatus.ON_LEAVE);
    }

    // getStatusDistribution tests
    @Test
    void getStatusDistribution_emptyDatabase_returnsEmpty() {
        assertThat(service.getStatusDistribution()).isEmpty();
    }

    @Test
    void getStatusDistribution_singleStatus_countsCorrectly() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","X","b@x.com", Position.MANAGER, 12000);
        e1.setStatus(EmploymentStatus.ACTIVE);
        e2.setStatus(EmploymentStatus.ACTIVE);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        Map<String, Integer> result = service.getStatusDistribution();
        assertThat(result).containsEntry("ACTIVE", 2);
    }

    @Test
    void getStatusDistribution_multipleStatuses_countsCorrectly() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","X","b@x.com", Position.MANAGER, 12000);
        e1.setStatus(EmploymentStatus.ACTIVE);
        e2.setStatus(EmploymentStatus.ACTIVE);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        service.updateEmployeeStatus("a@x.com", EmploymentStatus.ON_LEAVE);
        Map<String, Integer> result = service.getStatusDistribution();
        assertThat(result)
                .containsEntry("ACTIVE", 1)
                .containsEntry("ON_LEAVE", 1);
    }

    // getEmployeesByStatus tests
    @Test
    void getEmployeesByStatus_nullStatus_returnsEmpty() {
        assertThat(service.getEmployeesByStatus(null)).isEmpty();
    }

    @Test
    void getEmployeesByStatus_noMatches_returnsEmpty() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        assertThat(service.getEmployeesByStatus(EmploymentStatus.ON_LEAVE)).isEmpty();
    }

    @Test
    void getEmployeesByStatus_withMatches_returnsFiltered() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","X","b@x.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        service.updateEmployeeStatus("a@x.com", EmploymentStatus.ON_LEAVE);
        List<Employee> result = service.getEmployeesByStatus(EmploymentStatus.ON_LEAVE);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("a@x.com");
    }

    // getEmployeesManagerAndAbove tests
    @Test
    void getEmployeesManagerAndAbove_emptyDatabase_returnsEmpty() {
        assertThat(service.getEmployeesManagerAndAbove()).isEmpty();
    }

    @Test
    void getEmployeesManagerAndAbove_onlyLowerLevel_returnsEmpty() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        assertThat(service.getEmployeesManagerAndAbove()).isEmpty();
    }

    @Test
    void getEmployeesManagerAndAbove_withManagers_returnsManagers() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","X","b@x.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        List<Employee> result = service.getEmployeesManagerAndAbove();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPosition()).isEqualTo(Position.MANAGER);
    }

    // getEmployeesByDepartment tests
    @Test
    void getEmployeesByDepartment_emptyDatabase_returnsEmpty() {
        assertThat(service.getEmployeesByDepartment(1L)).isEmpty();
    }

    @Test
    void getEmployeesByDepartment_noMatches_returnsEmpty() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        e1.setDepartmentId(1L);
        service.AddEmployee(e1);
        assertThat(service.getEmployeesByDepartment(2L)).isEmpty();
    }

    @Test
    void getEmployeesByDepartment_withMatches_returnsFiltered() {
        Employee e1 = new Employee("A","A","X","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","X","b@x.com", Position.MANAGER, 12000);
        e1.setDepartmentId(1L);
        e2.setDepartmentId(1L);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        assertThat(service.getEmployeesByDepartment(1L)).hasSize(2);
    }

    // getCompanyStatisticsDTO tests
    @Test
    void getCompanyStatisticsDTO_nullCompany_returnsEmpty() {
        assertThat(service.getCompanyStatisticsDTO(null)).isEmpty();
    }

    @Test
    void getCompanyStatisticsDTO_blankCompany_returnsEmpty() {
        assertThat(service.getCompanyStatisticsDTO("   ")).isEmpty();
    }

    @Test
    void getCompanyStatisticsDTO_notFound_returnsEmpty() {
        Employee e1 = new Employee("A","B","X","a@x.com", Position.PROGRAMISTA, 8000);
        service.AddEmployee(e1);
        assertThat(service.getCompanyStatisticsDTO("NotExisting")).isEmpty();
    }

    @Test
    void getCompanyStatisticsDTO_found_returnsDTO() {
        Employee e1 = new Employee("A","A","Acme","a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B","B","Acme","b@x.com", Position.MANAGER, 12000);
        service.AddEmployee(e1);
        service.AddEmployee(e2);
        var result = service.getCompanyStatisticsDTO("Acme");
        assertThat(result).isPresent();
        assertThat(result.get().getCompanyName()).isEqualTo("Acme");
    }
}