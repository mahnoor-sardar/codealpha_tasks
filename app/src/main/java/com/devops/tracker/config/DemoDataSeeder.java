package com.devops.tracker.config;

import com.devops.tracker.domain.Deployment;
import com.devops.tracker.domain.DeploymentStatus;
import com.devops.tracker.domain.EnvironmentType;
import com.devops.tracker.domain.Project;
import com.devops.tracker.repository.DeploymentRepository;
import com.devops.tracker.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final ProjectRepository projectRepository;
    private final DeploymentRepository deploymentRepository;

    public DemoDataSeeder(ProjectRepository projectRepository, DeploymentRepository deploymentRepository) {
        this.projectRepository = projectRepository;
        this.deploymentRepository = deploymentRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (projectRepository.count() > 0) {
            return;
        }

        Project payments = projectRepository.save(new Project(
                "payments-api",
                "Handles checkout, refunds, and settlement webhooks.",
                "https://github.com/example/payments-api",
                "platform-team"
        ));
        Project portal = projectRepository.save(new Project(
                "customer-portal",
                "Public web portal for account management and billing history.",
                "https://github.com/example/customer-portal",
                "frontend-team"
        ));
        Project inventory = projectRepository.save(new Project(
                "inventory-service",
                "Stock levels, warehouse sync, and allocation for orders.",
                "https://github.com/example/inventory-service",
                "commerce-team"
        ));

        Instant now = Instant.now();
        deploymentRepository.save(new Deployment(
                payments,
                "1.8.2",
                EnvironmentType.PRODUCTION,
                DeploymentStatus.SUCCESSFUL,
                now.minus(2, ChronoUnit.HOURS),
                "a4f91c2",
                "Blue/green cutover completed."
        ));
        deploymentRepository.save(new Deployment(
                payments,
                "1.9.0-rc.1",
                EnvironmentType.STAGING,
                DeploymentStatus.RUNNING,
                now.minus(20, ChronoUnit.MINUTES),
                "c81b0ee",
                "Canary at 10% traffic."
        ));
        deploymentRepository.save(new Deployment(
                portal,
                "3.4.1",
                EnvironmentType.PRODUCTION,
                DeploymentStatus.FAILED,
                now.minus(6, ChronoUnit.HOURS),
                "e2b7710",
                "CDN cache invalidation timed out."
        ));
        deploymentRepository.save(new Deployment(
                portal,
                "3.4.1",
                EnvironmentType.STAGING,
                DeploymentStatus.SUCCESSFUL,
                now.minus(8, ChronoUnit.HOURS),
                "e2b7710",
                "Staging smoke tests passed."
        ));
        deploymentRepository.save(new Deployment(
                inventory,
                "0.12.0",
                EnvironmentType.DEVELOPMENT,
                DeploymentStatus.PENDING,
                now.minus(45, ChronoUnit.MINUTES),
                "feature/sync-retry",
                "Waiting for QA approval."
        ));

        log.info("Loaded demo projects and deployments for local demonstration");
    }
}
