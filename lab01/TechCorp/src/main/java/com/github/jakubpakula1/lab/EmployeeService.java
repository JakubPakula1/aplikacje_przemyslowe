package com.github.jakubpakula1.lab;

import com.github.jakubpakula1.lab.modules.Employee;
import com.github.jakubpakula1.lab.modules.Position;

import java.util.*;

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
}
