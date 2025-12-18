package com.github.jakubpakula1.lab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jakubpakula1.lab.dto.StatusUpdateDTO;
import com.github.jakubpakula1.lab.exception.DuplicateEmailException;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.service.EmployeeService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private Employee mockEmployee(String name, String surname, String email, String company, BigDecimal salary, EmploymentStatus status) {
        Employee e = mock(Employee.class);
        when(e.getName()).thenReturn(name);
        when(e.getSurname()).thenReturn(surname);
        when(e.getEmail()).thenReturn(email);
        when(e.getCompany()).thenReturn(company);
        when(e.getSalary()).thenReturn(salary);
        when(e.getStatus()).thenReturn(status);
        return e;
    }

    @Test
    @DisplayName("GET /api/employees - zwraca 200 i listę pracowników")
    void testGetAllEmployees() throws Exception {
        Employee e1 = mockEmployee("John", "Doe", "john@example.com", "Acme", BigDecimal.valueOf(5000), EmploymentStatus.ACTIVE);
        Employee e2 = mockEmployee("Jane", "Smith", "jane@example.com", "Acme", BigDecimal.valueOf(6000), EmploymentStatus.ON_LEAVE);

        when(employeeService.getAllEmployees()).thenReturn(List.of(e1, e2));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].email").value("john@example.com"))
                .andExpect(jsonPath("$[1].email").value("jane@example.com"));

        verify(employeeService).getAllEmployees();
    }

    @Test
    @DisplayName("GET /api/employees/{email} - zwraca pracownika")
    void testGetEmployeeByEmail() throws Exception {
        Employee e = mockEmployee("John", "Doe", "john@example.com", "Acme", BigDecimal.valueOf(5000), EmploymentStatus.ACTIVE);
        when(employeeService.getEmployeeByEmail("john@example.com")).thenReturn(e);

        mockMvc.perform(get("/api/employees/john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.company").value("Acme"))
                .andExpect(jsonPath("$.salary").value(5000));

        verify(employeeService).getEmployeeByEmail("john@example.com");
    }

    @Test
    @DisplayName("GET /api/employees/{email} - nieistniejący zwraca 404")
    void testGetEmployeeNotFound() throws Exception {
        when(employeeService.getEmployeeByEmail("noone@example.com")).thenReturn(null);

        mockMvc.perform(get("/api/employees/noone@example.com"))
                .andExpect(status().isNotFound());

        verify(employeeService).getEmployeeByEmail("noone@example.com");
    }

    @Test
    @DisplayName("POST /api/employees - tworzy pracownika, zwraca 201 i Location")
    void testCreateEmployee() throws Exception {
        var createMap = new java.util.HashMap<String, Object>();
        createMap.put("name", "John");
        createMap.put("surname", "Doe");
        createMap.put("email", "john@techcorp.com");
        createMap.put("company", "Acme");
        createMap.put("position", "PROGRAMISTA");
        createMap.put("salary", 5000);
        createMap.put("status", "ACTIVE");

        Employee created = mockEmployee("John", "Doe", "john@techcorp.com", "Acme", BigDecimal.valueOf(5000), EmploymentStatus.ACTIVE);
        when(employeeService.addEmployee(ArgumentMatchers.any())).thenReturn(created);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMap)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.containsString("/api/employees/john@techcorp.com")))
                .andExpect(jsonPath("$.email").value("john@techcorp.com"));

        verify(employeeService).addEmployee(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("POST /api/employees - duplikat zwraca 409")
    void testCreateEmployeeDuplicate() throws Exception {
        var createMap = new java.util.HashMap<String, Object>();
        createMap.put("name", "John");
        createMap.put("surname", "Doe");
        createMap.put("email", "john@techcorp.com");
        createMap.put("company", "Acme");
        createMap.put("salary", 5000);
        createMap.put("position", "PROGRAMISTA");
        createMap.put("status", "ACTIVE");

        when(employeeService.addEmployee(ArgumentMatchers.any()))
                .thenThrow(new DuplicateEmailException("Employee with this email already exists!"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMap)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Employee with this email already exists!"))
                .andExpect(jsonPath("$.status").value(409));

        verify(employeeService).addEmployee(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("DELETE /api/employees/{email} - zwraca 204")
    void testDeleteEmployee() throws Exception {
        when(employeeService.deleteEmployee("john@example.com")).thenReturn(true);

        mockMvc.perform(delete("/api/employees/john@example.com"))
                .andExpect(status().isNoContent());

        verify(employeeService).deleteEmployee("john@example.com");
    }

    @Test
    @DisplayName("GET /api/employees?company=... - filtrowanie po firmie")
    void testFilterByCompany() throws Exception {
        Employee e = mockEmployee("Alice", "Wong", "alice@example.com", "Globex", BigDecimal.valueOf(4500), EmploymentStatus.ACTIVE);
        when(employeeService.getCompanyEmployees("Globex")).thenReturn(List.of(e));

        mockMvc.perform(get("/api/employees").param("company", "Globex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].company").value("Globex"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"));

        verify(employeeService).getCompanyEmployees("Globex");
    }

    @Test
    @DisplayName("PATCH /api/employees/{email}/status - zmiana statusu")
    void testPatchChangeStatus() throws Exception {
        StatusUpdateDTO dto = new StatusUpdateDTO();
        dto.setStatus(EmploymentStatus.ON_LEAVE);

        Employee updated = mockEmployee("John", "Doe", "john@example.com", "Acme", BigDecimal.valueOf(5000), EmploymentStatus.ON_LEAVE);
        when(employeeService.updateEmployeeStatus("john@example.com", EmploymentStatus.ON_LEAVE)).thenReturn(updated);

        mockMvc.perform(patch("/api/employees/john@example.com/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.status").value("ON_LEAVE"));

        verify(employeeService).updateEmployeeStatus("john@example.com", EmploymentStatus.ON_LEAVE);
    }

        @Test
        @DisplayName("PUT /api/employees/{email} - aktualizacja zwraca 200")
        void testUpdateEmployeeSuccess() throws Exception {
            var updateMap = new java.util.HashMap<String, Object>();
            updateMap.put("name", "John");
            updateMap.put("surname", "Updated");
            updateMap.put("email", "john.updated@techcorp.com");
            updateMap.put("company", "Acme");
            updateMap.put("position", "PROGRAMISTA");
            updateMap.put("salary", 7000);
            updateMap.put("status", "ACTIVE");

        Employee existing = mockEmployee("John", "Doe", "john@techcorp.com", "Acme", BigDecimal.valueOf(5000), EmploymentStatus.ACTIVE);
        when(employeeService.getEmployeeByEmail("john@techcorp.com")).thenReturn(existing);

        Employee saved = mockEmployee("John", "Updated", "john.updated@techcorp.com", "Acme", BigDecimal.valueOf(7000), EmploymentStatus.ACTIVE);
        when(employeeService.updateEmployee(eq("john@techcorp.com"), ArgumentMatchers.any())).thenReturn(saved);

            mockMvc.perform(put("/api/employees/john@techcorp.com")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateMap)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("john.updated@techcorp.com"))
                    .andExpect(jsonPath("$.surname").value("Updated"))
                    .andExpect(jsonPath("$.salary").value(7000));

            verify(employeeService).getEmployeeByEmail("john@techcorp.com");
            verify(employeeService).updateEmployee(eq("john@techcorp.com"), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("PUT /api/employees/{email} - duplikat e-maila zwraca 409")
        void testUpdateEmployeeDuplicateConflict() throws Exception {
            var updateMap = new java.util.HashMap<String, Object>();
            updateMap.put("name", "John");
            updateMap.put("surname", "Doe");
            updateMap.put("email", "existing@techcorp.com"); // próbujemy zmienić na istniejący
            updateMap.put("company", "Acme");
            updateMap.put("position", "PROGRAMISTA");
            updateMap.put("salary", 6000);
            updateMap.put("status", "ACTIVE");

            Employee existing = mockEmployee("John", "Doe", "john@techcorp.com", "Acme", BigDecimal.valueOf(5000), EmploymentStatus.ACTIVE);
            when(employeeService.getEmployeeByEmail("john@techcorp.com")).thenReturn(existing);

            when(employeeService.updateEmployee(eq("john@techcorp.com"), ArgumentMatchers.any()))
                    .thenThrow(new DuplicateEmailException("Employee with this email already exists!"));

            mockMvc.perform(put("/api/employees/john@techcorp.com")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateMap)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Employee with this email already exists!"))
                    .andExpect(jsonPath("$.status").value(409));

            verify(employeeService).getEmployeeByEmail("john@techcorp.com");
            verify(employeeService).updateEmployee(eq("john@techcorp.com"), ArgumentMatchers.any());
        }

        @Test
        @DisplayName("POST /api/employees - walidacja: puste imię zwraca 400")
        void testCreateEmployeeEmptyFirstName() throws Exception {
            var createMap = new java.util.HashMap<String, Object>();
            createMap.put("name", ""); // puste imię
            createMap.put("surname", "Doe");
            createMap.put("email", "john@techcorp.com");
            createMap.put("company", "Acme");
            createMap.put("salary", 5000);
            createMap.put("position", "PROGRAMISTA");
            createMap.put("status", "ACTIVE");

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createMap)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Błąd walidacji danych"))
                    .andExpect(jsonPath("$.errors.firstName").exists());
        }

        @Test
        @DisplayName("POST /api/employees - walidacja: ujemna pensja zwraca 400")
        void testCreateEmployeeNegativeSalary() throws Exception {
            var createMap = new java.util.HashMap<String, Object>();
            createMap.put("name", "John");
            createMap.put("surname", "Doe");
            createMap.put("email", "john@techcorp.com");
            createMap.put("company", "Acme");
            createMap.put("salary", -5000); // ujemna pensja
            createMap.put("position", "PROGRAMISTA");
            createMap.put("status", "ACTIVE");

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createMap)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Błąd walidacji danych"))
                    .andExpect(jsonPath("$.errors.salary").exists());
        }

        @Test
        @DisplayName("POST /api/employees - walidacja: email bez domeny @techcorp.com zwraca 400")
        void testCreateEmployeeInvalidDomain() throws Exception {
            var createMap = new java.util.HashMap<String, Object>();
            createMap.put("name", "John");
            createMap.put("surname", "Doe");
            createMap.put("email", "john@example.com"); // brak domeny @techcorp.com
            createMap.put("company", "Acme");
            createMap.put("salary", 5000);
            createMap.put("position", "PROGRAMISTA");
            createMap.put("status", "ACTIVE");

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createMap)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Błąd walidacji danych"))
                    .andExpect(jsonPath("$.errors.email").exists());
        }

        @Test
        @DisplayName("POST /api/employees - walidacja: poprawne dane z @techcorp.com domeny zwracają 201")
        void testCreateEmployeeValidTechCorpEmail() throws Exception {
            var createMap = new java.util.HashMap<String, Object>();
            createMap.put("name", "John");
            createMap.put("surname", "Doe");
            createMap.put("email", "john@techcorp.com"); // prawidłowa domena
            createMap.put("company", "Acme");
            createMap.put("salary", 5000);
            createMap.put("position", "PROGRAMISTA");
            createMap.put("status", "ACTIVE");

            Employee created = mockEmployee("John", "Doe", "john@techcorp.com", "Acme", BigDecimal.valueOf(5000), EmploymentStatus.ACTIVE);
            when(employeeService.addEmployee(ArgumentMatchers.any())).thenReturn(created);

            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createMap)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("john@techcorp.com"));

            verify(employeeService).addEmployee(ArgumentMatchers.any());
        }
}