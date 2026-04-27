package edu.tcu.cs.projectpulse.controller;

import edu.tcu.cs.projectpulse.dto.TeamDto;
import edu.tcu.cs.projectpulse.model.Team;
import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.service.TeamService;
import edu.tcu.cs.projectpulse.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;

    // UC-7: find teams with optional filters
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<Map<String, Object>>> getTeams(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String sectionName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) String instructor) {
        return ResponseEntity.ok(teamService.searchTeams(sectionId, sectionName, teamName, instructor)
                .stream().map(this::toDetailMap).collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createTeam(@Valid @RequestBody TeamDto dto) {
        return ResponseEntity.ok(toDetailMap(teamService.createTeam(dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getTeam(@PathVariable Long id) {
        return ResponseEntity.ok(toDetailMap(teamService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateTeam(@PathVariable Long id,
                                                           @Valid @RequestBody TeamDto dto) {
        return ResponseEntity.ok(toDetailMap(teamService.updateTeam(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignStudents(@PathVariable Long id,
                                                @RequestBody Map<String, List<Long>> body) {
        teamService.assignStudentsToTeam(id, body.get("studentIds"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeStudent(@PathVariable Long id, @PathVariable Long studentId) {
        teamService.removeStudentFromTeam(id, studentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignInstructors(@PathVariable Long id,
                                                   @RequestBody Map<String, List<Long>> body) {
        teamService.assignInstructorsToTeam(id, body.get("instructorIds"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/instructors/{instructorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeInstructor(@PathVariable Long id, @PathVariable Long instructorId) {
        teamService.removeInstructorFromTeam(id, instructorId);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toDetailMap(Team t) {
        return Map.of(
            "id", t.getId(),
            "name", t.getName(),
            "description", t.getDescription() != null ? t.getDescription() : "",
            "websiteUrl", t.getWebsiteUrl() != null ? t.getWebsiteUrl() : "",
            "sectionId", t.getSection().getId(),
            "sectionName", t.getSection().getName(),
            "students", t.getStudents().stream().map(s -> Map.of(
                "id", s.getId(), "firstName", s.getFirstName(), "lastName", s.getLastName(), "email", s.getEmail()
            )).collect(Collectors.toList()),
            "instructors", t.getInstructors().stream().map(i -> Map.of(
                "id", i.getId(), "firstName", i.getFirstName(), "lastName", i.getLastName(), "email", i.getEmail()
            )).collect(Collectors.toList())
        );
    }
}
