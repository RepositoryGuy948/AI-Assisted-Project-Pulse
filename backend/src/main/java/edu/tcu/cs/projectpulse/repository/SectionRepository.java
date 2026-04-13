package edu.tcu.cs.projectpulse.repository;

import edu.tcu.cs.projectpulse.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    boolean existsByName(String name);

    Optional<Section> findByName(String name);

    @Query("SELECT s FROM Section s WHERE :name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Section> searchByName(@Param("name") String name);
}
