# Auth Service – Champions World

A Spring Boot–based microservice responsible for authentication, authorization, and customer identity management within the Champions World E-Commerce Microservices Architecture.

---

## Tech Stack

- **Java 21**
- **Spring Boot** + **Spring Security**
- **JWT** (Bearer Token Authentication)
- **PostgreSQL**
- **Maven**
- **Google OAuth 2.0**
- **MSG91** (SMS OTP Service)
- **JavaMailSender** (Email OTP / SMTP)

---

## Project Structure

```
src/main/java/com/shopping/b2c_ecommerce/
│
├── config/
│   ├── CorsGlobalConfig.java         # Global CORS configuration
│   ├── CorsProperties.java           # CORS properties binding
│   ├── PasswordConfig.java           # BCrypt password encoder bean
│   └── SecurityConfig.java           # Spring Security filter chain & access rules
│
├── controller/
│   ├── AdminController.java          # Admin management endpoints
│   ├── CommonController.java         # Token validation & password reset
│   ├── CustomerController.java       # Customer auth (OTP, OAuth, /me)
│   └── CustomerAddressController.java# Address CRUD endpoints
│
├── dto/                              # Request & Response objects
│   ├── AddAddressRequest.java
│   ├── AddressResponse.java
│   ├── AdminStatusRequest/Response.java
│   ├── AdminSummaryResponse.java
│   ├── EmailOtpSendRequest/VerifyRequest.java
│   ├── ErrorResponse.java
│   ├── ForgotPasswordRequest.java
│   ├── GoogleCodeRequest / LoginRequest / OAuthRequest / TokenResponse / UserInfo.java
│   ├── LoginRequest / LoginResponse.java
│   ├── MeResponse.java
│   ├── OtpSendRequest / OtpVerifyRequest.java
│   ├── RegisterRequest / RegisterStepRequest.java
│   ├── ResetPasswordRequest.java
│   ├── TokenValidationResponse.java
│   ├── UserAddressResponse.java
│   └── UserIdentity.java
│
├── entity/
│   ├── Address.java
│   ├── PasswordResetToken.java
│   ├── Role.java
│   ├── User.java
│   └── UserRole.java
│
├── enums/
│   └── AuthProvider.java             # EMAIL, PHONE, GOOGLE
│
├── exception/
│   ├── GlobalExceptionHandler.java   # Centralized @ControllerAdvice handler
│   ├── AccountInactiveException.java
│   ├── AddressNotFoundException.java
│   ├── EmailOtpSendException.java
│   ├── EmailSendException.java
│   ├── GoogleOAuthException.java
│   ├── GoogleUserInfoException.java
│   ├── InvalidCredentialsException.java
│   ├── OtpNotVerifiedException.java
│   ├── OtpSendFailedException.java
│   ├── OtpVerificationException.java
│   ├── PasswordMismatchException.java
│   ├── PasswordResetTokenInvalidException.java
│   ├── PasswordResetTokenNotFoundException.java
│   ├── PasswordResetUserNotFoundException.java
│   ├── RoleNotAssignedException.java
│   ├── RoleNotFoundException.java
│   ├── UnauthorizedAddressAccessException.java
│   ├── UnauthorizedAdminActionException.java
│   ├── UserAlreadyExistsException.java
│   ├── UserNotAdminException.java
│   └── UserNotFoundException.java
│
├── repository/
│   ├── AddressRepository.java
│   ├── PasswordResetTokenRepository.java
│   ├── RoleRepository.java
│   ├── UserRepository.java
│   └── UserRoleRepository.java
│
├── security/
│   ├── JwtAuthenticationFilter.java  # Per-request JWT validation filter
│   └── JwtUtil.java                  # JWT generation, parsing & validation
│
└── service/
    ├── AddressService.java
    ├── AuthService.java
    ├── EmailOtpService.java
    ├── EmailService.java
    ├── GoogleOAuthService.java
    ├── OtpService.java
    ├── PasswordResetService.java
    ├── RoleService.java
    └── UserService.java
```

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
| POST   | `/auth/register/phone/complete`       | Complete phone registration     |
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

## OTP & Messaging Infrastructure

### SMS OTP — MSG91

Phone-based OTP (login, registration, forgot password) is delivered via **MSG91**, a cloud communication platform widely used for transactional SMS in India.

**DLT Registration:**
As mandated by TRAI for commercial SMS in India, the sender ID and SMS templates have been registered on the **STPL (Smartping) DLT platform**. This ensures regulatory compliance and guaranteed SMS delivery to Indian mobile numbers.

- **DLT Platform:** STPL (Smartping) — [smartping.ai](https://smartping.ai)
- **Entity Type:** Registered Business Entity
- **Template Type:** Transactional OTP
- **Sender ID:** Registered under the client's business entity

**How it works:**
1. A request is made to the MSG91 API with the recipient's phone number and the registered template ID.
2. MSG91 generates and dispatches a time-bound OTP SMS to the user.
3. The OTP is stored server-side (hashed / in-memory with TTL) and validated upon the user's submission.
4. OTPs are single-use and expire after a configurable duration (e.g., 5 minutes).

**Key `application.yml` properties for MSG91:**

```yaml
msg91:
  auth-key: your_msg91_auth_key
  template-id: your_dlt_registered_template_id
  sender-id: your_registered_sender_id
  otp-expiry-minutes: 5
```

---

### Email OTP — JavaMailSender (SMTP)

Email-based OTP (login, registration, forgot password) is sent using **Spring Boot's JavaMailSender** configured with the client's business email account and its **App Password** (used in place of the actual account password for secure SMTP authentication, typically generated from Google Workspace or Gmail account settings).

**Configuration details:**
- **SMTP Host:** `smtp.gmail.com`
- **SMTP Port:** `587` (TLS/STARTTLS)
- **Auth:** Enabled via App Password (not the actual account password)
- **From Address:** Client's registered business email address
- **Transport Protocol:** SMTP with STARTTLS encryption

**How it works:**
1. A `SimpleMailMessage` or `MimeMessage` is constructed with the recipient's email, a subject line, and an OTP body.
2. `JavaMailSender.send()` dispatches the email through the configured SMTP server.
3. The OTP is stored server-side and validated upon submission, with a configurable expiry window.
4. Each OTP is single-use; re-requesting invalidates any previously issued OTP for that email.

**Key `application.yml` properties for SMTP:**

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_client_email@gmail.com
    password: your_app_password       # App Password, not the actual account password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
        debug: false

mail:
  from: your_client_email@gmail.com
  otp-expiry-minutes: 5
  subject-otp: "Your OTP for Champions World"
```

> **Note:** The App Password is generated from the email account's security settings (e.g., Google Account → Security → 2-Step Verification → App Passwords). It should be stored securely and never committed to version control. Use environment variables or a secrets manager in production.

---

## API Request & Response Reference

Full request/response examples for all endpoints (including body schemas, success responses, and error scenarios) are documented in the API reference document:

📄 **[Auth Service – Full API Documentation](https://drive.google.com/file/d/1fCc2iToO-H7MVDZZMr764P2XnM9NhELq/view?usp=sharing)**

This document covers all 24 endpoints across `AdminAuthController`, `CommonAuthController`, `CustomerAuthController`, and `CustomerAddressController`, and is intended for frontend developers, QA testers, and integrating backend services.

---

## OTP Storage Strategy

OTPs for both SMS and email flows are stored **in-memory** on the server side using a `Map` with expiry tracking.

- Each OTP entry is keyed by the user's phone number or email address.
- OTPs are single-use — once verified, the entry is removed from memory immediately.
- If a new OTP is requested before the previous one expires, the old entry is overwritten, effectively invalidating it.
- OTPs expire after a configured TTL (default: 5 minutes).

> **Note:** Since OTPs are stored in application memory, they do not survive a server restart. This is acceptable for a stateless OTP flow. For multi-instance deployments, migrating OTP storage to a distributed cache like **Redis** with TTL is recommended.

---

## Error Handling

Errors are handled centrally via a `GlobalExceptionHandler` class annotated with `@ControllerAdvice`. Every custom exception in the `exception/` package maps to a specific HTTP status and returns a human-readable error message directly in the response body as a plain string.

This means there is no generic error envelope — the response body itself is the error description, making it straightforward for clients to display messages directly.

Example error responses:

| Scenario | HTTP Status | Response Body |
|---|---|---|
| Invalid credentials | `401 Unauthorized` | `"Invalid email or password"` |
| OTP expired or wrong | `400 Bad Request` | `"Invalid or expired OTP"` |
| User not found | `404 Not Found` | `"User not found"` |
| Account deactivated | `403 Forbidden` | `"Account is inactive"` |
| Unauthorized action | `403 Forbidden` | `"You are not authorized to perform this action"` |
| Address not found | `404 Not Found` | `"Address not found or does not belong to this user"` |
| User already exists | `409 Conflict` | `"User already exists with this email/phone"` |
| Password mismatch | `400 Bad Request` | `"Passwords do not match"` |

Custom exception classes such as `InvalidCredentialsException`, `OtpVerificationException`, `UserNotFoundException`, and others are thrown from the service layer and caught by `GlobalExceptionHandler`, which sets the appropriate HTTP status and writes the message to the response.

---

## JWT Token

The service uses **HS256-signed JWTs** for stateless authentication. Tokens are generated upon successful login and must be included in the `Authorization` header for all protected endpoints.

**Header:**
```
Authorization: Bearer <token>
```

**Token Payload (Claims):**

| Claim | Type | Description |
|---|---|---|
| `sub` | String | User's email address (subject) |
| `userId` | Long | Internal user ID |
| `role` | String | Assigned role: `SUPER_ADMIN`, `ADMIN`, or `CUSTOMER` |
| `iat` | Timestamp | Issued-at time (Unix epoch, seconds) |
| `exp` | Timestamp | Expiry time (Unix epoch, seconds) |

**Example decoded payload:**
```json
{
  "sub": "admin1@example.com",
  "userId": 5,
  "role": "ADMIN",
  "iat": 1771414628,
  "exp": 1771501028
}
```

- Default token validity: **24 hours** (configurable via `jwt.expiration` in ms)
- Signing algorithm: **HS256**
- Token validation is performed on every request by `JwtAuthenticationFilter` before the request reaches the controller.

---

## Getting Started

**Prerequisites:** Java 21, Maven, PostgreSQL

```bash
# 1. Clone the repository
git clone <repository-url>
cd auth-service

# 2. Configure your database and secrets
cp src/main/resources/application.yml.example src/main/resources/application.yml
# Edit application.yml with your DB credentials, JWT secret, Google OAuth config,
# MSG91 credentials, and SMTP / JavaMailSender config

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

  mail:
    host: smtp.gmail.com
    port: 587
    username: your_client_email@gmail.com
    password: your_app_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

jwt:
  secret: your_jwt_secret
  expiration: 86400000  # 24 hours in ms

google:
  oauth:
    client-id: your_google_client_id

msg91:
  auth-key: your_msg91_auth_key
  template-id: your_dlt_registered_template_id
  sender-id: your_registered_sender_id
  otp-expiry-minutes: 5

mail:
  from: your_client_email@gmail.com
  otp-expiry-minutes: 5
  subject-otp: "Your OTP for Champions World"
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
