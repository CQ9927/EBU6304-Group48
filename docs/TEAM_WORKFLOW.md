# Team Workflow (Git + Collaboration)

## Branch Strategy
- Main branch: `main` (protected)
- Feature branch pattern: `{name}/{feature}`
- Example: `cuihongqing/ta-profile-form`

## Commit Rules
- Use small commits tied to one task.
- Commit message format:
  - `feat: ...`
  - `fix: ...`
  - `docs: ...`
  - `refactor: ...`

## Pull Request Rules
- Every change goes through PR (no direct push to `main`).
- At least 1 teammate approval before merge.
- PR should include:
  - What changed
  - Why changed
  - How tested

## Definition of Done
- Code builds and runs locally.
- Related docs updated when needed.
- No unresolved TODO that blocks demo flow.
- PR approved and merged.

## Weekly Cadence
- Monday: sprint planning and task assignment
- Mid-week: sync and blockers discussion
- Sunday: integration and merge to `main`

## Conflict Handling
- Pull latest `main` before starting work.
- Resolve conflicts in feature branch and push again.
- If data schema changes, announce in group chat and update docs.
