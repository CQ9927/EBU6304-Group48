---
title: "EBU6304 Software Engineering Group Project — Final Report"
subtitle: "TA Recruitment System"
author: "Group 48"
date: "2025-26"
---

# Final Short Report

## Group 48 — TA Recruitment System

| Role | QM No. | Name |
|------|--------|------|
| Member 1 |  |  |
| Member 2 |  |  |
| Member 3 |  |  |
| Member 4 |  |  |
| Member 5 |  |  |
| Member 6 |  |  |

---

## 1. Design

### 1.1 Design Strategy

The TA Recruitment System is a web-based application that connects Teaching Assistants (TAs) with Module Organisers (MOs) at BUPT International School. The design strategy prioritises **simplicity**, **modularity**, and **constraint compliance**. The project handout mandates a Java Servlet/JSP architecture deployed on Apache Tomcat with text-file-based persistence — no database systems are permitted. These constraints shaped every design decision.

Our approach follows two guiding principles: (a) **separation of concerns** at every layer, so that each team member can work independently without merge conflicts; and (b) **convention over configuration**, using standard Maven layout and annotation-driven servlet mapping to minimise boilerplate and reduce integration friction.

### 1.2 Architecture

The system adopts a **layered Model-View-Controller (MVC)** architecture organised into three tiers:

```
[Presentation]     JSP Views + CSS (app.css)
      |
[Business Logic]   Servlets → Services → Repositories
      |
[Data]             JSON files on local filesystem
```

**Presentation layer:** Fourteen JSP pages render HTML views, styled by a single shared stylesheet (`app.css`, ~1,120 lines) using BEM naming conventions and CSS custom properties (design tokens). Views are organised by role under `/WEB-INF/jsp/` and are inaccessible directly via URL — all access flows through servlets.

**Business Logic layer:** Servlets annotated with `@WebServlet` handle HTTP requests and delegate to service and repository classes. Two servlet filters wrap all requests: `EncodingFilter` (UTF-8 enforcement) and `AuthFilter` (session-based role access control). Two service classes — `MatchingService` (rule-based conflict detection) and `WorkloadService` (aggregated admin dashboard data) — orchestrate across repositories.

**Data layer:** Five JSON files store all persistent state: `users.json`, `profiles.json`, `jobs.json`, `applications.json`, and `selection.json`. Each file is managed by a dedicated repository class that encapsulates Gson serialisation/deserialisation behind synchronised file I/O operations.

### 1.3 Component Design

The Java package `com.ebu6304.group48` contains seven sub-packages (32 source files):

| Package | Responsibility | Key Classes |
|---------|---------------|-------------|
| `config/` | Application bootstrap and path resolution | `AppPaths`, `DemoDataContextListener` |
| `filter/` | Request preprocessing (encoding, auth) | `AuthFilter`, `EncodingFilter` |
| `model/` | Domain entities as POJOs | `User`, `Profile`, `Job`, `Application` |
| `repository/` | File-based JSON data access | `UserRepository`, `JobRepository`, `ProfileRepository`, `ApplicationRepository` |
| `service/` | Cross-cutting business logic | `MatchingService`, `WorkloadService` |
| `servlet/` | HTTP request handling (14 servlets) | `LoginServlet`, `TaDashboardServlet`, `MoSelectServlet`, etc. |
| `util/` | Shared utilities | `PasswordHash`, `SessionKeys`, `RoleLanding` |

The view layer mirrors this organisation — JSP pages are grouped by role (`ta/`, `mo/`, `admin/`, `auth/`) with a shared include (`_include/app-header.jsp`) providing role-aware navigation.

### 1.4 Design Patterns Applied

**Repository Pattern.** Each JSON data file is accessed exclusively through its repository class. Repositories encapsulate file paths, Gson configuration, and synchronised read/write logic. Consumers (servlets and services) never touch the filesystem directly. This enables the data format to evolve independently of business logic.

**Filter Chain.** The `EncodingFilter → AuthFilter → Servlet` pipeline demonstrates the Chain of Responsibility pattern. Filters are declaratively mapped in `web.xml` and execute in order, with each filter responsible for a single concern.

**Front Controller (variation).** While not a classic Front Controller, `AuthFilter` acts as a central gateway, inspecting every request to protected URL patterns (`/ta/*`, `/mo/*`, `/admin/*`) and redirecting unauthenticated or wrong-role users before any servlet executes.

**Singleton (lock objects).** Each repository declares a `static final Object FILE_LOCK` used as a synchronisation monitor for all file I/O. This provides thread-safe concurrent access to the JSON data files without introducing a dependency on external locking infrastructure.

**Model-View-Controller.** Servlets act as controllers (processing requests, invoking business logic, placing data in request attributes), JSPs act as views (rendering HTML from request-scoped data), and model/repository classes form the model layer.

### 1.5 Data Design

Five JSON files constitute the complete data model:

```
users.json        — accounts: userId, username, passwordHash (salted SHA-256), role (TA/MO/ADMIN)
profiles.json     — TA profiles: name, email, major, skills[], availability[]
jobs.json         — job postings: title, type (MODULE/INVIGILATION), semester, capacity, status (OPEN/CLOSED)
applications.json — applications: jobId, applicantUserId, matchScore, status (SUBMITTED→UNDER_REVIEW→SELECTED/REJECTED)
selection.json    — audit log of MO selection decisions
```

**Design conventions:** All IDs are unique prefixed strings (e.g., `J-XXXXXXXX`, `A-XXXXXXXX`). Timestamps use ISO 8601 format. Enumerated values use uppercase string constants. The application status follows a defined state machine: `SUBMITTED → UNDER_REVIEW → SELECTED/REJECTED`. A demo data seeding mechanism copies bundled JSON files from the WAR classpath to a runtime directory (`${user.home}/ebu6304-group48-data/`) on first startup, ensuring the application works out of the box.

---

## 2. Implementation

### 2.1 Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 |
| Build | Maven (WAR packaging) | 3.9+ |
| Web Framework | Java Servlet API (javax) | 4.0.1 |
| Templating | JSP + JSTL | 1.2.2 / 1.2.5 |
| JSON | Gson (Google) | 2.11.0 |
| Dev Server | Jetty Maven Plugin | 9.4.54 |
| Production Server | Apache Tomcat | 9.x |

The key build command is `mvn jetty:run`, which starts an embedded Jetty server on port 8080 at context path `/ta-recruitment`. Production deployment uses `mvn package` to produce `ta-recruitment.war`, deployed to a standalone Tomcat instance.

### 2.2 Implementation Strategy and Build Plan

The project was delivered over **four sprints** spanning March to May 2026:

**Sprint 1 (Weeks 1–3): Foundation.** Project bootstrap: Maven WAR skeleton, `web.xml` configuration, package structure, placeholder JSP pages, README and team documentation. Outcome: compilable empty project with agreed architecture.

**Sprint 2 (Weeks 4–6): Core Authentication and TA/MO Flows.** User registration and login with salted SHA-256 password hashing. `AuthFilter` enforces role-based access control. TA profile creation/update, CV file upload with format validation (PDF/DOC/DOCX, 5 MB limit), and job browsing with skill-matching display. MO job posting with form validation and applicant listing.

**Sprint 3 (Weeks 7–9): Matching, Admin, and Polish.** TA application submission and persistent status tracking. MO applicant review with status progression (SUBMITTED → UNDER_REVIEW → SELECTED/REJECTED). Admin workload dashboard with aggregated statistics and rule-based conflict hints (over-capacity, duplicate applications, closed-job pending applications, low-score selections). Shared UI stylesheet (`app.css`) with responsive design, design tokens, and role-aware navigation header.

**Sprint 4 (Weeks 10–12): Integration, Bug Fixing, and Final Deliverables.** Cross-module integration testing, merge conflict resolution, UI consistency pass, documentation updates, demo video recording, and this final report.

### 2.3 Version Control and Collaboration

The team used **GitHub** with a protected `main` branch. All changes followed a **Pull Request (PR)-based workflow**:

- **Branch naming:** `{member}/{feature}` (e.g., `BUCOD/admin-workload`, `SpPt2FeMa`)
- **Commit conventions:** `feat:`, `fix:`, `docs:`, `refactor:` prefixes
- **PR requirements:** At least one teammate approval before merge; description must state what changed, why, and how it was tested
- **Weekly cadence:** Monday planning, mid-week sync, Sunday integration

To minimise merge conflicts across five contributors, the team adopted **exclusive file ownership zones**. Each member "owned" specific servlets, repositories, and JSP pages. For shared data files (e.g., `applications.json`), one member owned writes while others read through exposed repository methods, with schema changes announced in the group chat and documented in `DATA_SCHEMA.md`.

### 2.4 Key Implementation Highlights

**Authentication and Authorisation.** `AuthFilter` intercepts requests to `/ta/*`, `/mo/*`, and `/admin/*`, checking the HTTP session for `auth.userId`, `auth.username`, and `auth.role` attributes. Wrong-role access redirects to the user's role-appropriate landing page with a notice. Unauthenticated access redirects to `/login?next=<original_path>`, with open-redirect protection via a `safeNext()` validator.

**Demo Data Seeding.** `DemoDataContextListener` (a `ServletContextListener`) fires on application startup. It checks whether the runtime data directory is empty and, if so, copies bundled JSON seed files from the WAR classpath. Four demo accounts exist out of the box (`ta_demo`, `ta_li`, `mo_demo`, `admin_demo`; password: `demo123`), enabling immediate walkthrough of all workflows.

**Synchronised File I/O.** All repository read/write operations are wrapped in `synchronized (FILE_LOCK)` blocks, ensuring thread safety for the JSON file store without external dependencies. Repositories return empty collections (never null) on I/O errors, providing graceful degradation.

**Password Security.** `PasswordHash` implements salted SHA-256 hashing using the format `SHA-256("ebu6304:" + username + ":" + plainPassword)`. While not production-grade (bcrypt/Argon2 would be preferred), this meets the coursework requirements and is explicitly documented as such.

**Matching Heuristics.** `MatchingService.buildConflictHints()` runs five rule checks: unknown job references in applications, duplicate applications (same TA + same job), over-capacity selections, closed jobs with pending applications, and selected applications with low match scores (< 50). Results are displayed on the admin workload dashboard.

---

## 3. Testing

### 3.1 Testing Strategy

Given the project's scope and the absence of a database layer, the team adopted a **manual functional testing** approach. The decision to forgo automated testing frameworks (JUnit, Mockito) was pragmatic: the primary complexity lay in the HTTP request/response cycle and session state, which are more naturally verified through end-to-end walkthroughs in a browser. All testing was performed against a local Tomcat/Jetty instance with the seeded demo dataset.

### 3.2 Test Case Design

The test suite was organised around the three user roles and their core workflows:

**Authentication Tests:**

| Test Case | Input | Expected Outcome |
|-----------|-------|-----------------|
| Valid login (demo account) | `ta_demo` / `demo123` | Redirect to TA dashboard, session populated |
| Invalid password | `ta_demo` / `wrong` | Error message, stay on login page |
| Duplicate registration | Existing username | Error: username already taken |
| Empty credentials | Blank fields submitted | Client-side validation blocks submission |

**Role-Based Access Control Tests:**

| Test Case | Action | Expected Outcome |
|-----------|--------|-----------------|
| Unauthenticated access | Navigate directly to `/ta/dashboard` | Redirect to `/login?next=/ta/dashboard` |
| Wrong-role access (TA visits MO) | Log in as TA, navigate to `/mo/dashboard` | Redirect to TA dashboard with `?notice=forbidden` |
| Session expiry | Clear session cookie, navigate to protected page | Redirect to login |

**TA Workflow Tests:**

| Test Case | Steps | Expected Outcome |
|-----------|-------|-----------------|
| Complete profile | Fill all fields, submit | Profile saved, confirmation message displayed |
| Partial profile | Submit with empty required fields | Validation error, form re-displayed |
| Upload CV | Select PDF file < 5 MB | File saved, listed in CV manager |
| Upload invalid file type | Select .exe file | Rejected with error message |
| Browse jobs | View `/ta/jobs` | OPEN jobs listed with skill match percentages |
| Apply for job | Click apply on an OPEN job | Application created, status = SUBMITTED |
| Duplicate application | Apply again for same job | Prevented or warning displayed |

**MO Workflow Tests:**

| Test Case | Steps | Expected Outcome |
|-----------|-------|-----------------|
| Post new job | Fill all fields, submit | Job created with status OPEN |
| Post invalid job | Submit with missing required fields | Validation error |
| View applicants | Open a job's applicant list | All applications listed with match scores |
| Select applicant | Change status to SELECTED | Status updated, applicant sees new status |
| Reject applicant | Change status to REJECTED | Status updated accordingly |

**Admin Workflow Tests:**

| Test Case | Steps | Expected Outcome |
|-----------|-------|-----------------|
| View workload dashboard | Log in as admin, visit `/admin/workload` | Aggregated stats displayed per job |
| Conflict detection | Create over-capacity selection | Warning hint appears on dashboard |
| Closed-job pending apps | Close a job with pending applications | Warning hint appears |

### 3.3 Test Results

All core workflow test cases passed. The demo walkthrough — register as TA, complete profile, apply for a job, MO reviews and selects, TA verifies updated status — executes end-to-end without errors. Edge cases handled correctly include: empty data files (system auto-seeds from classpath), concurrent access to the same JSON file (synchronised blocks prevent corruption), and session timeout (AuthFilter redirects to login).

### 3.4 Testing Limitations

- **No automated unit tests:** Repository methods, service logic, and password hashing have no programmatic test coverage. Regression testing is entirely manual.
- **No integration test framework:** HTTP request/response cycles are not tested programmatically (e.g., no use of HttpUnit or Spring MockMvc equivalents).
- **No CI/CD pipeline:** Builds and tests are run manually on each developer's machine.
- **Limited edge-case coverage:** Concurrent write stress testing, large-file CV uploads beyond 5 MB, and malformed JSON recovery were not systematically tested.

---

## 4. The Use of Generative AI (GenAI)

### 4.1 Tools Used

The primary GenAI tool employed throughout the project was **Claude Code** (Anthropic), an AI-powered coding assistant integrated into the development environment. Team members used it across multiple stages of the software development lifecycle.

### 4.2 Application at Different Stages

**Requirements Analysis and Planning.** Claude Code assisted in breaking down the project handout into discrete tasks, identifying dependencies between modules, and drafting team documentation (`TEAM_TASKS.md`, `DATA_SCHEMA.md`, `ROUTES_AND_MODULES.md`). It helped translate natural-language requirements into structured Markdown specifications.

**Design.** The AI contributed to architecture discussions by suggesting the layered MVC pattern, recommending the repository abstraction for file-based persistence, and proposing the filter chain design for cross-cutting concerns. It generated initial package-structure diagrams and identified the need for exclusive file ownership to avoid merge conflicts.

**Code Generation.** This was the most heavily AI-assisted stage. Claude Code generated:
- Servlet scaffolding with `@WebServlet` annotations, `doGet()`/`doPost()` method stubs, and JSP forwarding logic
- Repository implementations with Gson serialisation, synchronised file I/O, and `ensureStorage()` patterns
- JSP templates with form validation, conditional rendering, and role-aware navigation
- CSS stylesheet with BEM naming, design tokens, and responsive grid layouts
- Model POJOs with constructors, getters/setters, and `toString()` methods

**Debugging and Troubleshooting.** When build failures or runtime errors occurred, Claude Code diagnosed issues from stack traces, identified root causes (e.g., missing dependencies, incorrect URL patterns, classpath misconfiguration), and suggested fixes. It was particularly effective at resolving Maven dependency and compilation issues.

**Documentation.** The AI generated code comments, JavaDoc-style documentation, Markdown reports (`MEMBER-B-COMPLETION.md`), and this final report. It also assisted in converting Chinese-language team communications to English documentation.

**Refactoring and Code Review.** Claude Code reviewed pull requests for consistency with project conventions, identified potential null-pointer risks, and suggested simplifications.

### 4.3 Effectiveness Evaluation

**Strengths:**
- **Rapid prototyping:** Boilerplate code that would take hours to write manually (repository classes with all CRUD operations, servlet skeletons, JSP form templates) was generated in minutes.
- **Consistency:** The AI reliably applied the same patterns across all files — synchronised locks, Gson configuration, error-handling conventions — reducing stylistic divergence across five contributors.
- **Error diagnosis speed:** Stack trace analysis reduced debugging time significantly compared to manual investigation.
- **Documentation throughput:** Generating structured Markdown documentation from code was near-instantaneous.

**Limitations:**
- **Context window constraints:** As the project grew beyond ~30 files, the AI could not hold the entire codebase in context simultaneously, occasionally leading to suggestions inconsistent with code it had not recently seen.
- **API hallucination:** On rare occasions, the AI suggested methods or classes that do not exist in the javax.servlet or Gson APIs, requiring manual correction.
- **Security complacency:** The AI generated SHA-256-based password hashing without proactively recommending bcrypt/Argon2. Human judgment was needed to assess whether this was acceptable for the coursework context.
- **Over-engineering tendency:** Without explicit constraints, the AI sometimes proposed abstractions (interfaces, factory patterns, builder patterns) disproportionate to the project's size and scope.

**Challenges:**
- **Prompt engineering learning curve:** Team members needed time to learn how to write effective prompts that produced useful output — overly broad prompts yielded vague suggestions; overly narrow prompts missed context.
- **Code ownership ambiguity:** When multiple team members used AI on overlapping files, it became harder to attribute and review changes. The exclusive file ownership policy mitigated this.
- **Verification overhead:** Every AI-generated code block required human verification for correctness, security, and alignment with project conventions. The time saved in writing was partially offset by time spent in review.

### 4.4 Critical Reflection

GenAI, specifically Claude Code, served as a **force multiplier** throughout this project. It accelerated development by an estimated 2–3× compared to writing equivalent code manually, particularly for repetitive infrastructure code (repositories, model classes, JSP templates). However, it was most effective when treated as a **junior pair programmer** rather than an autonomous developer: generating drafts that experienced team members then reviewed, corrected, and integrated. Architectural decisions — the choice of MVC layering, the filter chain design, the exclusive-ownership collaboration model — remained firmly human-driven. The key lesson is that GenAI reduces the cost of writing code but does not reduce the need for thoughtful design, rigorous review, and team coordination.

---

## 5. Individual Contribution and Reflection

*Each group member should complete one copy of the template below. Maximum 300 words per member.*

---

**QM no:** 231226772
**Name:** [Please fill in your name]
**Main contribution:** Designed and implemented the complete TA-facing module of the recruitment system. This included three servlets — TaProfileServlet for profile creation and updating with UUID-based ID generation, form validation, and create-or-update logic; TaCvServlet for CV file upload with format restrictions (PDF/DOC/DOCX, 5 MB limit), multi-file management, and active CV designation; and TaJobsServlet for job browsing with multi-criteria filtering by type, semester, and skill; the accompanying JSP view implements a skill-matching display that computes (user skills ∩ job skills / job skills) × 100% for personalised match scores. Created the three corresponding JSP views with responsive design, client-side validation, and user-facing message systems. Authored four core data model classes (Profile, Job, Application, User) that established the project-wide data schema, and defined three repository classes (ProfileRepository, JobRepository, ApplicationRepository) that set the data access contract subsequently extended by other team members. Wrote the detailed dependency requirements document (REQUIREMENTS-MEMBER-B.md) that coordinated cross-module integration.

**Reflective statement:** This project provided my first hands-on experience building a full-stack Java web application with Servlet/JSP. I gained practical understanding of MVC architecture — how servlets act as controllers, JSPs as views, and repositories as the model layer. The most significant challenge was managing inter-module dependencies: my servlets required working repository implementations from other team members that were not yet available during early development. I addressed this by designing placeholder repositories that returned sensible defaults, allowing front-end development and compilation to proceed independently. This taught me the value of interface-first design and clear dependency documentation. I also learned session-based authentication, file-based persistence with Gson and synchronised access, and the collaborative discipline of PR-based Git workflows. Working in a constrained environment — no database, plain Servlets without Spring — deepened my appreciation for framework abstractions and strengthened my fundamental Java web development skills. The REQUIREMENTS-MEMBER-B.md document I wrote proved essential for cross-module coordination, reinforcing that clear written specifications are as important as the code itself.

---

**QM no:** 
**Name:** 
**Main contribution:** 

**Reflective statement:** 

---

**QM no:** 
**Name:** 
**Main contribution:** 

**Reflective statement:** 

---

**QM no:** 
**Name:** 
**Main contribution:** 

**Reflective statement:** 

---

**QM no:** 
**Name:** 
**Main contribution:** 

**Reflective statement:** 

---

**QM no:** 
**Name:** 
**Main contribution:** 

**Reflective statement:** 

---

*End of report.*
