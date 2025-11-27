package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.dao.EmployeeDAO;
import com.github.jakubpakula1.lab.model.CompanyStatistics;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @Mock
    private EmployeeDAO employeeDAO;

    @InjectMocks
    private EmployeeService service;


    @Test
    void addEmployee_null_throws() {
        assertThatThrownBy(() -> service.AddEmployee(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Employee can't be null");
    }

    @Test
    void addEmployee_valid_succeeds() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findByEmail("jan@x.com")).thenReturn(Optional.empty());

        service.AddEmployee(emp);

        verify(employeeDAO).save(emp);
    }

    @Test
    void addEmployee_duplicateEmail_throws() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findByEmail("jan@x.com")).thenReturn(Optional.of(emp));

        assertThatThrownBy(() -> service.AddEmployee(emp))
                .isInstanceOf(IllegalArgumentException.class);
        verify(employeeDAO, never()).save(emp);
    }

    @Test
    void addEmployee_multipleValid_succeeds() {
        Employee emp1 = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Anna", "Nowak", "Y", "anna@y.com", Position.MANAGER, 12000);
        when(employeeDAO.findByEmail("jan@x.com")).thenReturn(Optional.empty());
        when(employeeDAO.findByEmail("anna@y.com")).thenReturn(Optional.empty());

        service.AddEmployee(emp1);
        service.AddEmployee(emp2);

        verify(employeeDAO, times(2)).save(any());
    }

    @Test
    void displayWorkers_empty_doesNotThrow() {
        when(employeeDAO.findAll()).thenReturn(Collections.emptyList());

        assertThatCode(() -> service.DisplayWorkers()).doesNotThrowAnyException();
    }

    @Test
    void displayWorkers_withEmployees_doesNotThrow() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp));

        assertThatCode(() -> service.DisplayWorkers()).doesNotThrowAnyException();
    }

    @Test
    void displayWorkers_withMultipleEmployees_doesNotThrow() {
        Employee emp1 = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Anna", "Nowak", "Y", "anna@y.com", Position.MANAGER, 12000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2));

        assertThatCode(() -> service.DisplayWorkers()).doesNotThrowAnyException();
    }

    @Test
    void getCompanyEmployees_emptyDatabase_returnsEmpty() {
        when(employeeDAO.findAll()).thenReturn(Collections.emptyList());

        List<Employee> result = service.getCompanyEmployees("X");

        assertThat(result).isEmpty();
    }

    @Test
    void getCompanyEmployees_nonExistingCompany_returnsEmpty() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp));

        List<Employee> result = service.getCompanyEmployees("NonExisting");

        assertThat(result).isEmpty();
    }

    @Test
    void getCompanyEmployees_nullCompanyName_returnsEmpty() {
        assertThat(service.getCompanyEmployees(null)).isEmpty();
    }

    @Test
    void getCompanyEmployees_blankCompanyName_returnsEmpty() {
        assertThat(service.getCompanyEmployees("   ")).isEmpty();
    }

    @Test
    void getCompanyEmployees_caseInsensitive_returnsEmployees() {
        Employee emp = new Employee("Jan", "Kowalski", "Acme", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp));

        List<Employee> result = service.getCompanyEmployees("acme");

        assertThat(result).hasSize(1).extracting(Employee::getEmail).contains("jan@x.com");
    }

    @Test
    void getCompanyEmployees_multipleFromCompany_returnsAll() {
        Employee emp1 = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Anna", "Nowak", "X", "anna@x.com", Position.MANAGER, 12000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2));

        List<Employee> result = service.getCompanyEmployees("X");

        assertThat(result).hasSize(2);
    }

    @Test
    void getEmployeesSortedByLastName_emptyDatabase_returnsEmpty() {
        when(employeeDAO.findAll()).thenReturn(Collections.emptyList());

        List<Employee> result = service.getEmployeesSortedByLastName();

        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeesSortedByLastName_singleEmployee_returnsSame() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp));

        List<Employee> result = service.getEmployeesSortedByLastName();

        assertThat(result).hasSize(1).extracting(Employee::getSurname).contains("Kowalski");
    }

    @Test
    void getEmployeesSortedByLastName_sortsByLastNameIgnoreCase() {
        Employee emp1 = new Employee("Jan", "Zebra", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Anna", "Apple", "X", "anna@x.com", Position.MANAGER, 12000);
        Employee emp3 = new Employee("Bob", "Monkey", "X", "bob@x.com", Position.WICEPREZES, 9500);
        when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2, emp3));

        List<Employee> result = service.getEmployeesSortedByLastName();

        assertThat(result).extracting(Employee::getSurname).containsExactly("Apple", "Monkey", "Zebra");
    }

    @Test
    void getEmployeesSortedByLastName_mixedCase_sortsCaseInsensitive() {
        Employee emp1 = new Employee("Jan", "zebra", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Anna", "APPLE", "X", "anna@x.com", Position.MANAGER, 12000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2));

        List<Employee> result = service.getEmployeesSortedByLastName();

        assertThat(result).extracting(Employee::getSurname).containsExactly("APPLE", "zebra");
    }

    @Test
    void getEmployeesByPosition_emptyDatabase_returnsEmpty() {
        when(employeeDAO.findAll()).thenReturn(Collections.emptyList());

        Map<String, List<Employee>> result = service.getEmployeesByPosition();

        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeesByPosition_singlePosition_groupsCorrectly() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp));

        Map<String, List<Employee>> result = service.getEmployeesByPosition();

        assertThat(result).containsKey("PROGRAMISTA").extractingByKey("PROGRAMISTA").asList().hasSize(1);
    }

    @Test
    void getEmployeesByPosition_multiplePositions_groupsCorrectly() {
        Employee emp1 = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Anna", "Nowak", "Y", "anna@y.com", Position.MANAGER, 12000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2));

        Map<String, List<Employee>> result = service.getEmployeesByPosition();

        assertThat(result).containsKeys("PROGRAMISTA", "MANAGER")
                .extractingByKey("PROGRAMISTA").asList().hasSize(1);
    }

    @Test
    void getPositionStatistics_emptyDatabase_returnsEmpty() {
        when(employeeDAO.findAll()).thenReturn(Collections.emptyList());

        Map<String, Integer> result = service.getPositionStatistics();

        assertThat(result).isEmpty();
    }

    @Test
    void getPositionStatistics_singlePosition_countsCorrectly() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);

        when(employeeDAO.findAll()).thenReturn(List.of(emp));

        Map<String, Integer> result = service.getPositionStatistics();

        assertThat(result).containsEntry("PROGRAMISTA", 1);
    }

    @Test
    void getPositionStatistics_multiplePositions_countsCorrectly() {
        Employee emp1 = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Anna", "Nowak", "X", "anna@x.com", Position.PROGRAMISTA, 9000);
        Employee emp3 = new Employee("Bob", "Smith", "Y", "bob@y.com", Position.MANAGER, 12000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2, emp3));

        Map<String, Integer> result = service.getPositionStatistics();

        assertThat(result).containsEntry("PROGRAMISTA", 2).containsEntry("MANAGER", 1);
    }

    @Test
    void getAverageSalary_emptyEmployees_returnsZero() {
        when(employeeDAO.findAll()).thenReturn(Collections.emptyList());

        double result = service.getAverageSalary();

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void getAverageSalary_singleEmployee_returnsBaseSalary() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp));

        double result = service.getAverageSalary();

        assertThat(result).isEqualTo(8000.0);
    }

    @Test
    void getAverageSalary_multipleEmployees_calculatesCorrectly() {
        Employee emp1 = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Anna", "Nowak", "X", "anna@x.com", Position.MANAGER, 12000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2));

        double result = service.getAverageSalary();

        assertThat(result).isEqualTo(10000.0);
    }

    @Test
    void getHighestPaidEmployee_empty_returnsEmptyOptional() {
        when(employeeDAO.findAll()).thenReturn(Collections.emptyList());

        Optional<Employee> result = service.getHighestPaidEmployee();

        assertThat(result).isEmpty();
    }

    @Test
    void getHighestPaidEmployee_singleEmployee_returnsThatEmployee() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp));

        Optional<Employee> result = service.getHighestPaidEmployee();

        assertThat(result).isPresent().get().extracting(Employee::getEmail).isEqualTo("jan@x.com");
    }

    @Test
    void getHighestPaidEmployee_multipleEmployees_returnsHighest() {
        Employee emp1 = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Anna", "Nowak", "Y", "anna@y.com", Position.WICEPREZES, 9500);
        when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2));

        Optional<Employee> result = service.getHighestPaidEmployee();

        assertThat(result).isPresent().get().extracting(Employee::getPosition).isEqualTo(Position.WICEPREZES);
    }

    @Test
    void validateSalaryConsistency_emptyDatabase_returnsEmpty() {
        when(employeeDAO.findAll()).thenReturn(Collections.emptyList());

        List<Employee> result = service.validateSalaryConsistency();

        assertThat(result).isEmpty();
    }

    @Test
    void validateSalaryConsistency_allConsistent_returnsEmpty() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp));

        List<Employee> result = service.validateSalaryConsistency();

        assertThat(result).isEmpty();
    }

    @Test
    void validateSalaryConsistency_singleInconsistent_detectsIt() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 5000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp));

        List<Employee> result = service.validateSalaryConsistency();

        assertThat(result).hasSize(1).extracting(Employee::getEmail).contains("jan@x.com");
    }

    @Test
    void validateSalaryConsistency_multipleInconsistent_detectsAll() {
        Employee emp1 = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 5000);
        Employee emp2 = new Employee("Anna", "Nowak", "Y", "anna@y.com", Position.MANAGER, 8000);
        when(employeeDAO.findAll()).thenReturn(List.of(emp1, emp2));

        List<Employee> result = service.validateSalaryConsistency();

        assertThat(result).hasSize(2);
    }

    @Test
    void getCompanyStatistics_emptyDatabase_returnsEmpty() {
        when(employeeDAO.getCompanyStatistics()).thenReturn(Collections.emptyList());

        Map<String, CompanyStatistics> result = service.getCompanyStatistics();

        assertThat(result).isEmpty();
    }

    @Test
    void getCompanyStatistics_singleCompany_calculatesCorrectly() {
        CompanyStatistics stat = new CompanyStatistics("Acme", 1, 8000.0, "Jan Kowalski");
        when(employeeDAO.getCompanyStatistics()).thenReturn(List.of(stat));

        Map<String, CompanyStatistics> result = service.getCompanyStatistics();

        assertThat(result).containsKey("Acme");
    }

    @Test
    void getCompanyStatistics_multipleCompanies_groupsCorrectly() {
        CompanyStatistics stat1 = new CompanyStatistics("Acme", 1, 8000.0, "Jan Kowalski");
        CompanyStatistics stat2 = new CompanyStatistics("XYZ", 2, 10000.0, "Anna Nowak");
        when(employeeDAO.getCompanyStatistics()).thenReturn(List.of(stat1, stat2));

        Map<String, CompanyStatistics> result = service.getCompanyStatistics();

        assertThat(result).containsKeys("Acme", "XYZ");
    }

    @Test
    void getEmployeeByEmail_notFound_returnsNull() {
        when(employeeDAO.findByEmail("notfound@x.com")).thenReturn(Optional.empty());

        Employee result = service.getEmployeeByEmail("notfound@x.com");

        assertThat(result).isNull();
    }

    @Test
    void getEmployeeByEmail_found_returnsEmployee() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findByEmail("jan@x.com")).thenReturn(Optional.of(emp));

        Employee result = service.getEmployeeByEmail("jan@x.com");

        assertThat(result).isNotNull().extracting(Employee::getEmail).isEqualTo("jan@x.com");
    }

    @Test
    void getEmployeeByEmail_caseInsensitive_returnsEmployee() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findByEmail("jan@x.com")).thenReturn(Optional.of(emp));

        Employee result = service.getEmployeeByEmail("JAN@X.COM");

        assertThat(result).isNotNull();
        verify(employeeDAO).findByEmail("jan@x.com");
    }

    @Test
    void updateEmployee_notFound_returnsNull() {
        Employee updated = new Employee("A", "B", "X", "a@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findByEmail("notfound@x.com")).thenReturn(Optional.empty());

        Employee result = service.updateEmployee("notfound@x.com", updated);

        assertThat(result).isNull();
        verify(employeeDAO, never()).save(any());
    }

    @Test
    void updateEmployee_validUpdate_updatesEmployee() {
        Employee e1 = new Employee("A", "B", "X", "old@x.com", Position.PROGRAMISTA, 8000);
        Employee updated = new Employee("NewName", "NewSurname", "Y", "old@x.com", Position.MANAGER, 12000);
        when(employeeDAO.findByEmail("old@x.com")).thenReturn(Optional.of(e1));

        Employee result = service.updateEmployee("old@x.com", updated);

        assertThat(result).isNotNull().extracting(Employee::getName).isEqualTo("NewName");
        verify(employeeDAO).save(result);
    }

    @Test
    void updateEmployee_duplicateEmail_throws() {
        Employee e1 = new Employee("A", "B", "X", "a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("C", "D", "X", "c@x.com", Position.MANAGER, 12000);
        Employee updated = new Employee("A", "B", "X", "c@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findByEmail("a@x.com")).thenReturn(Optional.of(e1));
        when(employeeDAO.findByEmail("c@x.com")).thenReturn(Optional.of(e2));

        assertThatThrownBy(() -> service.updateEmployee("a@x.com", updated))
                .isInstanceOf(IllegalArgumentException.class);
        verify(employeeDAO, never()).save(any());
    }

    @Test
    void updateEmployee_sameEmail_succeeds() {
        Employee e1 = new Employee("A", "B", "X", "a@x.com", Position.PROGRAMISTA, 8000);
        Employee updated = new Employee("NewName", "B", "X", "a@x.com", Position.PROGRAMISTA, 9000);
        when(employeeDAO.findByEmail("a@x.com")).thenReturn(Optional.of(e1));

        Employee result = service.updateEmployee("a@x.com", updated);

        assertThat(result).isNotNull();
        verify(employeeDAO).save(result);
    }

    @Test
    void deleteEmployee_notFound_returnsFalse() {
        when(employeeDAO.findByEmail("notfound@x.com")).thenReturn(Optional.empty());

        boolean result = service.deleteEmployee("notfound@x.com");

        assertThat(result).isFalse();
        verify(employeeDAO, never()).delete(any());
    }

    @Test
    void deleteEmployee_found_deletesAndReturnsTrue() {
        Employee e1 = new Employee("A", "B", "X", "a@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findByEmail("a@x.com")).thenReturn(Optional.of(e1));

        boolean result = service.deleteEmployee("a@x.com");

        assertThat(result).isTrue();
        verify(employeeDAO).delete("a@x.com");
    }

    @Test
    void updateEmployeeStatus_notFound_returnsNull() {
        when(employeeDAO.findByEmail("notfound@x.com")).thenReturn(Optional.empty());

        Employee result = service.updateEmployeeStatus("notfound@x.com", EmploymentStatus.ACTIVE);

        assertThat(result).isNull();
    }

    @Test
    void updateEmployeeStatus_found_updatesStatus() {
        Employee e1 = new Employee("A", "B", "X", "a@x.com", Position.PROGRAMISTA, 8000);
        when(employeeDAO.findByEmail("a@x.com")).thenReturn(Optional.of(e1));

        Employee result = service.updateEmployeeStatus("a@x.com", EmploymentStatus.ON_LEAVE);

        assertThat(result).isNotNull().extracting(Employee::getStatus).isEqualTo(EmploymentStatus.ON_LEAVE);
        verify(employeeDAO).save(result);
    }

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
    void getStatusDistribution_emptyDatabase_returnsEmpty() {
        when(employeeDAO.findAll()).thenReturn(Collections.emptyList());

        Map<String, Integer> result = service.getStatusDistribution();

        assertThat(result).isEmpty();
    }

    @Test
    void getStatusDistribution_multipleStatuses_countsCorrectly() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B", "B", "X", "b@x.com", Position.MANAGER, 12000);
        e1.setStatus(EmploymentStatus.ACTIVE);
        e2.setStatus(EmploymentStatus.ON_LEAVE);
        when(employeeDAO.findAll()).thenReturn(List.of(e1, e2));

        Map<String, Integer> result = service.getStatusDistribution();

        assertThat(result).containsEntry("ACTIVE", 1).containsEntry("ON_LEAVE", 1);
    }

    @Test
    void getEmployeesByStatus_withMatches_returnsFiltered() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B", "B", "X", "b@x.com", Position.MANAGER, 12000);
        e1.setStatus(EmploymentStatus.ON_LEAVE);
        e2.setStatus(EmploymentStatus.ACTIVE);
        when(employeeDAO.findAll()).thenReturn(List.of(e1, e2));

        List<Employee> result = service.getEmployeesByStatus(EmploymentStatus.ON_LEAVE);

        assertThat(result).hasSize(1).extracting(Employee::getEmail).contains("a@x.com");
    }

    @Test
    void getEmployeesManagerAndAbove_withManagers_returnsManagers() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B", "B", "X", "b@x.com", Position.MANAGER, 12000);
        when(employeeDAO.findAll()).thenReturn(List.of(e1, e2));

        List<Employee> result = service.getEmployeesManagerAndAbove();

        assertThat(result).hasSize(1).extracting(Employee::getPosition).contains(Position.MANAGER);
    }

    @Test
    void getEmployeesByDepartment_withMatches_returnsFiltered() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("B", "B", "X", "b@x.com", Position.MANAGER, 12000);
        e1.setDepartmentId(1L);
        e2.setDepartmentId(1L);
        when(employeeDAO.findAll()).thenReturn(List.of(e1, e2));

        List<Employee> result = service.getEmployeesByDepartment(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void getCompanyStatisticsDTO_found_returnsDTO() {
        CompanyStatistics stat = new CompanyStatistics("Acme", 2, 10000.0, "Jan Kowalski");
        Employee e1 = new Employee("Jan", "Kowalski", "Acme", "jan@x.com", Position.PROGRAMISTA, 8000);
        Employee e2 = new Employee("Anna", "Nowak", "Acme", "anna@x.com", Position.MANAGER, 12000);
        when(employeeDAO.getCompanyStatistics()).thenReturn(List.of(stat));
        when(employeeDAO.findAll()).thenReturn(List.of(e1, e2));

        var result = service.getCompanyStatisticsDTO("Acme");

        assertThat(result).isPresent().get().extracting("companyName").isEqualTo("Acme");
    }
}