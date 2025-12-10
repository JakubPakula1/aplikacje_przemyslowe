package com.github.jakubpakula1.lab.controller;

import com.github.jakubpakula1.lab.dto.EmployeeDTO;
import com.github.jakubpakula1.lab.dto.EmployeeListProjection;
import com.github.jakubpakula1.lab.dto.StatusUpdateDTO;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import com.github.jakubpakula1.lab.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService){
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(
            @RequestParam(required = false) String company) {

        List<Employee> employees;
        if (company == null || company.isBlank()) {
            employees = this.employeeService.getAllEmployees();
        } else {
            employees = this.employeeService.getCompanyEmployees(company.trim());
        }

        List<EmployeeDTO> dtos = employees.stream().map(this::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDTO>> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false) Integer maxSalary,
            @RequestParam(required = false) Long departmentId) {

        List<Employee> employees = employeeService.searchEmployees(name, surname, company, position, minSalary, maxSalary, departmentId);
        List<EmployeeDTO> dtos = employees.stream().map(this::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{email}")
    public ResponseEntity<EmployeeDTO> getEmployeeByEmail(@PathVariable String email) {
        Employee employee = this.employeeService.getEmployeeByEmail(email);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        EmployeeDTO employeeDTO = mapToDto(employee);
        return ResponseEntity.ok(employeeDTO);
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        if (employeeDTO == null || employeeDTO.getEmail() == null || employeeDTO.getEmail().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Employee toCreate = new Employee(
                employeeDTO.getName(),
                employeeDTO.getSurname(),
                employeeDTO.getCompany(),
                employeeDTO.getEmail().trim(),
                employeeDTO.getPosition(),
                employeeDTO.getSalary()
        );

        if (employeeDTO.getStatus() != null) {
            toCreate.setStatus(employeeDTO.getStatus());
        }

        Employee created = this.employeeService.addEmployee(toCreate);

        java.net.URI location = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{email}")
                .buildAndExpand(created.getEmail())
                .toUri();

        return ResponseEntity.created(location).body(mapToDto(created));
    }

    @PutMapping("/{email}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable String email, @Valid @RequestBody EmployeeDTO employeeDTO) {
        Employee existing = this.employeeService.getEmployeeByEmail(email);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        String name = employeeDTO.getName() != null ? employeeDTO.getName() : existing.getName();
        String surname = employeeDTO.getSurname() != null ? employeeDTO.getSurname() : existing.getName();
        String company = employeeDTO.getCompany() != null ? employeeDTO.getCompany() : existing.getCompany();
        Position position = employeeDTO.getPosition() != null ? employeeDTO.getPosition() : existing.getPosition();
        int salary = employeeDTO.getSalary() != 0 ? employeeDTO.getSalary() : existing.getSalary();
        EmploymentStatus status = employeeDTO.getStatus() != null ? employeeDTO.getStatus() : existing.getStatus();

        Employee updated = new Employee(
                name,
                surname,
                company,
                email,
                position,
                salary
        );

        updated.setStatus(status);
        if (employeeDTO.getDepartmentId() != null) {
            // Pobierz departament z bazy i ustaw
            updated.setDepartment(existing.getDepartment());
        }

        Employee saved = this.employeeService.updateEmployee(email, updated);

        return ResponseEntity.ok(mapToDto(saved));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String email) {
        boolean deleted = this.employeeService.deleteEmployee(email);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{email}/status")
    public ResponseEntity<EmployeeDTO> updateStatus(@PathVariable String email, @RequestBody StatusUpdateDTO dto) {
        if (dto == null || dto.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }
        Employee updated = this.employeeService.updateEmployeeStatus(email, dto.getStatus());
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapToDto(updated));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeDTO>> getByStatus(@PathVariable EmploymentStatus status) {
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Employee> employees = this.employeeService.getEmployeesByStatus(status);
        List<EmployeeDTO> dtos = new ArrayList<>();
        for (Employee e : employees) {
            dtos.add(mapToDto(e));
        }
        return ResponseEntity.ok(dtos);
    }

    private EmployeeDTO mapToDto(Employee e) {
        return new EmployeeDTO(
                e.getName(),
                e.getSurname(),
                e.getEmail(),
                e.getCompany(),
                e.getPosition(),
                e.getSalary(),
                e.getStatus(),
                e.getDepartment() != null ? e.getDepartment().getId() : null
        );
    }
}