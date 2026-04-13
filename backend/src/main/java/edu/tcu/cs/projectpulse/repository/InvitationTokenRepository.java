package edu.tcu.cs.projectpulse.repository;

import edu.tcu.cs.projectpulse.model.InvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationTokenRepository extends JpaRepository<InvitationToken, Long> {

    Optional<InvitationToken> findByToken(String token);

    boolean existsByTokenAndUsedFalse(String token);
}
