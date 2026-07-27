# BFF Service (Backend for Frontend)

Aggregates User Service, Account Service, and Transaction Service into
a single frontend-optimized response. Per project convention (see
[`docs/OPENCODE.md`](../docs/OPENCODE.md) §2), this is the **only**
service allowed to call other microservices directly — User, Account,
and Transaction never call each other.

## ⚠️ Built against assumed contracts — reconciliation needed

This service was scaffolded **before** the real Account Service and
Transaction Service code was available. Every downstream DTO
(`AccountDto`, `TransactionDto`) and every client's endpoint path
(`AccountServiceClient`, `TransactionServiceClient`) mirrors the
**project spec's documented JSON examples**, not verified real code.

**Before trusting this service's output, reconcile:**

1. **Field names.** Open `dto/response/AccountDto.java` and
   `TransactionDto.java` and diff every field against what your
   friend's services actually return. A renamed field (e.g. `type`
   instead of `accountType`) deserializes silently to `null` — it
   won't throw an error, it'll just quietly produce an incomplete
   dashboard. This is the single most dangerous failure mode here.
2. **Base URLs and ports.** `application.yml` assumes Account Service
   on `8082` and Transaction Service on `8083` (per `docs/PORTS.md`).
   Confirm those are the actual ports/hosts, or override via
   `ACCOUNT_SERVICE_URL` / `TRANSACTION_SERVICE_URL` env vars.
3. **404 semantics.** This BFF assumes both `GET
   /users/{userId}/accounts` and `GET
   /accounts/{accountId}/transactions` return `404` for "none found"
   (per spec), and treats that as an **empty list**, not an error —
   see the `onErrorResume` calls in `AccountServiceClient` and
   `TransactionServiceClient`. If the real services instead return
   `200` with an empty array, this code already works either way; if
   they return something else (e.g. `204`), this needs a fix.
4. **Error response shape.** `GlobalExceptionHandler` here assumes
   downstream failures don't need their exact shape parsed — the BFF
   swallows any downstream error into a generic `500`. This is fine
   *unless* a downstream 4xx should be surfaced differently (e.g. a
   validation error) — not needed for the current dashboard endpoint,
   but worth knowing if this pattern gets copied for a `POST` BFF
   endpoint later (e.g. transfer initiation).

## What this service owns

Only orchestration — `GET /bff/dashboard/{userId}`. No persistence, no
business rules of its own beyond combining other services' data.

## A known spec inconsistency, flagged rather than silently resolved

The spec's dashboard example shows transactions with a narrower field
set (`transactionId, amount, toAccountId, description, timestamp`)
than the spec's own transaction-history endpoint example (which adds
`fromAccountId` and `deliveryStatus`). This service's `TransactionDto`
keeps the fuller set — see the comment in `TransactionDto.java` for
the reasoning. If the frontend needs the narrower shape specifically,
that's a one-line change in `DashboardServiceImpl`, not a redesign.

## Auth integration detail worth knowing

User Service's `/profile` endpoint requires an `X-User-Id` header that
must match the requested `{userId}` (our own earlier decision — own
profile only). This BFF is on the trusted side of that boundary, same
as the gateway, so `UserServiceClient` sets that header itself when
calling on a user's behalf. If User Service's auth contract changes,
this is the one place in bff-service that needs updating to match.

## Concurrency model

Inbound API is synchronous Spring MVC (`spring-boot-starter-webmvc`);
outbound calls to the three downstream services use `WebClient`
(`spring-boot-starter-webflux`), run concurrently, then `.block()` at
the controller boundary. This is a deliberate middle ground — full
WebFlux end-to-end wasn't judged worth the added complexity for one
aggregation endpoint. See the comment in `bff-service/pom.xml` for
the reasoning, and revisit if more high-concurrency endpoints get
added here later.

A 5-second timeout wraps the whole aggregation (see
`DashboardServiceImpl.AGGREGATION_TIMEOUT`) so one slow downstream
call can't hang this endpoint indefinitely. Tune this once real
network latency between services is known.

## Running locally

```bash
cd vbank
mvn -pl bff-service -am spring-boot:run
```
Runs on **port 8084** (see `docs/PORTS.md`). Requires User Service
(8081), Account Service (8082), and Transaction Service (8083) all
running for the dashboard endpoint to succeed end-to-end.

## API documentation

`http://localhost:8084/swagger-ui.html` once running.

## Testing

```bash
mvn -pl bff-service test
```
`DashboardServiceImplTest` covers the aggregation logic (success,
empty-accounts, and both failure-propagation paths) with all three
downstream clients mocked — no real HTTP calls, no dependency on the
other services actually running.

**Not yet added:** any test against the *real* Account/Transaction
services (would require them running, or a contract test / WireMock
setup) — this is exactly the gap the reconciliation checklist above
exists to close once that code is available.
