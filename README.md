# EBU6304-Group48

BUPT International School Teaching Assistant Recruitment System - Agile Software Engineering Project

## Group Members

Team roster (GitHub username — student ID):

- CQ9927 - 231225775
- zzzskl - 231226772
- yunmengdd - 231225764
- SpPt2FeMa - 231225812
- BUCOD - 231225904

## Software (Servlet / JSP)

- **JDK:** 17+
- **Build:** Apache Maven 3.9+
- **Runtime:** Apache Tomcat **9.x** (`javax.servlet` API — do not deploy this WAR to Tomcat 10+ without migrating to Jakarta EE)

### Build

```bash
mvn -q -DskipTests package
```

Artifact: `target/ta-recruitment.war`

### Run (Tomcat)

1. Copy `target/ta-recruitment.war` to Tomcat `webapps/`.
2. Start Tomcat.
3. Open `http://localhost:8080/ta-recruitment/` (home redirects to `/ta-recruitment/home`).

### Run (Jetty — local dev)

From the project root (same context path as Tomcat):

```bash
mvn jetty:run
```

Default port is **8080**. If that port is already in use:

```bash
mvn jetty:run -Djetty.port=9080
```

Then open `http://localhost:9080/ta-recruitment/` (or `8080` without override). Stop with Ctrl+C in that terminal.

### Data files

- **Committed samples (empty arrays):** `data/*.json` in the repo — use as templates.
- **Runtime directory (default):** `${user.home}/ebu6304-group48-data/` on the machine running Tomcat.  
  Override with context param `dataDirectory` in `src/main/webapp/WEB-INF/web.xml` (see comment there).

### Demo accounts (development)

On first use, if `users.json` is missing or empty in the **runtime** data directory, three users are created automatically (password for all: **`demo123`**):

| Username | Role |
|----------|------|
| `ta_demo` | TA |
| `mo_demo` | MO |
| `admin_demo` | ADMIN |

Passwords are stored as **SHA-256** hashes (see `PasswordHash`) — fine for coursework; document limitations in the report.

### Smoke test (auth)

1. `mvn -q -DskipTests package`, deploy WAR to Tomcat 9.
2. Open `/ta-recruitment/login` (adjust context path if different).
3. Sign in as `ta_demo` / `demo123` → should open **TA dashboard**.
4. Try `/ta-recruitment/mo/dashboard` while still logged in as TA → should redirect to login (wrong role).
5. **Register** a new user → new row in runtime `users.json`.

### End-to-end demo (apply flow)

Use demo accounts (password `demo123` for all):

1. Sign in as **`mo_demo`** → **Post a new job** (`/mo/jobs/new`), submit the form (note the job id if shown in the table).
2. Sign out, sign in as **`ta_demo`** → complete **Profile** and **CV** if prompted, then open **Jobs** and **Apply** to that job.
3. Sign in as **`mo_demo`** again → **Review applications** (`/mo/jobs/select`), filter by job if needed, move an application through **Under review** → **Select** or **Reject**.
4. Sign in as **`ta_demo`** → **Status** (`/ta/status`) should reflect the updated application status.

### Project layout

- `src/main/java/com/ebu6304/group48/` — servlets, filters, `repository` (e.g. `UserRepository`, `JobRepository`, `ApplicationRepository`, `ProfileRepository`), `service`, models
- `src/main/webapp/` — JSP under `WEB-INF/jsp/`, shared UI `css/app.css`, `WEB-INF/web.xml`
- `docs/` — scope, schema, routes, workflow, `TEAM_TASKS.md`
