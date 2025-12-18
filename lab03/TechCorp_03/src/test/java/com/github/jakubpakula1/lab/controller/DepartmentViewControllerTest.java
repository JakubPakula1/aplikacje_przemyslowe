package com.github.jakubpakula1.lab.controller;

import com.github.jakubpakula1.lab.dto.CreateDepartmentDTO;
import com.github.jakubpakula1.lab.model.Department;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.Position;
import com.github.jakubpakula1.lab.service.DepartmentService;
import com.github.jakubpakula1.lab.service.EmployeeService;
import com.github.jakubpakula1.lab.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentViewController.class)
class DepartmentViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private FileStorageService fileStorageService;

    private Department testDepartment;
    private List<Department> testDepartments;
    private Employee testManager;

    @BeforeEach
    void setUp() {
        testManager = new Employee("Jan", "Kowalski", "Company", "jan@example.com", Position.MANAGER, BigDecimal.valueOf(8000));

        testDepartment = new Department(1L, "IT", "Warsaw", 100000.0, "jan@example.com");

        Department dept2 = new Department(2L, "HR", "Krakow", 50000.0, "manager2@example.com");
        testDepartments = Arrays.asList(testDepartment, dept2);
    }

    @Test
    void testListDepartmentsSuccess() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(testDepartments);
        when(employeeService.getEmployeeByEmail("jan@example.com")).thenReturn(testManager);
        when(employeeService.getEmployeeByEmail("manager2@example.com")).thenReturn(
                new Employee("Jane", "Smith", "TestCompany", "manager2@example.com", Position.MANAGER, BigDecimal.valueOf(7000))
        );

        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/list"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("departmentManagers"))
                .andExpect(model().attribute("departments", hasSize(2)));

        verify(departmentService, times(1)).getAllDepartments();
    }

    @Test
    void testListDepartmentsEmpty() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/list"))
                .andExpect(model().attribute("departments", hasSize(0)));

        verify(departmentService, times(1)).getAllDepartments();
    }

    @Test
    void testAddDepartmentFormDisplay() throws Exception {
        List<Employee> managers = Arrays.asList(testManager);
        when(employeeService.getEmployeesManagerAndAbove()).thenReturn(managers);

        mockMvc.perform(get("/departments/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("possibleManagers"));

        verify(employeeService, times(1)).getEmployeesManagerAndAbove();
    }

    @Test
    void testAddDepartmentSuccess() throws Exception {
        doNothing().when(departmentService).addDepartment(any(CreateDepartmentDTO.class));

        mockMvc.perform(post("/departments/add")
                        .param("name", "IT")
                        .param("location", "Warsaw")
                        .param("budget", "100000.0")
                        .param("managerEmail", "jan@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("message"));

        verify(departmentService, times(1)).addDepartment(any(CreateDepartmentDTO.class));
    }

    @Test
    void testEditDepartmentFormDisplay() throws Exception {
        List<Employee> managers = Arrays.asList(testManager);
        when(departmentService.getDepartmentById(1L)).thenReturn(testDepartment);
        when(employeeService.getEmployeesManagerAndAbove()).thenReturn(managers);

        mockMvc.perform(get("/departments/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("possibleManagers"))
                .andExpect(model().attribute("department", hasProperty("name", is("IT"))));

        verify(departmentService, times(1)).getDepartmentById(1L);
        verify(employeeService, times(1)).getEmployeesManagerAndAbove();
    }

    @Test
    void testEditDepartmentSuccess() throws Exception {
        when(departmentService.updateDepartment(eq(1L), any(CreateDepartmentDTO.class))).thenReturn(testDepartment);

        mockMvc.perform(post("/departments/edit")
                        .param("id", "1")
                        .param("name", "IT Updated")
                        .param("location", "Warsaw")
                        .param("budget", "120000.0")
                        .param("managerEmail", "jan@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"));

        verify(departmentService, times(1)).updateDepartment(eq(1L), any(CreateDepartmentDTO.class));
    }

    @Test
    void testDeleteDepartmentSuccess() throws Exception {
        when(departmentService.deleteDepartment(1L)).thenReturn(testDepartment);

        mockMvc.perform(delete("/departments/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("message"));

        verify(departmentService, times(1)).deleteDepartment(1L);
    }

    @Test
    void testGetDepartmentDetails() throws Exception {
        List<Employee> employees = Arrays.asList(testManager);
        when(departmentService.getDepartmentById(1L)).thenReturn(testDepartment);
        when(employeeService.getEmployeesByDepartment(1L)).thenReturn(employees);

        mockMvc.perform(get("/departments/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/details"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("departmentEmployees"))
                .andExpect(model().attribute("department", hasProperty("name", is("IT"))))
                .andExpect(model().attribute("departmentEmployees", hasSize(1)));

        verify(departmentService, times(1)).getDepartmentById(1L);
        verify(employeeService, times(1)).getEmployeesByDepartment(1L);
    }

    @Test
    void testListDepartmentDocuments() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(testDepartment);
        when(fileStorageService.listEmployeeDocuments("dept_1@company.local")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/departments/documents/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/documents"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("documents"))
                .andExpect(model().attributeExists("fileTypes"));

        verify(departmentService, times(1)).getDepartmentById(1L);
        verify(fileStorageService, times(1)).listEmployeeDocuments("dept_1@company.local");
    }
}