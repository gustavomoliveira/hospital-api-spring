# Hospital API

*[Ler em português](README.pt-BR.md)*

A REST API for managing a hospital's core records — patients, doctors, appointments, and admissions — built with Spring Boot and PostgreSQL.

Stack: **Java 21, Spring Boot 4.1.0, Spring Data JPA, Bean Validation, PostgreSQL (runtime), H2 (test), Lombok, JUnit 5 + Mockito, Maven**.

## Table of contents

- [What it does](#what-it-does)
- [Technical decisions](#technical-decisions)
- [Running locally](#running-locally)
- [Project structure](#project-structure)

## What it does

The API exposes CRUD-style endpoints over four domain entities:

| Entity | Endpoint prefix | Exposed via API? |
|---|---|---|
| `Paciente` (Patient) | `/pacientes` | Yes — create, get by id, list, delete |
| `Medico` (Doctor) | `/medicos` | Yes — create, list, plus a ranking endpoint |
| `Consulta` (Appointment) | `/consultas` | Yes — create only |
| `Internacao` (Admission) | — | No — modeled as a JPA entity, not yet exposed through any controller/service/repository |

Beyond plain CRUD, `GET /medicos/consultas/ranking` returns doctors ordered by their total number of appointments, computed with a single aggregate query rather than loaded and counted in Java.

On startup, a `DataLoader` (`CommandLineRunner`) seeds two doctors and two patients if the tables are empty — useful to have data to hit the API against immediately after starting the service.

## Technical decisions

### 1. Package-by-feature, not package-by-layer

Unlike a layout that groups all controllers together, all services together, etc., this project groups everything belonging to one concept in a single package:

```
domain/paciente/    → Paciente, PacienteController, PacienteService, PacienteRepository,
                       PacienteRequestDTO, PacienteResponseDTO, PacienteMapper
domain/medico/      → same shape, for Medico
domain/consulta/    → same shape, for Consulta
domain/internacao/  → only the entity so far
```

Cross-cutting concerns that don't belong to one feature — the two generic exception classes, the global exception handler, `DataLoader` — sit outside `domain/`, in `exception/` and `insfrastructure/` respectively. This trades off against a layered style (grouping all controllers together, all services together, and so on): here, everything about "patients" is in one place, at the cost of not having a single `controller/` folder to scan for every endpoint in the system.

### 2. DTOs and mapping kept explicit and manual

Every entity has a `RequestDTO` (input, as a `record`) and a `ResponseDTO` (output, also a `record`), converted through a small static `Mapper` class (`PacienteMapper.toEntity(...)`, `PacienteMapper.toDTO(...)`). No mapping library (MapStruct, ModelMapper) is used — the conversion is a few lines of manual field assignment per entity. This keeps the mapping visible and debuggable at the cost of some repetition across the three DTOs/mappers in the project.

Entities themselves follow the same shape in all four: a public constructor taking business fields (no `id`), a protected/implicit no-args constructor for JPA (`@NoArgsConstructor`), `@Getter` only — no setters, so the only way to build one is through the constructor.

### 3. Bean Validation on request DTOs, enforced consistently across controllers

`PacienteRequestDTO` and `MedicoRequestDTO` declare Jakarta Validation constraints (`@NotBlank`, `@NotNull`, and `@CPF` from `hibernate-validator`'s Brazil-specific constraints, which validates a CPF's check digits, not just its format). `ConsultaRequestDTO` does the same with `@NotNull` on its required fields. All three controllers (`PacienteController`, `MedicoController`, `ConsultaController`) apply `@Valid` on the `@RequestBody` parameter, so a blank name or an invalid CPF is rejected with a `400 Bad Request` before it ever reaches the service layer.

### 4. Aggregate query via JPQL constructor expression

The doctor ranking endpoint is backed by one JPQL query using a constructor expression, rather than fetching all doctors with their appointments and counting in Java:

```java
@Query("SELECT new dev.gustavo.at.domain.medico.MedicoConsultasDTO(m.nome, COUNT(c)) " +
        "FROM Medico m LEFT JOIN m.consultas c " +
        "GROUP BY m.nome " +
        "ORDER BY COUNT(c) DESC")
List<MedicoConsultasDTO> findMedicosByTotalConsultas();
```

The `LEFT JOIN` ensures doctors with zero appointments still appear in the ranking with a count of zero, instead of being silently excluded as an `INNER JOIN` would do. The query returns `MedicoConsultasDTO` instances directly — Hibernate builds the DTO from the query result, so no intermediate entity list is materialized just to be thrown away after counting.

### 5. Centralized exception handling with a consistent error shape

A single `@RestControllerAdvice` (`GlobalExceptionHandler`) maps the four domain exceptions to HTTP responses, all wrapped in the same `ErrorResponseDTO` (`status`, `message`, `timeStamp`). The two "already exists" cases (`PacienteJaExisteException`, `MedicoJaExisteException`) map to `409 Conflict`, and the two "not found" cases (`PacienteNaoExisteException`, `MedicoNaoExisteException`) map to `404 Not Found` — each exception carrying the status code that actually matches its semantics.

### 6. Environment-based configuration for the database, H2 for tests

`src/main/resources/application.properties` reads the datasource entirely from environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) with no defaults and no values committed — the app won't start without them set. Locally, these are supplied through a `.env` file (git-ignored, never committed) with `.env.example` checked into the repository as a template showing which keys are expected, without real values. `src/test/resources/application.properties` overrides this with a self-contained in-memory H2 database (`ddl-auto=create-drop`), so the test suite never touches Postgres or requires any environment variable to run.

### 7. Two testing styles, at two different levels

- `HospitalServiceTest` — unit tests against `PacienteService` and `MedicoService`, with `PacienteRepository`/`MedicoRepository` mocked via Mockito (`@Mock`, `@InjectMocks`). These verify business logic (duplicate CPF/CRM checks, not-found handling) without touching a database at all.
- `PacienteIntegracaoTest` — full-stack tests using `@SpringBootTest` + `@AutoConfigureMockMvc`, hitting the real `/pacientes` endpoints through `MockMvc` and asserting on the actual JSON response, backed by the H2 test database described above.

This gives fast, isolated coverage of business rules and slower, end-to-end coverage of the HTTP contract, rather than relying on only one style.

### 8. Actuator with a minimal exposed surface

`spring-boot-starter-actuator` is on the classpath, but `management.endpoints.web.exposure.include=health` only exposes the health endpoint — not `/actuator/env`, `/actuator/beans`, or any of the other actuator endpoints that can leak configuration or internal details if left open by default.

## Running locally

> Instructions assume **macOS** with a terminal.

This service needs a running PostgreSQL instance — unlike an H2-in-memory setup, there's a real database to have available before starting the app.

### Prerequisites

- **JDK 21** installed and selected (`java -version` should print 21).
- **PostgreSQL** installed locally. Easiest way on macOS is via Homebrew:
  ```bash
  brew install postgresql@16
  brew services start postgresql@16
  ```
- No local Maven install is required — the project ships its own Maven Wrapper (`mvnw`).

### 1. Clone the repository

```bash
git clone https://github.com/gustavomoliveira/hospital-api-spring.git
cd hospital-api-spring
```

### 2. Create the database

```bash
createdb hospital
```

### 3. Set the environment variables the app expects

`application.properties` reads these three variables with no fallback, so the app won't start without them:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/hospital
export DB_USERNAME=$(whoami)
export DB_PASSWORD=
```

If your local Postgres user needs a password, set `DB_PASSWORD` accordingly; a fresh Homebrew install typically has no password on the default superuser.

### 4. Run the application

```bash
./mvnw spring-boot:run
```

On first run, the `DataLoader` seeds two doctors and two patients automatically — you don't need to create data manually before trying the endpoints.

### 5. Verify it's running

```bash
curl -i http://localhost:8080/pacientes
```

This should return `200 OK` with a JSON array containing the two seeded patients (João Silva and Maria Oliveira).

### 6. Try the doctor ranking endpoint

```bash
curl -i http://localhost:8080/medicos/consultas/ranking
```

Both seeded doctors should appear with `totalConsultas: 0`, since no appointment has been created yet.

### 7. Run the test suite

Tests don't need Postgres or the environment variables above — they run entirely against H2:

```bash
./mvnw test
```

### Opening in IntelliJ

Open `pom.xml` at the repository root as a project in IntelliJ. Before running `AtApplication` from the IDE, set the three environment variables above in the run configuration (**Run → Edit Configurations → Environment variables**), otherwise the app fails to start with a datasource error.

### Running via Docker instead

The repository also ships a `Dockerfile` and a `docker-compose.yml` that start Postgres and the app together, reading the database password from `POSTGRES_PASSWORD` in a local `.env` file (git-ignored; copy `.env.example` to `.env` and fill in a real password before running). One thing to know: the `Dockerfile` copies a pre-built `target/*.jar` rather than building it inside the container, so you need to run `./mvnw clean package -DskipTests` first.

```bash
cp .env.example .env   # then edit .env with a real password
./mvnw clean package -DskipTests
docker compose up --build
```

## Project structure

```
hospital-api-spring/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/dev/gustavo/at/
    │   │   ├── AtApplication.java
    │   │   ├── domain/
    │   │   │   ├── paciente/     # Paciente entity, controller, service, repository, DTOs, mapper
    │   │   │   ├── medico/       # Medico entity, controller, service, repository, DTOs, mapper
    │   │   │   ├── consulta/     # Consulta entity, controller, service, repository, DTOs, mapper
    │   │   │   └── internacao/   # Internacao entity only — no controller/service/repository yet
    │   │   ├── exception/        # domain-specific exceptions (shared across features)
    │   │   └── insfrastructure/
    │   │       ├── config/       # DataLoader
    │   │       └── exception/    # GlobalExceptionHandler, ErrorResponseDTO
    │   └── resources/
    │       └── application.properties   # datasource from env vars, Postgres dialect
    └── test/
        ├── java/dev/gustavo/at/
        │   ├── AtApplicationTests.java
        │   └── domain/paciente/
        │       ├── HospitalServiceTest.java      # unit tests, Mockito
        │       └── PacienteIntegracaoTest.java    # integration tests, MockMvc + H2
        └── resources/
            └── application.properties   # H2 datasource, overrides the main profile for tests
```

Note the package name is `insfrastructure` (not `infrastructure`) — that's how it's spelled in the actual source tree.
