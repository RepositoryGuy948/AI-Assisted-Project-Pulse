package edu.tcu.cs.projectpulse.controller;

import edu.tcu.cs.projectpulse.dto.ActiveWeekDto;
import edu.tcu.cs.projectpulse.dto.InviteRequest;
import edu.tcu.cs.projectpulse.dto.SectionDto;
import edu.tcu.cs.projectpulse.model.ActiveWeek;
import edu.tcu.cs.projectpulse.model.Section;
import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.service.SectionService;
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
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;
    private final UserService userService;

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
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
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
                .map(this::weekToDto).collect(Collectors.toList()));
    }

    @PutMapping("/{id}/active-weeks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateActiveWeeks(@PathVariable Long id,
                                                   @RequestBody ActiveWeekDto.BulkUpdateRequest request) {
        sectionService.updateActiveWeeks(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/invite-students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> inviteStudents(@PathVariable Long id,
                                                               @Valid @RequestBody InviteRequest request) {
        userService.inviteUsers(id, request, User.Role.STUDENT);
        return ResponseEntity.ok(Map.of("message", "Invitations sent."));
    }

    @PostMapping("/{id}/invite-instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> inviteInstructors(@PathVariable Long id,
                                                                   @Valid @RequestBody InviteRequest request) {
        userService.inviteUsers(id, request, User.Role.INSTRUCTOR);
        return ResponseEntity.ok(Map.of("message", "Invitations sent."));
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

    private ActiveWeekDto weekToDto(ActiveWeek w) {
        ActiveWeekDto dto = new ActiveWeekDto();
        dto.setId(w.getId());
        dto.setStartDate(w.getStartDate());
        dto.setEndDate(w.getEndDate());
        dto.setActive(w.isActive());
        dto.setSectionId(w.getSection().getId());
        return dto;
    }

    private Map<String, Object> toDetailMap(Section section) {
        return Map.of(
            "id", section.getId(),
            "name", section.getName(),
            "startDate", section.getStartDate(),
            "endDate", section.getEndDate(),
            "rubricId", section.getRubric() != null ? section.getRubric().getId() : null,
            "rubricName", section.getRubric() != null ? section.getRubric().getName() : null,
            "teams", section.getTeams().stream().map(t -> Map.of(
                "id", t.getId(), "name", t.getName()
            )).collect(Collectors.toList())
        );
    }
}
