package com.github.jakubpakula1.lab.service;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.ImportSummary;
import com.github.jakubpakula1.lab.model.Position;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.*;
import java.util.List;

public class ImportService {
    private static EmployeeService employeeService;

    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public ImportSummary importFromCsv(String filePath) throws IOException {
        ImportSummary summary = new ImportSummary();
        int importedEmployees = 0;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Pomijamy pierwszy wiersz (nagłówki)
            reader.readLine();
            lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] row = line.split(";");
                    if (row.length < 6) {
                        summary.addError(lineNumber, "Invalid number of columns in row");
                        continue;
                    }

                    String firstName = row[0].trim();
                    String lastName = row[1].trim();
                    String email = row[2].trim();
                    String company = row[3].trim();
                    Position position;
                    try {
                        position = Position.valueOf(row[4].trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        summary.addError(lineNumber, "Invalid position: " + row[4].trim());
                        continue;
                    }

                    int salary;
                    try {
                        salary = Integer.parseInt(row[5].trim());
                    } catch (NumberFormatException e) {
                        summary.addError(lineNumber, "Invalid salary value: " + row[5].trim());
                        continue;
                    }

                    employeeService.AddEmployee(new Employee(firstName, lastName, company, email, position, salary));
                    importedEmployees++;
                } catch (Exception e) {
                    summary.addError(lineNumber, "Error while processing row: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IOException("Error reading CSV file: " + e.getMessage(), e);
        }

        summary.setImportedEmployees(importedEmployees);
        return summary;
    }
}
