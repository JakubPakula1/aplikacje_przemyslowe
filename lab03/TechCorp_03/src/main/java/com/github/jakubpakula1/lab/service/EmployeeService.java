package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.dao.EmployeeDAO;
import com.github.jakubpakula1.lab.dto.CompanyStatisticsDTO;
import com.github.jakubpakula1.lab.model.CompanyStatistics;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeDAO employeeDAO;

    public EmployeeService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public List<Employee> getEmployees(){
        return employeeDAO.findAll();
    }

    public Employee getEmployeeByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return employeeDAO.findByEmail(email.toLowerCase()).orElse(null);
    }

    public Employee AddEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee can't be null");
        }
        Optional<Employee> existing = employeeDAO.findByEmail(employee.getEmail());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Employee with this email already exists!");
        }
        if (employee.getStatus() == null) {
            employee.setStatus(EmploymentStatus.ACTIVE);
        }

        employeeDAO.save(employee);
        return employee;
    }

    public Employee updateEmployee(String email, Employee updated) {
        if (email == null || email.isBlank() || updated == null) return null;

        Optional<Employee> existing = employeeDAO.findByEmail(email);
        if (existing.isEmpty()) return null;

        Employee employee = existing.get();

        String newEmail = updated.getEmail();
        if (newEmail != null && !newEmail.equalsIgnoreCase(email)) {
            Optional<Employee> emailExists = employeeDAO.findByEmail(newEmail);
            if (emailExists.isPresent()) {
                throw new IllegalArgumentException("Employee with this email already exists!");
            }
        }

        employee.setName(updated.getName());
        employee.setSurname(updated.getSurname());
        employee.setCompany(updated.getCompany());
        employee.setEmail(updated.getEmail());
        employee.setPosition(updated.getPosition());
        employee.setSalary(updated.getSalary());
        employee.setStatus(updated.getStatus());
        employee.setDepartmentId(updated.getDepartmentId());
        employee.setPhotoFileName(updated.getPhotoFileName());

        employeeDAO.save(employee);
        return employee;
    }

    public boolean deleteEmployee(String email) {
        if (email == null || email.isBlank()) return false;

        Optional<Employee> existing = employeeDAO.findByEmail(email.toLowerCase());
        if (existing.isEmpty()) return false;

        employeeDAO.delete(email);
        return true;
    }

    public void DisplayWorkers() {
        List<Employee> employees = getEmployees();
        if (employees.isEmpty()) {
            System.out.println("There aren't any employees in database.");
            return;
        }
        System.out.println("List of all employees:");
        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            if (e != null) {
                System.out.printf("%d. %s\n", i + 1, e);
            }
        }
    }

    public List<Employee> getCompanyEmployees(String company) {
        if (company == null || company.isBlank()) return Collections.emptyList();
        return getEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> company.equalsIgnoreCase(e.getCompany()))
                .collect(Collectors.toList());
    }

    public List<Employee> getEmployeesSortedByLastName() {
        return getEmployees().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Employee::getSurname, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public Map<String, List<Employee>> getEmployeesByPosition() {
        return getEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .collect(Collectors.groupingBy(e -> e.getPosition().name()));
    }

    public Map<String, Integer> getPositionStatistics() {
        return getEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .collect(Collectors.groupingBy(e -> e.getPosition().name(), Collectors.summingInt(e -> 1)));
    }

    public double getAverageSalary() {
        return getAverageSalary(null);
    }

    public double getAverageSalary(String company) {
        return getEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> company == null || company.isBlank() || company.equalsIgnoreCase(e.getCompany()))
                .mapToInt(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    public Map<String, Integer> getStatusDistribution() {
        return getEmployees().stream()
                .filter(Objects::nonNull)
                .map(Employee::getStatus)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Enum::name, Collectors.summingInt(s -> 1)));
    }

    public Optional<Employee> getHighestPaidEmployee() {
        return getEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .max(Comparator.comparingDouble(e -> e.getPosition().getBaseSalary()));
    }

    public List<Employee> validateSalaryConsistency() {
        return getEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .filter(e -> e.getSalary() < e.getPosition().getBaseSalary())
                .collect(Collectors.toList());
    }

    public Map<String, CompanyStatistics> getCompanyStatistics() {
        return employeeDAO.getCompanyStatistics().stream()
                .collect(Collectors.toMap(
                        CompanyStatistics::getCompany,
                        stat -> stat
                ));
    }

    public Optional<CompanyStatistics> getCompanyStatistics(String company) {
        if (company == null || company.isBlank()) {
            return Optional.empty();
        }

        return employeeDAO.getCompanyStatistics().stream()
                .filter(stat -> stat.getCompany() != null && stat.getCompany().equalsIgnoreCase(company.trim()))
                .findFirst();
    }


    public Optional<CompanyStatisticsDTO> getCompanyStatisticsDTO(String company) {
        if (company == null || company.isBlank()) {
            return Optional.empty();
        }
        String name = company.trim();

        List<CompanyStatistics> allStats = employeeDAO.getCompanyStatistics();
        CompanyStatistics cs = allStats.stream()
                .filter(stat -> stat.getBestEarningName() != null) // Zamiast sprawdzania company
                .findFirst()
                .orElse(null);

        if (cs == null) {
            return Optional.empty();
        }

        int highestSalary = getCompanyEmployees(name).stream()
                .filter(Objects::nonNull)
                .mapToInt(Employee::getSalary)
                .max()
                .orElse(0);

        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                name,
                cs.getNumberOfEmployees(),
                cs.getAverageSalary(),
                highestSalary,
                cs.getBestEarningName()
        );
        return Optional.of(dto);
    }

    public Employee updateEmployeeStatus(String email, EmploymentStatus status) {
        if (email == null || email.isBlank() || status == null) {
            throw new IllegalArgumentException("Email i status muszą być podane");
        }

        Optional<Employee> existing = employeeDAO.findByEmail(email);
        if (existing.isEmpty()) return null;

        Employee employee = existing.get();
        employee.setStatus(status);
        employeeDAO.save(employee);

        return employee;
    }

    public List<Employee> getEmployeesByStatus(EmploymentStatus status) {
        if (status == null) return Collections.emptyList();
        return getEmployees().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toList());
    }

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

    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        return getEmployees().stream()
                .filter(employee -> Objects.equals(employee.getDepartmentId(), departmentId))
                .toList();
    }

    @Transactional
    public void deleteAllEmployees() {
        employeeDAO.deleteAll();
    }

}