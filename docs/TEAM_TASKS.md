# Team tasks — ownership & merge-conflict avoidance

This document is the **single source of truth** for who does what. If you need to touch another person’s **exclusive** files, open an Issue first or pair in a short sync.

---

## 1. People → responsibilities (one primary owner each)

| GitHub | Role theme | You implement | Primary routes | Data / persistence |
|--------|------------|---------------|----------------|---------------------|
| **CQ9927** | Integration & auth (maintainer) | Login, register, logout, session, `AuthFilter`, `EncodingFilter`, `HomeServlet`, shared fixes, **batch dashboard / home nav updates** when others finish features | `/login`, `/register`, `/logout`, `/home` | `users.json` via `UserRepository` only |
| **zzzskl** | TA profile & CV | `TaProfileServlet`, `TaCvServlet`, JSPs, `ProfileRepository` (new), links on **TA dashboard** for your pages only | `/ta/profile`, `/ta/cv` | `profiles.json`; CV files under runtime dir (agreed subfolder name in PR) |
| **SpPt2FeMa** | TA jobs, apply, status | `TaJobsServlet`, `TaApplyServlet`, `TaStatusServlet`, JSPs, `ApplicationRepository` (new), **read** jobs for listing | `/ta/jobs`, `/ta/apply`, `/ta/status` | `applications.json` (TA create/list); **read** `jobs.json` for listings |
| **yunmengdd** | MO jobs & selection | `MoPostJobServlet`, `MoSelectServlet`, JSPs, `JobRepository` (new), extend `MoDashboardServlet` / `mo/dashboard.jsp` with your entry links only | `/mo/jobs/new`, `/mo/jobs/select` | `jobs.json` (write); `applications.json` / `selection.json` for MO updates (**coordinate with SpPt2FeMa** — see §3) |
| **BUCOD** | Admin workload & rules | Replace/extend `AdminDashboardServlet` workload UI, `MatchingService`, `WorkloadService`, admin JSPs; `AdminUsersServlet`, `AdminApplicationsServlet` | `/admin/workload`, `/admin/users`, `/admin/applications` | Read `jobs.json`, `applications.json`; write `users.json` (ban/password) and `applications.json` (admin revoke) per `DATA_SCHEMA.md` — PRs touching `UserRepository` / `LoginServlet` / `AuthFilter` / `login.jsp` / `web.xml` need **CQ9927** review; `ApplicationRepository` extensions need **SpPt2FeMa** review |

**`TaDashboardServlet` / `ta/dashboard.jsp`:** each member adds **only** the links/buttons for **their** TA routes (small PRs). If two people edit the same lines, **CQ9927** resolves or merges a combined nav in one integration PR.

---

## 2. Exclusive file zones (to reduce Git conflicts)

| GitHub | Prefer to create/edit only under | Do **not** change without agreement |
|--------|-----------------------------------|-------------------------------------|
| CQ9927 | `.../filter/*`, `LoginServlet`, `RegisterServlet`, `LogoutServlet`, `HomeServlet`, `UserRepository`, `User.java`, `SessionKeys`, `PasswordHash`, `auth/*.jsp`, `home.jsp`, `web.xml` (only if required) | Others’ servlets/JSPs |
| zzzskl | `TaProfileServlet`, `TaCvServlet`, `ta/profile.jsp`, `ta/cv.jsp`, `ProfileRepository`, `model` classes **only if** profile/CV-specific | `ApplicationRepository`, `JobRepository`, MO/Admin servlets |
| SpPt2FeMa | `TaJobsServlet`, `TaApplyServlet`, `TaStatusServlet`, `ta/jobs.jsp`, `ta/apply.jsp`, `ta/status.jsp`, `ApplicationRepository` | `JobRepository` internals (see §3); auth filters |
| yunmengdd | `MoPostJobServlet`, `MoSelectServlet`, `mo/post-job.jsp`, `mo/select.jsp`, `JobRepository`, MO-related updates to `mo/dashboard.jsp` | `UserRepository`, `ApplicationRepository` **body** (use §3) |
| BUCOD | `MatchingService`, `WorkloadService`, `admin/*.jsp`, admin-only servlets | `UserRepository` / core auth files only with **CQ9927** review (admin ban/password features) |

Use **`@WebServlet`** for new endpoints so you rarely need the same `web.xml` edit.

---

## 3. Shared data — how to avoid merge fights

| Topic | Rule |
|-------|------|
| **`jobs.json`** | **yunmengdd** owns `JobRepository` and all **write** shapes. **SpPt2FeMa** needs listings: ask **yunmengdd** to add `findOpenJobs()` (or similar) in a small PR, or **SpPt2FeMa** opens a PR that **only** adds read methods and assigns **yunmengdd** as reviewer. |
| **`applications.json`** | **SpPt2FeMa** owns `ApplicationRepository` for TA flows. **yunmengdd** for MO status changes: **either** extend `ApplicationRepository` in a PR **reviewed by SpPt2FeMa**, **or** agree in stand-up and merge in order **SpPt2FeMa first**, then **yunmengdd**. |
| **Admin revoke** | `ApplicationRepository.rejectByAdmin(...)` sets `REJECTED` + `adminRevoked`; `updateStatus` (MO flow) clears `adminRevoked`. Further rule changes: coordinate **SpPt2FeMa** / **yunmengdd**. |
| **`DATA_SCHEMA.md`** | Any field/enum change: **one PR** that updates the doc **and** code; tag both data owners in review. |
| **`pom.xml`** | Dependency adds: one PR at a time; ping **CQ9927** if the branch is busy. |

---

## 4. Git workflow (short)

1. **Pull `main` before** `git checkout -b yourname/feature-xyz`.
2. **Small PRs** (one feature or one servlet + JSP).
3. **Do not** reformat unrelated files.
4. If Git marks a conflict in a file you do not own, **ask the owner** or **CQ9927** to help resolve.

---

## 5. Auth smoke test (everyone after pulling `main`)

1. Deploy WAR, open `/login`.
2. `ta_demo` / `demo123` → `/ta/dashboard`.
3. `mo_demo` / `demo123` → `/mo/dashboard`.
4. TA session opening `/mo/dashboard` → blocked.
5. Register a user → row in runtime `users.json`.
