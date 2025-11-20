package com.github.jakubpakula1.lab.controller;

import com.github.jakubpakula1.lab.exception.EmployeeNotFoundException;
import com.github.jakubpakula1.lab.exception.FileStorageException;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmployeeDocument;
import com.github.jakubpakula1.lab.model.FileType;
import com.github.jakubpakula1.lab.service.EmployeeService;
import com.github.jakubpakula1.lab.service.FileStorageService;
import com.github.jakubpakula1.lab.service.ImportService;
import com.github.jakubpakula1.lab.model.ImportSummary;
import com.github.jakubpakula1.lab.model.ImportSummary.ErrorEntry;
import com.github.jakubpakula1.lab.service.ReportGeneratorService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final ImportService importService;
    private final ReportGeneratorService reportService;
    private final EmployeeService employeeService;

    public FileUploadController(FileStorageService fileStorageService, ImportService importService,ReportGeneratorService reportService, EmployeeService employeeService) {
        this.fileStorageService = fileStorageService;
        this.importService = importService;
        this.reportService = reportService;
        this.employeeService = employeeService;
    }

    @PostMapping("/import/csv")
    public ResponseEntity<ImportSummary> importCsv(@RequestParam("file") MultipartFile file) {
        try {
            String storedName = fileStorageService.storeFile(file, null);
            Path filePath = fileStorageService.loadFile(storedName);
            ImportSummary summary = importService.importFromCsv(String.valueOf(filePath));
            return ResponseEntity.ok(summary);
        } catch (FileStorageException ex) {
            ImportSummary err = new ImportSummary();
            err.setImportedEmployees(0);
            err.addError(-1, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        } catch (Exception ex) {
            ImportSummary err = new ImportSummary();
            err.setImportedEmployees(0);
            err.addError(-1, "Błąd serwera: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @PostMapping("/import/xml")
    public ResponseEntity<ImportSummary> importXml(@RequestParam("file") MultipartFile file) {
        try {
            String storedName = fileStorageService.storeFile(file, null);
            Path filePath = fileStorageService.loadFile(storedName);
            ImportSummary summary = importService.importFromXml(String.valueOf(filePath));
            return ResponseEntity.ok(summary);
        } catch (FileStorageException ex) {
            ImportSummary err = new ImportSummary();
            err.setImportedEmployees(0);
            err.addError(-1, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        } catch (Exception ex) {
            ImportSummary err = new ImportSummary();
            err.setImportedEmployees(0);
            err.addError(-1, "Błąd serwera: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<Resource> exportCsv(@RequestParam(value = "company", required = false) String company) {
        Resource csv = reportService.generateEmployeesCsv(Optional.ofNullable(company));
        String filename = company == null ? "employees.csv" : "employees_" + sanitizeFilename(company) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(value = "/reports/statistics/{companyName}", produces = "application/pdf")
    public ResponseEntity<Resource> exportCompanyStatisticsPdf(@PathVariable String companyName) {
        Resource pdf = reportService.generateCompanyStatisticsPdf(companyName);
        String filename = "statistics_" + sanitizeFilename(companyName) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

        @PostMapping("/documents/{email}")
        public ResponseEntity<EmployeeDocument> uploadDocument(
                @PathVariable String email,
                @RequestParam("file") MultipartFile file,
                @RequestParam("type") FileType type) {

            boolean exists = employeeService.getEmployees()
                    .stream()
                    .anyMatch(e -> email.equalsIgnoreCase(e.getEmail()));

            if (!exists) {
                throw new EmployeeNotFoundException("Pracownik nie istnieje: " + email);
            }

            EmployeeDocument doc = fileStorageService.storeEmployeeDocument(email, file, type);
            return ResponseEntity.status(201).body(doc);
        }

        @GetMapping("/documents/{email}")
        public ResponseEntity<List<EmployeeDocument>> listDocuments(@PathVariable String email) {
            List<EmployeeDocument> list = fileStorageService.listEmployeeDocuments(email);
            return ResponseEntity.ok(list);
        }

        @GetMapping("/documents/{email}/{documentId}")
        public ResponseEntity<Resource> downloadDocument(@PathVariable String email, @PathVariable String documentId) {
            EmployeeDocument doc = fileStorageService.findEmployeeDocument(email, documentId);
            Resource resource = fileStorageService.loadEmployeeDocumentAsResource(doc);

            String contentType = "application/octet-stream";
            try {
                Path path = Paths.get(doc.getFilePath());
                String probe = Files.probeContentType(path);
                if (probe != null) contentType = probe;
            } catch (Exception ignored) {}

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getOriginalFileName() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }

        @DeleteMapping("/documents/{email}/{documentId}")
        public ResponseEntity<Void> deleteDocument(@PathVariable String email, @PathVariable String documentId) {
            fileStorageService.deleteEmployeeDocument(email, documentId);
            return ResponseEntity.noContent().build();
        }
    private String sanitizeFilename(String s) {
        return s == null ? "report" : s.replaceAll("[^a-zA-Z0-9-_.]", "_");
    }
    @PostMapping("/photos/{email}")
    public ResponseEntity<String> uploadPhoto(@PathVariable String email, @RequestParam("file") MultipartFile file) {
        Employee emp = employeeService.getEmployeeByEmail(email);
        if (emp == null) {
            throw new EmployeeNotFoundException("Pracownik nie istnieje: " + email);
        }
        String filename = fileStorageService.storeEmployeePhoto(email, file);
        emp.setPhotoFileName(filename);
        return ResponseEntity.status(201).body(filename);
    }

    @GetMapping("/photos/{email}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String email) {
        Employee emp = employeeService.getEmployeeByEmail(email);
        if (emp == null) {
            throw new EmployeeNotFoundException("Pracownik nie istnieje: " + email);
        }
        String photo = emp.getPhotoFileName();
        if (photo == null || photo.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = fileStorageService.loadEmployeePhotoAsResource(photo);
        String contentType = "application/octet-stream";
        try {
            Path path = Paths.get(fileStorageService.loadEmployeePhotoAsResource(photo).getURI());
            String probe = Files.probeContentType(path);
            if (probe != null) contentType = probe;
        } catch (Exception ignored) {}
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photo + "\"")
                .contentType(mediaType)
                .body(resource);
    }

    @DeleteMapping("/photos/{email}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String email) {
        Employee emp = employeeService.getEmployeeByEmail(email);
        if (emp == null) {
            throw new EmployeeNotFoundException("Pracownik nie istnieje: " + email);
        }
        String photo = emp.getPhotoFileName();
        if (photo != null && !photo.isBlank()) {
            fileStorageService.deleteEmployeePhoto(photo);
            emp.setPhotoFileName(null);
        }
        return ResponseEntity.noContent().build();
    }
}