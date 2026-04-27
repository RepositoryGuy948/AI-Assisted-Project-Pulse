package edu.tcu.cs.projectpulse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private Long sectionId;
    private String sectionName;
    private List<TeamSummary> supervisedTeams;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamSummary {
        private Long id;
        private String name;
        private String sectionName;
    }
}
