# SecureAuth Backend

A secure RESTful authentication and user management backend built with **Java, Spring Boot, Spring Security, JWT, and PostgreSQL**.

SecureAuth provides user registration, login, JWT-based authentication, protected user profile access, and server-side logout with token revocation.

## Overview

SecureAuth is a backend authentication system designed to demonstrate how modern authentication and authorization mechanisms work in a Spring Boot application.

Users can create an account using their name, unique user ID, email, and password. After successful login, the server generates a JSON Web Token (JWT) that can be used to access protected endpoints.

The application also implements server-side JWT revocation, ensuring that a logged-out token cannot be reused before its original expiration time.

## Features

- **User Registration:** Register with name, unique user ID, email, and password.
- **Secure Password Storage:** Passwords are hashed using BCrypt.
- **User Login:** Authenticate users using email and password.
- **JWT Authentication:** Generate signed JWT access tokens after successful authentication.
- **Token Expiration:** Access tokens expire after 5 minutes (300,000 milliseconds).
- **Protected Endpoints:** Restrict access to authenticated users.
- **User Profile:** Retrieve the currently authenticated user's information.
- **Server-Side Logout:** Revoke JWT tokens and prevent their future reuse.
- **Token Revocation Validation:** Validate token status against the database on protected requests.
- **Role-Based User Model:** Support USER and ADMIN roles.
- **Input Validation:** Validate registration and login requests.
- **Centralized Exception Handling:** Handle validation, authentication, and application errors.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 24 | Programming Language |
| Spring Boot 4.1.2 | Backend Framework |
| Spring Security | Authentication and Security |
| Spring Security OAuth2 Resource Server | JWT Validation |
| Spring Data JPA | Database Operations |
| Hibernate | ORM |
| PostgreSQL 18 | Relational Database |
| Maven | Build and Dependency Management |
| JWT (HS256) | Token-Based Authentication |
| BCrypt | Password Hashing |
| Jakarta Validation | Request Validation |
| Postman | API Testing |

## Architecture

The project follows a layered architecture to separate responsibilities and make the application easier to maintain.

```text
src/main/java/com/example/secureauth/
│
├── config/
│   ├── JwtConfig.java
│   ├── PasswordConfig.java
│   └── SecurityConfig.java
│
├── controller/
│   ├── AuthController.java
│   └── UserController.java
│
├── dto/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   ├── ApiResponse.java
│   └── UserProfileResponse.java
│
├── entity/
│   ├── User.java
│   └── RevokedToken.java
│
├── enums/
│   └── Role.java
│
├── exception/
│   └── GlobalExceptionHandler.java
│
├── repository/
│   ├── UserRepository.java
│   └── RevokedTokenRepository.java
│
├── security/
│   ├── JwtService.java
│   └── JwtRevocationValidator.java
│
└── service/
    ├── AuthService.java
    └── UserDetailsServiceImpl.java
```

## Authentication Flow

### 1. Registration

A new user submits their name, user ID, email, and password.

- The backend validates the submitted information.
- Duplicate email addresses and user IDs are rejected.
- The password is hashed using BCrypt.
- The default role is assigned as `USER`.
- The user information is stored in PostgreSQL.

Public registration does not accept a user-selected role.

### 2. Login

The user submits their email and password.

- Spring Security authenticates the credentials.
- The backend generates a signed JWT using the HS256 algorithm.
- The JWT contains the user's email as its subject, user ID, role, issue time, expiration time, and a unique token ID (`jti`).
- The access token is returned to the client.

### 3. Accessing Protected Resources

The client sends the JWT in the Authorization header:

```http
Authorization: Bearer YOUR_ACCESS_TOKEN
```

Spring Security validates the JWT signature, expiration, and revocation status before allowing access to protected endpoints.

### 4. Logout and Token Revocation

SecureAuth implements server-side JWT revocation.

When a user logs out:

1. The backend extracts the JWT from the authenticated request.
2. It retrieves the token's unique `jti`.
3. The token ID and expiration time are saved in the `revoked_tokens` table.
4. Future requests using that token are rejected by the JWT revocation validator.

This prevents the logged-out token from being reused even if it has not reached its original expiration time.

## JWT Configuration

| Property | Value |
|---|---|
| Algorithm | HS256 |
| Token Type | Bearer |
| Expiration Time | 5 Minutes |
| Expiration in Milliseconds | 300000 |
| Token Identifier | UUID (`jti`) |
| Token Revocation | Database-Based |

The JWT secret is configured through application properties and must be kept private.

## REST API Documentation

**Base URL**

```text
http://localhost:8080
```

### 1. Register User

**Endpoint:** `POST /api/auth/register`

**Access:** Public

Request Body:

```json
{
  "name": "Naimul Islam",
  "userId": "naimul1086",
  "email": "naimul@example.com",
  "password": "SecurePass123"
}
```

Successful Response — `201 Created`

```json
{
  "success": true,
  "message": "Registration successful"
}
```

### 2. Login

**Endpoint:** `POST /api/auth/login`

**Access:** Public

Request Body:

```json
{
  "email": "naimul@example.com",
  "password": "SecurePass123"
}
```

Successful Response — `200 OK`

```json
{
  "accessToken": "YOUR_JWT_ACCESS_TOKEN",
  "tokenType": "Bearer",
  "expiresIn": 300000,
  "id": 1,
  "userId": "naimul1086",
  "name": "Naimul Islam",
  "email": "naimul@example.com",
  "role": "USER"
}
```

### 3. Get Current User Profile

**Endpoint:** `GET /api/users/me`

**Access:** Authenticated

Authorization Header:

```http
Authorization: Bearer YOUR_JWT_ACCESS_TOKEN
```

Successful Response — `200 OK`

```json
{
  "id": 1,
  "userId": "naimul1086",
  "name": "Naimul Islam",
  "email": "naimul@example.com",
  "role": "USER"
}
```

### 4. Logout

**Endpoint:** `POST /api/auth/logout`

**Access:** Authenticated

Authorization Header:

```http
Authorization: Bearer YOUR_JWT_ACCESS_TOKEN
```

Request Body: None

Successful Response — `200 OK`

```json
{
  "success": true,
  "message": "Logout successful"
}
```

After successful logout, the same token cannot be used to access protected resources.

Expected result when reusing a revoked token:

```http
401 Unauthorized
```

## Database Design

SecureAuth currently uses two database entities.

### Users Table

Stores registered user information.

| Column | Description |
|---|---|
| id | Primary key |
| user_id | Unique user identifier |
| name | User's full name |
| email | Unique email address |
| password | BCrypt-hashed password |
| role | USER or ADMIN |

### Revoked Tokens Table

Stores revoked JWT identifiers.

| Column | Description |
|---|---|
| jti | Unique JWT identifier |
| expires_at | Original token expiration time |

Hibernate automatically manages the database schema using the configured JPA settings.

## Getting Started

Follow these steps to run the backend locally.

### Prerequisites

Install the following:

- Java JDK 24
- Apache Maven
- PostgreSQL 18 or compatible version
- IntelliJ IDEA (recommended)
- Postman (for API testing)

### 1. Clone the Repository

```bash
git clone https://github.com/naimul1086/SecureAuth.git
```

Navigate to the project directory:

```bash
cd SecureAuth
```

### 2. Create PostgreSQL Database

Open PostgreSQL and execute:

```sql
CREATE DATABASE secureauth_db;
```

### 3. Configure Application Properties

Open:

```text
src/main/resources/application.properties
```

Add your local database credentials and JWT secret:

```properties
spring.application.name=secureauth

# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/secureauth_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_DATABASE_PASSWORD

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

# Server Configuration
server.port=8080

# JWT Configuration
app.jwt.expiration=300000
app.jwt.secret=YOUR_64_CHARACTER_RANDOM_SECRET
```

**Important Security Notice:**

- Replace the database password with your own PostgreSQL password.
- Generate a strong, random JWT secret.
- Never commit real database credentials or JWT secrets to GitHub.
- Keep local secrets in your own configuration and use environment variables or an external secret manager for production.

### 4. Run the Application

Using Maven:

```bash
mvn spring-boot:run
```

Or run the main application class from IntelliJ IDEA.

After successful startup, the backend will be available at:

```text
http://localhost:8080
```

## API Testing with Postman

The following authentication flow has been tested:

| Test Case | Expected Result |
|---|---|
| Register a new user | 201 Created |
| Login with valid credentials | 200 OK |
| Access profile using valid JWT | 200 OK |
| Logout using valid JWT | 200 OK |
| Reuse revoked JWT | 401 Unauthorized |

The backend was tested locally using Postman and PostgreSQL.

## Security Implementation

The project includes the following security mechanisms:

- BCrypt password hashing instead of storing plain-text passwords.
- Stateless authentication using JWT.
- HS256 JWT signature verification.
- Five-minute token expiration.
- Server-side token revocation using a database.
- Protected API endpoints using Spring Security.
- Public registration always assigns the USER role.
- Request validation and centralized exception handling.

## Future Improvements

Planned improvements for future versions:

- Android client integration using Kotlin and XML.
- Retrofit-based API communication.
- MVVM architecture on the Android client.
- Secure token storage on Android.
- Refresh token implementation.
- Role-based authorization for administrative endpoints.
- Automatic cleanup of expired revoked-token records.
- HTTPS deployment and production configuration.
- Automated integration and security tests.

## Author

**MD Naimul Islam**

- GitHub: [@naimul1086](https://github.com/naimul1086)
- LinkedIn: [naimul1086](https://linkedin.com/in/naimul1086)

---

*SecureAuth Backend — A practical implementation of JWT-based authentication and server-side token revocation using Spring Boot and PostgreSQL.*