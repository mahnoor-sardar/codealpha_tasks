package com.devops.tracker.dto;

import com.devops.tracker.domain.DeploymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DeploymentStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private DeploymentStatus status;

    @Size(max = 1000, message = "Notes must be 1000 characters or fewer")
    private String notes;

    public DeploymentStatusUpdateRequest() {
    }

    public DeploymentStatusUpdateRequest(DeploymentStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
