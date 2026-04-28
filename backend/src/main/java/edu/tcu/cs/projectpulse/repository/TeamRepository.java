package edu.tcu.cs.projectpulse.repository;

import edu.tcu.cs.projectpulse.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByNameAndSectionId(String name, Long sectionId);

    List<Team> findBySectionId(Long sectionId);

    // UC-7: search with optional filters; sorting done in service layer
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN t.instructors i WHERE " +
           "(:sectionId IS NULL OR t.section.id = :sectionId) AND " +
           "(:sectionName IS NULL OR LOWER(t.section.name) LIKE LOWER(CONCAT('%', :sectionName, '%'))) AND " +
           "(:teamName IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :teamName, '%'))) AND " +
           "(:instructor IS NULL OR LOWER(i.firstName) LIKE LOWER(CONCAT('%', :instructor, '%')) " +
           "                     OR LOWER(i.lastName)  LIKE LOWER(CONCAT('%', :instructor, '%')))")
    List<Team> searchTeams(@Param("sectionId") Long sectionId,
                           @Param("sectionName") String sectionName,
                           @Param("teamName") String teamName,
                           @Param("instructor") String instructor);
}
