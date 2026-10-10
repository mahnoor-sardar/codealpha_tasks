package com.devops.tracker.repository;

import com.devops.tracker.domain.Deployment;
import com.devops.tracker.domain.EnvironmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

    List<Deployment> findByProjectIdOrderByDeployedAtDesc(Long projectId);

    Optional<Deployment> findFirstByProjectIdOrderByDeployedAtDesc(Long projectId);

    Optional<Deployment> findFirstByProjectIdAndEnvironmentOrderByDeployedAtDesc(
            Long projectId,
            EnvironmentType environment
    );

    void deleteByProjectId(Long projectId);
}
