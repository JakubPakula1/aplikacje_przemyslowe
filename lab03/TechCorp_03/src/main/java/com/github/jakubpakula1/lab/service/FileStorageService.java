package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.exception.FileNotFoundException;
import com.github.jakubpakula1.lab.exception.FileStorageException;
import com.github.jakubpakula1.lab.exception.InvalidFileException;
import com.github.jakubpakula1.lab.model.EmployeeDocument;
import com.github.jakubpakula1.lab.model.FileType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class FileStorageService {
    private final Path uploadPath;
    private final Path reportPath;
    private final List<String> allowedExtensions;
    private final long maxFileSizeBytes;
    private final Map<String, List<EmployeeDocument>> store = new ConcurrentHashMap<>();

    public FileStorageService(
            @Value("${app.upload.directory}") String uploadDir,
            @Value("${app.reports.directory}") String reportDir,
            @Value("${app.upload.allowed-extensions}") String extensions,
            @Value("${spring.servlet.multipart.max-file-size}") String maxFileSizeStr) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.reportPath = Paths.get(reportDir).toAbsolutePath().normalize();
        this.allowedExtensions = Arrays.asList(extensions.split(","));
        this.maxFileSizeBytes = Long.parseLong(maxFileSizeStr);

        try {
            Files.createDirectories(this.uploadPath);
            Files.createDirectories(this.reportPath);
        } catch (IOException ex) {
            throw new RuntimeException("Nie można utworzyć katalogu dla plików", ex);
        }
    }

    public Path loadFile(String filename) {
        return uploadPath.resolve(filename).normalize();
    }
    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = loadFile(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Plik nie został znaleziony: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Błąd podczas tworzenia zasobu pliku: " + filename, ex);
        }
    }
    public void deleteFile(String filename) {
        try {
            Path filePath = uploadPath.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Błąd podczas usuwania pliku", ex);
        }
    }

    public String storeFile(MultipartFile file, String bookTitle) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new FileStorageException("Nazwa pliku jest wymagana");
        }

        String extension = getFileExtension(originalFilename);
        String filename = generateUniqueFilename(bookTitle != null ? bookTitle : "file", extension);
        Path targetLocation = this.uploadPath.resolve(filename);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException ex) {
            throw new FileStorageException("Błąd podczas zapisu pliku", ex);
        }
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Plik jest pusty");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new InvalidFileException("Plik przekracza dozwolony rozmiar: " + maxFileSizeBytes + " bytes");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidFileException("Nazwa pliku jest wymagana");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new InvalidFileException("Niedozwolone rozszerzenie pliku. Dozwolone: " + allowedExtensions);
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentTypeAllowedForExtension(extension, contentType)) {
            throw new InvalidFileException("Content-Type niezgodny z rozszerzeniem pliku: " + contentType);
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    private String generateUniqueFilename(String bookTitle, String extension) {
        // Czyszczenie tytułu z niedozwolonych znaków
        String cleanTitle = bookTitle.replaceAll("[^a-zA-Z0-9]", "_");
        // Dodanie UUID dla unikalności
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return cleanTitle + "_" + uniqueId + "." + extension;
    }
    // ----- metody dla dokumentów pracowniczych -----

    public EmployeeDocument storeEmployeeDocument(String email, MultipartFile file, FileType fileType) {
        validateFile(file);

        try {
            Path targetDir = this.uploadPath.resolve("documents").resolve(sanitizeEmail(email));
            Files.createDirectories(targetDir);

            String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
            String extension = getFileExtension(originalFilename);
            String baseName = removeExtension(originalFilename);
            String storedFilename = sanitizeFilename(baseName) + "_" + UUID.randomUUID().toString().substring(0, 8);
            if (!extension.isEmpty()) storedFilename += "." + extension;

            Path target = targetDir.resolve(storedFilename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            EmployeeDocument doc = new EmployeeDocument();
            doc.setId(UUID.randomUUID().toString());
            doc.setEmployeeEmail(email);
            doc.setFileName(storedFilename);
            doc.setOriginalFileName(originalFilename);
            doc.setFileType(fileType);
            doc.setUploadDate(LocalDateTime.now());
            doc.setFilePath(target.toAbsolutePath().toString());

            store.computeIfAbsent(email.toLowerCase(), k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(doc);

            return doc;
        } catch (IOException ex) {
            throw new FileStorageException("Błąd zapisu dokumentu", ex);
        }
    }

    public List<EmployeeDocument> listEmployeeDocuments(String email) {
        return Collections.unmodifiableList(store.getOrDefault(email.toLowerCase(), Collections.emptyList()));
    }

    public EmployeeDocument findEmployeeDocument(String email, String documentId) {
        return store.getOrDefault(email.toLowerCase(), Collections.emptyList())
                .stream()
                .filter(d -> Objects.equals(d.getId(), documentId))
                .findFirst()
                .orElseThrow(() -> new FileNotFoundException("Dokument nie został znaleziony: " + documentId));
    }

    public Resource loadEmployeeDocumentAsResource(EmployeeDocument doc) {
        try {
            Path file = Paths.get(doc.getFilePath());
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Plik nie został znaleziony: " + doc.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Błąd podczas tworzenia zasobu pliku: " + doc.getFileName(), ex);
        }
    }

    public void deleteEmployeeDocument(String email, String documentId) {
        List<EmployeeDocument> list = store.getOrDefault(email.toLowerCase(), Collections.emptyList());
        Optional<EmployeeDocument> found = list.stream().filter(d -> Objects.equals(d.getId(), documentId)).findFirst();
        if (found.isEmpty()) {
            throw new FileNotFoundException("Dokument nie został znaleziony: " + documentId);
        }
        EmployeeDocument doc = found.get();
        try {
            Files.deleteIfExists(Paths.get(doc.getFilePath()));
        } catch (IOException ex) {
            throw new FileStorageException("Błąd podczas usuwania pliku", ex);
        }
        list.remove(doc);
    }

    private String sanitizeEmail(String e) {
        return e == null ? "unknown" : e.replaceAll("[^a-zA-Z0-9@._-]", "_");
    }

    private String sanitizeFilename(String s) {
        return s == null ? "file" : s.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String removeExtension(String filename) {
        int lastDot = Optional.ofNullable(filename).orElse("").lastIndexOf('.');
        if (lastDot == -1) return filename;
        return filename.substring(0, lastDot);
    }
    // ----- metody dla zdjęć pracowników -----

    public String storeEmployeePhoto(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Plik jest pusty");
        }

        long maxPhotoSize = 2L * 1024 * 1024; // 2 MB
        if (file.getSize() > maxPhotoSize) {
            throw new FileStorageException("Plik zdjęcia przekracza 2MB");
        }

        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String extension = getFileExtension(original).toLowerCase();
        Set<String> allowed = Set.of("jpg", "jpeg", "png");
        if (!allowed.contains(extension)) {
            throw new FileStorageException("Niedozwolone rozszerzenie zdjęcia. Dozwolone: jpg, jpeg, png");
        }

        // prosty check nagłówka pliku
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            boolean ok = false;
            if (read >= 2) {
                // JPEG: FF D8
                if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8) ok = true;
            }
            if (!ok && read >= 8) {
                // PNG signature
                byte[] pngSig = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
                ok = true;
                for (int i = 0; i < 8; i++) {
                    if (header[i] != pngSig[i]) { ok = false; break; }
                }
            }
            if (!ok) {
                throw new FileStorageException("Plik nie wygląda na obraz JPG/PNG");
            }
        } catch (IOException e) {
            throw new FileStorageException("Błąd podczas odczytu pliku", e);
        }

        try {
            Path photosDir = this.uploadPath.resolve("photos");
            Files.createDirectories(photosDir);

            String extNormalized = extension.equals("jpeg") ? "jpg" : extension;
            String filename = sanitizeEmail(email) + "." + extNormalized;
            Path target = photosDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException ex) {
            throw new FileStorageException("Błąd podczas zapisu zdjęcia", ex);
        }
    }

    public Resource loadEmployeePhotoAsResource(String filename) {
        try {
            Path filePath = this.uploadPath.resolve("photos").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Zdjęcie nie zostało znalezione: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Błąd podczas tworzenia zasobu zdjęcia: " + filename, ex);
        }
    }

    public void deleteEmployeePhoto(String filename) {
        try {
            Path filePath = this.uploadPath.resolve("photos").resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Błąd podczas usuwania zdjęcia", ex);
        }
    }

    private boolean contentTypeAllowedForExtension(String ext, String contentType) {
        if (contentType == null) return true;
        return switch (ext) {
            case "jpg", "jpeg" -> contentType.startsWith("image/");
            case "png" -> contentType.equals("image/png") || contentType.startsWith("image/");
            case "pdf" -> contentType.equals("application/pdf");
            case "csv" -> contentType.contains("csv") || contentType.startsWith("text/");
            case "xml" -> contentType.contains("xml") || contentType.equals("text/xml");
            case "txt" -> contentType.startsWith("text/");
            default ->
                // dla innych rozszerzeń nie wymuszamy ścisłej kontroli
                    true;
        };

}}

