# Verification record

## Branch

`feat/production-ready-foundation`

## Passed in the creation workspace

- XML parsing for the parent and service Maven POMs
- Duplicate-key-aware YAML parsing for Docker Compose, Argo CD, Helm values, and Spring configuration
- JSON parsing for the Keycloak realm import
- Bash syntax checks for both verification scripts
- Git whitespace and patch integrity checks
- ZIP integrity validation after packaging

## Required before merging

Run the following on a machine or Jenkins agent with Java 21, Maven 3.9+, Docker Compose, and Helm 3:

```bash
./scripts/verify.sh
./scripts/smoke-test.sh
```

Do not merge the feature branch unless both commands exit with status zero.

The smoke test covers unauthorized access, invalid input, JWT authentication, customer data isolation, order creation, and order retrieval through the gateway.

## Documentation verification

The documentation must match the implementation. In particular:

- Kafka is provisioned and configured, but business event publishing is not implemented.
- The ServiceMonitor is implemented, but Prometheus and Grafana installation is external.
- Application logs go to stdout, but Alloy and Loki deployment is not implemented.
- Argo CD automatic reconciliation is defined, but the Jenkins GitOps update stage is still a placeholder.
- Production endpoint and registry values remain examples and must be replaced.

Screenshots may demonstrate repository state or a test result. They do not replace repeatable verification commands.
