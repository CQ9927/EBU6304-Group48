# TA Recruitment System — User Manual

**Group 48 · EBU6304 Software Engineering Group Project · May 2026**

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Demo Accounts](#2-demo-accounts)
3. [TA Guide](#3-ta-guide)
   - [3.1 Dashboard](#31-ta-dashboard)
   - [3.2 Profile](#32-ta-profile)
   - [3.3 CV Management](#33-ta-cv-management)
   - [3.4 Browse & Apply for Jobs](#34-browse--apply-for-jobs)
   - [3.5 Application Status](#35-application-status)
   - [3.6 Invitations](#36-invitations)
4. [MO Guide](#4-mo-guide)
   - [4.1 Dashboard & Post a Job](#41-mo-dashboard--post-a-job)
   - [4.2 Review Applications](#42-review-applications)
   - [4.3 Invite TAs](#43-invite-tas)
5. [Admin Guide](#5-admin-guide)
   - [5.1 Admin Console (Workload)](#51-admin-console-workload)
   - [5.2 TA Workload Monitor](#52-ta-workload-monitor)
   - [5.3 User Management](#53-user-management)
   - [5.4 Application Management](#54-application-management)
6. [Auth Pages](#6-auth-pages)
7. [Error Pages](#7-error-pages)
8. [Tabi AI Assistant](#8-tabi-ai-assistant)
9. [End-to-End Walkthrough](#9-end-to-end-walkthrough)

---

## 1. Getting Started

### Prerequisites

- Java 17+
- Apache Maven 3.9+
- Apache Tomcat 9.x (or use embedded Jetty for local development)

### Quick Start (Jetty)

```bash
cd EBU6304-Group48
mvn jetty:run
```

Open **http://localhost:8080/ta-recruitment/** in your browser. If port 8080 is in use:

```bash
mvn jetty:run -Djetty.port=9080
```

### Production Deploy (Tomcat)

```bash
mvn package
# Copy target/ta-recruitment.war to Tomcat webapps/
# Start Tomcat
# Open http://localhost:8080/ta-recruitment/
```

### Data Storage

All data is stored as JSON files under `${user.home}/ebu6304-group48-data/`. On first startup the demo dataset is automatically seeded from the bundled WAR resources.

---

## 2. Demo Accounts

| Username | Role | Password |
|----------|------|----------|
| `ta_demo` | TA | `demo123` |
| `ta_li` | TA | `demo123` |
| `mo_demo` | MO | `demo123` |
| `admin_demo` | ADMIN | `demo123` |

You can also register new accounts via the **Register** page.

---

## 3. TA Guide

### 3.1 TA Dashboard

After logging in as a TA, you land on the dashboard. It shows:

- **Stats bar**: number of open jobs, your applications, in-review count, pending invitations
- **Invitation alert**: a prominent banner if you have pending MO invitations
- **Profile completeness card**: shown if your profile is under 100%
- **Latest applications**: recent 5 applications with status badges and match scores
- **Best Matches for You**: open jobs ranked by match score, with matched/missing skills

![TA Dashboard](screenshots/03-ta-dashboard.png)

### 3.2 TA Profile

Navigate to **Profile** in the top nav. Fill in:

1. **Full Name** (required)
2. **Email Address** (required)
3. **Major/Program** (required)
4. **Skills** — type comma-separated values (e.g. "Java, Python, Teaching"). Suggested skills from open jobs appear as clickable chips.
5. **Availability** — check all time slots you are available (20 options matching MO schedule choices).
6. **Additional Notes** (optional)

Click **Save profile**. A success message appears at the top.

![TA Profile](screenshots/04-ta-profile.png)

### 3.3 TA CV Management

Navigate to **CV** in the top nav.

- **Upload CV**: Click "Choose File", select a `.txt` file, then click "Upload CV". The system extracts skills via AI analysis.
- **Set as Active**: If you have multiple CVs, click "Set as Active" on the one you want to use for applications.
- **Delete CV**: Click "Delete" to remove an old CV.

![TA CV Management](screenshots/05-ta-cv.png)

### 3.4 Browse & Apply for Jobs

Navigate to **Jobs** in the top nav.

- **Filters**: Use the dropdowns and text input to filter by Job Type, Semester, or Required Skill.
- **Job table**: Each row shows title, type, schedule, required skills (matched ✓ / missing ✗), match score percentage, status, and an **Apply** button.
- **AI Analysis**: Click "✨ AI Analysis" or "🔍 Skill Gap" for a detailed match explanation.
- **Apply**: Click the green "Apply" button on an OPEN job. You must have a complete profile and CV first.

![TA Jobs](screenshots/06-ta-jobs.png)

### 3.5 Application Status

Navigate to **Status** in the top nav.

Shows all your submitted applications with:
- Job title, application ID, applied date
- Current status: SUBMITTED → UNDER REVIEW → SELECTED / REJECTED
- Match scores and skill breakdowns

![TA Application Status](screenshots/07-ta-status.png)

### 3.6 Invitations

Navigate to **Invitations** in the top nav.

Shows:
- **Pending invitations**: MO-sent invitations with job details, match scores, and Accept/Decline buttons
- **History**: Previously accepted or declined invitations

Click **Accept** to automatically create an application. Click **Decline** to reject.

![TA Invitations](screenshots/08-ta-invitations.png)

---

## 4. MO Guide

### 4.1 MO Dashboard & Post a Job

After logging in as an MO, you land on the dashboard showing:
- **Stats**: total posts, open jobs, pending applications
- **Your Positions**: card list of all jobs with applicant counts and status
- **Post New Job** button (opens a modal)

#### Post a New Job

Click **"+ Post New Job"** to open the modal. Fill in:

1. **Title** (required) — e.g. "CS101 Tutorial Assistant"
2. **Type** (required) — MODULE or INVIGILATION
3. **Semester** (required) — e.g. 2026 Spring
4. **Schedule** (required) — 20 time slot options
5. **Capacity** (required) — number of TAs needed
6. **Required Skills** — comma-separated, e.g. "Java, Teaching"
7. **Application Deadline** — optional datetime

Click **"Create job"** to post.

![MO Dashboard with Post Job Modal](screenshots/09-mo-dashboard.png)

### 4.2 Review Applications

Navigate to **Review Applications** in the top nav.

- **Filter by job**: Select a job from the dropdown
- **Capacity bar**: Shows capacity, selected count, and remaining slots
- **Application table**: Each row shows applicant details (name, major, email, AI CV summary), match breakdown (skills/schedule/major/profile scores), missing skills, and action buttons
- **Batch operations**: Check multiple applicants → sticky bar appears at the bottom → "Mark as Under Review" or "Reject Selected"

Status progression: **SUBMITTED → UNDER REVIEW → SELECTED / REJECTED**

![MO Review Applications](screenshots/10-mo-select.png)

### 4.3 Invite TAs

Navigate to **Invite TAs** in the top nav.

- Select a job, then browse all TAs ranked by match score
- Click **"Send Invitation"** on promising candidates who haven't yet applied
- TAs receive the invitation on their dashboard and can Accept/Decline

![MO Invite TAs](screenshots/11-mo-invite.png)

---

## 5. Admin Guide

### 5.1 Admin Console (Workload)

After logging in as ADMIN, you land on the workload dashboard.

**Left column:**
- **4 metric cards**: Total Users, Jobs, Applications, Alerts
- **Alerts & Warnings**: Over-capacity jobs, open jobs with no selections, rule-based conflict hints
- **Per-job Workload Table**: Job ID, status, capacity, application counts by stage, risk

**Right column:**
- **AI Workload Insights**: Click "✨ Analyze with AI" for AI-generated workload recommendations

Toggle the workload table visibility with the **"Hide Details"** button.

![Admin Console](screenshots/12-admin-workload.png)

### 5.2 TA Workload Monitor

The TA Workload Monitor shows workload from the TA perspective — hours, assignments, schedule conflicts, and risk flags.

**Features:**

- **4 stat cards**: Total TAs, Selected Assignments, TAs over threshold, Schedule Conflicts
- **Configurable threshold**: Set max weekly hours (default 10 hrs); TAs exceeding it get a **High load** flag
- **TA workload table**: Each row shows TA name, major, Selected/Pending/Rejected counts, estimated weekly hours, assigned jobs, and risk flags
- **Risk flags**: High load (yellow), Banned (red), Schedule clash (red), No CV (yellow)
- **Detail expand**: Click **Detail** on any TA row to see full info — username, email, skills, CV status, and per-job breakdown with schedule, weekly hours, and semester

![TA Workload Monitor](admin-workload-monitor.png)

### 5.3 User Management

Navigate to **Users** in the top nav.

- **Role stats**: Total / TA / MO / Banned counts
- **User table**: User ID, username, role (color-coded badge), banned status, ban reason, appeal info
- **Ban/Unban**: Fill in ban reason and click Ban. Click Unban to reverse.
- **Reset Password**: Enter new password and click "Reset PW"

![Admin User Management](screenshots/13-admin-users.png)

### 5.4 Application Management

Navigate to **Applications** in the top nav.

- **Filter by status**: Dropdown to filter by SUBMITTED / UNDER REVIEW / SELECTED / REJECTED
- **Result count**: "Showing X applications with status Y"
- **Application table**: App ID, job, applicant, match score (color-coded), status badge, admin revoke flag
- **Revoke**: Click "Revoke" on SUBMITTED or UNDER REVIEW applications (SELECTED/REJECTED are finalised)

![Admin Applications](screenshots/14-admin-applications.png)

---

## 6. Auth Pages

### Login

Navigate to `/ta-recruitment/login`.

Enter username and password. Successful login redirects to your role-appropriate landing page.

![Login Page](screenshots/01-login.png)

### Register

Navigate to `/ta-recruitment/register`.

Fill in username, password (min 8 chars), confirm password, and select role (TA or MO). Admin accounts cannot be self-registered.

![Register Page](screenshots/02-register.png)

---

## 7. Error Pages

### 404 — Not Found

When navigating to a non-existent page.

![404 Error Page](screenshots/15-404.png)

### 500 — Internal Server Error

When an unexpected server error occurs.

> *To capture a 500 error screenshot, trigger an internal server error in the application.*

---

## 8. Tabi AI Assistant

Tabi (🦉) is an AI-powered chat assistant available on all authenticated pages. She appears as a floating owl button in the bottom-right corner.

### Features

- **Job recommendations**: Ask "What jobs match my skills?" to see open positions ranked by match score
- **Application status**: Ask "How are my applications doing?" to check statuses
- **Profile help**: Tabi detects missing profile fields and suggests what to fill in
- **MO applicant overview**: MOs can ask about applicant counts and status
- **Admin alerts**: Admins can ask about over-capacity jobs or unstaffed positions

### How to Use

1. Click the floating **🦉 button** on any page
2. The chat panel opens — type a question and press Enter
3. Tabi responds with text and interactive cards (job listings, status summaries, etc.)
4. Click links in cards to jump directly to relevant pages
5. Click the **− button** or anywhere outside to close

You can **drag** the button or the chat panel header to reposition them.

![Tabi AI Assistant](screenshots/16-tabi-chat.png)

---

## 9. End-to-End Walkthrough

Follow these steps to verify the complete workflow:

### Step-by-step

1. **Register as TA**: Go to `/register`, create a new TA account
2. **Complete profile**: Navigate to Profile, fill in all fields, add skills and availability
3. **Upload CV**: Go to CV, upload a `.txt` CV file
4. **Logout**, then login as **`mo_demo`**
5. **Post a job**: Click "+ Post New Job", fill the form, submit
6. **Logout**, then login as your TA account
7. **Browse jobs**: Go to Jobs, verify the new job appears with a match score
8. **Apply**: Click Apply on the job
9. **Check status**: Go to Status, verify application appears as SUBMITTED
10. **Logout**, login as **`mo_demo`**
11. **Review**: Go to Review Applications, find the TA's application
12. **Progress the application**: Under Review → Select
13. **Logout**, login as your TA account
14. **Verify**: Go to Status, verify status updated to SELECTED
15. **Logout**, login as **`admin_demo`**
16. **Check workload**: Verify the job appears in the workload table with correct counts

![Final Status Page](screenshots/07-ta-status.png)

---

*End of User Manual · Group 48 · EBU6304 · May 2026*
