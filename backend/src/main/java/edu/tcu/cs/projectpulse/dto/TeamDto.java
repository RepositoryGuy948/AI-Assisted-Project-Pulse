package edu.tcu.cs.projectpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeamDto {
    private Long id;

    @NotBlank
    private String name;

    private String description;
    private String websiteUrl;

    private Long sectionId;
}
