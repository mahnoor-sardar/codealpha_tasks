package com.devops.tracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "deployments")
public class Deployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 64)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EnvironmentType environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeploymentStatus status;

    @Column(nullable = false)
    private Instant deployedAt;

    @Column(length = 120)
    private String gitReference;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Deployment() {
    }

    public Deployment(
            Project project,
            String version,
            EnvironmentType environment,
            DeploymentStatus status,
            Instant deployedAt,
            String gitReference,
            String notes
    ) {
        this.project = project;
        this.version = version;
        this.environment = environment;
        this.status = status;
        this.deployedAt = deployedAt;
        this.gitReference = gitReference;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (deployedAt == null) {
            deployedAt = createdAt;
        }
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getVersion() {
        return version;
    }

    public EnvironmentType getEnvironment() {
        return environment;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public Instant getDeployedAt() {
        return deployedAt;
    }

    public String getGitReference() {
        return gitReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
