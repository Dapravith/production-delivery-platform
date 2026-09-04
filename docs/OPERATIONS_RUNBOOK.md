# Operations runbook

## 1. Scope

This runbook covers the gateway and order-service workloads deployed through Helm and Argo CD. Replace example names and endpoints with the real environment configuration before operational use.

## 2. Normal release verification

Record the GitOps revision and expected image tags, then verify:

```bash
argocd app get production-delivery-platform
kubectl -n platform get deployment,pod,service,hpa,pdb
kubectl -n platform rollout status deployment/gateway-service --timeout=3m
kubectl -n platform rollout status deployment/order-service --timeout=3m
kubectl -n platform get pods \
  -o custom-columns='NAME:.metadata.name,IMAGE:.spec.containers[0].image,READY:.status.containerStatuses[0].ready,RESTARTS:.status.containerStatuses[0].restartCount'
```

Check request rate, error ratio, latency, pod restarts, and dependency health for at least one normal traffic window.

## 3. Incident triage

Use this order to avoid random changes:

1. Confirm user impact and affected environment.
2. Identify the last known-good time.
3. Check whether a deployment or configuration change occurred.
4. Inspect Argo CD sync and health.
5. Inspect pod readiness, restarts, and events.
6. Inspect service metrics and logs.
7. Check dependency health.
8. Mitigate first, then investigate deeply.

## 4. Quick diagnosis

### Application unavailable

```bash
kubectl -n platform get pods -o wide
kubectl -n platform describe deployment gateway-service
kubectl -n platform describe deployment order-service
kubectl -n platform get events --sort-by=.lastTimestamp | tail -n 50
```

### CrashLoopBackOff

```bash
kubectl -n platform logs deployment/order-service --previous --tail=200
kubectl -n platform describe pod <pod-name>
```

Check configuration, missing Secrets, incompatible database migrations, memory limits, and dependency endpoints.

### Readiness failure

```bash
kubectl -n platform port-forward service/order-service 8081:8081
curl --fail http://localhost:8081/actuator/health/readiness
```

Do not change a readiness probe to hide a dependency or startup failure. Correct the cause.

### High latency or errors

Check:

- Gateway and order-service P95/P99 latency
- 4xx versus 5xx ratio
- CPU throttling and memory pressure
- R2DBC connection pool usage
- PostgreSQL latency and locks
- Redis latency and errors
- Rate-limit rejection volume

## 5. Rollback

### GitOps rollback

Revert the environment repository promotion commit:

```bash
git revert <bad-promotion-commit>
git push
argocd app wait production-delivery-platform --health --sync --timeout 300
```

### Emergency Kubernetes rollback

```bash
kubectl -n platform rollout undo deployment/gateway-service
kubectl -n platform rollout undo deployment/order-service
kubectl -n platform rollout status deployment/gateway-service --timeout=3m
kubectl -n platform rollout status deployment/order-service --timeout=3m
```

Then update Git immediately. Otherwise Argo CD self-heal can restore the bad declared state.

## 6. Database migration failure

Flyway migrations run during order-service startup.

1. Stop further promotion.
2. Capture the Flyway error and schema history.
3. Do not edit an already-applied migration.
4. Create a new forward-fix migration.
5. Restore from backup only when the migration is destructive and the recovery plan requires it.

Every destructive migration needs a tested rollback or forward-recovery plan before deployment.

## 7. Credential incident

If a real credential is exposed:

1. Revoke or rotate it immediately.
2. Identify every environment and log where it may have appeared.
3. Replace the Kubernetes or external secret.
4. Restart only affected workloads when required.
5. Review access logs.
6. Remove the secret from Git history using an approved repository-cleanup process.
7. Document the incident and preventive control.

Deleting the visible Git line is not sufficient because history and forks may retain the secret.

## 8. Scaling response

Before changing replica counts, determine the bottleneck. Adding pods will not fix an exhausted database, Redis, or downstream API.

```bash
kubectl -n platform get hpa
kubectl -n platform top pods
kubectl -n platform describe hpa gateway-service
kubectl -n platform describe hpa order-service
```

For emergency temporary scaling:

```bash
kubectl -n platform scale deployment/gateway-service --replicas=4
kubectl -n platform scale deployment/order-service --replicas=4
```

Update GitOps values after confirming the capacity requirement, or Argo CD will restore the declared count.

## 9. Post-incident review

Record:

- Customer impact and duration
- Detection source
- Timeline
- Root and contributing causes
- Mitigation and recovery
- What worked and failed
- Corrective action, owner, and due date
- Monitoring and runbook improvements

Use the review to improve the system, not to assign blame.

## 10. Emergency contacts

Populate this table before production use:

| Responsibility | Primary | Backup | Escalation |
| --- | --- | --- | --- |
| Application | Not assigned | Not assigned | Not assigned |
| Kubernetes | Not assigned | Not assigned | Not assigned |
| Database | Not assigned | Not assigned | Not assigned |
| Identity | Not assigned | Not assigned | Not assigned |
| Security | Not assigned | Not assigned | Not assigned |
