package edu.tcu.cs.projectpulse.repository;

import edu.tcu.cs.projectpulse.model.PeerEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeerEvaluationRepository extends JpaRepository<PeerEvaluation, Long> {

    List<PeerEvaluation> findByEvaluateeIdAndActiveWeekId(Long evaluateeId, Long weekId);

    List<PeerEvaluation> findByEvaluatorIdAndActiveWeekId(Long evaluatorId, Long weekId);

    Optional<PeerEvaluation> findByEvaluatorIdAndEvaluateeIdAndActiveWeekId(
            Long evaluatorId, Long evaluateeId, Long weekId);

    boolean existsByEvaluatorIdAndEvaluateeIdAndActiveWeekId(
            Long evaluatorId, Long evaluateeId, Long weekId);

    @Query("SELECT pe FROM PeerEvaluation pe WHERE pe.team.id = :teamId AND pe.activeWeek.id = :weekId")
    List<PeerEvaluation> findByTeamAndWeek(@Param("teamId") Long teamId, @Param("weekId") Long weekId);

    @Query("SELECT pe FROM PeerEvaluation pe WHERE pe.evaluatee.id = :studentId AND " +
           "pe.activeWeek.id IN :weekIds ORDER BY pe.activeWeek.startDate ASC")
    List<PeerEvaluation> findByEvaluateeAndWeeks(@Param("studentId") Long studentId,
                                                  @Param("weekIds") List<Long> weekIds);

    @Query("SELECT DISTINCT pe.evaluator FROM PeerEvaluation pe WHERE pe.team.id = :teamId AND pe.activeWeek.id = :weekId")
    List<Object> findEvaluatorsForTeamAndWeek(@Param("teamId") Long teamId, @Param("weekId") Long weekId);
}
