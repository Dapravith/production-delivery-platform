# Production Delivery Platform

A portfolio-grade GitOps delivery platform for Java 21 and Spring WebFlux microservices.

## What it demonstrates

- Reactive gateway and order API with OAuth2 JWT validation at both trust boundaries
- Principal-based Redis rate limiting at the gateway
- Reactive PostgreSQL access, versioned Flyway migrations, and Kafka connectivity
- Non-root minimal containers with JVM container memory settings
- Jenkins quality gates, image builds, Trivy scanning, registry publishing, and Helm validation
- Kubernetes readiness/liveness probes, resource boundaries, HPA, PDB, and restricted container security
- Argo CD automated reconciliation, pruning, self-healing, and retry
- Prometheus scraping through ServiceMonitor; logs are emitted to stdout for collection by Grafana Alloy/Promtail and Loki

## Delivery flow

1. A developer opens a pull request. Jenkins runs tests and Helm validation.
2. A merge to `main` creates immutable images tagged with the Git commit SHA.
3. Jenkins scans and pushes images, then updates image tags in the GitOps repository.
4. Argo CD detects the desired-state change and performs a rolling deployment.
5. Kubernetes probes and metrics validate health. Prometheus and Loki feed Grafana dashboards and alerts.

## Local prerequisites

Java 21, Maven 3.9+, Docker Compose, Helm 3, and optionally a local Kubernetes cluster such as kind.

Start infrastructure:

```bash
./scripts/verify.sh
./scripts/smoke-test.sh
```

The smoke test builds the services, starts PostgreSQL, Redis, Kafka, Keycloak, the order service, and the gateway, obtains a JWT, verifies that anonymous access is rejected, and exercises order creation and retrieval. It shuts the stack down afterward unless `SMOKE_KEEP_RUNNING=true` is set.

The included Keycloak realm creates a local `platform-cli` client, scopes `orders.read` and `orders.write`, and a demo account. The local credentials are deliberately limited to development and must never be reused outside this environment.

## Production boundaries

This is a strong delivery foundation, not a claim that a new system is production-ready on day one. Before production, add TLS and ingress, external secret management, a managed or replicated data layer, network policies, backup restoration tests, SLO-based alerts, audit event persistence, OpenTelemetry tracing, signed images/SBOMs, and a rollback runbook.

## First hands-on milestone

Install Java 21, Maven, Docker Compose, and Helm, then run `./scripts/verify.sh` and `./scripts/smoke-test.sh`. The milestone is complete only when both scripts exit successfully. Next, provision a local kind cluster, install the Prometheus Operator CRDs, and apply the rendered chart.
