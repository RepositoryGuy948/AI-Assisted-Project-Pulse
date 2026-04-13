package edu.tcu.cs.projectpulse.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "peer_evaluation_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeerEvaluationScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peer_evaluation_id", nullable = false)
    private PeerEvaluation peerEvaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", nullable = false)
    private Criterion criterion;

    @Column(nullable = false)
    private Integer score;
}
