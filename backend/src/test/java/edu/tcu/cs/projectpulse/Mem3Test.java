package edu.tcu.cs.projectpulse;

import edu.tcu.cs.projectpulse.dto.PeerEvaluationDto;
import edu.tcu.cs.projectpulse.model.*;
import edu.tcu.cs.projectpulse.repository.*;
import edu.tcu.cs.projectpulse.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Mem3Test {

    // --- Shared mocks (injected into whichever service needs each type) ---
    @Mock TeamRepository teamRepository;
    @Mock SectionRepository sectionRepository;
    @Mock UserRepository userRepository;
    @Mock WeeklyActivityReportRepository warRepository;
    @Mock PeerEvaluationRepository peerEvaluationRepository;
    @Mock EmailService emailService;
    @Mock ActiveWeekRepository activeWeekRepository;
    @Mock CriterionRepository criterionRepository;
    @Mock InvitationTokenRepository tokenRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks TeamService teamService;
    @InjectMocks PeerEvaluationService peerEvaluationService;
    @InjectMocks UserService userService;

    // ---- UC-12: Assign students to a team ----
    @Test
    void uc12_assignStudentsToTeam_studentsAreOnTeam() {
        Section section = Section.builder().id(1L).name("Spring 2025").build();
        Team team = Team.builder().id(1L).name("Team A").section(section).build();
        User s1 = User.builder().id(10L).email("s1@test.com").firstName("Alice").lastName("A")
                .role(User.Role.STUDENT).password("pw").enabled(true).build();
        User s2 = User.builder().id(11L).email("s2@test.com").firstName("Bob").lastName("B")
                .role(User.Role.STUDENT).password("pw").enabled(true).build();

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(userRepository.findById(10L)).thenReturn(Optional.of(s1));
        when(userRepository.findById(11L)).thenReturn(Optional.of(s2));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        teamService.assignStudentsToTeam(1L, List.of(10L, 11L));

        assertEquals(team, s1.getTeam(), "s1 should be assigned to the team");
        assertEquals(team, s2.getTeam(), "s2 should be assigned to the team");
        verify(userRepository, times(2)).save(any(User.class));
    }

    // ---- UC-13: Remove a student from a team ----
    @Test
    void uc13_removeStudentFromTeam_studentRemovedButUserRecordRemains() {
        Section section = Section.builder().id(1L).name("Spring 2025").build();
        Team team = Team.builder().id(1L).name("Team A").section(section).build();
        User student = User.builder().id(10L).email("s@test.com").firstName("Alice").lastName("A")
                .role(User.Role.STUDENT).password("pw").enabled(true).team(team).build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        teamService.removeStudentFromTeam(1L, 10L);

        assertNull(student.getTeam(), "Student should no longer have a team assignment");
        verify(userRepository).save(student);
        verify(userRepository, never()).delete(any(User.class));
    }

    // ---- UC-14: Delete a team ----
    @Test
    void uc14_deleteTeam_cascadesWarsAndPeerEvalsAndRemovesTeam() {
        Section section = Section.builder().id(1L).name("Spring 2025").build();
        User student = User.builder().id(10L).email("s@test.com").firstName("Alice").lastName("A")
                .role(User.Role.STUDENT).password("pw").enabled(true).build();
        User instructor = User.builder().id(20L).email("i@test.com").firstName("Prof").lastName("P")
                .role(User.Role.INSTRUCTOR).password("pw").enabled(true).build();
        Team team = Team.builder().id(1L).name("Team A").section(section).build();
        team.getStudents().add(student);
        team.getInstructors().add(instructor);
        student.setTeam(team);

        WeeklyActivityReport war = WeeklyActivityReport.builder().id(1L).student(student).build();
        PeerEvaluation eval = PeerEvaluation.builder()
                .id(1L).evaluator(student).evaluatee(student).activeWeek(
                        ActiveWeek.builder().id(1L).startDate(LocalDate.now().minusDays(14))
                                .endDate(LocalDate.now().minusDays(8)).section(section).build())
                .team(team).build();

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(warRepository.findByStudentId(10L)).thenReturn(List.of(war));
        when(peerEvaluationRepository.findByTeamId(1L)).thenReturn(List.of(eval));
        when(userRepository.saveAll(any())).thenReturn(List.of(student));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        teamService.deleteTeam(1L);

        verify(warRepository).deleteAll(List.of(war));
        verify(peerEvaluationRepository).deleteAll(List.of(eval));
        verify(teamRepository).delete(team);
    }

    // ---- UC-17: Delete a student ----
    @Test
    void uc17_deleteStudent_cascadesWarsAndPeerEvalsAndDeletesUser() {
        Section section = Section.builder().id(1L).name("Spring 2025").build();
        Team team = Team.builder().id(1L).name("Team A").section(section).build();
        User student = User.builder().id(10L).email("s@test.com").firstName("Alice").lastName("A")
                .role(User.Role.STUDENT).password("pw").enabled(true).team(team).build();

        WeeklyActivityReport war = WeeklyActivityReport.builder().id(1L).student(student).build();
        PeerEvaluation evalAsEvaluatee = PeerEvaluation.builder()
                .id(1L).evaluatee(student).evaluator(student).activeWeek(
                        ActiveWeek.builder().id(1L).startDate(LocalDate.now().minusDays(14))
                                .endDate(LocalDate.now().minusDays(8)).section(section).build())
                .team(team).build();
        PeerEvaluation evalAsEvaluator = PeerEvaluation.builder()
                .id(2L).evaluator(student).evaluatee(student).activeWeek(
                        ActiveWeek.builder().id(2L).startDate(LocalDate.now().minusDays(7))
                                .endDate(LocalDate.now().minusDays(1)).section(section).build())
                .team(team).build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(warRepository.findByStudentId(10L)).thenReturn(List.of(war));
        when(peerEvaluationRepository.findByEvaluateeId(10L)).thenReturn(List.of(evalAsEvaluatee));
        when(peerEvaluationRepository.findByEvaluatorId(10L)).thenReturn(List.of(evalAsEvaluator));
        when(userRepository.save(any(User.class))).thenReturn(student);

        userService.deleteUser(10L);

        verify(warRepository).deleteAll(List.of(war));
        verify(peerEvaluationRepository).deleteAll(List.of(evalAsEvaluatee));
        verify(peerEvaluationRepository).deleteAll(List.of(evalAsEvaluator));
        verify(userRepository).delete(student);
    }

    // ---- UC-19: Assign instructors to a team ----
    @Test
    void uc19_assignInstructorsToTeam_atLeastOneInstructorAssigned() {
        Section section = Section.builder().id(1L).name("Spring 2025").build();
        Team team = Team.builder().id(1L).name("Team A").section(section).build();
        User instructor = User.builder().id(20L).email("i@test.com").firstName("Prof").lastName("P")
                .role(User.Role.INSTRUCTOR).password("pw").enabled(true).build();

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(userRepository.findById(20L)).thenReturn(Optional.of(instructor));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        teamService.assignInstructorsToTeam(1L, List.of(20L));

        assertTrue(team.getInstructors().contains(instructor), "Team should contain the assigned instructor");
        verify(teamRepository).save(team);
    }

    // ---- UC-20: Remove an instructor from a team ----
    @Test
    void uc20_removeInstructorFromTeam_instructorIsRemovedFromTeam() {
        Section section = Section.builder().id(1L).name("Spring 2025").build();
        User instructor1 = User.builder().id(20L).email("i1@test.com").firstName("Prof1").lastName("P1")
                .role(User.Role.INSTRUCTOR).password("pw").enabled(true).build();
        User instructor2 = User.builder().id(21L).email("i2@test.com").firstName("Prof2").lastName("P2")
                .role(User.Role.INSTRUCTOR).password("pw").enabled(true).build();
        Team team = Team.builder().id(1L).name("Team A").section(section).build();
        team.getInstructors().add(instructor1);
        team.getInstructors().add(instructor2);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        teamService.removeInstructorFromTeam(1L, 20L);

        assertFalse(team.getInstructors().stream().anyMatch(i -> i.getId().equals(20L)),
                "Removed instructor should no longer be on the team");
        verify(teamRepository).save(team);
    }

    // ---- UC-28: Submit peer evaluation for a completed week with valid scores ----
    @Test
    void uc28_submitPeerEvaluation_savedWithValidScoresForPastWeek() {
        Section section = Section.builder().id(1L).name("Spring 2025").build();
        User evaluator = User.builder().id(1L).email("eval@test.com").firstName("Eval").lastName("E")
                .role(User.Role.STUDENT).password("pw").enabled(true).build();
        User evaluatee = User.builder().id(2L).email("tee@test.com").firstName("Tee").lastName("T")
                .role(User.Role.STUDENT).password("pw").enabled(true).build();
        Team team = Team.builder().id(1L).name("Team A").section(section).build();
        ActiveWeek pastWeek = ActiveWeek.builder().id(1L)
                .startDate(LocalDate.now().minusDays(14))
                .endDate(LocalDate.now().minusDays(8))
                .active(false).section(section).build();
        Criterion criterion = Criterion.builder().id(1L).name("Teamwork").maxScore(10.0).build();

        PeerEvaluationDto.ScoreDto scoreDto = new PeerEvaluationDto.ScoreDto();
        scoreDto.setCriterionId(1L);
        scoreDto.setScore(8);

        PeerEvaluationDto dto = new PeerEvaluationDto();
        dto.setEvaluatorId(1L);
        dto.setEvaluateeId(2L);
        dto.setWeekId(1L);
        dto.setTeamId(1L);
        dto.setPublicComment("Great teamwork");
        dto.setPrivateComment("Could communicate more proactively");
        dto.setScores(List.of(scoreDto));

        PeerEvaluation saved = PeerEvaluation.builder()
                .id(1L).evaluator(evaluator).evaluatee(evaluatee).activeWeek(pastWeek).team(team)
                .publicComment("Great teamwork").privateComment("Could communicate more proactively")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(evaluator));
        when(userRepository.findById(2L)).thenReturn(Optional.of(evaluatee));
        when(activeWeekRepository.findById(1L)).thenReturn(Optional.of(pastWeek));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(peerEvaluationRepository
                .findByEvaluatorIdAndEvaluateeIdAndActiveWeekId(1L, 2L, 1L))
                .thenReturn(Optional.empty());
        when(criterionRepository.findById(1L)).thenReturn(Optional.of(criterion));
        when(peerEvaluationRepository.save(any(PeerEvaluation.class))).thenReturn(saved);

        PeerEvaluation result = peerEvaluationService.submitEvaluation(dto);

        assertNotNull(result);
        verify(peerEvaluationRepository).save(any(PeerEvaluation.class));
    }

    // ---- UC-29: Student peer eval report — private comments and evaluator names excluded ----
    @Test
    void uc29_studentReport_excludesPrivateCommentsAndEvaluatorIdentity() {
        Section section = Section.builder().id(1L).name("Spring 2025").build();
        Team team = Team.builder().id(1L).name("Team A").section(section).build();
        User evaluator = User.builder().id(1L).email("eval@test.com").firstName("Eval").lastName("E")
                .role(User.Role.STUDENT).password("pw").enabled(true).build();
        User evaluatee = User.builder().id(2L).email("tee@test.com").firstName("Tee").lastName("T")
                .role(User.Role.STUDENT).password("pw").enabled(true).build();
        ActiveWeek week = ActiveWeek.builder().id(1L)
                .startDate(LocalDate.now().minusDays(14))
                .endDate(LocalDate.now().minusDays(8))
                .section(section).build();
        Criterion criterion = Criterion.builder().id(1L).name("Teamwork").maxScore(10.0).build();
        PeerEvaluationScore score = PeerEvaluationScore.builder()
                .id(1L).criterion(criterion).score(7).build();
        PeerEvaluation eval = PeerEvaluation.builder()
                .id(1L).evaluator(evaluator).evaluatee(evaluatee).activeWeek(week).team(team)
                .publicComment("Good effort").privateComment("Needs to improve focus")
                .scores(new ArrayList<>(List.of(score)))
                .build();

        // called twice: once in buildStudentReport and once inside computeGrade
        when(peerEvaluationRepository.findByEvaluateeIdAndActiveWeekId(2L, 1L))
                .thenReturn(List.of(eval));
        when(criterionRepository.findById(1L)).thenReturn(Optional.of(criterion));

        Map<String, Object> report = peerEvaluationService.buildStudentReport(2L, 1L);

        assertTrue(report.containsKey("grade"), "Student report must include grade");
        assertTrue(report.containsKey("publicComments"), "Student report must include public comments");
        assertFalse(report.containsKey("privateComments"), "Student report must NOT include private comments key");
        assertFalse(report.containsKey("privateComment"), "Student report must NOT include private comment field");
        assertFalse(report.containsKey("evaluatorId"), "Student report must NOT expose evaluator ID");
        assertFalse(report.containsKey("evaluatorName"), "Student report must NOT expose evaluator name");

        @SuppressWarnings("unchecked")
        List<String> publicComments = (List<String>) report.get("publicComments");
        assertTrue(publicComments.contains("Good effort"), "Public comment should appear in student report");
    }

    // ---- UC-33: Instructor student report — private comments and evaluator names included ----
    @Test
    void uc33_instructorReport_includesPrivateCommentsAndEvaluatorIdentity() {
        Section section = Section.builder().id(1L).name("Spring 2025").build();
        Team team = Team.builder().id(1L).name("Team A").section(section).build();
        User evaluator = User.builder().id(1L).email("eval@test.com").firstName("Eval").lastName("E")
                .role(User.Role.STUDENT).password("pw").enabled(true).build();
        User evaluatee = User.builder().id(2L).email("tee@test.com").firstName("Tee").lastName("T")
                .role(User.Role.STUDENT).password("pw").enabled(true).build();
        ActiveWeek week = ActiveWeek.builder().id(1L)
                .startDate(LocalDate.now().minusDays(14))
                .endDate(LocalDate.now().minusDays(8))
                .section(section).build();
        Criterion criterion = Criterion.builder().id(1L).name("Teamwork").maxScore(10.0).build();
        PeerEvaluationScore score = PeerEvaluationScore.builder()
                .id(1L).criterion(criterion).score(8).build();
        PeerEvaluation eval = PeerEvaluation.builder()
                .id(1L).evaluator(evaluator).evaluatee(evaluatee).activeWeek(week).team(team)
                .publicComment("Solid contributor").privateComment("Missed two stand-ups")
                .scores(new ArrayList<>(List.of(score)))
                .build();

        PeerEvaluationDto dto = peerEvaluationService.toDto(eval);

        assertNotNull(dto.getPrivateComment(), "Instructor DTO must include the private comment");
        assertEquals("Missed two stand-ups", dto.getPrivateComment());
        assertNotNull(dto.getEvaluatorId(), "Instructor DTO must include the evaluator ID");
        assertEquals(1L, dto.getEvaluatorId(), "Evaluator ID should identify the submitter");
    }
}