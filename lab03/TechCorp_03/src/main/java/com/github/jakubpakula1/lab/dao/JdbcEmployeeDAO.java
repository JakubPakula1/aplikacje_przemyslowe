package com.github.jakubpakula1.lab.dao;

import com.github.jakubpakula1.lab.model.CompanyStatistics;
import com.github.jakubpakula1.lab.model.Employee;
import com.github.jakubpakula1.lab.model.EmploymentStatus;
import com.github.jakubpakula1.lab.model.Position;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcEmployeeDAO implements EmployeeDAO {
    private final JdbcTemplate jdbcTemplate;

    public JdbcEmployeeDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Employee> employeeRowMapper = (rs, rowNum) -> {
        Employee employee = new Employee();
        employee.setId(rs.getLong("id"));
        employee.setName(rs.getString("first_name"));
        employee.setSurname(rs.getString("last_name"));
        employee.setEmail(rs.getString("email"));
        employee.setCompany(rs.getString("company"));
        employee.setPosition(Position.valueOf(rs.getString("position")));
        employee.setSalary(rs.getInt("salary"));
        employee.setStatus(EmploymentStatus.valueOf(rs.getString("status")));
        employee.setPhotoFileName(rs.getString("photo_file_name"));
        employee.setDepartmentId(rs.getLong("department_id"));
        return employee;
    };

    @Override
    public List<Employee> findAll() {
        String sql = "SELECT * FROM employees";
        List<Employee> results = jdbcTemplate.query(sql, employeeRowMapper);
        return results != null ? results : Collections.emptyList();
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        String sql = "SELECT * FROM employees WHERE email = ?";
        List<Employee> results = jdbcTemplate.query(sql, employeeRowMapper, email);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void save(Employee employee) {
        String sql = "INSERT INTO employees (first_name, last_name, email, company, position, salary, status, photo_file_name, department_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            employee.getName(),
            employee.getSurname(),
            employee.getEmail(),
            employee.getCompany(),
            employee.getPosition().name(),
            employee.getSalary(),
            employee.getStatus().name(),
            employee.getPhotoFileName(),
            employee.getDepartmentId());
    }

    @Override
    public void delete(String email) {
        String sql = "DELETE FROM employees WHERE email = ?";
        jdbcTemplate.update(sql, email);
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM employees";
        jdbcTemplate.update(sql);
    }

    @Override
    public List<CompanyStatistics> getCompanyStatistics() {
        String sql = "SELECT company, COUNT(*) as employee_count, AVG(salary) as avg_salary, " +
                "(SELECT CONCAT(first_name, ' ', last_name) FROM employees e2 " +
                "WHERE e2.company = e1.company ORDER BY e2.salary DESC LIMIT 1) as best_earning_name " +
                "FROM employees e1 " +
                "GROUP BY company " +
                "ORDER BY company";

        List<CompanyStatistics> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            String company = rs.getString("company");
            int employeeCount = rs.getInt("employee_count");
            double avgSalary = rs.getDouble("avg_salary");
            String bestEarningName = rs.getString("best_earning_name");

            return new CompanyStatistics(company, employeeCount, avgSalary, bestEarningName);
        });

        return results != null ? results : Collections.emptyList();
    }
}