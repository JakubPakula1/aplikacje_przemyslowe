package com.github.jakubpakula1.lab.service;

import com.github.jakubpakula1.lab.model.AuditLog;
import com.github.jakubpakula1.lab.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent(String message){
        AuditLog log = new AuditLog(LocalDateTime.now(), message);
        auditLogRepository.save(log);
    }
}
