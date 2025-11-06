package com.github.jakubpakula1.lab.controller;

import com.github.jakubpakula1.lab.dto.CompanyStatisticsDTO;
import com.github.jakubpakula1.lab.model.CompanyStatistics;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final EmployeeService employeeService;

    public StatisticsController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/salary/average")
    public ResponseEntity<Map<String, Double>> getAverageSalary(@RequestParam(required = false) String company) {
        double average = employeeService.getAverageSalary(company);
        Map<String, Double> result = Collections.singletonMap("averageSalary", average);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/company/{companyName}")
    public ResponseEntity<CompanyStatisticsDTO> getCompanyStatistics(@PathVariable String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return employeeService.getCompanyStatisticsDTO(companyName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/positions")
    public ResponseEntity<Map<String, Integer>> getPositionStatistics() {
        Map<String, Integer> stats = employeeService.getPositionStatistics();
        return ResponseEntity.ok(stats != null ? stats : Collections.emptyMap());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Integer>> getStatusDistribution() {
        Map<String, Integer> distribution = employeeService.getStatusDistribution();
        return ResponseEntity.ok(distribution != null ? distribution : Collections.emptyMap());
    }
}