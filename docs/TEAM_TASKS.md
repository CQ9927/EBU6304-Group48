# Team task split (suggested)

Auth, session, and role routing are implemented on `main` / latest branch — teammates can start from the dashboards below.

| Area | Routes (first deliverables) | Suggested owner (GitHub) |
|------|---------------------------|-------------------------|
| Auth baseline (done) | `/login`, `/register`, `/logout`, `AuthFilter`, `users.json` | CQ9927 |
| TA features | `/ta/profile`, `/ta/cv`, `/ta/jobs`, `/ta/apply`, `/ta/status` | zzzskl + SpPt2FeMa (split) |
| MO features | `/mo/jobs/new`, `/mo/jobs/select` | yunmengdd |
| Admin / rules | `/admin/workload`, matching & workload services | BUCOD |

Adjust names in your sprint planning; keep `docs/ROUTES_AND_MODULES.md` in sync when URLs change.

## How to verify auth

1. First deploy: open `/login` — demo users are created automatically under the runtime data directory (see README).
2. Log in as `ta_demo` / `demo123` → must land on `/ta/dashboard`.
3. Log in as `mo_demo` / `demo123` → `/mo/dashboard`.
4. Log in as `admin_demo` / `demo123` → `/admin/workload`.
5. While logged in as TA, open `/mo/dashboard` in the same browser → should redirect to login with forbidden or similar.
6. Register a new user → appears in `users.json` under the runtime data directory.
