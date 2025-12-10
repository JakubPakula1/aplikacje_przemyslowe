package com.github.jakubpakula1.lab.repository;

import com.github.jakubpakula1.lab.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
