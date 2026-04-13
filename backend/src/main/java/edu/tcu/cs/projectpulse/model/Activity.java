package edu.tcu.cs.projectpulse.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private Double plannedHours;
    private Double actualHours;

    @Enumerated(EnumType.STRING)
    private ActivityStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "war_id", nullable = false)
    private WeeklyActivityReport war;

    public enum Category {
        DEVELOPMENT, TESTING, BUGFIX, COMMUNICATION, DOCUMENTATION,
        DESIGN, PLANNING, LEARNING, DEPLOYMENT, SUPPORT, MISCELLANEOUS
    }

    public enum ActivityStatus {
        IN_PROGRESS, UNDER_TESTING, DONE
    }
}
