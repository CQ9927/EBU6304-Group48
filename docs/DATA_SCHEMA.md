# Data Schema (Text-File Persistence)

## File Overview
- `data/users.json`: account and role info
- `data/profiles.json`: TA profile data
- `data/jobs.json`: job postings from MO
- `data/applications.json`: TA applications and status
- `data/selection.json`: MO selection decisions (optional audit log)

## Common Rules
- All IDs are unique strings (UUID or prefixed incremental IDs).
- All timestamps use ISO 8601 format.
- Enumerations must use uppercase constants.

## users.json
```json
[
  {
    "userId": "U001",
    "username": "CQ9927",
    "passwordHash": "sha256:...",
    "role": "TA",
    "createdAt": "2026-03-27T10:00:00Z"
  }
]
```

Role enum:
- `TA`
- `MO`
- `ADMIN`

## profiles.json
```json
[
  {
    "profileId": "P001",
    "userId": "U001",
    "name": "Cui Hongqing",
    "email": "ta48@school.edu",
    "major": "Software Engineering",
    "skills": ["Java", "Python"],
    "availability": ["TUE_18_20", "THU_18_20"],
    "notes": "Sample note",
    "updatedAt": "2026-03-27T10:30:00Z"
  }
]
```

## jobs.json
```json
[
  {
    "jobId": "J001",
    "title": "CS101 Tutorial Assistant",
    "type": "MODULE",
    "semester": "2026_SPRING",
    "schedule": "WED_18_20",
    "capacity": 2,
    "requiredSkills": ["Python", "Teaching"],
    "postedByUserId": "U010",
    "status": "OPEN",
    "createdAt": "2026-03-27T11:00:00Z"
  }
]
```

Job status enum:
- `OPEN`
- `CLOSED`

Job type enum:
- `MODULE`
- `INVIGILATION`

## applications.json
```json
[
  {
    "applicationId": "A001",
    "jobId": "J001",
    "applicantUserId": "U001",
    "matchScore": 84,
    "missingSkills": ["Teaching"],
    "status": "SUBMITTED",
    "note": "Sample note",
    "createdAt": "2026-03-27T12:00:00Z",
    "updatedAt": "2026-03-27T12:00:00Z"
  }
]
```

Application status enum:
- `SUBMITTED`
- `UNDER_REVIEW`
- `SELECTED`
- `REJECTED`

## selection.json (optional)
```json
[
  {
    "selectionId": "S001",
    "applicationId": "A001",
    "jobId": "J001",
    "reviewerUserId": "U010",
    "decision": "SELECTED",
    "decisionAt": "2026-03-27T13:00:00Z"
  }
]
```

Decision enum:
- `SELECTED`
- `REJECTED`
