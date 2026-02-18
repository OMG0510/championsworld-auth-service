# Auth Service – Champions World

A Spring Boot–based microservice responsible for authentication, authorization, and customer identity management within the Champions World E-Commerce Microservices Architecture.

---

## Tech Stack

- **Java 17**
- **Spring Boot** + **Spring Security**
- **JWT** (Bearer Token Authentication)
- **PostgreSQL**
- **Maven**
- **Google OAuth 2.0**

---

## Features

### Authentication
- Email + Password login (Admin)
- Phone OTP login
- Email OTP login
- Google OAuth login

### Registration
- Email-based registration with OTP verification
- Phone-based registration with OTP verification

### Security
- JWT token generation & validation
- Role-based access control (SUPER_ADMIN, ADMIN, CUSTOMER)
- BCrypt password hashing

### Admin Management
- Register Admin *(SUPER_ADMIN only)*
- Activate / Deactivate Admin
- List all Admins
- Hard delete Admin

### Password Management
- Forgot Password (OTP-based)
- Reset Password

### Customer
- Get logged-in user profile (`/auth/me`)
- Add, update, delete, and list addresses

---

## Role Structure

| Role        | Permissions                                      |
|-------------|--------------------------------------------------|
| SUPER_ADMIN | Manage admins, full platform access              |
| ADMIN       | Platform management                              |
| CUSTOMER    | Login, register, manage profile & addresses      |

---

## API Reference

### Admin Auth — `/auth`

| Method | Endpoint                        | Description              |
|--------|---------------------------------|--------------------------|
| POST   | `/auth/register/admin`          | Register a new admin     |
| POST   | `/auth/login`                   | Admin email/password login |
| PUT    | `/auth/admin/{adminId}/status`  | Activate/deactivate admin |
| GET    | `/auth/show-admins`             | List all admins          |
| DELETE | `/auth/admin/delete/{adminId}`  | Hard delete an admin     |

### Common Auth — `/auth`

| Method | Endpoint                  | Description               |
|--------|---------------------------|---------------------------|
| GET    | `/auth/validate-token`    | Validate a JWT token      |
| POST   | `/auth/forgot-password`   | Request OTP for password reset |
| POST   | `/auth/reset-password`    | Reset password using OTP  |

### Customer Auth — `/auth`

| Method | Endpoint                              | Description                     |
|--------|---------------------------------------|---------------------------------|
| POST   | `/auth/login/otp/phone/send`          | Send OTP to phone               |
| POST   | `/auth/login/otp/phone/verify`        | Verify phone OTP & login        |
| POST   | `/auth/login/otp/email/send`          | Send OTP to email               |
| POST   | `/auth/login/otp/email/verify`        | Verify email OTP & login        |
| POST   | `/auth/register/email/start`          | Start email registration        |
| POST   | `/auth/register/email/verify`         | Verify email OTP                |
| POST   | `/auth/register/email/complete`       | Complete email registration     |
| POST   | `/auth/register/phone/start`          | Start phone registration        |
| POST   | `/auth/register/phone/verify`         | Verify phone OTP                |
| POST   | `/auth/register/mobile/complete`      | Complete phone registration     |
| POST   | `/auth/oauth/google`                  | Google OAuth login              |
| GET    | `/auth/me`                            | Get logged-in user profile      |

### Customer Addresses — `/customer/addresses`

| Method | Endpoint                              | Description         |
|--------|---------------------------------------|---------------------|
| POST   | `/customer/addresses`                 | Add address         |
| GET    | `/customer/addresses`                 | Get all addresses   |
| PUT    | `/customer/addresses/{addressId}`     | Update address      |
| DELETE | `/customer/addresses/{addressId}`     | Delete address      |

---

## Authentication Flow

1. User authenticates via Password, OTP, or Google OAuth
2. Server validates credentials and issues a JWT
3. Client includes token in all subsequent requests: `Authorization: Bearer <token>`
4. JWT filter validates the token on each request
5. Spring Security enforces role-based access control

---

## Getting Started

**Prerequisites:** Java 17, Maven, PostgreSQL

```bash
# 1. Clone the repository
git clone <repository-url>
cd auth-service

# 2. Configure your database and secrets
cp src/main/resources/application.yml.example src/main/resources/application.yml
# Edit application.yml with your DB credentials, JWT secret, and Google OAuth config

# 3. Run the application
mvn spring-boot:run
```

### Key `application.yml` Properties

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: your_db_user
    password: your_db_password

jwt:
  secret: your_jwt_secret
  expiration: 86400000  # 24 hours in ms

google:
  oauth:
    client-id: your_google_client_id
```

---

## Architecture

This service is part of a larger microservices ecosystem. Each service owns its database and is independently deployable.

```
Champions World Platform
├── Auth Service          ← This service
├── Catalogue Service
├── Cart & Order Service
└── Payment Service
```

---

## Author

**Om** — Java Backend Developer | Microservices Enthusiast
