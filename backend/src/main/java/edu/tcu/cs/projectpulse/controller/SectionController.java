package edu.tcu.cs.projectpulse.controller;

import edu.tcu.cs.projectpulse.dto.ActiveWeekDto;
import edu.tcu.cs.projectpulse.dto.InviteRequest;
import edu.tcu.cs.projectpulse.dto.SectionDto;
import edu.tcu.cs.projectpulse.dto.TeamDto;
import edu.tcu.cs.projectpulse.model.ActiveWeek;
import edu.tcu.cs.projectpulse.model.Section;
import edu.tcu.cs.projectpulse.model.Team;
import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.service.SectionService;
import edu.tcu.cs.projectpulse.service.TeamService;
import edu.tcu.cs.projectpulse.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;
    private final UserService userService;
    private final TeamService teamService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<SectionDto>> getSections(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(sectionService.searchSections(name).stream()
                .map(this::toDto).collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionDto> createSection(@Valid @RequestBody SectionDto dto) {
        return ResponseEntity.ok(toDto(sectionService.createSection(dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getSection(@PathVariable Long id) {
        Section section = sectionService.getById(id);
        return ResponseEntity.ok(toDetailMap(section));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionDto> updateSection(@PathVariable Long id,
                                                     @Valid @RequestBody SectionDto dto) {
        return ResponseEntity.ok(toDto(sectionService.updateSection(id, dto)));
    }

    @GetMapping("/{id}/active-weeks")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<ActiveWeekDto>> getActiveWeeks(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.getActiveWeeks(id).stream()
                .map(w -> weekToDto(w, id)).collect(Collectors.toList()));
    }

    @PutMapping("/{id}/active-weeks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateActiveWeeks(@PathVariable Long id,
                                                   @RequestBody ActiveWeekDto.BulkUpdateRequest request) {
        sectionService.updateActiveWeeks(id, request);
        return ResponseEntity.ok().build();
    }

    // UC-9: Create a team scoped to a section
    @PostMapping("/{id}/teams")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createTeamInSection(
            @PathVariable Long id,
            @Valid @RequestBody TeamDto dto) {
        dto.setSectionId(id);
        Team team = teamService.createTeam(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(teamToMap(team));
    }

    @PostMapping("/{id}/invite-students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> inviteStudents(@PathVariable Long id,
                                                               @Valid @RequestBody InviteRequest request) {
        int count = userService.inviteUsers(id, request, User.Role.STUDENT);
        return ResponseEntity.ok(Map.of("message", "Invitations sent.", "count", count));
    }

    @PostMapping("/{id}/invite-instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> inviteInstructors(@PathVariable Long id,
                                                                   @Valid @RequestBody InviteRequest request) {
        int count = userService.inviteUsers(id, request, User.Role.INSTRUCTOR);
        return ResponseEntity.ok(Map.of("message", "Invitations sent.", "count", count));
    }

    private SectionDto toDto(Section s) {
        SectionDto dto = new SectionDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setStartDate(s.getStartDate());
        dto.setEndDate(s.getEndDate());
        if (s.getRubric() != null) dto.setRubricId(s.getRubric().getId());
        return dto;
    }

    private ActiveWeekDto weekToDto(ActiveWeek w, Long sectionId) {
        ActiveWeekDto dto = new ActiveWeekDto();
        dto.setId(w.getId());
        dto.setStartDate(w.getStartDate());
        dto.setEndDate(w.getEndDate());
        dto.setActive(w.isActive());
        dto.setSectionId(sectionId);
        return dto;
    }

    private Map<String, Object> teamToMap(Team t) {
        return Map.of(
            "id", t.getId(),
            "name", t.getName(),
            "description", t.getDescription() != null ? t.getDescription() : "",
            "websiteUrl", t.getWebsiteUrl() != null ? t.getWebsiteUrl() : "",
            "sectionId", t.getSection().getId(),
            "sectionName", t.getSection().getName()
        );
    }

    private Map<String, Object> toDetailMap(Section section) {
        // Students assigned to teams in this section
        Set<Long> assignedStudentIds = section.getTeams().stream()
                .flatMap(t -> t.getStudents().stream())
                .map(User::getId)
                .collect(Collectors.toSet());

        // Instructors assigned to teams in this section
        Set<Long> assignedInstructorIds = section.getTeams().stream()
                .flatMap(t -> t.getInstructors().stream())
                .map(User::getId)
                .collect(Collectors.toSet());

        // All students in section (those whose team is in this section)
        List<User> allStudents = userService.searchStudents(null, null, null, null, section.getId());
        List<User> unassignedStudents = allStudents.stream()
                .filter(s -> !assignedStudentIds.contains(s.getId()))
                .collect(Collectors.toList());

        // All instructors in section (those assigned to any team in this section)
        List<User> allInstructors = section.getTeams().stream()
                .flatMap(t -> t.getInstructors().stream())
                .distinct()
                .collect(Collectors.toList());
        List<User> unassignedInstructors = allInstructors.stream()
                .filter(i -> !assignedInstructorIds.contains(i.getId()))
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", section.getId());
        result.put("name", section.getName());
        result.put("startDate", section.getStartDate());
        result.put("endDate", section.getEndDate());
        result.put("rubricId", section.getRubric() != null ? section.getRubric().getId() : null);
        result.put("rubricName", section.getRubric() != null ? section.getRubric().getName() : null);
        result.put("rubricCriteria", section.getRubric() != null
                ? section.getRubric().getCriteria().stream().map(c -> Map.of(
                        "id", c.getId(), "name", c.getName(), "description", c.getDescription() != null ? c.getDescription() : "", "maxScore", c.getMaxScore()
                  )).collect(Collectors.toList())
                : List.of());
        result.put("teams", section.getTeams().stream().map(t -> Map.of(
                "id", t.getId(), "name", t.getName(),
                "students", t.getStudents().stream().map(s -> Map.of(
                        "id", s.getId(), "firstName", s.getFirstName(), "lastName", s.getLastName()
                )).collect(Collectors.toList()),
                "instructors", t.getInstructors().stream().map(i -> Map.of(
                        "id", i.getId(), "firstName", i.getFirstName(), "lastName", i.getLastName()
                )).collect(Collectors.toList())
        )).collect(Collectors.toList()));
        result.put("unassignedStudents", unassignedStudents.stream().map(s -> Map.of(
                "id", s.getId(), "firstName", s.getFirstName(), "lastName", s.getLastName(), "email", s.getEmail()
        )).collect(Collectors.toList()));
        result.put("unassignedInstructors", unassignedInstructors.stream().map(i -> Map.of(
                "id", i.getId(), "firstName", i.getFirstName(), "lastName", i.getLastName(), "email", i.getEmail()
        )).collect(Collectors.toList()));
        return result;
    }
}
