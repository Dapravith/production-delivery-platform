# Production Delivery Platform instructions

Follow the repository working agreement in `AGENTS.md` and read the relevant documents under `docs/` before proposing or changing code.

- Tie changes to a `PDP-###` backlog item or explain the urgent defect.
- Preserve the service, data-ownership, security, CI/CD, and GitOps boundaries in `AGENTS.md`.
- Make small, reviewable changes and add tests for behavior and failure paths.
- Run the documented verification commands. Never claim unexecuted checks passed.
- Never add secrets, private data, production credentials, or private endpoints.
- AI does not approve its own changes or deploy directly to production.
- Update documentation and progress only when behavior or accepted evidence changed.
