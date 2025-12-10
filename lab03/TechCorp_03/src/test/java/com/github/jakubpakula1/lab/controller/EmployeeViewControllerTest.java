package com.github.jakubpakula1.lab.controller;

import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.ImportSummary;
import com.github.jakubpakula1.lab.model.Position;
import com.github.jakubpakula1.lab.service.DepartmentService;
import com.github.jakubpakula1.lab.service.EmployeeService;
import com.github.jakubpakula1.lab.service.FileStorageService;
import com.github.jakubpakula1.lab.service.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @WebMvcTest(EmployeeViewController.class)
    class EmployeeViewControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private EmployeeService employeeService;

        @MockBean
        private DepartmentService departmentService;

        @MockBean
        private ImportService importService;

        @MockBean
        private FileStorageService fileStorageService;

        private List<Employee> testEmployees;
        private Employee emp1;
        private Employee emp2;
        private Employee testEmployee;
        @BeforeEach
        void setUp() {
            emp1 = new Employee("Jan", "Kowalski", "Google", "jan@techcorp.com", Position.PROGRAMISTA, 8000);
            emp1.setStatus(EmploymentStatus.ACTIVE);

            emp2 = new Employee("Anna", "Nowak", "Microsoft", "anna@techcorp.com", Position.MANAGER, 12000);
            emp2.setStatus(EmploymentStatus.ACTIVE);

            testEmployee = new Employee("John", "Doe", "TestCompany", "john@techcorp.com", Position.PROGRAMISTA, 5000);
            testEmployee.setStatus(EmploymentStatus.ACTIVE);

            testEmployees = Arrays.asList(emp1, emp2);

            when(departmentService.getAllDepartments()).thenReturn(Collections.emptyList());

        }

        @Test
        void testListEmployeesSuccess() throws Exception {
            Page<Employee> page = new PageImpl<>(testEmployees, PageRequest.of(0, 10), 2);
            when(employeeService.getAllEmployeesPage(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/employees"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employees/list"))
                    .andExpect(model().attributeExists("employees"))
                    .andExpect(model().attribute("employees", hasSize(2)))
                    .andExpect(model().attribute("employees", hasItem(
                            allOf(
                                    hasProperty("name", equalTo("Jan")),
                                    hasProperty("company", equalTo("Google"))
                            )
                    )));

            verify(employeeService, times(1)).getAllEmployeesPage(any(Pageable.class));
        }

        @Test
        void testListEmployeesEmpty() throws Exception {
            Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
            when(employeeService.getAllEmployeesPage(any(Pageable.class))).thenReturn(emptyPage);

            mockMvc.perform(get("/employees"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employees/list"))
                    .andExpect(model().attribute("employees", hasSize(0)));
        }
        @Test
        void testAddEmployeeFormDisplay() throws Exception {
            mockMvc.perform(get("/employees/add"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employees/add-form"))
                    .andExpect(model().attributeExists("employee"))
                    .andExpect(model().attributeExists("positions"))
                    .andExpect(model().attributeExists("employmentStatuses"))
                    .andExpect(model().attributeExists("departments"));
        }

        @Test
        void testAddEmployeeValidationErrorName() throws Exception {
            mockMvc.perform(post("/employees/add")
                    .param("name", "")
                    .param("surname", "Doe")
                    .param("email", "john@example.com")
                    .param("company", "TestCompany")
                    .param("position", "PROGRAMISTA")
                    .param("salary", "5000")
                    .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employees/add-form"))
                    .andExpect(model().hasErrors());

            verify(employeeService, never()).addEmployee(any(Employee.class));
        }

        @Test
        void testAddEmployeeValidationErrorSalary() throws Exception {
            mockMvc.perform(post("/employees/add")
                    .param("name", "John")
                    .param("surname", "Doe")
                    .param("email", "john@example.com")
                    .param("company", "TestCompany")
                    .param("position", "PROGRAMISTA")
                    .param("salary", "-100")
                    .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employees/add-form"))
                    .andExpect(model().hasErrors());

            verify(employeeService, never()).addEmployee(any(Employee.class));
        }

        @Test
        void testAddEmployeeDuplicateEmail() throws Exception {
            doThrow(new IllegalArgumentException("Employee with this email already exists!"))
                    .when(employeeService).addEmployee(any(Employee.class));

            mockMvc.perform(post("/employees/add")
                    .param("name", "John")
                    .param("surname", "Doe")
                    .param("email", "john@example.com")
                    .param("company", "TestCompany")
                    .param("position", "PROGRAMISTA")
                    .param("salary", "5000")
                    .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employees/add-form"))
                    .andExpect(model().hasErrors());

            verify(employeeService, times(1)).addEmployee(any(Employee.class));
        }
        @Test
        void testEditEmployeeFormDisplay() throws Exception {
            when(employeeService.getEmployeeByEmail(emp1.getEmail())).thenReturn(emp1);

            mockMvc.perform(get("/employees/edit/" + emp1.getEmail()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employees/edit-form"))
                    .andExpect(model().attributeExists("employee"))
                    .andExpect(model().attributeExists("positions"))
                    .andExpect(model().attributeExists("employmentStatuses"))
                    .andExpect(model().attributeExists("departments"))
                    .andExpect(model().attribute("employee", hasProperty("email", is(emp1.getEmail()))));

            verify(employeeService, times(1)).getEmployeeByEmail(emp1.getEmail());
        }

        @Test
        void testEditEmployeeSuccess() throws Exception {
            when(employeeService.updateEmployee(eq("john@example.com"), any(Employee.class))).thenReturn(testEmployee);

            mockMvc.perform(post("/employees/edit")
                            .param("email", "john@example.com")
                            .param("name", "John Updated")
                            .param("surname", "Doe")
                            .param("company", "TestCompany")
                            .param("position", "MANAGER")
                            .param("salary", "6000")
                            .param("status", "ACTIVE"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/employees"));

            verify(employeeService, times(1)).updateEmployee(eq("john@example.com"), any(Employee.class));
        }

        @Test
        void testEditEmployeeValidationErrorEmail() throws Exception {
            mockMvc.perform(post("/employees/edit")
                    .param("email", "")
                    .param("name", "John")
                    .param("surname", "Doe")
                    .param("company", "TestCompany")
                    .param("position", "PROGRAMISTA")
                    .param("salary", "5000")
                    .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employees/edit-form"))
                    .andExpect(model().hasErrors());

            verify(employeeService, never()).updateEmployee(anyString(), any(Employee.class));
        }

        @Test
        void testEditEmployeeValidationErrorNegativeSalary() throws Exception {
            mockMvc.perform(post("/employees/edit")
                    .param("email", "john@example.com")
                    .param("name", "John")
                    .param("surname", "Doe")
                    .param("company", "TestCompany")
                    .param("position", "PROGRAMISTA")
                    .param("salary", "-5000")
                    .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employees/edit-form"))
                    .andExpect(model().hasErrors());

            verify(employeeService, never()).updateEmployee(anyString(), any(Employee.class));
        }

        @Test
        void testImportEmployeesCsvSuccess() throws Exception {
            ImportSummary summary = new ImportSummary();
            summary.setImportedEmployees(2);

            when(importService.importFromCsv(anyString())).thenReturn(summary);

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "employees.csv",
                    "text/csv",
                    "John;Doe;john@example.com;TestCompany;PROGRAMISTA;5000".getBytes()
            );

            mockMvc.perform(multipart("/employees/import")
                    .file(file)
                    .param("fileType", "csv"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/employees"))
                    .andExpect(flash().attributeExists("message"));

            verify(importService, times(1)).importFromCsv(anyString());
        }

        @Test
        void testImportEmployeesXmlSuccess() throws Exception {
            ImportSummary summary = new ImportSummary();
            summary.setImportedEmployees(1);

            when(importService.importFromXml(anyString())).thenReturn(summary);

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "employees.xml",
                    "text/xml",
                    "<employees><employee><name>John</name></employee></employees>".getBytes()
            );

            mockMvc.perform(multipart("/employees/import")
                    .file(file)
                    .param("fileType", "xml"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/employees"))
                    .andExpect(flash().attributeExists("message"));

            verify(importService, times(1)).importFromXml(anyString());
        }

        @Test
        void testImportEmployeesEmptyFile() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "employees.csv",
                    "text/csv",
                    new byte[0]
            );

            mockMvc.perform(multipart("/employees/import")
                    .file(file)
                    .param("fileType", "csv"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/employees/import"))
                    .andExpect(flash().attributeExists("error"));

            verify(importService, never()).importFromCsv(anyString());
        }

        @Test
        void testImportEmployeesUnsupportedFileType() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "employees.json",
                    "application/json",
                    "{}".getBytes()
            );

            mockMvc.perform(multipart("/employees/import")
                    .file(file)
                    .param("fileType", "json"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/employees/import"))
                    .andExpect(flash().attributeExists("error"));

            verify(importService, never()).importFromCsv(anyString());
            verify(importService, never()).importFromXml(anyString());
        }

        @Test
        void testImportEmployeesWithErrors() throws Exception {
            ImportSummary summary = new ImportSummary();
            summary.setImportedEmployees(1);
            summary.addError(2, "Invalid position");
            summary.addError(3, "Invalid salary");

            when(importService.importFromCsv(anyString())).thenReturn(summary);

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "employees.csv",
                    "text/csv",
                    "John;Doe;john@example.com;TestCompany;PROGRAMISTA;5000".getBytes()
            );

            mockMvc.perform(multipart("/employees/import")
                    .file(file)
                    .param("fileType", "csv"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/employees/import-results"))
                    .andExpect(flash().attributeExists("errors", "summary"));

            verify(importService, times(1)).importFromCsv(anyString());
        }

        @Test
        void testImportEmployeesImportException() throws Exception {
            when(importService.importFromCsv(anyString())).thenThrow(new IOException("File read error"));

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "employees.csv",
                    "text/csv",
                    "data".getBytes()
            );

            mockMvc.perform(multipart("/employees/import")
                    .file(file)
                    .param("fileType", "csv"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/employees/import"))
                    .andExpect(flash().attributeExists("error"));

            verify(importService, times(1)).importFromCsv(anyString());
        }

        @Test
        void testImportResultsPage() throws Exception {
            mockMvc.perform(get("/employees/import-results"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employees/import-results"));
        }
    }