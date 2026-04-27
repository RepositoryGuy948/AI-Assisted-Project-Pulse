package edu.tcu.cs.projectpulse.repository;

import edu.tcu.cs.projectpulse.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(User.Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
           "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled)")
    List<User> searchByRole(@Param("role") User.Role role,
                            @Param("firstName") String firstName,
                            @Param("lastName") String lastName,
                            @Param("email") String email,
                            @Param("enabled") Boolean enabled);

    @Query("SELECT u FROM User u LEFT JOIN u.team t LEFT JOIN t.section s WHERE u.role = 'STUDENT' AND " +
           "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:teamId IS NULL OR t.id = :teamId) AND " +
           "(:sectionId IS NULL OR s.id = :sectionId OR u.team IS NULL)")
    List<User> searchStudents(@Param("firstName") String firstName,
                              @Param("lastName") String lastName,
                              @Param("email") String email,
                              @Param("teamId") Long teamId,
                              @Param("sectionId") Long sectionId);
}
