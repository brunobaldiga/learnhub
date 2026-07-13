# Learnhub API

Learnhub is a REST API for an online course platform, built as a portfolio project to practice designing and implementing a real backend system from scratch: authentication, relational data modeling, payments, and content management.

Live API and interactive docs: https://learnhub-8zwy.onrender.com/swagger-ui/index.html

Note: the app is hosted on Render's free tier, so the first request after a period of inactivity can take up to a minute to respond while the instance spins back up.

## Overview

The platform allows creators to publish courses made of sections and videos, and allows users to browse, purchase, and review those courses. Access is controlled through JWT authentication, and every write operation is scoped to the authenticated user, so a creator can only manage their own courses and a user can only see their own purchases and enrollments.

## Features

- User registration and login with JWT-based authentication
- Course creation and management, including draft, review, and publish status
- Course sections and videos, with ordering support
- Course search and listing with pagination and sorting
- Course purchases, with automatic enrollment on payment
- Payment history with date range filtering
- Enrollment tracking with progress based on completed lessons
- Course reviews with rating and comment, restricted to users who purchased the course

## Tech stack

- Java 17
- Spring Boot (Web, Security, Data JPA, Validation)
- PostgreSQL
- Flyway for database migrations
- JWT authentication (java-jwt)
- springdoc-openapi for the Swagger/OpenAPI documentation
- JUnit and Spring Boot Test for unit and integration tests
- JaCoCo for test coverage reports
- Deployed on Render

## Architecture

The codebase is organized by domain rather than by technical layer. Each feature area (user, course, section, enrollment, payment) has its own package containing its controller, service, repository, and DTOs. This keeps related code together and makes each module easy to navigate on its own.

```
org.example.learnhub
├── user
├── course
├── section
├── enrollment
├── payment
├── gateway
├── config
└── exception
```

Cross-cutting concerns such as security configuration, exception handling, and global request/response setup live in their own packages (`config`, `exception`, `gateway`) rather than being spread across the domain modules.

## API documentation

Full API documentation is available through Swagger UI once the application is running:

```
/swagger-ui/index.html
```

The raw OpenAPI spec is available at:

```
/v3/api-docs
```

### Main resources

| Resource | Description |
|---|---|
| `/api/users` | Registration, login, and profile |
| `/api/courses` | Course creation, listing, search, and updates |
| `/api/courses/{courseId}/sections` | Course sections |
| `/api/sections/{sectionId}/videos` | Videos within a section |
| `/api/courses/{courseId}/reviews` | Course reviews |
| `/api/payments` | Course purchases and payment history |
| `/api/enrollments` | User enrollments and progress |

All endpoints except registration and login require a valid JWT, sent as a Bearer token in the `Authorization` header.

## Running locally

### With Docker

The project ships with a `docker-compose.yml` that runs the API alongside its own PostgreSQL instance, using the prebuilt image published at [brunobaldiga/learnhub](https://hub.docker.com/r/brunobaldiga/learnhub). This is the fastest way to get it running, no Java or Maven required:

```
git clone https://github.com/brunobaldiga/learnhub.git
cd learnhub
docker compose up
```

The API will be available at `http://localhost:8080`, with Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

### From source

To build and run the application locally, for example to make changes to the code:

1. Clone the repository:

```
git clone https://github.com/brunobaldiga/learnhub.git
cd learnhub
```

2. Create a PostgreSQL database for the project.

3. Set the following environment variables, or rely on the defaults in `application.yaml` for local development:

```
DB_URL=jdbc:postgresql://localhost:5432/learnhub
DB_USER=your_db_user
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret
```

4. Run the application:

```
./mvnw spring-boot:run
```

Flyway will run the database migrations automatically on startup.

Prerequisites: Java 17, Maven (or the included wrapper `./mvnw`), and PostgreSQL.

## Testing

The project includes unit and integration tests for the service and controller layers of every module, run automatically on push and pull requests through GitHub Actions.

To run the tests locally:

```
./mvnw clean test
```

A JaCoCo coverage report is generated after running the tests, at `target/site/jacoco/index.html`.

## Contact

Bruno — brunobaldiga@gmail.com