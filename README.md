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

### Data files

- **Committed samples (empty arrays):** `data/*.json` in the repo — use as templates.
- **Runtime directory (default):** `${user.home}/ebu6304-group48-data/` on the machine running Tomcat.  
  Override with context param `dataDirectory` in `src/main/webapp/WEB-INF/web.xml` (see comment there).

### Project layout

- `src/main/java/com/ebu6304/group48/` — servlets, filters, services, repositories (to be added)
- `src/main/webapp/` — JSP, `WEB-INF/web.xml`
- `docs/` — scope, schema, routes, workflow
