package com.devops.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 120, message = "Project name must be 120 characters or fewer")
    private String name;

    @Size(max = 500, message = "Description must be 500 characters or fewer")
    private String description;

    @Size(max = 300, message = "Repository URL must be 300 characters or fewer")
    private String repositoryUrl;

    @NotBlank(message = "Owner is required")
    @Size(max = 120, message = "Owner must be 120 characters or fewer")
    private String owner;

    public ProjectRequest() {
    }

    public ProjectRequest(String name, String description, String repositoryUrl, String owner) {
        this.name = name;
        this.description = description;
        this.repositoryUrl = repositoryUrl;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
