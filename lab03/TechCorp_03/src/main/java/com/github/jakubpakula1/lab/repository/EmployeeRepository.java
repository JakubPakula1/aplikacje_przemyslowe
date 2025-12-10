package com.github.jakubpakula1.lab.repository;

import com.github.jakubpakula1.lab.dto.EmployeeListProjection;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Employee> findByCompanyIgnoreCase(String company);

    List<Employee> findAllByOrderBySurnameAsc();

    List<Employee> findByStatus(EmploymentStatus status);


    List<Employee> findByDepartment_Id(Long departmentId);

    @Query("SELECT new com.github.jakubpakula1.lab.dto.EmployeeListProjection(e.id, e.email, e.name, e.surname, e.position, e.department) FROM Employee e")
    Page<EmployeeListProjection> findAllProjected(Pageable pageable);

    @Query("SELECT new com.github.jakubpakula1.lab.dto.EmployeeListProjection(e.id, e.email, e.name, e.surname, e.position, e.department) FROM Employee e WHERE LOWER(e.company) = LOWER(:company)")
    Page<EmployeeListProjection> findByCompanyIgnoreCaseProjected(@Param("company") String company, Pageable pageable);

}
