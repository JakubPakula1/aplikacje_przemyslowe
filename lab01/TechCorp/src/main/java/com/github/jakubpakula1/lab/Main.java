package com.github.jakubpakula1.lab;

import com.github.jakubpakula1.lab.modules.Employee;
import com.github.jakubpakula1.lab.modules.Position;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        // Inicjalizacja serwisu
        EmployeeService service = new EmployeeService();

        // Tworzenie przykładowych pracowników
        Employee pracownik1 = new Employee("Jan", "Kowalski", "ABC Corp", "jan@firma.pl", Position.PROGRAMISTA);
        Employee pracownik2 = new Employee("Anna", "Nowak", "ABC Corp", "anna@firma.pl", Position.MANAGER);
        Employee pracownik3 = new Employee("Piotr", "Wiśniewski", "XYZ Inc", "piotr@xyz.pl", Position.STAZYSTA);
        Employee pracownik4 = new Employee("Alicja", "Zając", "XYZ Inc", "alicja@xyz.pl", Position.WICEPREZES);
        Employee pracownik5 = new Employee("Marek", "Kowalczyk", "ABC Corp", "marek@firma.pl", Position.PREZES);

        // 1. AddEmployee - dodawanie pracowników
        System.out.println("=== Dodawanie pracowników ===");
        service.AddEmployee(pracownik1);
        service.AddEmployee(pracownik2);
        service.AddEmployee(pracownik3);
        service.AddEmployee(pracownik4);
        service.AddEmployee(pracownik5);

        // 2. DisplayWorkers - wyświetlenie wszystkich pracowników
        System.out.println("\n=== Lista wszystkich pracowników ===");
        service.DisplayWorkers();

        // 3. getCompanyEmployees - pracownicy z konkretnej firmy
        System.out.println("\n=== Pracownicy ABC Corp ===");
        Employee[] abcEmployees = service.getCompanyEmployees("ABC Corp");
        for (Employee emp : abcEmployees) {
            System.out.println(emp);
        }

        // 4. getEmployeesSortedByLastName - sortowanie po nazwisku
        System.out.println("\n=== Pracownicy posortowani wg nazwiska ===");
        Employee[] sortedEmployees = service.getEmployeesSortedByLastName();
        for (Employee emp : sortedEmployees) {
            System.out.println(emp);
        }

        // 5. getEmployeesByPosition - grupowanie wg stanowiska
        System.out.println("\n=== Grupowanie według stanowisk ===");
        Map<String, List<Employee>> byPosition = service.getEmployeesByPosition();
        for (Map.Entry<String, List<Employee>> entry : byPosition.entrySet()) {
            System.out.println("Stanowisko: " + entry.getKey());
            for (Employee emp : entry.getValue()) {
                System.out.println("  - " + emp.getName() + " " + emp.getSurname());
            }
        }

        // 6. getPositionStatistics - statystyki stanowisk
        System.out.println("\n=== Statystyki stanowisk ===");
        Map<String, Integer> stats = service.getPositionStatistics();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " pracowników");
        }

        // 7. getAverageSalary - średnia pensja
        System.out.println("\n=== Średnia pensja ===");
        double avgSalary = service.getAverageSalary();
        System.out.println("Średnia pensja: " + avgSalary + " zł");

        // 8. getHighestPaidEmployee - najlepiej opłacany pracownik
        System.out.println("\n=== Najlepiej opłacany pracownik ===");
        Optional<Employee> highestPaid = service.getHighestPaidEmployee();
        highestPaid.ifPresent(emp ->
            System.out.println(emp.getName() + " " + emp.getSurname() +
                " na stanowisku " + emp.getPosition().name() +
                " z pensją " + emp.getPosition().getBaseSalary() + " zł")
        );
    }
}