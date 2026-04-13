package edu.tcu.cs.projectpulse.repository;

import edu.tcu.cs.projectpulse.model.ActiveWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActiveWeekRepository extends JpaRepository<ActiveWeek, Long> {

    List<ActiveWeek> findBySectionIdOrderByStartDate(Long sectionId);

    List<ActiveWeek> findBySectionIdAndActiveTrue(Long sectionId);

    Optional<ActiveWeek> findBySectionIdAndStartDate(Long sectionId, LocalDate startDate);
}
