package edu.tcu.cs.projectpulse.config;

import edu.tcu.cs.projectpulse.model.*;
import edu.tcu.cs.projectpulse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final TeamRepository teamRepository;
    private final ActiveWeekRepository activeWeekRepository;
    private final WeeklyActivityReportRepository warRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // ── Users ──────────────────────────────────────────────────────────────────
        ensureUser("admin@projectpulse.edu", "admin123", "System",  "Admin",    User.Role.ADMIN);
        User instructor = ensureUser("b.wei@abc.edu",       "123456",  "Bingyang", "Wei",      User.Role.INSTRUCTOR);

        User john  = ensureUser("j.smith@abc.edu",   "123456", "John",  "Smith",   User.Role.STUDENT);
        User sarah = ensureUser("s.johnson@abc.edu", "123456", "Sarah", "Johnson", User.Role.STUDENT);
        User alex  = ensureUser("a.davis@abc.edu",   "123456", "Alex",  "Davis",   User.Role.STUDENT);
        User maria = ensureUser("m.lee@abc.edu",     "123456", "Maria", "Lee",     User.Role.STUDENT);
        User kevin = ensureUser("k.nguyen@abc.edu",  "123456", "Kevin", "Nguyen",  User.Role.STUDENT);
        ensureUser("m.garcia@abc.edu", "123456", "Maria",  "Garcia", User.Role.STUDENT);
        ensureUser("t.nguyen@abc.edu", "123456", "Tommy",  "Nguyen", User.Role.STUDENT);
        ensureUser("a.patel@abc.edu",  "123456", "Anika",  "Patel",  User.Role.STUDENT);

        // ── Section ────────────────────────────────────────────────────────────────
        Section section = ensureSection("2024-2025",
                LocalDate.of(2024, 8, 26),
                LocalDate.of(2025, 5, 15));

        // ── Active Weeks ───────────────────────────────────────────────────────────
        // Historical weeks for existing WAR seed data
        ActiveWeek week1 = ensureActiveWeek(section,
                LocalDate.of(2025, 1, 13), LocalDate.of(2025, 1, 19));
        ActiveWeek week2 = ensureActiveWeek(section,
                LocalDate.of(2025, 1, 20), LocalDate.of(2025, 1, 26));

        // Current and previous week (relative to today) so peer eval window is always open
        LocalDate thisMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastMonday = thisMonday.minusWeeks(1);
        ActiveWeek prevWeek    = ensureActiveWeek(section, lastMonday,        lastMonday.plusDays(6));
        ActiveWeek currentWeek = ensureActiveWeek(section, thisMonday,        thisMonday.plusDays(6));

        // ── Teams ──────────────────────────────────────────────────────────────────
        Team teamAlpha = ensureTeam("Team Alpha", section,
                "Full-stack web app for campus event management");
        Team teamBeta  = ensureTeam("Team Beta",  section,
                "Mobile-first inventory tracking system");

        ensureInstructorOnTeam(instructor, teamAlpha);
        ensureInstructorOnTeam(instructor, teamBeta);

        // ── Student → Team assignments ─────────────────────────────────────────────
        ensureStudentTeam(john,  teamAlpha);
        ensureStudentTeam(sarah, teamAlpha);
        ensureStudentTeam(alex,  teamAlpha);
        ensureStudentTeam(maria, teamBeta);
        ensureStudentTeam(kevin, teamBeta);

        // ── Weekly Activity Reports ────────────────────────────────────────────────
        ensureWar(john, week1, new ActivitySeed[]{
            new ActivitySeed(Activity.Category.DEVELOPMENT,   "Implemented user authentication module",    4.0, 5.0, Activity.ActivityStatus.DONE),
            new ActivitySeed(Activity.Category.TESTING,       "Wrote unit tests for the auth service",     2.0, 2.5, Activity.ActivityStatus.DONE)
        });

        ensureWar(sarah, week1, new ActivitySeed[]{
            new ActivitySeed(Activity.Category.PLANNING,      "Created sprint board and assigned tasks",   2.0, 2.0, Activity.ActivityStatus.DONE),
            new ActivitySeed(Activity.Category.DOCUMENTATION, "Drafted REST API docs for all endpoints",   3.0, 3.5, Activity.ActivityStatus.DONE)
        });

        ensureWar(alex, week2, new ActivitySeed[]{
            new ActivitySeed(Activity.Category.DEVELOPMENT,   "Built event listing page with filters",     5.0, 6.0, Activity.ActivityStatus.DONE),
            new ActivitySeed(Activity.Category.BUGFIX,        "Fixed broken pagination on search view",    1.0, 1.5, Activity.ActivityStatus.DONE)
        });

        ensureWar(maria, week1, new ActivitySeed[]{
            new ActivitySeed(Activity.Category.DESIGN,        "Created Figma wireframes for inventory UI", 3.0, 4.0, Activity.ActivityStatus.DONE),
            new ActivitySeed(Activity.Category.DEVELOPMENT,   "Set up React Native project scaffold",      2.0, 2.0, Activity.ActivityStatus.DONE)
        });

        ensureWar(kevin, week2, new ActivitySeed[]{
            new ActivitySeed(Activity.Category.TESTING,       "E2E tests for barcode scanner flow",        4.0, 4.0, Activity.ActivityStatus.DONE),
            new ActivitySeed(Activity.Category.DEPLOYMENT,    "Deployed staging build to TestFlight",      1.0, 1.5, Activity.ActivityStatus.DONE)
        });

        // WARs for the current rolling previous week (keeps peer eval window open)
        ensureWar(john, prevWeek, new ActivitySeed[]{
            new ActivitySeed(Activity.Category.DEVELOPMENT,   "Integrated notification service",           3.0, 3.5, Activity.ActivityStatus.DONE),
            new ActivitySeed(Activity.Category.TESTING,       "Regression tests for auth module",          2.0, 2.0, Activity.ActivityStatus.DONE)
        });
        ensureWar(sarah, prevWeek, new ActivitySeed[]{
            new ActivitySeed(Activity.Category.DOCUMENTATION, "Updated API docs with new endpoints",       2.0, 2.5, Activity.ActivityStatus.DONE),
            new ActivitySeed(Activity.Category.PLANNING,      "Sprint retrospective and next sprint plan", 1.5, 1.5, Activity.ActivityStatus.DONE)
        });
        ensureWar(alex, prevWeek, new ActivitySeed[]{
            new ActivitySeed(Activity.Category.DEVELOPMENT,   "Refactored search component for reuse",     4.0, 5.0, Activity.ActivityStatus.DONE),
            new ActivitySeed(Activity.Category.BUGFIX,        "Fixed date formatting bug in event cards",  1.0, 1.0, Activity.ActivityStatus.DONE)
        });
        ensureWar(maria, prevWeek, new ActivitySeed[]{
            new ActivitySeed(Activity.Category.DEVELOPMENT,   "Built item detail screen in React Native",  4.0, 4.5, Activity.ActivityStatus.DONE),
            new ActivitySeed(Activity.Category.DESIGN,        "Polished onboarding flow UI",               2.0, 2.0, Activity.ActivityStatus.DONE)
        });
        ensureWar(kevin, prevWeek, new ActivitySeed[]{
            new ActivitySeed(Activity.Category.TESTING,       "Added unit tests for inventory service",    3.0, 3.0, Activity.ActivityStatus.DONE),
            new ActivitySeed(Activity.Category.DEPLOYMENT,    "Set up CI pipeline for automated builds",   2.0, 2.5, Activity.ActivityStatus.DONE)
        });
    }

    // ── Helpers ────────────────────────────────────────────────────────────────────

    private User ensureUser(String email, String rawPassword, String firstName, String lastName, User.Role role) {
        return userRepository.findByEmail(email).map(existing -> {
            boolean changed = false;
            if (!existing.isEnabled()) {
                existing.setEnabled(true);
                changed = true;
            }
            if (!passwordEncoder.matches(rawPassword, existing.getPassword())) {
                existing.setPassword(passwordEncoder.encode(rawPassword));
                changed = true;
                log.info("Reset password for seed user: {}", email);
            }
            if (changed) userRepository.save(existing);
            return existing;
        }).orElseGet(() -> {
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(role)
                    .enabled(true)
                    .build();
            user = userRepository.save(user);
            log.info("Created seed user: {} / {}", email, rawPassword);
            return user;
        });
    }

    private Section ensureSection(String name, LocalDate start, LocalDate end) {
        return sectionRepository.findByName(name).orElseGet(() -> {
            Section s = Section.builder()
                    .name(name)
                    .startDate(start)
                    .endDate(end)
                    .build();
            s = sectionRepository.save(s);
            log.info("Created seed section: {}", name);
            return s;
        });
    }

    private ActiveWeek ensureActiveWeek(Section section, LocalDate start, LocalDate end) {
        return activeWeekRepository.findBySectionIdAndStartDate(section.getId(), start).orElseGet(() -> {
            ActiveWeek w = ActiveWeek.builder()
                    .section(section)
                    .startDate(start)
                    .endDate(end)
                    .active(true)
                    .build();
            w = activeWeekRepository.save(w);
            log.info("Created seed active week: {} → {}", start, end);
            return w;
        });
    }

    private Team ensureTeam(String name, Section section, String description) {
        return teamRepository.findBySectionId(section.getId()).stream()
                .filter(t -> t.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    Team t = Team.builder()
                            .name(name)
                            .section(section)
                            .description(description)
                            .build();
                    t = teamRepository.save(t);
                    log.info("Created seed team: {}", name);
                    return t;
                });
    }

    private void ensureInstructorOnTeam(User instructor, Team team) {
        if (!team.getInstructors().contains(instructor)) {
            team.getInstructors().add(instructor);
            teamRepository.save(team);
            log.info("Assigned instructor {} to {}", instructor.getEmail(), team.getName());
        }
    }

    private void ensureStudentTeam(User student, Team team) {
        if (student.getTeam() == null) {
            student.setTeam(team);
            userRepository.save(student);
            log.info("Assigned student {} to {}", student.getEmail(), team.getName());
        }
    }

    private void ensureWar(User student, ActiveWeek week, ActivitySeed[] seeds) {
        if (warRepository.findByStudentIdAndActiveWeekId(student.getId(), week.getId()).isPresent()) {
            return;
        }
        WeeklyActivityReport war = WeeklyActivityReport.builder()
                .student(student)
                .activeWeek(week)
                .build();
        war = warRepository.save(war);
        for (ActivitySeed seed : seeds) {
            Activity a = Activity.builder()
                    .category(seed.category())
                    .description(seed.description())
                    .plannedHours(seed.plannedHours())
                    .actualHours(seed.actualHours())
                    .status(seed.status())
                    .war(war)
                    .build();
            war.getActivities().add(a);
        }
        warRepository.save(war);
        log.info("Created seed WAR for {} / week starting {}", student.getEmail(), week.getStartDate());
    }

    private record ActivitySeed(
            Activity.Category category,
            String description,
            double plannedHours,
            double actualHours,
            Activity.ActivityStatus status
    ) {}
}
