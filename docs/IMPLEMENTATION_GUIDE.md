# Implementation guide

This guide explains how to reproduce the platform from source and how each implementation step contributes to a production delivery workflow.

## 1. Prepare the workstation

Install and verify:

```bash
java -version
mvn -version
docker version
docker compose version
helm version
git --version
```

Expected versions:

| Tool | Minimum |
| --- | --- |
| Java | 21 |
| Maven | 3.9 |
| Docker Compose | v2 |
| Helm | 3 |
| Kubernetes | 1.29 recommended or newer |

The parent Maven build enforces Java 21. Using Java 17 should fail early instead of producing inconsistent artifacts.

## 2. Clone the implementation branch

```bash
git clone https://github.com/Dapravith/production-delivery-platform.git
cd production-delivery-platform
git switch feat/production-ready-foundation
git status
```

The status must show a clean working tree before verification.

## 3. Understand the Maven modules

The parent `pom.xml` controls shared versions and Java rules. Each deployable service owns its runtime dependencies and Spring Boot packaging.

```bash
mvn -B clean verify
```

Expected result:

- Both modules compile on Java 21
- Gateway rate-limit key tests pass
- Order ownership and validation tests pass
- Spring Boot JAR files are created under each module's `target/` directory

## 4. Start the local platform

The shortest path is the smoke test:

```bash
./scripts/smoke-test.sh
```

To inspect the stack after the test:

```bash
SMOKE_KEEP_RUNNING=true ./scripts/smoke-test.sh
docker compose ps
docker compose logs --tail=100 gateway-service order-service
```

Local ports:

| Component | URL or port |
| --- | --- |
| Gateway | `http://localhost:8080` |
| Order service | `http://localhost:8081` |
| Keycloak | `http://localhost:8180` |
| PostgreSQL | `localhost:5432` |
| Redis | `localhost:6379` |
| Kafka | `localhost:9092` |

The application ports are exposed for learning and diagnosis. In a shared production environment, expose only the gateway or ingress.

## 5. Verify authentication manually

Request a development token:

```bash
TOKEN_RESPONSE=$(curl --fail --silent --show-error \
  --data-urlencode client_id=platform-cli \
  --data-urlencode username=demo-user \
  --data-urlencode password=demo-password \
  --data-urlencode grant_type=password \
  http://localhost:8180/realms/platform/protocol/openid-connect/token)

ACCESS_TOKEN=$(python3 -c 'import json,sys; print(json.load(sys.stdin)["access_token"])' <<<"$TOKEN_RESPONSE")
```

Do not print or commit the token. It is short-lived but still grants access.

Create an order through the gateway:

```bash
curl --fail --silent --show-error \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"amount":10.50}' \
  http://localhost:8080/api/orders
```

Read the authenticated user's orders:

```bash
curl --fail --silent --show-error \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:8080/api/orders
```

## 6. Build the images

The Dockerfiles use a Maven build stage and a smaller Java runtime stage. The services run as an unprivileged `app` user.

```bash
docker build -f gateway-service/Dockerfile -t local/gateway-service:dev .
docker build -f order-service/Dockerfile -t local/order-service:dev .
```

Inspect the configured runtime user:

```bash
docker image inspect local/gateway-service:dev --format '{{.Config.User}}'
docker image inspect local/order-service:dev --format '{{.Config.User}}'
```

Both commands should print `app`.

## 7. Validate the Helm chart

```bash
helm lint deploy/helm/platform -f deploy/helm/platform/values-prod.yaml
helm template platform deploy/helm/platform \
  -f deploy/helm/platform/values-prod.yaml \
  > /tmp/platform-rendered.yaml
```

The chart should render:

- 2 Deployments
- 2 Services
- 2 HorizontalPodAutoscalers
- 2 PodDisruptionBudgets
- 1 ServiceMonitor when monitoring is enabled

The automated count checks live in `scripts/verify.sh`.

## 8. Prepare a Kubernetes namespace

The application chart expects its dependencies and database Secret to exist.

```bash
kubectl create namespace platform
kubectl -n platform create secret generic platform-database \
  --from-literal=username='REPLACE_ME' \
  --from-literal=password='REPLACE_ME'
```

The literal command is acceptable only for disposable local clusters because it can enter shell history. Use an external secret manager for shared environments.

Update these values before deployment:

- `global.registry`
- `gateway.tag`
- `order.tag`
- `config.oidcIssuerUri`
- `config.oidcJwkSetUri`
- PostgreSQL, Redis, and Kafka endpoints
- `secrets.existingSecret`
- Prometheus operator selector labels

## 9. Deploy with Helm

```bash
helm upgrade --install platform deploy/helm/platform \
  --namespace platform \
  --create-namespace \
  -f deploy/helm/platform/values-prod.yaml \
  --wait \
  --timeout 5m
```

Verify:

```bash
kubectl -n platform get deployments,pods,services,hpa,pdb
kubectl -n platform rollout status deployment/gateway-service --timeout=3m
kubectl -n platform rollout status deployment/order-service --timeout=3m
kubectl -n platform get events --sort-by=.lastTimestamp
```

## 10. Configure Jenkins

The Jenkins agent must have:

- Java 21 and Maven 3.9+
- Docker access appropriate for the agent design
- Helm 3
- Trivy
- Git
- Credentials named `container-registry`

Replace `registry.example.com/platform` in the Jenkinsfile or inject it through a controlled environment configuration. Never embed a registry password in the repository.

Pipeline behavior:

1. Checkout source
2. Derive a 12-character immutable image tag from Git
3. Run Maven verification
4. Build both images
5. Fail on critical Trivy findings
6. Push images only from `main`
7. Validate Helm
8. Hand off to the GitOps promotion step

The last step remains intentionally incomplete. Implement it using a separate environment repository and a narrowly scoped bot credential.

## 11. Configure Argo CD

Edit `deploy/argocd/application.yaml`:

- Set the real GitOps repository URL
- Confirm the chart path
- Confirm the destination cluster and namespace
- Register repository credentials when the repository is private

Apply:

```bash
kubectl apply -f deploy/argocd/application.yaml
argocd app get production-delivery-platform
argocd app sync production-delivery-platform
argocd app wait production-delivery-platform --health --sync --timeout 300
```

After automated sync is trusted, manual `argocd app sync` should be reserved for controlled recovery or testing.

## 12. Add observability

Install a Prometheus Operator compatible stack, then ensure its ServiceMonitor selector matches `serviceMonitor.additionalLabels`.

Verify targets:

```bash
kubectl -n platform get servicemonitor production-delivery-platform -o yaml
kubectl -n platform port-forward service/gateway-service 8080:8080
curl http://localhost:8080/actuator/prometheus
```

Grafana and Loki are not deployed by this repository yet. Follow [Observability](OBSERVABILITY.md) before claiming complete monitoring.

## 13. Completion criteria

The implementation is accepted only when:

- `scripts/verify.sh` passes
- `scripts/smoke-test.sh` passes
- Images run as non-root
- Helm lint and rendering pass
- Both Kubernetes rollouts become healthy
- Prometheus reports both targets as `UP`
- A tested rollback restores a known-good image
- No production placeholders or local credentials remain in deployed values
