package com.github.jakubpakula1.lab;

import com.github.jakubpakula1.lab.model.CompanyStatistics;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.ImportSummary;
import com.github.jakubpakula1.lab.model.Position;
import com.github.jakubpakula1.lab.service.ApiService;
import com.github.jakubpakula1.lab.service.EmployeeService;
import com.github.jakubpakula1.lab.service.ImportService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        // Inicjalizacja serwisu
        EmployeeService service = new EmployeeService();

        ImportService importService = new ImportService(service);

        try{
            ImportSummary summ = importService.importFromCsv("/Users/kuba/Desktop/Studia UG/semestr_5/aplikacje_przemyslowe/lab02/TechCorp2/src/main/resources/pracownicy.csv");
            System.out.println(summ.getErrors());
            System.out.println(summ.getImportedEmployees());
            Map<String, CompanyStatistics> stats = service.getCompanyStatistics();
            for (Map.Entry<String, CompanyStatistics> entry : stats.entrySet()) {
                System.out.println("Firma: " + entry.getKey());
                System.out.println(entry.getValue());
                System.out.println();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        ApiService apiService = new ApiService();
//        try {
//            Employee[] result = apiService.fetchFromAPI();
//            System.out.println("Pracownicy z API:");
//            for (Employee emp : result) {
//                System.out.println(emp);
//            }
//            System.out.println("Łącznie pobrano " + result.length + " pracowników.");
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
}