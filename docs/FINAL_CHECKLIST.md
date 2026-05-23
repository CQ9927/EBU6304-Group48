# Final Submission Checklist — Group 48

## 1. Branches to Merge

- [ ] **`deyu/MO_second_update`** — yunmengdd, 1 commit `0ae713d "second update Mo"`, last activity 2026-05-23

> PDF requirement: *"All sub-branch contributions are merged to the master branch by the checking date."*

---

## 2. Missing Deliverables

### 2.1 Test Programs

- [ ] Add test dependency to `pom.xml` (JUnit 5)
- [ ] Create `src/test/java/com/ebu6304/group48/` directory
- [ ] Write tests for core components:
  - [ ] `MatchingServiceTest` — match score computation, missing skills, conflict hints
  - [ ] `PasswordHashTest` — hashing determinism, verification
  - [ ] `RoleLandingTest` — correct default paths per role
  - [ ] `ApplicationRepositoryTest` — save, find, status transitions, dedup
  - [ ] `JobRepositoryTest` — findAll, findById, findAllOpenJobs, save
  - [ ] `ProfileRepositoryTest` — findByUserId, save (create + update)
- [ ] Update `FINAL_REPORT.md` Section 3.4 (currently says "No automated unit tests")

> PDF requirement: *"Test programs"* and *"Testing"* scoring category (5 marks)

### 2.2 User Manual with Screenshots

- [ ] Capture screenshots for each main page:
  - [ ] Login page (`/login`)
  - [ ] TA Dashboard (`/ta/dashboard`)
  - [ ] TA Profile (`/ta/profile`)
  - [ ] TA CV Management (`/ta/cv`)
  - [ ] TA Jobs (`/ta/jobs`)
  - [ ] TA Application Status (`/ta/status`)
  - [ ] MO Dashboard (`/mo/dashboard`) — including post-job modal
  - [ ] MO Review Applications (`/mo/jobs/select`)
  - [ ] Admin Workload (`/admin/workload`)
  - [ ] Admin Users (`/admin/users`)
  - [ ] Admin Applications (`/admin/applications`)
  - [ ] Ban Appeal (`/ban-appeal`)
  - [ ] 404 / 500 error pages
- [ ] Write step-by-step walkthrough for each role (TA / MO / Admin)
- [ ] Document all demo accounts and passwords
- [ ] Save as `docs/USER_MANUAL.pdf`

> PDF requirement: *"A user manual including key screenshots of the application (at least one for each main frame)"*

---

## 3. Bugs to Fix

- [ ] **CV "Set as Active" button has no backend handler**
  - File: `src/main/webapp/WEB-INF/jsp/ta/cv.jsp` line 99 (`action="setactive"`)
  - File: `src/main/java/com/ebu6304/group48/servlet/TaCvServlet.java` (`doPost` only handles `upload` and `delete`)
  - Fix: add `"setactive"` case in `doPost` that updates `profile.cvFileName` to the selected file

---

## 4. FINAL_REPORT.md Issues

- [ ] Fix date: `date: "2025-26"` → `date: "2025-2026"` or `date: "May 2026"`
- [ ] Fill member table (line 12-19): QM No. + Name for all 5 members
- [ ] Fill 4 blank member sections (lines 280-317):
  - [ ] Member 2 — Main contribution + Reflective statement
  - [ ] Member 3 — Main contribution + Reflective statement
  - [ ] Member 4 — Main contribution + Reflective statement
  - [ ] Member 5 — Main contribution + Reflective statement

### Known member contributions (from git history):

| GitHub | Student ID | Branch / Work Area |
|--------|-----------|-------------------|
| CQ9927 | 231225775 | Project bootstrap, servlet skeleton, auth filter, matching/MO enhancement, release |
| zzzskl | 231226772 | TA servlets (profile, CV, jobs), model classes, repositories, yl-test branch |
| yunmengdd | 231225764 | MO module (dashboard, post job, select), deyu/MOupdate branches |
| SpPt2FeMa | 231225812 | TA matching features, SpPt2FeMa-s1-ta-matching branch |
| BUCOD | 231225904 | Admin workload dashboard, admin users/applications, docs/readme |

---

## 5. Other Items

- [ ] Update `README.md` if the build/run instructions change
- [ ] Verify all 5 JSON data files load without errors on a fresh checkout
- [ ] Do a full end-to-end smoke test: TA registers → fills profile → uploads CV → applies → MO reviews → selects → TA checks status → Admin sees workload
- [ ] Record demonstration video (≤ 10 minutes, all functions, English narration, no post-editing)
- [ ] Package final ZIP: `Software_group48.zip` with source code, tests, JavaDocs, user manual, README
