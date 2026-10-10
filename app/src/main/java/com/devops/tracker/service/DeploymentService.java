package com.devops.tracker.service;

import com.devops.tracker.domain.Deployment;
import com.devops.tracker.domain.Project;
import com.devops.tracker.dto.DeploymentRequest;
import com.devops.tracker.dto.DeploymentResponse;
import com.devops.tracker.dto.DeploymentStatusUpdateRequest;
import com.devops.tracker.exception.ResourceNotFoundException;
import com.devops.tracker.repository.DeploymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class DeploymentService {

    private static final Logger log = LoggerFactory.getLogger(DeploymentService.class);

    private final DeploymentRepository deploymentRepository;
    private final ProjectService projectService;

    public DeploymentService(DeploymentRepository deploymentRepository, ProjectService projectService) {
        this.deploymentRepository = deploymentRepository;
        this.projectService = projectService;
    }

    @Transactional(readOnly = true)
    public List<DeploymentResponse> listByProject(Long projectId) {
        projectService.requireProject(projectId);
        return deploymentRepository.findByProjectIdOrderByDeployedAtDesc(projectId).stream()
                .map(EntityMapper::toDeploymentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeploymentResponse getDeployment(Long id) {
        return EntityMapper.toDeploymentResponse(requireDeployment(id));
    }

    @Transactional
    public DeploymentResponse recordDeployment(Long projectId, DeploymentRequest request) {
        Project project = projectService.requireProject(projectId);
        Instant deployedAt = request.getDeployedAt() == null ? Instant.now() : request.getDeployedAt();
        Deployment deployment = new Deployment(
                project,
                request.getVersion().trim(),
                request.getEnvironment(),
                request.getStatus(),
                deployedAt,
                blankToNull(request.getGitReference()),
                blankToNull(request.getNotes())
        );
        Deployment saved = deploymentRepository.save(deployment);
        log.info(
                "Recorded deployment id={} projectId={} version={} environment={} status={}",
                saved.getId(),
                projectId,
                saved.getVersion(),
                saved.getEnvironment(),
                saved.getStatus()
        );
        return EntityMapper.toDeploymentResponse(saved);
    }

    @Transactional
    public DeploymentResponse updateStatus(Long id, DeploymentStatusUpdateRequest request) {
        Deployment deployment = requireDeployment(id);
        deployment.setStatus(request.getStatus());
        if (request.getNotes() != null) {
            deployment.setNotes(blankToNull(request.getNotes()));
        }
        log.info("Updated deployment id={} status={}", id, request.getStatus());
        return EntityMapper.toDeploymentResponse(deployment);
    }

    private Deployment requireDeployment(Long id) {
        return deploymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment " + id + " was not found"));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
