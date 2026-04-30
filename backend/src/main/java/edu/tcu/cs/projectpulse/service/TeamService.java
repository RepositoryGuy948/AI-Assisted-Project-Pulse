package edu.tcu.cs.projectpulse.service;

import edu.tcu.cs.projectpulse.dto.TeamDto;
import edu.tcu.cs.projectpulse.model.Section;
import edu.tcu.cs.projectpulse.model.Team;
import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.repository.PeerEvaluationRepository;
import edu.tcu.cs.projectpulse.repository.SectionRepository;
import edu.tcu.cs.projectpulse.repository.TeamRepository;
import edu.tcu.cs.projectpulse.repository.UserRepository;
import edu.tcu.cs.projectpulse.repository.WeeklyActivityReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final WeeklyActivityReportRepository warRepository;
    private final PeerEvaluationRepository peerEvaluationRepository;
    private final EmailService emailService;

    public Team createTeam(TeamDto dto) {
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new RuntimeException("Section not found"));
        if (teamRepository.existsByNameAndSectionId(dto.getName(), dto.getSectionId())) {
            throw new IllegalArgumentException("Team '" + dto.getName() + "' already exists in this section.");
        }
        Team team = Team.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .websiteUrl(dto.getWebsiteUrl())
                .section(section)
                .build();
        return teamRepository.save(team);
    }

    public Team updateTeam(Long id, TeamDto dto) {
        Team team = getById(id);
        if (!team.getName().equals(dto.getName()) &&
            teamRepository.existsByNameAndSectionId(dto.getName(), team.getSection().getId())) {
            throw new IllegalArgumentException("Team '" + dto.getName() + "' already exists in this section.");
        }
        team.setName(dto.getName());
        team.setDescription(dto.getDescription());
        team.setWebsiteUrl(dto.getWebsiteUrl());
        return teamRepository.save(team);
    }

    public Team getById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found: " + id));
    }

    public List<Team> searchTeams(Long sectionId, String sectionName, String teamName, String instructor) {
        return teamRepository.searchTeams(sectionId, sectionName, teamName, instructor)
                .stream()
                .sorted(Comparator
                        .comparing((Team t) -> t.getSection() != null ? t.getSection().getName() : "",
                                Comparator.reverseOrder())
                        .thenComparing(t -> t.getName().toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }

    public void deleteTeam(Long id) {
        Team team = getById(id);
        String teamName = team.getName();
        String sectionName = team.getSection().getName();

        List<String> studentEmails = team.getStudents().stream()
                .map(User::getEmail).collect(Collectors.toList());
        List<String> instructorEmails = team.getInstructors().stream()
                .map(User::getEmail).collect(Collectors.toList());
        List<Long> studentIds = team.getStudents().stream()
                .map(User::getId).collect(Collectors.toList());

        // Delete WARs for all students on the team (cascades to Activities)
        studentIds.forEach(sid ->
                warRepository.deleteAll(warRepository.findByStudentId(sid)));

        // Delete peer evaluations for the team (cascades to PeerEvaluationScores)
        peerEvaluationRepository.deleteAll(peerEvaluationRepository.findByTeamId(id));

        // Remove students from team
        team.getStudents().forEach(s -> s.setTeam(null));
        userRepository.saveAll(team.getStudents());

        // Clear instructors (removes join table rows)
        team.getInstructors().clear();
        teamRepository.save(team);

        teamRepository.delete(team);

        String body = "The team \"" + teamName + "\" in section " + sectionName +
                " has been deleted by an administrator.\n\n" +
                "Please log in to Project Pulse for more information.\n\n" +
                "Best regards,\nProject Pulse Team";
        studentEmails.forEach(email ->
                emailService.sendEmail(email, "Team deleted - Project Pulse", body));
        instructorEmails.forEach(email ->
                emailService.sendEmail(email, "Team deleted - Project Pulse", body));
    }

    public void assignStudentsToTeam(Long teamId, List<Long> studentIds) {
        Team team = getById(teamId);
        String teamName = team.getName();
        String sectionName = team.getSection().getName();
        studentIds.forEach(sid -> {
            User student = userRepository.findById(sid)
                    .orElseThrow(() -> new RuntimeException("Student not found: " + sid));
            student.setTeam(team);
            userRepository.save(student);
            String body = "You have been assigned to team \"" + teamName + "\" in section " + sectionName + ".\n\n" +
                    "Please log in to Project Pulse to view your team details.\n\n" +
                    "Best regards,\nProject Pulse Team";
            emailService.sendEmail(student.getEmail(), "You've been assigned to a team - Project Pulse", body);
        });
    }

    public void removeStudentFromTeam(Long teamId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        if (student.getTeam() == null || !student.getTeam().getId().equals(teamId)) {
            throw new IllegalStateException("Student is not assigned to this team.");
        }
        String teamName = student.getTeam().getName();
        String sectionName = student.getTeam().getSection() != null
                ? student.getTeam().getSection().getName() : "";
        student.setTeam(null);
        userRepository.save(student);
        if (!teamName.isEmpty()) {
            String body = "You have been removed from team \"" + teamName + "\" in section " + sectionName + ".\n\n" +
                    "Please log in to Project Pulse for more information.\n\n" +
                    "Best regards,\nProject Pulse Team";
            emailService.sendEmail(student.getEmail(), "Team assignment update - Project Pulse", body);
        }
    }

    public void assignInstructorsToTeam(Long teamId, List<Long> instructorIds) {
        Team team = getById(teamId);
        String teamName = team.getName();
        String sectionName = team.getSection().getName();
        instructorIds.forEach(iid -> {
            User instructor = userRepository.findById(iid)
                    .orElseThrow(() -> new RuntimeException("Instructor not found: " + iid));
            team.getInstructors().add(instructor);
            String body = "You have been assigned as an instructor for team \"" + teamName + "\" in section " + sectionName + ".\n\n" +
                    "Please log in to Project Pulse to view your team details.\n\n" +
                    "Best regards,\nProject Pulse Team";
            emailService.sendEmail(instructor.getEmail(), "You've been assigned to a team - Project Pulse", body);
        });
        teamRepository.save(team);
    }

    public void removeInstructorFromTeam(Long teamId, Long instructorId) {
        Team team = getById(teamId);
        if (team.getInstructors().size() <= 1) {
            throw new IllegalStateException("Cannot remove the last instructor from a team.");
        }
        User instructor = team.getInstructors().stream()
                .filter(i -> i.getId().equals(instructorId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Instructor not found on team: " + instructorId));
        String teamName = team.getName();
        String sectionName = team.getSection().getName();
        team.getInstructors().removeIf(i -> i.getId().equals(instructorId));
        teamRepository.save(team);
        String body = "You have been removed as an instructor from team \"" + teamName + "\" in section " + sectionName + ".\n\n" +
                "Please log in to Project Pulse for more information.\n\n" +
                "Best regards,\nProject Pulse Team";
        emailService.sendEmail(instructor.getEmail(), "Team assignment update - Project Pulse", body);
    }
}
