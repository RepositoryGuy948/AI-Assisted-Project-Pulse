package edu.tcu.cs.projectpulse.dto;

import edu.tcu.cs.projectpulse.model.Activity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityDto {
    private Long id;

    @NotNull
    private Activity.Category category;

    @NotBlank
    private String description;

    private Double plannedHours;
    private Double actualHours;

    private Activity.ActivityStatus status;

    private Long warId;
    private Long weekId;
}
