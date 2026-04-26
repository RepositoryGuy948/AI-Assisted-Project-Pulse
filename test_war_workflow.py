#!/usr/bin/env python3
"""
Project Pulse — End-to-End WAR Workflow Test
============================================
Tests the complete student WAR submission workflow:
  1. Login as admin
  2. Create a section + team
  3. Invite a student (via email)
  4. Fetch token from Mailpit
  5. Register the student
  6. Assign student to the team
  7. Login as student, submit a WAR with 3 activities
  8. Login as instructor, generate the team WAR report
  9. Verify the student's activities appear in the report

Requirements:
  pip install requests

Usage:
  python test_war_workflow.py

Assumptions:
  - Backend running on http://localhost:8080
  - Mailpit running on http://localhost:8025
  - Default seeds exist (admin@projectpulse.edu / admin123,
    instructor b.wei@abc.edu / 123456)
"""

import sys
import re
import time
import json
import requests
from datetime import date, timedelta

# ── Config ─────────────────────────────────────────────────────────────────────
API          = "http://localhost:8080/api"
MAILPIT      = "http://localhost:8025/api/v1"

ADMIN_EMAIL  = "admin@projectpulse.edu"
ADMIN_PASS   = "admin123"
INST_EMAIL   = "b.wei@abc.edu"
INST_PASS    = "123456"

# New student created by this test run
STUDENT_EMAIL = "war.test.student@tcu.edu"
STUDENT_PASS  = "TestPass123!"

# ── Colours ────────────────────────────────────────────────────────────────────
GRN  = "\033[92m"
RED  = "\033[91m"
YLW  = "\033[93m"
BLU  = "\033[94m"
CYN  = "\033[96m"
BLD  = "\033[1m"
RST  = "\033[0m"

def ok(msg):     print(f"    {GRN}✓{RST}  {msg}")
def warn(msg):   print(f"    {YLW}!{RST}  {msg}")
def info(msg):   print(f"    {BLU}→{RST}  {msg}")
def fail(msg):
    print(f"\n    {RED}✗  FAILED: {msg}{RST}\n")
    sys.exit(1)

def step(n, title):
    print(f"\n{BLD}{CYN}{'─'*60}{RST}")
    print(f"{BLD}{CYN}  Step {n}: {title}{RST}")
    print(f"{BLD}{CYN}{'─'*60}{RST}")

def assert_ok(r, context=""):
    if r.status_code >= 400:
        try:
            body = r.json()
        except Exception:
            body = r.text
        fail(f"{context} — HTTP {r.status_code}: {body}")

# ── HTTP helpers ───────────────────────────────────────────────────────────────
def post(path, data, token=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    return requests.post(f"{API}{path}", json=data, headers=headers)

def get(path, params=None, token=None):
    headers = {}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    return requests.get(f"{API}{path}", params=params, headers=headers)

def put(path, data, token=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    return requests.put(f"{API}{path}", json=data, headers=headers)

# ── Mailpit helpers ────────────────────────────────────────────────────────────
def mailpit_latest_to(address, retries=10, delay=1.0):
    """Poll Mailpit until an email arrives for `address`. Returns the message dict."""
    for attempt in range(retries):
        r = requests.get(f"{MAILPIT}/messages")
        if r.status_code == 200:
            messages = r.json().get("messages") or []
            for msg in messages:
                recipients = [t.get("Address", "") for t in (msg.get("To") or [])]
                if address.lower() in [a.lower() for a in recipients]:
                    return msg
        if attempt < retries - 1:
            info(f"  Waiting for email… (attempt {attempt + 1}/{retries})")
            time.sleep(delay)
    fail(f"No email found for {address} in Mailpit after {retries} attempts")

def mailpit_body(message_id):
    """Fetch the plain-text body of a Mailpit message."""
    r = requests.get(f"{MAILPIT}/message/{message_id}")
    if r.status_code != 200:
        fail(f"Could not fetch Mailpit message {message_id}")
    data = r.json()
    # Prefer plain text; fall back to HTML
    return data.get("Text") or data.get("HTML") or ""

def extract_token(body):
    """Pull the registration UUID token out of the email body."""
    match = re.search(r"token=([0-9a-f\-]{36})", body)
    if not match:
        fail(f"Could not find token=<uuid> in email body:\n{body[:500]}")
    return match.group(1)

# ── Date helpers ───────────────────────────────────────────────────────────────
def last_monday():
    today = date.today()
    return today - timedelta(days=today.weekday())   # weekday() 0 = Monday

# ══════════════════════════════════════════════════════════════════════════════
# MAIN TEST
# ══════════════════════════════════════════════════════════════════════════════
def main():
    print(f"\n{BLD}{'═'*60}{RST}")
    print(f"{BLD}  Project Pulse — WAR Workflow End-to-End Test{RST}")
    print(f"{BLD}{'═'*60}{RST}")

    # ──────────────────────────────────────────────────────────────────────────
    step(1, "Login as admin")
    # ──────────────────────────────────────────────────────────────────────────
    r = post("/auth/login", {"email": ADMIN_EMAIL, "password": ADMIN_PASS})
    assert_ok(r, "Admin login")
    admin_token = r.json()["token"]
    ok(f"Logged in as {ADMIN_EMAIL}  (token …{admin_token[-8:]})")

    # ──────────────────────────────────────────────────────────────────────────
    step(2, "Create section and team")
    # ──────────────────────────────────────────────────────────────────────────
    monday = last_monday()
    section_start = (monday - timedelta(weeks=2)).isoformat()
    section_end   = (monday + timedelta(weeks=10)).isoformat()
    section_name  = f"E2E-Test-{monday.isoformat()}"

    # Create section (idempotent — if it already exists, fetch it)
    r = post("/sections", {
        "name":      section_name,
        "startDate": section_start,
        "endDate":   section_end,
    }, token=admin_token)

    if r.status_code == 400 and "already exists" in r.text:
        warn(f"Section '{section_name}' already exists — fetching it")
        r2 = get("/sections", params={"name": section_name}, token=admin_token)
        assert_ok(r2, "Fetch sections")
        sections = r2.json()
        matching = [s for s in sections if s["name"] == section_name]
        if not matching:
            fail(f"Section '{section_name}' not found after existence error")
        section_id = matching[0]["id"]
    else:
        assert_ok(r, "Create section")
        section_id = r.json()["id"]
        ok(f"Section created  id={section_id}  name='{section_name}'")
        ok(f"  dates: {section_start} → {section_end}")
        ok(f"  active weeks auto-generated from section date range")

    # Fetch the active weeks (auto-generated on section create)
    r = get(f"/sections/{section_id}/active-weeks", token=admin_token)
    assert_ok(r, "Fetch active weeks")
    weeks = r.json()
    if not weeks:
        fail("No active weeks generated for section — check section start/end dates")
    ok(f"Found {len(weeks)} active weeks")

    # Pick the week whose startDate <= today (most recent past week)
    today_str = date.today().isoformat()
    past_weeks = [w for w in weeks if w["startDate"] <= today_str]
    if not past_weeks:
        fail("No past/current weeks found — section start date may be in the future")
    target_week = sorted(past_weeks, key=lambda w: w["startDate"])[-1]
    week_id = target_week["id"]
    ok(f"Using week id={week_id}  start={target_week['startDate']}  active={target_week['active']}")

    # Create team in section
    team_name = f"E2E-Team-{monday.isoformat()}"
    r = post(f"/sections/{section_id}/teams", {
        "name":        team_name,
        "description": "End-to-end test team",
        "websiteUrl":  "https://example.com",
    }, token=admin_token)

    if r.status_code in (400, 409) and "already exists" in r.text.lower():
        warn(f"Team '{team_name}' already exists — fetching it")
        r2 = get("/teams", params={"teamName": team_name}, token=admin_token)
        assert_ok(r2, "Fetch teams")
        teams = r2.json()
        matching = [t for t in teams if t["name"] == team_name]
        if not matching:
            fail(f"Team '{team_name}' not found after existence error")
        team_id = matching[0]["id"]
    else:
        assert_ok(r, "Create team")
        team_id = r.json()["id"]
        ok(f"Team created  id={team_id}  name='{team_name}'")

    # Assign the seeded instructor to the team
    r2 = get("/instructors", params={"email": INST_EMAIL}, token=admin_token)
    assert_ok(r2, "Find instructor")
    instructors = r2.json()
    if not instructors:
        fail(f"Instructor {INST_EMAIL} not found — ensure DataInitializer ran")
    instructor_id = instructors[0]["id"]
    r2 = post(f"/teams/{team_id}/instructors",
              {"instructorIds": [instructor_id]}, token=admin_token)
    assert_ok(r2, "Assign instructor to team")
    ok(f"Instructor {INST_EMAIL} (id={instructor_id}) assigned to team")

    # ──────────────────────────────────────────────────────────────────────────
    step(3, "Invite student by email")
    # ──────────────────────────────────────────────────────────────────────────
    # Clear any previous emails for this address in Mailpit
    requests.delete(f"{MAILPIT}/messages")
    info(f"Mailpit inbox cleared")

    r = post(f"/sections/{section_id}/invite-students",
             {"emails": STUDENT_EMAIL}, token=admin_token)
    assert_ok(r, "Invite student")
    count = r.json().get("count", "?")
    ok(f"Invitation sent to {STUDENT_EMAIL}  (count={count})")

    # ──────────────────────────────────────────────────────────────────────────
    step(4, "Fetch registration token from Mailpit")
    # ──────────────────────────────────────────────────────────────────────────
    info(f"Polling Mailpit at {MAILPIT}/messages …")
    msg = mailpit_latest_to(STUDENT_EMAIL)
    ok(f"Email received  id={msg['ID']}  subject='{msg.get('Subject', '')}'")

    body = mailpit_body(msg["ID"])
    token = extract_token(body)
    ok(f"Registration token extracted: {token}")

    # Validate the token via the API
    r = get("/register/validate-token", params={"token": token})
    assert_ok(r, "Validate token")
    token_info = r.json()
    ok(f"Token valid — email={token_info['email']}  role={token_info['role']}")

    # ──────────────────────────────────────────────────────────────────────────
    step(5, "Register student account")
    # ──────────────────────────────────────────────────────────────────────────
    r = post("/register/student", {
        "token":     token,
        "firstName": "Eve",
        "lastName":  "Tester",
        "email":     STUDENT_EMAIL,
        "password":  STUDENT_PASS,
    })
    if r.status_code == 400 and "already" in r.text.lower():
        warn("Student already registered — skipping registration, logging in directly")
        student_token  = None
        student_id     = None
    else:
        assert_ok(r, "Register student")
        student_data  = r.json()
        student_token = student_data["token"]
        student_id    = student_data["userId"]
        ok(f"Student registered  id={student_id}  email={student_data['email']}")
        ok(f"  Auto-login token issued: …{student_token[-8:]}")

    # ──────────────────────────────────────────────────────────────────────────
    step(6, "Assign student to team")
    # ──────────────────────────────────────────────────────────────────────────
    # Look up student id (needed if we skipped registration above)
    if student_id is None:
        r = get("/students", params={"email": STUDENT_EMAIL}, token=admin_token)
        assert_ok(r, "Find student")
        students = r.json()
        if not students:
            fail(f"Student {STUDENT_EMAIL} not found in /students")
        student_id = students[0]["id"]
        info(f"Found existing student id={student_id}")

    r = post(f"/teams/{team_id}/students",
             {"studentIds": [student_id]}, token=admin_token)
    assert_ok(r, "Assign student to team")
    ok(f"Student id={student_id} assigned to team id={team_id}")

    # ──────────────────────────────────────────────────────────────────────────
    step(7, "Login as student")
    # ──────────────────────────────────────────────────────────────────────────
    r = post("/auth/login", {"email": STUDENT_EMAIL, "password": STUDENT_PASS})
    assert_ok(r, "Student login")
    student_login = r.json()
    student_token = student_login["token"]
    student_id    = student_login["userId"]
    ok(f"Logged in as {STUDENT_EMAIL}  id={student_id}")

    # ──────────────────────────────────────────────────────────────────────────
    step(8, "Submit WAR with 3 activities")
    # ──────────────────────────────────────────────────────────────────────────
    activities_to_submit = [
        {
            "category":     "DEVELOPMENT",
            "description":  "Implemented the login page using Vue 3 Composition API",
            "plannedHours": 4.0,
            "actualHours":  3.5,
            "status":       "DONE",
        },
        {
            "category":     "TESTING",
            "description":  "Wrote unit tests for AuthService covering 5 methods",
            "plannedHours": 2.0,
            "actualHours":  2.5,
            "status":       "DONE",
        },
        {
            "category":     "DOCUMENTATION",
            "description":  "Updated the API README with all new endpoints",
            "plannedHours": 1.0,
            "actualHours":  0.5,
            "status":       "IN_PROGRESS",
        },
    ]

    submitted_ids = []
    for i, activity in enumerate(activities_to_submit, 1):
        r = post(f"/students/{student_id}/wars/{week_id}/activities",
                 activity, token=student_token)
        assert_ok(r, f"Add activity {i}")
        act_id = r.json()["id"]
        submitted_ids.append(act_id)
        ok(f"Activity {i} added  id={act_id}  "
           f"[{activity['category']}]  "
           f"planned={activity['plannedHours']}h  "
           f"actual={activity['actualHours']}h  "
           f"status={activity['status']}")

    ok(f"WAR submitted with {len(submitted_ids)} activities  "
       f"(ids: {submitted_ids})")

    # Verify WAR is readable back
    r = get(f"/students/{student_id}/wars/{week_id}", token=student_token)
    assert_ok(r, "Read back WAR")
    war_data = r.json()
    returned_count = len(war_data.get("activities", []))
    if returned_count != 3:
        fail(f"Expected 3 activities in WAR, got {returned_count}")
    ok(f"WAR read-back confirmed  {returned_count} activities")

    # ──────────────────────────────────────────────────────────────────────────
    step(9, "Login as instructor")
    # ──────────────────────────────────────────────────────────────────────────
    r = post("/auth/login", {"email": INST_EMAIL, "password": INST_PASS})
    assert_ok(r, "Instructor login")
    inst_token = r.json()["token"]
    ok(f"Logged in as {INST_EMAIL}  (token …{inst_token[-8:]})")

    # ──────────────────────────────────────────────────────────────────────────
    step(10, "Generate team WAR report and verify")
    # ──────────────────────────────────────────────────────────────────────────
    r = get(f"/teams/{team_id}/war-report",
            params={"weekId": week_id}, token=inst_token)
    assert_ok(r, "Team WAR report")
    report = r.json()

    info(f"Report returned {len(report)} student row(s)")
    for row in report:
        name      = f"{row.get('studentFirstName','')} {row.get('studentLastName','')}".strip()
        submitted = row.get("submitted", False)
        acts      = row.get("activities", [])
        marker    = f"{GRN}✓ submitted{RST}" if submitted else f"{YLW}✗ not submitted{RST}"
        print(f"      {BLD}{name}{RST}  {marker}  {len(acts)} activity/ies")
        for a in acts:
            print(f"         • [{a.get('category')}]  "
                  f"{a.get('description','')[:55]}…  "
                  f"planned={a.get('plannedHours')}h  "
                  f"actual={a.get('actualHours')}h  "
                  f"status={a.get('status')}")

    # Find our student's row
    student_row = next(
        (row for row in report if row.get("studentId") == student_id), None
    )
    if student_row is None:
        fail(f"Student id={student_id} not found in team WAR report")

    if not student_row.get("submitted"):
        fail(f"Student row shows submitted=false — WAR was not linked to team")

    report_activities = student_row.get("activities", [])
    if len(report_activities) != 3:
        fail(f"Expected 3 activities in report, got {len(report_activities)}")

    # Verify category/status of each activity
    expected_categories = {"DEVELOPMENT", "TESTING", "DOCUMENTATION"}
    actual_categories   = {a["category"] for a in report_activities}
    if actual_categories != expected_categories:
        fail(f"Category mismatch — expected {expected_categories}, got {actual_categories}")

    ok(f"Student row found  submitted=True  activities={len(report_activities)}")
    ok(f"Categories verified: {sorted(actual_categories)}")

    # Check total hours
    total_planned = sum(a.get("plannedHours") or 0 for a in report_activities)
    total_actual  = sum(a.get("actualHours")  or 0 for a in report_activities)
    ok(f"Hours totals  planned={total_planned}h  actual={total_actual}h")

    # ──────────────────────────────────────────────────────────────────────────
    print(f"\n{BLD}{'═'*60}{RST}")
    print(f"{BLD}{GRN}  ALL STEPS PASSED ✓{RST}")
    print(f"{BLD}{'═'*60}{RST}")

    print(f"""
{BLD}Summary:{RST}
  Section    : {section_name} (id={section_id})
  Team       : {team_name} (id={team_id})
  Week       : id={week_id}  start={target_week['startDate']}
  Student    : Eve Tester <{STUDENT_EMAIL}> (id={student_id})
  Instructor : {INST_EMAIL} (id={instructor_id})
  Activities : {submitted_ids}
""")

if __name__ == "__main__":
    # Quick connectivity check
    for name, url in [("Backend", f"{API}/auth/login"), ("Mailpit", f"{MAILPIT}/messages")]:
        try:
            requests.head(url, timeout=3)
        except requests.exceptions.ConnectionError:
            print(f"\n{RED}ERROR: Cannot reach {name} at {url}{RST}")
            print(f"  Make sure {name} is running before running this script.")
            print(f"  Backend: mvn spring-boot:run  (port 8080)")
            print(f"  Mailpit: docker compose up mailpit  (port 8025)\n")
            sys.exit(1)

    main()
