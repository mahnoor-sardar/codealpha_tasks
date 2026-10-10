package com.devops.tracker.service;

import com.devops.tracker.domain.Deployment;
import com.devops.tracker.domain.DeploymentStatus;
import com.devops.tracker.domain.Project;
import com.devops.tracker.domain.ProjectHealth;
import com.devops.tracker.dto.DeploymentResponse;
import com.devops.tracker.dto.DeploymentSummary;
import com.devops.tracker.dto.ProjectResponse;

final class EntityMapper {

    private EntityMapper() {
    }

    static ProjectResponse toProjectResponse(Project project, Deployment latest) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getRepositoryUrl(),
                project.getOwner(),
                deriveHealth(latest),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                latest == null ? null : toSummary(latest)
        );
    }

    static DeploymentResponse toDeploymentResponse(Deployment deployment) {
        Project project = deployment.getProject();
        return new DeploymentResponse(
                deployment.getId(),
                project.getId(),
                project.getName(),
                deployment.getVersion(),
                deployment.getEnvironment(),
                deployment.getStatus(),
                deployment.getDeployedAt(),
                deployment.getGitReference(),
                deployment.getNotes(),
                deployment.getCreatedAt()
        );
    }

    static DeploymentSummary toSummary(Deployment deployment) {
        return new DeploymentSummary(
                deployment.getId(),
                deployment.getVersion(),
                deployment.getEnvironment(),
                deployment.getStatus(),
                deployment.getDeployedAt()
        );
    }

    static ProjectHealth deriveHealth(Deployment latest) {
        if (latest == null) {
            return ProjectHealth.UNKNOWN;
        }
        if (latest.getStatus() == DeploymentStatus.FAILED) {
            return ProjectHealth.DEGRADED;
        }
        if (latest.getStatus() == DeploymentStatus.SUCCESSFUL) {
            return ProjectHealth.HEALTHY;
        }
        return ProjectHealth.UNKNOWN;
    }
}
