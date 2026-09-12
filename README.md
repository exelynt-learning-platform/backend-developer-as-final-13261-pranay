# Resource Booking System

A RESTful Resource Booking System built using **Java 17, Spring Boot 4.1.1, Spring Security, JWT, Spring Data JPA, Hibernate, and MySQL**.

Technology Stack
Java 17

Spring Boot 4.1.1

Spring Security

JWT (JJWT)

Spring Data JPA / Hibernate

MySQL / H2 (test)

Maven

JUnit 5, Mockito, MockMvc

Swagger / OpenAPI

The application provides authentication, role-based authorization, resource management, reservation management, validation, filtering, pagination, sorting, and reservation conflict detection.

## Features

### Authentication & Authorization

- User Signup and Login
- JWT-based authentication with access & refresh tokens
- BCrypt password hashing
- Role-based authorization — `ADMIN` and `USER`
- Stateless authentication
- Access/Refresh token type validation
- Refresh token rotation (new access + refresh issued on refresh)
- Disabled accounts cannot login or refresh tokens
- Duplicate username / email detection (HTTP 409)
- Invalid credentials return HTTP 401 (no info leak)

### Resource Management

- Create Resource — ADMIN only
- Get Resource — USER / ADMIN
- Get All Resources — USER / ADMIN (paginated, sortable)
- Update Resource — ADMIN only
- Delete Resource — ADMIN only

### Reservation Management

- Create Reservation — USER / ADMIN
- Get Reservation — USER / ADMIN
- USER can view only their own reservations
- ADMIN can view all reservations
- Update Reservation (full) — ADMIN only
- Update Reservation Status (PATCH) — ADMIN only
- Delete Reservation — ADMIN only
- Reservation status: `PENDING`, `CONFIRMED`, `CANCELLED`
- **Reservation overlap detection** (CANCELLED reservations excluded)
- Status transition rules enforced
  - `PENDING` → `CONFIRMED` / `CANCELLED`
  - `CONFIRMED` → `CANCELLED`
  - `CANCELLED` → immutable
- Confirmed reservations cannot be moved into the past
- Status cannot be changed after reservation has started

### Filtering, Pagination & Sorting

Reservations support:

- Status filtering
- Minimum / Maximum price filtering
- Pagination (max page size enforced — 400 if exceeded)
- Sorting with **whitelist** of allowed fields (`id`, `price`, `startTime`, `endTime`, `status`)

Example:

```text
/api/reservations?status=CONFIRMED&minPrice=100&maxPrice=1000&page=0&size=10&sort=price,desc