package com.github.jakubpakula1.lab;

import com.github.jakubpakula1.lab.model.CompanyStatistics;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.service.ApiService;
import com.github.jakubpakula1.lab.service.EmployeeService;
import com.github.jakubpakula1.lab.service.ImportService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@ImportResource("classpath:employees-beans.xml")
public class EmployeeManagementApplication implements CommandLineRunner {

    private final ImportService importService;
    private final ApiService apiService;
    private final EmployeeService employeeService;
    private final List<Employee> xmlEmployees;
    private final String csvFilePath;

    public EmployeeManagementApplication(
            ImportService importService,
            ApiService apiService,
            EmployeeService employeeService,
            @Qualifier("xmlEmployees") List<Employee> xmlEmployees,
            @Value("${app.import.csv-file}") String csvFilePath) {
        this.importService = importService;
        this.apiService = apiService;
        this.employeeService = employeeService;
        this.xmlEmployees = xmlEmployees;
        this.csvFilePath = csvFilePath;
    }

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== EMPLOYEE MANAGEMENT SYSTEM ===\n");

        // Import z CSV
        System.out.println(">>> Import pracowników z pliku CSV: " + csvFilePath);
        importService.importFromCsv(csvFilePath);
        System.out.println();

        // Dodanie pracowników z XML
        System.out.println(">>> Dodawanie pracowników z konfiguracji XML:");
        for (Employee emp : xmlEmployees) {
            employeeService.AddEmployee(emp);
        }
        System.out.println();

        // Pobranie danych z API
        System.out.println(">>> Pobieranie pracowników z REST API:");
        Employee[] apiEmployees = apiService.fetchFromAPI();
        System.out.println("Pobrano " + apiEmployees.length + " pracowników z API");
        for (Employee emp : apiEmployees) {
            employeeService.AddEmployee(emp);
        }
        System.out.println();

        // Wyświetlenie statystyk dla firmy
        System.out.println(">>> Statystyki wszystkich firm:");
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
        stats.forEach((company, companyStats) -> {
            System.out.println("  [" + company + "]");
            System.out.println("    - Liczba pracowników: " + companyStats.getNumberOfEmployees());
            System.out.println("    - Średnia pensja: " + String.format("%.2f", companyStats.getAverageSalary()));
            System.out.println("    - Najlepiej zarabiający: " + companyStats.getBestEarningName());
        });
        System.out.println();
        System.out.println();

        // Walidacja wynagrodzeń
        System.out.println(">>> Walidacja spójności wynagrodzeń:");
        Employee[] invalidEmployees = employeeService.validateSalaryConsistency();
        if (invalidEmployees.length > 0) {
            System.out.println("Znaleziono " + invalidEmployees.length + " pracowników poniżej stawki bazowej:");
            for (Employee emp : invalidEmployees) {
                System.out.println("  - " + emp.getName() + " " + emp.getSurname() +
                                 " (aktualna: " + emp.getSalary() + ", bazowa: " + emp.getPosition().getBaseSalary() + " Firma: "+emp.getCompany() + ")");
            }
        } else {
            System.out.println("Wszystkie wynagrodzenia są zgodne ze stawkami bazowymi");
        }

        System.out.println("\n=== ZAKOŃCZONO PRZETWARZANIE ===");
    }}