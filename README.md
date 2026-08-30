# Road Cutting Permission — API and Portal

A complete full-stack implementation of the Road Cutting Permission take-home assignment, Revision 3.1. The solution contains a Spring Boot 3 / Java 17 backend, PostgreSQL + Flyway persistence, and a React + TypeScript + Vite portal.

## Project overview

The service supports applicant fee previews and application creation, a role-gated verification/approval workflow, tenant-isolated officer queues, applicant-owned application views, and a transition timeline sourced from persisted history.

## Technology stack

- Backend: Java 17, Spring Boot 3.5.x, Spring Web, Spring Validation, Spring Data JPA, PostgreSQL driver
- Database: PostgreSQL 16, Flyway
- Frontend: React 19, TypeScript, Vite
- Testing: JUnit 5, Spring Boot Test
- Containerisation: Docker Compose

## Project structure

```text
road-cutting-permission/
├── backend/
│   ├── pom.xml
│   └── src/
├── frontend/
│   ├── package.json
│   └── src/
├── docker-compose.yml
└── README.md
```

## Prerequisites

- JDK 17+
- Maven 3.9+ (or use the Maven wrapper if added locally)
- Node.js 20+
- npm 10+
- PostgreSQL 16+, unless using Docker

## Run database

With Docker:

```bash
docker compose up -d postgres
```

The database is exposed at `localhost:5432` with database `rcp`, user `rcp`, password `rcp`.

## Run backend from IntelliJ IDEA

1. Open the `road-cutting-permission` folder in IntelliJ IDEA.
2. Set the project SDK to Java 17 or newer.
3. Import `backend/pom.xml` as a Maven project.
4. Start PostgreSQL (for example, `docker compose up -d postgres`).
5. Run `com.example.rcp.RoadCuttingPermissionApplication`.
6. The API starts on `http://localhost:8080`.

Flyway creates the schema automatically. `ddl-auto` is disabled; there is no manual table creation step.

## Run backend from terminal

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

## Run frontend

```bash
cd frontend
npm install
npm run dev
```

Open the Vite URL shown in the terminal, normally `http://localhost:5173`.

The frontend calls `http://localhost:8080` by default. Override it with `VITE_API_BASE_URL` if required.

## Docker one-command startup

The stretch item selected is **one-command startup**. From the project root:

```bash
docker compose up --build
```

This starts PostgreSQL, the API and the portal. Open `http://localhost:5173`.

## API list

All request envelopes carry caller identity in `RequestInfo.userInfo`; authentication/login is deliberately out of scope.

- `POST /rcp/v1/_calculate` — stateless fee preview.
- `POST /rcp/v1/_create` — recomputes the fee server-side, creates an `APPLIED` application, and assigns an application number.
- `POST /rcp/v1/_action` — applies a configured workflow action after server-side role and tenant checks.
- `POST /rcp/v1/_search` — searches by application number, status and mobile number with offset/limit capped server-side.
- `POST /rcp/v1/_get` — retrieves one application, restricted by tenant and applicant ownership.
- `POST /rcp/v1/_update` — updates an applicant-owned `APPLIED` application and recomputes its fee. This endpoint is an implementation assumption because the brief says a sent-back applicant may edit and resubmit, but the endpoint list does not name an edit API.

## Example `_calculate` request

```json
{
  "RequestInfo": {
    "apiId": "portal",
    "msgId": "abc|en_IN",
    "userInfo": {
      "uuid": "u-1",
      "userName": "9990000001",
      "tenantId": "dehradun",
      "roles": [{ "code": "APPLICANT" }]
    }
  },
  "Calculation": {
    "tenantId": "dehradun",
    "roadType": "BT",
    "lengthInMeters": 12.5,
    "widthInMeters": 1.2,
    "durationInDays": 6,
    "applicantType": "PRIVATE",
    "proposedStartDate": "2026-03-02"
  }
}
```

The response includes `reviewRef: "K7Q2"` in the `Calculation` object as required by the addendum.

## Fee rules

- `area = ceil(length × width)`; the product is rounded up once.
- Restoration = area × configured restoration rate.
- Permission = area × configured day rate × duration.
- Government agency permission fee is zero; restoration and deposit still apply.
- Urgency surcharge applies only when `proposedStartDate - applicationDate < 3`; exactly three days has no surcharge.
- Security deposit = max(configured minimum, 25% of restoration).
- Total uses HALF_UP rounding.
- Rates are loaded from `backend/src/main/resources/config/rates.json` through `RateConfigService`; city overrides merge onto defaults.

The supplied examples are reproduced by the automated fee tests: Dehradun total 24,485 and Haridwar total 27,480. The underlying specification requires these exact rules and examples.

## Workflow

The lifecycle is configured in `workflow.json`, not encoded as a switch/if ladder in the workflow service:

`APPLIED -> PENDING_APPROVAL -> APPROVED`

`PENDING_APPROVAL -> REJECTED`

`PENDING_APPROVAL -> APPLIED` via `SEND_BACK`

`APPLIED -> CANCELLED` via `CANCEL`

VERIFY/SEND_BACK require `VERIFIER`; APPROVE/REJECT require `APPROVER`; CANCEL requires `APPLICANT` and only from APPLIED. Every transition stores actor, role, timestamp and optional comment.

## Tenant isolation

Every application read/write is scoped to the caller tenant. Applicant reads are additionally scoped to the caller UUID. The server never accepts a client-supplied tenant as authority: the request tenant must match the caller's `RequestInfo.userInfo.tenantId`.

## Concurrency

Application numbers use a PostgreSQL sequence, so two concurrent creates cannot generate the same numeric sequence value. The workflow uses JPA optimistic locking on `application.version`; if two officers act on the same application concurrently, one commits and the stale update fails with a conflict rather than silently applying two transitions. This is backed by the database rather than an in-memory check.

## Tests

```bash
cd backend
mvn test
```

Tests cover the two worked examples, government-agency fee behaviour, urgency boundary, security-deposit floor, inactive road type rejection, tenant override, illegal transition, and tenant isolation.

## Written answers

### 1. Rate versioning

The current implementation snapshots the computed monetary values onto an application, so a later rate-file change does not recalculate an already-created permit. What is missing is explicit rate-version provenance: an application should also store a rate configuration version/order ID and the exact effective-date snapshot used to calculate it. With two more iterations I would introduce versioned rate records with effective-from timestamps, select the version at application time, and retain it immutably for issued permits. The risk is a migration and reconciliation problem if historical configuration is changed after production data exists, so version IDs and immutable snapshots should be introduced before changing the live calculation source.

### 2. Concurrency

Two officers opening the same application can both see the same state, but the database `version` column means only one successful transaction can update that row. The other receives a conflict and must refresh before attempting another action. That is the desired behaviour: one transition wins, the stale officer does not overwrite the new state, and history contains only the transition that actually committed.

### 3. The decision I am least happy with

I deliberately kept the identity model at the assignment boundary: callers provide `RequestInfo.userInfo` because authentication is explicitly out of scope. In a production service this is not sufficient as a trust boundary. With two more days I would integrate the organisation's identity provider, map claims to roles and tenant, and reject forged identity fields. The risk of changing this after launch is that authorization semantics and existing test data change, so the integration should be introduced behind a well-defined principal abstraction and rolled out with compatibility tests.

## Assumptions / deliberately not built

- Authentication/login is not implemented because the specification explicitly says it is out of scope.
- The portal provides a small identity selector so the evaluator can exercise applicant, verifier and approver flows without a login system; the selected identity is sent in `RequestInfo`.
- `applicationDate` is the backend server's current date for calculation/create; clients cannot manipulate it.
- The assignment's examples use 2026 dates, while normal UI operation uses today's server date. A proposed start date in the past is rejected.
- A sent-back application remains `APPLIED`; `_update` permits applicant edits while APPLIED and resets its fee using current configuration. This is the minimal interpretation of “edit and resubmit”.
- City prefixes are configured as `DDN` and `HRD`; financial year is calculated from the server date using April–March.
- Search returns an envelope with `applications`, `total`, `offset`, `limit`; applicant search is automatically restricted to the caller's own records.

## AI usage

AI assistance was used to accelerate project scaffolding, API DTO design, and test coverage. One place it helped was translating the fee rules and workflow into a layered implementation and boundary tests. A place it can mislead is assuming a seemingly reasonable workflow endpoint is specified when it is not; `_update` is therefore explicitly documented as an assumption and the authentication boundary is kept exactly as the brief states.

## Submission checklist

- Backend + frontend source included.
- Flyway migrations included.
- PostgreSQL configuration included.
- Automated tests included.
- Docker one-command startup included as the single selected stretch item.
- No secrets committed.
- No hard-coded fee rates in Java code.
- No monetary calculations use `double`.

Spec revision: 3.1-KESTREL.
