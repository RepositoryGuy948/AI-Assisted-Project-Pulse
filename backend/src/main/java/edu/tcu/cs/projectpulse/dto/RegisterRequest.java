package edu.tcu.cs.projectpulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String token;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String firstName;

    private String middleInitial;

    @NotBlank
    private String lastName;

    @NotBlank
    private String password;

    private String confirmPassword;
}
