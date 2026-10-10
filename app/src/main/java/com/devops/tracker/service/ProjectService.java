package com.devops.tracker.service;

import com.devops.tracker.domain.Deployment;
import com.devops.tracker.domain.Project;
import com.devops.tracker.dto.ProjectRequest;
import com.devops.tracker.dto.ProjectResponse;
import com.devops.tracker.exception.DuplicateResourceException;
import com.devops.tracker.exception.ResourceNotFoundException;
import com.devops.tracker.repository.DeploymentRepository;
import com.devops.tracker.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final DeploymentRepository deploymentRepository;

    public ProjectService(ProjectRepository projectRepository, DeploymentRepository deploymentRepository) {
        this.projectRepository = projectRepository;
        this.deploymentRepository = deploymentRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id) {
        return toResponse(requireProject(id));
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        String name = request.getName().trim();
        if (projectRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A project named '" + name + "' already exists");
        }
        Project project = new Project(
                name,
                blankToNull(request.getDescription()),
                blankToNull(request.getRepositoryUrl()),
                request.getOwner().trim()
        );
        Project saved = projectRepository.save(project);
        log.info("Created project id={} name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = requireProject(id);
        String name = request.getName().trim();
        if (projectRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("A project named '" + name + "' already exists");
        }
        project.setName(name);
        project.setDescription(blankToNull(request.getDescription()));
        project.setRepositoryUrl(blankToNull(request.getRepositoryUrl()));
        project.setOwner(request.getOwner().trim());
        log.info("Updated project id={}", id);
        return toResponse(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = requireProject(id);
        deploymentRepository.deleteByProjectId(id);
        projectRepository.delete(project);
        log.info("Deleted project id={} name={}", id, project.getName());
    }

    Project requireProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project " + id + " was not found"));
    }

    private ProjectResponse toResponse(Project project) {
        Deployment latest = deploymentRepository.findFirstByProjectIdOrderByDeployedAtDesc(project.getId())
                .orElse(null);
        return EntityMapper.toProjectResponse(project, latest);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
