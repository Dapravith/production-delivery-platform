# Agile workflow

## 1. One-week personal sprint

The short cadence creates frequent evidence and prevents the project from becoming a long list of unfinished tools.

```mermaid
flowchart LR
    R[Refine] --> P[Plan]
    P --> B[Build]
    B --> V[Verify]
    V --> RV[Review]
    RV --> RT[Retrospective]
    RT --> R
```

## 2. Weekly schedule

| Day | Activity | Output |
| --- | --- | --- |
| Monday | Select sprint goal and ready items | Sprint issue and committed scope |
| Tuesday | Implement highest-risk item | Small tested commit |
| Wednesday | Continue integration | Working vertical slice |
| Thursday | Test, secure, and document | Test and documentation evidence |
| Friday | Finish and prepare review | Candidate result and updated tracker |
| Saturday | Review and retrospective | Demo evidence and improvement action |
| Sunday | Buffer, learning, or rest | No mandatory new scope |

## 3. Sprint capacity

Use relative sizes:

- S: up to half a focused day
- M: roughly one focused day
- L: two or more focused days and should usually be split

For a working developer, plan no more than three to five focused tasks per week. Reserve at least 20% of available time for unexpected failures, documentation, and learning.

## 4. Definition of Ready

A backlog item is Ready only when:

- The problem and expected outcome are clear
- Acceptance criteria can be tested
- Dependencies and affected components are identified
- Security and data impact are noted
- Required environment and access are available
- The item is small enough for one sprint or split into smaller work
- Evidence to retain is defined

## 5. Definition of Done

An item is Done only when applicable checks pass:

- Code or configuration is committed on a feature branch
- Tests cover the change and pass
- Security implications are reviewed
- Documentation is updated
- No sensitive value is committed
- CI and deployment definitions remain valid
- The change is deployed to its target environment
- Observability confirms expected behavior
- Acceptance evidence is linked from the issue
- Review comments and release-blocking defects are resolved

“Code written” is not Done.

## 6. Backlog refinement

Refine the next two sprints every Saturday:

1. Remove duplicates and obsolete work.
2. Split large stories.
3. Confirm dependencies.
4. Reorder by risk and product value.
5. Move only Ready items into the next sprint.
6. Keep lower-priority ideas in Backlog.

## 7. Daily progress update

Use this short format in the active sprint issue:

```markdown
### YYYY-MM-DD

- Completed:
- Next:
- Blocked:
- Evidence:
- Risk or decision:
```

If nothing changed, write “No progress” and the reason. Do not silently mark time as progress.

## 8. Sprint planning template

```markdown
## Sprint goal

One outcome that can be demonstrated.

## Committed backlog

- [ ] PDP-### Story and acceptance result

## Capacity

Available focused days:
Reserved buffer:

## Risks and dependencies

- Risk:
- Dependency:

## Evidence expected

- Test report
- Screenshot or dashboard
- Commit or image digest
```

## 9. Sprint review template

```markdown
## Goal result

Achieved / Partially achieved / Not achieved

## Demonstration

- Commit:
- Environment:
- Test result:
- Screenshot or dashboard:

## Accepted work

- PDP-###

## Returned to backlog

- PDP-### and reason
```

## 10. Retrospective template

```markdown
## Continue

- What worked

## Stop

- What caused waste or risk

## Start

- One improvement for next sprint

## Measured result

- Planned items:
- Accepted items:
- Blocked items:
- Escaped defects:
```

Limit the improvement action to one change that can be tested in the next sprint.

## 11. Handling unplanned work

- Production incident: interrupt the sprint and record the scope change.
- Critical security issue: interrupt when exposure is credible.
- Normal defect: prioritize against committed work.
- New idea: add it to Backlog; do not silently expand the sprint.
- Blocked item: document the blocker within one working day and select another Ready item if available.

## 12. Metrics

Track only metrics that help decisions:

| Metric | Use |
| --- | --- |
| Planned versus accepted items | Improve realistic sprint planning |
| Cycle time | Identify slow workflow stages |
| Blocked time | Remove recurring dependencies |
| Escaped defects | Improve tests and review |
| Deployment frequency | Measure delivery flow |
| Change failure rate | Measure release quality |
| Recovery time | Measure operational readiness |

Do not use velocity to compare people or reward inflated estimates.
