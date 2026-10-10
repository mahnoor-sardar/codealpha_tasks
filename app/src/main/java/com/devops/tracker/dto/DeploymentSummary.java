package com.devops.tracker.dto;

import com.devops.tracker.domain.DeploymentStatus;
import com.devops.tracker.domain.EnvironmentType;

import java.time.Instant;

public record DeploymentSummary(
        Long id,
        String version,
        EnvironmentType environment,
        DeploymentStatus status,
        Instant deployedAt
) {
}
