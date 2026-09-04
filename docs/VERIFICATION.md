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
