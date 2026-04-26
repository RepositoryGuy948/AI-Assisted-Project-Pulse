package edu.tcu.cs.projectpulse.controller;

import edu.tcu.cs.projectpulse.dto.PeerEvaluationDto;
import edu.tcu.cs.projectpulse.model.PeerEvaluation;
import edu.tcu.cs.projectpulse.model.PeerEvaluationScore;
import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.repository.ActiveWeekRepository;
import edu.tcu.cs.projectpulse.repository.UserRepository;
import edu.tcu.cs.projectpulse.service.PeerEvaluationService;
import edu.tcu.cs.projectpulse.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PeerEvaluationController {

    private final PeerEvaluationService peerEvalService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ActiveWeekRepository activeWeekRepository;

    // UC-28: Submit/update peer evaluation
    @PostMapping("/students/{evaluatorId}/peer-evaluations")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<PeerEvaluationDto> submitEvaluation(
            @PathVariable Long evaluatorId,
            @RequestBody PeerEvaluationDto dto) {
        dto.setEvaluatorId(evaluatorId);
        return ResponseEntity.ok(peerEvalService.toDto(peerEvalService.submitEvaluation(dto)));
    }

    // UC-29: Student views own report for a week
    @GetMapping("/students/{studentId}/peer-evaluation-report")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Map<String, Object>> getStudentReport(
            @PathVariable Long studentId,
            @RequestParam Long weekId) {
        return ResponseEntity.ok(peerEvalService.buildStudentReport(studentId, weekId));
    }

    // UC-31: Instructor views entire section peer evaluation for a week
    @GetMapping("/sections/{sectionId}/peer-evaluation-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<Map<String, Object>>> getSectionReport(
            @PathVariable Long sectionId,
            @RequestParam Long weekId) {

        // Get all students in section
        List<User> students = userRepository.searchStudents(null, null, null, null, sectionId, null, null);
        List<Map<String, Object>> report = students.stream().map(student -> {
            List<PeerEvaluation> received = peerEvalService.getReceivedEvaluations(student.getId(), weekId);
            double grade = peerEvalService.computeGrade(student.getId(), weekId);
            List<String> commentedBy = received.stream()
                    .filter(e -> e.getPublicComment() != null && !e.getPublicComment().isBlank())
                    .map(e -> e.getEvaluator().getFirstName() + " " + e.getEvaluator().getLastName())
                    .collect(Collectors.toList());
            return Map.<String, Object>of(
                "studentId", student.getId(),
                "studentName", student.getFirstName() + " " + student.getLastName(),
                "grade", grade,
                "evaluationCount", received.size(),
                "commentedBy", commentedBy,
                "evaluations", received.stream().map(this::evalToInstructorMap).collect(Collectors.toList())
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(report);
    }

    // UC-33: Instructor views individual student peer evaluations over a period
    @GetMapping("/students/{studentId}/peer-evaluation-report/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<Map<String, Object>>> getStudentPeriodReport(
            @PathVariable Long studentId,
            @RequestParam List<Long> weekIds) {

        List<PeerEvaluation> evals = peerEvalService.getStudentEvaluationsForPeriod(studentId, weekIds);

        // Group by week
        Map<Long, List<PeerEvaluation>> byWeek = evals.stream()
                .collect(Collectors.groupingBy(e -> e.getActiveWeek().getId()));

        List<Map<String, Object>> report = byWeek.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Long wkId = entry.getKey();
                    List<PeerEvaluation> weekEvals = entry.getValue();
                    double grade = weekEvals.stream()
                            .mapToDouble(e -> e.getScores().stream().mapToInt(PeerEvaluationScore::getScore).sum())
                            .average().orElse(0.0);
                    return Map.<String, Object>of(
                        "weekId", wkId,
                        "weekStartDate", weekEvals.get(0).getActiveWeek().getStartDate().toString(),
                        "grade", grade,
                        "evaluations", weekEvals.stream().map(this::evalToInstructorMap).collect(Collectors.toList())
                    );
                }).collect(Collectors.toList());

        return ResponseEntity.ok(report);
    }

    // Evaluations submitted by a student for a week (to check completion)
    @GetMapping("/students/{evaluatorId}/submitted-evaluations")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<PeerEvaluationDto>> getSubmittedEvaluations(
            @PathVariable Long evaluatorId,
            @RequestParam Long weekId) {
        return ResponseEntity.ok(peerEvalService.getSubmittedEvaluations(evaluatorId, weekId)
                .stream().map(peerEvalService::toDto).collect(Collectors.toList()));
    }

    private Map<String, Object> evalToInstructorMap(PeerEvaluation eval) {
        return Map.of(
            "id", eval.getId(),
            "evaluatorName", eval.getEvaluator().getFirstName() + " " + eval.getEvaluator().getLastName(),
            "publicComment", eval.getPublicComment() != null ? eval.getPublicComment() : "",
            "privateComment", eval.getPrivateComment() != null ? eval.getPrivateComment() : "",
            "totalScore", eval.getScores().stream().mapToInt(PeerEvaluationScore::getScore).sum(),
            "scores", eval.getScores().stream().map(s -> Map.of(
                "criterionId", s.getCriterion().getId(),
                "criterionName", s.getCriterion().getName(),
                "score", s.getScore()
            )).collect(Collectors.toList())
        );
    }
}
