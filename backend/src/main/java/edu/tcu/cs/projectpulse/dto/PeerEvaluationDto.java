package edu.tcu.cs.projectpulse.dto;

import lombok.Data;

import java.util.List;

@Data
public class PeerEvaluationDto {
    private Long id;
    private Long evaluatorId;
    private Long evaluateeId;
    private Long weekId;
    private Long teamId;
    private String publicComment;
    private String privateComment;
    private List<ScoreDto> scores;

    @Data
    public static class ScoreDto {
        private Long criterionId;
        private Integer score;
    }
}
