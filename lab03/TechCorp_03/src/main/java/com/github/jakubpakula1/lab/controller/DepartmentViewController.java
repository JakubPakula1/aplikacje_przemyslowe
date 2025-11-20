package com.github.jakubpakula1.lab.controller;

import com.github.jakubpakula1.lab.dto.CreateDepartmentDTO;
import com.github.jakubpakula1.lab.model.*;
import com.github.jakubpakula1.lab.service.DepartmentService;
import com.github.jakubpakula1.lab.service.EmployeeService;
import com.github.jakubpakula1.lab.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/departments")
public class DepartmentViewController {

    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final FileStorageService fileStorageService;

    public DepartmentViewController(DepartmentService departmentService, EmployeeService employeeService, FileStorageService fileStorageService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.fileStorageService = fileStorageService;
    }
    @GetMapping
    public String listDepartments(Model model){
        List<Department> departments = this.departmentService.getAllDepartments();

        Map<Long, Employee> departmentManagers = departments.stream()
                .collect(Collectors.toMap(
                        Department::getId,
                        dept -> {
                            try {
                                return this.employeeService.getEmployeeByEmail(dept.getManagerEmail());
                            } catch (Exception e) {
                                return null;
                            }
                        },
                        (existing, replacement) -> existing
                ));

        model.addAttribute("departments", departments);
        model.addAttribute("departmentManagers", departmentManagers);

        return "departments/list";
    }

    @GetMapping("/{id}")
    public String getDepartmentDetails(@PathVariable Long id, Model model){
        Department department = this.departmentService.getDepartmentById(id);
        List<Employee> departmentEmployees = this.employeeService.getEmployeesByDepartment(id);
        model.addAttribute("department", department);
        model.addAttribute("departmentEmployees", departmentEmployees);

        return "departments/details";
    }

    @GetMapping("/add")
    public String addFormDepartment(Model model){

        List<Employee> managersAndAbove = this.employeeService.getEmployeesManagerAndAbove();
        model.addAttribute("department", new Department());
        model.addAttribute("possibleManagers", managersAndAbove);

        return "departments/form";
    }

    @PostMapping("/add")
    public String addDepartment(@ModelAttribute CreateDepartmentDTO createDepartmentDTO, RedirectAttributes redirectAttributes){
        this.departmentService.addDepartment(createDepartmentDTO);
        redirectAttributes.addFlashAttribute("message", "Department dodany pomyslnie");
        return "redirect:/departments";
    }

    @GetMapping("/edit/{id}")
    public String editDepartmentForm(@PathVariable Long id, Model model){
        List<Employee> managersAndAbove = this.employeeService.getEmployeesManagerAndAbove();

        model.addAttribute("department", this.departmentService.getDepartmentById(id));
        model.addAttribute("possibleManagers", managersAndAbove);

        return "departments/form";
    }

    @PostMapping("/edit")
    public String editDepartment(@RequestParam Long id, @ModelAttribute CreateDepartmentDTO createDepartmentDTO){
        this.departmentService.updateDepartment(id, createDepartmentDTO);
        return "redirect:/departments";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        this.departmentService.deleteDepartment(id);
        redirectAttributes.addFlashAttribute("message", "Department usunięty pomyślnie");
        return "redirect:/departments";
    }
    @GetMapping("/documents/{id}")
    public String listDepartmentDocuments(@PathVariable Long id, Model model){
        Department department = this.departmentService.getDepartmentById(id);
        String departmentEmail = "dept_" + id + "@company.local";

        model.addAttribute("department", department);
        model.addAttribute("documents", this.fileStorageService.listEmployeeDocuments(departmentEmail));
        model.addAttribute("fileTypes", FileType.values());

        return "departments/documents";
    }

    @PostMapping("/documents/{id}/upload")
    public String uploadDepartmentDocument(@PathVariable Long id,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestParam("fileType") FileType fileType,
                                           RedirectAttributes redirectAttributes){
        try {
            Department department = this.departmentService.getDepartmentById(id);
            if (department == null) {
                redirectAttributes.addFlashAttribute("error", "Departament nie istnieje");
                return "redirect:/departments/documents/" + id;
            }

            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Proszę wybrać plik");
                return "redirect:/departments/documents/" + id;
            }

            String departmentEmail = "dept_" + id + "@company.local";
            this.fileStorageService.storeEmployeeDocument(departmentEmail, file, fileType);

            redirectAttributes.addFlashAttribute("message", "Plik przesłany pomyślnie");
            return "redirect:/departments/documents/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd podczas przesyłania: " + e.getMessage());
            return "redirect:/departments/documents/" + id;
        }
    }

    @GetMapping("/documents/{id}/download/{documentId}")
    public ResponseEntity<Resource> downloadDepartmentDocument(@PathVariable Long id,
                                                               @PathVariable String documentId){
        try {
            String departmentEmail = "dept_" + id + "@company.local";
            var document = this.fileStorageService.findEmployeeDocument(departmentEmail, documentId);
            Resource resource = this.fileStorageService.loadEmployeeDocumentAsResource(document);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/documents/{id}/delete/{documentId}")
    public String deleteDepartmentDocument(@PathVariable Long id,
                                           @PathVariable String documentId,
                                           RedirectAttributes redirectAttributes){
        try {
            String departmentEmail = "dept_" + id + "@company.local";
            this.fileStorageService.deleteEmployeeDocument(departmentEmail, documentId);

            redirectAttributes.addFlashAttribute("message", "Plik usunięty pomyślnie");
            return "redirect:/departments/documents/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Błąd podczas usuwania: " + e.getMessage());
            return "redirect:/departments/documents/" + id;
        }
    }
}
