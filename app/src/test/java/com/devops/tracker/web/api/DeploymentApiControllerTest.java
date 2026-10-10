package com.devops.tracker.web.api;

import com.devops.tracker.domain.DeploymentStatus;
import com.devops.tracker.domain.EnvironmentType;
import com.devops.tracker.dto.DeploymentResponse;
import com.devops.tracker.exception.GlobalExceptionHandler;
import com.devops.tracker.exception.ResourceNotFoundException;
import com.devops.tracker.service.DeploymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeploymentApiController.class)
@Import(GlobalExceptionHandler.class)
class DeploymentApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeploymentService deploymentService;

    @Test
    void createDeploymentReturnsCreated() throws Exception {
        when(deploymentService.recordDeployment(eq(1L), any())).thenReturn(
                new DeploymentResponse(
                        10L,
                        1L,
                        "payments-api",
                        "1.4.0",
                        EnvironmentType.PRODUCTION,
                        DeploymentStatus.SUCCESSFUL,
                        Instant.parse("2026-09-04T12:00:00Z"),
                        "a4f91c2",
                        "cutover",
                        Instant.parse("2026-09-04T12:00:00Z")
                )
        );

        mockMvc.perform(post("/api/projects/1/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "version": "1.4.0",
                                  "environment": "PRODUCTION",
                                  "status": "SUCCESSFUL",
                                  "gitReference": "a4f91c2"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value("1.4.0"));
    }

    @Test
    void createDeploymentValidatesMissingEnvironment() throws Exception {
        mockMvc.perform(post("/api/projects/1/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "version": "1.4.0",
                                  "status": "PENDING"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void listDeploymentsReturnsNotFoundForUnknownProject() throws Exception {
        when(deploymentService.listByProject(88L)).thenThrow(new ResourceNotFoundException("Project 88 was not found"));

        mockMvc.perform(get("/api/projects/88/deployments"))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchDeploymentUpdatesStatus() throws Exception {
        when(deploymentService.updateStatus(eq(10L), any())).thenReturn(
                new DeploymentResponse(
                        10L,
                        1L,
                        "payments-api",
                        "1.4.0",
                        EnvironmentType.PRODUCTION,
                        DeploymentStatus.FAILED,
                        Instant.parse("2026-09-04T12:00:00Z"),
                        "a4f91c2",
                        "health check failed",
                        Instant.parse("2026-09-04T12:00:00Z")
                )
        );

        mockMvc.perform(patch("/api/deployments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "FAILED",
                                  "notes": "health check failed"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void listDeploymentsReturnsOk() throws Exception {
        when(deploymentService.listByProject(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/projects/1/deployments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
