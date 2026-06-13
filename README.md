# DevForge Backend

Spring Boot backend for the DevForge developer platform.

## Requirements

- Java 21
- PostgreSQL 14+
- Redis 6+ for production caching
- Maven wrapper included

## Profiles

- `dev`: local development defaults. Flyway is disabled by default and Hibernate can update schema.
- `test`: H2 in-memory database, Flyway disabled.
- `prod`: production profile. Flyway enabled, Hibernate schema mutation disabled.

Set the active profile:

```bash
export SPRING_PROFILES_ACTIVE=prod
```

## Required Environment Variables

```bash
export DB_URL=jdbc:postgresql://host:5432/devforge
export DB_USERNAME=devforge
export DB_PASSWORD=change-me
export JWT_SECRET=change-me-minimum-32-bytes
export CORS_ALLOWED_ORIGINS=https://your-frontend.example.com
```

Production Redis cache:

```bash
export CACHE_TYPE=redis
export REDIS_HOST=redis-host
export REDIS_PORT=6379
export REDIS_PASSWORD=change-me
export CACHE_TTL_MS=600000
```

Optional production settings:

```bash
export SERVER_PORT=8080
export ACTUATOR_EXPOSED_ENDPOINTS=health,info
export ACTUATOR_HEALTH_DETAILS=never
export OPENAPI_ENABLED=true
export SWAGGER_UI_ENABLED=false
```

## Build

```bash
./mvnw clean compile
./mvnw clean package
```

## Run Locally

```bash
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

API base path:

```text
http://localhost:8080/api/v1
```

## Database Migrations

Flyway migrations live in:

```text
src/main/resources/db/migration
```

Production uses Flyway with `spring.jpa.hibernate.ddl-auto=validate`.

For an existing non-empty database, `spring.flyway.baseline-on-migrate=true` is enabled by default. Before first production deployment, verify the migration matches the live schema and remove duplicate data that conflicts with unique constraints.

## Neon Demo Verification

With the backend running against Neon, create a persistent 10-user demo dataset through the authenticated API:

```bash
DEMO_PASSWORD='use-a-strong-demo-password' ./scripts/seed-neon-demo.sh
```

Optional variables:

```bash
API_BASE_URL=http://localhost:8080/api/v1
RUN_ID=release-check-01
REPORT_DIR=.seed-reports
```

The script creates uniquely named users, projects, a community, memberships, follows, likes, bookmarks, comments, and replies. It does not delete or overwrite existing data. Reports are written beneath the ignored `.seed-reports/` directory and never include passwords or JWTs.

To verify a previously created run without adding duplicate records:

```bash
MODE=verify RUN_ID=release-check-01 DEMO_PASSWORD='the-original-demo-password' ./scripts/seed-neon-demo.sh
```

## OpenAPI

Swagger UI:

```text
/api/v1/swagger-ui.html
```

OpenAPI JSON:

```text
/api/v1/v3/api-docs
```

Disable Swagger UI in production with:

```bash
export SWAGGER_UI_ENABLED=false
```

## Actuator

Public health endpoints:

```text
/api/v1/actuator/health
/api/v1/actuator/health/liveness
/api/v1/actuator/health/readiness
/api/v1/actuator/info
```

Only `health` and `info` are exposed by default.

## CORS

CORS is configured through environment variables:

```bash
export CORS_ALLOWED_ORIGINS=https://your-frontend.example.com
export CORS_ALLOWED_METHODS=GET,POST,PUT,PATCH,DELETE,OPTIONS
export CORS_ALLOWED_HEADERS=Authorization,Content-Type
export CORS_ALLOW_CREDENTIALS=true
```

Do not use wildcard origins with credentials in production.

## Deployment Checklist

- Rotate any previously committed database or JWT secrets.
- Set `SPRING_PROFILES_ACTIVE=prod`.
- Set all required database and JWT environment variables.
- Set exact production CORS origins.
- Run Flyway migrations before serving traffic.
- Use Redis for production cache.
- Keep `JPA_DDL_AUTO=validate`.
- Keep SQL logging disabled.
- Expose only required actuator endpoints.
