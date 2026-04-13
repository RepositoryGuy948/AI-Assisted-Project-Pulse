package edu.tcu.cs.projectpulse.repository;

import edu.tcu.cs.projectpulse.model.Rubric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RubricRepository extends JpaRepository<Rubric, Long> {
    boolean existsByName(String name);
}
