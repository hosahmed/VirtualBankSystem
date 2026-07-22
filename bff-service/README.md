# BFF Service

Aggregates User Service, Account Service, and Transaction Service into a single frontend-optimized response. Per project convention, this is the **only** service allowed to call other microservices directly.

## Prerequisites

- Java 21
- User Service running on port **8081**
- Account Service running on port **8082**
- Transaction Service running on port **8083**

## Quick Start

### 1. Start dependent services

```bash
docker compose up -d
```

Starts MySQL, User Service, BFF Service, Account Service and Transaction Service.

### 2. Test

```bash
# Dashboard (aggregates profile, accounts, transactions)
curl -X GET http://localhost:8084/bff/dashboard/{userId}
```

Swagger UI: <http://localhost:8084/swagger-ui.html>

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/bff/dashboard/{userId}` | Aggregated dashboard for a user |

## Package Structure

```
com.vbank.bffservice
├── config/          # WebClient bean definitions, OpenAPI config
├── controller/      # HTTP layer (thin controller)
├── service/         # Business logic interface
│   └── impl/        # Aggregation orchestration
├── client/          # WebClient-based downstream service clients
├── dto/
│   └── response/    # Outbound DTOs
└── exception/       # Custom exceptions + GlobalExceptionHandler
```

## Architecture

```
Client → WSO2 Gateway (OAuth2) → BFF Service → User Service (8081)
                                             → Account Service (8082)
                                             → Transaction Service (8083)
```

The BFF sits behind the same WSO2 API Gateway as every other service. It is the only service that calls multiple microservices directly — all other services are strictly single-domain.

## Layered Architecture Diagram

```
                     HTTP Request (GET /bff/dashboard/{userId})
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│                    BffController                          │  ← controller/
│  receive HTTP → call service → return response.           │
│  No logic, no try/catch.                                  │
└──────────────────────┬───────────────────────────────────┘
                       │ delegates to interface
┌──────────────────────▼───────────────────────────────────┐
│               DashboardService (interface)                │  ← service/
│  Contract: getDashboard(userId)                           │
└──────────────────────┬───────────────────────────────────┘
                       │ implemented by
┌──────────────────────▼───────────────────────────────────┐
│               DashboardServiceImpl                        │  ← service/impl/
│  ALL orchestration logic lives here:                      │
│  • Calls UserServiceClient for profile                    │
│  • Calls AccountServiceClient for accounts                │
│  • For each account, calls TransactionServiceClient       │
│    (async, concurrency=8)                                 │
│  • Combines all into DashboardResponse                    │
│  • 5-second timeout on the whole aggregation              │
└──┬──────────────┬──────────────┬─────────────────────────┘
   │              │              │
   ▼              ▼              ▼
┌──────────┐ ┌──────────┐ ┌──────────────┐
│UserSvc   │ │AccountSvc│ │TransactionSvc│
│Client    │ │Client    │ │Client        │
└────┬─────┘ └────┬─────┘ └──────┬───────┘
     │            │              │
     ▼            ▼              ▼
┌──────────┐ ┌──────────┐ ┌──────────────┐
│ User Svc │ │Account   │ │ Transaction  │
│ (8081)   │ │Svc(8082) │ │ Svc  (8083)  │
└──────────┘ └──────────┘ └──────────────┘
```

## Data Flow Diagram For A Request - GET /bff/dashboard/{userId}

```
Client calls GET /bff/dashboard/{userId}
       │
       ▼
Tomcat receives on port 8084
       │
       ▼
BffController.getDashboard(userId)
       │  delegates to service interface
       ▼
DashboardServiceImpl.getDashboard(userId)
       │
       ├── userServiceClient.getProfile(userId)   ──→ HTTP GET /users/{userId}/profile
       │                                                 │
       │                                                 ▼ returns UserProfileDto
       │
       ├── accountServiceClient.getAccountsForUser(userId) ──→ HTTP GET /users/{userId}/accounts
       │                                                       │
       │                                                       ▼ returns Flux<AccountDto>
       │
       │   For each account (flatMap, concurrency=8):
       │   └── transactionServiceClient.getTransactionsForAccount(accountId)
       │                                                     ──→ HTTP GET /accounts/{accountId}/transactions
       │                                                         │
       │                                                         ▼ returns Flux<TransactionDto>
       │
       └── Combine → DashboardResponse(user profile + accounts with transactions)
       │
       ▼
Controller wraps in ResponseEntity.ok()
       │
       ▼
Jackson serializes DTO → JSON response to client
```

## Ports

BFF Service uses port `8084`.
