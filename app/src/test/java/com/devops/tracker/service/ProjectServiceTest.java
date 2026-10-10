package com.devops.tracker.service;

import com.devops.tracker.domain.Deployment;
import com.devops.tracker.domain.DeploymentStatus;
import com.devops.tracker.domain.EnvironmentType;
import com.devops.tracker.domain.Project;
import com.devops.tracker.domain.ProjectHealth;
import com.devops.tracker.dto.ProjectRequest;
import com.devops.tracker.dto.ProjectResponse;
import com.devops.tracker.exception.DuplicateResourceException;
import com.devops.tracker.exception.ResourceNotFoundException;
import com.devops.tracker.repository.DeploymentRepository;
import com.devops.tracker.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private DeploymentRepository deploymentRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, deploymentRepository);
    }

    @Test
    void createProjectPersistsTrimmedFields() {
        when(projectRepository.existsByNameIgnoreCase("payments-api")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            return project;
        });
        when(deploymentRepository.findFirstByProjectIdOrderByDeployedAtDesc(null)).thenReturn(Optional.empty());

        ProjectResponse response = projectService.createProject(
                new ProjectRequest(" payments-api ", "  Checkout service  ", " https://git.example/payments ", " platform ")
        );

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("payments-api");
        assertThat(captor.getValue().getOwner()).isEqualTo("platform");
        assertThat(response.health()).isEqualTo(ProjectHealth.UNKNOWN);
        assertThat(response.latestDeployment()).isNull();
    }

    @Test
    void createProjectRejectsDuplicateName() {
        when(projectRepository.existsByNameIgnoreCase("payments-api")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(
                new ProjectRequest("payments-api", "desc", null, "owner")
        )).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getProjectUsesLatestDeploymentForHealth() {
        Project project = new Project("inventory-service", "stock", null, "commerce");
        Deployment latest = new Deployment(
                project,
                "0.9.0",
                EnvironmentType.PRODUCTION,
                DeploymentStatus.FAILED,
                Instant.parse("2026-09-04T12:00:00Z"),
                "abc",
                "rollback"
        );
        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));
        when(deploymentRepository.findFirstByProjectIdOrderByDeployedAtDesc(null)).thenReturn(Optional.of(latest));

        ProjectResponse response = projectService.getProject(7L);

        assertThat(response.health()).isEqualTo(ProjectHealth.DEGRADED);
        assertThat(response.latestDeployment().version()).isEqualTo("0.9.0");
    }

    @Test
    void deleteProjectRemovesDeploymentsFirst() {
        Project project = new Project("customer-portal", "ui", null, "frontend");
        when(projectRepository.findById(3L)).thenReturn(Optional.of(project));

        projectService.deleteProject(3L);

        verify(deploymentRepository).deleteByProjectId(3L);
        verify(projectRepository).delete(project);
    }

    @Test
    void getProjectThrowsWhenMissing() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void listProjectsReturnsMappedResults() {
        Project project = new Project("payments-api", "desc", null, "platform");
        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(deploymentRepository.findFirstByProjectIdOrderByDeployedAtDesc(null)).thenReturn(Optional.empty());

        assertThat(projectService.listProjects()).hasSize(1);
        assertThat(projectService.listProjects().getFirst().name()).isEqualTo("payments-api");
    }
}
