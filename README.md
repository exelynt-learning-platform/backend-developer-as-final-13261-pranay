# Resource Booking System

A RESTful Resource Booking System built using **Java 17, Spring Boot, Spring Security, JWT, Spring Data JPA, Hibernate, and MySQL**.

The application provides secure authentication, role-based authorization, resource management, reservation management, filtering, pagination, sorting, validation, and centralized exception handling.

---

## 1. Features

### Authentication & Authorization

* User Signup
* User Login
* JWT-based authentication
* BCrypt password encryption
* Role-based authorization
* Two roles:

    * `ADMIN`
    * `USER`
* Stateless authentication using JWT

### Resource Management

* Create Resource — ADMIN only
* Get Resource by ID — USER / ADMIN
* Get All Resources — USER / ADMIN
* Update Resource — ADMIN only
* Delete Resource — ADMIN only

### Reservation Management

* Create Reservation — USER / ADMIN
* Get Reservation by ID
* Get Reservations with pagination
* USER can view only their own reservations
* ADMIN can view all reservations
* Update Reservation — ADMIN only
* Delete Reservation — ADMIN only
* Reservation status:

    * `PENDING`
    * `CONFIRMED`
    * `CANCELLED`

### Filtering

Reservations can be filtered using:

* Status
* Minimum price
* Maximum price

### Pagination & Sorting

Reservation and resource listing APIs support:

* Page number
* Page size
* Sorting

Example:

```text
?page=0&size=10&sort=price,desc
```

### Validation

The application validates:

* Required fields
* Resource name
* Resource type
* Price
* Reservation start time
* Reservation end time
* Start time must be before end time
* Reservation start time must be in the future
* Minimum price cannot be greater than maximum price

---

## 2. Technology Stack

| Technology        | Version / Usage      |
| ----------------- | -------------------- |
| Java              | 17                   |
| Spring Boot       | 4.1.1                |
| Spring Security   | JWT Authentication   |
| Spring Data JPA   | Database access      |
| Hibernate         | ORM                  |
| MySQL             | Database             |
| JWT               | Authentication Token |
| BCrypt            | Password encryption  |
| Maven             | Build Tool           |
| JUnit 5           | Unit Testing         |
| Mockito           | Mocking              |
| Swagger / OpenAPI | API Documentation    |

---

## 3. Project Structure

```text
Resource_Booking_System
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.Resource_Booking_System
│   │   │
│   │   │       ├── Configure
│   │   │       │   ├── JwtAuthenticationFilter
│   │   │       │   ├── JwtUtil
│   │   │       │   ├── MyUserDetailsService
│   │   │       │   └── SecurityConfig
│   │   │       │
│   │   │       ├── Controller
│   │   │       │   ├── AuthController
│   │   │       │   ├── ResourceController
│   │   │       │   └── ReservationController
│   │   │       │
│   │   │       ├── Dto
│   │   │       │   ├── AuthResponse
│   │   │       │   ├── LoginRequest
│   │   │       │   ├── SignUpRequest
│   │   │       │   ├── ResourceDto
│   │   │       │   ├── ReservationRequest
│   │   │       │   ├── ReservationResponse
│   │   │       │   └── ReservationUpdateRequest
│   │   │       │
│   │   │       ├── Entity
│   │   │       │   ├── User
│   │   │       │   ├── Role
│   │   │       │   ├── Resource
│   │   │       │   ├── Reservation
│   │   │       │   └── ReservationStatus
│   │   │       │
│   │   │       ├── Exception
│   │   │       │   ├── GlobalExceptionHandler
│   │   │       │   ├── BadRequestException
│   │   │       │   ├── ResourceNotFoundException
│   │   │       │   ├── ReservationNotFoundException
│   │   │       │   └── UsernameNotFoundException
│   │   │       │
│   │   │       ├── IService
│   │   │       │   ├── IAuthService
│   │   │       │   ├── IResourceService
│   │   │       │   └── IReservationService
│   │   │       │
│   │   │       ├── Repository
│   │   │       │   ├── UserRepository
│   │   │       │   ├── ResourceRepository
│   │   │       │   └── ReservationRepository
│   │   │       │
│   │   │       ├── ServiceImpl
│   │   │       │   ├── AuthServiceImpl
│   │   │       │   ├── ResourceServiceImpl
│   │   │       │   └── ReservationServiceImpl
│   │   │       │
│   │   │       └── Specification
│   │   │           └── ReservationSpecification
│   │   │
│   │   └── resources
│   │       └── application.properties
│   │
│   └── test
│       └── java
│           └── com.Resource_Booking_System
│               ├── Controller
│               └── Service
│
├── pom.xml
└── README.md
```

---

## 4. Database Configuration

Create a MySQL database:

```sql
CREATE DATABASE resource_booking_system;
```

Update `application.properties`:

```properties
spring.application.name=Resource_Booking_System

spring.datasource.url=jdbc:mysql://localhost:3306/resource_booking_system
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

Replace:

```text
YOUR_PASSWORD
```

with your MySQL password.

---

## 5. JWT Configuration

Configure the JWT secret and expiration in `application.properties`.

Example:

```properties
jwt.secret=YOUR_SECRET_KEY
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000
```

Do not commit real production secrets to GitHub.

For production, use environment variables or a secret-management solution.

---

## 6. Running the Application

### Step 1 — Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

### Step 2 — Open the Project

Open the project in:

* IntelliJ IDEA
* Eclipse
* Spring Tool Suite

### Step 3 — Configure MySQL

Create the database and update:

```text
application.properties
```

### Step 4 — Build the Project

Using Maven:

```bash
mvn clean install
```

### Step 5 — Run the Application

```bash
mvn spring-boot:run
```

Or run the main Spring Boot application class from the IDE.

Application will start at:

```text
http://localhost:8080
```

---

# 7. Authentication APIs

## Signup

### Request

```http
POST /auth/signup
```

### Example Request Body

```json
{
    "username": "john",
    "email": "john@gmail.com",
    "password": "password123"
}
```

New users are registered with the `USER` role.

---

## Login

### Request

```http
POST /auth/login
```

### Example Request Body

```json
{
    "username": "john",
    "password": "password123"
}
```

### Example Response

```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Use the access token for protected APIs:

```http
Authorization: Bearer <accessToken>
```

---

# 8. Resource APIs

Base URL:

```text
/api/resources
```

| Method | Endpoint              | Role         |
| ------ | --------------------- | ------------ |
| POST   | `/api/resources`      | ADMIN        |
| GET    | `/api/resources/{id}` | USER / ADMIN |
| GET    | `/api/resources`      | USER / ADMIN |
| PUT    | `/api/resources/{id}` | ADMIN        |
| DELETE | `/api/resources/{id}` | ADMIN        |

---

## Create Resource

```http
POST /api/resources
```

Example:

```json
{
    "name": "Conference Room",
    "description": "Meeting room with projector",
    "type": "ROOM",
    "available": true
}
```

Response status:

```text
201 CREATED
```

---

## Get Resource

```http
GET /api/resources/1
```

Response:

```text
200 OK
```

---

## Get All Resources

```http
GET /api/resources?page=0&size=10
```

Sorting example:

```text
GET /api/resources?page=0&size=10&sort=name,asc
```

---

## Update Resource

```http
PUT /api/resources/1
```

Example:

```json
{
    "name": "Updated Conference Room",
    "description": "Updated meeting room",
    "type": "ROOM",
    "available": true
}
```

---

## Delete Resource

```http
DELETE /api/resources/1
```

---

# 9. Reservation APIs

Base URL:

```text
/api/reservations
```

| Method | Endpoint                 | Role         |
| ------ | ------------------------ | ------------ |
| POST   | `/api/reservations`      | USER / ADMIN |
| GET    | `/api/reservations/{id}` | USER / ADMIN |
| GET    | `/api/reservations`      | USER / ADMIN |
| PUT    | `/api/reservations/{id}` | ADMIN        |
| DELETE | `/api/reservations/{id}` | ADMIN        |

---

## Create Reservation

```http
POST /api/reservations
```

Example:

```json
{
    "resourceId": 1,
    "price": 500.00,
    "startTime": "2026-10-01T10:00:00",
    "endTime": "2026-10-01T12:00:00"
}
```

The user is taken from the authenticated JWT.

The client does **not** provide `userId`.

New reservations are created with:

```text
PENDING
```

Response status:

```text
201 CREATED
```

---

# 10. Get Reservation

```http
GET /api/reservations/1
```

### USER

A USER can access only their own reservation.

If a USER tries to access another user's reservation:

```text
403 FORBIDDEN
```

### ADMIN

ADMIN can access any reservation.

---

# 11. Get Reservations

```http
GET /api/reservations?page=0&size=10
```

### USER

Returns only the authenticated user's reservations.

### ADMIN

Returns all reservations.

---

# 12. Reservation Filtering

Filter by status:

```http
GET /api/reservations?status=CONFIRMED
```

Filter by minimum price:

```http
GET /api/reservations?minPrice=100
```

Filter by maximum price:

```http
GET /api/reservations?maxPrice=1000
```

Filter by price range:

```http
GET /api/reservations?minPrice=100&maxPrice=1000
```

Multiple filters:

```http
GET /api/reservations?status=CONFIRMED&minPrice=100&maxPrice=1000&page=0&size=10
```

---

# 13. Pagination & Sorting

Pagination:

```http
GET /api/reservations?page=0&size=10
```

Sorting by price:

```http
GET /api/reservations?page=0&size=10&sort=price,desc
```

Sorting by start time:

```http
GET /api/reservations?page=0&size=10&sort=startTime,asc
```

---

# 14. Update Reservation

ADMIN only:

```http
PUT /api/reservations/1
```

Example:

```json
{
    "resourceId": 1,
    "price": 700.00,
    "startTime": "2026-10-01T10:00:00",
    "endTime": "2026-10-01T12:00:00",
    "status": "CONFIRMED"
}
```

Allowed statuses:

```text
PENDING
CONFIRMED
CANCELLED
```

---

# 15. Delete Reservation

ADMIN only:

```http
DELETE /api/reservations/1
```

---

# 16. Security

The application uses:

* Spring Security
* JWT authentication
* BCrypt password hashing
* Stateless session management
* Role-based authorization
* JWT authentication filter

Authentication header:

```http
Authorization: Bearer <JWT_TOKEN>
```

Unauthorized requests return:

```text
401 UNAUTHORIZED
```

Authenticated users without sufficient permission return:

```text
403 FORBIDDEN
```

---

# 17. User Ownership

Reservation ownership is controlled using the authenticated user's identity from JWT.

The application does not trust a `userId` supplied by the client.

For example:

```text
JWT
 ↓
username
 ↓
UserDetails
 ↓
Authenticated User
 ↓
Reservation ownership check
```

Therefore:

```text
USER → Own reservations only
ADMIN → All reservations
```

---

# 18. Exception Handling

The application provides centralized exception handling for common errors.

Examples:

```text
400 BAD REQUEST
401 UNAUTHORIZED
403 FORBIDDEN
404 NOT FOUND
```

Examples of validation errors:

* Invalid request data
* Resource not found
* Reservation not found
* Invalid reservation time
* Resource unavailable
* Invalid price range

---

# 19. Testing

The project includes unit tests using:

* JUnit 5
* Mockito
* Spring MVC Test

Test coverage includes:

### Authentication

* Signup success
* Duplicate username
* Duplicate email
* Login success
* User not found

### Resource

* Create resource
* Get resource
* Get all resources
* Update resource
* Delete resource
* Resource not found

### Reservation

* Create reservation
* Invalid reservation time
* Past start time
* Resource unavailable
* Get own reservation
* ADMIN reservation access
* USER ownership restriction
* Reservation filtering
* Pagination
* Update reservation authorization
* Delete reservation authorization

### Controller Tests

Controller endpoints are tested using `MockMvc`.

Run tests:

```bash
mvn test
```

---

# 20. API Documentation

Swagger / OpenAPI documentation is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

---

# 21. Default Roles

The application supports:

```text
ADMIN
USER
```

Normal signup creates:

```text
USER
```

ADMIN users should be seeded/configured separately.

---

# 22. HTTP Status Codes

| Status | Meaning                                 |
| ------ | --------------------------------------- |
| 200    | Request successful                      |
| 201    | Resource created                        |
| 400    | Invalid request / validation error      |
| 401    | Authentication required / invalid token |
| 403    | Access denied                           |
| 404    | Resource not found                      |
| 500    | Internal server error                   |

---

# 23. Database Relationships

### User → Reservation

One user can have multiple reservations.

```text
User
  |
  | 1
  |
  | *
Reservation
```

### Resource → Reservation

One resource can have multiple reservations.

```text
Resource
   |
   | 1
   |
   | *
Reservation
```

Therefore, `Reservation` contains:

```java
@ManyToOne
private User user;

@ManyToOne
private Resource resource;
```

---

# 24. Application Flow

### Login Flow

```text
Client
  ↓
POST /auth/login
  ↓
AuthenticationManager
  ↓
UserDetailsService
  ↓
BCrypt Password Verification
  ↓
JWT Generation
  ↓
Access Token + Refresh Token
```

### Protected API Flow

```text
Client
  ↓
Authorization: Bearer JWT
  ↓
JwtAuthenticationFilter
  ↓
Extract Username
  ↓
Load UserDetails
  ↓
Validate JWT
  ↓
SecurityContext
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
Database
```

---

# 25. Future Improvements

Possible future enhancements:

* Refresh token endpoint
* Email notifications
* Reservation conflict detection
* Docker Compose
* Production environment configuration
* Redis caching
* Audit logging
* CI/CD pipeline

---

# 26. Author

Developed as a Backend Developer Assignment using Java and Spring Boot.

**Technology:** Java 17 + Spring Boot + Spring Security + JWT + JPA/Hibernate + MySQL
