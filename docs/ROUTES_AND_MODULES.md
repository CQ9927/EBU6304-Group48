# Routes and Module Ownership

## Route Map

| Route | Servlet | JSP | Data Files | Owner |
|---|---|---|---|---|
| `/home` | `HomeServlet` | `home.jsp` | — | Core |
| `/login` | `LoginServlet` | `auth/login.jsp` | `users.json` | CQ9927 (baseline) |
| `/ban-appeal` | `BanAppealServlet` | `auth/ban-appeal.jsp` | `users.json` (read/write appeal fields) | BUCOD (public; no role filter) |
| `/register` | `RegisterServlet` | `auth/register.jsp` | `users.json` | CQ9927 (baseline) |
| `/logout` | `LogoutServlet` | — | — | CQ9927 (baseline) |
| `/ta/dashboard` | `TaDashboardServlet` | `ta/dashboard.jsp` | — | Placeholder → extend |
| `/ta/profile` | `TaProfileServlet` | `ta/profile.jsp` | `profiles.json` | zzzskl |
| `/ta/cv` | `TaCvServlet` | `ta/cv.jsp` | `profiles.json`, `CV_*.txt/json` | zzzskl |
| `/ta/jobs` | `TaJobsServlet` | `ta/jobs.jsp` | `jobs.json` | SpPt2FeMa |
| `/ta/apply` | `TaApplyServlet` | `ta/apply.jsp` | `applications.json` | SpPt2FeMa |
| `/ta/status` | `TaStatusServlet` | `ta/status.jsp` | `applications.json` | SpPt2FeMa |
| `/mo/dashboard` | `MoDashboardServlet` | `mo/dashboard.jsp` | — | Placeholder → extend |
| `/mo/jobs/new` | `MoPostJobServlet` | `mo/post-job.jsp` | `jobs.json` | yunmengdd |
| `/mo/jobs/select` | `MoSelectServlet` | `mo/select.jsp` | `applications.json`, `selection.json` | yunmengdd |
| `/admin/workload` | `AdminDashboardServlet` | `admin/workload.jsp` | `jobs.json`, `applications.json` | BUCOD |
| `/admin/users` | `AdminUsersServlet` | `admin/users.jsp` | `users.json` (ban / reset password) | BUCOD (+ CQ9927 review for `UserRepository`) |
| `/admin/applications` | `AdminApplicationsServlet` | `admin/applications.jsp` | `applications.json` (admin revoke) | BUCOD (+ SpPt2FeMa review for `ApplicationRepository`) |


## Shared Modules
- `service/MatchingService`: score and missing skills logic
- `service/WorkloadService`: workload summary and conflict hints
- `repository/*Repository`: all file read/write operations
- `filter/AuthFilter`: login and role-based page access (`/ta/*`, `/mo/*`, `/admin/*`); re-checks `users.json` `banned` and redirects to `/login?error=banned` if set
- `web.xml` `appealContactUrl`: link target for banned-account appeal (shown on login page)


## Ownership Rules
- Owner writes first implementation and unit-level checks.
- Any change to shared modules requires at least one review approval.
- Data contract changes must update `DATA_SCHEMA.md` in the same PR.
