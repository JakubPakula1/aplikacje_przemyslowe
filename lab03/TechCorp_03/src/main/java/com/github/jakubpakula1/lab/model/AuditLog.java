package com.github.jakubpakula1.lab.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    protected AuditLog() {
    }

    public AuditLog(LocalDateTime eventDate, String message) {
        this.eventDate = eventDate;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public String getMessage() {
        return message;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}