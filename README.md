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

- **Runtime directory (default):** `${user.home}/ebu6304-group48-data/` on the machine running Tomcat.  
  Override with context param `dataDirectory` in `src/main/webapp/WEB-INF/web.xml` (see comment there).
- **Demo dataset (repo):** [`data/`](data/) is bundled into the WAR under `classpath:data/`. On startup, `DemoDataContextListener` copies `profiles.json`, `jobs.json`, `applications.json`, and `selection.json` into the **runtime** directory when each file is missing or contains only `[]`, and copies missing files from `data/cvs/` into `{runtime}/cvs/`. `users.json` is synced separately by `UserRepository`. You can still copy or edit runtime files by hand when needed.

### Demo accounts (development)

Bundled demo users come from [`data/users.json`](data/users.json), which is also packaged into the WAR (`classpath:data/users.json`). Password for every account below is **`demo123`**.

| Username | Role |
|----------|------|
| `ta_demo` | TA |
| `ta_li` | TA |
| `mo_demo` | MO |
| `admin_demo` | ADMIN |

**Behaviour:** If runtime `users.json` is missing or empty (`[]`), it is filled with that full list. If the file already has users, each bundled demo username (case-insensitive) is **updated** from the bundle or **appended** if missing — so wrong password hashes in older files are corrected on the next access, without removing non-demo registrations.

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
