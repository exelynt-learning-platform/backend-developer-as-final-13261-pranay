# Resource Booking System

A RESTful Resource Booking System built using **Java 17, Spring Boot, Spring Security, JWT, Spring Data JPA, Hibernate, and MySQL**.

The application provides authentication, role-based authorization, resource management, reservation management, validation, filtering, pagination, sorting, and reservation conflict detection.

## Features

### Authentication & Authorization

* User Signup and Login
* JWT-based authentication
* BCrypt password hashing
* Role-based authorization
* `ADMIN` and `USER` roles
* Stateless authentication
* Access and refresh token generation
* Access token and refresh token type validation

### Resource Management

* Create Resource — ADMIN only
* Get Resource — USER / ADMIN
* Get All Resources — USER / ADMIN
* Update Resource — ADMIN only
* Delete Resource — ADMIN only

### Reservation Management

* Create Reservation — USER / ADMIN
* Get Reservation — USER / ADMIN
* USER can view only their own reservations
* ADMIN can view all reservations
* Update Reservation — ADMIN only
* Delete Reservation — ADMIN only
* Reservation status:

  * `PENDING`
  * `CONFIRMED`
  * `CANCELLED`
* Reservation overlap detection
* Cancelled reservations cannot be modified

### Filtering, Pagination & Sorting

Reservations support:

* Status filtering
* Minimum price
* Maximum price
* Pagination
* Sorting

Example:

```text
/api/reservations?status=CONFIRMED&minPrice=100&maxPrice=1000&page=0&size=10&sort=price,desc
```

## Technology Stack

* Java 17
* Spring Boot 4.1.1
* Spring Security
* JWT
* Spring Data JPA / Hibernate
* MySQL
* Maven
* JUnit 5
* Mockito
* Swagger / OpenAPI

## Project Structure

```text
src/main/java/com/Resource_Booking_System
│
├── Configure
├── Controller
├── Dto
├── Entity
├── Exception
├── IService
├── Repository
├── ServiceImpl
└── Specification

src/test/java
├── Controller
└── Service

pom.xml
README.md
```

## Database Configuration

Create the MySQL database:

```sql
CREATE DATABASE resource_booking_system;
```

Configure database credentials using environment variables:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/resource_booking_system
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

## JWT Configuration

JWT secret is externalized:

```properties
jwt.secret=${JWT_SECRET}
```

Required environment variables:

```text
DB_USER
DB_PASSWORD
JWT_SECRET
ADMIN_PASSWORD
USER_PASSWORD
```

Do not commit real credentials or secrets to GitHub.

## Running the Application

Build:

```bash
mvn clean install
```

Run:

```bash
mvn spring-boot:run
```

Application:

```text
http://localhost:8080
```

## Authentication APIs

### Signup

```http
POST /auth/signup
```

Example:

```json
{
  "username": "john",
  "email": "john@gmail.com",
  "password": "password123"
}
```

New users are created with the `USER` role.

### Login

```http
POST /auth/login
```

Example response:

```json
{
  "accessToken": "JWT_ACCESS_TOKEN",
  "refreshToken": "JWT_REFRESH_TOKEN"
}
```

Use the access token:

```http
Authorization: Bearer <accessToken>
```

## API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

## Testing

Run tests:

```bash
mvn test
```

Tests use:

* JUnit 5
* Mockito
* MockMvc

## Default Roles

```text
ADMIN
USER
```

Normal signup creates a `USER`.

Development seed users are created only with the `dev` profile using environment-based passwords.

## HTTP Status Codes

| Status | Meaning               |
| ------ | --------------------- |
| 200    | Success               |
| 201    | Created               |
| 204    | No Content            |
| 400    | Bad Request           |
| 401    | Unauthorized          |
| 403    | Forbidden             |
| 404    | Not Found             |
| 500    | Internal Server Error |
