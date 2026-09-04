# Project closure

## 1. Closure rule

The project is not closed when code reaches production. It closes after the production release is stable, ownership is transferred, evidence is archived, and remaining work is intentionally handled.

## 2. Closure checklist

### Product

- [ ] Product objective met
- [ ] Included scope accepted
- [ ] Excluded and deferred scope recorded
- [ ] UAT approval retained
- [ ] Known limitations communicated

### Engineering

- [ ] Source and GitOps repositories clean and documented
- [ ] Production commit, image tags, and digests recorded
- [ ] Tests and scan reports retained
- [ ] Database migrations and recovery documented
- [ ] Architecture diagrams match production
- [ ] No temporary credential or debug configuration remains

### Operations

- [ ] Dashboards and alerts owned
- [ ] Runbooks tested
- [ ] On-call and escalation contacts assigned
- [ ] Backup and restore tested
- [ ] Rollback tested
- [ ] Capacity baseline recorded
- [ ] Access reviewed and least privilege confirmed

### Security

- [ ] Threat model reviewed
- [ ] No unapproved critical or high issue remains
- [ ] Secret rotation and ownership documented
- [ ] Image verification enforced
- [ ] Audit and retention requirements satisfied

### Knowledge transfer

- [ ] Setup and deployment demonstrated
- [ ] Incident exercise completed
- [ ] Maintainer can operate without the original implementer
- [ ] Support boundary and service owner recorded

## 3. Remaining backlog disposition

Every unfinished item must be:

- Accepted into a named future release
- Converted to operational maintenance
- Deferred with a reason and review date
- Rejected with a documented decision

Nothing should remain silently “almost done.”

## 4. Final report template

```markdown
# Production Delivery Platform closure report

## Outcome

## Scope delivered

## Production release
- Date:
- Commit:
- Image digests:
- GitOps revision:

## Quality evidence

## Security posture

## Operations handover

## Incidents and lessons

## Remaining backlog

## Final metrics
- Lead time:
- Deployment frequency:
- Change failure rate:
- Recovery time:

## Approval
- Product owner:
- Operations owner:
- Security reviewer:
```

## 5. Repository closeout

After approval:

1. Tag the final release.
2. Publish release notes.
3. Link final evidence.
4. Close completed lifecycle issues.
5. Move remaining work to the future backlog.
6. Archive temporary environments and revoke temporary access.
7. Keep production repositories active while the service is maintained.

Do not archive the production source repository merely because the initial project phase is closed.
