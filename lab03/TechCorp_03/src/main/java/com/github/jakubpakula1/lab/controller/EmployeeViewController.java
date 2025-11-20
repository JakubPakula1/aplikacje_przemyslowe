package com.github.jakubpakula1.lab.controller;

import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.ImportSummary;
import com.github.jakubpakula1.lab.model.Position;
import com.github.jakubpakula1.lab.service.DepartmentService;
import com.github.jakubpakula1.lab.service.EmployeeService;
import com.github.jakubpakula1.lab.service.FileStorageService;
import com.github.jakubpakula1.lab.service.ImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/employees")
public class EmployeeViewController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final ImportService importService;
    private final FileStorageService fileStorageService;
    public EmployeeViewController(EmployeeService employeeService, DepartmentService departmentService, ImportService importService, FileStorageService fileStorageService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.importService = importService;
        this.fileStorageService =fileStorageService;
    }
    @GetMapping
    public String listEmployees(Model model){
        List<Employee> employees = this.employeeService.getEmployees();

        model.addAttribute("employees", employees);

        return "employees/list";
    }

    @GetMapping("/add")
    public String addEmployeeForm(Model model){
        model.addAttribute("employee", new Employee());
        model.addAttribute("positions", Position.values());
        model.addAttribute("employmentStatuses", EmploymentStatus.values());
        model.addAttribute("departments", this.departmentService.getAllDepartments());

        return "employees/add-form";
    }

    @PostMapping("/add")
    public String addEmployee(@ModelAttribute Employee employee,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            bindingResult.rejectValue("name", "error.name", "Imię jest wymagane");
        }
        if (employee.getSurname() == null || employee.getSurname().trim().isEmpty()) {
            bindingResult.rejectValue("surname", "error.surname", "Nazwisko jest wymagane");
        }
        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            bindingResult.rejectValue("email", "error.email", "Email jest wymagany");
        }
        if (employee.getCompany() == null || employee.getCompany().trim().isEmpty()) {
            bindingResult.rejectValue("company", "error.company", "Firma jest wymagana");
        }
        if (employee.getSalary() < 0) {
            bindingResult.rejectValue("salary", "error.salary", "Wynagrodzenie nie może być ujemne");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("positions", Position.values());
            model.addAttribute("employmentStatuses", EmploymentStatus.values());
            model.addAttribute("departments", this.departmentService.getAllDepartments());
            return "employees/add-form";
        }

        try {
            this.employeeService.AddEmployee(employee);
            redirectAttributes.addFlashAttribute("message", "Pracownik dodany pomyślnie");
            return "redirect:/employees";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("error.employee", e.getMessage());
            model.addAttribute("positions", Position.values());
            model.addAttribute("employmentStatuses", EmploymentStatus.values());
            model.addAttribute("departments", this.departmentService.getAllDepartments());
            return "employees/add-form";
        }
    }

    @GetMapping("/edit/{email}")
    public String editEmployeeForm(@PathVariable String email, Model model){
        model.addAttribute("employee", this.employeeService.getEmployeeByEmail(email));
        model.addAttribute("positions", Position.values());
        model.addAttribute("employmentStatuses", EmploymentStatus.values());
        model.addAttribute("departments", this.departmentService.getAllDepartments());
        return "employees/edit-form";
    }

    @PostMapping("/edit")
    public String editEmployee(@ModelAttribute Employee employee,
                               BindingResult bindingResult,
                               Model model) {

        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            bindingResult.rejectValue("email", "error.email", "Email jest wymagany");
        }
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            bindingResult.rejectValue("name", "error.name", "Imię jest wymagane");
        }
        if (employee.getSurname() == null || employee.getSurname().trim().isEmpty()) {
            bindingResult.rejectValue("surname", "error.surname", "Nazwisko jest wymagane");
        }
        if (employee.getSalary() < 0) {
            bindingResult.rejectValue("salary", "error.salary", "Wynagrodzenie nie może być ujemne");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("positions", Position.values());
            model.addAttribute("employmentStatuses", EmploymentStatus.values());
            model.addAttribute("departments", this.departmentService.getAllDepartments());
            return "employees/edit-form";
        }

        try {
            this.employeeService.updateEmployee(employee.getEmail(), employee);
            return "redirect:/employees";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("error.employee", e.getMessage());
            model.addAttribute("positions", Position.values());
            model.addAttribute("employmentStatuses", EmploymentStatus.values());
            model.addAttribute("departments", this.departmentService.getAllDepartments());
            return "employees/edit-form";
        }
    }

    @DeleteMapping("/delete/{email}")
    public String deleteEmployee(@PathVariable String email, RedirectAttributes redirectAttributes){
        this.employeeService.deleteEmployee(email);
        redirectAttributes.addFlashAttribute("message", "Pracownik usunięty pomyślnie");
        return "employees/list";
    }

    @GetMapping("/search")
    public String searchEmployeesForm(){
        return "employees/search-form";
    }
    @PostMapping("/search")
    public String searchEmployees(@RequestParam("company") String company, Model model){
        model.addAttribute("employees", this.employeeService.getCompanyEmployees(company));
        model.addAttribute("company", company);

        return "employees/search-results";
    }
    @GetMapping("/import")
    public String importForm(){
        return "employees/import-form";
    }

    @PostMapping("/import")
    public String importEmployees(@RequestParam("file") MultipartFile file,
                                  @RequestParam("fileType") String fileType,
                                  RedirectAttributes redirectAttributes){
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Proszę wybrać plik");
                return "redirect:/employees/import";
            }

            String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
            file.transferTo(new java.io.File(tempFilePath));

            ImportSummary summary;
            if ("csv".equalsIgnoreCase(fileType)) {
                summary = importService.importFromCsv(tempFilePath);
            } else if ("xml".equalsIgnoreCase(fileType)) {
                summary = importService.importFromXml(tempFilePath);
            } else {
                redirectAttributes.addFlashAttribute("error", "Nieznany typ pliku");
                return "redirect:/employees/import";
            }

            new java.io.File(tempFilePath).delete();

            redirectAttributes.addFlashAttribute("summary", summary);
            redirectAttributes.addFlashAttribute("importedCount", summary.getImportedEmployees());

            if (!summary.getErrors().isEmpty()) {
                redirectAttributes.addFlashAttribute("errors", summary.getErrors());
                return "redirect:/employees/import-results";
            }

            redirectAttributes.addFlashAttribute("message", "Zaimportowano " + summary.getImportedEmployees() + " pracowników");
            return "redirect:/employees";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd podczas importu: " + e.getMessage());
            return "redirect:/employees/import";
        }
    }

    @GetMapping("/import-results")
    public String importResults(Model model){
        return "employees/import-results";
    }
}
