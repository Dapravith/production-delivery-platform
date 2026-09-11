# Verification record

Last updated: 2026-09-11

## Verified baseline: PR #13

- Branch: `feat/pdp-012-016-application-hardening`
- Full commit: `6ce466e1503e67796b120306b2295c219088cbc1`
- [Pull request #13](https://github.com/Dapravith/production-delivery-platform/pull/13)
- [Successful hosted run](https://github.com/Dapravith/production-delivery-platform/actions/runs/33858143361), [job and logs](https://github.com/Dapravith/production-delivery-platform/actions/runs/33858143361/job/100976109433)
- Tools recorded: Java 21.0.12.1, Maven 3.9.16, Docker 28.0.4, Compose 2.38.2, Helm 3.21.4.
- Maven: 9 gateway tests and 16 order tests; 25 total, no failures, errors, or skipped tests; clean build passed.
- Compose configuration, Helm lint, and manifest checks passed.
- Real Compose smoke passed authentication, audience, authorization, validation, runtime database permissions, customer isolation, order creation, and retrieval.

This is GitHub-hosted Linux evidence, not Jenkins, Kubernetes, recovery, production, or human acceptance. PR #13 is not merged at the time of this record.

## S01 follow-up: verification and local-stack safety

Branch: `fix/s01-verification-evidence`. Scope: PDP-011, PDP-030, PDP-040 and evidence reconciliation for PDP-010/PDP-012–016.

- Upgrade local Keycloak from 26.1 to 26.7.3; bind all published demo ports to loopback.
- Isolate smoke tests with a disposable Compose project and refuse to reuse existing project containers or volumes before registering cleanup.
- Require Helm 3 and verify resource counts in both default and production profiles.
- Share verification wrappers between GitHub Actions and Jenkins; retain logs, full commit metadata, exit codes, and JUnit reports, including failure evidence.
- Exercise script control flow with seven standard-library tests using mocked CLIs. These do not replace application runtime tests or Jenkins acceptance.

Follow-up hosted verification: **pending**. Do not reuse the baseline run as proof of the changed Keycloak image or delivery scripts.

## Reproduction

On a prepared Java 21, Maven 3.9+, Docker Compose 2, Helm 3, Git, curl, and Python 3 environment:

```bash
python3 scripts/test_delivery.py
bash scripts/ci-verify.sh verify
bash scripts/ci-verify.sh smoke
```

Evidence is written to ignored `build/evidence/`; Maven JUnit XML remains in each module's `target/surefire-reports/`. CI archives this synthetic local-stack evidence. Do not run the smoke test against production or upload private data or credentials.

Before merge, both runtime phases must pass on the follow-up commit and a human must review the results. PDP-030 separately requires real Jenkins success and deliberate-failure evidence; no Jenkins environment is accessible in this session.

## Documentation verification

The documentation must match the implementation. In particular:

- Kafka is provisioned and configured, but business event publishing is not implemented.
- The ServiceMonitor is implemented, but Prometheus and Grafana installation is external.
- Application logs go to stdout, but Alloy and Loki deployment is not implemented.
- Argo CD automatic reconciliation is defined, but the Jenkins GitOps update stage is still a placeholder.
- Production endpoint and registry values remain examples and must be replaced.

Screenshots may demonstrate repository state or a test result. They do not replace repeatable verification commands.
