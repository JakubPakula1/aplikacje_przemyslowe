package com.github.jakubpakula1.lab.repository;

import com.github.jakubpakula1.lab.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
