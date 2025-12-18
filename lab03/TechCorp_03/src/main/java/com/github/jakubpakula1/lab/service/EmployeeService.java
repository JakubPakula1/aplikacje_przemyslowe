package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.dto.CompanyStatisticsDTO;
import com.github.jakubpakula1.lab.dto.EmployeeListProjection;
import com.github.jakubpakula1.lab.exception.DuplicateEmailException;
import com.github.jakubpakula1.lab.model.CompanyStatistics;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import com.github.jakubpakula1.lab.repository.EmployeeRepository;
import com.github.jakubpakula1.lab.specification.EmployeeSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployeesPage(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees(){
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<EmployeeListProjection> getAllEmployeesProjected(Pageable pageable) {
        return employeeRepository.findAllProjected(pageable);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeListProjection> getCompanyEmployeesProjected(String company, Pageable pageable) {
        if (company == null || company.isBlank()) return Page.empty(pageable);
        return employeeRepository.findByCompanyIgnoreCaseProjected(company, pageable);
    }
    
    public Employee getEmployeeByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return employeeRepository.findByEmail(email).orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<Employee> searchEmployees(String name, String surname, String company,
                                          Position position, Integer minSalary,
                                          Integer maxSalary, Long departmentId, Pageable pageable) {
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.hasFirstName(name))
                .and(EmployeeSpecification.hasLastName(surname))
                .and(EmployeeSpecification.hasCompany(company))
                .and(EmployeeSpecification.hasPosition(position))
                .and(EmployeeSpecification.salaryGreaterThanOrEqual(minSalary))
                .and(EmployeeSpecification.salaryLessThanOrEqual(maxSalary))
                .and(EmployeeSpecification.hasDepartment(departmentId));

        return employeeRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<Employee> searchEmployees(String name, String surname, String company,
                                          Position position, Integer minSalary,
                                          Integer maxSalary, Long departmentId) {
        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.hasFirstName(name))
                .and(EmployeeSpecification.hasLastName(surname))
                .and(EmployeeSpecification.hasCompany(company))
                .and(EmployeeSpecification.hasPosition(position))
                .and(EmployeeSpecification.salaryGreaterThanOrEqual(minSalary))
                .and(EmployeeSpecification.salaryLessThanOrEqual(maxSalary))
                .and(EmployeeSpecification.hasDepartment(departmentId));

        return employeeRepository.findAll(spec);
    }

    @Transactional
    public Employee addEmployee(@Valid Employee employee) {
        if (employee == null) return null;
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new DuplicateEmailException("Employee with this email already exists!");
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(String email, @Valid Employee updated) {
        if (email == null || email.isBlank() || updated == null) return null;

        Optional<Employee> existingOpt = employeeRepository.findByEmail(email);
        if (existingOpt.isEmpty()) return null;

        Employee employee = existingOpt.get();

        String newEmail = updated.getEmail();
        if (newEmail != null && !newEmail.equalsIgnoreCase(email)) {
            if (employeeRepository.findByEmail(newEmail).isPresent()) {
                throw new DuplicateEmailException("Employee with this email already exists!");
            }
            employee.setEmail(newEmail);
        }

        employee.setName(updated.getName());
        employee.setSurname(updated.getSurname());
        employee.setCompany(updated.getCompany());
        employee.setPosition(updated.getPosition());
        employee.setSalary(updated.getSalary());
        employee.setStatus(updated.getStatus());

        employee.setDepartment(updated.getDepartment());
        employee.setPhotoFileName(updated.getPhotoFileName());

        return employeeRepository.save(employee);
    }
    @Transactional
    public boolean deleteEmployee(String email) {
        if (email == null || email.isBlank()) return false;

        Optional<Employee> existing = employeeRepository.findByEmail(email.toLowerCase());
        if (existing.isEmpty()) return false;

        employeeRepository.delete(existing.get());
        return true;
    }

    public void displayWorkers() {
        List<Employee> employees = employeeRepository.findAll();
        if (employees == null || employees.isEmpty()) {
            System.out.println("There aren't any employees in database.");
            return;
        }

        System.out.println("List of all employees:");
        int index = 1;
        for (Employee e : employees) {
            if (e != null) {
                System.out.printf("%d. %s%n", index++, e);
            }
        }
    }
    @Transactional(readOnly = true)
    public List<Employee> getCompanyEmployees(String company) {
        if (company == null || company.isBlank()) return Collections.emptyList();
        return employeeRepository.findByCompanyIgnoreCase(company);
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeesSortedByLastName() {
        return employeeRepository.findAllByOrderBySurnameAsc();
    }

    @Transactional(readOnly = true)
    public Map<String, List<Employee>> getEmployeesByPosition() {
        return getAllEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .collect(Collectors.groupingBy(e -> e.getPosition().name()));
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getPositionStatistics() {
        return getAllEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .collect(Collectors.groupingBy(e -> e.getPosition().name(), Collectors.summingInt(e -> 1)));
    }

    @Transactional(readOnly = true)
    public double getAverageSalary() {
        return getAverageSalary(null);
    }

    @Transactional(readOnly = true)
    public double getAverageSalary(String company) {
        return getAllEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> company == null || company.isBlank() || company.equalsIgnoreCase(e.getCompany()))
                .map(Employee::getSalary)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getStatusDistribution() {
        return  getAllEmployees().stream()
                .filter(Objects::nonNull)
                .map(Employee::getStatus)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Enum::name, Collectors.summingInt(s -> 1)));
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getHighestPaidEmployee() {
        return getAllEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .max(Comparator.comparing(
                        e -> e.getPosition().getBaseSalary(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ));
    }

    @Transactional(readOnly = true)
    public List<Employee> validateSalaryConsistency() {
        return getAllEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null && e.getSalary() != null && e.getPosition().getBaseSalary() != null)
                .filter(e -> e.getSalary().compareTo(e.getPosition().getBaseSalary()) < 0)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, CompanyStatistics> getCompanyStatistics() {
        return getAllEmployees().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Employee::getCompany,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                employees -> new CompanyStatistics(
                                        employees.get(0).getCompany(),
                                        employees.size(),
                                        employees.stream()
                                                .map(Employee::getSalary)
                                                .filter(Objects::nonNull)
                                                .mapToDouble(BigDecimal::doubleValue)
                                                .average()
                                                .orElse(0.0),
                                        employees.stream()
                                                .max(Comparator.comparing(
                                                        Employee::getSalary,
                                                        Comparator.nullsLast(Comparator.naturalOrder())
                                                ))
                                                .map(e -> e.getName() + " " + e.getName())
                                                .orElse("")
                                )
                        )
                ));
    }

    @Transactional(readOnly = true)
    public Optional<CompanyStatistics> getCompanyStatistics(String company) {
        if (company == null || company.isBlank()) {
            return Optional.empty();
        }

        return getAllEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getCompany() != null && e.getCompany().equalsIgnoreCase(company.trim()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        employees -> employees.isEmpty() ? Optional.empty() : Optional.of(
                                new CompanyStatistics(
                                        company.trim(),
                                        employees.size(),
                                        employees.stream()
                                                .map(Employee::getSalary)
                                                .filter(Objects::nonNull)
                                                .mapToDouble(BigDecimal::doubleValue)
                                                .average()
                                                .orElse(0.0),
                                        employees.stream()
                                                .max(Comparator.comparing(
                                                        Employee::getSalary,
                                                        Comparator.nullsLast(Comparator.naturalOrder())
                                                ))
                                                .map(e -> e.getName() + " " + e.getName())
                                                .orElse("")
                                )
                        )
                ));
    }

    @Transactional(readOnly = true)
    public Optional<CompanyStatisticsDTO> getCompanyStatisticsDTO(String company) {
        if (company == null || company.isBlank()) {
            return Optional.empty();
        }

        String name = company.trim();
        List<Employee> companyEmployees = getCompanyEmployees(name);

        if (companyEmployees.isEmpty()) {
            return Optional.empty();
        }

        double highestSalaryDouble = companyEmployees.stream()
                .map(Employee::getSalary)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .max()
                .orElse(0.0);

        int highestSalary = (int) highestSalaryDouble;

        String bestEarner = companyEmployees.stream()
                .max(Comparator.comparing(
                        Employee::getSalary,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(e -> e.getName() + " " + e.getName())
                .orElse("");

        double averageSalary = companyEmployees.stream()
                .map(Employee::getSalary)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                name,
                companyEmployees.size(),
                averageSalary,
                highestSalary,
                bestEarner
        );
        return Optional.of(dto);
    }
    @Transactional
    public Employee updateEmployeeStatus(String email, EmploymentStatus status) {
        if (email == null || email.isBlank() || status == null) {
            throw new IllegalArgumentException("Email i status muszą być podane");
        }

        Employee existing = getEmployeeByEmail(email);
        if (existing == null) return null;

        existing.setStatus(status);

        return existing;
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByStatus(EmploymentStatus status) {
        if (status == null) return Collections.emptyList();
        return employeeRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeesManagerAndAbove() {
        Map<String, List<Employee>> employees = getEmployeesByPosition();
        return employees.values()
                .stream()
                .flatMap(List::stream)
                .filter(employee -> {
                    Position position = employee.getPosition();
                    return position.getHierarchyLevel() <= 3;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        if (departmentId == null) return Collections.emptyList();
        return employeeRepository.findByDepartment_Id(departmentId);
    }

    @Transactional
    public void deleteAllEmployees() {
        employeeRepository.deleteAll();
    }

}