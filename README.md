# DevOps Project Deployment Tracker

A Java web application for recording software projects and the deployments that move them through development, staging, and production. It is the CodeAlpha DevOps internship project: a Gradle-built Spring Boot service with automated tests, a Docker image, and an Azure Pipelines definition that can be connected later.

This repository is Phase 1 (local application). Azure resources are documented, not provisioned.

## Problem statement

Internship and small-team delivery work often lives in chat messages and spreadsheets: which service is in production, which release failed, and who owns the repo. This tracker gives a single, inspectable place for that operational picture so a CI pipeline and a container platform have a real application to build, test, and deploy.

## Features

- Dashboard of projects with latest version, environment, deployment status, and a derived health indicator
- Create, view, update, and delete projects
- Record deployments (version, environment, status, timestamp, optional git reference and notes)
- Update deployment status through the API
- JSON REST API for automation
- Server-rendered UI for walkthroughs
- `/actuator/health` for Docker and Azure readiness checks
- Optional demo data for a non-empty first run

Health on the dashboard is derived from the latest deployment:

| Latest deployment status | Project health |
| --- | --- |
| `SUCCESSFUL` | `HEALTHY` |
| `FAILED` | `DEGRADED` |
| `PENDING`, `RUNNING`, or no deployments | `UNKNOWN` |

## Architecture

```
Browser / API client
        │
   Spring MVC / REST controllers
        │
      Services
        │
    Spring Data JPA repositories
        │
     H2 database (local default)
```

Configuration is injected from environment variables (and `application.yml` defaults). Actuator exposes health and info only.

## Technology stack

- Java 21
- Gradle 9.7.1 (Wrapper)
- Spring Boot 3.5 (Web, Thymeleaf, Data JPA, Validation, Actuator)
- H2 in-memory database
- JUnit 5 and Spring Boot Test
- Docker (multi-stage image)

## Project structure

```
devops-project-deployment-tracker/
├── app/
│   ├── build.gradle
│   └── src/
│       ├── main/java/com/devops/tracker/   # application, domain, API, UI
│       ├── main/resources/                 # config, templates, CSS
│       └── test/java/                      # service, API, health, UI tests
├── gradle/wrapper/
├── gradlew / gradlew.bat
├── settings.gradle
├── Dockerfile
├── docker-compose.yml
├── azure-pipelines.yml
└── README.md
```

## Local setup

Requirements:

- Windows 11 (commands below are for PowerShell)
- Java 21 (Eclipse Temurin recommended)
- Docker Desktop if you want to run the container later (Phase 2). Docker has not been verified in this environment yet.
- Git

Confirm Java:

```powershell
java -version
```

You do not need a global Gradle install. Use the Wrapper from the repository root.

## Running with Gradle

From `D:\DevOps-Project-Deployment-Tracker`:

```powershell
.\gradlew.bat bootRun
```

Then open [http://localhost:8080](http://localhost:8080).

The process listens on `SERVER_PORT` (default `8080`). Local Gradle runs load demo projects when `APP_SEED_DATA` is unset or `true`. The Docker image defaults `APP_SEED_DATA` to `false`; `docker-compose.yml` turns it back on for a local demo.

## Running tests

```powershell
.\gradlew.bat clean test
```

## Building the application

```powershell
.\gradlew.bat clean build
```

The runnable JAR is:

`app\build\libs\deployment-tracker.jar`

Run it without Gradle:

```powershell
java -jar app\build\libs\deployment-tracker.jar
```

## Running with Docker

These files are prepared (`Dockerfile`, `.dockerignore`, `docker-compose.yml`). **Building and running the image has not been verified yet** — Docker Desktop was not available during Phase 1. Confirm the engine is running, then:

```powershell
docker compose up --build
```

Or without Compose (add `-e APP_SEED_DATA=true` if you want the sample projects):

```powershell
docker build -t devops-project-deployment-tracker:local .
docker run --name deployment-tracker -p 8080:8080 devops-project-deployment-tracker:local
```

Keep `SERVER_PORT=8080` inside the container. Change only the **host** side of `-p` / Compose `ports` if 8080 is taken on Windows.

Useful lifecycle commands (after Compose is up):

```powershell
docker compose ps
docker compose logs -f deployment-tracker
docker compose stop
docker compose start
docker compose down
```

## Docker health check

The image and Compose file are written to probe `GET /actuator/health` (HTTP 200 when the app is `UP`). **Container health status has not been observed yet** (Phase 2).

After the container is running, inspect:

```powershell
docker inspect --format="{{json .State.Health}}" deployment-tracker
```

A healthy container reports `"Status":"healthy"` after the start period. If it stays `unhealthy`, read `docker logs deployment-tracker` and confirm port `8080` is free on the host.

## API overview

Base URL: `http://localhost:8080`

| Method | Path | Description |
| --- | --- | --- |
| GET | `/actuator/health` | Application health |
| GET | `/api/projects` | List projects |
| POST | `/api/projects` | Create project |
| GET | `/api/projects/{id}` | Get project |
| PUT | `/api/projects/{id}` | Update project |
| DELETE | `/api/projects/{id}` | Delete project and its deployments |
| GET | `/api/projects/{id}/deployments` | List deployments for a project |
| POST | `/api/projects/{id}/deployments` | Record a deployment |
| GET | `/api/deployments/{id}` | Get one deployment |
| PATCH | `/api/deployments/{id}` | Update status and notes |

Example create project:

```powershell
curl.exe -X POST http://localhost:8080/api/projects -H "Content-Type: application/json" -d "{\"name\":\"payments-api\",\"owner\":\"platform-team\",\"description\":\"Checkout and refunds\"}"
```

Example record deployment:

```powershell
curl.exe -X POST http://localhost:8080/api/projects/1/deployments -H "Content-Type: application/json" -d "{\"version\":\"1.2.0\",\"environment\":\"STAGING\",\"status\":\"RUNNING\",\"gitReference\":\"c81b0ee\"}"
```

Environments: `DEVELOPMENT`, `STAGING`, `PRODUCTION`.  
Statuses: `PENDING`, `RUNNING`, `SUCCESSFUL`, `FAILED`.

Validation failures return HTTP 400 with field details. Unknown IDs return 404. Duplicate project names return 409.

## Configuration / environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP port |
| `APP_SEED_DATA` | `true` for local Gradle; `false` in the Docker image (`true` in Compose) | Load demo projects on empty database |
| `APP_LOG_LEVEL` | `INFO` | Logging for `com.devops.tracker` |
| `SPRING_DATASOURCE_URL` | in-memory H2 | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | empty | Database password |
| `SPRING_JPA_DDL_AUTO` | `update` | Schema handling |
| `H2_CONSOLE_ENABLED` | `false` | Enable `/h2-console` only for local debugging |
| `THYMELEAF_CACHE` | `true` | Template cache |

Copy `.env.example` to `.env` for Docker Compose. Do not commit `.env` or real credentials.

## CI/CD explanation

`azure-pipelines.yml` is a **template**. It has not been run against Azure DevOps yet (Phase 3).

Intended stages:

1. Install / use Java 21  
2. `gradlew clean test`  
3. Publish JUnit results  
4. `gradlew bootJar`  
5. Build the Docker image  
6. Run the image and call `/actuator/health`  
7. Push to Azure Container Registry when `PUSH_TO_ACR=true`  
8. Deploy to Azure App Service when `DEPLOY_TO_APP_SERVICE=true`

Stages 7 and 8 stay off until you create Azure resources and service connections. No secrets belong in the YAML; use Azure DevOps variable groups and service connections.

## Azure deployment architecture

Intended path (after local Docker works):

```
Azure Repos / GitHub
        │
  Azure Pipelines
        │
  Gradle test + bootJar
        │
  Docker build
        │
 Azure Container Registry
        │
 Azure App Service (Linux Web App for Containers)
```

You will need:

- An Azure subscription and resource group  
- Azure Container Registry  
- App Service plan + Web App configured to pull the image  
- Azure DevOps project (or GitHub + Azure Pipelines)  
- Service connections:  
  - Docker Registry connection to ACR (`ACR_SERVICE_CONNECTION`)  
  - Azure Resource Manager connection (`AZURE_SERVICE_CONNECTION`)  
- Pipeline variables: `ACR_LOGIN_SERVER`, `AZURE_WEBAPP_NAME`, `AZURE_RESOURCE_GROUP`, `PUSH_TO_ACR`, `DEPLOY_TO_APP_SERVICE`

Do not create these from the application itself. Use the Azure Portal or Azure CLI when you are ready to spend subscription quota.

## Troubleshooting

**Gradle cannot find Java 21**  
Install Temurin 21 and ensure `JAVA_HOME` points at it. The build uses a Java 21 toolchain.

**Port 8080 already in use**  
Set `SERVER_PORT=8081` or stop the other process.

**Empty dashboard**  
For Gradle, `APP_SEED_DATA` defaults to `true`. For `docker run` without Compose, the image defaults to `false` — set `-e APP_SEED_DATA=true` or create a project in the UI. Tests disable seeding.

**Health endpoint not UP**  
Wait for startup, then `curl.exe http://localhost:8080/actuator/health`. Check application logs for datasource or port errors.

**Docker build fails on Windows**  
Use Linux containers in Docker Desktop. The image runs `./gradlew` inside Linux.

**H2 console**  
Keep `H2_CONSOLE_ENABLED=false` unless you are debugging locally. It is not intended for deployment.

## Future improvements

- Persistent database (Azure Database for PostgreSQL) instead of in-memory H2  
- Authentication and role-based access  
- Deployment events published from the pipeline (version + git SHA)  
- Structured log shipping and Application Insights  
- Container scan (Trivy) in the pipeline  
- Production-grade secret storage (Azure Key Vault)

## License

Use this project for the CodeAlpha internship portfolio unless your program specifies otherwise.
