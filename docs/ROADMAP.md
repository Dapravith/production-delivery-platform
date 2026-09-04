# Twelve-week DevOps implementation roadmap

This roadmap turns the repository into a practical transition project from full-stack development to DevOps. Complete one production skill and preserve one piece of evidence each week.

## Week 1: Reproducible build

Goal: make the Java build deterministic on a clean Java 21 machine.

Why it matters: a deployment pipeline cannot be trusted if only one developer's machine can build the software.

Task:

```bash
mvn -B clean verify
```

Done when both modules build from a clean clone and JUnit reports are archived.

Evidence: Maven log, test report, commit SHA, and tool versions.

## Week 2: Authentication and authorization

Goal: prove JWT validation and scope enforcement at both services.

Why it matters: internal services cannot assume that gateway authentication is sufficient.

Task: run the smoke test, then add automated wrong-issuer, missing-scope, and cross-customer access tests.

Done when permitted calls succeed and forbidden calls consistently return `401` or `403`.

Evidence: test report with positive and negative cases.

## Week 3: Secure containers

Goal: build minimal images and verify runtime restrictions.

Why it matters: containers become production artifacts and must be reproducible, small, and non-root.

Task: build both images, inspect their users and layers, and scan them with Trivy.

Done when both images run as `app` and have no unapproved critical findings.

Evidence: image digests, user inspection output, and scan reports.

## Week 4: Jenkins quality gates

Goal: run pull-request checks on a Jenkins agent.

Why it matters: quality rules must run automatically before code is trusted.

Task: configure Java 21, Maven, Docker, Helm, Trivy, Git, and registry credentials on the agent.

Done when a valid branch passes and a deliberately failing test blocks the pipeline.

Evidence: one successful and one intentionally failed Jenkins run.

## Week 5: Immutable registry publishing

Goal: publish commit-SHA-tagged images.

Why it matters: immutable tags let operators identify and restore an exact release.

Task: replace the example registry and publish both service images from `main`.

Done when each tag resolves to a recorded image digest and can be pulled on a clean machine.

Evidence: repository tags, digests, and source commit.

## Week 6: Kubernetes and Helm

Goal: deploy both services to a local `kind` or development cluster.

Why it matters: this connects application packaging to scheduling, health checks, resources, and scaling.

Task: prepare dependencies and Secret, install the chart, and verify rollouts, HPA, and PDB.

Done when both deployments are ready, probes pass, and the gateway handles an authenticated order request.

Evidence: Helm release, pod list, rollout status, and API response.

## Week 7: Separate GitOps state

Goal: move environment-specific values to a dedicated repository.

Why it matters: deployment state needs its own review history and access policy.

Task: create development, staging, and production value files with no secrets.

Done when a chart render from each environment is valid and differences are intentional.

Evidence: reviewed GitOps repository structure and Helm render results.

## Week 8: Argo CD reconciliation

Goal: let Argo CD deploy and repair desired state.

Why it matters: Git becomes the source of truth and manual drift becomes visible and reversible.

Task: configure the Application, sync it, manually change one safe label, and observe self-healing.

Done when Argo CD reports `Synced` and `Healthy` and corrects the controlled drift.

Evidence: Argo CD revision and before/after resource state.

## Week 9: Metrics and dashboards

Goal: monitor traffic, errors, latency, and saturation.

Why it matters: teams cannot operate a service they cannot measure.

Task: install Prometheus and Grafana, connect the ServiceMonitor, and build a service-overview dashboard.

Done when both targets are `UP` and the dashboard changes during smoke traffic.

Evidence: target status and dashboard screenshot without sensitive data.

## Week 10: Logs and tracing

Goal: correlate a request across gateway and order service.

Why it matters: distributed failures are difficult to diagnose from isolated logs.

Task: add structured JSON logs, Grafana Alloy, Loki, Micrometer Tracing, and an OpenTelemetry Collector.

Done when one trace or request ID retrieves the related spans and logs.

Evidence: trace view and correlated Loki query.

## Week 11: SLO alerts and response

Goal: turn telemetry into actionable alerts.

Why it matters: dashboards require someone to watch them; alerts should detect meaningful user impact.

Task: define a starting availability SLO, configure an error-budget alert, and route a test alert to Telegram or another non-production channel.

Done when the alert fires, includes a runbook, and produces a recovery notification.

Evidence: alert payload, acknowledgement time, and runbook link.

## Week 12: Failure and rollback exercise

Goal: prove the team can recover a failed release.

Why it matters: reliable delivery includes recovery, not only successful deployment.

Task: promote an invalid image tag in development, detect the failure, revert Git, and measure recovery.

Done when the previous healthy version is restored and the incident timeline is documented.

Evidence: Git revert, Argo CD history, rollout result, and recovery duration.

## Portfolio outcome

At the end, prepare a short case study containing:

- Architecture and major decisions
- Delivery workflow
- Security controls
- Dashboards and alerts
- One incident and rollback exercise
- Measured improvements
- Known limitations and next steps

This evidence is stronger than listing DevOps tools without showing how they work together.
