package edu.tcu.cs.projectpulse.service;

import edu.tcu.cs.projectpulse.dto.PeerEvaluationDto;
import edu.tcu.cs.projectpulse.model.*;
import edu.tcu.cs.projectpulse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PeerEvaluationService {

    private final PeerEvaluationRepository peerEvalRepository;
    private final UserRepository userRepository;
    private final ActiveWeekRepository activeWeekRepository;
    private final TeamRepository teamRepository;
    private final CriterionRepository criterionRepository;

    public PeerEvaluation submitEvaluation(PeerEvaluationDto dto) {
        User evaluator = userRepository.findById(dto.getEvaluatorId())
                .orElseThrow(() -> new RuntimeException("Evaluator not found"));
        User evaluatee = userRepository.findById(dto.getEvaluateeId())
                .orElseThrow(() -> new RuntimeException("Evaluatee not found"));
        ActiveWeek week = activeWeekRepository.findById(dto.getWeekId())
                .orElseThrow(() -> new RuntimeException("Week not found"));
        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Check for existing evaluation (upsert)
        PeerEvaluation eval = peerEvalRepository
                .findByEvaluatorIdAndEvaluateeIdAndActiveWeekId(
                        dto.getEvaluatorId(), dto.getEvaluateeId(), dto.getWeekId())
                .orElseGet(() -> PeerEvaluation.builder()
                        .evaluator(evaluator)
                        .evaluatee(evaluatee)
                        .activeWeek(week)
                        .team(team)
                        .build());

        eval.setPublicComment(dto.getPublicComment());
        eval.setPrivateComment(dto.getPrivateComment());
        eval.getScores().clear();

        if (dto.getScores() != null) {
            for (PeerEvaluationDto.ScoreDto scoreDto : dto.getScores()) {
                Criterion criterion = criterionRepository.findById(scoreDto.getCriterionId())
                        .orElseThrow(() -> new RuntimeException("Criterion not found"));
                PeerEvaluationScore score = PeerEvaluationScore.builder()
                        .peerEvaluation(eval)
                        .criterion(criterion)
                        .score(scoreDto.getScore())
                        .build();
                eval.getScores().add(score);
            }
        }

        return peerEvalRepository.save(eval);
    }

    /**
     * UC-29 / UC-31: Compute grade for a student in a given week.
     * Grade = average of total scores across all evaluations received.
     */
    public double computeGrade(Long studentId, Long weekId) {
        List<PeerEvaluation> evals = peerEvalRepository.findByEvaluateeIdAndActiveWeekId(studentId, weekId);
        if (evals.isEmpty()) return 0.0;

        double totalSum = evals.stream()
                .mapToDouble(eval -> eval.getScores().stream()
                        .mapToInt(PeerEvaluationScore::getScore)
                        .sum())
                .sum();
        return totalSum / evals.size();
    }

    public List<PeerEvaluation> getReceivedEvaluations(Long studentId, Long weekId) {
        return peerEvalRepository.findByEvaluateeIdAndActiveWeekId(studentId, weekId);
    }

    public List<PeerEvaluation> getSubmittedEvaluations(Long evaluatorId, Long weekId) {
        return peerEvalRepository.findByEvaluatorIdAndActiveWeekId(evaluatorId, weekId);
    }

    public List<PeerEvaluation> getTeamEvaluations(Long teamId, Long weekId) {
        return peerEvalRepository.findByTeamAndWeek(teamId, weekId);
    }

    public List<PeerEvaluation> getStudentEvaluationsForPeriod(Long studentId, List<Long> weekIds) {
        return peerEvalRepository.findByEvaluateeAndWeeks(studentId, weekIds);
    }

    /**
     * Build student peer evaluation report for a single week (UC-29).
     * Returns data safe for student view (no private comments, no evaluator identity).
     */
    public Map<String, Object> buildStudentReport(Long studentId, Long weekId) {
        List<PeerEvaluation> evals = peerEvalRepository.findByEvaluateeIdAndActiveWeekId(studentId, weekId);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("grade", computeGrade(studentId, weekId));
        report.put("evaluationCount", evals.size());

        // Average scores per criterion
        Map<Long, List<Integer>> scoresByCriterion = new LinkedHashMap<>();
        List<String> publicComments = new ArrayList<>();

        for (PeerEvaluation eval : evals) {
            for (PeerEvaluationScore score : eval.getScores()) {
                scoresByCriterion.computeIfAbsent(score.getCriterion().getId(), k -> new ArrayList<>())
                        .add(score.getScore());
            }
            if (eval.getPublicComment() != null && !eval.getPublicComment().isBlank()) {
                publicComments.add(eval.getPublicComment());
            }
        }

        List<Map<String, Object>> criterionAverages = scoresByCriterion.entrySet().stream()
                .map(entry -> {
                    Criterion c = criterionRepository.findById(entry.getKey()).orElse(null);
                    double avg = entry.getValue().stream().mapToInt(i -> i).average().orElse(0);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("criterionId", entry.getKey());
                    m.put("criterionName", c != null ? c.getName() : "Unknown");
                    m.put("averageScore", avg);
                    return m;
                }).collect(Collectors.toList());

        report.put("criterionAverages", criterionAverages);
        report.put("publicComments", publicComments);
        return report;
    }

    public PeerEvaluationDto toDto(PeerEvaluation eval) {
        PeerEvaluationDto dto = new PeerEvaluationDto();
        dto.setId(eval.getId());
        dto.setEvaluatorId(eval.getEvaluator().getId());
        dto.setEvaluateeId(eval.getEvaluatee().getId());
        dto.setWeekId(eval.getActiveWeek().getId());
        dto.setTeamId(eval.getTeam().getId());
        dto.setPublicComment(eval.getPublicComment());
        dto.setPrivateComment(eval.getPrivateComment());
        dto.setScores(eval.getScores().stream().map(s -> {
            PeerEvaluationDto.ScoreDto sd = new PeerEvaluationDto.ScoreDto();
            sd.setCriterionId(s.getCriterion().getId());
            sd.setScore(s.getScore());
            return sd;
        }).collect(Collectors.toList()));
        return dto;
    }
}
