package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.ImportSummary;
import com.github.jakubpakula1.lab.model.Position;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Service
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

    /**
     * Importuje pracowników z pliku XML.
     * Oczekiwana struktura XML:
     * <employees>
     *   <employee>
     *     <name>John</name>
     *     <surname>Doe</surname>
     *     <email>john@example.com</email>
     *     <company>TechCorp</company>
     *     <position>PROGRAMISTA</position>
     *     <salary>5000</salary>
     *     <status>ACTIVE</status>
     *   </employee>
     * </employees>
     */
    public ImportSummary importFromXml(String filePath) throws IOException {
        ImportSummary summary = new ImportSummary();
        int importedEmployees = 0;
        int elementNumber = 0;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(filePath));

            NodeList employees = doc.getElementsByTagName("employee");

            for (int i = 0; i < employees.getLength(); i++) {
                elementNumber = i + 1;
                try {
                    Element empElement = (Element) employees.item(i);

                    String name = getElementValue(empElement, "name");
                    String surname = getElementValue(empElement, "surname");
                    String email = getElementValue(empElement, "email");
                    String company = getElementValue(empElement, "company");
                    String positionStr = getElementValue(empElement, "position");
                    String salaryStr = getElementValue(empElement, "salary");

                    // Walidacja pól wymaganych
                    if (name == null || name.trim().isEmpty()) {
                        summary.addError(elementNumber, "Pole 'name' jest wymagane");
                        continue;
                    }
                    if (surname == null || surname.trim().isEmpty()) {
                        summary.addError(elementNumber, "Pole 'surname' jest wymagane");
                        continue;
                    }
                    if (email == null || email.trim().isEmpty()) {
                        summary.addError(elementNumber, "Pole 'email' jest wymagane");
                        continue;
                    }
                    if (company == null || company.trim().isEmpty()) {
                        summary.addError(elementNumber, "Pole 'company' jest wymagane");
                        continue;
                    }
                    if (positionStr == null || positionStr.trim().isEmpty()) {
                        summary.addError(elementNumber, "Pole 'position' jest wymagane");
                        continue;
                    }
                    if (salaryStr == null || salaryStr.trim().isEmpty()) {
                        summary.addError(elementNumber, "Pole 'salary' jest wymagane");
                        continue;
                    }

                    // Parsowanie Position
                    Position position;
                    try {
                        position = Position.valueOf(positionStr.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        summary.addError(elementNumber, "Nieprawidłowa pozycja: " + positionStr.trim());
                        continue;
                    }

                    // Parsowanie salary
                    int salary;
                    try {
                        salary = Integer.parseInt(salaryStr.trim());
                        if (salary < 0) {
                            summary.addError(elementNumber, "Wynagrodzenie nie może być ujemne: " + salary);
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        summary.addError(elementNumber, "Nieprawidłowa wartość wynagrodzenia: " + salaryStr.trim());
                        continue;
                    }

                    // Opcjonalny status zatrudnienia
                    String statusStr = getElementValue(empElement, "status");
                    EmploymentStatus status = EmploymentStatus.ACTIVE;
                    if (statusStr != null && !statusStr.trim().isEmpty()) {
                        try {
                            status = EmploymentStatus.valueOf(statusStr.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            summary.addError(elementNumber, "Nieprawidłowy status: " + statusStr.trim());
                            continue;
                        }
                    }

                    // Tworzenie i dodawanie pracownika
                    Employee employee = new Employee(name.trim(), surname.trim(), company.trim(), email.trim(), position, salary);
                    employee.setStatus(status);
                    employeeService.AddEmployee(employee);
                    importedEmployees++;

                } catch (Exception e) {
                    summary.addError(elementNumber, "Błąd przy przetwarzaniu elementu: " + e.getMessage());
                }
            }

        } catch (ParserConfigurationException e) {
            throw new IOException("Błąd konfiguracji parsera XML: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new IOException("Błąd parsowania XML: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("Błąd odczytywania pliku XML: " + e.getMessage(), e);
        }

        summary.setImportedEmployees(importedEmployees);
        return summary;
    }

    /**
     * Pomocnicza metoda do pobierania wartości elementu XML.
     */
    private String getElementValue(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }
}
