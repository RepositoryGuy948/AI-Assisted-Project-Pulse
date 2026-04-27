package edu.tcu.cs.projectpulse.controller;

import edu.tcu.cs.projectpulse.dto.ActivityDto;
import edu.tcu.cs.projectpulse.model.Activity;
import edu.tcu.cs.projectpulse.model.WeeklyActivityReport;
import edu.tcu.cs.projectpulse.service.WARService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WARController {

    private final WARService warService;

    @GetMapping("/students/{studentId}/wars")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<Map<String, Object>>> getStudentWARs(@PathVariable Long studentId) {
        return ResponseEntity.ok(warService.getStudentWARs(studentId)
                .stream().map(this::warToMap).collect(Collectors.toList()));
    }

    @GetMapping("/students/{studentId}/wars/{weekId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getWAR(@PathVariable Long studentId,
                                                       @PathVariable Long weekId) {
        WeeklyActivityReport war = warService.getWARByStudentAndWeek(studentId, weekId);
        if (war == null) return ResponseEntity.ok(Map.of("activities", List.of()));
        return ResponseEntity.ok(warToMap(war));
    }

    @PostMapping("/students/{studentId}/wars/{weekId}/activities")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ActivityDto> addActivity(@PathVariable Long studentId,
                                                    @PathVariable Long weekId,
                                                    @Valid @RequestBody ActivityDto dto) {
        return ResponseEntity.ok(warService.toDto(warService.addActivity(studentId, weekId, dto)));
    }

    @PutMapping("/activities/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ActivityDto> updateActivity(@PathVariable Long id,
                                                       @Valid @RequestBody ActivityDto dto) {
        return ResponseEntity.ok(warService.toDto(warService.updateActivity(id, dto)));
    }

    @DeleteMapping("/activities/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        warService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }

    // Team WAR report (UC-32) — Instructor and Student can both generate
    @GetMapping("/teams/{teamId}/war-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<Map<String, Object>>> getTeamWARReport(@PathVariable Long teamId,
                                                                       @RequestParam Long weekId) {
        return ResponseEntity.ok(warService.getTeamWARs(teamId, weekId)
                .stream().map(this::warToMap).collect(Collectors.toList()));
    }

    // Individual student WAR report (UC-34)
    @GetMapping("/students/{studentId}/war-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<Map<String, Object>>> getStudentWARReport(
            @PathVariable Long studentId,
            @RequestParam List<Long> weekIds) {
        return ResponseEntity.ok(warService.getStudentWARsForPeriod(studentId, weekIds)
                .stream().map(this::warToMap).collect(Collectors.toList()));
    }

    private Map<String, Object> warToMap(WeeklyActivityReport war) {
        return Map.of(
            "id", war.getId(),
            "studentId", war.getStudent().getId(),
            "studentFirstName", war.getStudent().getFirstName(),
            "studentLastName", war.getStudent().getLastName(),
            "weekId", war.getActiveWeek().getId(),
            "weekStartDate", war.getActiveWeek().getStartDate().toString(),
            "activities", war.getActivities().stream().map(warService::toDto).collect(Collectors.toList())
        );
    }
}
