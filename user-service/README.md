# User Service

Manages user authentication, registration, and basic profile information.

## Prerequisites

- Java 21
- MySQL 8.0 (via Docker)

## Quick Start

### 1. Start MySQL

```bash
docker compose up -d
```

Creates a MySQL container on port `5555` with database `vbank_users`, user `vbank_user` / `vbank_pass`.

### 2. Build and run

```bash
./mvnw spring-boot:run -pl user-service
```

Starts on `http://localhost:8081`.

### 3. Test

```bash
# Register
curl -X POST http://localhost:8081/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john.doe","password":"securePassword123","email":"john.doe@example.com","firstName":"John","lastName":"Doe"}'

# Login
curl -X POST http://localhost:8081/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john.doe","password":"securePassword123"}'

# Profile (requires X-User-Id header matching the userId from register/login)
curl -X GET http://localhost:8081/users/{userId}/profile \
  -H "X-User-Id: {userId}"
```

Swagger UI: <http://localhost:8081/swagger-ui.html>

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/users/register` | No | Register new user |
| POST | `/users/login` | No | Authenticate user |
| GET | `/users/{userId}/profile` | `X-User-Id` header | Get user profile |

## Package Structure

```
com.vbank.userservice
├── config/          # Bean configuration (PasswordEncoder, WebConfig)
├── controller/      # HTTP layer (thin controllers)
├── service/         # Business logic interface
│   └── impl/        # Business logic implementation
├── repository/      # Spring Data JPA repositories
├── entity/          # JPA entities
├── dto/
│   ├── request/     # Inbound DTOs with validation
│   └── response/    # Outbound DTOs
├── mapper/          # Entity ⇄ DTO mapping
├── exception/       # Custom exceptions + GlobalExceptionHandler
└── security/        # Gateway auth interceptor
```

## Architecture

```
Client → WSO2 Gateway (OAuth2) → X-User-Id header → User Service → MySQL
```

The WSO2 API Gateway handles OAuth2. Internal services trust the `X-User-Id` header forwarded by the gateway. When testing directly (bypassing the gateway), you set this header manually.

## Layered Architecture Diagram

```
                     HTTP Request
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│                 GatewayAuthInterceptor              │  ← security/
│  Validates X-User-Id header, stashes UUID as attr   │
└──────────────────────┬──────────────────────────────┘
                       │ passes through
┌──────────────────────▼──────────────────────────────┐
│                  UserController                     │  ← controller/
│  receive HTTP → call service → return response.     |
| No logic, no try/catch.                             │
└──────────────────────┬──────────────────────────────┘
                       │ delegates to interface
┌──────────────────────▼──────────────────────────────┐
│               UserService (interface)               │  ← service/
│  Contract: register(), login(), getProfile()        │
└──────────────────────┬──────────────────────────────┘
                       │ implemented by
┌──────────────────────▼──────────────────────────────┐
│               UserServiceImpl                       │  ← service/impl/
│  ALL business logic lives here:                     │
│  • Password hashing via PasswordEncoder (BCrypt)    │
│  • Login credential validation                      │
│  • Status checks (active/suspended)                 │
│  • Calls repository → gets entity → calls mapper →  │
│    returns DTO                                      │
└──┬──────────────┬──────────────┬────────────────────┘
   │              │              │
   ▼              ▼              ▼
┌─────────┐ ┌──────────┐ ┌──────────┐
│UserRepo │ │Password  │ │UserMapper│
│(JPA)    │ │Encoder   │ │          │
└────┬────┘ └──────────┘ └────┬─────┘
     │                        │
     ▼                        ▼
┌─────────┐           ┌───────────────┐
│   DB    │           │    DTOs       │
│ (MySQL) │           │ • RegisterReq │
└─────────┘           │ • LoginReq    │
                      │ • UserRegResp │
                      │ • LoginResp   │
                      │ • ProfileResp │
                      └───────────────┘
```

## Data Flow Diagram For A Request - POST /users/register

```
Client sends JSON body
       │
       ▼
Tomcat receives on port 8081
       │
       ▼
UserController.register(@Valid @RequestBody RegisterRequest)
       │  Jackson deserializes JSON → RegisterRequest POJO
       │  @Valid triggers Bean Validation constraints
       │  (if validation fails → GlobalExceptionHandler returns 400)
       ▼
UserService.register(RegisterRequest)   ← depends on interface, not impl
       │
       ▼
UserServiceImpl.register()
       ├── userRepository.existsByUsername()  ← checks DB
       ├── userRepository.existsByEmail()     ← checks DB  
       ├── passwordEncoder.encode(password)   ← BCrypt hash
       ├── User.builder()...build()           ← creates entity
       ├── userRepository.save(user)          ← inserts to MySQL
       └── userMapper.toRegisteredResponse()  ← entity → DTO
       │
       ▼
Returns UserRegisteredResponse DTO  ← never exposes entity
       │
       ▼
Controller wraps in ResponseEntity.status(CREATED)
       │
       ▼
Jackson serializes DTO → JSON response to client
```

## Ports

User Service uses port `8081`.
