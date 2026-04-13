package edu.tcu.cs.projectpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RubricDto {
    private Long id;

    @NotBlank
    private String name;

    @NotEmpty
    private List<CriterionDto> criteria;

    @Data
    public static class CriterionDto {
        private Long id;

        @NotBlank
        private String name;

        private String description;

        private Double maxScore;
    }
}
