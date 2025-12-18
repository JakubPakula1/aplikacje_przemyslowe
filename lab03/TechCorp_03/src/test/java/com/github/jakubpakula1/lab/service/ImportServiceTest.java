package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.model.ImportSummary;
import com.github.jakubpakula1.lab.model.Position;
import com.github.jakubpakula1.lab.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImportServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeService employeeService;

    private ImportService importService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository);
        importService = new ImportService(employeeService);
    }



    @Test
    void importFromCsv_singleValidRow_importedSuccessfully(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary).isNotNull();
        assertThat(summary.getImportedEmployees()).isEqualTo(1);
        assertThat(summary.getErrors()).isEmpty();
        verify(employeeRepository).deleteAll();
        verify(employeeRepository).save(any());
    }

    @Test
    void importFromCsv_multipleValidRows_importedSuccessfully(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA;8000\n" +
                "Anna;Nowak;anna@techcorp.com;Y;MANAGER;12000\n" +
                "Piotr;Lewandowski;piotr@techcorp.com;Z;WICEPREZES;9500\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(3);
        assertThat(summary.getErrors()).isEmpty();
        verify(employeeRepository, times(3)).save(any());
    }

    @Test
    void importFromCsv_validRow_parsesFirstName(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(1);
        verify(employeeRepository).save(argThat(emp -> emp.getName().equals("Jan")));
    }

    @Test
    void importFromCsv_validRow_parsesLastName(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(1);
        verify(employeeRepository).save(argThat(emp -> emp.getSurname().equals("Kowalski")));
    }

    @Test
    void importFromCsv_validRow_parsesCompanyName(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;Acme Corp;PROGRAMISTA;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(1);
        verify(employeeRepository).save(argThat(emp -> emp.getCompany().equals("Acme Corp")));
    }

    @Test
    void importFromCsv_validRow_parsesPosition(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Anna;Nowak;anna@techcorp.com;Y;MANAGER;12000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(1);
        verify(employeeRepository).save(argThat(emp -> emp.getPosition() == Position.MANAGER));
    }

    @Test
    void importFromCsv_validRow_parsesSalary(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA;15000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(1);
        verify(employeeRepository).save(argThat(emp -> emp.getSalary().compareTo(BigDecimal.valueOf(15000)) == 0));
    }

    @Test
    void importFromCsv_validRow_trimsWhitespace(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "  Jan  ;  Kowalski  ;  jan@techcorp.com  ;  X  ;  PROGRAMISTA  ;  8000  \n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(1);
        verify(employeeRepository).save(argThat(emp -> emp.getName().equals("Jan")));
    }

    @Test
    void importFromCsv_emptyFile_returnsZeroImported(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(0);
        assertThat(summary.getErrors()).isEmpty();
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void importFromCsv_invalidPosition_addsError(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;INVALID_POSITION;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(0);
        assertThat(summary.getErrors()).hasSize(1)
                .extracting("errorMessage")
                .allMatch(msg -> msg.toString().contains("Invalid position"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void importFromCsv_invalidPosition_specifiesLineNumber(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;INVALID_POS;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors()).hasSize(1)
                .extracting("lineNumber")
                .containsExactly(2);
    }

    @Test
    void importFromCsv_invalidPosition_caseInsensitive(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;programista;8000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(1);
        verify(employeeRepository).save(any());
    }

    @Test
    void importFromCsv_multipleInvalidPositions_addsMultipleErrors(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;INVALID1;8000\n" +
                "Anna;Nowak;anna@techcorp.com;Y;INVALID2;12000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors()).hasSize(2)
                .extracting("lineNumber")
                .containsExactly(2, 3);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void importFromCsv_invalidSalary_addsError(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA;not_a_number\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(0);
        assertThat(summary.getErrors()).hasSize(1)
                .extracting("errorMessage")
                .allMatch(msg -> msg.toString().contains("Invalid salary value"));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void importFromCsv_invalidSalary_specifiesLineNumber(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA;abc\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors()).hasSize(1)
                .extracting("lineNumber")
                .containsExactly(2);
    }

    @Test
    void importFromCsv_negativeSalary_importedSuccessfully(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA;-1000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(1);
        verify(employeeRepository).save(argThat(emp -> emp.getSalary().compareTo(BigDecimal.valueOf(-1000)) == 0));
    }

    @Test
    void importFromCsv_multipleInvalidSalaries_addsMultipleErrors(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA;invalid1\n" +
                "Anna;Nowak;anna@techcorp.com;Y;MANAGER;invalid2\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors()).hasSize(2)
                .extracting("lineNumber")
                .containsExactly(2, 3);
    }

    @Test
    void importFromCsv_tooFewColumns_addsError(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(0);
        assertThat(summary.getErrors()).hasSize(1)
                .extracting("errorMessage")
                .allMatch(msg -> msg.toString().contains("Invalid number of columns"));
    }

    @Test
    void importFromCsv_tooFewColumns_specifiesLineNumber(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors()).hasSize(1)
                .extracting("lineNumber")
                .containsExactly(2);
    }

    @Test
    void importFromCsv_multipleRowsWithInvalidColumns_addsMultipleErrors(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski\n" +
                "Anna;Nowak;anna@techcorp.com\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getErrors()).hasSize(2)
                .extracting("lineNumber")
                .containsExactly(2, 3);
    }

    @Test
    void importFromCsv_validAndInvalidRows_summaryCorrect(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA;8000\n" +
                "Anna;Nowak;anna@techcorp.com;Y;INVALID_POS;5000\n" +
                "Piotr;Lewandowski;piotr@techcorp.com;Z;MANAGER;9500\n" +
                "Bob;Smith;bob@techcorp.com;A;WICEPREZES;10000\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(3);
        assertThat(summary.getErrors()).hasSize(1);
        verify(employeeRepository, times(3)).save(any());
    }

    @Test
    void importFromCsv_validAndInvalidRows_validRowsImported(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        String content = "firstName;lastName;email;company;position;salary\n" +
                "Jan;Kowalski;jan@techcorp.com;X;PROGRAMISTA;8000\n" +
                "Anna;Nowak;anna@techcorp.com;Y;INVALID_POS;5000\n" +
                "Piotr;Lewandowski;piotr@techcorp.com;Z;MANAGER;9500\n";
        Files.writeString(csv, content);

        ImportSummary summary = importService.importFromCsv(csv.toString());

        assertThat(summary.getImportedEmployees()).isEqualTo(2);
        verify(employeeRepository, times(2)).save(any());
    }

    @Test
    void importFromCsv_fileNotFound_throwsIOException() {
        assertThatThrownBy(() -> importService.importFromCsv("/nonexistent/path/file.csv"))
                .isInstanceOf(IOException.class);
    }

    @Test
    void importFromCsv_invalidPath_throwsIOException() {
        assertThatThrownBy(() -> importService.importFromCsv(""))
                .isInstanceOf(IOException.class);
    }

    @Test
    void constructor_createsInstance() {
        ImportService service = new ImportService(employeeService);

        assertThat(service).isNotNull();
    }

    @Test
    void constructor_withEmployeeService_createsInstance() {
        EmployeeService service = new EmployeeService(employeeRepository);
        ImportService importService = new ImportService(service);

        assertThat(importService).isNotNull();
    }
}