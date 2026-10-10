package com.devops.tracker.dto;

import com.devops.tracker.domain.ProjectHealth;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        String repositoryUrl,
        String owner,
        ProjectHealth health,
        Instant createdAt,
        Instant updatedAt,
        DeploymentSummary latestDeployment
) {
}
