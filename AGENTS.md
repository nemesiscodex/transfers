# Repository Guidelines

## Project Structure & Module Organization
- Source: `src/main/java` under `org.nemesiscodex.transfers` (e.g., `core/controller`, `core/service`, `core/repository`, `core/dto`, `core/security`, `config`).
- Resources: `src/main/resources` (`application.yaml`, `db/migration/*` for Flyway).
- Tests: `src/test/java` mirror package layout; Spring Boot + JUnit 5 + Testcontainers.
- Build tooling: Gradle (`build.gradle`, `settings.gradle`); Java 25 (see `.sdkmanrc`).
- Local infra: `compose.yml` provides PostgreSQL and Valkey (Redis-compatible).

## Build, Test, and Development Commands
- Prereqs: Java 25, Docker (for Testcontainers/compose).
- Start infra: `docker compose up -d` (PostgreSQL + Valkey).
- Run app: `./gradlew bootRun` (Flyway runs automatically; ensure DB/Redis env configured if not using compose).
- Run tests: `./gradlew test` (uses JUnit Platform and `spring.profiles.active=test`).
- Build artifacts: `./gradlew build` (compile + test + jar), clean: `./gradlew clean`.

## Coding Style & Naming Conventions
- Java style: 4-space indents, braces on same line, meaningful names.
- Packages: `org.nemesiscodex.transfers.*`; classes `PascalCase`, methods/fields `camelCase`, constants `UPPER_SNAKE_CASE`.
- DTOs may use Java `record`s; keep immutable where practical.
- Keep imports organized; avoid unused code. No formatter is enforced—match existing style.

## Testing Guidelines
- Frameworks: JUnit 5, Spring Boot Test, Reactor `StepVerifier`, Testcontainers.
- Naming: place tests under the same package path; suffix with `*Test`.
- Run specific test: `./gradlew test --tests 'org.nemesiscodex.transfers..*'`.
- Tests run with the `test` profile; Flyway is disabled in tests; containers start automatically.

## Commit & Pull Request Guidelines
- Commits: concise, imperative subject (e.g., "Add user signup validation"). Conventional Commits (`feat:`, `fix:`) welcome.
- PRs: include description, rationale, local run/test steps, linked issues, and curl/examples if API changes.
- Note schema changes and include a Flyway migration.

## Security & Configuration Tips
- Override `jwt.secret` via environment/config; never commit secrets. Example: `JWT_SECRET=... ./gradlew bootRun`.
- Flyway migrations: add files to `src/main/resources/db/migration` as `VYYYYMMDDHHmm__description.sql` (see existing pattern).
- When adding configuration, prefer profile-specific YAML or env vars.

## Agent-Specific Instructions
- Keep diffs minimal; follow existing package layout and test patterns.
- Update or add tests alongside changes; do not break reactive flows.
