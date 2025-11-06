package com.github.jakubpakula1.lab.controller;

import com.github.jakubpakula1.lab.dto.EmployeeDTO;
import com.github.jakubpakula1.lab.dto.StatusUpdateDTO;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService){
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(@RequestParam(required = false) String company) {
        List<Employee> employees;
        if (company == null || company.isBlank()) {
            employees = this.employeeService.getEmployees();
        } else {
            employees = this.employeeService.getCompanyEmployees(company.trim());
        }

        List<EmployeeDTO> employeeDTOS = new ArrayList<>();
        for (Employee employee : employees) {
            employeeDTOS.add(mapToDto(employee));
        }
        return ResponseEntity.ok(employeeDTOS);
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
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        if (employeeDTO == null || employeeDTO.getEmail() == null || employeeDTO.getEmail().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String positionStr = employeeDTO.getPosition() != null ? employeeDTO.getPosition().name() : null;
        String statusStr = employeeDTO.getStatus() != null ? employeeDTO.getStatus().name() : null;

        Employee toCreate = new Employee(
                employeeDTO.getName(),
                employeeDTO.getSurname(),
                employeeDTO.getEmail().trim(),
                employeeDTO.getCompany(),
                positionStr,
                employeeDTO.getSalary(),
                statusStr
        );

        Employee created = this.employeeService.AddEmployee(toCreate);

        java.net.URI location = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{email}")
                .buildAndExpand(created.getEmail())
                .toUri();

        return ResponseEntity.created(location).body(mapToDto(created));
    }

    @PutMapping("/{email}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable String email, @RequestBody EmployeeDTO employeeDTO) {
        Employee existing = this.employeeService.getEmployeeByEmail(email);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        String name = employeeDTO.getName() != null ? employeeDTO.getName() : existing.getName();
        String surname = employeeDTO.getSurname() != null ? employeeDTO.getSurname() : existing.getSurname();
        String company = employeeDTO.getCompany() != null ? employeeDTO.getCompany() : existing.getCompany();
        String positionStr = employeeDTO.getPosition() != null ? employeeDTO.getPosition().name()
                : (existing.getPosition() == null ? null : existing.getPosition().name());
        int salary = employeeDTO.getSalary() != 0 ? employeeDTO.getSalary() : existing.getSalary();
        String statusStr = employeeDTO.getStatus() != null ? employeeDTO.getStatus().name()
                : (existing.getStatus() == null ? null : existing.getStatus().name());

        Employee updated = new Employee(
                name,
                surname,
                company,
                email,
                positionStr,
                salary,
                statusStr
        );

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
                e.getStatus()
        );
}
}
