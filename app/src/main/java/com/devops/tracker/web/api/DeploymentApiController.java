package com.devops.tracker.web.api;

import com.devops.tracker.dto.DeploymentRequest;
import com.devops.tracker.dto.DeploymentResponse;
import com.devops.tracker.dto.DeploymentStatusUpdateRequest;
import com.devops.tracker.service.DeploymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
public class DeploymentApiController {

    private final DeploymentService deploymentService;

    public DeploymentApiController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @GetMapping("/api/projects/{projectId}/deployments")
    public List<DeploymentResponse> listForProject(@PathVariable Long projectId) {
        return deploymentService.listByProject(projectId);
    }

    @PostMapping("/api/projects/{projectId}/deployments")
    public ResponseEntity<DeploymentResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody DeploymentRequest request
    ) {
        DeploymentResponse created = deploymentService.recordDeployment(projectId, request);
        return ResponseEntity.created(URI.create("/api/deployments/" + created.id())).body(created);
    }

    @GetMapping("/api/deployments/{id}")
    public DeploymentResponse get(@PathVariable Long id) {
        return deploymentService.getDeployment(id);
    }

    @PatchMapping("/api/deployments/{id}")
    public DeploymentResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody DeploymentStatusUpdateRequest request
    ) {
        return deploymentService.updateStatus(id, request);
    }
}
