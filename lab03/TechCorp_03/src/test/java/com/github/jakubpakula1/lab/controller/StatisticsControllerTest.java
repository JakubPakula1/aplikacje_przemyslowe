package com.github.jakubpakula1.lab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jakubpakula1.lab.dto.CompanyStatisticsDTO;
import com.github.jakubpakula1.lab.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.ArgumentMatchers;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void getAverageSalary_noCompany_returnsAverage() throws Exception {
        when(employeeService.getAverageSalary(ArgumentMatchers.isNull())).thenReturn(5000.0);

        mockMvc.perform(get("/api/statistics/salary/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(5000.0));

        verify(employeeService).getAverageSalary((String) null);
    }

    @Test
    void getAverageSalary_withCompany_returnsAverage() throws Exception {
        when(employeeService.getAverageSalary("Acme")).thenReturn(6000.0);

        mockMvc.perform(get("/api/statistics/salary/average").param("company", "Acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary").value(6000.0));

        verify(employeeService).getAverageSalary("Acme");
    }

    @Test
    void getCompanyStatistics_found_returnsDto() throws Exception {
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO("Acme", 2, 5500.0, 6000.0, "Jane Doe");
        when(employeeService.getCompanyStatisticsDTO("Acme")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/statistics/company/Acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Acme"))
                .andExpect(jsonPath("$.employeeCount").value(2))
                .andExpect(jsonPath("$.averageSalary").value(5500.0))
                .andExpect(jsonPath("$.highestSalary").value(6000.0))
                .andExpect(jsonPath("$.topEarnerName").value("Jane Doe"));

        verify(employeeService).getCompanyStatisticsDTO("Acme");
    }

    @Test
    void getCompanyStatistics_notFound_returns404() throws Exception {
        when(employeeService.getCompanyStatisticsDTO("Nope")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/statistics/company/Nope"))
                .andExpect(status().isNotFound());

        verify(employeeService).getCompanyStatisticsDTO("Nope");
    }

    @Test
    void getPositionStatistics_returnsMap() throws Exception {
        Map<String, Integer> positions = Map.of("PROGRAMISTA", 2, "MANAGER", 1);
        when(employeeService.getPositionStatistics()).thenReturn(positions);

        mockMvc.perform(get("/api/statistics/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PROGRAMISTA").value(2))
                .andExpect(jsonPath("$.MANAGER").value(1));

        verify(employeeService).getPositionStatistics();
    }

    @Test
    void getStatusDistribution_returnsMap() throws Exception {
        Map<String, Integer> distribution = Map.of("ACTIVE", 3, "ON_LEAVE", 1);
        when(employeeService.getStatusDistribution()).thenReturn(distribution);

        mockMvc.perform(get("/api/statistics/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ACTIVE").value(3))
                .andExpect(jsonPath("$.ON_LEAVE").value(1));

        verify(employeeService).getStatusDistribution();
    }
}