package edu.tcu.cs.projectpulse.repository;

import edu.tcu.cs.projectpulse.model.WeeklyActivityReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyActivityReportRepository extends JpaRepository<WeeklyActivityReport, Long> {

    Optional<WeeklyActivityReport> findByStudentIdAndActiveWeekId(Long studentId, Long weekId);

    List<WeeklyActivityReport> findByStudentId(Long studentId);

    List<WeeklyActivityReport> findByActiveWeekId(Long weekId);

    @Query("SELECT w FROM WeeklyActivityReport w WHERE w.student.team.id = :teamId AND w.activeWeek.id = :weekId")
    List<WeeklyActivityReport> findByTeamAndWeek(@Param("teamId") Long teamId, @Param("weekId") Long weekId);

    @Query("SELECT w FROM WeeklyActivityReport w WHERE w.student.id = :studentId AND " +
           "w.activeWeek.id IN :weekIds ORDER BY w.activeWeek.startDate ASC")
    List<WeeklyActivityReport> findByStudentAndWeeks(@Param("studentId") Long studentId,
                                                     @Param("weekIds") List<Long> weekIds);
}
