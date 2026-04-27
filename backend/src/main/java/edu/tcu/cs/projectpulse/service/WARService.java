package edu.tcu.cs.projectpulse.service;

import edu.tcu.cs.projectpulse.dto.ActivityDto;
import edu.tcu.cs.projectpulse.model.Activity;
import edu.tcu.cs.projectpulse.model.ActiveWeek;
import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.model.WeeklyActivityReport;
import edu.tcu.cs.projectpulse.repository.ActiveWeekRepository;
import edu.tcu.cs.projectpulse.repository.ActivityRepository;
import edu.tcu.cs.projectpulse.repository.UserRepository;
import edu.tcu.cs.projectpulse.repository.WeeklyActivityReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WARService {

    private final WeeklyActivityReportRepository warRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActiveWeekRepository activeWeekRepository;

    public WeeklyActivityReport getOrCreateWAR(Long studentId, Long weekId) {
        return warRepository.findByStudentIdAndActiveWeekId(studentId, weekId)
                .orElseGet(() -> {
                    User student = userRepository.findById(studentId)
                            .orElseThrow(() -> new RuntimeException("Student not found"));
                    ActiveWeek week = activeWeekRepository.findById(weekId)
                            .orElseThrow(() -> new RuntimeException("Week not found"));
                    WeeklyActivityReport war = WeeklyActivityReport.builder()
                            .student(student)
                            .activeWeek(week)
                            .build();
                    return warRepository.save(war);
                });
    }

    public Activity addActivity(Long studentId, Long weekId, ActivityDto dto) {
        WeeklyActivityReport war = getOrCreateWAR(studentId, weekId);
        Activity activity = Activity.builder()
                .category(dto.getCategory())
                .description(dto.getDescription())
                .plannedHours(dto.getPlannedHours())
                .actualHours(dto.getActualHours())
                .status(dto.getStatus())
                .war(war)
                .build();
        return activityRepository.save(activity);
    }

    public Activity updateActivity(Long activityId, ActivityDto dto) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        activity.setCategory(dto.getCategory());
        activity.setDescription(dto.getDescription());
        activity.setPlannedHours(dto.getPlannedHours());
        activity.setActualHours(dto.getActualHours());
        activity.setStatus(dto.getStatus());
        return activityRepository.save(activity);
    }

    public void deleteActivity(Long activityId) {
        activityRepository.deleteById(activityId);
    }

    public List<WeeklyActivityReport> getStudentWARs(Long studentId) {
        return warRepository.findByStudentId(studentId);
    }

    public WeeklyActivityReport getWARByStudentAndWeek(Long studentId, Long weekId) {
        return warRepository.findByStudentIdAndActiveWeekId(studentId, weekId)
                .orElse(null);
    }

    public List<WeeklyActivityReport> getTeamWARs(Long teamId, Long weekId) {
        return warRepository.findByTeamAndWeek(teamId, weekId);
    }

    public List<WeeklyActivityReport> getStudentWARsForPeriod(Long studentId, List<Long> weekIds) {
        return warRepository.findByStudentAndWeeks(studentId, weekIds);
    }

    // Cascade delete helpers (used by Member 3 when removing a student or team)
    public void deleteAllWarsByStudent(Long studentId) {
        warRepository.deleteAll(warRepository.findByStudentId(studentId));
    }

    public void deleteAllWarsByTeam(Long teamId) {
        warRepository.deleteAll(warRepository.findByTeamId(teamId));
    }

    public ActivityDto toDto(Activity a) {
        ActivityDto dto = new ActivityDto();
        dto.setId(a.getId());
        dto.setCategory(a.getCategory());
        dto.setDescription(a.getDescription());
        dto.setPlannedHours(a.getPlannedHours());
        dto.setActualHours(a.getActualHours());
        dto.setStatus(a.getStatus());
        dto.setWarId(a.getWar().getId());
        return dto;
    }
}
