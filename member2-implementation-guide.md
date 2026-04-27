# Project Pulse — Member 2 Implementation Guide
# Teams, Students & WAR System

> **How to use this file:** Feed this document to Claude Code as context when starting your implementation.
> Also point Claude Code at the reference repo: `https://github.com/Washingtonwei/project-pulse`
>
> **Your role:** You are Member 2 on a 3-person team. You own the Teams, Students, and WAR (Weekly Activity Report) subsystem. The other members are building their pieces in parallel, so you MUST follow the shared contracts defined below to ensure everything integrates cleanly.

---

## PART A: SHARED FOUNDATION (All Members Must Follow)

> This section defines the contracts that ALL three members must agree on. Do NOT deviate from these entity definitions, package structure, or API conventions — they are the integration boundary.

### A1. Project Structure

```
project-pulse/
├── backend/
│   ├── pom.xml                          # Spring Boot 4.x, Maven
│   └── src/main/java/edu/tcu/cs/projectpulse/
│       ├── ProjectPulseApplication.java
│       ├── system/                       # Shared: auth, config, exceptions, security
│       │   ├── SecurityConfig.java
│       │   ├── JwtProvider.java          # (or session-based auth)
│       │   ├── GlobalExceptionHandler.java
│       │   └── StatusCode.java           # Standard API response codes
│       ├── section/                      # Member 1 owns
│       │   ├── Section.java
│       │   ├── SectionRepository.java
│       │   ├── SectionService.java
│       │   ├── SectionController.java
│       │   └── dto/
│       ├── rubric/                       # Member 1 owns
│       │   ├── Rubric.java
│       │   ├── Criterion.java
│       │   └── ...
│       ├── activeweek/                   # Member 1 owns
│       │   ├── ActiveWeek.java
│       │   └── ...
│       ├── invitation/                   # Shared: M1 for instructors, M2 for students
│       │   ├── InvitationToken.java
│       │   └── ...
│       ├── user/                         # Shared: base user entity
│       │   ├── AppUser.java              # Base user with role (ADMIN/STUDENT/INSTRUCTOR)
│       │   ├── AppUserRepository.java
│       │   └── ...
│       ├── team/                         # >>> YOU (Member 2) own this
│       │   ├── Team.java
│       │   ├── TeamRepository.java
│       │   ├── TeamService.java
│       │   ├── TeamController.java
│       │   └── dto/
│       ├── student/                      # >>> YOU (Member 2) own this
│       │   ├── Student.java              # (or just use AppUser with STUDENT role)
│       │   ├── StudentRepository.java
│       │   ├── StudentService.java
│       │   ├── StudentController.java
│       │   └── dto/
│       ├── war/                          # >>> YOU (Member 2) own this
│       │   ├── WeeklyActivityReport.java
│       │   ├── Activity.java
│       │   ├── WarRepository.java
│       │   ├── ActivityRepository.java
│       │   ├── WarService.java
│       │   ├── WarController.java
│       │   └── dto/
│       ├── peerevaluation/               # Member 3 owns
│       │   ├── PeerEvaluation.java
│       │   ├── PeerEvaluationScore.java
│       │   └── ...
│       └── instructor/                   # Member 1 owns
│           ├── Instructor.java
│           └── ...
├── frontend/
│   ├── package.json                      # Vue.js + Vuetify (NOT ElementPlus)
│   └── src/
│       ├── router/index.js
│       ├── stores/                       # Pinia stores
│       ├── api/                          # Axios service layer
│       │   ├── axiosInstance.js          # Base config with auth headers
│       │   ├── teamApi.js               # >>> YOU
│       │   ├── studentApi.js            # >>> YOU
│       │   ├── warApi.js                # >>> YOU
│       │   ├── sectionApi.js            # Member 1
│       │   └── peerEvalApi.js           # Member 3
│       ├── views/
│       │   ├── admin/                   # Admin pages
│       │   ├── student/                 # Student pages
│       │   └── instructor/              # Instructor pages
│       └── components/                  # Reusable Vuetify components
├── docker-compose.yml
└── Dockerfile
```

### A2. Shared Entity Definitions (JPA)

> These are the contracts. Follow these field names and relationships exactly so all three members' code is compatible.

```java
// === BASE USER (Shared — all members depend on this) ===
@Entity
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;      // BCrypt encoded
    private String firstName;
    private String middleInitial; // nullable, mainly for instructors
    private String lastName;
    @Enumerated(EnumType.STRING)
    private Role role;            // ADMIN, STUDENT, INSTRUCTOR
    private boolean enabled = true;

    // Relationships are managed by role-specific logic
}
// enum Role { ADMIN, STUDENT, INSTRUCTOR }

// === SECTION (Member 1 owns — you READ from this) ===
@Entity
public class Section {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;          // Format: "YYYY-YYYY"
    private LocalDate startDate;
    private LocalDate endDate;
    @ManyToOne
    private Rubric rubric;
    @OneToMany(mappedBy = "section")
    private List<Team> teams;
    @OneToMany(mappedBy = "section")
    private List<ActiveWeek> activeWeeks;
}

// === ACTIVE WEEK (Member 1 owns — you READ from this) ===
@Entity
public class ActiveWeek {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate startDate;  // Always a Monday
    private LocalDate endDate;    // Always a Sunday
    private boolean active = true;
    @ManyToOne
    private Section section;
}

// === TEAM (YOU own this) ===
@Entity
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;          // Unique within section
    private String description;
    private String websiteUrl;
    @ManyToOne
    private Section section;
    @ManyToMany
    private List<AppUser> students;     // Users with STUDENT role
    @ManyToMany
    private List<AppUser> instructors;  // Users with INSTRUCTOR role
}

// === WAR (YOU own this) ===
@Entity
public class WeeklyActivityReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private AppUser student;
    @ManyToOne
    private ActiveWeek activeWeek;
    @OneToMany(mappedBy = "war", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities;
}

@Entity
public class Activity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private WeeklyActivityReport war;
    @Enumerated(EnumType.STRING)
    private ActivityCategory category;
    private String description;
    private Double plannedHours;
    private Double actualHours;
    @Enumerated(EnumType.STRING)
    private ActivityStatus status;
}

// enum ActivityCategory { DEVELOPMENT, TESTING, BUGFIX, COMMUNICATION,
//     DOCUMENTATION, DESIGN, PLANNING, LEARNING, DEPLOYMENT, SUPPORT, MISCELLANEOUS }
// enum ActivityStatus { IN_PROGRESS, UNDER_TESTING, DONE }

// === RUBRIC (Member 1 owns — you DON'T touch this) ===
@Entity
public class Rubric {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    @OneToMany(mappedBy = "rubric", cascade = CascadeType.ALL)
    private List<Criterion> criteria;
}

@Entity
public class Criterion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Double maxScore;
    @ManyToOne
    private Rubric rubric;
}

// === PEER EVALUATION (Member 3 owns — you DON'T touch this) ===
@Entity
public class PeerEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private AppUser evaluator;
    @ManyToOne
    private AppUser evaluatee;
    @ManyToOne
    private ActiveWeek activeWeek;
    private String publicComment;
    private String privateComment;
    @OneToMany(mappedBy = "peerEvaluation", cascade = CascadeType.ALL)
    private List<PeerEvaluationScore> scores;
}

// === INVITATION TOKEN (Shared) ===
@Entity
public class InvitationToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    @Column(unique = true)
    private String token;         // UUID
    @ManyToOne
    private Section section;
    @Enumerated(EnumType.STRING)
    private Role role;            // STUDENT or INSTRUCTOR
    private boolean used = false;
    private LocalDateTime createdAt;
}
```

### A3. Shared API Response Wrapper

All endpoints return a consistent JSON shape:

```java
public class Result {
    private boolean flag;     // true = success, false = error
    private int code;         // StatusCode constant
    private String message;   // Human-readable
    private Object data;      // Payload
}
// StatusCode: SUCCESS=200, INVALID_ARGUMENT=400, UNAUTHORIZED=401,
//             FORBIDDEN=403, NOT_FOUND=404, INTERNAL_SERVER_ERROR=500
```

### A4. Authentication (Shared)

Use Spring Security with JWT (or session-based). All members must:
- Protect endpoints by role: `@PreAuthorize("hasAuthority('ADMIN')")`, etc.
- The login endpoint `POST /api/auth/login` returns a token
- All subsequent requests include `Authorization: Bearer <token>`
- Frontend stores the token and attaches it via Axios interceptor

### A5. Docker Compose (Shared)

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

## PART B: YOUR USE CASES (Member 2 — 12 UCs, 21 pts)

> These are YOUR use cases. Implement the full backend (controller → service → repository) AND frontend (Vue views + Vuetify components) for each.

### B1. Team Management

#### UC-7: Find Senior Design Teams
- **Actors:** Admin, Instructor
- **Endpoint:** `GET /api/teams?sectionId=&sectionName=&teamName=&instructor=`
- **Search by:** section ID, section name, team name, instructor (all optional)
- **Response:** List of `{teamName, description, websiteUrl, members[], instructors[]}`
- **Sort:** Section name descending, then team name ascending
- **Frontend:** `/admin/teams` — Vuetify data table with search/filter fields

#### UC-8: View a Senior Design Team
- **Actors:** Admin, Instructor
- **Endpoint:** `GET /api/teams/{id}`
- **Response:** `{id, name, description, websiteUrl, students[], instructors[]}`
- **Frontend:** `/admin/teams/:id` — detail card with member lists

#### UC-9: Create a Senior Design Team
- **Actor:** Admin
- **Endpoint:** `POST /api/sections/{sectionId}/teams`
- **Request body:** `{name, description, websiteUrl}`
- **Validation:** Team name must be unique within section (duplicate detection)
- **Frontend:** Form dialog on the teams page

#### UC-10: Edit a Senior Design Team
- **Actor:** Admin
- **Endpoint:** `PUT /api/teams/{id}`
- **Editable:** name, description, websiteUrl
- **Validation:** Name must remain unique within section
- **Frontend:** Edit dialog pre-filled with current values

### B2. Student Invitations & Registration

#### UC-11: Invite Students to Join a Section
- **Actor:** Admin → **Secondary:** Student
- **Endpoint:** `POST /api/sections/{sectionId}/invite-students`
- **Request body:** `{emails: "john@tcu.edu; jane@tcu.edu"}`
- **Flow:**
  1. Parse semicolon-separated emails (ignore spaces)
  2. Validate email format for each
  3. Display count + default email message (admin can customize)
  4. On confirm: for each email, create `InvitationToken` with unique UUID
  5. Send email via Mailpit (SMTP port 1025) with registration link: `/register/student?token=<uuid>`
- **Email template:**
  ```
  Subject: Welcome to The Peer Evaluation Tool - Complete Your Registration
  Hello,
  [Admin Name] has invited you to join The Peer Evaluation Tool.
  To complete your registration, please use the link below:
  [Registration link]
  ```
- **Frontend:** `/admin/invite/students` — textarea for emails, preview panel, send button

#### UC-25: Set Up Student Account
- **Actor:** Student
- **Endpoint:** `POST /api/register/student`
- **Request body:** `{token, firstName, lastName, email, password}`
- **Flow:**
  1. Student clicks unique link from invitation email
  2. System validates token (exists, not used, role=STUDENT)
  3. If already registered → redirect to login
  4. Student enters: first name, last name, email, password
  5. System validates input, creates `AppUser` with role=STUDENT
  6. Marks token as used. Redirects to login page.
- **Frontend:** `/register/student?token=...` — clean registration form

#### UC-26: Edit Student Account
- **Actor:** Student
- **Endpoint:** `PUT /api/students/{id}`
- **Editable:** firstName, lastName, email
- **Validation:** Standard field validation
- **Frontend:** `/student/account` — profile edit form

### B3. Student Discovery

#### UC-15: Find Students
- **Actors:** Admin, Instructor
- **Endpoint:** `GET /api/students?firstName=&lastName=&email=&sectionName=&teamName=&sectionId=&teamId=`
- **All search params optional**
- **Response:** List of `{firstName, lastName, teamName}`
- **Sort:** Section name descending, then last name ascending
- **Frontend:** `/admin/students` — searchable data table

#### UC-16: View a Student
- **Actors:** Admin, Instructor
- **Endpoint:** `GET /api/students/{id}`
- **Response:** `{firstName, lastName, sectionName, teamName, peerEvaluations, wars}`
- **Note:** This view is also the launching point for UC-33 (peer eval report) and UC-34 (WAR report) — those are built by Members 1 and 3 respectively, but your "View Student" page should have links/buttons to those report views
- **Frontend:** `/admin/students/:id` — student detail card

### B4. WAR System (Your Core Feature)

#### UC-27: Manage WAR Activities (CRUD) ⭐ Hard
- **Actor:** Student
- **Endpoints:**
  - `GET /api/students/{id}/wars?weekId=` — get WAR for a specific week
  - `POST /api/students/{id}/wars/{weekId}/activities` — add activity
  - `PUT /api/activities/{activityId}` — edit activity
  - `DELETE /api/activities/{activityId}` — delete activity
- **Flow:**
  1. Student selects an active week (cannot select FUTURE weeks, but CAN select non-active weeks per BR-2)
  2. System shows existing activities for that week
  3. Student can: **Add**, **Edit**, or **Delete** activities
  4. Each activity has: category (enum), description, plannedHours, actualHours, status (enum)
- **Activity categories:** DEVELOPMENT, TESTING, BUGFIX, COMMUNICATION, DOCUMENTATION, DESIGN, PLANNING, LEARNING, DEPLOYMENT, SUPPORT, MISCELLANEOUS
- **Activity statuses:** IN_PROGRESS, UNDER_TESTING, DONE
- **Business rule:** If no WAR exists for that student+week, auto-create one on first activity add
- **Frontend:** `/student/war/:weekId` — week selector dropdown + activity table with add/edit/delete actions. Each row is an activity. Add/Edit opens a form dialog.

### B5. WAR Reports

#### UC-32: Generate Team WAR Report
- **Actors:** Instructor, Student
- **Endpoint:** `GET /api/teams/{teamId}/war-report?week=<activeWeekId>`
- **Parameters:** Active week (default: previous week)
- **Response:** For each student on the team, list their activities that week
- **Columns:** Student name, activity category, planned activity, description, planned hours, actual hours, status
- **Sort:** By student last name ascending
- **Must show:** Students who did NOT submit a WAR that week
- **Format:** HTML report (rendered in the frontend)
- **Frontend:** `/instructor/reports/war/team/:id` — week selector + report table

#### UC-34: Generate Individual Student WAR Report ⭐ Moved from M3
- **Actors:** Instructor
- **Endpoint:** `GET /api/students/{id}/war-report?startWeek=<id>&endWeek=<id>`
- **Parameters:** Start active week, end active week (period)
- **Response:** Activities grouped by active week
- **Columns per week group:** Activity category, planned activity, description, planned hours, actual hours, status
- **Sort:** By active week chronologically
- **Precondition:** Instructor navigates via UC-16 (View Student) → clicks "WAR Report"
- **Frontend:** `/instructor/reports/war/student/:id` — start/end week selectors + grouped report

---

## PART C: INTEGRATION POINTS

> These are the boundaries where your code touches the other members' code. Follow these contracts exactly.

### C1. You DEPEND ON (Member 1 provides these)

| What | How you use it |
|------|----------------|
| `Section` entity | You query sections to scope teams. Use `SectionRepository.findById()` |
| `ActiveWeek` entity | Your WAR system needs active weeks for the week selector. Use `ActiveWeekRepository.findBySection()` |
| `Rubric` / `Criterion` entities | You don't use these directly, but they exist in the DB |
| `InvitationToken` entity | You CREATE tokens when inviting students (UC-11). Member 1 uses the same entity for instructor invitations |
| Auth/Security system | You use `@PreAuthorize` annotations. M1 sets up Spring Security config |

**Action items:**
- Coordinate with Member 1 on the `Section` and `ActiveWeek` entity field names early
- Make sure `InvitationToken` supports both STUDENT and INSTRUCTOR roles
- Agree on the auth/JWT implementation before anyone builds endpoints

### C2. You PROVIDE TO others

| What | Who uses it |
|------|-------------|
| `Team` entity + `TeamRepository` | Member 3 needs teams for student/instructor assignment (UC-12, UC-19) |
| `Student` lookup / `StudentRepository` | Member 3 needs to find students for assignment and deletion |
| `WeeklyActivityReport` + `Activity` entities | Member 3's cascade delete (UC-14, UC-17) must delete associated WARs |
| WAR report service methods | Your `WarService.getTeamWarReport()` and `getStudentWarReport()` could be reused |

**Action items:**
- Make your `TeamService` methods public and well-documented so Member 3 can call them
- Your `WarRepository` should have methods like `findByStudentAndActiveWeek()` and `deleteByTeam()` that Member 3's cascade logic can use
- Expose a `WarService.deleteAllByStudent(studentId)` and `WarService.deleteAllByTeam(teamId)` for cascade deletes

### C3. Member 3 DEPENDS ON you for cascade deletes

Member 3 owns UC-14 (Delete Team) and UC-17 (Delete Student). Both require deleting associated WARs. You should provide:

```java
// In WarService.java — Member 3 calls these
public void deleteAllWarsByStudent(Long studentId) {
    warRepository.deleteByStudentId(studentId);
}

public void deleteAllWarsByTeam(Long teamId) {
    // Find all students on the team, delete their WARs for this team
    List<AppUser> students = teamRepository.findById(teamId).getStudents();
    for (AppUser student : students) {
        warRepository.deleteByStudentId(student.getId());
    }
}
```

### C4. Shared Frontend Router

Your routes in `router/index.js`:
```javascript
// === YOUR ROUTES (Member 2) ===

// Admin views
{ path: '/admin/teams', component: () => import('@/views/admin/TeamList.vue'), meta: { role: 'ADMIN' } },
{ path: '/admin/teams/:id', component: () => import('@/views/admin/TeamDetail.vue'), meta: { role: 'ADMIN' } },
{ path: '/admin/students', component: () => import('@/views/admin/StudentList.vue'), meta: { role: 'ADMIN' } },
{ path: '/admin/students/:id', component: () => import('@/views/admin/StudentDetail.vue'), meta: { role: 'ADMIN' } },
{ path: '/admin/invite/students', component: () => import('@/views/admin/InviteStudents.vue'), meta: { role: 'ADMIN' } },

// Student views
{ path: '/student/account', component: () => import('@/views/student/StudentAccount.vue'), meta: { role: 'STUDENT' } },
{ path: '/student/war/:weekId?', component: () => import('@/views/student/WarManager.vue'), meta: { role: 'STUDENT' } },

// Registration (public)
{ path: '/register/student', component: () => import('@/views/auth/StudentRegister.vue') },

// Instructor report views
{ path: '/instructor/reports/war/team/:id', component: () => import('@/views/instructor/TeamWarReport.vue'), meta: { role: 'INSTRUCTOR' } },
{ path: '/instructor/reports/war/student/:id', component: () => import('@/views/instructor/StudentWarReport.vue'), meta: { role: 'INSTRUCTOR' } },
```

---

## PART D: IMPLEMENTATION ORDER (Suggested)

> Start with what others depend on, then build your features.

### Phase 1: Shared Foundation (coordinate with team)
1. ☐ Agree on entity definitions (Part A2) — commit the model classes
2. ☐ Set up Docker Compose + Spring Boot + Vue.js scaffold
3. ☐ Implement `AppUser` entity + auth system (can split with M1)
4. ☐ Commit and push so all members work from the same base

### Phase 2: Team CRUD (others depend on this)
5. ☐ `Team` entity, repository, service, controller
6. ☐ UC-9: Create team
7. ☐ UC-10: Edit team
8. ☐ UC-7: Find teams (search/filter)
9. ☐ UC-8: View team detail
10. ☐ Frontend pages for all team CRUD

### Phase 3: Student Lifecycle
11. ☐ UC-11: Invite students (email system + invitation tokens)
12. ☐ UC-25: Student registration
13. ☐ UC-26: Edit student account
14. ☐ UC-15: Find students
15. ☐ UC-16: View student detail
16. ☐ Frontend pages for student lifecycle

### Phase 4: WAR System (your core feature)
17. ☐ `WeeklyActivityReport` + `Activity` entities, repos, service
18. ☐ UC-27: Manage WAR activities (add/edit/delete)
19. ☐ WAR manager frontend (week selector + activity CRUD)

### Phase 5: WAR Reports
20. ☐ UC-32: Team WAR report
21. ☐ UC-34: Individual student WAR report
22. ☐ Report frontend pages

### Phase 6: Integration & Cascade Support
23. ☐ Expose `deleteAllWarsByStudent()` and `deleteAllWarsByTeam()` for M3
24. ☐ Add report links to Student Detail view (for M1/M3 reports)
25. ☐ Test end-to-end with team's merged code

---

## PART E: HOW TO USE THIS WITH CLAUDE CODE

### Step 1: Clone the reference repo first
```bash
git clone https://github.com/Washingtonwei/project-pulse.git reference-pulse
```
This is Dr. Wei's reference implementation. Don't copy from it — use it to understand patterns.

### Step 2: Set up your team's repo
```bash
mkdir project-pulse-team && cd project-pulse-team
git init
# Set up the scaffold (Spring Boot + Vue.js)
```

### Step 3: Feed this document to Claude Code
In Claude Code, start a conversation like:

```
I'm building Project Pulse, a web app for managing senior design teams.
I'm Member 2 on a 3-person team.

Read this spec file: @member2-implementation-guide.md
Also look at the reference repo in ./reference-pulse/ for implementation patterns.

Let's start with Phase 1 — setting up the shared foundation.
Create the Spring Boot project with the entity definitions from Part A2.
```

### Step 4: Work through the phases
For each phase, tell Claude Code:
```
Now let's implement Phase 2 — Team CRUD.
Start with UC-9 (Create Team). Build the full stack:
entity → repository → service → controller → DTO → Vue frontend page.
Follow the patterns from the reference repo.
```

### Step 5: Before each git push, check integration
```
Review my code against Part C (integration points).
Make sure my Team and WAR entities are compatible with what
Member 1 and Member 3 need.
```

### Tips for Claude Code
- **Be specific about which UC you're working on** — "Implement UC-27" is better than "build the WAR system"
- **Reference the reference repo** — "Follow the pattern used in the reference repo's controller layer"
- **Ask it to generate tests** — "Write unit tests for WarService"
- **Keep the AI interaction log** — this is a graded deliverable (3-4 high-quality examples)
