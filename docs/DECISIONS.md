# Architecture Decision Log (ADR-lite)

## Template
- Date:
- Decision:
- Reason:
- Impact:

---

## 2026-03-27 - Use Servlet/JSP instead of standalone Java app
- Decision: Build a Servlet/JSP web application.
- Reason: Fits handout constraints and supports role-based web workflow demonstration.
- Impact: Require Tomcat runtime, WAR packaging, and route-based module split.

## 2026-03-27 - Use text files instead of database
- Decision: Persist data in JSON/CSV/TXT files only.
- Reason: Mandatory handout requirement.
- Impact: Need robust file I/O handling and strict schema documentation.

## 2026-03-27 - Enforce PR-based collaboration
- Decision: Protect `main` and require approval before merge.
- Reason: Improve collaboration quality and contribution visibility.
- Impact: Every task must be done in feature branches with PR review.

## 2026-03-28 - Shared stylesheet and English-only UI
- Decision: Add `src/main/webapp/css/app.css` (design tokens, header, cards, tables, forms) and link it from all JSPs; keep user-facing copy in English.
- Reason: Consistent look for demos and coursework report; easier maintenance than per-page inline CSS.
- Impact: New UI changes should prefer extending `app.css` and shared classes; Tomcat must serve `/css/app.css` under the app context.
