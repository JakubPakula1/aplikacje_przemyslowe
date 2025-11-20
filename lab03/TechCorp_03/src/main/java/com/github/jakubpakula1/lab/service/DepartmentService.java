package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.dto.CreateDepartmentDTO;
import com.github.jakubpakula1.lab.model.Department;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DepartmentService {
    private final Map<Long, Department> departmentMap;
    private long nextId = 1;
    public DepartmentService() {
        this.departmentMap = new ConcurrentHashMap<>();
    }
    public void addDepartment(CreateDepartmentDTO createDepartmentDTO) {
        Department department = new Department(
                nextId++,
                createDepartmentDTO.getName(),
                createDepartmentDTO.getLocation(),
                createDepartmentDTO.getBudget(),
                createDepartmentDTO.getManagerEmail()
        );
        departmentMap.put(department.getId(), department);
    }

    public List<Department> getAllDepartments() {
        return List.copyOf(departmentMap.values());
    }

    public Department getDepartmentById(Long id){
        return departmentMap.get(id);
    }

    public Department updateDepartment(Long id, CreateDepartmentDTO createDepartmentDTO) {
        Department department = departmentMap.get(id);
        if (department != null) {
            department.setName(createDepartmentDTO.getName());
            department.setLocation(createDepartmentDTO.getLocation());
            department.setBudget(createDepartmentDTO.getBudget());
            department.setManagerEmail(createDepartmentDTO.getManagerEmail());
        }
        return department;
    }

    public Department deleteDepartment(Long id){
        Department departmentToDelete = departmentMap.get(id);
        departmentMap.remove(id);
        return departmentToDelete;
    }
}
