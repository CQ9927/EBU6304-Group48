# Routes and Module Ownership

## Route Map

| Route | Servlet | JSP | Data Files | Owner |
|---|---|---|---|---|
| `/login` | `LoginServlet` | `auth/login.jsp` | `users.json` | Member A |
| `/register` | `RegisterServlet` | `auth/register.jsp` | `users.json` | Member A |
| `/ta/profile` | `TaProfileServlet` | `ta/profile.jsp` | `profiles.json` | Member B |
| `/ta/cv` | `TaCvServlet` | `ta/cv.jsp` | `profiles.json`, `CV_*.txt/json` | Member B |
| `/ta/jobs` | `TaJobsServlet` | `ta/jobs.jsp` | `jobs.json` | Member B |
| `/ta/apply` | `TaApplyServlet` | `ta/apply.jsp` | `applications.json` | Member C |
| `/ta/status` | `TaStatusServlet` | `ta/status.jsp` | `applications.json` | Member C |
| `/mo/jobs/new` | `MoPostJobServlet` | `mo/post-job.jsp` | `jobs.json` | Member D |
| `/mo/jobs/select` | `MoSelectServlet` | `mo/select.jsp` | `applications.json`, `selection.json` | Member D |
| `/admin/workload` | `AdminWorkloadServlet` | `admin/workload.jsp` | `jobs.json`, `applications.json` | Member E |

## Shared Modules
- `service/MatchingService`: score and missing skills logic
- `service/WorkloadService`: workload summary and conflict hints
- `repository/*Repository`: all file read/write operations
- `filter/AuthFilter`: login and role-based page access

## Ownership Rules
- Owner writes first implementation and unit-level checks.
- Any change to shared modules requires at least one review approval.
- Data contract changes must update `DATA_SCHEMA.md` in the same PR.
