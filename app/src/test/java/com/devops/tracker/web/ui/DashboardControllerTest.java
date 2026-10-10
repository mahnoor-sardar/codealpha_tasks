package com.devops.tracker.web.ui;

import com.devops.tracker.dto.ProjectResponse;
import com.devops.tracker.domain.ProjectHealth;
import com.devops.tracker.service.DeploymentService;
import com.devops.tracker.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private DeploymentService deploymentService;

    @Test
    void dashboardRenders() throws Exception {
        when(projectService.listProjects()).thenReturn(List.of(
                new ProjectResponse(1L, "payments-api", "desc", null, "platform", ProjectHealth.HEALTHY, Instant.now(), Instant.now(), null)
        ));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }
}
