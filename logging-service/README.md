# Logging Service

Kafka consumer that persists request/response log messages from
every other microservice into a dump table for debugging, auditing,
and monitoring (spec section 5).

## What this service owns

- Consuming from the `logging` Kafka topic (name configurable via
  `app.kafka.logging-topic`, but must match every producer exactly)
- Parsing the `{message, messageType, dateTime}` envelope
- Persisting to the `log_entries` dump table
- A read-only query API (`GET /logs`) — **an addition beyond the
  spec's minimum**, since the spec only says the table "can be
  queried later" without specifying how. Remove if the team decides
  direct DB access is sufficient.

This service has **no producer role**. Every other service
(User/Account/Transaction/BFF) is responsible for publishing its own
request/response logs to this same topic — see
[`docs/OPENCODE.md`](../docs/OPENCODE.md)'s Kafka section for the
integration requirement and audit checklist.

## The one thing that will silently break this service

**Topic name and message shape must match exactly across every
producer and this consumer.** There is no error on either side if
they don't — a producer publishing to the wrong topic name, or with
a field misspelled (`messageType` vs `message_type`), just means
messages never arrive here. Nothing crashes, nothing logs an error
that points at the real problem. This is why `application.yml`
externalizes the topic name with a loud comment rather than hardcoding
it, and why the `docs/OPENCODE.md` audit checklist explicitly asks to
verify this across all four producing services.

## Resilience decisions

- A message that fails to parse is **caught inside the Kafka listener
  and logged as a warning**, never rethrown — see
  `LoggingKafkaListener`. Letting an exception escape a
  `@KafkaListener` method triggers the container's retry/error
  handling, which for a permanently-malformed message means endless
  redelivery attempts, not eventual success.
- `auto-offset-reset: earliest` — a restarted consumer picks up
  messages published while it was down, rather than silently skipping
  them. Reasonable for an audit/dump table where completeness matters
  more than avoiding the occasional reprocessed message.

## Running locally

Requires a running Kafka broker and MySQL instance.

```bash
cd vbank
mvn -pl logging-service -am spring-boot:run
```
Runs on **port 8085** (see `docs/PORTS.md`).

## API documentation

`http://localhost:8085/swagger-ui.html`

## Testing

```bash
mvn -pl logging-service test
```
`LogEntryServiceImplTest` covers message parsing and persistence
(well-formed message, invalid JSON, unrecognized `messageType`) with
the repository mocked — no real Kafka broker involved.

**Not yet added:** an embedded-Kafka integration test that actually
publishes to a test topic and confirms `LoggingKafkaListener` consumes
and persists it end-to-end. The `spring-boot-starter-kafka-test`
dependency is already in `pom.xml` for this, just not used yet.
