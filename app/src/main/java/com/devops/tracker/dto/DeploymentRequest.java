package com.devops.tracker.dto;

import com.devops.tracker.domain.DeploymentStatus;
import com.devops.tracker.domain.EnvironmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class DeploymentRequest {

    @NotBlank(message = "Version is required")
    @Size(max = 64, message = "Version must be 64 characters or fewer")
    private String version;

    @NotNull(message = "Environment is required")
    private EnvironmentType environment;

    @NotNull(message = "Status is required")
    private DeploymentStatus status = DeploymentStatus.PENDING;

    private Instant deployedAt;

    @Size(max = 120, message = "Git reference must be 120 characters or fewer")
    private String gitReference;

    @Size(max = 1000, message = "Notes must be 1000 characters or fewer")
    private String notes;

    public DeploymentRequest() {
    }

    public DeploymentRequest(
            String version,
            EnvironmentType environment,
            DeploymentStatus status,
            Instant deployedAt,
            String gitReference,
            String notes
    ) {
        this.version = version;
        this.environment = environment;
        this.status = status;
        this.deployedAt = deployedAt;
        this.gitReference = gitReference;
        this.notes = notes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public EnvironmentType getEnvironment() {
        return environment;
    }

    public void setEnvironment(EnvironmentType environment) {
        this.environment = environment;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public Instant getDeployedAt() {
        return deployedAt;
    }

    public void setDeployedAt(Instant deployedAt) {
        this.deployedAt = deployedAt;
    }

    public String getGitReference() {
        return gitReference;
    }

    public void setGitReference(String gitReference) {
        this.gitReference = gitReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
