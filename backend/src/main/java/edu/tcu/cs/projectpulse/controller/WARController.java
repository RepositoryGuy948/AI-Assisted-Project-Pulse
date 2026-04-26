package edu.tcu.cs.projectpulse.controller;

import edu.tcu.cs.projectpulse.dto.ActivityDto;
import edu.tcu.cs.projectpulse.model.Activity;
import edu.tcu.cs.projectpulse.model.Team;
import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.model.WeeklyActivityReport;
import edu.tcu.cs.projectpulse.service.TeamService;
import edu.tcu.cs.projectpulse.service.WARService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WARController {

    private final WARService warService;
    private final TeamService teamService;

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

    // Team WAR report (UC-32) — students can view their own team's report
    @GetMapping("/teams/{teamId}/war-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<Map<String, Object>>> getTeamWARReport(@PathVariable Long teamId,
                                                                       @RequestParam Long weekId) {
        Team team = teamService.getById(teamId);
        List<WeeklyActivityReport> submitted = warService.getTeamWARs(teamId, weekId);
        Map<Long, WeeklyActivityReport> byStudent = submitted.stream()
                .collect(Collectors.toMap(w -> w.getStudent().getId(), w -> w));

        List<Map<String, Object>> result = team.getStudents().stream()
                .sorted(Comparator.comparing(User::getLastName))
                .map(student -> {
                    WeeklyActivityReport war = byStudent.get(student.getId());
                    if (war != null) return warToMap(war);
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("submitted", false);
                    row.put("studentId", student.getId());
                    row.put("studentFirstName", student.getFirstName());
                    row.put("studentLastName", student.getLastName());
                    row.put("weekId", weekId);
                    row.put("activities", List.of());
                    return row;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Individual student WAR report (UC-34) — instructor/admin only per spec
    @GetMapping("/students/{studentId}/war-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<Map<String, Object>>> getStudentWARReport(
            @PathVariable Long studentId,
            @RequestParam List<Long> weekIds) {
        return ResponseEntity.ok(warService.getStudentWARsForPeriod(studentId, weekIds)
                .stream().map(this::warToMap).collect(Collectors.toList()));
    }

    private Map<String, Object> warToMap(WeeklyActivityReport war) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", war.getId());
        m.put("submitted", true);
        m.put("studentId", war.getStudent().getId());
        m.put("studentFirstName", war.getStudent().getFirstName());
        m.put("studentLastName", war.getStudent().getLastName());
        m.put("weekId", war.getActiveWeek().getId());
        m.put("weekStartDate", war.getActiveWeek().getStartDate().toString());
        m.put("activities", war.getActivities().stream().map(warService::toDto).collect(Collectors.toList()));
        return m;
    }
}
