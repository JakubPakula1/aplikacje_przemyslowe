package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.model.Employee;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 Uwaga: ReportService zakłada istnienie bean'a EmployeeService z metodami:
 - List<Employee> findAll()
 - List<Employee> findByCompany(String company)
 Dostosuj nazwę/implementację do istniejącego serwisu/repozytorium w projekcie.
*/
@Service
public class ReportGeneratorService {

    private final EmployeeService employeeService;

    public ReportGeneratorService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public Resource generateEmployeesCsv(Optional<String> companyOpt) {
        List<Employee> employees = companyOpt
                .map(employeeService::getCompanyEmployees)
                .orElseGet(employeeService::getEmployees);

        String header = "name,surname,company,email,position,salary\n";
        String body = employees.stream()
                .map(e -> String.format("%s,%s,%s,%s,%s,%d",
                        escapeCsv(e.getName()),
                        escapeCsv(e.getSurname()),
                        escapeCsv(e.getCompany()),
                        escapeCsv(e.getEmail()),
                        escapeCsv(e.getPosition() != null ? e.getPosition().name() : ""),
                        e.getSalary()))
                .collect(Collectors.joining("\n"));

        byte[] bytes = (header + body).getBytes(StandardCharsets.UTF_8);
        return new ByteArrayResource(bytes);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    public Resource generateCompanyStatisticsPdf(String companyName) {
        List<Employee> employees = employeeService.getCompanyEmployees(companyName);
        int total = employees.size();

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(50, 700);
                cs.showText("Statistics for company: " + companyName);
                cs.endText();

                cs.beginText();
                cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, 670);
                cs.showText("Total employees: " + total);
                cs.endText();

                // prosta tabela / lista (można rozbudować)
                int y = 640;
                for (Employee e : employees) {
                    cs.beginText();
                    cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 10);
                    cs.newLineAtOffset(50, y);
                    String line = String.format("%s %s (%s)", e.getName(), e.getSurname(), e.getEmail());
                    cs.showText(truncate(line, 90));
                    cs.endText();
                    y -= 14;
                    if (y < 50) break;
                }
            }

            doc.save(baos);
            return new ByteArrayResource(baos.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException("Błąd generowania PDF", ex);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}