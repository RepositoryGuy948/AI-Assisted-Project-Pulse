# Project Pulse — Full Implementation Specification

> **Purpose:** This document is a single, self-contained specification for building the **Project Pulse** web application. Feed this to Claude Code (or the Claude API) along with the reference repository at `https://github.com/Washingtonwei/project-pulse` to generate the full project.

---

## 1. Project Overview

**Project Pulse** is a web application for managing senior design / capstone team projects. It replaces a manual workflow (Google Sheets for weekly activity reports, Excel uploads for peer evaluations) with an integrated platform.

### Core Capabilities
1. **Weekly Activity Reports (WAR)** — students log activities each week
2. **Peer Evaluations** — students rate teammates weekly using a configurable rubric
3. **Instructor Dashboard** — instructors generate aggregated reports for grading
4. **Admin Management** — admin sets up sections, teams, rubrics, active weeks, and manages users

### Actors
| Actor | Description |
|-------|-------------|
| **Admin** | System administrator. Sets up sections, teams, rubrics, active weeks. Manages students and instructors. There is typically one admin. |
| **Instructor** | Faculty member or client. Supervises one or more teams. Views reports and peer evaluations. |
| **Student** | Senior design student. Submits WARs and peer evaluations weekly. Views own evaluation feedback. |

---

## 2. Technical Stack (MANDATORY)

| Layer | Technology | Notes |
|-------|-----------|-------|
| **Backend** | Spring Boot 4.x | REST API, Maven build |
| **Frontend** | Vue.js + Vuetify | Do NOT use ElementPlus |
| **Database** | MySQL (or PostgreSQL) | Persistent; no in-memory/local-only |
| **Local Dev** | Docker Compose | MySQL + Mailpit (for emails) |
| **Deployment** | Microsoft Azure | Must be publicly accessible |

### Reference Repository Structure
```
project-pulse/
├── backend/          # Spring Boot (Maven)
│   └── src/main/java/...
│       ├── controller/    # REST controllers
│       ├── service/       # Business logic
│       ├── repository/    # JPA repositories
│       ├── model/         # JPA entities
│       └── dto/           # Data Transfer Objects
├── frontend/         # Vue.js (Vite)
│   └── src/
│       ├── views/         # Page-level components
│       ├── components/    # Reusable UI components
│       ├── stores/        # Pinia state stores
│       ├── router/        # Vue Router config
│       └── api/           # Axios API service layer
├── docker/           # Grafana, Prometheus configs (optional)
├── Dockerfile        # Spring Boot containerization
└── docker-compose.yml # MySQL, Mailpit, etc.
```

### Default Dev Credentials
- Student: `j.smith@abc.edu` / `123456`
- Instructor: `b.wei@abc.edu` / `123456`

### Backend Architecture Pattern
Follow the standard Spring Boot layered pattern:
- **Controller** → accepts HTTP requests, returns ResponseEntity
- **Service** → business logic, validation, orchestration
- **Repository** → JPA/Spring Data interface for DB access
- **Entity/Model** → JPA annotated classes mapping to DB tables
- **DTO** → data transfer objects for API request/response payloads

---

## 3. Domain Glossary (Key Concepts)

### Senior Design Section
A particular offering of the two-course sequence (fall + spring). Format: `YYYY-YYYY` (e.g., "2024-2025"). Has a start date, end date, active weeks, teams, students, instructors, and a rubric.

### Senior Design Team
A group of 5-6 students collaborating on a capstone project. Has a name, description, and website URL. Belongs to one section.

### Active Week
A week (Monday–Sunday) during which students MUST submit WARs and peer evaluations. The admin configures which weeks are active vs. inactive (e.g., winter break is inactive). A week is identified by its Monday date.

### Weekly Activity Report (WAR)
A structured report of what a student did in a given week. Each WAR contains multiple **activities**. Each activity has:
- **Category**: DEVELOPMENT, TESTING, BUGFIX, COMMUNICATION, DOCUMENTATION, DESIGN, PLANNING, LEARNING, DEPLOYMENT, SUPPORT, MISCELLANEOUS
- **Description** (text)
- **Planned hours** (number)
- **Actual hours** (number)
- **Status**: In Progress, Under Testing, Done

### Peer Evaluation
A weekly assessment where each student rates ALL teammates (including self). Based on a **Rubric**. Includes:
- Scores for each rubric criterion (integer, e.g., 1-10)
- **Public comments** — visible to the evaluatee
- **Private comments** — visible to the instructor only

### Rubric
A scoring guide with a name and multiple **criteria**. Each criterion has: name, description, max score (positive, can be decimal). Example criteria: Quality of Work (1-10), Productivity (1-10), Initiative (1-10), Courtesy (1-10), Open-mindedness (1-10), Engagement in Meetings (1-10).

---

## 4. Business Rules

| ID | Rule |
|----|------|
| **BR-1** | Every team must have at least one instructor. Commonly two (TCU instructor + client). An instructor can be assigned to multiple teams. |
| **BR-2** | Active weeks: Fall — weeks 5-15. Spring — weeks 1-15. Winter holidays are inactive. Students submit peer evaluations only during active weeks. WARs can be submitted outside active weeks. |
| **BR-3** | Peer evaluation cannot be edited once completed. (TODO — current implementation may allow edits after submission) |
| **BR-4** | A student can only submit a peer evaluation for the PREVIOUS week. They have one week to complete it. Missed evaluations cannot be made up. |
| **BR-5** | Students can only see their own rubric criterion scores, public comments, and overall grade. They NEVER see private comments or evaluator identity. |

---

## 5. Database Schema (Inferred from Use Cases)

Design the following entities. Use JPA annotations. The reference repo can guide exact column names.

### Core Entities
```
Section (id, name [format: YYYY-YYYY], startDate, endDate, rubric_id)
Team (id, name [unique within section], description, websiteUrl, section_id)
ActiveWeek (id, startDate [Monday], endDate [Sunday], section_id, isActive)

User (id, email [unique], password, firstName, middleInitial, lastName, role [ADMIN/INSTRUCTOR/STUDENT], enabled)
  — Or separate Student/Instructor tables linked to User

StudentTeamAssignment (student_id, team_id)
InstructorTeamAssignment (instructor_id, team_id)

Rubric (id, name [unique])
Criterion (id, rubric_id, name, description, maxScore)

WeeklyActivityReport (id, student_id, activeWeek_id)
Activity (id, war_id, category, description, plannedHours, actualHours, status)

PeerEvaluation (id, evaluator_id, evaluatee_id, activeWeek_id, team_id)
PeerEvaluationScore (id, peerEvaluation_id, criterion_id, score [integer])
PeerEvaluationComment (id, peerEvaluation_id, publicComment, privateComment)

InvitationToken (id, email, token [unique], section_id, role, used, createdAt)
```

---

## 6. Complete Use Case Specifications (34 total)

### Admin Use Cases (UC-1 through UC-24)

#### UC-1: Create a Rubric
- **Actor:** Admin
- **Flow:** Admin enters rubric name (must be unique) + multiple criteria (name, description, maxScore). System validates, confirms, saves.
- **Details:** maxScore must be positive, can be decimal. Admin can cancel anytime.

#### UC-2: Find Senior Design Sections
- **Actor:** Admin
- **Search by:** section name (optional)
- **Display:** section name, team names. Sort: section name descending, team names ascending.

#### UC-3: View a Senior Design Section
- **Actor:** Admin
- **Display:** section name, start/end dates, teams with members and instructors, unassigned instructors, unassigned students, rubric used.

#### UC-4: Create a Senior Design Section
- **Actor:** Admin
- **Details:** section name (unique), start date, end date, rubric to use. Duplicate detection by name.

#### UC-5: Edit a Senior Design Section
- **Actor:** Admin
- **Editable:** section name, start/end dates, rubric. Section name must remain unique.

#### UC-6: Set Up Active Weeks
- **Actor:** Admin
- **Flow:** System displays all weeks of the section (derived from start/end dates). Admin marks inactive weeks. System saves. References BR-2.

#### UC-7: Find Senior Design Teams
- **Actor:** Admin, Instructor
- **Search by:** section ID, section name, team name, instructor (all optional)
- **Display:** team name, description, website URL, members, instructors. Sort: section name desc, then team name asc.

#### UC-8: View a Senior Design Team
- **Actor:** Admin, Instructor
- **Display:** team name, description, website URL, members, instructors.

#### UC-9: Create a Senior Design Team
- **Actor:** Admin
- **Details:** team name (unique within section), description, website URL. Duplicate detection.

#### UC-10: Edit a Senior Design Team
- **Actor:** Admin
- **Editable:** team name, description, website URL. Name must remain unique.

#### UC-11: Invite Students to Join a Section
- **Actor:** Admin → **Secondary:** Student
- **Flow:** Admin enters semicolon-separated emails. System validates format, displays count, shows default email message (customizable). Sends invitation with unique registration link per student.
- **Email format:** `john.doe@tcu.edu; f.smith@tcu.edu` (semicolons, spaces ignored)
- **Triggers:** UC-25 (student sets up account after receiving email)

#### UC-12: Assign Students to Teams
- **Actor:** Admin
- **Flow:** System shows list of teams + list of students. Admin drags/assigns students to teams. Confirms. System notifies students.
- **Preconditions:** Teams exist, students have registered.

#### UC-13: Remove Student from Team
- **Actor:** Admin
- **Flow:** View team → remove student → confirm → notify student.
- **Priority:** Low / Rare

#### UC-14: Delete a Senior Design Team
- **Actor:** Admin
- **Cascade:** Removes students/instructors from team first. Permanently deletes team + associated WARs and peer evaluations. Notifies affected users.
- **Priority:** Low / Rare

#### UC-15: Find Students
- **Actor:** Admin, Instructor
- **Search by:** first name, last name, email, section name, team name, section ID, team ID (all optional)
- **Display:** first name, last name, team name. Sort: section name desc, then last name asc.

#### UC-16: View a Student
- **Actor:** Admin, Instructor
- **Display:** first name, last name, section name, team name, peer evaluations, WARs.

#### UC-17: Delete a Student
- **Actor:** Admin
- **Cascade:** Permanently deletes student + associated WARs and peer evaluations. Physical delete.
- **Priority:** Low / Rare

#### UC-18: Invite Instructors to Register
- **Actor:** Admin → **Secondary:** Instructor
- **Flow:** Same pattern as UC-11 but for instructors. Sends email with unique registration link.
- **Triggers:** UC-30 (instructor sets up account)

#### UC-19: Assign Instructors to Teams
- **Actor:** Admin
- **Flow:** Same pattern as UC-12 but for instructors. Multiple instructors per team allowed. Notifies instructors. References BR-1.

#### UC-20: Remove Instructor from Team
- **Actor:** Admin
- **Flow:** View team → remove instructor → confirm → notify. References BR-1.
- **Priority:** Low / Rare

#### UC-21: Find Instructors
- **Actor:** Admin
- **Search by:** first name, last name, email (all optional)
- **Display:** first name, last name. Sort: last name ascending.

#### UC-22: View an Instructor
- **Actor:** Admin
- **Display:** first name, last name, section, teams.

#### UC-23: Deactivate an Instructor
- **Actor:** Admin
- **Flow:** View instructor → deactivate with reason → confirm. Instructor loses system access but data is preserved. Can be reactivated later.
- **Priority:** Low / Rare

#### UC-24: Reactivate an Instructor
- **Actor:** Admin
- **Flow:** View deactivated instructor → reactivate → confirm → notify instructor.
- **Priority:** Low / Rare

---

### Student Use Cases (UC-25 through UC-29)

#### UC-25: Set Up Student Account
- **Actor:** Student
- **Trigger:** Student clicks unique registration link from invitation email
- **Details:** first name, last name, email, password. If already registered, redirect to login.

#### UC-26: Edit Student Account
- **Actor:** Student
- **Editable:** first name, last name, email.

#### UC-27: Manage WAR Activities (CRUD)
- **Actor:** Student
- **Flow:** Select active week (cannot select future week). View existing activities. Choose: Add / Edit / Delete activity.
- **Activity fields:** category (enum), description, planned hours, actual hours, status (enum)
- **Note:** Students CAN submit WARs outside active weeks (BR-2).

#### UC-28: Submit Peer Evaluation
- **Actor:** Student
- **Flow:** Student evaluates EVERY team member (self included) for the PREVIOUS week. Each evaluation: scores per criterion (integers), public comment, private comment. System validates. Peer evaluations can be edited after submission.
- **Rules:** BR-3, BR-4
- **Details:** Every teammate MUST be evaluated. Scores MUST be integers.

#### UC-29: View Own Peer Evaluation Report
- **Actor:** Student
- **Parameters:** Select active week (default: previous week)
- **Display:** For each criterion, show average score across all evaluators. Show public comments only. Show overall grade.
- **Algorithm:** For each criterion, average the scores from all evaluators. Overall grade = sum of total scores from each evaluator, averaged.
- **Security:** Student NEVER sees private comments or evaluator identity (BR-5).

---

### Instructor Use Cases (UC-30 through UC-34)

#### UC-30: Set Up Instructor Account
- **Actor:** Instructor
- **Trigger:** Instructor clicks unique registration link from invitation email
- **Details:** first name, middle initial, last name, password, confirm password.

#### UC-31: Generate Peer Evaluation Report (Entire Section)
- **Actor:** Instructor
- **Parameters:** Select active week (default: previous week)
- **Display columns:** Student name, grade, commented by, public comments, private comments. Sort by last name ascending.
- **Algorithm:** For each student: collect all peer evaluations received that week. For each evaluation, compute total score (sum of criterion scores). Average the totals. Example: John gets evals with totals 58 and 50 → grade = (58+50)/2 = 54.
- **Show:** Who did NOT submit evaluations. Instructor can drill into details (scores by evaluator per criterion).

#### UC-32: Generate WAR Report (Team)
- **Actor:** Instructor, Student
- **Parameters:** Select active week (default: previous week)
- **Display columns:** Student name, activity category, planned activity, description, planned hours, actual hours, status. Sort by last name ascending.
- **Show:** Who did NOT submit WAR for that week.

#### UC-33: Generate Peer Evaluation Report (Individual Student)
- **Actor:** Instructor
- **Parameters:** Select start and end active weeks (period)
- **Display columns:** Week, grade, commented by, public comments, private comments. Sort by week chronologically.
- **Uses same algorithm as UC-31.**

#### UC-34: Generate WAR Report (Individual Student)
- **Actor:** Instructor
- **Parameters:** Select start and end active weeks (period)
- **Display:** Activities grouped by active week. Columns: category, planned activity, description, planned hours, actual hours, status. Sort by week chronologically.

---

## 7. API Design Guidelines

Follow RESTful conventions. Suggested endpoint patterns:

```
# Sections
GET    /api/sections              # List/search sections
POST   /api/sections              # Create section
GET    /api/sections/{id}         # View section details
PUT    /api/sections/{id}         # Edit section
POST   /api/sections/{id}/active-weeks  # Set up active weeks

# Teams
GET    /api/teams                 # List/search teams
POST   /api/sections/{sectionId}/teams  # Create team in section
GET    /api/teams/{id}            # View team
PUT    /api/teams/{id}            # Edit team
DELETE /api/teams/{id}            # Delete team

# Users / Invitations
POST   /api/sections/{id}/invite-students    # Send student invitations
POST   /api/sections/{id}/invite-instructors # Send instructor invitations
POST   /api/register/student       # Student registration (with token)
POST   /api/register/instructor    # Instructor registration (with token)

# Student/Instructor assignments
POST   /api/teams/{id}/students              # Assign students to team
DELETE /api/teams/{id}/students/{studentId}   # Remove student
POST   /api/teams/{id}/instructors           # Assign instructors to team
DELETE /api/teams/{id}/instructors/{instructorId} # Remove instructor

# Students & Instructors CRUD
GET    /api/students              # List/search students
GET    /api/students/{id}         # View student
DELETE /api/students/{id}         # Delete student (cascade)
PUT    /api/students/{id}         # Edit student account
GET    /api/instructors           # List/search instructors
GET    /api/instructors/{id}      # View instructor
PUT    /api/instructors/{id}/deactivate   # Deactivate
PUT    /api/instructors/{id}/reactivate   # Reactivate

# Rubrics
POST   /api/rubrics               # Create rubric
GET    /api/rubrics               # List rubrics
GET    /api/rubrics/{id}          # View rubric

# WARs
GET    /api/students/{id}/wars                    # Get student's WARs
POST   /api/students/{id}/wars/{weekId}/activities # Add activity
PUT    /api/activities/{id}                        # Edit activity
DELETE /api/activities/{id}                        # Delete activity

# Peer Evaluations
POST   /api/students/{id}/peer-evaluations  # Submit peer evaluation
GET    /api/students/{id}/peer-evaluation-report  # Student views own report

# Reports (Instructor)
GET    /api/sections/{id}/peer-evaluation-report?week=...           # UC-31
GET    /api/teams/{id}/war-report?week=...                          # UC-32
GET    /api/students/{id}/peer-evaluation-report?startWeek=...&endWeek=... # UC-33
GET    /api/students/{id}/war-report?startWeek=...&endWeek=...      # UC-34
```

---

## 8. Frontend Page Structure

```
/ (Login page)
/register/student?token=...
/register/instructor?token=...

# Admin views
/admin/sections
/admin/sections/:id
/admin/sections/:id/active-weeks
/admin/teams
/admin/teams/:id
/admin/students
/admin/students/:id
/admin/instructors
/admin/instructors/:id
/admin/rubrics
/admin/rubrics/new
/admin/invite/students
/admin/invite/instructors
/admin/assign/students
/admin/assign/instructors

# Student views
/student/dashboard
/student/account
/student/war/:weekId
/student/peer-evaluation
/student/my-evaluation-report

# Instructor views
/instructor/dashboard
/instructor/teams
/instructor/teams/:id
/instructor/students
/instructor/students/:id
/instructor/reports/peer-evaluation/section
/instructor/reports/peer-evaluation/student/:id
/instructor/reports/war/team/:id
/instructor/reports/war/student/:id
```

---

## 9. Implementation Notes

### Email System
- Use **Mailpit** for local development (SMTP on port 1025, web UI on port 8025)
- Invitation emails must contain unique, one-time-use registration links
- Email template: "Welcome to The Peer Evaluation Tool - Complete Your Registration"

### Authentication
- Implement role-based access control (Admin, Instructor, Student)
- Use Spring Security with JWT or session-based auth
- Each role has different accessible endpoints and UI views

### Peer Evaluation Grade Algorithm
```
For a student in a given week:
1. Collect all peer evaluations RECEIVED by that student
2. For each evaluation, compute total = sum of all criterion scores
3. Grade = average of all totals

Example:
  Tim gives John: 10+9+10+9+10+10 = 58
  Lily gives John: 5+5+10+10+10+10 = 50
  John's grade = (58 + 50) / 2 = 54
```

### Docker Compose Setup
```yaml
services:
  mysql:
    image: mysql:8
    ports: ["3306:3306"]
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: project_pulse
  mailpit:
    image: axllent/mailpit
    ports: ["1025:1025", "8025:8025"]
```

---

## 10. Team Work Division (3 Members)

### Member 1 — Admin Setup, Configuration & Section Reports (13 UCs, 22 difficulty points)
UC-1, UC-2, UC-3, UC-4, UC-5, UC-6, UC-18, UC-21, UC-22, UC-23, UC-24, UC-30, **UC-31**

**Domain:** Sections, rubrics, active weeks, instructor lifecycle (invite, register, find, view, deactivate, reactivate), **section-level peer evaluation report**. Member 1 implements the shared peer eval grading algorithm (used by UC-31, UC-29, UC-33).

### Member 2 — Teams, Students & WAR System (12 UCs, 21 difficulty points)
UC-7, UC-8, UC-9, UC-10, UC-11, UC-15, UC-16, UC-25, UC-26, UC-27, UC-32, **UC-34**

**Domain:** Team CRUD, student lifecycle (invite, register, find, view), WAR activity management, **team + individual WAR reports**

### Member 3 — Assignments, Peer Evaluations & Student Reports (9 UCs, 18 difficulty points)
UC-12, UC-13, **UC-14**, UC-17, UC-19, UC-20, UC-28, UC-29, UC-33

**Domain:** Student/instructor team assignments, team deletion (cascade), peer evaluation submission, **student-facing peer eval report**, instructor-level student peer eval report. Reuses the grading algorithm implemented by Member 1.

### Shared Work (All Members)
- Authentication/login system
- Database schema design
- Project scaffolding (Spring Boot + Vue.js)
- Docker Compose configuration
- Azure deployment

---

## 11. Getting Started Checklist

1. **Clone the reference repo** `git clone https://github.com/Washingtonwei/project-pulse.git` — study the code, run it locally
2. **Design the database schema** based on Section 5 above
3. **Scaffold your own Spring Boot + Vue.js project** mirroring the reference structure
4. **Set up Docker Compose** for MySQL + Mailpit
5. **Implement use cases** per the team division in Section 10
6. **Log all AI interactions** (required deliverable — 3-4 high-quality examples)
7. **Deploy to Azure** with persistent MySQL
8. **Prepare demo** — every member must be able to explain any part of the code
