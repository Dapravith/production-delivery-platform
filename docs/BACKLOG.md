# Product backlog

Last updated: 2026-09-04

Statuses follow [Agile workflow](AGILE_WORKFLOW.md). Sprint assignments follow [Sprint plan](SPRINT_PLAN.md).

## Priority rules

- P0: release or security blocker
- P1: required for the first production release
- P2: improves reliability or developer experience but can follow the first release
- P3: future enhancement

## Epic progress

| Epic | Scope | Status |
| --- | --- | --- |
| E1 Product and architecture | Scope, architecture, workflow, documentation | Done |
| E2 Application foundation | Build, security, persistence, local runtime | In progress |
| E3 Event-driven integration | Kafka events, contracts, reliability | Backlog |
| E4 CI/CD and supply chain | Jenkins, registry, scans, provenance | In progress |
| E5 Kubernetes and GitOps | Helm, environments, Argo CD, promotion | In progress |
| E6 Observability | Metrics, dashboards, logs, traces, alerts | In progress |
| E7 Test and quality | Integration, security, load, resilience | In progress |
| E8 Release and operations | Staging, UAT, production, hypercare, closure | Backlog |

## E1: Product and architecture

| ID | Backlog item | Priority | Size | Sprint | Status | Acceptance evidence |
| --- | --- | --- | --- | --- | --- | --- |
| PDP-001 | Define product scope and lifecycle | P1 | S | Pre-sprint | Done | Product plan committed |
| PDP-002 | Document runtime and delivery architecture | P1 | M | Pre-sprint | Done | Architecture diagrams committed |
| PDP-003 | Define one-week agile workflow | P1 | S | Pre-sprint | Done | Ready/Done and review rules committed |
| PDP-004 | Establish backlog and progress tracker | P1 | M | Pre-sprint | Done | IDs, priorities, sprints, and statuses committed |

## E2: Application foundation

| ID | Backlog item | Priority | Size | Sprint | Status | Acceptance evidence |
| --- | --- | --- | --- | --- | --- | --- |
| PDP-010 | Build gateway and order modules on Java 21 | P0 | M | S01 | In progress | Clean `mvn verify` report |
| PDP-011 | Run the complete Compose smoke test | P0 | M | S01 | In progress | Successful smoke-test log |
| PDP-012 | Add WebTestClient security integration tests | P1 | M | S02 | Ready | 401, 403, wrong issuer, and scope tests |
| PDP-013 | Validate JWT audience | P0 | M | S02 | Ready | Wrong audience rejected |
| PDP-014 | Add maximum amount and currency rules | P1 | S | S02 | Ready | Validation tests and API response |
| PDP-015 | Separate Flyway and runtime DB permissions | P1 | M | S03 | Backlog | Two identities and permission test |
| PDP-016 | Add API problem-details error contract | P1 | M | S03 | Backlog | Contract tests and examples |

## E3: Event-driven integration

| ID | Backlog item | Priority | Size | Sprint | Status | Acceptance evidence |
| --- | --- | --- | --- | --- | --- | --- |
| PDP-020 | Define `OrderCreated` event schema | P1 | M | S04 | Backlog | Versioned schema and compatibility rule |
| PDP-021 | Implement transactional outbox | P0 | L | S04 | Backlog | DB transaction and retry tests |
| PDP-022 | Publish outbox events to Kafka | P1 | M | S04 | Backlog | Event visible with trace and event IDs |
| PDP-023 | Add idempotent consumer example | P1 | M | S05 | Backlog | Duplicate event test passes |
| PDP-024 | Add dead-letter and replay procedure | P1 | M | S05 | Backlog | Failed message replay evidence |

## E4: CI/CD and supply chain

| ID | Backlog item | Priority | Size | Sprint | Status | Acceptance evidence |
| --- | --- | --- | --- | --- | --- | --- |
| PDP-030 | Run Jenkins PR quality gates | P0 | M | S01 | Ready | Successful and deliberately failed runs |
| PDP-031 | Configure registry and SHA image publishing | P0 | M | S05 | Backlog | Tags and immutable digests |
| PDP-032 | Add dependency and secret scanning | P1 | M | S05 | Backlog | Archived reports and enforced threshold |
| PDP-033 | Generate SBOM and provenance | P1 | M | S06 | Backlog | SBOM and attestation artifacts |
| PDP-034 | Sign images and enforce verification | P1 | L | S06 | Backlog | Admission rejects unsigned image |
| PDP-035 | Replace placeholder GitOps update stage | P0 | M | S06 | Backlog | Bot commit updates only image tags |

## E5: Kubernetes and GitOps

| ID | Backlog item | Priority | Size | Sprint | Status | Acceptance evidence |
| --- | --- | --- | --- | --- | --- | --- |
| PDP-040 | Lint and render Helm chart on Helm 3 | P0 | S | S01 | In progress | `verify.sh` success |
| PDP-041 | Deploy to a development Kubernetes cluster | P0 | L | S07 | Backlog | Healthy deployments and API test |
| PDP-042 | Add TLS ingress and DNS | P0 | M | S07 | Backlog | Valid certificate and HTTPS test |
| PDP-043 | Add external secret synchronization | P0 | M | S07 | Backlog | Secret rotates without Git value |
| PDP-044 | Add default-deny NetworkPolicies | P0 | M | S08 | Backlog | Allowed traffic passes; forbidden traffic fails |
| PDP-045 | Create separate environment values repository | P0 | M | S08 | Backlog | Dev, staging, UAT, production render cleanly |
| PDP-046 | Configure Argo CD projects and applications | P0 | M | S08 | Backlog | Synced, Healthy, and constrained destinations |
| PDP-047 | Test Argo CD drift self-healing | P1 | S | S08 | Backlog | Controlled drift corrected |

## E6: Observability

| ID | Backlog item | Priority | Size | Sprint | Status | Acceptance evidence |
| --- | --- | --- | --- | --- | --- | --- |
| PDP-050 | Connect ServiceMonitor to Prometheus | P0 | M | S07 | Backlog | Both application targets `UP` |
| PDP-051 | Create four-golden-signals Grafana dashboard | P1 | M | S09 | Backlog | Dashboard responds to test traffic |
| PDP-052 | Add structured logs and correlation IDs | P1 | M | S09 | Backlog | One ID locates both service logs |
| PDP-053 | Deploy Alloy and Loki collection | P1 | M | S09 | Backlog | Searchable pod logs with safe labels |
| PDP-054 | Add OpenTelemetry traces | P1 | L | S10 | Backlog | Gateway-to-database trace visible |
| PDP-055 | Define SLOs and error-budget alerts | P0 | M | S10 | Backlog | Tested alert and recovery notification |

## E7: Test and quality

| ID | Backlog item | Priority | Size | Sprint | Status | Acceptance evidence |
| --- | --- | --- | --- | --- | --- | --- |
| PDP-060 | Automate Kubernetes deployment verification | P1 | M | S08 | Backlog | CI artifact with rollout result |
| PDP-061 | Add API security test suite | P0 | M | S09 | Backlog | Required negative cases pass |
| PDP-062 | Establish performance baseline | P0 | M | S10 | Backlog | P50/P95/P99 and resource report |
| PDP-063 | Test pod and node resilience | P1 | M | S10 | Backlog | Traffic continues within SLO |
| PDP-064 | Test backup restoration | P0 | L | S11 | Backlog | Restored data verified |
| PDP-065 | Run failed-release rollback exercise | P0 | M | S11 | Backlog | Recovery time and evidence recorded |

## E8: Release and operations

| ID | Backlog item | Priority | Size | Sprint | Status | Acceptance evidence |
| --- | --- | --- | --- | --- | --- | --- |
| PDP-070 | Deploy and validate staging | P0 | L | S11 | Backlog | Staging gate approved |
| PDP-071 | Prepare UAT scenarios and data | P0 | M | S12 | Backlog | Reviewed UAT pack |
| PDP-072 | Execute UAT and resolve blockers | P0 | L | S12 | Backlog | Signed UAT result |
| PDP-073 | Complete production readiness review | P0 | M | S13 | Backlog | Go/no-go checklist approved |
| PDP-074 | Deploy production release | P0 | L | S13 | Backlog | Healthy release and rollback ready |
| PDP-075 | Run one-week hypercare | P0 | M | S14 | Backlog | Daily health records and no open critical incident |
| PDP-076 | Complete handover and project closure | P0 | M | S14 | Backlog | Closure report accepted |

## Backlog change log

| Date | Change | Reason |
| --- | --- | --- |
| 2026-09-04 | Initial full-lifecycle backlog created | Establish evidence-based path from planning to closure |
