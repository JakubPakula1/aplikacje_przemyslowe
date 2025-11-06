package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.dto.CompanyStatisticsDTO;
import com.github.jakubpakula1.lab.model.CompanyStatistics;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final List<Employee> employees;

    public EmployeeService() {
        this.employees = new ArrayList<>();
    }

    public List<Employee> getEmployees(){
        return this.employees;
    }

    public Employee getEmployeeByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return employees.stream()
                .filter(Objects::nonNull)
                .filter(e -> email.equalsIgnoreCase(e.getEmail()))
                .findAny()
                .orElse(null);
    }

    public Employee AddEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee can't be null");
        }
        boolean exists = employees.stream()
                .filter(Objects::nonNull)
                .anyMatch(e -> e.equals(employee));
        if (exists) {
            throw new IllegalArgumentException("Employee with this email already exists!");
        }
        employees.add(employee);

        return employee;
    }

    public Employee updateEmployee(String email, Employee updated) {
        if (email == null || email.isBlank() || updated == null) return null;

        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            if (e != null && email.equalsIgnoreCase(e.getEmail())) {

                String newEmail = updated.getEmail();
                if (newEmail != null && !newEmail.equalsIgnoreCase(email)) {
                    boolean exists = employees.stream()
                            .filter(Objects::nonNull)
                            .anyMatch(emp -> newEmail.equalsIgnoreCase(emp.getEmail()));
                    if (exists) {
                        throw new IllegalArgumentException("Employee with this email already exists!");
                    }
                }

                e.setName(updated.getName());
                e.setSurname(updated.getSurname());
                e.setCompany(updated.getCompany());
                e.setEmail(updated.getEmail());
                e.setPosition(updated.getPosition());
                e.setSalary(updated.getSalary());
                e.setStatus(updated.getStatus());

                return e;
            }
        }
        return null;
    }

    public boolean deleteEmployee(String email) {
        if (email == null || email.isBlank()) return false;
        Iterator<Employee> it = employees.iterator();
        while (it.hasNext()) {
            Employee e = it.next();
            if (e != null && email.equalsIgnoreCase(e.getEmail())) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public void DisplayWorkers() {
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
        return employees.stream()
                .filter(Objects::nonNull)
                .filter(e -> company.equalsIgnoreCase(e.getCompany()))
                .collect(Collectors.toList());
    }

    public List<Employee> getEmployeesSortedByLastName() {
        return employees.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Employee::getSurname, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public Map<String, List<Employee>> getEmployeesByPosition() {
        return employees.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .collect(Collectors.groupingBy(e -> e.getPosition().name()));
    }

    public Map<String, Integer> getPositionStatistics() {
        return employees.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .collect(Collectors.groupingBy(e -> e.getPosition().name(), Collectors.summingInt(e -> 1)));
    }

    public double getAverageSalary() {
        return getAverageSalary(null);
    }

    public double getAverageSalary(String company) {
        return employees.stream()
                .filter(Objects::nonNull)
                .filter(e -> company == null || company.isBlank() || company.equalsIgnoreCase(e.getCompany()))
                .mapToInt(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    public Map<String, Integer> getStatusDistribution() {
        return employees.stream()
                .filter(Objects::nonNull)
                .map(Employee::getStatus)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(status -> status.name(), Collectors.summingInt(s -> 1)));
    }

    public Optional<Employee> getHighestPaidEmployee() {
        return employees.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .max(Comparator.comparingDouble(e -> e.getPosition().getBaseSalary()));
    }

    public List<Employee> validateSalaryConsistency() {
        return employees.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .filter(e -> e.getSalary() < e.getPosition().getBaseSalary())
                .collect(Collectors.toList());
    }

    public Map<String, CompanyStatistics> getCompanyStatistics() {
        return employees.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getCompany() != null && !e.getCompany().isBlank())
                .collect(Collectors.groupingBy(
                        Employee::getCompany,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            int count = list.size();
                            double avg = list.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
                            String best = list.stream().max(Comparator.comparingInt(Employee::getSalary))
                                    .map(Employee::getFullName).orElse("");
                            return new CompanyStatistics(count, avg, best);
                        })
                ));
    }

    public Optional<CompanyStatisticsDTO> getCompanyStatisticsDTO(String company) {
        if (company == null || company.isBlank()) {
            return Optional.empty();
        }
        String name = company.trim();
        Map<String, CompanyStatistics> statsMap = getCompanyStatistics();
        CompanyStatistics cs = statsMap != null ? statsMap.get(name) : null;
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
        for (int i = 0; i < employees.size(); i++) {
            Employee e = employees.get(i);
            if (e != null && email.equalsIgnoreCase(e.getEmail())) {
                e.setStatus(status);
                return e;
            }
        }
        return null;
    }

    public List<Employee> getEmployeesByStatus(EmploymentStatus status) {
        if (status == null) return Collections.emptyList();
        return employees.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toList());
    }
}