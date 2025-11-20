package com.github.jakubpakula1.lab.controller;

import com.github.jakubpakula1.lab.dto.CompanyStatisticsDTO;
import com.github.jakubpakula1.lab.model.CompanyStatistics;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.service.DepartmentService;
import com.github.jakubpakula1.lab.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/statistics")
public class StatisticsViewController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public StatisticsViewController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String displayDashboard(Model model) {
        List<Employee> allEmployees = employeeService.getEmployees();
        int totalEmployees = allEmployees.size();
        double averageSalary = employeeService.getAverageSalary();
        int totalDepartments = departmentService.getAllDepartments().size();

        Map<String, CompanyStatistics> companyStats = employeeService.getCompanyStatistics();
        Map<String, Integer> positionStats = employeeService.getPositionStatistics();
        Map<String, Integer> statusDistribution = employeeService.getStatusDistribution();

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("averageSalary", String.format("%.2f", averageSalary));
        model.addAttribute("totalDepartments", totalDepartments);
        model.addAttribute("companyStatistics", companyStats);
        model.addAttribute("positionStatistics", positionStats);
        model.addAttribute("statusDistribution", statusDistribution);

        return "statistics/dashboard";
    }

    @GetMapping("/company/{name}")
    public String displayCompanyStatistics(@PathVariable String name, Model model) {
        Optional<CompanyStatisticsDTO> statsOpt = employeeService.getCompanyStatisticsDTO(name);

        if (statsOpt.isEmpty()) {
            model.addAttribute("error", "Brak danych dla firmy: " + name);
            return "statistics/company-details";
        }

        CompanyStatisticsDTO stats = statsOpt.get();
        List<Employee> employees = employeeService.getCompanyEmployees(name);

        model.addAttribute("company", stats);
        model.addAttribute("employees", employees);
        model.addAttribute("employeeCount", employees.size());

        return "statistics/company-details";
    }
}