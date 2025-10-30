package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.model.CompanyStatistics;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.Position;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    Employee[] employees;

    public EmployeeService() {
        this.employees = new Employee[]{};
    }

    public void AddEmployee(Employee employee){
        if (employee == null){
            throw new IllegalArgumentException("Employee cant be null");
        }

        for(Employee existingEmployee : employees){
            if(existingEmployee != null && existingEmployee.equals(employee)){
                throw new IllegalArgumentException("Employee with this email already exists!");
            }
        }

        Employee[] newEmployees = new Employee[employees.length + 1];
        System.arraycopy(employees, 0, newEmployees, 0, employees.length);
        newEmployees[employees.length] = employee;

        employees = newEmployees;

        System.out.println("Added employee to database: " + employee.getFullName());
    }

    public void DisplayWorkers(){
        if (employees == null || employees.length == 0) {
            System.out.println("There isnt any employees in database.");
            return;
        }
        System.out.println("List of all employees:");
        for (int i = 0; i < employees.length; i++) {
            if (employees[i] != null) {
                System.out.printf("%d. %s\n", i + 1, employees[i]);
            }
        }
    }

    public Employee[] getCompanyEmployees(String company){
        if (employees == null || employees.length == 0) {
            System.out.println("There aren't any employees in the database.");
            return new Employee[0];
        }

        if (company == null || company.isBlank()) {
            System.out.println("Company name cannot be null or empty.");
            return new Employee[0];
        }

        return Arrays.stream(employees)
                .filter(Objects::nonNull)
                .filter(e -> company.equalsIgnoreCase(e.getCompany()))
                .toArray(Employee[]::new);
    }

    public Employee[] getEmployeesSortedByLastName(){
        if (employees == null || employees.length == 0) {
            System.out.println("There aren't any employees in the database.");
            return new Employee[0];
        }
        Employee[] sorted = Arrays.copyOf(employees, employees.length);

        Arrays.sort(sorted, Comparator.comparing(Employee::getSurname, String.CASE_INSENSITIVE_ORDER));

        return sorted;
    }

    public Map<String, List<Employee>> getEmployeesByPosition(){
        if (employees == null || employees.length == 0) {
            System.out.println("There aren't any employees in the database.");
            return Collections.emptyMap();
        }
        Map<String, List<Employee>> stats = new HashMap<>();
            for(Employee employee : employees){
                if(employee != null && employee.getPosition() != null){
                String position = employee.getPosition().name();
                stats.computeIfAbsent(position, k -> new ArrayList<>()).add(employee);
            }
        }
        return stats;
    }

    public Map<String, Integer> getPositionStatistics(){
        if (employees == null || employees.length == 0) {
            System.out.println("There aren't any employees in the database.");
            return Collections.emptyMap();
        }
        Map<String, Integer> stats = new HashMap<>();
        for(Employee employee : employees){
            if (employee != null && employee.getPosition() != null) {
                stats.put(employee.getPosition().name(), stats.getOrDefault(employee.getPosition().name(), 0) + 1);
            }
        }
        return stats;
    }

    public double getAverageSalary(){
        if (employees == null || employees.length == 0) {
            System.out.println("There aren't any employees in the database.");
            return 0.0;
        }

        return Arrays.stream(employees)
                .filter(Objects::nonNull)
                .map(Employee::getPosition)
                .filter(Objects::nonNull)
                .mapToDouble(Position::getBaseSalary)
                .average()
                .orElse(0.0);
    }

    public Optional<Employee> getHighestPaidEmployee(){
        if (employees == null || employees.length == 0) {
            System.out.println("There aren't any employees in the database.");
            return Optional.empty();
        }
        return Arrays.stream(employees)
                .filter(Objects::nonNull)
                .filter(employee -> employee.getPosition() != null)
                .max(Comparator.comparingDouble(e -> e.getPosition().getBaseSalary()));

    }

    public Employee[] validateSalaryConsistency(){
        if (employees == null || employees.length == 0) {
            System.out.println("There aren't any employees in the database.");
            return new Employee[0];
        }
        return Arrays.stream(employees)
                .filter(Objects::nonNull)
                .filter(e -> e.getPosition() != null)
                .filter(e -> e.getSalary() < e.getPosition().getBaseSalary())
                .toArray(Employee[] :: new);
    }

    public Map<String, CompanyStatistics> getCompanyStatistics(){
        if (employees == null || employees.length == 0) {
            System.out.println("There aren't any employees in the database.");
            return new HashMap<>();
        }
        return Arrays.stream(employees)
                .filter(Objects::nonNull)
                .filter(e -> e.getCompany() != null && !e.getCompany().isBlank())
                .collect(Collectors.groupingBy(
                        Employee::getCompany,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                employeeList -> {
                                    int count = employeeList.size();
                                    double avgSalary = employeeList.stream()
                                            .mapToDouble(Employee::getSalary)
                                            .average()
                                            .orElse(0.0);
                                    String bestEarningName = employeeList.stream()
                                            .max(Comparator.comparingInt(Employee::getSalary))
                                            .map(Employee::getFullName)
                                            .orElse("");
                                    return new CompanyStatistics(count, avgSalary, bestEarningName);
                                }
                        )
                ));
    }
}
