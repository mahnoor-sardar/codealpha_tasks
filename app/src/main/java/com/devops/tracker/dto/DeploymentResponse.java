package com.devops.tracker.dto;

import com.devops.tracker.domain.DeploymentStatus;
import com.devops.tracker.domain.EnvironmentType;

import java.time.Instant;

public record DeploymentResponse(
        Long id,
        Long projectId,
        String projectName,
        String version,
        EnvironmentType environment,
        DeploymentStatus status,
        Instant deployedAt,
        String gitReference,
        String notes,
        Instant createdAt
) {
}
