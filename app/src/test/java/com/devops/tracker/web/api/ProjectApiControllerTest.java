package com.devops.tracker.web.api;

import com.devops.tracker.domain.ProjectHealth;
import com.devops.tracker.dto.ProjectResponse;
import com.devops.tracker.exception.DuplicateResourceException;
import com.devops.tracker.exception.GlobalExceptionHandler;
import com.devops.tracker.exception.ResourceNotFoundException;
import com.devops.tracker.service.ProjectService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProjectApiController.class)
@Import(GlobalExceptionHandler.class)
class ProjectApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void listProjectsReturnsOk() throws Exception {
        when(projectService.listProjects()).thenReturn(List.of(
                new ProjectResponse(1L, "payments-api", "desc", null, "platform", ProjectHealth.HEALTHY, Instant.now(), Instant.now(), null)
        ));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("payments-api"));
    }

    @Test
    void createProjectReturnsCreated() throws Exception {
        when(projectService.createProject(any())).thenReturn(
                new ProjectResponse(5L, "inventory-service", "stock", null, "commerce", ProjectHealth.UNKNOWN, Instant.now(), Instant.now(), null)
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "inventory-service",
                                  "description": "stock",
                                  "owner": "commerce"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/projects/5"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void createProjectValidatesMissingName() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "owner": "platform"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").exists());
    }

    @Test
    void createProjectReturnsConflictForDuplicate() throws Exception {
        when(projectService.createProject(any())).thenThrow(
                new DuplicateResourceException("A project named 'payments-api' already exists")
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "payments-api",
                                  "owner": "platform"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A project named 'payments-api' already exists"));
    }

    @Test
    void getProjectRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/projects/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for 'id'"));
    }

    @Test
    void getProjectReturnsNotFound() throws Exception {
        when(projectService.getProject(42L)).thenThrow(new ResourceNotFoundException("Project 42 was not found"));

        mockMvc.perform(get("/api/projects/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateProjectReturnsOk() throws Exception {
        when(projectService.updateProject(eq(1L), any())).thenReturn(
                new ProjectResponse(1L, "payments-api", "updated", null, "platform", ProjectHealth.UNKNOWN, Instant.now(), Instant.now(), null)
        );

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "payments-api",
                                  "description": "updated",
                                  "owner": "platform"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("updated"));
    }

    @Test
    void deleteProjectReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMissingProjectReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Project 9 was not found")).when(projectService).deleteProject(9L);

        mockMvc.perform(delete("/api/projects/9"))
                .andExpect(status().isNotFound());
    }
}
