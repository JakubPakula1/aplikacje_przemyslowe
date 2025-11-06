package com.github.jakubpakula1.lab.dto;

import com.github.jakubpakula1.lab.model.EmploymentStatus;

public class StatusUpdateDTO {
    private EmploymentStatus status;

    public StatusUpdateDTO() {}

    public StatusUpdateDTO(EmploymentStatus status) {
        this.status = status;
    }

    public EmploymentStatus getStatus() { return status; }
    public void setStatus(EmploymentStatus status) { this.status = status; }
}