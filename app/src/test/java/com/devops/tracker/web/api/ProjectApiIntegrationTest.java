package com.devops.tracker.web.api;

import com.devops.tracker.dto.ProjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void projectLifecycleAndDuplicateName() {
        ResponseEntity<ProjectResponse> created = restTemplate.postForEntity(
                "/api/projects",
                Map.of(
                        "name", "release-board",
                        "description", "Tracks intern project releases",
                        "owner", "devops"
                ),
                ProjectResponse.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        Long id = created.getBody().id();

        ResponseEntity<ProjectResponse> fetched = restTemplate.getForEntity("/api/projects/" + id, ProjectResponse.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().name()).isEqualTo("release-board");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> duplicate = restTemplate.postForEntity(
                "/api/projects",
                new HttpEntity<>(Map.of("name", "Release-Board", "owner", "platform"), headers),
                String.class
        );
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        restTemplate.delete("/api/projects/" + id);
        ResponseEntity<String> missing = restTemplate.getForEntity("/api/projects/" + id, String.class);
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
