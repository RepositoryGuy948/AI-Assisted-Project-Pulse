package edu.tcu.cs.projectpulse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteRequest {
    @NotBlank
    private String emails; // semicolon-separated

    private String customMessage;
}
