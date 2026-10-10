package com.devops.tracker.service;

import com.devops.tracker.domain.Deployment;
import com.devops.tracker.domain.DeploymentStatus;
import com.devops.tracker.domain.EnvironmentType;
import com.devops.tracker.domain.Project;
import com.devops.tracker.dto.DeploymentRequest;
import com.devops.tracker.dto.DeploymentResponse;
import com.devops.tracker.dto.DeploymentStatusUpdateRequest;
import com.devops.tracker.exception.ResourceNotFoundException;
import com.devops.tracker.repository.DeploymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentServiceTest {

    @Mock
    private DeploymentRepository deploymentRepository;

    @Mock
    private ProjectService projectService;

    private DeploymentService deploymentService;
    private Project project;

    @BeforeEach
    void setUp() {
        deploymentService = new DeploymentService(deploymentRepository, projectService);
        project = new Project("payments-api", "payments", "https://git.example/payments", "platform");
    }

    @Test
    void recordDeploymentUsesCurrentTimeWhenTimestampMissing() {
        when(projectService.requireProject(1L)).thenReturn(project);
        when(deploymentRepository.save(any(Deployment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeploymentRequest request = new DeploymentRequest(
                "1.2.0",
                EnvironmentType.STAGING,
                DeploymentStatus.RUNNING,
                null,
                "c81b0ee",
                "canary"
        );

        DeploymentResponse response = deploymentService.recordDeployment(1L, request);

        assertThat(response.version()).isEqualTo("1.2.0");
        assertThat(response.environment()).isEqualTo(EnvironmentType.STAGING);
        assertThat(response.status()).isEqualTo(DeploymentStatus.RUNNING);
        assertThat(response.deployedAt()).isNotNull();
        assertThat(response.projectName()).isEqualTo("payments-api");
    }

    @Test
    void updateStatusChangesDeploymentState() {
        Deployment deployment = new Deployment(
                project,
                "1.2.0",
                EnvironmentType.PRODUCTION,
                DeploymentStatus.RUNNING,
                Instant.parse("2026-09-04T10:00:00Z"),
                "c81b0ee",
                "in progress"
        );
        when(deploymentRepository.findById(4L)).thenReturn(Optional.of(deployment));

        DeploymentResponse response = deploymentService.updateStatus(
                4L,
                new DeploymentStatusUpdateRequest(DeploymentStatus.SUCCESSFUL, "cutover complete")
        );

        assertThat(response.status()).isEqualTo(DeploymentStatus.SUCCESSFUL);
        assertThat(response.notes()).isEqualTo("cutover complete");
    }

    @Test
    void getDeploymentThrowsWhenMissing() {
        when(deploymentRepository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deploymentService.getDeployment(44L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listByProjectReturnsNewestFirstFromRepository() {
        when(projectService.requireProject(1L)).thenReturn(project);
        Deployment first = new Deployment(
                project,
                "1.1.0",
                EnvironmentType.DEVELOPMENT,
                DeploymentStatus.SUCCESSFUL,
                Instant.parse("2026-09-01T10:00:00Z"),
                null,
                null
        );
        when(deploymentRepository.findByProjectIdOrderByDeployedAtDesc(1L)).thenReturn(List.of(first));

        assertThat(deploymentService.listByProject(1L)).hasSize(1);
        assertThat(deploymentService.listByProject(1L).getFirst().version()).isEqualTo("1.1.0");
    }
}
