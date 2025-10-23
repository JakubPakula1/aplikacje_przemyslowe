package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.model.ImportSummary;
import com.github.jakubpakula1.lab.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

public class ImportServiceTest {

    private EmployeeService employeeService;
    private ImportService importService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
        importService = new ImportService(employeeService);
    }

    // importFromCsv - valid data tests
    @Test
    void importFromCsv_singleValidRow_importedSuccessfully(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary)
                .isNotNull();
        assertThat(summary.getImportedEmployees())
                .isEqualTo(1);
        assertThat(summary.getErrors())
                .isEmpty();
        assertThat(employeeService.getCompanyEmployees("X"))
                .hasSize(1)
                .extracting("email")
                .containsExactly("jan@x.com");
    }

    @Test
    void importFromCsv_multipleValidRows_importedSuccessfully(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;8000\n" +
                "Anna;Nowak;anna@y.com;Y;MANAGER;12000\n" +
                "Piotr;Lewandowski;piotr@z.com;Z;WICEPREZES;9500\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees())
                .isEqualTo(3);
        assertThat(summary.getErrors())
                .isEmpty();
        assertThat(employeeService.getCompanyEmployees("X"))
                .hasSize(1);
        assertThat(employeeService.getCompanyEmployees("Y"))
                .hasSize(1);
        assertThat(employeeService.getCompanyEmployees("Z"))
                .hasSize(1);
    }

    @Test
    void importFromCsv_validRow_parsesFirstName(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(employeeService.getCompanyEmployees("X"))
                .hasSize(1)
                .extracting("name")
                .containsExactly("Jan");
    }

    @Test
    void importFromCsv_validRow_parsesLastName(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(employeeService.getCompanyEmployees("X"))
                .hasSize(1)
                .extracting("surname")
                .containsExactly("Kowalski");
    }

    @Test
    void importFromCsv_validRow_parsesCompanyName(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;Acme Corp;PROGRAMISTA;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(employeeService.getCompanyEmployees("Acme Corp"))
                .hasSize(1);
    }

    @Test
    void importFromCsv_validRow_parsesPosition(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Anna;Nowak;anna@y.com;Y;MANAGER;12000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(employeeService.getCompanyEmployees("Y"))
                .hasSize(1)
                .extracting("position")
                .containsExactly(Position.MANAGER);
    }

    @Test
    void importFromCsv_validRow_parsesSalary(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;15000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(employeeService.getCompanyEmployees("X"))
                .hasSize(1)
                .extracting("salary")
                .containsExactly(15000);
    }

    @Test
    void importFromCsv_validRow_trimsWhitespace(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "  Jan  ;  Kowalski  ;  jan@x.com  ;  X  ;  PROGRAMISTA  ;  8000  \n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees())
                .isEqualTo(1);
        assertThat(employeeService.getCompanyEmployees("X"))
                .hasSize(1)
                .extracting("name")
                .containsExactly("Jan");
    }

    @Test
    void importFromCsv_emptyFile_returnsZeroImported(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees())
                .isEqualTo(0);
        assertThat(summary.getErrors())
                .isEmpty();
    }

    // importFromCsv - invalid position tests
    @Test
    void importFromCsv_invalidPosition_addsError(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;INVALID_POSITION;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees())
                .isEqualTo(0);
        assertThat(summary.getErrors())
                .hasSize(1)
                .extracting("errorMessage")
                .allMatch(msg -> msg.toString().contains("Invalid position"));
    }

    @Test
    void importFromCsv_invalidPosition_specifiesLineNumber(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;INVALID_POS;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors())
                .hasSize(1)
                .extracting("lineNumber")
                .containsExactly(2);
    }

    @Test
    void importFromCsv_invalidPosition_caseInsensitive(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;programista;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees())
                .isEqualTo(1);
    }

    @Test
    void importFromCsv_multipleInvalidPositions_addsMultipleErrors(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;INVALID1;8000\n" +
                "Anna;Nowak;anna@y.com;Y;INVALID2;12000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors())
                .hasSize(2)
                .extracting("lineNumber")
                .containsExactly(2, 3);
    }

    // importFromCsv - invalid salary tests
    @Test
    void importFromCsv_invalidSalary_addsError(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;not_a_number\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees())
                .isEqualTo(0);
        assertThat(summary.getErrors())
                .hasSize(1)
                .extracting("errorMessage")
                .allMatch(msg -> msg.toString().contains("Invalid salary value"));
    }

    @Test
    void importFromCsv_invalidSalary_specifiesLineNumber(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;abc\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors())
                .hasSize(1)
                .extracting("lineNumber")
                .containsExactly(2);
    }

    @Test
    void importFromCsv_negativeSalary_importedSuccessfully(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;-1000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees())
                .isEqualTo(1);
        assertThat(employeeService.getCompanyEmployees("X"))
                .hasSize(1)
                .extracting("salary")
                .containsExactly(-1000);
    }

    @Test
    void importFromCsv_multipleInvalidSalaries_addsMultipleErrors(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;invalid1\n" +
                "Anna;Nowak;anna@y.com;Y;MANAGER;invalid2\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors())
                .hasSize(2)
                .extracting("lineNumber")
                .containsExactly(2, 3);
    }

    // importFromCsv - invalid columns tests
    @Test
    void importFromCsv_tooFewColumns_addsError(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees())
                .isEqualTo(0);
        assertThat(summary.getErrors())
                .hasSize(1)
                .extracting("errorMessage")
                .allMatch(msg -> msg.toString().contains("Invalid number of columns"));
    }

    @Test
    void importFromCsv_tooFewColumns_specifiesLineNumber(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors())
                .hasSize(1)
                .extracting("lineNumber")
                .containsExactly(2);
    }

    @Test
    void importFromCsv_multipleRowsWithInvalidColumns_addsMultipleErrors(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski\n" +
                "Anna;Nowak;anna@y.com\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors())
                .hasSize(2)
                .extracting("lineNumber")
                .containsExactly(2, 3);
    }

    // importFromCsv - mixed valid and invalid rows tests
    @Test
    void importFromCsv_validAndInvalidRows_summaryCorrect(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;8000\n" +
                "Anna;Nowak;anna@y.com;Y;INVALID_POS;5000\n" +
                "Piotr;Z;piotr@z.com;Z;MANAGER;not_a_number\n" +
                "Bob;Smith;bob@a.com;A;WICEPREZES;10000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees())
                .isEqualTo(2);
        assertThat(summary.getErrors())
                .hasSize(2);
    }

    @Test
    void importFromCsv_validAndInvalidRows_validRowsImported(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;8000\n" +
                "Anna;Nowak;anna@y.com;Y;INVALID_POS;5000\n" +
                "Piotr;Lewandowski;piotr@z.com;Z;MANAGER;9500\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(employeeService.getCompanyEmployees("X"))
                .hasSize(1)
                .extracting("email")
                .containsExactly("jan@x.com");
        assertThat(employeeService.getCompanyEmployees("Z"))
                .hasSize(1)
                .extracting("email")
                .containsExactly("piotr@z.com");
    }

    // importFromCsv - file not found tests
    @Test
    void importFromCsv_fileNotFound_throwsIOException() {
        assertThatThrownBy(() -> importService.importFromCsv("/nonexistent/path/file.csv"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Error reading CSV file");
    }

    @Test
    void importFromCsv_invalidPath_throwsIOException() {
        assertThatThrownBy(() -> importService.importFromCsv(""))
                .isInstanceOf(IOException.class);
    }

    // importFromCsv - duplicate email tests
    @Test
    void importFromCsv_duplicateEmail_addsError(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@x.com;X;PROGRAMISTA;8000\n" +
                "Anna;Nowak;jan@x.com;Y;MANAGER;12000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees())
                .isEqualTo(1);
        assertThat(summary.getErrors())
                .hasSize(1);
    }

    // Constructor tests
    @Test
    void constructor_createsInstance() {
        ImportService service = new ImportService(employeeService);

        assertThat(service).isNotNull();
    }

    @Test
    void constructor_withEmployeeService_createsInstance() {
        EmployeeService service = new EmployeeService();
        ImportService importService = new ImportService(service);

        assertThat(importService).isNotNull();
    }
}