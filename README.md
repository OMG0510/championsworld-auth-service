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
