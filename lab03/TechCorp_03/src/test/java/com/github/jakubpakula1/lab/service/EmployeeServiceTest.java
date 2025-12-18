package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import com.github.jakubpakula1.lab.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService service;

    @Test
    void addEmployee_valid_succeeds() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        when(employeeRepository.existsByEmail("jan@x.com")).thenReturn(false);
        when(employeeRepository.save(emp)).thenReturn(emp);

        service.addEmployee(emp);

        verify(employeeRepository).save(emp);
    }

    @Test
    void addEmployee_duplicateEmail_throws() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        when(employeeRepository.existsByEmail("jan@x.com")).thenReturn(true);

        assertThatThrownBy(() -> service.addEmployee(emp))
                .isInstanceOf(com.github.jakubpakula1.lab.exception.DuplicateEmailException.class);
        verify(employeeRepository, never()).save(emp);
    }

    @Test
    void displayWorkers_empty_doesNotThrow() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatCode(() -> service.displayWorkers()).doesNotThrowAnyException();
    }

    @Test
    void displayWorkers_withEmployees_doesNotThrow() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        when(employeeRepository.findAll()).thenReturn(List.of(emp));

        assertThatCode(() -> service.displayWorkers()).doesNotThrowAnyException();
    }

    @Test
    void getCompanyEmployees_emptyDatabase_returnsEmpty() {
        when(employeeRepository.findByCompanyIgnoreCase("X")).thenReturn(Collections.emptyList());

        List<Employee> result = service.getCompanyEmployees("X");

        assertThat(result).isEmpty();
    }

    @Test
    void getCompanyEmployees_nonExistingCompany_returnsEmpty() {
        when(employeeRepository.findByCompanyIgnoreCase("NonExisting")).thenReturn(Collections.emptyList());

        List<Employee> result = service.getCompanyEmployees("NonExisting");

        assertThat(result).isEmpty();
    }

    @Test
    void getCompanyEmployees_nullCompanyName_returnsEmpty() {
        assertThat(service.getCompanyEmployees(null)).isEmpty();
    }

    @Test
    void getCompanyEmployees_caseInsensitive_returnsEmployees() {
        Employee emp = new Employee("Jan", "Kowalski", "Acme", "jan@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        when(employeeRepository.findByCompanyIgnoreCase("acme")).thenReturn(List.of(emp));

        List<Employee> result = service.getCompanyEmployees("acme");

        assertThat(result).hasSize(1).extracting(Employee::getEmail).contains("jan@x.com");
    }

    @Test
    void getEmployeesSortedByLastName_emptyDatabase_returnsEmpty() {
        when(employeeRepository.findAllByOrderBySurnameAsc()).thenReturn(Collections.emptyList());

        List<Employee> result = service.getEmployeesSortedByLastName();

        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeesSortedByLastName_sortsByLastName() {
        Employee emp1 = new Employee("Jan", "Zebra", "X", "jan@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee emp2 = new Employee("Anna", "Apple", "X", "anna@x.com", Position.MANAGER, BigDecimal.valueOf(12000));
        when(employeeRepository.findAllByOrderBySurnameAsc()).thenReturn(List.of(emp2, emp1));

        List<Employee> result = service.getEmployeesSortedByLastName();

        assertThat(result).extracting(Employee::getSurname).containsExactly("Apple", "Zebra");
    }

    @Test
    void getEmployeeByEmail_notFound_returnsNull() {
        when(employeeRepository.findByEmail("notfound@x.com")).thenReturn(Optional.empty());

        Employee result = service.getEmployeeByEmail("notfound@x.com");

        assertThat(result).isNull();
    }

    @Test
    void getEmployeeByEmail_found_returnsEmployee() {
        Employee emp = new Employee("Jan", "Kowalski", "X", "jan@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        when(employeeRepository.findByEmail("jan@x.com")).thenReturn(Optional.of(emp));

        Employee result = service.getEmployeeByEmail("jan@x.com");

        assertThat(result).isNotNull().extracting(Employee::getEmail).isEqualTo("jan@x.com");
    }

    @Test
    void updateEmployee_notFound_returnsNull() {
        Employee updated = new Employee("A", "B", "X", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        when(employeeRepository.findByEmail("notfound@x.com")).thenReturn(Optional.empty());

        Employee result = service.updateEmployee("notfound@x.com", updated);

        assertThat(result).isNull();
    }

    @Test
    void updateEmployee_validUpdate_updatesEmployee() {
        Employee e1 = new Employee("A", "B", "X", "old@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee updated = new Employee("NewName", "NewSurname", "Y", "old@x.com", Position.MANAGER, BigDecimal.valueOf(12000));
        when(employeeRepository.findByEmail("old@x.com")).thenReturn(Optional.of(e1));
        when(employeeRepository.save(any())).thenReturn(e1);

        Employee result = service.updateEmployee("old@x.com", updated);

        assertThat(result).isNotNull();
        verify(employeeRepository).save(any());
    }

    @Test
    void updateEmployee_changeEmail_updatesEmail() {
        Employee e1 = new Employee("A", "B", "X", "old@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee updated = new Employee("A", "B", "X", "new@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        when(employeeRepository.findByEmail("old@x.com")).thenReturn(Optional.of(e1));
        when(employeeRepository.findByEmail("new@x.com")).thenReturn(Optional.empty());
        when(employeeRepository.save(any())).thenReturn(e1);

        Employee result = service.updateEmployee("old@x.com", updated);

        assertThat(result).isNotNull();
        verify(employeeRepository).save(any());
    }

    @Test
    void updateEmployee_changeEmailToDuplicate_throws() {
        Employee e1 = new Employee("A", "B", "X", "old@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee updated = new Employee("A", "B", "X", "duplicate@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        when(employeeRepository.findByEmail("old@x.com")).thenReturn(Optional.of(e1));
        when(employeeRepository.findByEmail("duplicate@x.com")).thenReturn(Optional.of(new Employee("C", "D", "X", "duplicate@x.com", Position.MANAGER, BigDecimal.valueOf(12000))));

        assertThatThrownBy(() -> service.updateEmployee("old@x.com", updated))
                .isInstanceOf(com.github.jakubpakula1.lab.exception.DuplicateEmailException.class);
    }

    @Test
    void deleteEmployee_notFound_returnsFalse() {
        when(employeeRepository.findByEmail("notfound@x.com")).thenReturn(Optional.empty());

        boolean result = service.deleteEmployee("notfound@x.com");

        assertThat(result).isFalse();
    }

    @Test
    void deleteEmployee_found_deletesAndReturnsTrue() {
        Employee e1 = new Employee("A", "B", "X", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        when(employeeRepository.findByEmail("a@x.com")).thenReturn(Optional.of(e1));

        boolean result = service.deleteEmployee("a@x.com");

        assertThat(result).isTrue();
        verify(employeeRepository).delete(e1);
    }

    @Test
    void getStatusDistribution_emptyDatabase_returnsEmpty() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Integer> result = service.getStatusDistribution();

        assertThat(result).isEmpty();
    }

    @Test
    void getStatusDistribution_withEmployees_returnsDistribution() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        e1.setStatus(EmploymentStatus.ACTIVE);
        Employee e2 = new Employee("B", "B", "X", "b@x.com", Position.MANAGER, BigDecimal.valueOf(12000));
        e2.setStatus(EmploymentStatus.ACTIVE);
        Employee e3 = new Employee("C", "C", "X", "c@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(9000));
        e3.setStatus(EmploymentStatus.ON_LEAVE);
        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2, e3));

        Map<String, Integer> result = service.getStatusDistribution();

        assertThat(result).containsEntry("ACTIVE", 2).containsEntry("ON_LEAVE", 1);
    }

    @Test
    void getEmployeesByStatus_withMatches_returnsFiltered() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        e1.setStatus(EmploymentStatus.ON_LEAVE);
        when(employeeRepository.findByStatus(EmploymentStatus.ON_LEAVE)).thenReturn(List.of(e1));

        List<Employee> result = service.getEmployeesByStatus(EmploymentStatus.ON_LEAVE);

        assertThat(result).hasSize(1).extracting(Employee::getEmail).contains("a@x.com");
    }

    @Test
    void getEmployeesByStatus_noMatches_returnsEmpty() {
        when(employeeRepository.findByStatus(EmploymentStatus.ON_LEAVE)).thenReturn(Collections.emptyList());

        List<Employee> result = service.getEmployeesByStatus(EmploymentStatus.ON_LEAVE);

        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeesByDepartment_withMatches_returnsFiltered() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee e2 = new Employee("B", "B", "X", "b@x.com", Position.MANAGER, BigDecimal.valueOf(12000));
        when(employeeRepository.findByDepartment_Id(1L)).thenReturn(List.of(e1, e2));

        List<Employee> result = service.getEmployeesByDepartment(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void getEmployeesByDepartment_noMatches_returnsEmpty() {
        when(employeeRepository.findByDepartment_Id(1L)).thenReturn(Collections.emptyList());

        List<Employee> result = service.getEmployeesByDepartment(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllEmployees_returnsAllEmployees() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee e2 = new Employee("B", "B", "X", "b@x.com", Position.MANAGER, BigDecimal.valueOf(12000));
        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));

        List<Employee> result = service.getAllEmployees();

        assertThat(result).hasSize(2);
    }

    @Test
    void getAllEmployees_emptyDatabase_returnsEmpty() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Employee> result = service.getAllEmployees();

        assertThat(result).isEmpty();
    }

    @Test
    void getEmployeesByPosition_withMatches_returnsFiltered() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee e2 = new Employee("B", "B", "X", "b@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(9000));
        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));

        Map<String, List<Employee>> result = service.getEmployeesByPosition();

        assertThat(result).containsKey("PROGRAMISTA");
        assertThat(result.get("PROGRAMISTA")).hasSize(2);
    }

    @Test
    void getHighestPaidEmployee_withEmployees_returnsHighest() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee e2 = new Employee("B", "B", "X", "b@x.com", Position.MANAGER, BigDecimal.valueOf(12000));
        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));

        Optional<Employee> result = service.getHighestPaidEmployee();

        assertThat(result).isPresent();
        assertThat(result.get().getSalary()).isEqualTo(BigDecimal.valueOf(12000));
    }

    @Test
    void getHighestPaidEmployee_emptyDatabase_returnsEmpty() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        Optional<Employee> result = service.getHighestPaidEmployee();

        assertThat(result).isEmpty();
    }

    @Test
    void getAverageSalary_withEmployees_returnsAverage() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee e2 = new Employee("B", "B", "X", "b@x.com", Position.MANAGER, BigDecimal.valueOf(12000));
        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));

        double result = service.getAverageSalary();

        assertThat(result).isEqualTo(10000);
    }

    @Test
    void getAverageSalary_emptyDatabase_returnsEmpty() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        double result = service.getAverageSalary();

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void getAverageSalaryByCompany_withEmployees_returnsAverage() {
        Employee e1 = new Employee("A", "A", "Acme", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee e2 = new Employee("B", "B", "Acme", "b@x.com", Position.MANAGER, BigDecimal.valueOf(12000));
        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));

        double result = service.getAverageSalary("Acme");

        assertThat(result).isEqualTo(10000);
    }

    @Test
    void getAverageSalaryByCompany_noEmployees_returnsEmpty() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        double result = service.getAverageSalary("NonExistent");

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void updateEmployeeStatus_found_updates() {
        Employee e1 = new Employee("A", "B", "X", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        e1.setStatus(EmploymentStatus.ACTIVE);
        when(employeeRepository.findByEmail("a@x.com")).thenReturn(Optional.of(e1));

        Employee result = service.updateEmployeeStatus("a@x.com", EmploymentStatus.ON_LEAVE);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(EmploymentStatus.ON_LEAVE);
    }

    @Test
    void updateEmployeeStatus_notFound_returnsNull() {
        when(employeeRepository.findByEmail("notfound@x.com")).thenReturn(Optional.empty());

        Employee result = service.updateEmployeeStatus("notfound@x.com", EmploymentStatus.ON_LEAVE);

        assertThat(result).isNull();
    }

    @Test
    void getPositionStatistics_withEmployees_returnsStats() {
        Employee e1 = new Employee("A", "A", "X", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee e2 = new Employee("B", "B", "X", "b@x.com", Position.MANAGER, BigDecimal.valueOf(12000));
        Employee e3 = new Employee("C", "C", "X", "c@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(9000));
        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2, e3));

        Map<String, Integer> result = service.getPositionStatistics();

        assertThat(result).containsEntry("PROGRAMISTA", 2).containsEntry("MANAGER", 1);
    }

    @Test
    void getPositionStatistics_emptyDatabase_returnsEmpty() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Integer> result = service.getPositionStatistics();

        assertThat(result).isEmpty();
    }

    @Test
    void getCompanyStatistics_withEmployees_returnsStats() {
        Employee e1 = new Employee("A", "A", "Acme", "a@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(8000));
        Employee e2 = new Employee("B", "B", "Acme", "b@x.com", Position.MANAGER, BigDecimal.valueOf(12000));
        Employee e3 = new Employee("C", "C", "TechCorp", "c@x.com", Position.PROGRAMISTA, BigDecimal.valueOf(9000));
        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2, e3));

        Map<String, com.github.jakubpakula1.lab.model.CompanyStatistics> result = service.getCompanyStatistics();

        assertThat(result).containsKeys("Acme", "TechCorp");
        assertThat(result.get("Acme").getNumberOfEmployees()).isEqualTo(2);
        assertThat(result.get("TechCorp").getNumberOfEmployees()).isEqualTo(1);
    }

    @Test
    void getCompanyStatistics_emptyDatabase_returnsEmpty() {
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, com.github.jakubpakula1.lab.model.CompanyStatistics> result = service.getCompanyStatistics();

        assertThat(result).isEmpty();
    }

    @Test
    void deleteAllEmployees_deletesAll() {
        service.deleteAllEmployees();

        verify(employeeRepository).deleteAll();
    }
}