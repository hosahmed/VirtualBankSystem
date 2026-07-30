# BFF Service (Backend for Frontend)

Aggregates User Service, Account Service, and Transaction Service into
a single frontend-optimized response. Per project convention (see
[`docs/OPENCODE.md`](../docs/OPENCODE.md) §2), this is the **only**
service allowed to call other microservices directly — User, Account,
and Transaction never call each other.


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
