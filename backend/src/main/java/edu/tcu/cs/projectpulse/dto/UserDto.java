package edu.tcu.cs.projectpulse.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String role;
    private boolean enabled;
    private Long teamId;
    private String teamName;
    private String sectionName;
}
